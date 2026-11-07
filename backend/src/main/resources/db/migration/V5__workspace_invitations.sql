ALTER TABLE workspace_member ADD COLUMN id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE;
ALTER TABLE workspace_member ADD COLUMN version integer NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE workspace_member ADD COLUMN joined_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
-- Narrow non-recursive predicates for RLS; fixed search path and no PUBLIC execution.
CREATE FUNCTION lmt_is_workspace_owner(w uuid,a uuid) RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER
 SET search_path=pg_catalog,public,pg_temp AS $$ SELECT EXISTS(SELECT 1 FROM public.workspace_member WHERE workspace_id=w AND account_id=a AND active AND role='OWNER') $$;
REVOKE ALL ON FUNCTION lmt_is_workspace_owner(uuid,uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_is_workspace_owner(uuid,uuid) TO lmt_runtime;
CREATE POLICY member_owner ON workspace_member USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting('lmt.account_id',true),'')::uuid));
GRANT UPDATE(active,version,joined_at) ON workspace_member TO lmt_runtime;
CREATE TABLE workspace_invitation (
 id uuid PRIMARY KEY, workspace_id uuid NOT NULL REFERENCES workspace(id), workspace_name varchar(120) NOT NULL,
 invited_by uuid NOT NULL REFERENCES user_account(id), mobile varchar(13) NOT NULL CHECK(mobile ~ '^\+91[6-9][0-9]{9}$'),
 status text NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','accepted','declined','revoked')),
 version integer NOT NULL DEFAULT 1 CHECK(version>0), created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 expires_at timestamptz NOT NULL, accepted_by uuid REFERENCES user_account(id),
 CHECK(expires_at>created_at), CHECK((status='accepted')=(accepted_by IS NOT NULL))
);
CREATE INDEX invitation_recipient ON workspace_invitation(mobile,id);
CREATE INDEX invitation_workspace ON workspace_invitation(workspace_id,id);
ALTER TABLE workspace_invitation ENABLE ROW LEVEL SECURITY;
CREATE POLICY invitation_participant ON workspace_invitation USING(
 lmt_is_workspace_owner(workspace_id,nullif(current_setting('lmt.account_id',true),'')::uuid)
 OR mobile=(SELECT mobile FROM user_account WHERE id=nullif(current_setting('lmt.account_id',true),'')::uuid));
CREATE TABLE membership_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), workspace_id uuid NOT NULL REFERENCES workspace(id),
 operation text NOT NULL, request_key uuid NOT NULL, request_hash char(64) NOT NULL, result jsonb NOT NULL,
 occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(actor_id,workspace_id,operation,request_key)
);
ALTER TABLE membership_command ENABLE ROW LEVEL SECURITY;
CREATE POLICY membership_command_actor ON membership_command USING(actor_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
-- Commercial owns these allocation records. Reservation and consumption commit with membership.
CREATE TABLE manager_allocation (
 id uuid PRIMARY KEY, workspace_id uuid NOT NULL REFERENCES workspace(id), member_id uuid NOT NULL REFERENCES workspace_member(id),
 status text NOT NULL CHECK(status IN ('committed','released')), committed_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, released_at timestamptz
);
CREATE UNIQUE INDEX manager_one_allocation ON manager_allocation(member_id) WHERE status='committed';
ALTER TABLE manager_allocation ENABLE ROW LEVEL SECURITY;
CREATE POLICY manager_allocation_scope ON manager_allocation USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=manager_allocation.workspace_id AND m.active));
GRANT SELECT,INSERT,UPDATE ON workspace_invitation TO lmt_runtime;
GRANT SELECT,INSERT ON membership_command TO lmt_runtime;
GRANT SELECT,INSERT,UPDATE ON manager_allocation TO lmt_runtime;
GRANT UPDATE(used,reserved) ON workspace_grant TO lmt_runtime;
