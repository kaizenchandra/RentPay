-- Extend purpose binding without rewriting existing email proofs or command evidence.
ALTER TABLE auth_challenge DROP CONSTRAINT auth_challenge_purpose_check;
ALTER TABLE auth_challenge ADD CONSTRAINT auth_challenge_purpose_check CHECK(purpose IN ('sign_in','link_email','link_mobile'));
ALTER TABLE auth_challenge DROP CONSTRAINT challenge_link_binding;
ALTER TABLE auth_challenge ADD CONSTRAINT challenge_link_binding CHECK(
 (purpose='sign_in' AND link_account_id IS NULL AND link_profile_version IS NULL) OR
 (purpose IN ('link_email','link_mobile') AND
   ((purpose='link_email' AND channel='email') OR (purpose='link_mobile' AND channel='mobile')) AND
   link_account_id IS NOT NULL AND link_profile_version IS NOT NULL AND link_profile_version>0)
);
GRANT UPDATE(mobile) ON user_account TO lmt_runtime;
