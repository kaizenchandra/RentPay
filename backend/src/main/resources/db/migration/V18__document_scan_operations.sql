-- Documents-owned worker infrastructure: no file contents, filenames or raw provider payloads.
CREATE TABLE document_scan_job (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), upload_id uuid NOT NULL UNIQUE, account_id uuid NOT NULL,
 event_type text NOT NULL DEFAULT 'document.scan_requested.v1' CHECK(event_type='document.scan_requested.v1'),
 schema_version integer NOT NULL DEFAULT 1 CHECK(schema_version=1), aggregate_version integer NOT NULL DEFAULT 1 CHECK(aggregate_version=1),
 scope_kind text NOT NULL DEFAULT 'account' CHECK(scope_kind='account'), correlation_id uuid NOT NULL DEFAULT gen_random_uuid(), causation_id uuid NOT NULL,
 status text NOT NULL DEFAULT 'queued' CHECK(status IN ('queued','running','blocked','failed','cancelled')),
 revision integer NOT NULL DEFAULT 1 CHECK(revision>0), attempts integer NOT NULL DEFAULT 0 CHECK(attempts>=0),
 cycle_attempts integer NOT NULL DEFAULT 0 CHECK(cycle_attempts BETWEEN 0 AND 5),
 lease_token uuid, lease_until timestamptz, next_attempt_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 last_code text, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(upload_id,account_id) REFERENCES private_upload(id,account_id),
 CHECK((status='running')=(lease_token IS NOT NULL AND lease_until IS NOT NULL))
);
CREATE INDEX document_scan_due ON document_scan_job(status,next_attempt_at,lease_until);
CREATE TABLE document_scan_attempt (
 job_id uuid NOT NULL REFERENCES document_scan_job(id), attempt integer NOT NULL, lease_token uuid NOT NULL UNIQUE,
 started_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, finished_at timestamptz, outcome text NOT NULL DEFAULT 'running',
 PRIMARY KEY(job_id,attempt)
);
CREATE TABLE document_scan_command (
 account_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL, job_id uuid NOT NULL REFERENCES document_scan_job(id),
 request_hash text NOT NULL, reason varchar(300) NOT NULL, result jsonb NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(account_id,request_key)
);
ALTER TABLE document_scan_command ENABLE ROW LEVEL SECURITY;
ALTER TABLE document_scan_command FORCE ROW LEVEL SECURITY;
CREATE POLICY document_scan_command_subject ON document_scan_command USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid) WITH CHECK(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
-- Job/attempt metadata is worker infrastructure. Browser projections authorize the subject against private_upload first.
GRANT SELECT,INSERT,UPDATE ON document_scan_job,document_scan_attempt TO lmt_runtime;
GRANT SELECT,INSERT ON document_scan_command TO lmt_runtime;
-- Owner-only backfill; runtime RLS remains enabled and the FORCE setting is restored in this migration transaction.
ALTER TABLE private_upload NO FORCE ROW LEVEL SECURITY;
INSERT INTO document_scan_job(upload_id,account_id,causation_id) SELECT id,account_id,request_key FROM private_upload WHERE state='quarantined';
ALTER TABLE private_upload FORCE ROW LEVEL SECURITY;
INSERT INTO domain_audit(id,actor_id,action,resource_id) SELECT gen_random_uuid(),account_id,'DOCUMENT_SCAN_BACKFILLED',id FROM document_scan_job;
