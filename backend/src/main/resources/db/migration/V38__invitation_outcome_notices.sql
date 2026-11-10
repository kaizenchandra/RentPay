ALTER TABLE account_notification DROP CONSTRAINT account_notification_category_check;
ALTER TABLE account_notification ADD CONSTRAINT account_notification_category_check
 CHECK(category IN ('security','workspace_setup','workspace_access','workspace_invitation'));
