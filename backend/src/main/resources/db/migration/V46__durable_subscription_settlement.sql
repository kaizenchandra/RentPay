-- Receipt evidence commits before entitlement activation; all outcomes remain explicitly sandbox-only.
CREATE TABLE subscription_settlement(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,actor_id uuid NOT NULL REFERENCES user_account(id),
 status text NOT NULL CHECK(status IN('queued','applied','review_required','reversal_review')),
 version integer NOT NULL DEFAULT 1,received_at timestamptz NOT NULL DEFAULT clock_timestamp(),applied_at timestamptz,reversed_at timestamptz,
 attempts integer NOT NULL DEFAULT 0,lease_token uuid,lease_until timestamptz,next_attempt_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,last_code text,
 FOREIGN KEY(workspace_id,id) REFERENCES subscription_upgrade_change(workspace_id,id)
);
CREATE TABLE subscription_callback_event(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,change_id uuid NOT NULL,actor_id uuid NOT NULL REFERENCES user_account(id),
 event_key uuid NOT NULL,outcome text NOT NULL CHECK(outcome IN('pending','paid','reversed')),result jsonb NOT NULL,received_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(workspace_id,change_id) REFERENCES subscription_upgrade_change(workspace_id,id),UNIQUE(workspace_id,change_id,event_key)
);
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['subscription_settlement','subscription_callback_event'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY settlement_owner_scope ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);
 EXECUTE format('GRANT SELECT,INSERT ON %I TO lmt_runtime',t);
 END LOOP;
END $$;
CREATE TRIGGER immutable_subscription_callback BEFORE UPDATE OR DELETE ON subscription_callback_event FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
GRANT UPDATE(status,version,applied_at,reversed_at,attempts,lease_token,lease_until,next_attempt_at,last_code) ON subscription_settlement TO lmt_runtime;
CREATE INDEX settlement_due ON subscription_settlement(next_attempt_at,id) WHERE status='queued';
CREATE FUNCTION lmt_claim_subscription_settlements() RETURNS TABLE(id uuid,workspace_id uuid,actor_id uuid,token uuid) LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
 WITH exhausted AS (UPDATE public.subscription_settlement d SET status='review_required',version=d.version+1,last_code='JOB_FAILED',lease_token=NULL,lease_until=NULL WHERE d.status='queued' AND d.attempts>=5 AND d.lease_until<clock_timestamp() RETURNING d.id,d.workspace_id),
 logged AS (INSERT INTO public.domain_audit(id,scope_id,action,resource_id) SELECT gen_random_uuid(),e.workspace_id,'subscription.settlement_retry_exhausted',e.id FROM exhausted e RETURNING id),
 candidates AS (SELECT d.id FROM public.subscription_settlement d WHERE d.status='queued' AND d.attempts<5 AND d.next_attempt_at<=clock_timestamp() AND (d.lease_until IS NULL OR d.lease_until<clock_timestamp()) ORDER BY d.received_at,d.id FOR UPDATE SKIP LOCKED LIMIT 25)
 UPDATE public.subscription_settlement d SET lease_token=gen_random_uuid(),lease_until=clock_timestamp()+interval '60 seconds',attempts=d.attempts+1 FROM candidates c WHERE d.id=c.id RETURNING d.id,d.workspace_id,d.actor_id,d.lease_token;
$$;
CREATE FUNCTION lmt_fail_subscription_settlement(p_id uuid,p_token uuid,p_code text) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE scope uuid;
BEGIN
 IF p_code NOT IN('JOB_FAILED','AUTHORITY_REVOKED','SUBSCRIPTION_CHANGED','TARGET_UNAVAILABLE','POLICY_CHANGED','MODE_UNAVAILABLE','SECURITY_SUSPENDED','ACTIVATION_REVIEW_REQUIRED') THEN RAISE EXCEPTION 'Invalid recovery code'; END IF;
 UPDATE public.subscription_settlement d SET status=CASE WHEN p_code='JOB_FAILED' AND d.attempts<5 THEN 'queued' ELSE 'review_required' END,version=d.version+1,lease_token=NULL,lease_until=NULL,last_code=p_code,next_attempt_at=clock_timestamp()+least(300,power(2,least(d.attempts,8))::integer)*interval '1 second' WHERE d.id=p_id AND d.lease_token=p_token AND d.status='queued' RETURNING d.workspace_id INTO scope;
 IF scope IS NOT NULL THEN INSERT INTO public.domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),scope,'subscription.settlement_recovery',p_id); END IF;
END $$;
REVOKE ALL ON FUNCTION lmt_claim_subscription_settlements(),lmt_fail_subscription_settlement(uuid,uuid,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_claim_subscription_settlements(),lmt_fail_subscription_settlement(uuid,uuid,text) TO lmt_runtime;

