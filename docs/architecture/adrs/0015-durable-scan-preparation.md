# ADR 0015: Durable scan preparation without invented malware clearance

Status: Accepted for Phase 1 foundations, 2026-11-09. FS-21, ARCH-08/10 and INT-03 remain Partial.

## Scope and dependency boundary

Every quarantined upload needs visible, recoverable processing status. The repository has no approved/configured malware or format-validation adapter. Implement the real durable prerequisite operation now: check private storage, then record why scanning cannot proceed. Do not introduce fake clean/infected results, simulated scan success, public objects or downloads. `scannerMode=unavailable` describes this configuration; it is independent of job status.

V18 adds documents-owned `document_scan_job`, `document_scan_attempt` and subject-RLS `document_scan_command`. V19 adds a composite job/account foreign key to recovery commands. V19 is a forward migration because V18 had already been applied locally. One job per immutable upload; original quarantine evidence remains unchanged. Runtime cannot delete jobs/attempts or update/delete command evidence. Job/attempt rows are worker infrastructure metadata (no filenames or bytes), following the established welcome-worker boundary. Browser projections first authorize the account against private_upload; commands additionally carry a database-enforced subject relationship.

The queue envelope includes event ID/type/schema, upload/aggregate version, account scope/actor, correlation, causation and creation time. New uploads commit quarantine, scan intent and audits together. Existing quarantined uploads are backfilled with current-time queue/audit evidence; no historical attempt or scan is fabricated. During owner-only backfill, FORCE RLS is temporarily relaxed for the table owner inside the migration transaction and restored; runtime RLS stays enabled.

## State machine and leases

`queued → running → blocked` is the ordinary current path. The worker checks exact stored size/digest and records STORAGE_MISSING, STORAGE_CONTENT_MISMATCH, STORAGE_UNAVAILABLE, SUBJECT_AUTHORITY_UNAVAILABLE or SCANNER_UNAVAILABLE. Intact bytes do not establish malware safety. There is no completed/clean state in this foundation.

Claims use PostgreSQL `FOR UPDATE SKIP LOCKED`, at most five jobs per tick, a fresh lease token and durable attempt row. Completion rechecks the unexpired token before and after bounded local I/O, and revalidates the upload/account/quarantine relationship under transaction-local subject scope. This does not require a still-open browser session: the account-owned subject relationship is the authority. Account suspension/deletion policies must integrate here when those workflows are introduced.

Database/transaction failures schedule bounded exponential delay with jitter; if recording failure is also unavailable, lease expiry permits recovery. Expired claims finish their previous attempt as lease_expired and cannot later commit. Automatic cycles stop after five attempts. Blocked prerequisites do not spin automatically. Cancel/recheck serialize on the job; cancellation clears the lease and fences an unfinished worker. A separately confirmed recheck from blocked/failed/cancelled resets only the cycle count, preserving cumulative attempts and evidence.

Defaults: one-second poll, one-second initial delay, 30-second lease (validated range 1–300), five automatic attempts per cycle. Properties are `lmt.documents.scan-delay-ms`, `lmt.documents.scan-initial-delay-ms` and `lmt.documents.scan-lease-seconds`. These are development worker controls, not an operational SLA or commercial limit. Future remote scanners must use durable dispatch/reconciliation and approved data policy; never add remote provider calls inside this local completion transaction.

## API/UI and recovery

GET `/documents/{id}/scan` returns a strong operation ETag, status, current/cumulative attempts, next eligible time/lease, blocker and the latest five attempts with a truthful total. Shared job locking keeps the detail snapshot coherent. No lease token or file contents are exposed.

POST `.../retry` and `.../cancel` require current subject authority, CSRF, ETag, reason and account-scoped idempotency. A changed confirmation conflicts. Retry returns 202 with a durable status Location; this is acceptance, not scan success. Exact replay returns the original command result even after a worker advances the job; GET supplies current state. Cancellation never deletes quarantine or recalls an external result. This implementation has no external scanner side effect to recall.

`/account/uploads/{id}/scan` is linked from private uploads. The UI provides RHF/Zod reason validation, confirmation, linked field errors, unknown-outcome retry, conflict refresh and account-change clearing. It polls queued/running state at most ten times, pausing during edits/confirmation; manual refresh remains available. It explains unavailable scanning and the difference between accepted, running and blocked.

Recovery: inspect the recorded blocker and latest attempts. Repair storage through the existing recovery journey if appropriate, preserving mismatched evidence. Confirm **Review prerequisite recheck** with a reason; a missing scanner will still block. After an uncertain response use **Retry same scan command**. After stale-version rejection refresh before reconfirming. Do not manually reset attempt counts, delete command history or mark an object clean.

## Remaining work and evidence

Actual malware/format-validation adapters, scanner-version/digest evidence, clean release, expiring retrieval, S3/encryption, holds/retention/privacy requests, orphan cleanup, alert ownership and deployment drills remain open. This does not complete NOTIFY-001, TEN-PREM-002 or any domain-specific document requirement.

ScanOperationsIntegrationTest exercises separate PostgreSQL worker claims, current authority, subject FK, missing/stale preconditions, cancellation fencing, exact replay, audit rollback, expired-lease takeover, backoff, exhaustion and preserved attempt history. UploadIntegrationTest verifies a failed upload transaction leaves no queued scan. Browser tests use actual uploaded files and the real worker to observe blocked prerequisites, cancel with a lost response, recheck via 202 and inspect persisted attempts on desktop/mobile. See release status for executed results. No dependencies or versions changed.
