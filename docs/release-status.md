# Entitlement and usage continuation — 2026-11-07

## Delivered scope

Workspace owner → **Usage and eligibility** → inspect actual manager capacity and reconciled evidence → filter dated history → review capability eligibility and restrictions. Existing invitation/acceptance commands now use centralized, version-bound membership eligibility. Rentable-item usage is unavailable, not zero.

This advances **SAAS-ENT-001–003 and SAAS-QUOTA-004/005 / FS-05** to or within **Partial** status. It is a membership-focused foundation slice, not a complete entitlement engine, quota platform or Phase 1 completion. Add-ons, timed grants, overrides, paid lifecycle and rental capabilities remain pending.

## Changes

- Route `/workspace/billing/usage?workspace=…&from=…&to=…`, linked from setup and member administration. Date filters are URL-backed and inclusive Asia/Kolkata dates; current capacity is independent of the selected history period.
- OpenAPI **0.5.0** and regenerated TypeScript: owner-only GET workspace `/entitlements`, `/usage`, `/usage/history`, with nullable unavailable measurements, policy/plan provenance, bounded chronological pagination and field errors.
- Commercial `Entitlements` evaluates the persisted subscription, bound plan version, effective policy, explicit capability grants and workspace restrictions. Domain permission remains separate. `UsageQueries` reconciles counters with active memberships, allocations and event totals.
- **V6** adds versioned policy/rules and manager usage events; **V7** binds explicit historical Free plan versions. Existing migrations stay unchanged. Backfill uses original allocation timestamps. Runtime configuration is read-only and usage events cannot be updated/deleted by the runtime role.
- Commercial consumption/release writes usage evidence in the existing membership transaction. Restriction blocks additions; authorized corrective revocation remains available. Inconsistent source counts produce a visible reconciliation requirement and null remaining capacity; further consumption is blocked until reconciliation.

## Actual verification

| Command | Result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 19 tests, zero failures/errors/skips; executable jar. PostgreSQL tests include restrictions, scope denial, India date boundaries, counter mismatch, 28-event pagination and denied runtime event updates. |
| `npm run generate`, generated-file hash comparison, `npm run typecheck`, `npm run build` in `web` | PASS: generated contracts have no drift; strict TypeScript and production build. |
| `npm run test:e2e -- --workers=2` in `web` | PASS: 20 desktop/mobile tests, extended with real usage/schema/full-capacity/unavailable-inventory/date-filter/empty-history journeys, axe and overflow checks. |
| `npm run test:e2e -- tests/membership.spec.ts --workers=2` in `web` | PASS: 4 targeted desktop/mobile checks after the final reconciliation safeguard. |
| `node tests/foundation-recovery.mjs` in `web` | PASS: database outage/restart preserves session, workspace, invitation replay, accepted manager, exact usage-event ID and reconciled count without duplicates. |
| `python3 scripts/traceability.py` | PASS: 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates. |

The initial new isolation test exposed an omitted package in the safe problem-details handler: an authorized request for an inaccessible workspace surfaced as 403 through error dispatch. The handler now covers the orchestration controller and returns the intended privacy-safe 404; the test passes. Existing controllers retain their handlers.

The mobile usage screenshot was visually inspected. Manual screen-reader, full low-bandwidth/high-concurrency behavior, remote CI, backend dependency/secret scanning, retention/repair operations and production recovery targets remain unverified. No lint script exists. No financial behavior, real SMS or paid activation was added.

## Remaining scope and next step

Next Phase 1 work: extend entitlement configuration with authorized version administration and the required effective-grant layers; add general quota reservation/expiry recovery, private storage and durable job foundations. Minimally usable platform plan administration and remaining live identity/account requirements are still open. Do not advance to property operations by treating membership eligibility as property authority.

Full-source gap review retains pending owner/manager/tenant rental journeys, properties/inventory, discovery/tours/appointments and MARKET aliases, signing/possession/active tenancy, finance/deposits/receipts, helpdesk/privacy/reviews/police/exit, paid workspace and personal memberships, sale/services, advertising/AI, role dashboards and ARCH/INT deployment gates. No mandatory requirement is removed or declared complete from this subset.

Run the existing [README setup](../README.md), open an owner's workspace setup → **Usage and eligibility**, and accept/revoke a manager invitation to see real consumption/release history. [ADR 0005](architecture/adrs/0005-membership-entitlements-and-usage.md), the [decision register](decision-register.md) and [matrix](requirements-matrix.md) record policy, ownership and remaining gaps.

