# ADR 0007: Explicit commercial review authority

Status: accepted for the Phase 1 read-only plan review subset, 2026-11-08.

## Scope and boundary

FS-03 and FS-06 require explicit platform authority. Workspace ownership, co-ownership and manager membership do not establish this authority. Introduce `commercial_review_grant`, owned by the catalogue module, for reviewing versioned terms only. It is not a general administrator role or a support grant. Draft editing, publication, capability configuration and subscriber transitions remain pending.

V9 stores account, expiry, revocation, reason and operator attribution. Every grant insertion/update writes a separate audit row including the database actor in the same transaction. Runtime access is SELECT-only; it cannot issue/revoke grants, change plan terms or mutate grant audit. No grants are seeded. Only trusted operators can establish this prerequisite. Production onboarding, MFA and role governance remain open; existing identity is sandbox-only.

The local operator script is restricted to the fixed local Compose database and existing sandbox accounts. It accepts a 10–300 character reason and 1–24 hours, supports explicit revocation and invokes psql without shell interpolation. OS attribution is recorded as an operator label, not verified human identity. Deployment credentials must not be distributed through this script. Use revocation rather than deletion, preserving evidence.

## Read operation

`GET /api/v1/admin/plans?audience=workspace|consumer&cursor=` requires a session and a current, unrevoked grant on every request. `PlanReview` returns at most 25 rows ordered by immutable UUID, a next-page selector, matching count, read timestamp and access expiry. The cursor is a selector, never a permission grant. Invalid filters fail explicitly. Audit, count and projection share one repeatable-read transaction; an audit failure prevents delivery. Authorization uses the request's database snapshot; concurrent revocation applies to subsequent requests, including pagination. Previously viewed information cannot be recalled.

The projection contains commercial terms, draft/published state, dates, limits and decimal-string prices. It contains no subscriber, rental, police or grant-recipient records. Public `CatalogController` remains unchanged and still excludes drafts/future terms. Admin responses use no-store. Read-only routes grant no POST authority.

## Frontend and recovery

`/admin/plans` and `/admin/consumer-plans` use generated OpenAPI 0.7.0 types and the existing same-origin client. These routes are no-store/noindex. They show loading, empty, failed, forbidden, expired-session and retry states, with separate audience navigation and URL pagination. Published is explicitly distinct from currently effective. Historical contracts retain their references. No write buttons imply unimplemented publication.

Refresh/focus rechecks authorization and clears old results; grant expiry clears the screen and rechecks the backend. Logout uses existing session/draft clearing. The browser keeps no persisted copy of private version data. No irreversible effect is retried by this screen; refresh produces another intentional review-audit event.

## Verification and next step

Foundation PostgreSQL tests verify owner denial, explicit grant, no-store, successful-read audit, expiry/revocation, page reauthorization, draft exclusion and denied runtime writes. Browser tests provision and revoke a grant for their own sandbox account using the operator script, verify generated response schema, keyboard disclosure, mobile overflow, axe and private route headers. No live provider or commercial offer is simulated.

Next add exact-version draft editing and reviewed publication with independently authorized commands, scoped idempotency, immutable-term constraints, commercial-policy validation and concurrency tests. Full SAAS-ADMIN-002, FS-03/06 and Phase 1 remain Partial. Restore drills, production role approval, grant notification/retention policy and manual screen-reader verification remain pending.
