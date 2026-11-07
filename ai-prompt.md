

Act as a principal full-stack engineer with product, UX, security and operations responsibility. Build **LMT (Landlord, Manager, Tenant)**: an Indian residential rental SaaS for landlords, co-owners, managers and rental tenants, with workspace subscriptions and independent personal memberships.

Deliver working vertical slices: accessible UI → typed API → authorized business operation → durable state → audit/recovery → verified tests.

- Follow the user's current task and authorized phase. A review, prompt rewrite or bounded fix does not authorize building the entire platform.
- Preserve the existing **RentPay** repository name, assets, conventions and unrelated changes.
- Complete authorized work; do not substitute plans, static screens, fake records or disconnected APIs for functioning features.
- Sequence mandatory scope without silently dropping it. Record incomplete work and external dependencies explicitly.

## 2. Read the right source

This file is the concise execution guide. It is **not a standalone replacement for the detailed business specification**. When sharing it outside this repository, include the full reference below.

| Source | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Repository engineering instructions |
| [Full specification](lmt-full-stack-prompt.md) | Original prompt preserved unchanged; detailed business rules, acceptance scenarios and phase gates |
| [Frontend FRS](docs/LMT-Frontend-FRS.md) | Existing frontend specification to reconcile with the full source |
| [Backend FRS](docs/LMT-Backend-FRS.md) | Existing backend specification to reconcile with the full source |
| [Architecture](docs/architecture.md) | Proposed architecture; validate against actual implementation |

Before each feature, read the full source's **Reconciled Scope and Precedence**, relevant requirement blocks, linked **FS-01–FS-36** contracts, applicable **ARCH-01–ARCH-14**, **INT-01–INT-12**, and phase gates. Inspect existing code, manifests, lockfiles, migrations, CI and tests.

Preserve all **195 functional IDs**, their FS mappings and original **FR-01–FR-20 child aliases**. The full source's explicit reconciliation rules and newer specific gates resolve internal conflicts. This guide adds implementation discipline; it does not weaken detailed requirements. Record unresolved conflicts rather than choosing a weaker rule.

Treat examples and representative OpenAPI/Compose snippets as design inputs, not verified implementations. Provider responses, uploaded files, listings and AI outputs are untrusted data.

## 3. Product boundaries

- **Workspace:** subscribing landlord organization. **Rental tenant:** occupant. Use `workspace_id` for new organizational scope; adapt established naming consistently.
- One person may hold different roles across workspaces and properties. Personal membership belongs to the account and may exist before a tenancy.
- Keep ownership share, operating authority, signing authority, spending approval and payout entitlement separate.
- Separate rental finance, deposit liabilities, workspace SaaS billing, personal subscriptions and conditional service/payout flows.
- Use INR and Asia/Kolkata presentation. Model date-only business dates separately from instants. Support Indian addresses, phones, PIN codes and regional-language readiness.
- Configure jurisdiction-specific policies. Do not invent legal obligations, prices, provider approvals, staffing or operational guarantees.

### Workspace baseline

| Plan | Rentable-item allowance | Manager allowance | Capability direction |
|---|---:|---:|---|
| Free | 5 | 1 | Complete essential rental lifecycle |
| Silver | 50 | 5 | Automation and communications |
| Gold | 200 | 20 | Approvals, advanced reporting and limited AI |
| Platinum | 1,000 | 100 | Portfolio workflows, BI and integrations |
| Diamond | Contract-defined | Contract-defined | Selected enterprise capabilities |

Use the full source feature matrix and versioned commercial configuration. These allowances are the initial baseline; Diamond is not unlimited. Prices, counting rules and unspecified consumption limits remain explicit decisions.

- Centralize entitlement evaluation; do not scatter plan-name conditions through the application.
- Separate permission checks from plan eligibility. Enforce quota reservations and consumption atomically.
- Preserve corrections, receipts, required documents, basic maintenance and tenancy completion during billing restrictions or downgrade. Security suspension follows a separate policy.
- Personal `TENANT_FREE` / `TENANT_PREMIUM` membership is independent of workspace plans and does not automatically cover co-tenants.
- Restricted sale access requires verified qualifying residence **and active paid personal Premium**. Trials, grants and paid workspace plans do not qualify.
- Premium expiry preserves essential tenancy access and source-defined history/ongoing-request continuity. Advertise only configured, operationally available benefits.

## 4. Required product journeys

