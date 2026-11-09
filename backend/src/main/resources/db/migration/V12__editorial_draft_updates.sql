ALTER TABLE commercial_review_grant ADD COLUMN can_edit_draft boolean NOT NULL DEFAULT false;
ALTER TABLE commercial_review_grant_audit ADD COLUMN can_edit_draft boolean NOT NULL DEFAULT false;
CREATE OR REPLACE FUNCTION audit_commercial_review_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO commercial_review_grant_audit(account_id,valid_until,revoked,reason,issued_by,database_actor,can_create_draft,can_publish,can_edit_draft)
 VALUES(NEW.account_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user,NEW.can_create_draft,NEW.can_publish,NEW.can_edit_draft);
 RETURN NEW;
END;
$$;
CREATE TABLE plan_draft_edit_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 draft_id uuid NOT NULL REFERENCES plan_version(version_id), request_body text NOT NULL,
 before_terms jsonb NOT NULL, after_terms jsonb NOT NULL, result jsonb NOT NULL,
 reason varchar(300) NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(actor_id,request_key)
);
GRANT SELECT,INSERT ON plan_draft_edit_command TO lmt_runtime;
GRANT UPDATE(display_name,effective_at,commercial_disclosure) ON plan_version TO lmt_runtime;
