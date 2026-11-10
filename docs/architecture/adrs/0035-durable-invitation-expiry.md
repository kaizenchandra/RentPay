# 0035 — Durable invitation expiry reconciliation

Date: 2026-11-10. Status: Accepted for manager invitations.

## Decision

Persist invitation expiry and its owner notice rather than leaving every expired record pending indefinitely. The existing deadline projection continues to prevent acceptance immediately, including before worker processing. Do not change configured invitation lifetimes or fabricate membership, capacity, delivery or user action.

V39 adds terminal `expired` and a partial due index. A narrowly scoped SECURITY DEFINER function expires at most 25 pending due records per invocation. Its fixed search path, fully qualified table, revoked PUBLIC execution and runtime-only grant follow the existing predicate-function convention. The function accepts no resource/contact parameters and returns only changed invitation/workspace/inviter references and the existing workspace-name snapshot. Definer rights are necessary because normal invitation RLS must deny access after all owner authority is gone. The function is called only by the application's worker service; no HTTP endpoint exposes it.

Each candidate uses the existing `membership:<workspace>` transaction advisory lock, with try-lock/skip for contention, then a conditional update rechecks pending status and the database deadline. This coordinates with accept, decline and revoke; concurrent workers cannot apply expiry twice. No external I/O occurs under locks. The same transaction writes the system audit and, only if original inviter authority is still valid, an identity-owned in-app outcome notice. Otherwise it audits suppressed delivery. Deterministic event references retain deduplication. Notification failure rolls back expiry/version/audit; the next scheduled database reconciliation can retry safely.

The Java worker is disabled when jobs are disabled (API process), polls every 30 seconds after a 60-second startup delay, and processes bounded batches without an external transport retry loop. A database outage leaves due records discoverable after recovery/restart. Persistent database/notice errors require operator investigation; the scheduler's error logs are the current diagnostic surface. This slice does not claim a full queue-age dashboard or per-invitation retry-exhaustion UI.

OpenAPI 0.37.0 documents projection versus persisted expiry semantics. Owner and invitee screens explain that no membership was created and how to request/send a new invitation. Stale commands can return 412 after the worker increments the version; an unprocessed expired deadline returns 409. A replacement invitation has a new identity and still requires explicit acceptance and atomic capacity checks.

## Evidence and boundaries

FoundationIntegrationTest verifies notice-failure rollback, simultaneous workers and acceptance, one persisted version/audit/owner notice, private history, no capacity allocation, suppressed notice after owner revocation, and replacement acceptance. Real Compose browser verification advances only its own test invitation deadline, restarts the worker, then verifies expiry, responsive guidance, authorized owner notice and replacement flow. Artificial deadline advancement is a labeled test fixture; the production worker performs the transition.

Pre-expiry reminders, recipient expiry notifications, general job observability/recovery UI and external invitation transport remain incomplete. Existing sandbox/live gates are unchanged; this is not official/legal notice delivery.
