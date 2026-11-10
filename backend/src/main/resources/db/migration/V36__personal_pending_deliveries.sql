-- Bounded recipient queue projection; existing outbox remains the single owner of delivery state.
CREATE INDEX workspace_outbox_personal_pending ON workspace_outbox(actor_id,event_id)
 WHERE event_type='workspace.provisioned.v1' AND status IN ('pending','running','failed','blocked');
