-- Explicit platform review authority. Provisioned by trusted operators, never workspace owners.
CREATE TABLE commercial_review_grant (
 account_id uuid PRIMARY KEY REFERENCES user_account(id),
 valid_until timestamptz NOT NULL,
 revoked boolean NOT NULL DEFAULT false,
 reason varchar(300) NOT NULL CHECK(length(trim(reason)) BETWEEN 10 AND 300),
 issued_by text NOT NULL,
 updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE commercial_review_grant_audit (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 account_id uuid NOT NULL, valid_until timestamptz NOT NULL,
 revoked boolean NOT NULL, reason varchar(300) NOT NULL,
 issued_by text NOT NULL, database_actor text NOT NULL,
 recorded_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE FUNCTION audit_commercial_review_grant() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO commercial_review_grant_audit(account_id,valid_until,revoked,reason,issued_by,database_actor)
 VALUES(NEW.account_id,NEW.valid_until,NEW.revoked,NEW.reason,NEW.issued_by,current_user);
 RETURN NEW;
END;
$$;
CREATE TRIGGER commercial_review_grant_changed AFTER INSERT OR UPDATE ON commercial_review_grant
FOR EACH ROW EXECUTE FUNCTION audit_commercial_review_grant();
GRANT SELECT ON commercial_review_grant TO lmt_runtime;
-- No grant mutation, grant-audit mutation or plan write privileges for the runtime.
