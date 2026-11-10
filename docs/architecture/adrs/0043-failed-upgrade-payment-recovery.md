# ADR 0043: Failed upgrade payment recovery

Date: 2026-11-10. Status: accepted for local sandbox upgrade payments; renewal grace/live collection pending.

## Scope and decision

SAAS-SUB-005 / FS-04/05 requires authoritative failure, retry, grace and restriction states without duplicate charges. The implemented payment domain is local workspace upgrade simulation; no renewal invoice/mandate or approved live grace policy exists. A failed discretionary upgrade cannot make the existing Free/paid subscription delinquent. Preserve current access, display grace as not_applicable with no deadline, and implement real durable bounded retry of the same upgrade. Renewal grace/restriction application remains mandatory pending work, not a fabricated countdown or a completed feature.

V45 introduces immutable effective-dated sandbox retry policy. Initial explicit development policy `sandbox-upgrade-retry-v1` permits three total attempts per confirmed change and a 30-second cooldown after definitive failure. New confirmed changes bind that policy version. Manual confirmation is required; no automatic provider collection or retry worker exists. New approved policies require new versioned rows, never edits to referenced policy. No commercial prices, provider approvals, legal grace duration or renewal policy is invented.

Existing changes keep null retry policy, and their historical attempt counts are presented as unavailable. Pending/unknown legacy outcomes can still reconcile. A definitively failed legacy request requires fresh priced terms; the migration does not reconstruct missing historical attempts or manufacture failure times. This is an additive migration and must precede API deployment.

## State and concurrency

`SubscriptionUpgrades` remains the sole writer. `GET .../subscription/payment-recovery` returns current plan/subscription/workspace status, explicit grace applicability, restrictions, latest 25 upgrade requests and as-of time in a repeatable-read transaction. `POST .../changes/{id}/retry-payment` uses strong change If-Match and actor/workspace/logical-operation idempotency under the existing membership/subscription locks. It rechecks authority, sandbox mode, active workspace, exact quote expiry/subscription/policy/effective grants, offer availability, pending downgrade/cancellation/upgrade and capacity before incrementing the attempt and setting awaiting_payment. Audit and intent commit together. No payment event is created merely by retry confirmation.

Failure records a durable timestamp. Repeated definitive failure does not reset the cooldown or add another event. A failed attempt cannot directly become successful/unknown: an eligible retry must first create the next attempt. Unknown outcomes cannot start another attempt; reconcile on the original attempt. Terminal applied status cannot be undone by reordered failure. V45 uniquely keys each simulated outcome by change/attempt/outcome; the existing unique successful-settlement constraint prevents a second activation. Legacy events remain immutable with unknown attempt metadata.

Retries preserve the original quote/amount/recipient/purpose. Expired/changed terms and exhausted attempt budgets require a fresh reviewed upgrade, with new quoted terms and explicit confirmation. The budget is per confirmed request, not an account-wide anti-abuse/charge limit. No client-provided amount, role, retry count or successful state establishes financial truth. UI recovery after a lost response reuses the exact key/body/version. A distinct concurrent retry gets stale-version conflict; audit failure rolls back the attempt increment.

## UI and recovery

`/workspace/billing/subscription/payment-recovery?workspace={id}` is linked from the dashboard and failed upgrade rows. It shows failed/awaiting/unknown/applied status, last recorded failure, current attempt and limit, cooldown boundary, denial reason, quote amount/recipient/expiry, grace explanation and restrictions. Refresh rechecks current eligibility. Explicit confirmation supports cancellation and unknown-network retry. Reload/context changes read persisted scoped state; no browser storage or optimistic paid result. Live mode shows unavailable and hides simulated outcome controls.

Only requested new capacity is withheld after an upgrade failure; existing subscription and record access are unchanged, subject to separate permission/security restrictions. No records are deleted, no grace timer begins and no rental/personal payment is allocated. Audit is durable; external failure reminders and live provider callbacks are not claimed.

## Remaining requirements

Renewal invoice/payment intent generation, approved renewal grace/restriction transitions, recovery after renewal settlement, mandate retries, external provider signed callback/reconciliation and account-wide abuse/rate policy remain unimplemented. Full SAAS-SUB-005 and FS-04/05 remain Partial. Existing essential tenancy/signing/financial workflows also retain their separately tracked gaps. See release status for actual tests and unavailable checks.
