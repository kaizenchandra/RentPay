-- Expand integrity constraints without changing applied migrations or historical evidence.
ALTER TABLE property_space ADD CONSTRAINT property_space_workspace_id UNIQUE(workspace_id,id);
ALTER TABLE rentable_allocation ADD CONSTRAINT rentable_allocation_workspace_id UNIQUE(workspace_id,id);
ALTER TABLE rentable_allocation ADD CONSTRAINT rentable_resource_scope FOREIGN KEY(workspace_id,resource_id) REFERENCES property_space(workspace_id,id);
ALTER TABLE rentable_usage_event ADD CONSTRAINT rentable_event_scope FOREIGN KEY(workspace_id,allocation_id) REFERENCES rentable_allocation(workspace_id,id);
CREATE INDEX rentable_event_workspace_allocation ON rentable_usage_event(workspace_id,allocation_id);
