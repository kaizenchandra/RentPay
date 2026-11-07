# ADR 0005: Membership eligibility and reconciled usage

Status: accepted for the Phase 1 sandbox subset, 2026-11-07. SAAS-ENT-001–003 and SAAS-QUOTA-004/005 remain Partial.

## Delivered boundary

`/workspace/billing/usage?workspace=…` presents owner-authorized capacity, commercial eligibility and dated manager usage evidence. It uses generated contracts for `/workspaces/{w}/entitlements`, `/usage` and `/usage/history`. Managers and unrelated accounts receive privacy-safe 404. Entitlement eligibility never grants record or action authority.

Commercial `Entitlements` centralizes the existing invitation/acceptance guards. It evaluates the actual subscription state, its bound plan version, effective commercial policy version, explicit plan-capability mapping and workspace restrictions. V6 defines policy version 1 and two foundation capability rules; V7 binds existing published Free versions to them. Runtime access is SELECT-only for configuration, and updates/deletes of existing policy evidence are rejected. Future configuration must publish new versions, not rewrite historical rows. No dependency versions change.

Only sandbox membership is covered. Add-ons, time-limited grants, overrides, paid lifecycle, full rental feature matrices and administration of these configurations remain pending. They must not be inferred from these foundation rules. A missing required capability configuration fails closed. No browser-provided paid state, plan name or quota value authorizes creation.

## Restrictions and corrections

Invitation and acceptance re-evaluate `WORKSPACE_MEMBER_ADD` inside their authoritative membership transaction. Commercial restrictions return `WORKSPACE_RESTRICTED`; security suspension returns distinct `SECURITY_SUSPENDED`. Existing owner authority checks still run separately. No plan-name checks are scattered through those use cases.

Revocation remains a corrective security operation for the currently authorized owner, including when restricted/suspended or at capacity. It does not require a commercial purchase or successful entitlement lookup. The current supported policy advertises this preservation. Full billing-grace/downgrade and essential tenancy continuity are not implemented by this subset. Owner reads of commercial state remain available to explain the restriction; broader suspension policy still needs its domain contracts.

## Evidence and reconciliation

V6 creates `manager_usage_event` with unique allocation/kind keys and commit (+1) or release (-1) quantities. Backfill uses actual V5 allocation timestamps, including recorded releases; it does not invent historical activity. Runtime may SELECT/INSERT but cannot UPDATE/DELETE events. The commercial allocation, counters, event, membership transition and existing audit commit in one transaction. Duplicate membership commands do not create additional events.

Current manager usage is a stock, not a monthly consumption allowance. The snapshot compares its counter with active manager memberships, committed allocation records and the sum of usage events. If any disagree, it reports reconciliation required and null remaining capacity. Consumption also checks allocation/event totals under the grant row lock and refuses inconsistent counters. It does not repair or zero mismatched evidence. Operator correction commands remain pending; investigate the original records before a reviewed forward repair.

Rentable-item allowances remain visible, but used/reserved/remaining and reconciliation are null because inventory measurement is unavailable. This distinguishes absence of coverage from genuine zero. No payments, rental accounting or personal memberships are mixed into these capacity records.

History accepts inclusive date-only `from`/`to` in Asia/Kolkata, defaults to the latest 30 calendar days and allows up to 366 days. SQL uses the start of the first date through the exclusive start of the following day. Net change is the selected-period delta, not current usage. Chronological 25-item keyset pages bind cursors to actor/workspace/dates and include exact counts. Snapshot transactions use repeatable-read; private responses have no shared cache.

## Module boundaries and recovery

`WorkspaceUsageController` coordinates owner authorization from workspace and read projections from commercial in one transaction. `UsageQueries` and `Entitlements` do not depend on workspace application classes, preserving the workspace → commercial dependency direction. Commercial alone writes usage evidence/counters. Scoped projections may read membership count for reconciliation; no commercial command writes membership.

The UI preserves filters in the URL, aborts obsolete initial reads, ignores stale pagination, provides real empty/error/retry states and labels unconfigured upgrade checkout honestly. A database outage returns safe problem details; refresh recovers after restoration. Source-backed evidence references support investigation without exposing recipient contacts.

## Verification and outstanding gates

Backend integration tests cover cross-workspace/role denial, restriction denial/rollback, corrective revocation, duplicate release, unknown inventory coverage, deliberately inconsistent counter detection, India midnight boundaries, real repeated accept/revoke pagination, filter-bound cursors and denied event updates by the runtime role. Browser tests extend the real membership journey with full-capacity usage, generated schemas, URL date filters, empty history, keyboard/accessibility scans and mobile overflow. The restart script checks one accepted manager and the exact usage-event ID survive with reconciled counters.

Manual screen-reader, high-concurrency performance, full effective grant layering, generic reservations/expiry, financial usage funding, retention and production deployment evidence remain pending. These are not satisfied by this partial snapshot implementation.