---

## Historical report: workspace membership

The following checkpoint's counts and next-work list are historical.

# Workspace membership continuation — 2026-11-07

## Delivered journey and requirement status

Owner setup → review and create a manager invitation → recipient signs in with the matching test contact → review/accept or decline → select the authorized workspace context → owner revokes membership → subsequent workspace access is denied and capacity released. Cancellation, expiry, stale versions, forwarded IDs, quota exhaustion and unknown-response retry have real server behavior.

This advances the **SAAS-REG-001–003 / FS-02 membership foundation**, AUTH-001 context selection and **SAAS-QUOTA-005 / FS-05 manager capacity subset**. These requirements remain **Partial**; no full requirement or phase is declared complete. MGR-WF-001 property delegation/operations remains Pending. Simulated identity is explicitly labelled; no live SMS, email, verified contact or property authority is claimed.

## Changed surfaces and durable state

- Routes: `/workspace/settings/members?workspace=…`, `/account/invitations`, `/workspace?workspace=…`; linked from setup/account. Owners manage members; managers have a separate membership context without owner administration.
- OpenAPI **0.4.0**: member/invitation list, invitation create/detail/revoke, account invitation inbox/accept/decline, member revoke and self membership context. Generated TypeScript contracts include pagination, exact-version confirmation and safe errors.
- `WorkspaceMembership`/`MembershipController` own membership use cases; commercial `ManagerCapacity` exclusively writes grant counters/allocation history. Workspace → commercial is an explicit acyclic command dependency inside one transaction.
- Additive **V5** adds member IDs/versions/timestamps, invitation deadlines/states, scoped idempotency results, manager allocation history and RLS policies. V1–V4 and existing records are preserved. No dependency versions changed.
- Sandbox counting: active managers count; owners and pending invitations do not. Acceptance atomically reserves/consumes a slot from the persisted grant. Pending invitations promise no capacity. Default invitation lifetime is 168 hours, configurable 1–720; production policy approval remains open.
- Audit and in-app source status persist with each transition. Accepted membership is distinct from property, signing, spending and payout authority. External notification delivery/general jobs remain pending.

## Verification actually executed

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 17 tests, zero failures/errors/skips; executable jar packaged. Includes real PostgreSQL last-slot race, rollback, RLS and revocation evidence. |
| `npm run generate`, generated-file hash comparison, `npm run typecheck`, `npm run build` in `web` | PASS: no generated drift, strict TypeScript and production build. |
| `npm run test:e2e -- --workers=2` in `web` | PASS: all 20 desktop/mobile tests. Real owner/recipient invitation/acceptance/context/revocation, schema checks, keyboard confirmation/cancel, axe, mobile overflow and lost committed-response recovery; existing account/catalogue/workspace regressions. |
| `node tests/foundation-recovery.mjs` in `web` | PASS: DB outage yields safe 503; restart preserves session/workspace and invitation; same-key retries return the original records without duplication. |
| `python3 scripts/traceability.py` | PASS: 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained. |
| `npm audit --audit-level=high` in `web` | PASS: zero reported vulnerabilities in the current frontend graph. |
| `git diff --check` | PASS. |

The first unrestricted browser run had 18 passes and two connection-interruption failures; the API also logged a thread-starvation/clock-leap warning. Two-worker runs passed, including the final build. The cause at unrestricted concurrency is not established; this is not load/performance evidence. Initial local API startup failed because PostgreSQL was stopped; starting the existing Compose database recovered it without deleting data.

Permission/concurrency evidence: only the contact-bound account can accept; recipient/manager cannot invoke owner administration; expired/declined/cancelled invitations fail; revoked inviter authority blocks acceptance; owners cannot be removed through the manager operation; stale versions return 412. Two recipients race one slot: one commits and the other receives QUOTA_EXCEEDED with no membership. Duplicate acceptance/revocation creates no extra allocation/release; old acceptance replay cannot restore revoked membership. Private RLS queries without actor context return no invitations. Created-resource Location resolves only for its authorized owner.

The mobile members screen was visually inspected. Manual screen-reader testing, high-concurrency/low-bandwidth coverage, backend dependency/secret scanning, remote CI, live delivery, retention and production deployment/recovery targets remain unverified. No lint script is configured. No financial behavior was added.

## Run and next resumable step

