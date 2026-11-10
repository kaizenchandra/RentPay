ALTER TABLE account_notification_preference ADD COLUMN quiet_hours_enabled boolean NOT NULL DEFAULT false;
ALTER TABLE account_notification_preference ADD COLUMN quiet_start varchar(5) NOT NULL DEFAULT '22:00';
ALTER TABLE account_notification_preference ADD COLUMN quiet_end varchar(5) NOT NULL DEFAULT '07:00';
ALTER TABLE account_notification_preference ADD CONSTRAINT quiet_hour_format CHECK(
 quiet_start ~ '^([01][0-9]|2[0-3]):[0-5][0-9]$' AND quiet_end ~ '^([01][0-9]|2[0-3]):[0-5][0-9]$' AND quiet_start<>quiet_end);
