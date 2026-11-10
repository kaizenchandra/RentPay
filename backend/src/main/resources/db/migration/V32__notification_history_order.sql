-- Keyset history uses occurrence time plus a deterministic ID tie-breaker.
CREATE INDEX account_notification_history ON account_notification(account_id,occurred_at DESC,id DESC);
CREATE INDEX account_notification_unread_history ON account_notification(account_id,occurred_at DESC,id DESC) WHERE read_at IS NULL;
