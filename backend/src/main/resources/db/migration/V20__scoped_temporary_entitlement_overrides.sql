CREATE TABLE entitlement_operator_grant (
 account_id uuid NOT NULL REFERENCES user_account(id), workspace_id uuid NOT NULL REFERENCES workspace(id),
 valid_until timestamptz NOT NULL, revoked boolean NOT NULL DEFAULT false, reason varchar(300) NOT NULL,
 issued_by text NOT NULL, updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(account_id,workspace_id)
);
CREATE TABLE entitlement_operator_audit (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, account_id uuid NOT NULL, workspace_id uuid NOT NULL,
 valid_until timestamptz NOT NULL, revoked boolean NOT NULL, reason text NOT NULL, issued_by text NOT NULL,
 database_actor text NOT NULL, recorded_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE FUNCTION audit_entitlement_operator() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('entitlement-authority:'||NEW.account_id||':'||NEW.workspace_id,0));
 INSERT INTO entitlement_operator_audit(account_id,workspace_id,valid_until,revoked,reason,issued_by,database_actor)
 VALUES(NEW.account_id,NEW.workspace_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user); RETURN NEW;
END; $$;
CREATE TRIGGER entitlement_operator_changed AFTER INSERT OR UPDATE ON entitlement_operator_grant FOR EACH ROW EXECUTE FUNCTION audit_entitlement_operator();
ALTER TABLE entitlement_operator_grant ENABLE ROW LEVEL SECURITY;
ALTER TABLE entitlement_operator_grant FORCE ROW LEVEL SECURITY;
CREATE POLICY entitlement_operator_self ON entitlement_operator_grant FOR SELECT USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT ON entitlement_operator_grant TO lmt_runtime;
CREATE TABLE workspace_capability_override (
 workspace_id uuid PRIMARY KEY REFERENCES workspace(id), capability text NOT NULL CHECK(capability='WORKSPACE_MEMBER_ADD'),
 effect text NOT NULL CHECK(effect IN ('allow','deny','inherit')), expires_at timestamptz,
 revision integer NOT NULL CHECK(revision>0), updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK((effect='inherit')=(expires_at IS NULL))
);
ALTER TABLE workspace_capability_override ENABLE ROW LEVEL SECURITY;
ALTER TABLE workspace_capability_override FORCE ROW LEVEL SECURITY;
CREATE POLICY override_read ON workspace_capability_override FOR SELECT USING(
 EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=workspace_capability_override.workspace_id AND m.account_id=nullif(current_setting('lmt.account_id',true),'')::uuid AND m.active)
 OR EXISTS(SELECT 1 FROM entitlement_operator_grant g WHERE g.workspace_id=workspace_capability_override.workspace_id AND NOT g.revoked AND g.valid_until>clock_timestamp())
);
CREATE POLICY override_insert ON workspace_capability_override FOR INSERT WITH CHECK(EXISTS(SELECT 1 FROM entitlement_operator_grant g WHERE g.workspace_id=workspace_capability_override.workspace_id AND NOT g.revoked AND g.valid_until>clock_timestamp()));
CREATE POLICY override_update ON workspace_capability_override FOR UPDATE USING(EXISTS(SELECT 1 FROM entitlement_operator_grant g WHERE g.workspace_id=workspace_capability_override.workspace_id AND NOT g.revoked AND g.valid_until>clock_timestamp())) WITH CHECK(EXISTS(SELECT 1 FROM entitlement_operator_grant g WHERE g.workspace_id=workspace_capability_override.workspace_id AND NOT g.revoked AND g.valid_until>clock_timestamp()));
CREATE TABLE entitlement_override_command (
 account_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL, workspace_id uuid NOT NULL REFERENCES workspace(id),
 request_hash text NOT NULL, reason varchar(300) NOT NULL, before_state jsonb NOT NULL, result jsonb NOT NULL,
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(account_id,request_key)
);
ALTER TABLE entitlement_override_command ENABLE ROW LEVEL SECURITY;
ALTER TABLE entitlement_override_command FORCE ROW LEVEL SECURITY;
CREATE POLICY override_command_subject ON entitlement_override_command USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT,UPDATE ON workspace_capability_override TO lmt_runtime;
GRANT SELECT,INSERT ON entitlement_override_command TO lmt_runtime;