Use the [README setup](../README.md), open workspace setup → **Members and invitations**, and invite a test mobile. In another browser context, sign in with that mobile and open **Your invitations**. After acceptance, the owner can revoke the manager and verify the slot is available again. The local app uses the existing loopback API, Next.js build and persistent Compose database.

Next Phase 1 work: centralized effective entitlement evaluation/snapshots and broader quota foundations, then private storage/general durable jobs and minimally usable plan administration. Live identity/account management gaps from previous checkpoints remain open. Property-level delegation and actual rental operations follow in Phase 2; workspace membership must never be treated as blanket property authority.

Gap review against the full source retains pending properties/inventory, discovery/tours/appointments and MARKET aliases, signing/possession/active tenancy, rental finance/deposits/receipts, helpdesk/privacy/reviews/police/exit, paid workspace and independent personal memberships, sales/services, advertising/AI, role dashboards and ARCH/INT operational gates. No mandatory scope was dropped.

Policy, ownership and recovery: [ADR 0004](architecture/adrs/0004-sandbox-workspace-membership.md). Decisions: [decision register](decision-register.md). Detailed status: [requirements matrix](requirements-matrix.md).

---

## Historical report: account controls

The following checkpoint's counts and next-work list are historical.

# Account controls continuation — 2026-11-07

## Delivered journey

Sandbox sign-in → edit display name → durable save/reload, with explicit stale-edit recovery; or account security → inspect active sessions → confirm another browser's revocation → that browser is denied → durable security notice. **AUTH-001 / FS-01 remains Partial; Phase 1 is not complete.** This extends the existing identity and Free provisioning journey without changing RentPay's repository name, assets or unrelated work.

## Changed surfaces and persistence

- `/account/profile` and `/account/security`, linked from `/account`; private no-store/noindex pages, labelled fields, keyboard confirmation, error focus, safe in-memory draft preservation and exact-command recovery after an uncertain response.
- OpenAPI **0.3.0**: versioned GET/PATCH `/me/profile`, paginated GET `/me/sessions`, POST `/me/sessions/{id}/revoke`, paginated GET `/me/security-notifications`; generated TypeScript contracts.
- Identity-owned `AccountControls`/`AccountController`; V4 adds profile versions, management-session registry, account-scoped idempotency results and in-app notifications. Mutations commit with audit. Existing migrations are unchanged.
- Profile ETag/If-Match and row locks reject stale updates. Remote revocation requires current account authority, exact version, recent sign-in, CSRF and Origin. A separate management ID never authenticates a session. Tombstones protect revocation/logout against later framework session saves. Counts and 25-item pagination use actual active records.
- **Upgrade note:** cookies created before V4 must sign in again because they lack registry evidence. Account/workspace/history records survive. No database reset is required. [ADR 0003](architecture/adrs/0003-account-profile-and-session-controls.md) records policy and limits.

## Verification actually executed

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 15 tests, zero failures/errors/skips; PostgreSQL integration coverage and executable jar. |
| `npm run generate`, generated-file hash comparison, `npm run typecheck`, `npm run build` in `web` | PASS: no contract drift, strict TypeScript, production Next.js build. |
| `npm run test:e2e` in `web` | PASS: 16 desktop/mobile tests; real profile persistence/conflict recovery, lost committed-response retry, separate-browser revocation, schemas, keyboard confirmation, axe and overflow checks; existing catalogue/workspace regressions. |
| `node tests/foundation-recovery.mjs` in `web` | PASS: authenticated DB outage returns safe 503; restart preserves session/workspace and provisioning replay. |
| `python3 scripts/traceability.py` | PASS: 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained. |

Security evidence: cross-account session selectors return privacy-safe 404; stale/missing versions return 412/428; old authentication cannot revoke another session; current-session revocation directs the user to Sign out; repeat commands create one audit/notice; two profile updates using one version yield one success and one stale rejection. Revoked-browser requests receive 401. A browser test discards the successful save response and retries the exact command, proving version/notice counts do not increase twice. Private profile updates reject unknown authority fields.

The mobile session-security screenshot was visually inspected. Manual screen-reader and full low-bandwidth journeys, production retention, dependency/security scanning for this checkpoint, remote CI and full deployment/recovery gates were not verified. No lint script is configured. The preceding checkpoint's security/audit results are historical, not a new scan claim.

## Remaining work and resumable step

