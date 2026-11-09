CREATE TABLE capacity_repair_operator_grant (
 account_id uuid NOT NULL REFERENCES user_account(id), workspace_id uuid NOT NULL REFERENCES workspace(id),
 valid_until timestamptz NOT NULL, revoked boolean NOT NULL DEFAULT false, reason varchar(300) NOT NULL,
 issued_by text NOT NULL, updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(account_id,workspace_id)
);
CREATE TABLE capacity_repair_operator_audit (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, account_id uuid NOT NULL, workspace_id uuid NOT NULL,
 valid_until timestamptz NOT NULL, revoked boolean NOT NULL, reason text NOT NULL, issued_by text NOT NULL,
 database_actor text NOT NULL, recorded_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE FUNCTION audit_capacity_repair_operator() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('capacity-repair-authority:'||NEW.account_id||':'||NEW.workspace_id,0));
 INSERT INTO capacity_repair_operator_audit(account_id,workspace_id,valid_until,revoked,reason,issued_by,database_actor)
 VALUES(NEW.account_id,NEW.workspace_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user); RETURN NEW;
END; $$;
CREATE TRIGGER capacity_repair_operator_changed AFTER INSERT OR UPDATE ON capacity_repair_operator_grant FOR EACH ROW EXECUTE FUNCTION audit_capacity_repair_operator();
ALTER TABLE capacity_repair_operator_grant ENABLE ROW LEVEL SECURITY;
ALTER TABLE capacity_repair_operator_grant FORCE ROW LEVEL SECURITY;
CREATE POLICY capacity_repair_operator_self ON capacity_repair_operator_grant FOR SELECT USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT ON capacity_repair_operator_grant TO lmt_runtime;

ALTER TABLE workspace_grant ADD COLUMN repair_revision integer NOT NULL DEFAULT 0 CHECK(repair_revision>=0);
GRANT UPDATE(repair_revision) ON workspace_grant TO lmt_runtime;
CREATE TABLE capacity_repair_command (
 account_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 workspace_id uuid NOT NULL REFERENCES workspace(id), request_hash text NOT NULL,
 reason varchar(300) NOT NULL, before_state jsonb NOT NULL, result jsonb NOT NULL,
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(account_id,request_key)
);
ALTER TABLE capacity_repair_command ENABLE ROW LEVEL SECURITY;
ALTER TABLE capacity_repair_command FORCE ROW LEVEL SECURITY;
CREATE POLICY capacity_repair_command_subject ON capacity_repair_command USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT ON capacity_repair_command TO lmt_runtime;
