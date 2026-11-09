ALTER TABLE auth_challenge DROP CONSTRAINT auth_challenge_purpose_check;
ALTER TABLE auth_challenge ADD CONSTRAINT auth_challenge_purpose_check CHECK(purpose IN ('sign_in','link_email','link_mobile','reauthenticate'));
ALTER TABLE auth_challenge DROP CONSTRAINT challenge_link_binding;
ALTER TABLE auth_challenge ADD CONSTRAINT challenge_link_binding CHECK(
 (purpose='sign_in' AND link_account_id IS NULL AND link_profile_version IS NULL) OR
 (purpose IN ('link_email','link_mobile','reauthenticate') AND
  (purpose='reauthenticate' OR (purpose='link_email' AND channel='email') OR (purpose='link_mobile' AND channel='mobile')) AND
  link_account_id IS NOT NULL AND link_profile_version IS NOT NULL AND link_profile_version>0)
);
