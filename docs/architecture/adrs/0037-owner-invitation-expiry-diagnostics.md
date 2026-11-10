# 0037 — Owner-scoped invitation expiry diagnostics

Date: 2026-11-10. Status: Accepted for the existing invitation expiry worker.

## Decision

Expose a read-only `GET /api/v1/workspaces/{w}/invitation-expiry` snapshot to current owners and render it on Members and invitations. A running container is not evidence that individual expiry work succeeded. Owners need to distinguish an expired deadline from committed expiry processing.

The workspace module owns the query. Current owner authorization and database scope apply before aggregating only that workspace's persisted invitation records. Capture a database timestamp and use a repeatable-read transaction so counts share one state/time basis. The existing workspace invitation index supports scoping; no migration or cross-module table writes are required.

Definitions:
- Total: every invitation record in the workspace, including history beyond the current list page.
- Open: stored pending with deadline strictly after snapshot time.
- Awaiting expiry: stored pending with deadline at or before snapshot time.
- Recorded expired: stored terminal expired, including historical invitations replaced later.
- Oldest overdue: minimum deadline among awaiting records; null with zero age when none exist. Age is server-computed seconds at the snapshot.

Accepted, declined and revoked states count only toward total. These metrics do not establish notification receipt, worker health or a service guarantee. No invitation, quota, notice or job state changes during a read. Billing restrictions do not gate this owner diagnostic read; revoked owners and managers receive privacy-safe denial.

The UI exposes definitions, IST snapshot/deadline, explicit refresh, loading/empty/error states and operator guidance. Failed requests clear old data rather than displaying zero or stale success. Aborted requests cannot overwrite a changed workspace. Recovery remains the existing worker reconciliation after database/runtime restoration; replacement remains a separate confirmed owner operation. No diagnostic button bypasses domain transitions or directly invokes global worker SQL.

## Verification and limits

FoundationIntegrationTest verifies empty/open/due/recorded transitions against PostgreSQL, accepted-state denominator semantics, age/reset behavior, private caching and anonymous/manager/other-workspace/revoked-owner denial. The extended real Compose expiry test stops the worker, observes backlog, injects a network error, refreshes, restarts the worker and confirms eventual completion alongside the replacement journey.

A queue-age alerting backend, per-record retry exhaustion/repair workflow, automatic refresh and representative high-volume load/query-plan measurements are not provided by this slice. Counts are for invitations only; this is not a general operations dashboard.
