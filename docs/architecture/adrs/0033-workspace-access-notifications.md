# 0033 — Essential workspace access notifications

Date: 2026-11-10. Status: Accepted.

## Decision

Extend existing account-owned in-app notifications with `workspace_access` for accepted manager membership and owner revocation. FS-02 requires membership event notifications; FS-22 requires private event delivery and recovery. These are essential access notices, so optional workspace-setup opt-in and quiet hours do not suppress them. No external email/SMS is sent by this flow.

The authorized workspace use case calls the identity module's `WorkspaceAccessNotices` interface inside its existing transaction. The identity module owns the insert, recipient scope and audit. The interface requires an existing transaction and restores the caller's prior database account scope after writing. No workspace service directly writes identity tables. Membership/capacity, notice and audit commit together; failure rolls everything back, and existing command idempotency permits safe retry.

Grant events use the accepted invitation reference; revoke events use a deterministic membership/version reference. The existing account/source-event unique index provides duplicate suppression in addition to command replay. Notices contain only the workspace name snapshot and access-change explanation, without contact, financial, signing or private property evidence. React renders the snapshot as text. Retained account history remains readable after workspace revocation, but the generic Your workspaces link resolves only currently authorized contexts. A historical notice never restores access.

V37 extends the allowed category. OpenAPI 0.35.0 and generated client types reflect it; existing read/filter/paging/ETag APIs are reused. The UI labels this category Workspace access. No new user command or second notification queue is added.

## Verification and limits

FoundationIntegrationTest checks exact replay, recipient isolation, quiet-hours/opt-out independence, denied outsider revoke, injected database failure and atomic rollback, successful retry and one audit outcome. Existing last-slot membership race tests remain in that suite. `workspace-access-notices.mjs` verifies actual desktop/mobile invitation acceptance, revoke with a lost response, exactly two notices, denied revoked-resource access, read-status persistence, keyboard and axe/reflow.

Pending invitations, declines/expiry, owner-facing outcomes, provider delivery and further event categories remain separate incomplete work. Live invitations remain gated by existing approved identity requirements. No legal notification, provider receipt, or broad phase completion is claimed.
