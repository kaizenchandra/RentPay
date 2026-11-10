-- Local upgrade retries are distinct from renewal delinquency/grace policy.
CREATE TABLE subscription_retry_policy(
 version text PRIMARY KEY,mode text NOT NULL CHECK(mode='sandbox'),max_attempts integer NOT NULL CHECK(max_attempts BETWEEN 1 AND 10),
 cooldown_seconds integer NOT NULL CHECK(cooldown_seconds BETWEEN 1 AND 3600),effective_at timestamptz NOT NULL,disclosure text NOT NULL
);
INSERT INTO subscription_retry_policy VALUES('sandbox-upgrade-retry-v1','sandbox',3,30,CURRENT_TIMESTAMP,'Local test policy: at most three attempts per confirmed upgrade, 30 seconds after a definitive failure, manual confirmation only. No renewal grace or automatic collection.');
CREATE TRIGGER immutable_retry_policy BEFORE UPDATE OR DELETE ON subscription_retry_policy FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
GRANT SELECT ON subscription_retry_policy TO lmt_runtime;
ALTER TABLE subscription_upgrade_change ADD COLUMN retry_policy text REFERENCES subscription_retry_policy(version);
ALTER TABLE subscription_upgrade_change ADD COLUMN attempt_number integer NOT NULL DEFAULT 1 CHECK(attempt_number>=1);
ALTER TABLE subscription_upgrade_change ADD COLUMN failed_at timestamptz;
-- Existing requests receive no invented historical attempt count or failure time.
-- They remain readable and can reconcile pending outcomes; a failed legacy request needs fresh terms.
GRANT UPDATE(attempt_number,failed_at) ON subscription_upgrade_change TO lmt_runtime;
ALTER TABLE subscription_sandbox_payment_event ADD COLUMN attempt_number integer;
ALTER TABLE subscription_sandbox_payment_event ADD CONSTRAINT positive_payment_attempt CHECK(attempt_number IS NULL OR attempt_number>=1);
CREATE UNIQUE INDEX one_attempt_outcome ON subscription_sandbox_payment_event(change_id,attempt_number,outcome) WHERE attempt_number IS NOT NULL;
