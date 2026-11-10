ALTER TABLE workspace_subscription DROP CONSTRAINT workspace_subscription_status_check;
ALTER TABLE workspace_subscription ADD CONSTRAINT workspace_subscription_status_check CHECK(status IN ('active_free','active_paid'));
ALTER TABLE workspace_subscription ADD COLUMN version integer NOT NULL DEFAULT 1;
ALTER TABLE workspace_subscription ADD COLUMN period_start timestamptz;
ALTER TABLE workspace_subscription ADD COLUMN period_end timestamptz;
ALTER TABLE workspace_subscription ADD COLUMN base_amount numeric(14,2);
ALTER TABLE workspace_subscription ADD COLUMN interval_months integer;
ALTER TABLE workspace_subscription ADD CONSTRAINT subscription_period CHECK((period_start IS NULL AND period_end IS NULL AND base_amount IS NULL AND interval_months IS NULL) OR (period_end>period_start AND base_amount>=0 AND interval_months IN(1,12)));
GRANT UPDATE(plan_version_id,status,version,period_start,period_end,base_amount,interval_months) ON workspace_subscription TO lmt_runtime;
CREATE TABLE subscription_upgrade_offer(
 id uuid PRIMARY KEY,source_plan_id uuid NOT NULL REFERENCES plan_version(version_id),target_plan_id uuid NOT NULL REFERENCES plan_version(version_id),
 mode text NOT NULL CHECK(mode='sandbox'),base_amount numeric(14,2) NOT NULL CHECK(base_amount>0),tax_rate numeric(7,4) NOT NULL CHECK(tax_rate BETWEEN 0 AND 100),
 interval_months integer NOT NULL CHECK(interval_months IN(1,12)),proration text NOT NULL CHECK(proration IN('none_free_only','unused_time_credit')),
 recipient text NOT NULL,disclosure text NOT NULL,created_by text NOT NULL DEFAULT 'local_sandbox_operator',created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,CHECK(source_plan_id<>target_plan_id)
);
CREATE TRIGGER immutable_upgrade_offer BEFORE UPDATE OR DELETE ON subscription_upgrade_offer FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TABLE subscription_upgrade_offer_withdrawal(offer_id uuid PRIMARY KEY REFERENCES subscription_upgrade_offer(id),reason text NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP);
GRANT SELECT ON subscription_upgrade_offer,subscription_upgrade_offer_withdrawal TO lmt_runtime;
CREATE TABLE subscription_upgrade_quote(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),actor_id uuid NOT NULL REFERENCES user_account(id),offer_id uuid NOT NULL REFERENCES subscription_upgrade_offer(id),subscription_version integer NOT NULL,expires_at timestamptz NOT NULL,snapshot jsonb NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE subscription_upgrade_change(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),actor_id uuid NOT NULL REFERENCES user_account(id),quote_id uuid UNIQUE NOT NULL REFERENCES subscription_upgrade_quote(id),status text NOT NULL CHECK(status IN('awaiting_payment','payment_unknown','payment_failed','applied','review_required')),version integer NOT NULL DEFAULT 1,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,applied_at timestamptz);
CREATE UNIQUE INDEX one_open_upgrade ON subscription_upgrade_change(workspace_id) WHERE status IN('awaiting_payment','payment_unknown','review_required');
CREATE TABLE subscription_upgrade_command(actor_id uuid NOT NULL REFERENCES user_account(id),workspace_id uuid NOT NULL REFERENCES workspace(id),operation text NOT NULL,request_key uuid NOT NULL,request_hash char(64) NOT NULL,result jsonb NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(actor_id,workspace_id,operation,request_key));
CREATE TABLE subscription_sandbox_payment_event(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),change_id uuid NOT NULL REFERENCES subscription_upgrade_change(id),outcome text NOT NULL CHECK(outcome IN('paid','failed','unknown')),amount numeric(14,2) NOT NULL CHECK(amount>=0),currency text NOT NULL CHECK(currency='INR'),purpose text NOT NULL CHECK(purpose='workspace_saas_upgrade'),created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE UNIQUE INDEX one_verified_sandbox_settlement ON subscription_sandbox_payment_event(change_id) WHERE outcome='paid';
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['subscription_upgrade_quote','subscription_upgrade_change','subscription_upgrade_command','subscription_sandbox_payment_event'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY subscription_owner_scope ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);
 EXECUTE format('GRANT SELECT,INSERT ON %I TO lmt_runtime',t);
 END LOOP;
END $$;
GRANT UPDATE(status,version,applied_at) ON subscription_upgrade_change TO lmt_runtime;
CREATE TRIGGER immutable_upgrade_quote BEFORE UPDATE OR DELETE ON subscription_upgrade_quote FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TRIGGER immutable_sandbox_payment BEFORE UPDATE OR DELETE ON subscription_sandbox_payment_event FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();

GRANT UPDATE(allowance) ON workspace_grant TO lmt_runtime;
