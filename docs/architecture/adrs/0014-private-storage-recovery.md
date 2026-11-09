# ADR 0014: Private storage integrity and missing-file recovery

Status: Accepted for the local Phase 1 storage foundation, 2026-11-09.

## Problem and scope

A quarantined transfer receipt proves that bytes were accepted at that time. It does not prove the object remains present after storage loss or corruption. Provide account-owned integrity review and exact-byte restoration, without rewriting the receipt, clearing quarantine or manufacturing malware results. This advances FS-21 and ARCH-10 recovery foundations; full document workflows remain Partial/Pending.

The documents module owns V17 `upload_storage_review` and append-only-runtime `upload_storage_command`. Composite upload/account foreign keys and RLS enforce the same subject classification as V16. Platform/workspace roles confer no access. Current session validation precedes every read/write/replay.

## Commands and evidence

GET `/documents/{id}/storage` returns the last recorded observation with time, revision and strong ETag. The default is `unchecked`, revision zero, null checked time. It does not read object bytes or imply a current continuous monitor.

POST `.../storage/check` requires CSRF, ETag and actor-scoped Idempotency-Key. After subject authorization and upload-row locking, the local adapter checks regular-file type, bounded byte count and SHA-256. Outcomes are `intact`, `missing`, `mismatch` or `unavailable`; none implies scan clearance. Symlink objects are mismatches and a symlink/unavailable root cannot provide a valid result. Observation/revision, command result and audit commit atomically.

POST `.../storage/restore` requires the current missing observation, exact original size/SHA-256 bytes, ETag, CSRF and scoped key. It rechecks actual storage: a changed object is never overwritten. A matching object from a previously rolled-back audit may be reconciled. Current authorization precedes command replay; a changed key payload conflicts. Old exact retries return their recorded result, not a claim of current storage state; refresh the review for the latest observation.

The original immutable upload/expiry/digest and durable missing review precede restorative object I/O. After writing, verify the bytes before committing the new observation and audit. If the database/audit fails after writing, preserve the object and retry the same confirmation. No original receipt is edited, and the document stays quarantined. This is a bounded local filesystem operation, not a remote provider call held inside a database transaction. A future remote adapter requires durable dispatch/reconciliation under ARCH-08/INT-03 before use here.

## Local adapter publication and limitations

Superseding ADR 0013's atomic-move implementation, write and force a private temporary file, then create a new hard link at the UUID destination. This operation refuses an existing destination; if another object appears, reuse it only if its fingerprint matches. Remove the temporary name afterward. This avoids the replacement behavior that atomic moves may allow. Directory/file permissions remain 0700/0600; reads are bounded and use no-follow semantics. Unsupported filesystem operations fail closed. This POSIX sandbox adapter is not production object storage.

Files are private, and no content API is added. Scan availability remains explicitly `unavailable`. Retention/holds, automated orphan deletion, provider storage, scanner jobs/format validation, clean release, expiring access links, encryption/restore drills and full production readiness remain open. Do not delete mismatching evidence to force a restore; operator investigation is required. There is no claim that bytes remain intact after the recorded observation or that a hash check establishes safety.

## UI and verification

`/account/uploads` links quarantined records to `/account/uploads/{id}/storage`. The page displays filename, recorded size, account scope, observation/time and blocked scan access. Missing-file restoration requires original-file selection and explicit confirmation. Unknown responses freeze the same key/version/file for retry; stale versions require refresh. Account changes abort/ignore obsolete responses and clear private state. Safe file drafts remain in memory only.

Tests cover actual object removal, corruption and symlinks in isolated fixtures; subject denial/RLS; CSRF/preconditions; competing checks; stale confirmation; exact replay; audit rollback after physical restoration; preserved original receipt; and desktop/mobile keyboard/lost-response recovery. Browser failure injection removes only its own freshly created sandbox file, which the real restoration API replaces. See release status for actual results.

Reference: [Java 21 Files.createLink](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html#createLink(java.nio.file.Path,java.nio.file.Path)). No dependencies or versions changed.
