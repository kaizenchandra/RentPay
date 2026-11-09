CREATE TABLE private_upload (
 id uuid PRIMARY KEY, account_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 filename varchar(120) NOT NULL, media_type text NOT NULL CHECK(media_type IN ('application/pdf','image/png','image/jpeg')),
 byte_size integer NOT NULL CHECK(byte_size BETWEEN 1 AND 1048576), sha256 char(64) NOT NULL,
 expires_at timestamptz NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 received_at timestamptz, state text NOT NULL DEFAULT 'awaiting_upload' CHECK(state IN ('awaiting_upload','quarantined')),
 UNIQUE(account_id,request_key), CHECK ((state='quarantined')=(received_at IS NOT NULL))
);
CREATE INDEX private_upload_account_page ON private_upload(account_id,id);
ALTER TABLE private_upload ENABLE ROW LEVEL SECURITY;
ALTER TABLE private_upload FORCE ROW LEVEL SECURITY;
CREATE POLICY upload_subject ON private_upload USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid) WITH CHECK(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT ON private_upload TO lmt_runtime;
GRANT UPDATE(state,received_at) ON private_upload TO lmt_runtime;
CREATE FUNCTION preserve_quarantined_upload() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.state='quarantined' THEN RAISE EXCEPTION 'Quarantined evidence is immutable'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER preserve_quarantined_upload BEFORE UPDATE ON private_upload FOR EACH ROW EXECUTE FUNCTION preserve_quarantined_upload();
