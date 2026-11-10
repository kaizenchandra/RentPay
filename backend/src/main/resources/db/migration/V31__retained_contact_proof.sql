-- A generic recent login is insufficient to remove an identity contact.
ALTER TABLE account_session ADD COLUMN proof_channel text CHECK(proof_channel IN ('email','mobile'));
ALTER TABLE account_session ADD COLUMN proof_profile_version integer CHECK(proof_profile_version>0);
ALTER TABLE account_session ADD CONSTRAINT session_proof_pair CHECK((proof_channel IS NULL)=(proof_profile_version IS NULL));
GRANT UPDATE(proof_channel,proof_profile_version) ON account_session TO lmt_runtime;
