CREATE TABLE account_notification_preference (
 account_id uuid PRIMARY KEY REFERENCES user_account(id),
 workspace_setup_enabled boolean NOT NULL DEFAULT false,
 enabled_at timestamptz,
 version integer NOT NULL CHECK(version>0),
 CHECK(workspace_setup_enabled=(enabled_at IS NOT NULL))
);
ALTER TABLE account_notification_preference ENABLE ROW LEVEL SECURITY;
CREATE POLICY notification_preference_owner ON account_notification_preference
 USING(account_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
GRANT SELECT,INSERT,UPDATE ON account_notification_preference TO lmt_runtime;
ALTER TABLE account_notification ADD COLUMN category text NOT NULL DEFAULT 'security'
 CHECK(category IN ('security','workspace_setup'));
ALTER TABLE account_notification ADD COLUMN source_event_id uuid;
CREATE UNIQUE INDEX account_notice_event ON account_notification(account_id,source_event_id) WHERE source_event_id IS NOT NULL;
