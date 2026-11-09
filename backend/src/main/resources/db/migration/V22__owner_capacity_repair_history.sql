-- Read-only owner transparency; repair grants and command insert authority remain unchanged.
CREATE POLICY capacity_repair_owner_history ON capacity_repair_command FOR SELECT USING(
 lmt_is_workspace_owner(workspace_id,nullif(current_setting('lmt.account_id',true),'')::uuid)
);
CREATE INDEX capacity_repair_workspace_time ON capacity_repair_command(workspace_id,created_at);
CREATE UNIQUE INDEX capacity_repair_workspace_revision ON capacity_repair_command(workspace_id,((result->>'revision')::integer));
