ALTER TABLE user_account ADD COLUMN auth_generation integer NOT NULL DEFAULT 1 CHECK(auth_generation>0);
ALTER TABLE user_account ADD COLUMN auth_recovered_at timestamptz;
GRANT UPDATE(auth_generation,auth_recovered_at) ON user_account TO lmt_runtime;
ALTER TABLE auth_challenge DROP CONSTRAINT auth_challenge_purpose_check;
ALTER TABLE auth_challenge ADD CONSTRAINT auth_challenge_purpose_check CHECK(purpose IN ('sign_in','recover_account','link_email','link_mobile','reauthenticate'));
ALTER TABLE auth_challenge DROP CONSTRAINT challenge_link_binding;
ALTER TABLE auth_challenge ADD CONSTRAINT challenge_link_binding CHECK(
 (purpose IN ('sign_in','recover_account') AND link_account_id IS NULL AND link_profile_version IS NULL) OR
 (purpose IN ('link_email','link_mobile','reauthenticate') AND
  (purpose='reauthenticate' OR (purpose='link_email' AND channel='email') OR (purpose='link_mobile' AND channel='mobile')) AND
  link_account_id IS NOT NULL AND link_profile_version IS NOT NULL AND link_profile_version>0)
);
