# ADR 0013: Account-scoped sandbox upload quarantine

Status: Accepted for Phase 1 storage foundations, 2026-11-09. FS-21, ARCH-06/10 and INT-03/05 remain Partial.

## Scope and ownership

The documents module owns V16 `private_upload`, its account-owned intent metadata and the `PrivateObjectStore` interface. A working `/account/uploads` staging UI transfers actual test bytes. It is not the personal Premium vault, a tenancy document service or proof of official evidence. No workspace, tenancy, police, provider or support access is inferred from account ownership.

The production baseline remains private S3-compatible storage with approved scanning and authorized expiring retrieval. No provider is configured in this repository. The only current adapter is explicitly local-sandbox POSIX storage, enabled through the existing guarded `IdentityConfig`. Non-sandbox storage fails closed. There are no new dependencies or version upgrades.

## Intent, transfer and quarantine

An authenticated subject creates a 15-minute intent binding filename (display only), declared type, exact length and SHA-256. The request key is actor-scoped; replay returns the original intent representation, changed metadata conflicts. The sandbox's 1 MiB maximum, 20 new intents/account/hour and PDF/PNG/JPEG allowlist are technical test limits, not commercial membership benefits. All writes require CSRF and current session authorization.

Raw transfer uses `/documents/{id}/finalize` with the original strong intent ETag. The intent ID and fixed digest identify one immutable logical transfer; repeating those bytes returns the same quarantined state. This endpoint completes transfer only, not scan approval. Read at most declared size plus one byte after authorization and row lock; reject mismatched size/digest/header signature. Header matching is not full format validation or malware clearance. No clean state or download endpoint exists while scanning is unavailable.

RLS and explicit account filters isolate every metadata query; scope is transaction-local. Lists paginate 25 records in stable UUID order. Private endpoints/pages are no-store and noindex. Unknown outcomes keep the same file/intent in browser memory; reload exposes persisted status and allows reselection of the matching file for an unexpired intent. No browser storage is used. Quarantined rows are immutable, including through runtime updates; no runtime delete grant exists.

## Storage and recovery

Objects use server UUID paths beneath the sandbox inbox's sibling `quarantine` directory, outside web assets. Files are 0600 and the directory 0700. A temporary file is forced to disk and atomically moved. Existing target bytes must match before reuse; symlink targets/root are rejected. The durable intent is committed before object I/O. Metadata transition and audit commit together. If the audit/transaction fails after object creation, the private object remains bound to its intent and exact retry reconciles it without another object or audit event. There is no irreversible provider effect.

On interrupted transfer, retry the same confirmation. After reload, inspect **Your upload intents**; if still awaiting, select **Resume** and reselect the original bytes. Expired unfinished intents require a new intent. Quarantine cannot be cleared by an operator upload or a client field. No automated scanner retries are implied: `scanStatus=unavailable` is explicit.

Retain the database and `.local/quarantine` together for local recovery; do not delete objects to fix a failed command. Uncommitted temporary files and expired-intent objects remain private. Automated orphan cleanup, retention holds, deletion requests, scanner jobs, clean release, sharing, expiring download grants, encrypted production storage and restore drills remain Pending. A reviewed cleanup policy is required before automated deletion. File forcing does not establish a tested power-loss or disaster-recovery guarantee.

## Verification and references

UploadIntegrationTest exercises subject denial/RLS, CSRF, ETags, spoofed headers, size/digest conflicts, expiry, concurrent replay, audit rollback with a surviving private object and immutable quarantine. StorageModeTest verifies production fails closed. Browser tests exercise actual transfer, lost-response recovery, refresh, schema conformance and accessibility scans. See release status for actual command results.

Implementation references: [Java 21 Files](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html) for POSIX/atomic operations and [Spring error response ordering](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html) for the observed validation-handler precedence correction. LMT's advice now precedes Boot's order-zero handler, preserving canonical 422 field errors.
