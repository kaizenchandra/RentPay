INSERT INTO commercial_policy VALUES(2,CURRENT_TIMESTAMP,'Property foundation and existing membership capabilities; paid lifecycle remains separately gated.','migration:V41');
INSERT INTO commercial_capability SELECT 2,code,label,allow_restricted,allow_suspended FROM commercial_capability WHERE policy_version=1;
INSERT INTO commercial_capability VALUES
(2,'PROPERTY_CREATE','Create property records',false,false),
(2,'PROPERTY_EDIT','Maintain property records',true,false),
(2,'PROPERTY_ARCHIVE','Archive unused property records',true,false),
(2,'RENTABLE_CREATE','Create or restore rentable inventory',false,false);
INSERT INTO commercial_plan_capability SELECT 2,capability,plan_version_id FROM commercial_plan_capability WHERE policy_version=1;
INSERT INTO commercial_plan_capability SELECT 2,c.code,p.version_id FROM commercial_capability c CROSS JOIN plan_version p
 WHERE c.policy_version=2 AND c.code IN ('PROPERTY_CREATE','PROPERTY_EDIT','PROPERTY_ARCHIVE','RENTABLE_CREATE') AND p.audience='workspace' AND p.code='FREE' AND p.publication_state='published';
CREATE TABLE property_record(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),code varchar(40) NOT NULL,name varchar(120) NOT NULL,
 address_line varchar(240) NOT NULL,locality varchar(120) NOT NULL,city varchar(100) NOT NULL,state varchar(100) NOT NULL,pin varchar(6) NOT NULL CHECK(pin='' OR pin ~ '^[1-9][0-9]{5}$'),
 latitude numeric(9,6),longitude numeric(9,6),house_rules varchar(4000) NOT NULL DEFAULT '',
 status text NOT NULL DEFAULT 'draft' CHECK(status IN ('draft','review_required','active','restricted','archived')),
 ownership_status text NOT NULL DEFAULT 'unverified' CHECK(ownership_status IN ('unverified','review_pending','verified')),
 version integer NOT NULL DEFAULT 1,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(workspace_id,id),CHECK((latitude IS NULL)=(longitude IS NULL)),CHECK(latitude BETWEEN -90 AND 90),CHECK(longitude BETWEEN -180 AND 180)
);
CREATE UNIQUE INDEX property_code ON property_record(workspace_id,lower(code));
CREATE TABLE property_space(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL,property_id uuid NOT NULL,parent_id uuid,kind text NOT NULL CHECK(kind IN ('building','floor','unit','bed')),
 code varchar(40) NOT NULL,name varchar(120) NOT NULL,category varchar(80) NOT NULL DEFAULT '',area numeric(12,2),area_unit text NOT NULL DEFAULT 'sq_ft' CHECK(area_unit IN ('sq_ft','sq_m')),
 capacity integer NOT NULL DEFAULT 1 CHECK(capacity BETWEEN 1 AND 1000),bathrooms integer NOT NULL DEFAULT 0 CHECK(bathrooms BETWEEN 0 AND 100),balconies integer NOT NULL DEFAULT 0 CHECK(balconies BETWEEN 0 AND 100),
 furnishing text NOT NULL DEFAULT 'unfurnished' CHECK(furnishing IN ('unfurnished','semi_furnished','furnished')),rental_mode text NOT NULL DEFAULT 'whole' CHECK(rental_mode IN ('whole','shared')),
 rent numeric(14,2),deposit numeric(14,2),maintenance numeric(14,2),status text NOT NULL DEFAULT 'draft' CHECK(status IN ('draft','ready','maintenance_blocked','archived')),
 version integer NOT NULL DEFAULT 1,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(workspace_id,property_id) REFERENCES property_record(workspace_id,id),UNIQUE(workspace_id,property_id,id),
 FOREIGN KEY(workspace_id,property_id,parent_id) REFERENCES property_space(workspace_id,property_id,id),
 CHECK(parent_id IS NULL OR parent_id<>id),CHECK(area>0),CHECK(rent>=0),CHECK(deposit>=0),CHECK(maintenance>=0)
);
CREATE UNIQUE INDEX space_identifier ON property_space(property_id,lower(code));
CREATE INDEX space_parent ON property_space(property_id,parent_id);
CREATE TABLE property_command(actor_id uuid NOT NULL REFERENCES user_account(id),workspace_id uuid NOT NULL REFERENCES workspace(id),operation text NOT NULL,request_key uuid NOT NULL,request_hash char(64) NOT NULL,result jsonb NOT NULL,created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(actor_id,workspace_id,operation,request_key));
CREATE TABLE property_history(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),property_id uuid NOT NULL REFERENCES property_record(id),resource_id uuid NOT NULL,actor_id uuid NOT NULL REFERENCES user_account(id),action text NOT NULL,version integer NOT NULL,summary text NOT NULL,occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX property_history_recent ON property_history(property_id,occurred_at DESC,id);
CREATE TABLE rentable_allocation(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),resource_id uuid NOT NULL,status text NOT NULL CHECK(status IN ('committed','released')),created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,released_at timestamptz);
CREATE UNIQUE INDEX rentable_one_active ON rentable_allocation(resource_id) WHERE status='committed';
CREATE INDEX rentable_workspace ON rentable_allocation(workspace_id,status);
CREATE TABLE rentable_usage_event(id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),allocation_id uuid NOT NULL REFERENCES rentable_allocation(id),quantity integer NOT NULL CHECK(quantity IN (-1,1)),occurred_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(allocation_id,quantity));
-- Whole-unit counting is supported; shared bed/container counting requires an explicit commercial decision.
CREATE TABLE rentable_counting_policy(version integer PRIMARY KEY,shared_mode text NOT NULL CHECK(shared_mode IN ('unconfigured','beds_only','units_and_beds')),description text NOT NULL);
INSERT INTO rentable_counting_policy VALUES(1,'unconfigured','Development whole-unit baseline: count every nonarchived whole unit including drafts; building/floor containers do not count. Shared counting awaits approval.');
GRANT SELECT ON rentable_counting_policy TO lmt_runtime;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['property_record','property_space','property_command','property_history','rentable_allocation','rentable_usage_event'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);
 EXECUTE format('CREATE POLICY property_owner_scope ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);
 EXECUTE format('GRANT SELECT,INSERT,UPDATE ON %I TO lmt_runtime',t);
 END LOOP;
END $$;
REVOKE UPDATE ON property_history,rentable_usage_event FROM lmt_runtime;
