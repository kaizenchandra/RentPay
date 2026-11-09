ALTER TABLE plan_version ADD COLUMN row_revision integer NOT NULL DEFAULT 1;
CREATE FUNCTION advance_plan_revision() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN NEW.row_revision=OLD.row_revision+1; RETURN NEW; END;
$$;
CREATE TRIGGER advance_plan_revision BEFORE UPDATE ON plan_version
FOR EACH ROW EXECUTE FUNCTION advance_plan_revision();
ALTER TABLE commercial_review_grant ADD COLUMN can_publish boolean NOT NULL DEFAULT false;
ALTER TABLE commercial_review_grant_audit ADD COLUMN can_publish boolean NOT NULL DEFAULT false;
CREATE OR REPLACE FUNCTION audit_commercial_review_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO commercial_review_grant_audit(account_id,valid_until,revoked,reason,issued_by,database_actor,can_create_draft,can_publish)
 VALUES(NEW.account_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user,NEW.can_create_draft,NEW.can_publish);
 RETURN NEW;
END;
$$;
CREATE TABLE plan_activation_review (
 id uuid PRIMARY KEY, actor_id uuid NOT NULL REFERENCES user_account(id),
 draft_id uuid NOT NULL REFERENCES plan_version(version_id), draft_revision integer NOT NULL,
 source_id uuid NOT NULL REFERENCES plan_version(version_id),
 expires_at timestamptz NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE plan_activation_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 draft_id uuid NOT NULL REFERENCES plan_version(version_id), review_id uuid NOT NULL REFERENCES plan_activation_review(id),
 request_body text NOT NULL, result jsonb NOT NULL, reason varchar(300) NOT NULL,
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(actor_id,request_key)
);
GRANT SELECT,INSERT ON plan_activation_review,plan_activation_command TO lmt_runtime;
GRANT UPDATE(publication_state) ON plan_version TO lmt_runtime;
GRANT INSERT ON commercial_plan_capability TO lmt_runtime;
