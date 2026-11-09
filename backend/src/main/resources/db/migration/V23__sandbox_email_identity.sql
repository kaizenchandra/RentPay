-- Separate verified email identities. No automatic matching/linking to mobile accounts.
ALTER TABLE user_account ALTER COLUMN mobile DROP NOT NULL;
ALTER TABLE user_account ADD COLUMN email varchar(254) UNIQUE;
ALTER TABLE user_account ADD CONSTRAINT account_contact_required CHECK(mobile IS NOT NULL OR email IS NOT NULL);
ALTER TABLE auth_challenge ALTER COLUMN mobile DROP NOT NULL;
ALTER TABLE auth_challenge ADD COLUMN channel text NOT NULL DEFAULT 'mobile' CHECK(channel IN ('mobile','email'));
ALTER TABLE auth_challenge ADD COLUMN email varchar(254);
ALTER TABLE auth_challenge ADD CONSTRAINT challenge_contact_channel CHECK(
 (channel='mobile' AND mobile IS NOT NULL AND email IS NULL) OR (channel='email' AND email IS NOT NULL AND mobile IS NULL)
);
CREATE INDEX challenge_email_time ON auth_challenge(email,created_at) WHERE email IS NOT NULL;