Live identity/SMS, verified contact linking/changes, account preferences/export/deletion and external security notifications remain pending. Browser labels are unverified hints; notifications are in-app only. No new financial or quota behavior is implemented by this slice.

Next Phase 1 slice: invitation/member authority with account-scoped acceptance, expiry/revocation, role/scope boundaries and manager capacity policy, then effective entitlements and atomic quota reservation/consumption, private storage and general durable jobs. Resolve the relevant counting and authority decisions from the full source before implementing invitation quotas.

Gap review: properties/inventory, discovery/tours/appointments and MARKET aliases, signing/possession/active tenancy, rental finance/deposits/receipts, helpdesk/documents/reviews/police/exit, paid workspace and independent personal memberships, sale/service marketplaces, advertising/AI, role dashboards, provider integrations and ARCH/INT production gates remain as tracked in the matrix. No mandatory scope was dropped or marked complete from this subset.

Run the existing [README setup](../README.md), sign in with a local test code, then open `/account/profile` or `/account/security`. Sign into the same test number in another browser context to exercise remote revocation. Keep the database volume for upgrade/recovery. See the [requirements matrix](requirements-matrix.md) and [decision register](decision-register.md).

---

## Historical report: identity and Free provisioning

The following is the preceding checkpoint; its counts and remaining-work list are historical.

# Foundation continuation — 2026-11-07

## Delivered journey

Local sandbox sign-in/registration → private test code → backend session and CSRF → account workspace chooser → confirm current Free plan → atomic workspace creation → persisted setup/allowances/welcome → reload/context switch/logout now runs end to end. Identity delivery is **simulated locally**; authorization, sessions, transactions, database isolation, audit and recovery are real. No live SMS or real-world verified-phone claim is made.

**AUTH-001 and SAAS-REG-001–003 remain Partial.** Full source acceptance also requires live/provider-supported identity, broader account/profile/session controls, invitations, complete setup and rental-unit capacity enforcement. Phase 0/1 are not complete. All catalogue and previously tracked platform gaps remain visible in the 195-ID matrix.

## Changed surfaces and persistence

- Routes: `/login`, `/register`, `/verify-otp`, `/account`, `/create-workspace`, `/setup?workspace=…`; pricing links into the sandbox journey.
- API: challenge request/status/verify, session/CSRF/logout, account profile/contexts, workspace creation/list/setup. Complete implemented schemas, request headers, security and problem responses are in OpenAPI 0.2.0; TypeScript types regenerated.
- Java modules: identity, workspace, commercial Free provisioning, orchestration, technical audit/error support and persistent-session security. Existing catalogue owner preserved.
- V2: Spring Session tables; accounts/challenges/delivery; workspaces/membership/settings/consents; subscriptions/grants; durable idempotency, audit, outbox and in-app notices; private workspace RLS and narrow runtime grants.
- V3: new immutable Free version describing sandbox setup. V1 and historical version references were not rewritten.
- Local profile: sandbox-only loopback startup guard, allowlisted origins, private ignored `.local/` key/inbox; no secrets in browser bundles or application logs. [ADR 0002](architecture/adrs/0002-sandbox-identity-and-free-workspaces.md) records policies, module ownership, state transitions and recovery choices.

## Verification actually executed

| Command | Result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 12 backend tests, zero failures/errors/skips (11 PostgreSQL integration tests + one configuration test); packaged executable jar. |
| `npm run generate`, `npm run typecheck`, `npm run build` in `web` | PASS: generated contracts, strict TypeScript and production Next.js build. |
| `npm run test:e2e` in `web` | PASS: 10 desktop/mobile tests, including real login/provision/setup/reload/replay/logout, response schemas, private cache headers, form validation, keyboard catalogue controls and axe checks. |
| `node tests/recovery.mjs` | PASS: public catalogue DB outage → safe 503 → no fake records → retry to same persisted versions. |
| `node tests/foundation-recovery.mjs` | PASS: authenticated DB outage → safe 503 outside MVC/session loading → DB restart preserves session → same-key creation replay returns same workspace → setup accessible. |
| `npm audit --audit-level=high` | PASS: zero reported vulnerabilities in the current frontend dependency graph. |
| `python3 scripts/traceability.py` | PASS: 195 functional IDs, 36 FS mappings, 257 aliases, 14 ARCH and 12 INT gates retained. |

A screenshot of the real workspace setup journey was visually inspected. Manual screen-reader testing, complete low-bandwidth/browser coverage, performance, production infrastructure and remote CI execution remain unverified.

