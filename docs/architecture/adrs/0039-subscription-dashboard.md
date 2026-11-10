# ADR 0039: Authorized subscription dashboard projection

Date: 2026-11-10. Status: accepted for the current Free subscription lifecycle.

## Scope and sources

SAAS-SUB-001 with FS-04/05 requires `/workspace/billing/subscription` and an authorized subscription/effective-entitlement read. Full-source reconciliation and ARCH-04–07 / INT-01–06 / INT-09–12 apply. This bounded dashboard request does not authorize creating an unverified paid subscription, a second accounting engine or synthetic payment records. The existing frontend and backend FRSs agree on the displayed fields.

## Decision

`GET /api/v1/workspaces/{w}/subscription` establishes current owner membership (the current implemented billing authority), then reads the persisted subscription and its **contracted** plan version in a repeatable-read transaction. It reuses commercial `UsageQueries`/`Entitlements` and the catalogue's published/available workspace projection. It never substitutes the newest public Free version for the subscribed version. Managers and unrelated/revoked accounts receive privacy-safe 404; unauthenticated requests receive 401. No user-supplied role, entitlement or paid state is accepted.

The Next.js route is a private shell with client-side authorized fetching through the existing one-origin API. The API is no-store; existing private route/noindex headers apply. Abort handling and keyed workspace context prevent stale results from another context. Refresh clears prior data, so an outage cannot be mistaken for current figures. Read-only requests require no financial confirmation, mutation idempotency or new audit event. Existing subscription provisioning, capacity mutations and commercial overrides retain their own durable audit/recovery evidence.

Reuse existing durable tables and module interfaces; no migration, new dependencies or provider calls are necessary. The Java service owns the projection and all eligibility calculations. Prices are not calculated in JavaScript. Timestamp presentation uses Asia/Kolkata; no business billing dates are invented.

## Availability semantics

The database currently supports `active_free` only. Free has no recurring billing period/paid renewal/scheduled SaaS invoice; these are `not_applicable`, with nullable dates/amount and explanatory text. Subscription start is a separate persisted timestamp and must not masquerade as a billing-period start. Unknown values are never zero.

Add-on subscriptions, workspace email/WhatsApp metering and SaaS payment history have no authoritative implementation yet. Return explicit `unavailable` coverage instead of a fictitious empty list, zero usage or synthetic history. Sign-in OTP delivery is not workspace communication consumption. Rental and personal membership ledgers are not queried or merged.

Published catalogue alternatives are shown as comparison baselines with purchase unavailable; hidden/withdrawn and consumer offerings are excluded by the existing catalogue interface. Published capacity is not an active entitlement. Quote/tax/payment-provider integration is required before showing a purchasable upgrade. This does not implement SAAS-SUB-002 checkout.

## Verification and remaining scope

`SubscriptionIntegrationTest` uses PostgreSQL/Testcontainers to test persisted Free data, coverage/null semantics, quota/entitlement projection equivalence, preservation after public catalogue publication, billing-restricted read access, no-store, anonymous/manager/cross-workspace/revoked-owner denial. `web/tests/subscription-dashboard.mjs` verifies real local sign-in/provisioning/navigation, all dashboard headings, private API and HTML, failure recovery, workspace changes, keyboard, axe and mobile/desktop reflow. Exact results appear in release status.

SAAS-SUB-001 remains Partial across the full required paid lifecycle until real paid periods, invoice estimates, active add-ons, channel metering, priced eligible upgrades and SaaS payment history can be sourced. Add those through the owning commercial/communications workflows with ordinary authorization, immutable evidence, concurrency/provider recovery and pagination, then extend this projection and its tests. Do not create unconnected dashboard-only ledgers. Full FS-04/05 and Phase 7 remain incomplete.
