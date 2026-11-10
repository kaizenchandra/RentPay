ALTER TABLE commercial_capability ADD COLUMN operational boolean NOT NULL DEFAULT true;
ALTER TABLE commercial_capability ADD COLUMN protected_correction boolean NOT NULL DEFAULT false;
INSERT INTO commercial_policy VALUES(3,CURRENT_TIMESTAMP,'Central layered eligibility; unimplemented product capabilities fail closed. Local supplements are not live purchases.','migration:V47');
INSERT INTO commercial_capability SELECT 3,code,label,allow_restricted,allow_suspended,true,code IN('PROPERTY_EDIT','PROPERTY_ARCHIVE','WORKSPACE_MEMBER_REVOKE') FROM commercial_capability WHERE policy_version=2;
INSERT INTO commercial_capability VALUES
(3,'PROPERTY_MANAGEMENT','Property register and inventory setup',true,false,true,false),
(3,'RENTAL_LIFECYCLE','Complete rental lifecycle',true,false,false,true),
(3,'AUTO_ADVERTISE','Approved automatic advertising',false,false,false,false),
(3,'TENANT_HELPDESK_ADVANCED','Advanced tenant helpdesk',false,false,false,false),
(3,'EMAIL_NOTIFICATIONS','Workspace email notifications',false,false,false,false),
(3,'WHATSAPP_NOTIFICATIONS','Workspace WhatsApp notifications',false,false,false,false),
(3,'AI_CHATBOT','Scoped AI chatbot',false,false,false,false),
(3,'ADVANCED_REPORTING','Advanced reporting',false,false,false,false),
(3,'BULK_IMPORT','Bulk import',false,false,false,false),
(3,'PUBLIC_API','Public integration API',false,false,false,false),
(3,'CUSTOM_BRANDING','Custom branding',false,false,false,false);
INSERT INTO commercial_plan_capability SELECT 3,capability,plan_version_id FROM commercial_plan_capability WHERE policy_version=2;
INSERT INTO commercial_plan_capability SELECT 3,'PROPERTY_MANAGEMENT',version_id FROM plan_version WHERE audience='workspace' AND publication_state='published';
CREATE TABLE workspace_entitlement_input(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),policy_version integer NOT NULL,capability text NOT NULL,
 kind text NOT NULL CHECK(kind IN('add_on','timed_grant','commercial_override')),effect text NOT NULL CHECK(effect IN('allow','deny')),
 plan_version_id uuid REFERENCES plan_version(version_id),starts_at timestamptz NOT NULL,expires_at timestamptz NOT NULL CHECK(expires_at>starts_at),
 mode text NOT NULL CHECK(mode='sandbox'),reason varchar(300) NOT NULL,created_by text NOT NULL,created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(policy_version,capability) REFERENCES commercial_capability(policy_version,code),CHECK(kind='commercial_override' OR effect='allow'),UNIQUE(workspace_id,id)
);
CREATE TABLE workspace_entitlement_revocation(input_id uuid PRIMARY KEY,workspace_id uuid NOT NULL,reason varchar(300) NOT NULL,created_by text NOT NULL,created_at timestamptz NOT NULL DEFAULT clock_timestamp(),FOREIGN KEY(workspace_id,input_id) REFERENCES workspace_entitlement_input(workspace_id,id));
CREATE FUNCTION validate_entitlement_input() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('membership:'||NEW.workspace_id,0));
 IF NOT EXISTS(SELECT 1 FROM workspace WHERE id=NEW.workspace_id AND mode='sandbox') THEN RAISE EXCEPTION 'Local entitlement inputs require a sandbox workspace'; END IF;
 IF NEW.effect='deny' AND EXISTS(SELECT 1 FROM commercial_capability WHERE policy_version=NEW.policy_version AND code=NEW.capability AND protected_correction) THEN RAISE EXCEPTION 'Required corrections cannot be denied by commercial override'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER validate_entitlement_input BEFORE INSERT ON workspace_entitlement_input FOR EACH ROW EXECUTE FUNCTION validate_entitlement_input();
CREATE TRIGGER immutable_entitlement_input BEFORE UPDATE OR DELETE ON workspace_entitlement_input FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TRIGGER immutable_entitlement_revocation BEFORE UPDATE OR DELETE ON workspace_entitlement_revocation FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['workspace_entitlement_input','workspace_entitlement_revocation'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY entitlement_input_read ON %I FOR SELECT USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=%I.workspace_id AND m.account_id=nullif(current_setting(''lmt.account_id'',true),'''')::uuid AND m.active))',t,t);
 EXECUTE format('GRANT SELECT ON %I TO lmt_runtime',t);
 END LOOP;
END $$;
CREATE INDEX entitlement_input_scope ON workspace_entitlement_input(workspace_id,capability,expires_at);
CREATE FUNCTION audit_entitlement_input() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('membership:'||NEW.workspace_id,0));
 IF TG_TABLE_NAME='workspace_entitlement_input' THEN
 INSERT INTO domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),NEW.workspace_id,'entitlement.input_configured',NEW.id);
 ELSE
 INSERT INTO domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),NEW.workspace_id,'entitlement.input_revoked',NEW.input_id);
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER entitlement_input_audit AFTER INSERT ON workspace_entitlement_input FOR EACH ROW EXECUTE FUNCTION audit_entitlement_input();
CREATE TRIGGER entitlement_revocation_audit BEFORE INSERT ON workspace_entitlement_revocation FOR EACH ROW EXECUTE FUNCTION audit_entitlement_input();
