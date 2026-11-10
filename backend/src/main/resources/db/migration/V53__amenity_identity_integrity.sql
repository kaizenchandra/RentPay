-- Historical spellings cannot be reassigned by deleting their reservation.
REVOKE DELETE ON amenity_term FROM lmt_runtime;
ALTER TABLE amenity_type ADD CONSTRAINT amenity_availability_measure CHECK(tracking_mode NOT IN('feature','service') OR units=ARRAY['availability']::text[]);
CREATE FUNCTION guard_amenity_identity() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN
 IF NEW.id<>OLD.id OR NEW.workspace_id<>OLD.workspace_id OR NEW.code<>OLD.code OR NEW.version<>OLD.version+1 THEN RAISE EXCEPTION 'Catalog identity is immutable and revisions must advance once'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER amenity_identity_guard BEFORE UPDATE ON amenity_type FOR EACH ROW EXECUTE FUNCTION guard_amenity_identity();