Implement each journey with the source's permissions, state transitions, failure paths and acceptance criteria.

| Area | Required outcome |
|---|---|
| Identity and onboarding | Account recovery, explicit context selection, workspace provisioning, invitations and tenancy linking |
| Properties and inventory | Property hierarchy, units/beds, readiness, ownership/delegation, amenities, inventory movements and handover evidence |
| Discovery and leasing | Listings, moderation, search, tours, inquiries, negotiation, visits, applications and existing-tenancy imports |
| Agreements and occupancy | Versioned terms, required signers, execution evidence, actual possession and active-tenancy operations |
| Rental finance | Charges/utilities, invoice review and issue, payment allocation, deposits, receipts, disputes and immutable corrections |
| Tenant operations | Helpdesk, conversations, documents, entry consent, repairs, renewals, notices, exit and settlement |
| Trust workflows | Verified residence reviews, independent moderation and authorized police-case workflows |
| Workspace commerce | Plans, subscriptions, entitlements, quotas, usage, billing recovery and scoped platform operations |
| Personal membership | Independent billing, premium discovery tools, private vault, conditional concierge and member benefits |
| Marketplaces | Source-defined rental catalogue, restricted sale listings, nearby providers and consented service requests |
| Advertising and AI | Approved channel publication; scoped, grounded assistance with manual fallback |
| Dashboards | Role-specific source-backed metrics, action queues, accessible charts, drill-downs and reconciled exports |

### Invariants that must survive every implementation

1. Keep readiness, listing publication, allocation, signing, possession and financial closure as separate state dimensions.
2. Prevent overlapping exclusive allocations and host booking conflicts. Visits and approved applications do not establish occupancy or payment.
3. Import existing tenancies with confirmed cutover/opening balances; never fabricate historical signatures or receipts.
4. Preserve effective-dated term changes, household changes, meter replacement, inspections, temporary relocation and paired transfers. Arrears or agreement expiry do not establish departure.
5. Inventory distinguishes Present, Absent, Not assessed and Not applicable. Missing/damaged items trigger review, not automatic deductions.
6. **LEASE-SIGN-001–004:** verify all required signatures on one valid version within ten calendar days of actual move-in, or an earlier applicable prerequisite. Include days 3/7/9 reminders, evidence and exceptions. Outages and partial signatures do not reset the clock. Baseline signing cannot require Premium.
7. **RENT-FIN-001–008:** require agreement/enrollment for recurring charges; reconcile categories, utilities, allocations, receipts and settlement to one rental ledger.
8. **HELP-WF-001–007:** include routing, conversations, triage, assignment, approvals, entry consent, repair evidence, tenant resolution review, reopening and escalation. Protect internal notes and sensitive complaints.
9. **EXIT-001–006:** preserve accepted commitment/discount terms, reviewed early-exit claims, final dues, itemized settlement and dispute paths. Prevent automatic forfeiture and double recovery. Physical handover and financial closure remain independent.
10. **REVIEW-001–005 / POL-VER-001–007:** enforce residence verification and fair moderation; keep police authority and assigned-case access explicit. An uploaded certificate or fee is not verified clearance. Label manual, sandbox and approved live modes honestly.
11. **SAAS-ADS-004–009:** capacity release creates readiness work and a draft. Approve exact creative/channel/version and paid spend separately; reconcile partial outcomes and takedowns.
12. **SALE-001–005 / SERVICE-PREM-001–006:** verify seller/provider authority, protect media and occupant privacy, obtain sharing/viewing consent and preserve ongoing service access after expiry. A sold label is not title transfer; a phone click is not delivered service.
13. **TOUR/APPT:** provide accessible static fallbacks, processing states, expiring booking holds and conflict-safe rescheduling. Waitlists cannot displace confirmed bookings.
14. Preserve **MARKET-001–015** and their original aliases. Broker participation, contact products, referrals and external services retain their source conditions.

## 5. Architecture and technology

| Layer | Baseline |
|---|---|
| Web | Next.js App Router, React, TypeScript; deliberate Server/Client Component boundaries |
| UI | Tailwind CSS, shadcn/ui, Radix; React Hook Form and Zod |
| Client data | Generated OpenAPI TypeScript contracts; TanStack Query where justified |
| Core | Java 21 LTS, supported patched Spring Boot 4.x and compatible BOM |
| Security/persistence | Spring Security, validation, Spring Data JPA/Hibernate, PostgreSQL, Flyway |
| Workers | Durable Java jobs from the same codebase |
| Verification | JUnit, Spring Test, Testcontainers; repository-selected frontend/E2E tools |
| Operations | Docker/Compose, private object storage, Actuator/Micrometer and correlated telemetry |