Security/integrity evidence includes missing CSRF and wrong/missing Origin denial; session fixation protection and CSRF rotation; consumed/expired/locked challenge rejection; committed wrong-attempt counts; throttling and input allowlists; same-phone account recovery; session revocation/idle expiry; concurrent duplicate workspace commands; changed-payload conflicts; rollback after invalid plan; cross-workspace/pooled-connection RLS denial; revoked-member denial; and deduplicated welcome outbox replay. Sandbox configuration rejects production/mixed profiles and non-loopback binding.

Initial failures corrected during this continuation: unsupported JDBC projection convenience methods, an optional form resolver peer dependency conflict (used direct Zod validation instead), browser-test reads racing successful full-page navigation, and an expiry test initially mutating only the cleanup index rather than Spring Session's authoritative idle-time fields. No production identity was enabled to make tests pass.

## Remaining work and next stopping point

This completes the bounded **sandbox identity and Free provisioning journey**, not all of Phase 1. Next: account profile/device session controls, invitation/member authority, effective entitlements and atomic quota reservation/consumption, storage and general job foundations. Then deliver property/unit creation through those controls. Required identity/provider/legal/privacy decisions remain explicit prerequisites for live activation.

Original broader gaps remain: owner/manager/tenant operations, properties/inventory, tours/appointments/discovery and all MARKET aliases, signing/possession/active tenancy, finance/deposits/receipts, helpdesk/privacy/reviews/police/exit, paid workspace and independent consumer memberships, services/private sales, advertising/AI, full role dashboards, provider integrations and ARCH/INT deployment/release gates. No mandatory requirement was removed or marked complete based on this subset.

Setup, code retrieval and test commands: [README](../README.md). Decisions: [decision register](decision-register.md). Status: [requirements matrix](requirements-matrix.md).

---

## Historical report: initial catalogue delivery

The following describes the earlier checkpoint, not the current feature availability or test counts.

# Initial catalogue delivery — 2026-11-07

## Outcome and scope

A working read-only `/pricing` journey now renders PostgreSQL-backed, effective published plan versions through Java and generated OpenAPI TypeScript types. This is a **partial Phase 1 catalogue slice**, with Phase 0 traceability started. Neither phase nor any complete functional requirement is declared implemented.

Existing staged/unstaged specifications and IDE metadata were preserved. The repository had no application, build, lockfile, migrations, CI, tests, requirement matrix or README before this work.

| Requirement | Frontend | Backend | Delivered / remaining |
|---|---|---|---|
| SAAS-PLAN-001 / FS-03 | Partial | Partial | Five labelled baseline cards, interval selection, quantities, unknown-price disclosures, loading/empty/service-failure views; real active public projection. Feature comparison, approved commercial terms, registration/upgrade/sales actions remain pending. |
| SAAS-PLAN-002 / FS-03 | Partial | Partial | Public list/detail DTO, version/date, capacity, decimal-string money and disclosure. Complete feature/entitlement definitions and approved commercial conditions remain pending. |
| SAAS-PLAN-003 / FS-03 | Partial | Partial | Immutable published versions, effective selection and migration publication audit. Admin version drafting/activation, concurrency/ETag, subscription references, grandfathering and migrations remain pending. |

Routes: `/` redirects to `/pricing`; `/api/v1/public/plans` and `/api/v1/public/plans/{code}` support audience/interval filters. The OpenAPI catalogue describes only these implemented operations; it is not the full platform API. Consumer audience returns a genuine empty result; no membership offers are invented.

Modules/files: `backend` Java catalogue/security, `web` Next.js presentation, `contracts/openapi/lmt-v1.json`, Flyway `V1__versioned_public_catalog.sql`, local database `compose.yaml`, Maven wrapper, npm lockfile, CI workflow, traceability script and documentation.

## Executed verification

