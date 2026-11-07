-- Spring Session JDBC schema, owned by Flyway. No application DDL privilege.
CREATE TABLE spring_session (
 primary_id char(36) PRIMARY KEY, session_id char(36) NOT NULL,
 creation_time bigint NOT NULL, last_access_time bigint NOT NULL,
 max_inactive_interval integer NOT NULL, expiry_time bigint NOT NULL, principal_name varchar(100)
);
CREATE UNIQUE INDEX spring_session_ix1 ON spring_session(session_id);
CREATE INDEX spring_session_ix2 ON spring_session(expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session(principal_name);
CREATE TABLE spring_session_attributes (
 session_primary_id char(36) NOT NULL REFERENCES spring_session(primary_id) ON DELETE CASCADE,
 attribute_name varchar(200) NOT NULL, attribute_bytes bytea NOT NULL,
 PRIMARY KEY(session_primary_id,attribute_name)
);
CREATE TABLE user_account (
 id uuid PRIMARY KEY, mobile varchar(13) UNIQUE NOT NULL CHECK(mobile ~ '^\+91[6-9][0-9]{9}$'),
 display_name varchar(100) NOT NULL, identity_mode text NOT NULL CHECK(identity_mode IN ('sandbox','live')),
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE auth_challenge (
 id uuid PRIMARY KEY, session_hash char(64) NOT NULL, mobile varchar(13) NOT NULL,
 display_name varchar(100) NOT NULL, request_key uuid NOT NULL, expires_at timestamptz NOT NULL,
 attempts integer NOT NULL DEFAULT 0 CHECK(attempts BETWEEN 0 AND 5),
 consumed boolean NOT NULL DEFAULT false, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(session_hash,request_key)
);
CREATE INDEX challenge_mobile_time ON auth_challenge(mobile,created_at);
CREATE TABLE auth_delivery (
 challenge_id uuid PRIMARY KEY REFERENCES auth_challenge(id),
 status text NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','delivered','expired','failed')),
 attempts integer NOT NULL DEFAULT 0, next_attempt_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE domain_audit (
 id uuid PRIMARY KEY, actor_id uuid, scope_id uuid, action text NOT NULL,
 resource_id uuid NOT NULL, occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE workspace (
 id uuid PRIMARY KEY, name varchar(120) NOT NULL, customer_type text NOT NULL CHECK(customer_type IN ('individual','organization')),
 city varchar(100) NOT NULL, state varchar(100) NOT NULL,
 status text NOT NULL DEFAULT 'active' CHECK(status IN ('active','restricted','suspended')),
 mode text NOT NULL CHECK(mode IN ('sandbox','live')), version integer NOT NULL DEFAULT 1,
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE workspace_member (
 workspace_id uuid NOT NULL REFERENCES workspace(id), account_id uuid NOT NULL REFERENCES user_account(id),
 role text NOT NULL CHECK(role IN ('OWNER','MANAGER')), active boolean NOT NULL DEFAULT true,
 PRIMARY KEY(workspace_id,account_id)
);
CREATE INDEX membership_account ON workspace_member(account_id,workspace_id) WHERE active;
CREATE TABLE workspace_settings (
 workspace_id uuid PRIMARY KEY REFERENCES workspace(id), timezone text NOT NULL DEFAULT 'Asia/Kolkata', currency text NOT NULL DEFAULT 'INR'
);
CREATE TABLE workspace_consent (
 workspace_id uuid NOT NULL REFERENCES workspace(id), account_id uuid NOT NULL REFERENCES user_account(id),
 policy_version text NOT NULL, accepted_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(workspace_id,policy_version)
);
CREATE TABLE workspace_subscription (
 id uuid PRIMARY KEY, workspace_id uuid UNIQUE NOT NULL REFERENCES workspace(id),
 plan_version_id uuid NOT NULL REFERENCES plan_version(version_id),
 status text NOT NULL CHECK(status='active_free'), started_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE workspace_grant (
 workspace_id uuid NOT NULL REFERENCES workspace(id), resource text NOT NULL,
 allowance integer NOT NULL CHECK(allowance>=0), used integer NOT NULL DEFAULT 0 CHECK(used>=0),
 reserved integer NOT NULL DEFAULT 0 CHECK(reserved>=0), PRIMARY KEY(workspace_id,resource),
 CHECK(used+reserved<=allowance)
);
CREATE TABLE workspace_command (
 actor_id uuid NOT NULL REFERENCES user_account(id), request_key uuid NOT NULL,
 request_hash char(64) NOT NULL, workspace_id uuid NOT NULL REFERENCES workspace(id),
 created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(actor_id,request_key)
);
CREATE TABLE workspace_outbox (
 event_id uuid PRIMARY KEY, actor_id uuid NOT NULL REFERENCES user_account(id), event_type text NOT NULL, schema_version integer NOT NULL DEFAULT 1,
 occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP, aggregate_type text NOT NULL DEFAULT 'workspace',
 aggregate_id uuid NOT NULL REFERENCES workspace(id), aggregate_version integer NOT NULL DEFAULT 1,
 scope_kind text NOT NULL DEFAULT 'workspace', scope_id uuid NOT NULL REFERENCES workspace(id),
 correlation_id uuid NOT NULL, causation_id uuid NOT NULL,
 status text NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','delivered'))
);
CREATE TABLE workspace_notification (
 event_id uuid PRIMARY KEY REFERENCES workspace_outbox(event_id), workspace_id uuid NOT NULL REFERENCES workspace(id),
 message text NOT NULL, created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- App role cannot bypass these policies. Context is set transaction-locally from authenticated account.
ALTER TABLE workspace_member ENABLE ROW LEVEL SECURITY;
CREATE POLICY member_self ON workspace_member USING(account_id = nullif(current_setting('lmt.account_id',true),'')::uuid)
 WITH CHECK(account_id = nullif(current_setting('lmt.account_id',true),'')::uuid);
ALTER TABLE workspace ENABLE ROW LEVEL SECURITY;
CREATE POLICY workspace_read ON workspace FOR SELECT USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=id AND m.active));
CREATE POLICY workspace_create ON workspace FOR INSERT WITH CHECK(nullif(current_setting('lmt.account_id',true),'') IS NOT NULL);
ALTER TABLE workspace_command ENABLE ROW LEVEL SECURITY;
CREATE POLICY command_actor ON workspace_command USING(actor_id=nullif(current_setting('lmt.account_id',true),'')::uuid);
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['workspace_settings','workspace_consent','workspace_subscription','workspace_grant','workspace_notification'] LOOP
   EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
   EXECUTE format('CREATE POLICY scoped_member ON %I USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=%I.workspace_id AND m.active))',t,t);
 END LOOP;
END $$;
GRANT SELECT,INSERT,UPDATE,DELETE ON spring_session,spring_session_attributes TO lmt_runtime;
GRANT SELECT,INSERT ON user_account TO lmt_runtime;
GRANT SELECT,INSERT,UPDATE ON auth_challenge,auth_delivery TO lmt_runtime;
GRANT INSERT ON domain_audit TO lmt_runtime;
GRANT SELECT,INSERT ON workspace,workspace_member,workspace_settings,workspace_consent,workspace_subscription,workspace_grant,workspace_command TO lmt_runtime;
GRANT SELECT,INSERT,UPDATE ON workspace_outbox TO lmt_runtime;
GRANT SELECT,INSERT ON workspace_notification TO lmt_runtime;
