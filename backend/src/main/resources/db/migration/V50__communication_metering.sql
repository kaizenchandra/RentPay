CREATE TABLE communication_contract(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),plan_version_id uuid NOT NULL REFERENCES plan_version(version_id),
 channel text NOT NULL CHECK(channel IN('email','sms')),metric text NOT NULL CHECK(metric='provider_accepted'),
 allowance integer NOT NULL CHECK(allowance BETWEEN 0 AND 100000),starts_at timestamptz NOT NULL,ends_at timestamptz NOT NULL,
 mode text NOT NULL CHECK(mode='sandbox'),reason varchar(300) NOT NULL,created_at timestamptz NOT NULL DEFAULT clock_timestamp(),CHECK(ends_at>starts_at),UNIQUE(workspace_id,id)
);
CREATE FUNCTION guard_communication_contract() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('communication-contract:'||NEW.workspace_id||':'||NEW.channel,0));
 IF NOT EXISTS(SELECT 1 FROM workspace WHERE id=NEW.workspace_id AND mode='sandbox') THEN RAISE EXCEPTION 'Only sandbox contracts are supported'; END IF;
 IF EXISTS(SELECT 1 FROM communication_contract WHERE workspace_id=NEW.workspace_id AND channel=NEW.channel AND starts_at<NEW.ends_at AND ends_at>NEW.starts_at) THEN RAISE EXCEPTION 'Communication periods overlap'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER communication_contract_guard BEFORE INSERT ON communication_contract FOR EACH ROW EXECUTE FUNCTION guard_communication_contract();
CREATE TRIGGER communication_contract_immutable BEFORE UPDATE OR DELETE ON communication_contract FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE FUNCTION audit_communication_contract() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 INSERT INTO domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),NEW.workspace_id,'communication.contract_configured',NEW.id);RETURN NEW;
END $$;
CREATE TRIGGER communication_contract_audit AFTER INSERT ON communication_contract FOR EACH ROW EXECUTE FUNCTION audit_communication_contract();
CREATE TABLE communication_operation(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,contract_id uuid NOT NULL,actor_id uuid NOT NULL REFERENCES user_account(id),request_key uuid NOT NULL,contact_version integer NOT NULL,
 status text NOT NULL CHECK(status IN('queued','dispatching','accepted','failed','unknown','cancelled')),version integer NOT NULL DEFAULT 1,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),completed_at timestamptz,lease_token uuid,lease_until timestamptz,last_code text,
 FOREIGN KEY(workspace_id,contract_id) REFERENCES communication_contract(workspace_id,id),UNIQUE(workspace_id,actor_id,request_key),UNIQUE(workspace_id,id),UNIQUE(workspace_id,id,contract_id)
);
CREATE TABLE communication_usage_event(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,operation_id uuid NOT NULL,contract_id uuid NOT NULL,
 metric text NOT NULL CHECK(metric IN('dispatch_authorized','provider_accepted')),quantity integer NOT NULL CHECK(quantity=1),provider_reference varchar(100),occurred_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(workspace_id,operation_id,contract_id) REFERENCES communication_operation(workspace_id,id,contract_id),FOREIGN KEY(workspace_id,contract_id) REFERENCES communication_contract(workspace_id,id),UNIQUE(operation_id,metric)
);
CREATE TRIGGER communication_event_immutable BEFORE UPDATE OR DELETE ON communication_usage_event FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
DO $$ DECLARE t text; BEGIN FOREACH t IN ARRAY ARRAY['communication_contract','communication_operation','communication_usage_event'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY communication_owner ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);
 EXECUTE format('GRANT SELECT ON %I TO lmt_runtime',t);
 END LOOP;END $$;
GRANT INSERT ON communication_operation TO lmt_runtime;
CREATE INDEX communication_due ON communication_operation(created_at) WHERE status='queued';
CREATE INDEX communication_contract_usage ON communication_operation(contract_id,status);
CREATE INDEX communication_event_contract ON communication_usage_event(contract_id,metric);
CREATE FUNCTION lmt_claim_communication() RETURNS TABLE(id uuid,workspace_id uuid,actor_id uuid,token uuid) LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
 WITH expired AS(UPDATE public.communication_operation SET status='unknown',version=version+1,last_code='PROVIDER_OUTCOME_UNKNOWN' WHERE status='dispatching' AND lease_until<clock_timestamp() RETURNING id,workspace_id),
 logged AS(INSERT INTO public.domain_audit(id,scope_id,action,resource_id) SELECT gen_random_uuid(),workspace_id,'communication.outcome_unknown',id FROM expired RETURNING id),
 candidate AS(SELECT id FROM public.communication_operation WHERE status='queued' ORDER BY created_at,id LIMIT 1 FOR UPDATE SKIP LOCKED)
 UPDATE public.communication_operation o SET status='dispatching',version=version+1,lease_token=gen_random_uuid(),lease_until=clock_timestamp()+interval '60 seconds' FROM candidate c WHERE o.id=c.id RETURNING o.id,o.workspace_id,o.actor_id,o.lease_token;
$$;
CREATE FUNCTION lmt_finish_communication(p_id uuid,p_token uuid,p_status text,p_reference text,p_code text) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE o public.communication_operation; scope uuid; BEGIN
 IF p_status NOT IN('accepted','failed','unknown','cancelled') OR (p_status='accepted' AND (p_reference IS NULL OR p_reference !~ '^[A-Za-z0-9-]{1,100}$')) OR (p_code IS NOT NULL AND p_code NOT IN('PROVIDER_OUTCOME_UNKNOWN','PROVIDER_REJECTED','AUTHORITY_REVOKED','CONTRACT_UNAVAILABLE','CONTACT_UNAVAILABLE')) THEN RAISE EXCEPTION 'Invalid communication outcome'; END IF;
 SELECT workspace_id INTO scope FROM public.communication_operation WHERE id=p_id;
 IF scope IS NULL THEN RETURN; END IF;
 PERFORM pg_advisory_xact_lock(hashtextextended('communication:'||scope,0));
 SELECT * INTO o FROM public.communication_operation WHERE id=p_id AND lease_token=p_token AND status IN('dispatching','unknown') FOR UPDATE;
 IF NOT FOUND THEN RETURN; END IF;
 -- Late evidence may resolve an unknown result; a duplicate result cannot meter twice.
 UPDATE public.communication_operation SET status=p_status,version=version+1,last_code=p_code,completed_at=CASE WHEN p_status='unknown' THEN NULL ELSE clock_timestamp() END WHERE id=o.id;
 IF p_status='accepted' THEN INSERT INTO public.communication_usage_event(id,workspace_id,operation_id,contract_id,metric,quantity,provider_reference) VALUES(gen_random_uuid(),o.workspace_id,o.id,o.contract_id,'provider_accepted',1,p_reference) ON CONFLICT(operation_id,metric) DO NOTHING; END IF;
 INSERT INTO public.domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),o.workspace_id,'communication.'||p_status,o.id);
END $$;
REVOKE ALL ON FUNCTION lmt_claim_communication(),lmt_finish_communication(uuid,uuid,text,text,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_claim_communication(),lmt_finish_communication(uuid,uuid,text,text,text) TO lmt_runtime;
GRANT INSERT ON communication_usage_event TO lmt_runtime;