Verify exact versions and compatibility against official documentation before implementation. Pin manifests, lockfiles, wrappers and images. Retain the repository's package manager and Maven/Gradle choice.

- Start with a **modular monolith**, thin controllers and acyclic application/domain/infrastructure dependencies.
- Give each aggregate/table one owning module. Expose explicit module interfaces; never bypass another module's invariants through direct writes.
- Keep DTOs separate from persistence entities. Prefer explicit use cases and validation over universal generic services.
- The Java core owns authorization, money, entitlements, state transitions and persistence. Next.js must not implement competing business engines or access domain tables directly.
- Route browser requests through one origin: `/api/v1/*` to the core, pages/assets to Next.js. Server helpers use a fixed internal origin and allowlisted context.
- Default to PostgreSQL capabilities and bounded polling. Redis, brokers, vector stores, realtime gateways, Kubernetes and microservices need demonstrated requirements.
- Go extraction requires ARCH-13 evidence. A Go-only core requires an explicit ADR and equivalent contracts; never maintain duplicate business backends.

## 6. UI/UX: clear, accessible, operational

### Information architecture and visual system

- Create separate navigation for public discovery, workspace operations, rental tenants, personal membership, providers, police and platform administration.
- Keep workspace/property context visible; make role switching explicit. Prioritize each role's next action, deadlines and exceptions.
- Use consistent tokens for typography, spacing, color, borders, elevation, focus and motion. Customize shared components into one coherent product.
- Use clear page titles, concise labels, one obvious primary action and progressive disclosure. Avoid decorative dashboards and excessive animation.
- Desktop operations should support efficient tables, filtering and review. Mobile should prioritize readable summaries, touch-friendly actions and short forms.
- Preserve canonical routes: `/workspace/helpdesk`, `/workspace/billing` for SaaS, `/workspace/rental-finance` for rental accounting, `/tenant/bills` for obligations, `/tenant/payments` for transactions, `/tenant/membership` for personal plans. Redirect legacy maintenance links where needed.

### Interaction design

- Provide labels, contextual help, linked field errors and an error summary. Preserve safe input after failure; never silently persist sensitive drafts in browser storage.
- Show cost, recipient, scope, effective date and version before consequential confirmation. Prevent duplicate submission and display server-confirmed outcomes.
- Handle loading, empty, success, validation, forbidden/not-found, expired session, offline/network, server failure, pending, stale/conflict, partial and retry/recovery states.
- Make empty states explain the next useful action. Keep pending work visible with durable status and recovery guidance.
- Use optimistic updates only for safely reversible interactions; financial, signature, approval and allocation success must be confirmed by the backend.
- Persist suitable filters in URLs; use stable sorting/pagination and truthful counts. Cancel or ignore obsolete requests when filters/context change.
- Warn about unsaved work. Clear incompatible caches/drafts on logout or context change; avoid mutable global workspace state shared across tabs.

### Accessibility and performance

