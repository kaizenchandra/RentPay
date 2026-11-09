ALTER TABLE private_upload ADD CONSTRAINT private_upload_subject_unique UNIQUE(id,account_id);
CREATE TABLE upload_storage_review (
 upload_id uuid PRIMARY KEY, account_id uuid NOT NULL,
 outcome text NOT NULL CHECK(outcome IN ('intact','missing','mismatch','unavailable')),
 revision integer NOT NULL CHECK(revision>0), checked_at timestamptz NOT NULL,
 FOREIGN KEY(upload_id,account_id) REFERENCES private_upload(id,account_id)
);
CREATE TABLE upload_storage_command (
 account_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 upload_id uuid NOT NULL, operation text NOT NULL CHECK(operation IN ('check','restore')),
 request_hash text NOT NULL, result jsonb NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(account_id,request_key), FOREIGN KEY(upload_id,account_id) REFERENCES private_upload(id,account_id)
);
ALTER TABLE upload_storage_review ENABLE ROW LEVEL SECURITY;
ALTER TABLE upload_storage_review FORCE ROW LEVEL SECURITY;
ALTER TABLE upload_storage_command ENABLE ROW LEVEL SECURITY;
ALTER TABLE upload_storage_command FORCE ROW LEVEL SECURITY;
CREATE POLICY storage_review_subject ON upload_storage_review USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid) WITH CHECK(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
CREATE POLICY storage_command_subject ON upload_storage_command USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid) WITH CHECK(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT ON upload_storage_review,upload_storage_command TO lmt_runtime;
GRANT UPDATE(outcome,revision,checked_at) ON upload_storage_review TO lmt_runtime;