| Command / environment | Actual result |
|---|---|
| `python3 scripts/traceability.py` | PASS: 195 functional IDs, all 36 FS mappings, 256 child aliases plus FR-03, 14 ARCH and 12 INT gates. Coverage of identifiers does not establish acceptance or implementation. |
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw -B -ntp verify` in backend | PASS: 4 integration tests, zero failures/errors/skips; Java 21.0.13.0.3, Spring Boot 4.0.9, isolated PostgreSQL 17.11; executable jar packaged. |
| `npm run generate`, `npm run typecheck`, `npm run build` in web | PASS: generated DTOs, strict TypeScript, production Next.js build. |
| `npm run test:e2e` against built web + real API + persistent PostgreSQL | PASS: 6 desktop/mobile tests: plan data, interval selection, keyboard skip/disclosure, no horizontal overflow, axe WCAG checks, live list/detail/422/404/401 schema validation. |
| `node tests/recovery.mjs` | PASS: stop local DB, API 503 with safe problem/code/correlation/Retry-After, no fallback plan records on page, restart DB, retry preserves annual selection and identical persisted plan versions. |
| `docker compose up -d --wait db` | PASS: PostgreSQL health; persistent volume initialized; migration/runtime roles separated. |
| npm dependency audit during final dependency install | PASS: zero reported vulnerabilities in the resolved frontend graph. Not a complete security audit. |
| `git diff --check` | PASS for tracked working-tree diff; existing user changes were not modified. |

A desktop screenshot from the real recovery journey was visually inspected. No full manual screen-reader review, browser matrix or production performance claim is made.

Earlier failures were corrected: Spring 7 problem-detail status signature; initial system Maven selected Java 27 (final verification explicitly uses 21); fixture ordering after removing cleanup of immutable published rows; test focus before streamed content became visible; and a rollback disconnect incorrectly falling through to a 401. Two npm checks were initially invoked from repository root without a package.json, then rerun successfully from `web`. The initial TypeScript resolution selected unsupported major 7 for the generator peer range; it is now pinned to 5.9.3.

## Security, integrity and recovery evidence

Only catalogue GET and shallow health are public; unrelated routes deny anonymous requests. Runtime DB cannot update plans or read the audit table. No account authentication or authenticated mutations are claimed. CSRF remains enabled; proper opaque sessions/context/revocation remain Phase 1 work.

Integration tests exclude draft and future terms, preserve decimal-string amounts and null unknowns, reject invalid filters, enforce published-version update/delete rejection and verify five migration audit records. Plan publication is a durable migration action; ordinary reads do not fabricate business audit activity. No external provider is called.

The outage test identified and fixed transaction-rollback error mapping. Recovery reads the same persisted versions; it does not reseed or reset the database. No quota, financial, signing or booking concurrency guarantee is implied by these catalogue tests.

## Full-source gap review and next work

| Area | Current disposition / next prerequisite |
|---|---|
| Phase 0 architecture, state machines, complete API and schema design | Partial; matrix/ADR/decisions exist, but detailed gates and per-alias child interpretation are not complete. |
| Identity, context, invitations, recovery, Free workspace provisioning | Pending; next integrated slice after remaining foundation design gates. |
| Entitlements, quota reservations, personal/workspace independence | Pending; catalogue limits are display data, not an entitlement engine. |
| Owner/co-owner/manager/property/inventory/dashboard journeys | Pending phases 2–6; no fake records or operational metrics. |
| Discovery, MARKET aliases, tours, appointments, applications/imports | Pending phases 3+; original aliases retained with incomplete child-level interpretation flagged. |
| Agreements, ten-day signing, move-in/active tenancy | Pending phases 4/6; provider/legal decisions unresolved. |
| Rental finance, utility billing, payments, deposits and receipts | Pending phase 5; no money collected or allocated. |
| Helpdesk, documents, privacy, reviews, police, notices/exit/settlement | Pending phase 6; no authority or official result simulated. |
| Paid SaaS, consumer memberships, service/sale marketplaces | Pending 7/7A; approved commercial and provider configuration required. |
| Advertising, communications and AI | Pending later source phases; no external publication or AI actions. |
| ARCH/INT deployment, workers, storage, operations and release hardening | Pending; only database Compose is implemented, not the full edge/web/api/worker/migrate topology. No deployment or production-readiness claim. |

CI is authored but not remotely executed. Full lint policy, backend dependency/secret scanning, signed/digest-pinned images, branch protection, migration upgrade coverage, backups/restores, retention, operational ownership and load/accessibility gates remain open. Automated axe checks do not constitute WCAG certification. No provider sandbox or live provider exists in this slice.

Run instructions are in [README](../README.md). Next resumable milestone: complete Phase 0's foundation-specific authority/session/state/API decisions, then implement account proof → backend session/CSRF → explicit context → idempotent Free workspace creation → persistent membership/plan binding → UI with isolation and retry tests. Retain all remaining mandatory work in the existing matrix rather than starting a competing roadmap.
