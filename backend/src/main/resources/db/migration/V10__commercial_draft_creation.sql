ALTER TABLE commercial_review_grant ADD COLUMN can_create_draft boolean NOT NULL DEFAULT false;
ALTER TABLE commercial_review_grant_audit ADD COLUMN can_create_draft boolean NOT NULL DEFAULT false;
CREATE OR REPLACE FUNCTION audit_commercial_review_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO commercial_review_grant_audit(account_id,valid_until,revoked,reason,issued_by,database_actor,can_create_draft)
 VALUES(NEW.account_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user,NEW.can_create_draft);
 RETURN NEW;
END;
$$;
CREATE FUNCTION lock_commercial_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(hashtext('commercial-grant'),hashtext(NEW.account_id::text));
 RETURN NEW;
END;
$$;
CREATE TRIGGER commercial_grant_lock BEFORE INSERT OR UPDATE ON commercial_review_grant
FOR EACH ROW EXECUTE FUNCTION lock_commercial_grant();
CREATE TABLE plan_draft_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 source_id uuid NOT NULL REFERENCES plan_version(version_id),
 request_body text NOT NULL, draft_id uuid NOT NULL UNIQUE REFERENCES plan_version(version_id),
 reason varchar(300) NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(actor_id,request_key)
);
GRANT SELECT,INSERT ON plan_draft_command TO lmt_runtime;
GRANT INSERT ON plan_version TO lmt_runtime;
CREATE FUNCTION restrict_runtime_plan_insert() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF current_user='lmt_runtime' AND NEW.publication_state<>'draft' THEN
  RAISE EXCEPTION 'Runtime can only insert draft plans';
 END IF;
 RETURN NEW;
END;
$$;
CREATE TRIGGER runtime_draft_only BEFORE INSERT ON plan_version
FOR EACH ROW EXECUTE FUNCTION restrict_runtime_plan_insert();
