ALTER TABLE workspace_invitation ADD CONSTRAINT invitation_workspace_identity UNIQUE(workspace_id,id);
ALTER TABLE workspace_invitation ADD COLUMN replaces_invitation_id uuid UNIQUE;
ALTER TABLE workspace_invitation ADD CONSTRAINT invitation_replacement_scope
 FOREIGN KEY(workspace_id,replaces_invitation_id) REFERENCES workspace_invitation(workspace_id,id);
ALTER TABLE workspace_invitation ADD CONSTRAINT invitation_not_self_replacement CHECK(replaces_invitation_id IS NULL OR replaces_invitation_id<>id);
