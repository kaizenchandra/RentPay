-- Existing notices remain unread; never infer historical review from delivery or sign-in.
ALTER TABLE account_notification ADD COLUMN read_at timestamptz,
 ADD COLUMN version integer NOT NULL DEFAULT 1 CHECK(version>0);
CREATE INDEX account_notification_owner ON account_notification(account_id,id);
GRANT UPDATE(read_at,version) ON account_notification TO lmt_runtime;
