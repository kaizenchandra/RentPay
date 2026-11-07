CREATE TABLE commercial_policy (
 version integer PRIMARY KEY CHECK(version>0), effective_at timestamptz NOT NULL,
 description text NOT NULL, created_by text NOT NULL
);
CREATE TABLE commercial_capability (
 policy_version integer NOT NULL REFERENCES commercial_policy(version), code text NOT NULL,
 label text NOT NULL, allow_restricted boolean NOT NULL, allow_suspended boolean NOT NULL,
 PRIMARY KEY(policy_version,code)
);
INSERT INTO commercial_policy VALUES(1,CURRENT_TIMESTAMP,'Sandbox membership policy; full product entitlements and paid subscription lifecycle pending.','migration:V6');
INSERT INTO commercial_capability VALUES
(1,'WORKSPACE_MEMBER_ADD','Invite and accept manager membership',false,false),
(1,'WORKSPACE_MEMBER_REVOKE','Revoke manager membership and release capacity',true,true);
CREATE FUNCTION protect_commercial_policy() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Commercial policy evidence is immutable; publish a new version'; END;
$$;
CREATE TRIGGER immutable_commercial_policy BEFORE UPDATE OR DELETE ON commercial_policy FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TRIGGER immutable_commercial_capability BEFORE UPDATE OR DELETE ON commercial_capability FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TABLE manager_usage_event (
 id uuid PRIMARY KEY, workspace_id uuid NOT NULL REFERENCES workspace(id), allocation_id uuid NOT NULL REFERENCES manager_allocation(id),
 kind text NOT NULL CHECK(kind IN ('committed','released')), quantity integer NOT NULL,
 occurred_at timestamptz NOT NULL, UNIQUE(allocation_id,kind),
 CHECK((kind='committed' AND quantity=1) OR (kind='released' AND quantity=-1))
);
-- Historical projections retain actual V5 event times, not invented booking/signature/financial evidence.
INSERT INTO manager_usage_event SELECT gen_random_uuid(),workspace_id,id,'committed',1,committed_at FROM manager_allocation;
INSERT INTO manager_usage_event SELECT gen_random_uuid(),workspace_id,id,'released',-1,released_at FROM manager_allocation WHERE status='released';
CREATE INDEX manager_usage_time ON manager_usage_event(workspace_id,occurred_at,id);
ALTER TABLE manager_usage_event ENABLE ROW LEVEL SECURITY;
CREATE POLICY manager_usage_scope ON manager_usage_event USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=manager_usage_event.workspace_id AND m.active));
GRANT SELECT ON commercial_policy,commercial_capability TO lmt_runtime;
GRANT SELECT,INSERT ON manager_usage_event TO lmt_runtime;
