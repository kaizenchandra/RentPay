# ADR 0041: Scheduled subscription downgrades

Date: 2026-11-10. Status: accepted for local sandbox paid-to-Free; live and paid-to-paid policy pending.

## Decision and ownership

SAAS-SUB-003 / FS-04/05 requires reviewed capacity/feature changes, correct billing-boundary scheduling and preservation of excess records. Extend commercial's existing subscription and centralized entitlement evaluator. No dependency, payment backend or invented price is introduced. The persisted paid period end is the local effective boundary. Free requires no new charge or renewal settlement. Other targets remain explicitly unavailable until reviewed renewal pricing and verified payment behavior exist.

`SubscriptionDowngrades` owns review, schedule, cancel, retry and apply. V44 adds immutable scoped review snapshots, request lifecycle, idempotent command records, RLS and bounded worker claim/failure functions. Commands lock membership/override authority, subscription, request and ordered capacity grants as needed. Replays reauthorize. Reviews bind actor, exact subscription/target versions, boundary, quota usage and entitlement fingerprint and expire after ten minutes. Confirmation rejects changed usage or terms and conflicting open upgrades/downgrades.

Feature forecasting uses known policy at max(boundary, current database time). A delayed job must not evaluate newly changed policies at a historical time. Unknown future configuration is not promised. Changed policy, overrides or available target version require fresh review rather than silently substituting terms.

At the boundary the worker reauthorizes the original requester and revalidates terms. One transaction updates plan/status/version, clears the paid period, changes allowances while retaining used/reserved counters, marks applied and records audit plus private account notice. There are no deletes or changes to retained property, tenancy, finance or document records. External collection/refund is absent. No applied status is shown before commit.

V44 removes only the capacity constraint requiring used + reserved <= allowance. Nonnegative constraints remain, and atomic consumption predicates continue enforcing capacity for increases. An over-limit balance is a valid retained state, not corruption. Remaining capacity is clamped to zero and excess is explicit. Capacity repair may reconcile valid retained source evidence above allowance; it may not invent or delete source records to satisfy a lower limit.

## Durable recovery

The default worker runs every 30 seconds after a 60-second initial delay; existing jobs-enabled configuration controls execution. Claims are bounded to 25, lease for 60 seconds, use SKIP LOCKED and fence outcomes with a token. Transient failures use bounded exponential retry (2^attempt seconds, maximum 300), at most five attempts per retry cycle. Exhausted crashed leases also transition to review_required with audit. Permanent eligibility/authority/terms failures need owner review.

Cancel fences an outstanding worker claim. Explicit retry is versioned/idempotent, rechecks original authority and terms, and resets the retry-cycle count. If requester authority or terms changed, cancel and create a fresh review; never rewrite the original actor or force applied in SQL. An audit/notification failure rolls back application; a separate failure transaction records retry state. Process/database loss is recovered by lease expiry. Notices are private in-app records; no external delivery guarantee is made.

## Verification and limits

PostgreSQL tests cover boundary timing, duplicate/stale claims, cancellation, replay, authorization revocation, changed usage, exhausted leases, audit rollback, retry and retained over-limit source repair. The browser test shortens only its isolated workspace's billing clock, restarts the actual worker and verifies retained records, blocked expansion, status and notices. See release status for executed checks.

Paid-to-paid renewal settlement, approved live commercial/refund/grace policy, Diamond contract limits and full tenant essential-access journeys remain incomplete. Existing local paid subscriptions originate from the explicitly simulated upgrade adapter. This working local downgrade does not establish real paid commerce or completion of the full requirement.
