ALTER TABLE auth_challenge ADD COLUMN recovery_completed_at timestamptz;
-- Use existing committed audit evidence; never infer completion from a consumed proof alone.
UPDATE auth_challenge c SET recovery_completed_at=a.occurred_at
 FROM domain_audit a WHERE a.resource_id=c.id AND a.action='identity.account_recovered'
 AND c.purpose='recover_account' AND c.consumed;
ALTER TABLE auth_challenge ADD CONSTRAINT recovery_completion_evidence
 CHECK(recovery_completed_at IS NULL OR (purpose='recover_account' AND consumed));
