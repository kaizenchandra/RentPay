-- Availability belongs to the offering family, not immutable contracted versions.
CREATE TABLE plan_offering_availability (
 audience text NOT NULL CHECK(audience IN('workspace','consumer')),
 code text NOT NULL CHECK(code ~ '^[A-Z_]{2,32}$'),
 anchor_version_id uuid NOT NULL REFERENCES plan_version(version_id),
 available boolean NOT NULL, revision integer NOT NULL CHECK(revision>0),
 updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(audience,code),
 CHECK(audience<>'workspace' OR code<>'FREE' OR available)
);
CREATE TABLE plan_offering_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 selected_version_id uuid NOT NULL REFERENCES plan_version(version_id), request_body text NOT NULL,
 before_state jsonb NOT NULL, result jsonb NOT NULL, reason varchar(300) NOT NULL,
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(actor_id,request_key)
);
GRANT SELECT,INSERT,UPDATE ON plan_offering_availability TO lmt_runtime;
GRANT SELECT,INSERT ON plan_offering_command TO lmt_runtime;
ALTER TABLE commercial_review_grant ADD COLUMN can_manage_offering boolean NOT NULL DEFAULT false;
ALTER TABLE commercial_review_grant_audit ADD COLUMN can_manage_offering boolean NOT NULL DEFAULT false;
CREATE OR REPLACE FUNCTION audit_commercial_review_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO commercial_review_grant_audit(account_id,valid_until,revoked,reason,issued_by,database_actor,can_create_draft,can_publish,can_edit_draft,can_discard_draft,can_manage_offering)
 VALUES(NEW.account_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user,NEW.can_create_draft,NEW.can_publish,NEW.can_edit_draft,NEW.can_discard_draft,NEW.can_manage_offering);
 RETURN NEW;
END;
$$;
