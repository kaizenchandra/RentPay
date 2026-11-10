ALTER TABLE communication_operation ADD COLUMN recipient_fingerprint char(64);
CREATE TABLE communication_reconciliation(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,operation_id uuid NOT NULL,actor_id uuid NOT NULL REFERENCES user_account(id),request_key uuid NOT NULL,request_hash char(64) NOT NULL,
 reason varchar(300) NOT NULL,status text NOT NULL CHECK(status IN('queued','checking','matched','no_evidence','failed')),
 attempts integer NOT NULL DEFAULT 0,lease_token uuid,lease_until timestamptz,next_attempt_at timestamptz NOT NULL DEFAULT clock_timestamp(),last_code text,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),completed_at timestamptz,
 FOREIGN KEY(workspace_id,operation_id) REFERENCES communication_operation(workspace_id,id),UNIQUE(workspace_id,actor_id,request_key)
);
CREATE UNIQUE INDEX communication_one_reconciliation ON communication_reconciliation(operation_id) WHERE status IN('queued','checking');
CREATE INDEX communication_reconciliation_due ON communication_reconciliation(next_attempt_at) WHERE status IN('queued','checking');
ALTER TABLE communication_reconciliation ENABLE ROW LEVEL SECURITY;
CREATE POLICY communication_reconciliation_owner ON communication_reconciliation USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting('lmt.account_id',true),'')::uuid));
GRANT SELECT,INSERT ON communication_reconciliation TO lmt_runtime;
CREATE FUNCTION lmt_claim_communication_reconciliations() RETURNS TABLE(id uuid,workspace_id uuid,actor_id uuid,token uuid) LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
 WITH exhausted AS(UPDATE public.communication_reconciliation SET status='failed',last_code='RETRY_EXHAUSTED',completed_at=clock_timestamp() WHERE status='checking' AND lease_until<clock_timestamp() AND attempts>=5 RETURNING id,workspace_id),
 logged AS(INSERT INTO public.domain_audit(id,scope_id,action,resource_id) SELECT gen_random_uuid(),workspace_id,'communication.reconciliation_exhausted',id FROM exhausted RETURNING id),
 candidate AS(SELECT id FROM public.communication_reconciliation WHERE attempts<5 AND ((status='queued' AND next_attempt_at<=clock_timestamp()) OR (status='checking' AND lease_until<clock_timestamp())) ORDER BY created_at,id LIMIT 1 FOR UPDATE SKIP LOCKED)
 UPDATE public.communication_reconciliation r SET status='checking',attempts=attempts+1,lease_token=gen_random_uuid(),lease_until=clock_timestamp()+interval '60 seconds' FROM candidate c WHERE r.id=c.id RETURNING r.id,r.workspace_id,r.actor_id,r.lease_token;
$$;
CREATE FUNCTION lmt_finish_communication_reconciliation(p_id uuid,p_token uuid,p_code text,p_reference text) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE job public.communication_reconciliation; op public.communication_operation; scope uuid; outcome text; BEGIN
 IF p_code NOT IN('MATCHED','EVIDENCE_NOT_FOUND','EVIDENCE_AMBIGUOUS','REFERENCE_UNAVAILABLE','AUTHORITY_REVOKED','MODE_UNAVAILABLE','PROVIDER_UNAVAILABLE','OPERATION_CHANGED') THEN RAISE EXCEPTION 'Invalid reconciliation outcome'; END IF;
 SELECT workspace_id INTO scope FROM public.communication_reconciliation WHERE id=p_id;
 IF scope IS NULL THEN RETURN; END IF;
 PERFORM pg_advisory_xact_lock(hashtextextended('communication:'||scope,0));
 SELECT * INTO job FROM public.communication_reconciliation WHERE id=p_id AND status='checking' AND lease_token=p_token AND lease_until>clock_timestamp() FOR UPDATE;
 IF NOT FOUND THEN RETURN; END IF;
 IF NOT EXISTS(SELECT 1 FROM public.workspace_member WHERE workspace_id=job.workspace_id AND account_id=job.actor_id AND active AND role='OWNER') THEN p_code='AUTHORITY_REVOKED'; END IF;
 IF NOT EXISTS(SELECT 1 FROM public.workspace WHERE id=job.workspace_id AND mode='sandbox') THEN p_code='MODE_UNAVAILABLE'; END IF;
 SELECT * INTO op FROM public.communication_operation WHERE id=job.operation_id AND workspace_id=job.workspace_id;
 IF p_code='MATCHED' THEN
  IF op.status='unknown' THEN PERFORM public.lmt_finish_communication(op.id,op.lease_token,'accepted',p_reference,NULL);
  ELSIF op.status<>'accepted' THEN p_code='OPERATION_CHANGED'; END IF;
 END IF;
 outcome=CASE WHEN p_code='MATCHED' THEN 'matched' WHEN p_code IN('EVIDENCE_NOT_FOUND','EVIDENCE_AMBIGUOUS') THEN 'no_evidence' WHEN p_code='PROVIDER_UNAVAILABLE' AND job.attempts<5 THEN 'queued' ELSE 'failed' END;
 UPDATE public.communication_reconciliation SET status=outcome,last_code=p_code,completed_at=CASE WHEN outcome='queued' THEN NULL ELSE clock_timestamp() END,lease_token=NULL,lease_until=NULL,next_attempt_at=clock_timestamp()+(power(2,job.attempts)::integer+floor(random()*3)::integer)*interval '1 second' WHERE id=job.id;
 INSERT INTO public.domain_audit(id,scope_id,actor_id,action,resource_id) VALUES(gen_random_uuid(),job.workspace_id,job.actor_id,'communication.reconciliation_'||outcome,job.id);
END $$;
REVOKE ALL ON FUNCTION lmt_claim_communication_reconciliations(),lmt_finish_communication_reconciliation(uuid,uuid,text,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_claim_communication_reconciliations(),lmt_finish_communication_reconciliation(uuid,uuid,text,text) TO lmt_runtime;
