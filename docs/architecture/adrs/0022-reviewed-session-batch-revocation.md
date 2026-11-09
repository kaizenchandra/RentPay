# ADR 0022: Reviewed selected-session revocation

Status: Accepted for the AUTH-001 session-management subset.

Extend the existing account security controls with bounded, explicitly selected revocation. This supports reviewing multiple suspicious sessions without a sequence of partially successful commands. It is not account recovery, contact replacement or a guarantee to prevent future sign-in.

A user selects 1–25 other sessions from the authorized paginated session list. The server returns a read-only preview with browser hint, session ID, last verification and version. Its strong ETag binds the exact selected session versions, account and current browser. POST confirmation requires that ETag, recent current-session authentication, CSRF and actor-scoped idempotency. The limit bounds one transaction; it is not a commercial allowance or account session limit.

The identity module owns this operation and reuses account_session, identity_command, Spring Session, domain_audit and account_notification. No migration or new writer is needed. Confirmation locks the selected and current registry rows in deterministic database order, rechecks ownership/active state/recent proof, and validates the preview before writes. It deletes the selected framework sessions, writes registry tombstones, per-session audits, batch audit/command and one security notice in one transaction. Failed audit rolls the whole transaction back. Revoked registry tombstones remain authoritative even if an already-running framework request writes session data later.

Exactly listed sessions end. A new sign-in after preview and unselected sessions remain valid. The current session cannot be selected. A stale, ended, expired or inaccessible target rejects the entire selection; refresh/review is required. Exact idempotent retry returns the committed result; a changed selection/version conflicts. The UI freezes the reviewed command on an unknown outcome and provides same-command retry. Scope changes abort obsolete requests; no command secrets are stored in the browser.

FoundationIntegrationTest verifies scope, CSRF, missing/stale preconditions, duplicate/current selection denial, recent-auth enforcement, replay/conflict, competing commands, audit rollback and new-session preservation. session-batch.spec.ts verifies keyboard/focus, explicit review, real multi-browser revocation, lost-response recovery, one notice, mobile reflow and axe. Broader identity recovery and live-provider gates remain incomplete.
