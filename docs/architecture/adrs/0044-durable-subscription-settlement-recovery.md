# ADR 0044: Durable subscription receipt and activation recovery

Date: 2026-11-10. Status: accepted for explicitly local sandbox callbacks; live provider/renewal recovery remains pending.

## Problem and scope

SAAS-SUB-006 / FS-04/05 distinguishes payment received from successful entitlement activation. The earlier immediate sandbox adapter committed simulated payment and activation in one transaction. It could prove rollback, but could not represent a provider having already received money while activation failed. Add a distinct durable callback simulation and leased activation job using the existing commercial subscription writer. Keep the immediate simulator for existing test journeys; it is not live provider evidence.

The new adapter is an owner-authenticated, CSRF-protected endpoint, **not a signed live payment webhook**. Browser callback confirmations are clearly simulated; test clients may deliver the same authorized event while the page is closed. Actual provider identity/signature/replay windows, merchant references and reconciliation require the selected provider and remain unimplemented. No bank charge, refund or credit is fabricated.

## Receipt, worker and fencing

V46 adds `subscription_callback_event` and `subscription_settlement` with scoped composite change references, owner RLS and bounded security-definer worker claim/failure functions. Immutable callback events deduplicate workspace/change/event key; conflicting outcome under the same event key returns 409. Current authority is checked before replay. A paid callback inserts the existing purpose-separated simulated paid event exactly once and commits the receipt plus audit before activation. The change becomes review_required to reserve the existing one-open-change slot; its settlement state distinguishes queued activation from a fault. Pending callbacks remain payment_unknown without granting capacity. Duplicate or delayed pending evidence cannot undo received/applied state.

The Java worker claims up to 25 receipts with a 60-second fenced lease, reauthorizes the original receipt actor and invokes the existing upgrade writer without inserting another payment. It checks current policy/offer/entitlement/capacity/subscription terms; quote expiry is evaluated at receipt time, so a valid received payment is not invalidated merely by delayed processing. Late or conflicting paid evidence remains durable review work. Activation updates plan, quotas, change, receipt and audit in one transaction. A failure rolls that activation back while the previously committed receipt survives.

Default worker scheduling is every five seconds after a ten-second initial delay (`lmt.subscription.settlement-delay-ms` / `lmt.subscription.settlement-initial-delay-ms`). Transient failures use bounded exponential retry, at most five claims per cycle; crashed exhausted leases become review_required. A reviewed activation retry resets attempts for eligible resolved transient faults without collecting again. Policy/payment conflicts and reversals need operator reconciliation; that resolution/refund workflow is not yet implemented. A stopped database or worker can recover from durable intent and lease expiry rather than browser state.

## Reversal and concurrent changes

A reversal references an existing durable settlement; an unrecognized payment cannot manufacture a receipt. It appends immutable callback evidence, marks reversal_review, fences queued activation and preserves records. After activation, the current plan/grants remain intact while discretionary subscription changes are held for review. This is a tracked reversal case, **not completed refund/credit or automatic entitlement compensation**. A later duplicate paid callback cannot reactivate or erase the reversal. Essential access and payment history are not deleted.

Pending receipt activation occupies the same open-change slot used by upgrades; cancellation/downgrade confirmation cannot overtake it. Reversal holds also guard upgrade and cancellation/downgrade commands. Tests cover competing cancellation during an upgrade settlement. **Cancellation during renewal remains unimplemented** because there is no renewal collection/mandate engine; do not equate an upgrade race with that source scenario.

## API and UI

Existing payment recovery route adds receipt time, activation state/attempts/reason, explicit no-second-payment recovery and reversal review. `POST .../changes/{id}/sandbox-callbacks` records pending/paid/reversed simulation with versioned confirmation and event-key replay. `POST .../changes/{id}/retry-activation` binds confirmation to the settlement ETag. Existing GET change/recovery projections carry optional typed settlement data; old idempotency results may lack it and must be refreshed. Upgrade UI links to receipt recovery rather than offering another payment for an existing receipt.

No new dependencies, external effects, prices or duplicate entitlement engine. Deployment must apply V46 before API/worker and coordinate the new writer/worker versions. Do not run an old writer that lacks receipt guards alongside the enabled callback flow. See release status for tests and operational limits.
