ALTER TABLE auth_challenge ADD COLUMN purpose text NOT NULL DEFAULT 'sign_in' CHECK(purpose IN ('sign_in','link_email'));
ALTER TABLE auth_challenge ADD COLUMN link_account_id uuid REFERENCES user_account(id);
ALTER TABLE auth_challenge ADD COLUMN link_profile_version integer;
ALTER TABLE auth_challenge ADD CONSTRAINT challenge_link_binding CHECK(
 (purpose='sign_in' AND link_account_id IS NULL AND link_profile_version IS NULL) OR
 (purpose='link_email' AND channel='email' AND link_account_id IS NOT NULL AND link_profile_version IS NOT NULL AND link_profile_version>0)
);
GRANT UPDATE(email) ON user_account TO lmt_runtime;
