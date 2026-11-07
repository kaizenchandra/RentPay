# ADR 0006: Durable welcome operations and recovery

Status: accepted for the Phase 1 job foundation, 2026-11-07. ARCH-08 and INT-03 remain Partial. FS-32's imports and broader recovery workflows are not implemented.

## Problem and bounded scope

The previous welcome worker left revoked-authority events pending forever. A batch of those events could repeatedly occupy the head of the queue. It also had no durable attempt history, lease or owner-facing recovery controls.

Extend the existing `workspace_outbox` and in-app welcome handler, preserving its module ownership and notification identity. Do not introduce a second queue or claim that a welcome handler implements imports, provider delivery, exports, payment recovery or all general jobs. `WorkspaceWelcomeDelivery` schedules work; `WelcomeOperations` owns claims, execution, projections and recovery commands. No external effect is performed by this handler.

## State machine and transaction boundaries

`pending → running → delivered`, with transient failures returning to `pending`, exhausted attempts becoming `failed`, and revoked authority/unsupported handlers becoming `blocked`. Authorized cancellation produces `cancelled`. Manual retry moves only failed/blocked work back to pending after review; it is not delivery success.

A short claim transaction uses `FOR UPDATE SKIP LOCKED`, assigns a unique lease token, increments recorded/cycle attempts and inserts attempt evidence. Processing runs in a separate transaction. It locks the operation, checks the matching unexpired token and revalidates the original creator's current owner authority. A shared membership-row lock prevents authority updates from racing the DB-only business commit. The notification, delivery state and audit commit together. The existing notification event primary key deduplicates repeated attempts.

Expired leases are reclaimable. A stale worker cannot commit after lease replacement or cancellation. If recording a transient failure also loses the database, the committed lease remains recoverable after expiry. Queue claims are bounded to ten at a time. Five attempts are allowed per cycle, with exponential retry delay plus jitter; exhausted jobs need an authorized, reasoned manual retry. Recorded history is retained when a new cycle begins. Revoked jobs become blocked and no longer starve later authorized jobs.

The default lease is 30 seconds (`lmt.workspace.lease-seconds`, 1–300). Poll interval is `lmt.workspace.delivery-delay-ms` (default 1,000); `lmt.workspace.initial-delay-ms` defaults to zero and allows isolated worker tests. These are development execution defaults, not service-level guarantees. Work is persisted in PostgreSQL; the scheduler only wakes the worker.

Reference: [PostgreSQL row locking](https://www.postgresql.org/docs/17/explicit-locking.html). The chosen locks coordinate concurrent writers; no DB transaction is held open around a network/provider call.

## API, UI and authority

Owner route: `/workspace/operations?workspace=…`, linked from setup. List API: `GET /workspaces/{w}/operations`, bounded 25-item pages with actor/workspace-bound cursors. `GET /operations/{id}` returns operation state, strong ETag and the latest 20 recorded attempts. Claim tokens and raw exceptions are never projected.

`POST /operations/{id}/retry-failed` and `/cancel-request` require current owner authority, CSRF/Origin, exact If-Match, a scoped idempotency key and a normalized 10–300 character reason. Retry also checks that the original creator still has owner authority and that the handler exists. Authority is not implicitly transferred. Same confirmation replays its durable response; changed confirmation with the same key conflicts. Retry returns 202 with a polling Location, never a delivered claim.

Cancellation locks the same operation row as execution. It can stop pending/uncommitted work; a delivered operation is not recalled. Retry/cancel are explicit commands for this handler, not arbitrary job payload or SQL execution APIs. A future provider job needs its own permission, uncertain-outcome reconciliation and cancellation semantics.

The screen shows workspace context, actual status, recorded attempts, cycle budget, next attempt/lease times, reason-coded guidance and recent history. It confirms the target/version/reason, retains unknown-outcome command keys in memory and provides explicit retry. Pending work polls at four-second intervals for at most ten checks, then offers manual refresh. Private pages/APIs remain no-store/noindex. No sensitive draft is written to browser storage.

## Persistence and upgrade

V8 is additive except for widening the existing state constraint. It adds lease/version/schedule/error metadata, operation attempts and account-scoped recovery-command evidence. V1–V7 are unchanged; delivered notifications and existing event IDs remain intact. No past attempt history is fabricated: old delivered operations can have zero **recorded** attempts. Updated timestamps added by migration are tracking metadata, not historical delivery timestamps.

Queue/attempt tables are worker infrastructure with narrowly granted runtime access; all browser projections resolve the resource's workspace and check current owner authority before returning records. Command reasons/results use actor RLS. No platform administrator is given blanket private access by this change.

## Recovery runbook and verification

1. Read the current operation and recent attempts. Confirm workspace, state and reason; never edit the job state to force production success.
2. For revoked authority, resolve the original creator's legitimate access through the authorized membership process. This slice does not implement owner succession.
3. For transient failure exhaustion, repair the dependency first. Request retry with an explanation and current ETag; retain the same key if the response is uncertain.
4. For a stuck running worker, inspect its lease and allow normal reclaim after expiry. Do not reuse a stale token or publish directly.
5. Cancel only work not committed. Existing notifications remain and cannot be recalled through cancellation.

`OperationsIntegrationTest` uses actual PostgreSQL failures, concurrent claims, expired leases, revoked authority, retry replay, cancellation fencing and eleven blocked jobs before valid work. Browser tests use a **labelled test-only failed-state fixture** to exercise confirmation and a lost retry response; actual failure handling is independently verified by the PostgreSQL trigger-failure test. The restart script verifies stable operation identity and one notification alongside previous foundation recovery checks.

Live provider jobs, imports/partial rows, generic quotas/reservations, private storage, full monitoring/alerts, retention, operator role administration, manual screen-reader and production recovery targets remain pending. This is durable in-app welcome delivery, not SMS/email or production readiness.
