ALTER TABLE workspace_invitation ALTER COLUMN mobile DROP NOT NULL;
ALTER TABLE workspace_invitation ADD COLUMN email varchar(254);
ALTER TABLE workspace_invitation ADD CONSTRAINT invitation_one_contact
 CHECK((mobile IS NOT NULL AND email IS NULL) OR (mobile IS NULL AND email IS NOT NULL AND length(email)>3 AND position('@' in email)>1));
CREATE INDEX invitation_email_recipient ON workspace_invitation(email,id) WHERE email IS NOT NULL;
DROP POLICY invitation_participant ON workspace_invitation;
CREATE POLICY invitation_participant ON workspace_invitation USING(
 lmt_is_workspace_owner(workspace_id,nullif(current_setting('lmt.account_id',true),'')::uuid)
 OR mobile=(SELECT mobile FROM user_account WHERE id=nullif(current_setting('lmt.account_id',true),'')::uuid)
 OR email=(SELECT email FROM user_account WHERE id=nullif(current_setting('lmt.account_id',true),'')::uuid));
