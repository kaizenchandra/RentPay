-- Preserve existing definitions; new sandbox workspaces use independently rentable items.
INSERT INTO rentable_counting_policy(version,shared_mode,description) VALUES
(2,'beds_only','Sandbox independent-item baseline: each whole rentable unit and each separately rentable bed counts once. Shared-unit, property, building and floor containers count zero. Draft, ready and maintenance-blocked inventory counts; archived inventory counts zero. Tenancies and occupants are not quota items. Occupancy does not discount inventory. Weighted combinations are not supported.');
CREATE TABLE workspace_counting_policy(
 workspace_id uuid PRIMARY KEY REFERENCES workspace(id),
 policy_version integer NOT NULL REFERENCES rentable_counting_policy(version),
 bound_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Previously deployed workspaces keep the pre-existing definition, not the new default.
INSERT INTO workspace_counting_policy(workspace_id,policy_version)
 SELECT id,(SELECT max(version) FROM rentable_counting_policy WHERE version<2) FROM workspace;
ALTER TABLE workspace_counting_policy ENABLE ROW LEVEL SECURITY;
CREATE POLICY counting_member_read ON workspace_counting_policy USING(EXISTS(SELECT 1 FROM workspace_member m WHERE m.workspace_id=workspace_counting_policy.workspace_id AND m.account_id=nullif(current_setting('lmt.account_id',true),'')::uuid AND m.active));
GRANT SELECT ON workspace_counting_policy TO lmt_runtime;
CREATE FUNCTION bind_workspace_counting_policy() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$ BEGIN
 INSERT INTO public.workspace_counting_policy(workspace_id,policy_version) VALUES(NEW.id,CASE WHEN NEW.mode='sandbox' THEN 2 ELSE 1 END);
 INSERT INTO public.domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),NEW.id,'quota.counting_policy_bound',NEW.id);
 RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION bind_workspace_counting_policy() FROM PUBLIC;
CREATE TRIGGER workspace_counting_policy_bind AFTER INSERT ON workspace FOR EACH ROW EXECUTE FUNCTION bind_workspace_counting_policy();
CREATE FUNCTION preserve_counting_policy() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 RAISE EXCEPTION 'Counting definitions and workspace bindings are immutable; a reviewed migration is required';
END $$;
CREATE TRIGGER counting_policy_immutable BEFORE UPDATE OR DELETE ON rentable_counting_policy FOR EACH ROW EXECUTE FUNCTION preserve_counting_policy();
CREATE TRIGGER counting_binding_immutable BEFORE UPDATE OR DELETE ON workspace_counting_policy FOR EACH ROW EXECUTE FUNCTION preserve_counting_policy();
CREATE FUNCTION lmt_rentable_quantity(kind text,rental_mode text,shared_mode text) RETURNS integer LANGUAGE sql IMMUTABLE PARALLEL SAFE AS $$
 SELECT CASE WHEN kind='bed' THEN 1 WHEN kind='unit' AND (rental_mode='whole' OR shared_mode='units_and_beds') THEN 1 ELSE 0 END
$$;
GRANT EXECUTE ON FUNCTION lmt_rentable_quantity(text,text,text) TO lmt_runtime;
