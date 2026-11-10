ALTER TABLE workspace_subscription ADD CONSTRAINT subscription_paid_terms CHECK(status<>'active_paid' OR (period_start IS NOT NULL AND period_end IS NOT NULL AND base_amount IS NOT NULL AND interval_months IS NOT NULL));
ALTER TABLE subscription_upgrade_quote ADD CONSTRAINT quote_workspace_identity UNIQUE(workspace_id,id);
ALTER TABLE subscription_upgrade_change ADD CONSTRAINT change_quote_scope FOREIGN KEY(workspace_id,quote_id) REFERENCES subscription_upgrade_quote(workspace_id,id);
ALTER TABLE subscription_upgrade_change ADD CONSTRAINT change_workspace_identity UNIQUE(workspace_id,id);
ALTER TABLE subscription_sandbox_payment_event ADD CONSTRAINT payment_change_scope FOREIGN KEY(workspace_id,change_id) REFERENCES subscription_upgrade_change(workspace_id,id);
