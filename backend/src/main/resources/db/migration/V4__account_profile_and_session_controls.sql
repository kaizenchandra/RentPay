ALTER TABLE user_account ADD COLUMN version integer NOT NULL DEFAULT 1 CHECK(version>0);
GRANT UPDATE(display_name,version) ON user_account TO lmt_runtime;
CREATE TABLE account_session (
 id uuid PRIMARY KEY, account_id uuid NOT NULL REFERENCES user_account(id),
 token_hash char(64) NOT NULL UNIQUE, authenticated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 client_label varchar(100) NOT NULL, version integer NOT NULL DEFAULT 1 CHECK(version>0), revoked_at timestamptz
);
CREATE INDEX account_session_owner ON account_session(account_id,id);
CREATE TABLE identity_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), operation text NOT NULL,
 request_key uuid NOT NULL, request_hash char(64) NOT NULL, result jsonb NOT NULL,
 occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(actor_id,operation,request_key)
);
CREATE TABLE account_notification (
 id uuid PRIMARY KEY, account_id uuid NOT NULL REFERENCES user_account(id),
 message text NOT NULL, occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE identity_command ENABLE ROW LEVEL SECURITY;
CREATE POLICY identity_command_owner ON identity_command USING(actor_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
ALTER TABLE account_notification ENABLE ROW LEVEL SECURITY;
CREATE POLICY account_notice_owner ON account_notification USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT,UPDATE ON account_session TO lmt_runtime;
GRANT SELECT,INSERT ON identity_command,account_notification TO lmt_runtime;
-- Session registry is identity infrastructure queried before request scope exists. All queries bind both account and token hash.
-- Old authenticated cookies without a registry record must sign in again after this upgrade; no identity/provisioning history is erased.
