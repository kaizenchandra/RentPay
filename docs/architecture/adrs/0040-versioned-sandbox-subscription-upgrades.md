# ADR 0040: Versioned sandbox subscription upgrades

Date: 2026-11-10. Status: accepted for local simulation; live commerce remains blocked on configuration and provider implementation.

## Scope and decision

SAAS-SUB-002, FS-04/05, ARCH-04–09 and INT-01–06/09–12 require versioned priced confirmation, authoritative calculations, idempotency and verified settlement before activation. The repository had no approved paid prices, taxes, proration terms or payment adapter. Implement the complete local simulation path without making a live financial claim. No new dependencies or external provider calls are added.

V42 adds immutable configured sandbox offers/withdrawals, quotes, change requests, idempotent command results and purpose-separated simulated payment events; subscriptions gain a version and paid-period terms. V43 adds composite workspace foreign keys and mandatory paid-term constraints. Published catalogue prices remain unchanged. Offers are keyed to exact source/target plan versions; no implicit choice of latest public terms can rewrite a contract.

`SubscriptionUpgrades` is the commercial writer. Owner authorization and local sandbox identity/workspace gates precede commands and replay. Commands acquire the existing membership/override lock, then subscription row and ordered capacity grant locks. Effective-grant fingerprints bind the quote to the reviewed target eligibility, including temporary overrides. A changed/expired override, policy or subscription requires new review. The target cannot reduce current allowance or remove contracted capability bindings. Feature differences come from the centralized entitlement evaluator, not plan-name conditions.

Quotes expire after ten minutes and bind exact offer, source subscription version, target version, policy/effective-grant fingerprint, actor, capacities, configured feature changes, recipient, interval and decimal money. BigDecimal uses two-decimal HALF_UP arithmetic. `none_free_only` gives Free no credit. Configured `unused_time_credit` credits the paid base amount by remaining milliseconds divided by total period milliseconds, fixed at quote time; tax is applied to the resulting base at the configured test rate. Excess credit and expired periods fail closed. This is an explicit sandbox rule, not an approved Indian tax/refund policy. No client-supplied amount, tax, role or paid flag establishes financial truth.

Confirming an unexpired quote records an awaiting-payment change. It does not activate or increase capacity. One open change per workspace prevents competing checkouts. There is no external effect before durable intent: the separate, clearly labeled sandbox outcome endpoint simulates provider evidence only after confirmation. Failed/unknown outcomes preserve the current subscription; unknown outcomes use the same change for reconciliation. Expired quotes cannot receive a new successful simulated settlement: mark failed and review fresh terms. Replayed commands return their original result; use current-status reads to see subsequent changes.

A successful sandbox settlement atomically writes an immutable `workspace_saas_upgrade` event, updates subscribed plan/version and actual period, preserves used/reserved counts while increasing allowances, updates change status/time and writes audit. Audit/database failure rolls all of that back; the confirmed request remains retryable. Duplicate success cannot grant twice. Later failed/unknown signals cannot reverse an applied upgrade. No approved-credit bypass is implemented.

Effective time is actual verified sandbox settlement. The interval begins at activation and ends after the configured one/twelve calendar months in Asia/Kolkata. No delayed scheduled activation or automatic renewal collection is configured. The dashboard shows the recorded period and new effective grants; rental payments and personal membership billing remain separate.

## UI and recovery

The subscription dashboard links to `/workspace/billing/subscription/upgrade`. UI presents source/target plan, target capacity, configured feature differences, price/credit/tax/final amount/recipient, interval, quote expiry and the effective-time rule. Explicit confirmation is separate from quote generation and simulation. Pending/unknown/applied statuses survive refresh. Unknown network responses retain the same key/body for retry. No sensitive drafts are persisted in browser storage. History is explicitly the latest 25 upgrade requests, not a complete SaaS invoice ledger.

`scripts/configure-sandbox-upgrade.py` requires explicit test amount, tax rate, interval and credit policy and only targets the existing local Compose database. It copies already implemented source capability bindings, not unimplemented paid features, and audits configuration. No default prices are seeded. Test fixtures use INR 100 and 18% solely as arithmetic examples, never as advertised pricing/tax advice.

## Limits

Live pricing/tax/proration/merchant approval, a real provider adapter with signed callbacks/API reconciliation and durable dispatch recovery, production invoice/receipt/credit accounting, renewal lifecycle, scheduled activation, Diamond contract capacity and production operational gates remain unimplemented. Simulation cannot run in live identity or live workspaces. Do not interpret an active_paid record in this local sandbox as a real bank payment. SAAS-SUB-002 remains Partial with simulation recorded separately.

## Evidence

`SubscriptionUpgradeIntegrationTest`: persisted quotes, server arithmetic, expiry, missing/stale version, concurrent confirmation, changed-key/body rejection, unknown→paid recovery, paid-period credit, revoked/cross-workspace/live-workspace denial, duplicate/reordered outcomes, effective-grant change invalidation and injected audit rollback/retry. Browser test `subscription-upgrade.mjs`: real login, configured test offer, quote, cancel confirmation, lost-response replay, unknown result/reload/reconcile, dashboard/capacity activation, keyboard, axe and mobile/desktop reflow. Release status contains actual command results and unavailable checks.
