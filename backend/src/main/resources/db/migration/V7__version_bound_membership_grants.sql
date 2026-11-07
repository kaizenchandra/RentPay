CREATE TABLE commercial_plan_capability (
 policy_version integer NOT NULL, capability text NOT NULL, plan_version_id uuid NOT NULL REFERENCES plan_version(version_id),
 PRIMARY KEY(policy_version,capability,plan_version_id),
 FOREIGN KEY(policy_version,capability) REFERENCES commercial_capability(policy_version,code)
);
-- Bind supported sandbox membership grants to existing historical Free versions explicitly.
INSERT INTO commercial_plan_capability
SELECT c.policy_version,c.code,p.version_id FROM commercial_capability c CROSS JOIN plan_version p
WHERE p.audience='workspace' AND p.code='FREE' AND p.publication_state='published';
CREATE TRIGGER immutable_plan_capability BEFORE UPDATE OR DELETE ON commercial_plan_capability
FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
GRANT SELECT ON commercial_plan_capability TO lmt_runtime;
