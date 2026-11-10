ALTER TABLE workspace_invitation DROP CONSTRAINT workspace_invitation_status_check;
ALTER TABLE workspace_invitation ADD CONSTRAINT workspace_invitation_status_check
 CHECK(status IN ('pending','accepted','declined','revoked','expired'));
CREATE INDEX invitation_expiry_due ON workspace_invitation(expires_at,id) WHERE status='pending';
-- Worker-only application call. No arbitrary contact reads or client-directed state changes.
-- Definer rights are necessary to expire records after all workspace authority is revoked.
CREATE FUNCTION lmt_expire_due_invitations()
 RETURNS TABLE(invitation_id uuid,workspace_id uuid,inviter_id uuid,workspace_name varchar)
 LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public,pg_temp AS $$
DECLARE candidate record;
BEGIN
 FOR candidate IN SELECT i.id,i.workspace_id FROM public.workspace_invitation i
  WHERE i.status='pending' AND i.expires_at<=clock_timestamp() ORDER BY i.expires_at,i.id LIMIT 25
 LOOP
  IF pg_try_advisory_xact_lock(hashtextextended('membership:'||candidate.workspace_id::text,0)) THEN
   RETURN QUERY UPDATE public.workspace_invitation i SET status='expired',version=i.version+1
    WHERE i.id=candidate.id AND i.status='pending' AND i.expires_at<=clock_timestamp()
    RETURNING i.id,i.workspace_id,i.invited_by,i.workspace_name;
  END IF;
 END LOOP;
END $$;
REVOKE ALL ON FUNCTION lmt_expire_due_invitations() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION lmt_expire_due_invitations() TO lmt_runtime;
