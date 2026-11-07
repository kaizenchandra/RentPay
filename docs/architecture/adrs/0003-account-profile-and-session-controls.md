# ADR 0003: Account profile and session controls

Status: accepted for the local sandbox foundation, 2026-11-07. AUTH-001 / FS-01 remains Partial.

## Boundary and ownership

Identity owns profile display names, the account-session registry, identity command results and account security notifications. V4 adds these structures and the profile version. Spring Session continues to own opaque bearer-session persistence. No dependency versions or domain owners change.

The UI at `/account/profile` edits only display name. Mobile is masked and immutable through this operation. `/account/security` lists active browser sessions and in-app security notices; it confirms the selected browser and sign-in time before remote revocation. Browser/platform hints are coarse, untrusted labels, not verified device identity. No IP/location/raw User-Agent is retained by this feature.

## Authorization, concurrency and recovery

All selectors are account-bound on the server. Registry IDs are independent random management IDs, never authentication cookies. The registry stores only a SHA-256 session-token hash. Infrastructure queries bind the authenticated account; the pre-security revocation check also binds the token hash. Registry access is not protected by RLS because it runs before request-scoped database context; private command results and notices do use transaction-local account RLS.

Profile PATCH uses strong ETag/If-Match and a row lock. Concurrent stale writes return 412; missing preconditions return 428. Actor/operation/key-scoped command results are locked and committed with the profile update, audit and in-app notice. Same confirmation replays the stored result; a changed confirmation with the same key conflicts. Unknown browser outcomes retain the exact command key/body/version for retry. Safe unsaved fields remain in memory after network failure, never browser storage.

Remote revocation requires a recent sign-in (default 300 seconds; `lmt.security.reauthentication-seconds` accepts 1–1800), CSRF, allowed Origin and the exact target ETag. The current session uses Sign out instead. Revocation atomically writes a registry tombstone, deletes the Spring Session row and records the command/audit/notice. Every subsequent authenticated request checks the registry, preventing a concurrent framework save from resurrecting a revoked session. Sign out also writes the tombstone and audit transactionally. A request already authorized before revocation is not claimed to be cancelled.

Session lists join currently valid Spring Session records, exclude revoked/idle-expired/absolute-expired entries, and return bounded 25-item pages with actor-bound cursors and snapshot counts. Notices use the same bounded pagination. Private responses/pages are no-store and noindex. Browser session checks run on focus and every 60 seconds; server checks remain authoritative on every operation.

## Migration and limits

V4 is additive and preserves account/workspace/history records. **Authenticated cookies created before V4 lack registry evidence and must sign in again after upgrade.** Do not delete the database volume. Old registry entries are excluded from active lists; production retention/purge policy remains pending.

These notifications are durable in-app records, not SMS/email delivery. Live mobile verification, verified contact changes/linking, account deletion/export/preferences, broader recovery and invitation authority are pending. Sandbox codes remain simulated. No device-attestation, cancellation of in-flight work, legal identity, production readiness or complete FS-01 claim is made.

## Evidence

`FoundationIntegrationTest` verifies version races, exact command replay, input allowlists, account isolation, recent authentication, stale/missing preconditions, current-session protection and persisted remote invalidation. `account.spec.ts` verifies real desktop/mobile save/reload/conflict recovery, a lost committed-response retry, confirmation/cancellation, revocation in a separate browser, OpenAPI schemas, keyboard focus, axe and layout overflow. Existing provisioning and outage/restart tests remain regression checks. Manual screen-reader, production retention and deployment evidence remain open.

Implementation references: [Spring Session JDBC](https://docs.spring.io/spring-session/reference/configuration/jdbc.html) and [JDBC session API](https://docs.spring.io/spring-session/api/org/springframework/session/jdbc/JdbcIndexedSessionRepository.html). Framework session operations use their own transaction semantics; the registry tombstone is the enduring authorization guard.
