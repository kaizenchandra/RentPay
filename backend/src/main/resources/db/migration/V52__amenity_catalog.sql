CREATE TABLE amenity_type(
 id uuid PRIMARY KEY,workspace_id uuid NOT NULL REFERENCES workspace(id),code varchar(40) NOT NULL CHECK(code ~ '^[A-Z][A-Z0-9_]{1,39}$'),
 name varchar(120) NOT NULL,category varchar(80) NOT NULL,description varchar(1000) NOT NULL,tracking_mode text NOT NULL CHECK(tracking_mode IN('serialized','quantity','feature','service')),
 units text[] NOT NULL CHECK(cardinality(units) BETWEEN 1 AND 8),expected_details text[] NOT NULL CHECK(cardinality(expected_details)<=12),aliases text[] NOT NULL CHECK(cardinality(aliases)<=12),
 status text NOT NULL CHECK(status IN('active','retired')),version integer NOT NULL DEFAULT 1,updated_at timestamptz NOT NULL DEFAULT clock_timestamp(),UNIQUE(workspace_id,code),UNIQUE(workspace_id,id)
);
CREATE TABLE amenity_term(workspace_id uuid NOT NULL,type_id uuid NOT NULL,term varchar(120) NOT NULL,PRIMARY KEY(workspace_id,term),FOREIGN KEY(workspace_id,type_id) REFERENCES amenity_type(workspace_id,id));
CREATE TABLE amenity_type_version(workspace_id uuid NOT NULL,type_id uuid NOT NULL,version integer NOT NULL,snapshot jsonb NOT NULL,actor_id uuid REFERENCES user_account(id),recorded_at timestamptz NOT NULL DEFAULT clock_timestamp(),PRIMARY KEY(type_id,version),FOREIGN KEY(workspace_id,type_id) REFERENCES amenity_type(workspace_id,id));
CREATE TABLE amenity_command(workspace_id uuid NOT NULL REFERENCES workspace(id),actor_id uuid NOT NULL REFERENCES user_account(id),request_key uuid NOT NULL,request_hash char(64) NOT NULL,result jsonb NOT NULL,PRIMARY KEY(workspace_id,actor_id,request_key));
CREATE TRIGGER amenity_version_immutable BEFORE UPDATE OR DELETE ON amenity_type_version FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
CREATE TRIGGER amenity_command_immutable BEFORE UPDATE OR DELETE ON amenity_command FOR EACH ROW EXECUTE FUNCTION protect_commercial_policy();
DO $$ DECLARE t text; BEGIN FOREACH t IN ARRAY ARRAY['amenity_type','amenity_term','amenity_type_version','amenity_command'] LOOP
 EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY',t);EXECUTE format('CREATE POLICY amenity_owner_scope ON %I USING(lmt_is_workspace_owner(workspace_id,nullif(current_setting(''lmt.account_id'',true),'''')::uuid))',t);EXECUTE format('GRANT SELECT,INSERT ON %I TO lmt_runtime',t);
END LOOP;END $$;
GRANT UPDATE ON amenity_type TO lmt_runtime;
GRANT DELETE ON amenity_term TO lmt_runtime;
CREATE INDEX amenity_workspace_sort ON amenity_type(workspace_id,code,id);
-- Source-defined configuration defaults, not records of installed inventory.
CREATE FUNCTION seed_workspace_amenities(p_workspace uuid) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE item record; identifier uuid;
BEGIN
 FOR item IN SELECT * FROM (VALUES
 ('REFRIGERATOR','Refrigerator','Appliances','serialized',ARRAY['fridge']),('FREEZER','Standalone freezer','Appliances','serialized',ARRAY['separate freezer']),
 ('TELEVISION','Television','Appliances','serialized',ARRAY['tv']),('DINING_TABLE','Dining table','Furniture','quantity',ARRAY[]::text[]),('STUDY_TABLE','Study table','Furniture','quantity',ARRAY['desk']),('COFFEE_TABLE','Coffee table','Furniture','quantity',ARRAY[]::text[]),
 ('CHAIR','Chair','Furniture','quantity',ARRAY[]::text[]),('SOFA','Sofa','Furniture','quantity',ARRAY['couch']),('AIR_CONDITIONER','Air conditioner','Appliances','serialized',ARRAY['ac']),
 ('BED','Bed','Furniture','quantity',ARRAY[]::text[]),('MATTRESS','Mattress','Furniture','quantity',ARRAY[]::text[]),('WARDROBE','Wardrobe','Furniture','quantity',ARRAY[]::text[]),
 ('WASHING_MACHINE','Washing machine','Appliances','serialized',ARRAY[]::text[]),('MICROWAVE','Microwave','Appliances','serialized',ARRAY[]::text[]),('GAS_STOVE','Gas stove','Appliances','serialized',ARRAY[]::text[]),
 ('WATER_PURIFIER','Water purifier','Appliances','serialized',ARRAY[]::text[]),('WATER_HEATER','Water heater','Appliances','serialized',ARRAY['geyser']),('CEILING_FAN','Ceiling fan','Fixtures','quantity',ARRAY[]::text[]),('LIGHT','Light','Fixtures','quantity',ARRAY['lights']),('CURTAINS','Curtains','Fixtures','quantity',ARRAY[]::text[]),
 ('BALCONY','Balcony','Features','feature',ARRAY[]::text[]),('PARKING_ACCESS','Parking entitlement','Features','feature',ARRAY['parking']),('LIFT_ACCESS','Lift access','Features','feature',ARRAY['elevator access']),('WATER_SUPPLY','Water supply','Services','service',ARRAY[]::text[]),('INTERNET','Internet availability','Services','service',ARRAY['wifi'])
 ) AS seed(code,name,category,tracking,aliases) LOOP
  identifier=gen_random_uuid();
  INSERT INTO public.amenity_type(id,workspace_id,code,name,category,description,tracking_mode,units,expected_details,aliases,status)
  VALUES(identifier,p_workspace,item.code,item.name,item.category,'Configurable catalog definition; does not assert presence, ownership, location or service availability.',item.tracking,CASE WHEN item.tracking IN('feature','service') THEN ARRAY['availability'] ELSE ARRAY['item'] END,CASE WHEN item.tracking='serialized' THEN ARRAY['asset tag','serial number','brand','model'] ELSE ARRAY[]::text[] END,item.aliases,'active');
  INSERT INTO public.amenity_term(workspace_id,type_id,term) SELECT p_workspace,identifier,regexp_replace(lower(trim(value)),'[[:space:]]+','_','g') FROM unnest(ARRAY[item.code,item.name]||item.aliases) value GROUP BY 3 ON CONFLICT DO NOTHING;
  INSERT INTO public.amenity_type_version(workspace_id,type_id,version,snapshot) SELECT p_workspace,identifier,1,to_jsonb(a)-'workspace_id' FROM public.amenity_type a WHERE id=identifier;
 END LOOP;
 INSERT INTO public.domain_audit(id,scope_id,action,resource_id) VALUES(gen_random_uuid(),p_workspace,'inventory.catalog_defaults_created',p_workspace);
END $$;
REVOKE ALL ON FUNCTION seed_workspace_amenities(uuid) FROM PUBLIC;
DO $$ DECLARE w uuid; BEGIN FOR w IN SELECT id FROM workspace LOOP PERFORM seed_workspace_amenities(w); END LOOP;END $$;
CREATE FUNCTION seed_new_workspace_amenities() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$ BEGIN PERFORM public.seed_workspace_amenities(NEW.id);RETURN NEW;END $$;
REVOKE ALL ON FUNCTION seed_new_workspace_amenities() FROM PUBLIC;
CREATE TRIGGER workspace_amenity_defaults AFTER INSERT ON workspace FOR EACH ROW EXECUTE FUNCTION seed_new_workspace_amenities();
