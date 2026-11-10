# ADR 0029: Enforced optional account notification preference

Accepted for the implemented in-app workspace setup event, 2026-11-09 (FS-22; NOTIFY-001 remains Partial).

Account preferences must affect a real event, not merely persist unused settings. Add one explicitly optional account copy of `workspace.provisioned.v1`. It defaults off. The shared workspace setup notice and essential account-security notices remain independent and cannot be suppressed by this setting. No paid plan is required, no external provider is invoked, and no charge is established.

Identity owns `account_notification_preference`, account notice category/source-event fields and preference command/audit evidence. The existing authorized workspace handler invokes the identity module's `workspaceReady` entry point after verifying current creator/owner authority. No direct cross-module table writes are introduced. Delivery uses the existing outbox lease, fencing, transaction and retry/recovery behavior. Workspace creation already committed before notification handling.

Preference GET returns a version-zero default until the first write. PATCH requires CSRF, account scope, strong If-Match and actor-scoped idempotency. Changes and delivery serialize on the same account preference lock. Consent has an activation timestamp: events before the current opt-in period are suppressed even when their jobs run later. Re-enabling begins a new period; keeping an already-enabled preference retains its activation time. There is no backfill. Disabling before a claimed worker completes suppresses the personal copy; a completed delivery is retained in history.

The database enforces recipient/source-event uniqueness. Delivery and suppression have separate audit actions; shared-workspace operation success continues to describe the shared notice, not an external message or personal-copy guarantee. Preference changes generate essential in-app security notices. Old read-command results deserialize to the security category, preserving replay compatibility.

V33 adds preferences, RLS, category/source-event fields and deduplication index. No prior records are deleted or inferred as read. OpenAPI 0.30.0 and generated types expose the preference and notice category. UI confirms account scope, category, effective behavior and absence of cost before mutation; unknown responses retain the exact command.

Verification: FoundationIntegrationTest checks default, actor isolation, validation, strong versions and replay/conflict; OperationsIntegrationTest checks no backfill, deduplication and consent revocation after claim while shared delivery continues. Real desktop/mobile browser tests prove opt-in delivery, opt-out suppression, persistence, uncertain-response recovery and essential notice continuity. Quiet hours, other categories/channels, provider delivery/consent and metering remain separate incomplete requirements.
