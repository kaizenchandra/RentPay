ALTER TABLE workspace_outbox DROP CONSTRAINT workspace_outbox_status_check;
ALTER TABLE workspace_outbox ADD CONSTRAINT workspace_outbox_status_check CHECK(status IN ('pending','running','delivered','blocked','failed','cancelled'));
ALTER TABLE workspace_outbox ADD COLUMN version integer NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE workspace_outbox ADD COLUMN attempts integer NOT NULL DEFAULT 0 CHECK(attempts>=0);
ALTER TABLE workspace_outbox ADD COLUMN cycle_attempts integer NOT NULL DEFAULT 0 CHECK(cycle_attempts BETWEEN 0 AND 5);
ALTER TABLE workspace_outbox ADD COLUMN next_attempt_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE workspace_outbox ADD COLUMN lease_token uuid;
ALTER TABLE workspace_outbox ADD COLUMN lease_until timestamptz;
ALTER TABLE workspace_outbox ADD COLUMN last_code text;
ALTER TABLE workspace_outbox ADD COLUMN updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE workspace_outbox ADD CONSTRAINT operation_lease CHECK((status='running')=(lease_token IS NOT NULL AND lease_until IS NOT NULL));
CREATE INDEX workspace_operations_due ON workspace_outbox(status,next_attempt_at,lease_until);
CREATE INDEX workspace_operations_scope ON workspace_outbox(scope_id,event_id);
CREATE TABLE operation_attempt (
 event_id uuid NOT NULL REFERENCES workspace_outbox(event_id), attempt integer NOT NULL,
 lease_token uuid NOT NULL UNIQUE, started_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 finished_at timestamptz, outcome text NOT NULL DEFAULT 'running', PRIMARY KEY(event_id,attempt)
);
CREATE TABLE operation_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), event_id uuid NOT NULL REFERENCES workspace_outbox(event_id),
 operation text NOT NULL CHECK(operation IN ('retry','cancel')), request_key uuid NOT NULL,
 request_hash char(64) NOT NULL, reason varchar(300) NOT NULL, result jsonb NOT NULL,
 occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(actor_id,event_id,operation,request_key)
);
ALTER TABLE operation_command ENABLE ROW LEVEL SECURITY;
CREATE POLICY operation_command_actor ON operation_command USING(actor_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
-- Queue and attempt metadata are worker infrastructure; browser projections must authorize workspace owner first.
GRANT SELECT,INSERT,UPDATE ON operation_attempt TO lmt_runtime;
GRANT SELECT,INSERT ON operation_command TO lmt_runtime;
