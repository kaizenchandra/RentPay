# ADR 0042: Subscription cancellation through the shared boundary transition

Date: 2026-11-10. Status: accepted for local sandbox; live commercial/retention policy remains pending.

## Decision

SAAS-SUB-004, FS-04/05 and ARCH-04–09 require explicit cancellation consequences, durable effective dates, preservation of evidence and recoverable access changes. Reuse commercial's V44 reviewed boundary transition and Java worker rather than creating a competing subscription writer. New canonical endpoints under `/api/v1/workspaces/{w}/subscription/cancellation` distinguish cancellation from an ordinary downgrade. The dedicated UI is `/workspace/billing/subscription/cancellation?workspace={id}`.

A local cancellation ends simulated paid access at the persisted paid period end and continues the workspace on the configured Free version. It does not close/delete the workspace, suspend security access, terminate tenancies or alter personal memberships. Current usage and records survive; new capacity is restricted by Free allowances after application. Free workspace closure and live cancellation have no approved policy and are unavailable. No invented legal retention period, refund, credit or real payment is created.

The immutable review snapshot gains optional `cancellation` terms: versioned local policy, explicit refund/credit applicability with nullable decimal amounts/currency, retention requirements and access consequences. Missing/null terms identify existing ordinary downgrade reviews; no historical review is rewritten. Cancellation confirmation rejects ordinary downgrade reviews. Requests, original actor, effective boundary, attempts and applied time use the same scoped immutable review/request/command tables. No schema migration or new dependency is required; deployment is additive to V44's JSON snapshot contract. API/worker must deploy together before enabling cancellation UI; an older worker cannot produce cancellation-specific audit/notice labels.

Cancellation review replay uses its own logical idempotency operation before looking up the current target, so the original review can be recovered even after application. Confirmation, withdrawal and retry reuse the same scoped transition command records, shared pending-change exclusion, strong ETags, current owner authorization and transaction locks. Cross-endpoint replay cannot create a second transition. Shared downgrade history also labels cancellation requests and retains recovery access.

## Application and recovery

The worker reauthorizes the original requester and verifies subscription, target, cancellation policy version and effective entitlement fingerprint. It updates plan/allowances, applied state, cancellation audit and private in-app notice atomically. No domain record deletion or external payment occurs. An audit failure rolls back access changes. Existing leased claims, bounded retry, exhausted-lease recovery and reviewed retry continue to apply; see ADR 0041. Low-level job failure audits retain the shared engine's downgrade action names and link the request containing its cancellation policy.

Owner withdrawal before application fences a claimed worker. The shared request status is `cancelled`, displayed as **withdrawn** for cancellation requests to avoid implying that paid access ended. Application is separately `applied` with actual timestamp. Withdrawal does not refund or reverse an already-applied cancellation; a later paid purchase needs a new authorized quote. Policy/authority changes require withdrawal and fresh review, not rewriting the original request. Notices are account-private and transactional; external email/SMS is not claimed.

## Retention and money

Each cancellation stores its reviewed preservation policy: no automatic deletion, existing record permissions retained, no purge scheduled. Production retention duration, legal holds, exports/deletion scheduling and provider-specific signing continuity require approved policy and implementation. This is an explicit gap, not an unlimited-retention promise. Refund/credit status is `not_applicable_sandbox` and amounts are null because prior local payments were simulated; no synthetic credit note or refund receipt exists. Live settlement, refund eligibility/approval, credit accounting and uncertain-provider reconciliation remain required before enabling live cancellation.

## Evidence and limits

Tests cover cancellation-specific immutable terms, version rejection, scoped denial, original-review replay after application, one open transition, withdrawal fencing, Free continuation, retained membership, revoked authority and audit-failure rollback/recovery. The browser journey covers real local sign-in/upgrade, excess inventory, review/confirmation, withdrawal, committed-response loss, reload, worker restart/application, retained six units and blocked expansion, private notice and accessibility checks. Exact executed results are in release status.

Full requirement status remains Partial. Signing during cancellation cannot be certified until agreement/signing/provider workflows exist; the cancellation engine neither deletes nor mutates agreements/documents. Full Phase 7 and production operational readiness are not claimed.
