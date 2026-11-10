-- Retained usage may exceed a lower allowance; capacity writers still compare used+reserved to allowance atomically.
ALTER TABLE workspace_grant DROP CONSTRAINT workspace_grant_check;
CREATE TABLE subscription_downgrade_review(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),actor_id uuid NOT NULL REFERENCES user_account(id),snapshot jsonb NOT NULL,expires_at timestamptz NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(workspace_id,id));
CREATE TABLE subscription_downgrade_request(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),actor_id uuid NOT NULL REFERENCES user_account(id),review_id uuid UNIQUE NOT NULL,status text NOT NULL CHECK(status IN('scheduled','applied','cancelled','review_required')),effective_at timestamptz NOT NULL,version integer NOT NULL DEFAULT 1,attempts integer NOT NULL DEFAULT 0,lease_token uuid,lease_until timestamptz,next_attempt_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,last_code text,applied_at timestamptz,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(workspace_id,review_id) REFERENCES subscription_downgrade_review(workspace_id,id));
CREATE UNIQUE INDEX one_open_downgrade ON subscription_downgrade_request(workspace_id) WHERE status IN('scheduled','review_required');
CREATE INDEX downgrade_due ON subscription_downgrade_request(next_attempt_at,effective_at,id) WHERE status='scheduled';
CREATE TABLE subscription_downgrade_command(actor_id uuid NOT NULL REFERENCES user_account(id),workspace_id uuid NOT NULL REFERENCES workspace(id),operation text NOT NULL,request_key uuid NOT NULL,request_hash char(64) NOT NULL,result jsonb NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(actor_id,workspace_id,operation,request_key));
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['subscription_downgrade_review','subscription_downgrade_request','subscription_downgrade_command'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY downgrade_owner_scope ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);
 EXECUTE format('GRANT SELECT,INSERT ON %I TO lmt_runtime',t);
 END LOOP;
END $$;
GRANT UPDATE(status,version,attempts,lease_token,lease_until,next_attempt_at,last_code,applied_at) ON subscription_downgrade_request TO lmt_runtime;
CREATE TRIGGER immutable_downgrade_review BEFORE UPDATE OR DELETE ON subscription_downgrade_review FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
-- Bounded worker discovery only; no private business evidence is returned.
CREATE FUNCTION lmt_claim_due_downgrades() RETURNS TABLE(id uuid,workspace_id uuid,actor_id uuid,token uuid) LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
 WITH exhausted AS (UPDATE public.subscription_downgrade_request d SET status='review_required',version=d.version+1,last_code='JOB_FAILED',lease_token=NULL,lease_until=NULL WHERE d.status='scheduled' AND d.attempts>=5 AND d.lease_until<clock_timestamp() RETURNING d.id,d.workspace_id),
 logged AS (INSERT INTO public.domain_audit(id,scope_id,action,resource_id) SELECT gen_random_uuid(),e.workspace_id,'subscription.downgrade_retry_exhausted',e.id FROM exhausted e RETURNING id),
 candidates AS (SELECT d.id FROM public.subscription_downgrade_request d WHERE d.status='scheduled' AND d.attempts<5 AND d.effective_at<=clock_timestamp() AND d.next_attempt_at<=clock_timestamp() AND (d.lease_until IS NULL OR d.lease_until<clock_timestamp()) ORDER BY d.effective_at,d.id FOR UPDATE SKIP LOCKED LIMIT 25)
 UPDATE public.subscription_downgrade_request d SET lease_token=gen_random_uuid(),lease_until=clock_timestamp()+interval '60 seconds',attempts=d.attempts+1 FROM candidates c WHERE d.id=c.id RETURNING d.id,d.workspace_id,d.actor_id,d.lease_token;
$$;
CREATE FUNCTION lmt_fail_downgrade(p_id uuid,p_token uuid,p_code text) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE scope uuid;
BEGIN
 IF p_code NOT IN('JOB_FAILED','AUTHORITY_REVOKED','SUBSCRIPTION_CHANGED','TARGET_UNAVAILABLE','POLICY_CHANGED','MODE_UNAVAILABLE','SECURITY_SUSPENDED') THEN RAISE EXCEPTION 'Invalid recovery code'; END IF;
 UPDATE public.subscription_downgrade_request d SET status=CASE WHEN p_code='JOB_FAILED' AND d.attempts<5 THEN 'scheduled' ELSE 'review_required' END,version=d.version+1,lease_token=NULL,lease_until=NULL,last_code=p_code,next_attempt_at=clock_timestamp()+least(300,power(2,least(d.attempts,8))::integer)*interval '1 second' WHERE d.id=p_id AND d.lease_token=p_token AND d.status='scheduled' RETURNING d.workspace_id INTO scope;
 IF scope IS NOT NULL THEN INSERT INTO public.domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),scope,'subscription.downgrade_recovery',p_id); END IF;
END $$;
REVOKE ALL ON FUNCTION lmt_claim_due_downgrades(),lmt_fail_downgrade(uuid,uuid,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_claim_due_downgrades(),lmt_fail_downgrade(uuid,uuid,text) TO lmt_runtime;

ALTER TABLE account_notification DROP CONSTRAINT account_notification_category_check;
ALTER TABLE account_notification ADD CONSTRAINT account_notification_category_check CHECK(category IN('security','workspace_setup','workspace_access','workspace_invitation','workspace_subscription'));
