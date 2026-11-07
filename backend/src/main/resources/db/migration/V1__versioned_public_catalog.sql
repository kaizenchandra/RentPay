CREATE TABLE plan_version (
    version_id uuid PRIMARY KEY,
    audience text NOT NULL CHECK (audience IN ('workspace','consumer')),
    code text NOT NULL CHECK (code ~ '^[A-Z_]{2,32}$'),
    version_number integer NOT NULL CHECK (version_number > 0),
    display_name text NOT NULL,
    publication_state text NOT NULL CHECK (publication_state IN ('draft','published')),
    effective_at timestamptz NOT NULL,
    baseline_only boolean NOT NULL DEFAULT true,
    rentable_limit integer CHECK (rentable_limit >= 0),
    manager_limit integer CHECK (manager_limit >= 0),
    monthly_amount numeric(14,2) CHECK (monthly_amount >= 0),
    annual_amount numeric(14,2) CHECK (annual_amount >= 0),
    currency text NOT NULL CHECK (currency = 'INR'),
    commercial_disclosure text NOT NULL,
    UNIQUE(audience,code,version_number),
    UNIQUE(audience,code,effective_at)
);
CREATE INDEX plan_public_effective ON plan_version(audience,code,effective_at DESC)
    WHERE publication_state = 'published';
CREATE TABLE catalog_audit (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id uuid NOT NULL REFERENCES plan_version(version_id),
    action text NOT NULL,
    actor text NOT NULL,
    recorded_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE FUNCTION protect_published_plan() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.publication_state = 'published' THEN
        RAISE EXCEPTION 'Published plan versions are immutable';
    END IF;
    RETURN CASE WHEN TG_OP = 'DELETE' THEN OLD ELSE NEW END;
END;
$$;
CREATE TRIGGER immutable_published_plan BEFORE UPDATE OR DELETE ON plan_version
FOR EACH ROW EXECUTE FUNCTION protect_published_plan();
-- Specification baseline, not a live commercial offer or a claim of shipped capabilities.
INSERT INTO plan_version VALUES
('00000000-0000-4000-8000-000000000001','workspace','FREE',1,'Free','published','2026-01-01T00:00:00Z',true,5,1,0,0,'INR','Specification baseline. Registration is not available yet. No payment is collected.'),
('00000000-0000-4000-8000-000000000002','workspace','SILVER',1,'Silver','published','2026-01-01T00:00:00Z',true,50,5,NULL,NULL,'INR','Pricing, taxes, communication allowances and add-ons are not configured. Not available for purchase.'),
('00000000-0000-4000-8000-000000000003','workspace','GOLD',1,'Gold','published','2026-01-01T00:00:00Z',true,200,20,NULL,NULL,'INR','Pricing, taxes, communication allowances and add-ons are not configured. Not available for purchase.'),
('00000000-0000-4000-8000-000000000004','workspace','PLATINUM',1,'Platinum','published','2026-01-01T00:00:00Z',true,1000,100,NULL,NULL,'INR','Pricing, taxes, communication allowances and add-ons are not configured. Not available for purchase.'),
('00000000-0000-4000-8000-000000000005','workspace','DIAMOND',1,'Diamond','published','2026-01-01T00:00:00Z',true,NULL,NULL,NULL,NULL,'INR','Capacity and commercial terms require an approved contract. No unlimited allowance or sales service is offered.');
INSERT INTO catalog_audit(version_id,action,actor)
SELECT version_id,'BASELINE_PUBLISHED','migration:V1' FROM plan_version;
GRANT SELECT ON plan_version TO lmt_runtime;