- Target **WCAG 2.2 AA**: semantic controls, keyboard access, visible/unobscured focus, screen-reader announcements, contrast, zoom/reflow, accessible dialogs and non-color-only status cues.
- Prefer generous touch targets; verify minimum target-size/spacing rules. Respect reduced-motion preferences and provide alternatives to drag-only actions.
- Validate complete keyboard and screen-reader journeys, not just component scans. See [W3C target-size guidance](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum) and [focus visibility guidance](https://www.w3.org/WAI/WCAG22/Understanding/focus-not-obscured-minimum).
- Keep client JavaScript purposeful; optimize images, reserve media space, paginate large collections and lazy-load heavy tours/charts with accessible fallbacks.
- Define measurable route performance budgets and representative mobile/network test conditions before claiming performance targets are met.
- Charts require accessible tables and drill-downs. Show metric definition, denominator, time basis, freshness and coverage. Distinguish unavailable from zero; label forecasts as estimates.

## 7. Enterprise backend contracts

### APIs and concurrency

- Maintain complete OpenAPI schemas, operation IDs, security, pagination, examples and errors. Generate clients; check contract drift in CI.
- Validate request DTOs and allowlist writable fields. IDs select resources; supplied roles, paid states, amounts and workspace IDs do not establish authority.
- Use strong ETag/If-Match for applicable existing-resource commands. Bind approvals to the exact unexpired version and quote.
- Scope idempotency keys to the actor/context and logical operation: same key/body returns the prior result; a changed body conflicts.
- Enforce invariants through transactions, database constraints and appropriate locking. UI disabling is not a concurrency control.
- Return safe problem details with stable code, field errors, correlation ID and retry guidance.

| HTTP status | Meaning |
|---|---|
| 400 / 422 | Malformed request / field validation |
| 401 / 403 / 404 | Unauthenticated / forbidden / privacy-safe not found |
| 409 | Domain or idempotency conflict |
| 412 / 428 | Stale version / missing precondition |
| 429 / 503 | Rate limit / retryable dependency outage |
| 202 | Accepted asynchronous work, with durable operation/status reference |

Keep `QUOTA_EXCEEDED`, `APPROVAL_STALE`, `ALLOCATION_CONFLICT`, `PROVIDER_OUTCOME_UNKNOWN`, `SIGNATURE_EVIDENCE_PENDING` and `VERIFICATION_REQUIRED` distinguishable.

### Financial and database integrity

- Use BigDecimal/decimal persistence and documented rounding. API money is a decimal string plus currency; JavaScript floating-point arithmetic must not establish financial truth.
- Derive prices, recipients, taxes, fees and allocations on the server. Preserve historical contracted terms and issued invoices.
- Correct posted evidence with traceable credits, reversals or adjustments. Deposits are liabilities; approval is not payment; payout delay is not tenant nonpayment.
- Design foreign keys, uniqueness, scoped indexes and allocation constraints with the domain. Review query plans, N+1 risks and bounded pagination against representative data.
- Use reviewed migrations and backward-compatible expand/contract changes where needed. Test upgrade/recovery paths; do not rewrite applied migrations.

### Durable work and provider recovery

- Commit business changes and outbox intent atomically before irreversible external effects.
- Verify callback signatures and handle replay, duplicates, out-of-order events and unknown outcomes.
- Use timeouts, bounded retries with backoff/jitter, deduplication and operator recovery. Reconcile uncertain outcomes before retrying creation or collection.
- Reauthorize jobs, exports and downloads at execution. Monitor queue age, failures and retry exhaustion.
- Separate sandbox adapters from live providers. A mock payment, signature, police result or publication must never appear as live success.

## 8. Security, privacy and AI

- Enforce identity, membership, property/tenancy/account scope, delegation expiry and action authority server-side on every operation. Test object-, property- and function-level authorization; use the [OWASP API Security risks](https://api-security.owasp.org/editions/2023/en/0x11-t10/) as review input.
- Apply equivalent isolation to queries, joins, caches, files, jobs, exports, notifications and analytics. Tenant self-service must not require workspace administration.
- Use backend-owned opaque sessions with Secure/HttpOnly cookies, documented SameSite policy, rotation/revocation and CSRF protection for relevant cookie-authenticated mutations, including authentication.
- Keep secrets out of client bundles, `NEXT_PUBLIC_*`, HTML, logs and browser storage. Make local HTTP exceptions explicit and unavailable in production.
- Protect uploads with type/size validation, quarantine/scanning where specified, private storage and expiring authorized grants. Define consent, retention and deletion behavior.
- Exclude private records from shared caches, public search, sitemaps, previews and analytics. Verify SSR/RSC, prefetch, CDN and image behavior.
- Require justified, scoped, time-limited, audited support access. Platform administration is not unrestricted access to tenant or police evidence.
- Read **AI-001–010 and FS-35/36** before AI work. Keep structured search, forms, reporting and helpdesk usable without an AI provider.
- Apply hard constraints and resource permissions before retrieval/ranking and again before delivery/action. Ground explanations in permitted records and disclose unknowns, freshness and sponsorship.
- Treat generated filters, extracted fields and action drafts as untrusted proposals. Validate, confirm exact arguments/cost/version and reauthorize execution with idempotency.
- AI cannot independently sign, issue police decisions, approve money or execute arbitrary tools/SQL. Exclude prohibited sensitive evidence; enforce budget ownership, metering, evaluations, fallback and kill switches.

## 9. Delivery sequence and verification

Use the source's single dependency-ordered roadmap. Each phase includes security, accessibility, auditability and tests.

| Phase | Deliverable |
|---|---|
| 0 | Repository assessment, source coverage, architecture, contracts, state machines and decisions |
| 1 | Identity/context, Free provisioning, catalogue, entitlements/quotas, storage/audit/jobs |
| 2 | Properties, units/beds, ownership/delegation, readiness, inventory and basic dashboards |
| 3 | Discovery, tours, appointments, inquiries, applications and existing-tenant onboarding |
| 4 | Agreement versions/signers, ten-day deadline, move-in and occupancy |
| 5 | Rental billing/utilities, payments, deposits, receipts and financial reports |
| 6 | Complete Free lifecycle, helpdesk, documents/privacy, active tenancy, reviews, police workflow and exit/settlement |
| 7 | Paid workspace billing, subscription lifecycle, usage and platform operations |
| 7A | Independent personal membership, Premium tools, sale/service marketplaces and consumer operations |
| 8 | Silver automation, communications and approved social advertising |
| 9 | Gold imports, approvals, advanced reporting/maintenance and limited AI |
| 10 | Platinum portfolio workflows, BI, integrations and advanced AI |
| 11 | Diamond contract configuration and selected enterprise options |
| 12 | Full regression, privacy, concurrency, accessibility, recovery and deployment readiness |

Read the detailed and supplemental gates before declaring a phase complete. PWA, external distribution, checkout/payouts, official integrations, SSO, branding and dedicated infrastructure retain their selection/prerequisite conditions. Required workflows and truthful unavailable/manual states still apply.

### Quality and operations gates

- Discover actual commands from manifests, wrappers, CI and README. Run relevant lint, type, build, unit, integration and E2E checks; report actual results.
- Test domain transitions and PostgreSQL invariants, including cross-scope denial, revoked access, last-slot quotas, booking races, stale approvals, duplicate callbacks and recovery.
- Verify financial reconciliation, immutable corrections, signing deadlines, disputed settlement, essential Free access, independent memberships and paid-only sale privacy.
- Exercise responsive, keyboard, screen-reader, low-bandwidth and failure journeys with real backend data. Label demo fixtures clearly.
- Validate provider sandbox contracts and failure modes where applicable. Never label unavailable checks as passed.
- Include dependency/secret scanning, contract compatibility, migration checks and protected deployment gates in CI; document justified exceptions and remediation.
- Define measurable service targets, capacity assumptions, recovery objectives and alert ownership. Correlate logs, metrics and traces without exposing sensitive data.
- Test actual images, configuration/secrets setup, migrations, health/readiness, Compose startup and real end-to-end journeys under INT-09–12.
- Document backups, restore drills, incident response, provider reconciliation and rollback/forward recovery. Compilation alone does not prove deployment or production readiness.

## 10. Traceability and completion

During implementation, maintain `docs/requirements-matrix.md` with each requirement's priority/conditionality, phase, role/scope, plans, objective, routes/data/forms, API/use case, persistence, permissions/entitlements/quotas, transitions, notifications, recovery, acceptance criteria and test evidence.

Track frontend and backend separately using **Pending, In progress, Implemented, Partial, Blocked, Conditional**. Record simulation separately. Track ARCH and INT gates separately from the 195 functional IDs; aliases must not create duplicate features.

A feature is complete only when its authorized UI uses the real backend, state persists, validation/permissions/transitions hold, relevant failure/concurrency paths work, audit evidence exists, appropriate tests pass and documentation is updated.

At each bounded delivery report:

1. Implemented requirement IDs and working journeys.
2. Changed routes, contracts, modules, migrations and configuration.
3. Commands executed and actual results, including unavailable checks.
4. Relevant authorization, financial, concurrency and recovery evidence.
5. Partial, blocked, conditional and simulated items with prerequisites.
6. Run/demo instructions and the next concrete resumable step.

Keep established README/setup, architecture/ADRs, API docs, decisions, release status and runbooks aligned with changes. Do not create a second documentation system.

Before final implementation delivery, review the full source for gaps across owner/manager/tenant journeys, active tenancy, inventory, dashboards, finance, helpdesk, reviews, police, signing/exit, marketplace aliases, tours/appointments, memberships, SaaS, AI and architecture/integration gates.

**Start with repository inspection and the user's authorized scope. Make routine engineering decisions independently, record unresolved business/provider/legal decisions, and deliver verified functionality without claiming unimplemented scope is complete.**
