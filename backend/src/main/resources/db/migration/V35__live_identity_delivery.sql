ALTER TABLE auth_challenge ADD COLUMN identity_mode text NOT NULL DEFAULT 'sandbox' CHECK(identity_mode IN ('sandbox','live'));
ALTER TABLE auth_delivery DROP CONSTRAINT auth_delivery_status_check;
ALTER TABLE auth_delivery ADD CONSTRAINT auth_delivery_status_check CHECK(status IN ('pending','dispatching','accepted','unknown','delivered','expired','failed'));
ALTER TABLE auth_delivery ADD COLUMN dispatch_token uuid;
ALTER TABLE auth_delivery ADD COLUMN dispatched_at timestamptz;
ALTER TABLE auth_delivery ADD COLUMN provider_reference varchar(100);
ALTER TABLE auth_delivery ADD COLUMN last_code text;
CREATE INDEX auth_delivery_due ON auth_delivery(status,next_attempt_at);
