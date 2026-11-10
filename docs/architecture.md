# LMT — Software Architecture Design Specification

**Document Type:** Software Architecture Specification (SAS)  
**Product:** LMT — Landlord, Manager, Tenant  
**Architecture Style:** Domain-driven Modular Monolith with Hexagonal/Clean Architecture principles  
**Primary Market:** India  
**Status:** Proposed architecture baseline, subject to repository and dependency validation

---

## 1. Objective

Design a secure, scalable, maintainable, fault-tolerant, subscription-based multi-tenant SaaS platform for rental property discovery, rental operations, landlord/manager administration, tenant self-service, property marketplace capabilities and platform-level commercial administration.

The architecture must support:

- Individual landlords managing a few rental units.
- Professional property managers managing multiple properties.
- Co-owners with distinct legal, financial, signing and operational authorities.
- Rental tenants and authorized occupants.
- Prospective tenants, applicants and verified former tenants.
- Independent personal Premium subscriptions.
- Service providers and approved external partners.
- Authorized police-verification case workflows.
- Platform administrators and scoped support users.
- Enterprise customers with configurable limits and optional dedicated isolation.

The system must preserve the complete rental lifecycle:

**Property onboarding → Inventory/readiness → Listing → Discovery → Inquiry → Appointment → Application → Negotiation → Agreement → Move-in → Rent and utility billing → Payments → Maintenance → Renewal/notice → Move-out → Deposit settlement → Turnover/relisting.**

SaaS subscriptions, consumer membership, advanced marketplace services, advertising and AI extend the rental platform without replacing or paywalling its essential lifecycle.

## 2. Architectural Principles

### SAS-ARCH-001 — Architecture Style

Use a **modular monolith as the initial production architecture**.

The transactional business domains must reside in one authoritative Java/Spring Boot application, organized into independently owned business modules.

Each domain module must follow pragmatic application, domain and infrastructure boundaries.

A typical module structure:

```text
property/
  api/
  application/
    command/
    query/
  domain/
    model/
    policy/
    event/
  infrastructure/
    persistence/
    adapter/
```

Do not introduce a microservice merely because a domain has a separate business responsibility.

A module may be extracted into a service only after clear ownership, performance, scaling and operational evidence justify the additional distributed-system complexity.

### SAS-ARCH-002 — Design Rules

- Apply SOLID and DDD where they improve maintainability.
- Keep business invariants inside authoritative domain/application services.
- Keep REST controllers thin.
- Do not expose JPA entities directly through public APIs.
- Assign exactly one owning module to every domain aggregate and table.
- Prevent cyclic module dependencies.
- Use explicit application interfaces for cross-module collaboration.
- Prefer synchronous in-process coordination for atomic business operations.
- Use durable asynchronous processing for external side effects and deferred work.
- Avoid unnecessary generic repositories, universal business services and duplicated DTO hierarchies.
- Preserve existing repository architecture and conventions when an implementation already exists.

## 3. Technology Architecture

| Layer | Technology | Responsibility |
|---|---|---|
| Web frontend | Next.js App Router, React, TypeScript | Public website and role-based application UI |
| UI system | Tailwind CSS, shadcn/ui, Radix | Accessible responsive design |
| Forms | React Hook Form, Zod | Form state and client-side validation |
| API client | OpenAPI-generated TypeScript client | Contract-safe API consumption |
| Backend | Java 21, supported Spring Boot 4.x | Business and transaction authority |
| Authentication | Spring Security | Sessions, identity and access policies |
| Persistence | Spring Data JPA, Hibernate, JDBC where justified | Command persistence and optimized read queries |
| Database | PostgreSQL | Transactional system of record |
| Schema migrations | Flyway | Versioned database changes |
| Background processing | Durable Java workers | Billing, reminders, notifications, provider reconciliation |
| Caching | Optional Redis | Performance acceleration, not financial truth |
| Object storage | Private S3-compatible storage | Documents, evidence, photos and media |
| Search | PostgreSQL indexes, full-text/trigram | Rental discovery and filters |
| Observability | Micrometer, Actuator, OpenTelemetry-compatible tracing | Metrics, health, traces and operational diagnostics |
| Testing | JUnit, Spring Test, Testcontainers, Playwright | Behavioral, contract and end-to-end verification |
| Local deployment | Docker Compose | Reproducible development/integration runtime |
| Production deployment | AWS or GCP managed services | Independently scalable web/API/worker runtimes |
| Optional workers | Go | Measured, independently scalable transport/processing adapters |

Exact framework versions must be verified against current official compatibility documentation and pinned before implementation.

Kafka, Kubernetes, a dedicated search engine, vector database and standalone Go services are **not mandatory initial dependencies**.

## 4. Logical System Architecture

```text
                          USERS
                            |
          +-----------------+------------------+
          |                 |                  |
      Visitors         Landlords/           Platform/
      Tenants          Managers             Authorities
          |                 |                  |
          +-----------------+------------------+
                            |
                    HTTPS Edge / WAF
                            |
                    Next.js Application
                            |
                   Same-Origin REST API
                            |
                   Spring Boot Backend
                            |
              Identity / Authorization
                            |
              Resource Scope Resolution
                            |
               Entitlement Evaluation
                            |
                 Application Use Cases
                            |
          +-----------------+------------------+
          |                 |                  |
      SaaS Control      Rental Business     Marketplace
         Plane            Domains            and AI
          |                 |                  |
          +-----------------+------------------+
                            |
                        PostgreSQL
                            |
              Transactional Outbox / Jobs
                            |
                     Java Worker Runtime
                            |
             +--------------+--------------+
             |              |              |
          Payments       Messaging       E-sign /
          Providers      Providers       Media / AI
                            |
                     Object Storage
```

### Deployment boundary

Initially deploy:

1. Next.js web runtime.
2. Java Spring Boot API runtime.
3. Java durable worker runtime using the same codebase and release version.
4. Managed PostgreSQL.
5. Private object storage.
6. HTTPS ingress.
7. Centralized secret and telemetry services.

The Java API and worker are **deployment units, not independent business backends**.

## 5. Multi-Tenancy Architecture

### SAS-MT-001 — Tenant Model

A SaaS tenant is a **workspace**, not an occupant of a rental unit.

Each workspace has an immutable `workspace_id`.

A user may simultaneously belong to multiple workspaces and maintain independent personal tenancy relationships.

### SAS-MT-002 — Database Isolation

Default:

**Shared PostgreSQL database + shared schema + workspace discriminators + defense-in-depth Row-Level Security where appropriate.**

Workspace-owned tables must use appropriate scoped identifiers and ownership constraints.

Example:

```text
workspace
  id

property
  id
  workspace_id

unit
  id
  workspace_id
  property_id

tenancy
  id
  workspace_id
  rentable_inventory_id
```

Use composite foreign keys or equivalent validated constraints to prevent cross-workspace references.

The data model must also support resources that are not owned by a landlord workspace:

- Personal consumer accounts, subscriptions and document vaults.
- Service provider records and requester/provider interactions.
- Assigned police-verification cases.
- Platform-level configuration and moderation.
- Restricted cross-workspace marketplace projections.

Do not attach these resources to an arbitrary landlord workspace merely to simplify authorization.

### SAS-MT-003 — Authorization

For workspace operations, enforce:

```text
Authenticated account
    + Active workspace membership
    + Resource/property authority
    + Permitted business action
    + Effective SaaS entitlement
    + Applicable quota
    + Valid business state
```

For personal-account, police, provider or marketplace actions, apply their distinct ownership and access policies.

A client-supplied `workspace_id` is a selector, not proof of authority.

### SAS-MT-004 — RLS and Connection Handling

- Use a restricted application database role.
- Keep migration credentials separate.
- Apply trusted scope transaction-locally where RLS is used.
- Validate connection-pool reuse and cleanup.
- Apply current scope to workers, native queries and exports.
- Make cross-workspace administration explicitly privileged and audited.
- Test isolation on the actual PostgreSQL engine.

Dedicated schemas/databases may be evaluated for enterprise contracts through an ADR.

## 6. Identity, Sessions and Security Architecture

### SAS-SEC-001 — Identity

Support verified mobile OTP, verified email login/recovery and conditional federated login.

The backend owns session creation, rotation and revocation.

Use opaque Secure/HttpOnly cookies for browser sessions, with a shared server-side session store and CSRF protection for cookie-authenticated mutations.

### SAS-SEC-002 — Contextual Authorization

Use centralized security policies combining RBAC with relationship- and resource-based checks.

Separate:

- Workspace administrator authority.
- Legal ownership.
- Property management delegation.
- Signing authority.
- Financial approval authority.
- Tenant/occupant permissions.
- Personal Premium entitlement.
- Provider assignments.
- Police case assignments.
- Platform operational support access.

A paid subscription must never substitute for an action permission.

Revoked authority must be rechecked at the point of authoritative commitment, including for scheduled and background operations.

### SAS-SEC-003 — Sensitive Data Controls

Apply:

- TLS for communication.
- Encryption at rest where appropriate.
- Private storage and short-lived document access grants.
- Restricted secrets management.
- Purpose-limited access to identity and police evidence.
- Audit logs for sensitive operations.
- OTP/login abuse controls and rate limiting.
- File validation and scanning/quarantine.
- Request/response and cache privacy protections.
- Secure data retention, deletion and legal-hold policies.
- No sensitive personal records in ordinary application logs.

Security implementation must address OWASP API authorization risks, including BOLA/IDOR.

## 7. Backend Domain Architecture

The following are **logical module owners**, not individual microservices or independent databases.

| Domain | Primary ownership |
|---|---|
| Identity | Accounts, verified identities, sessions |
| Workspace | Registration, membership, workspace lifecycle |
| Authorization | Grants, delegation, approvals and scope |
| Commercial | Workspace plan catalog, subscriptions, entitlements, quotas and SaaS billing |
| Consumer commerce | Personal Free/Premium subscriptions, consumer billing and benefits |
| Property | Property/building/floor hierarchy, ownership and policies |
| Inventory | Rentable units/beds, assets, amenities, physical readiness |
| Discovery | Public rental listings, authenticity, moderation and search |
| Acquisition | Inquiries, offers, negotiation, applications and visits |
| Tenancy | Capacity allocation, occupancy, possession, household changes and transfers |
| Agreement | Agreement versions, signing obligations and execution evidence |
| Rental billing | Charge schedules, utility readings, invoices, adjustments and receivables |
| Rental payments | Payment intents, verified receipts, allocations and provider reconciliation |
| Deposit | Deposit liabilities, deductions, holds and final settlement |
| Maintenance | Helpdesk, assignments, repairs, escalation and completion |
| Document | File metadata, document versions, grants and access audit |
| Notification | Inbox, preferences, delivery intents and provider statuses |
| Tours/media | 360° scenes, publication snapshots and processing |
| Services | Public directory, Premium ads, providers and service requests |
| Sales | Restricted property-sale listings, inquiries and viewings |
| Police verification | Authority membership, cases, checks and decisions |
| Reviews | Residence eligibility, ratings, moderation and owner responses |
| Advertising | Drafts, approvals, social publication and optional paid budgets |
| Reporting | Metrics, dashboard projections, snapshots, forecasts and exports |
| AI/recommendation | Scoped retrieval, ranking, explanation, drafts and proposals |
| Platform admin | Commercial configuration, moderation and support |
| Orchestration | Atomic multi-module use-case coordination and operation tracking |

### Module dependency policy

A module must not directly modify another module's JPA entities or repositories.

Cross-module operations use exported application interfaces.

For operations requiring multiple domain writes to succeed together, an application-level coordinator owns one local PostgreSQL transaction.

Example:

```text
MoveInUseCase
  ├── ValidateTenancyAndAuthority
  ├── RecordPossession
  ├── CreateSignatureObligation
  ├── PersistAuditRecord
  └── PersistOutboxEvent
          |
       COMMIT
```

External messaging and signing requests occur after commit.

Avoid `REQUIRES_NEW` for constituent writes that must roll back atomically.

## 8. Core Domain Invariants

### SAS-DOM-001 — Rentable Inventory

- One independently rentable unit/bed cannot have incompatible overlapping allocations.
- A room and its beds cannot be double-counted as independent additive capacity.
- A scheduled visit does not reserve rental inventory.
- A future notice does not establish vacancy.
- Property readiness, listing publication, legal tenancy and physical possession are separate states.

Use transactional constraints and appropriate exclusion/locking strategies, not application-only `exists()` checks.

### SAS-DOM-002 — Rental Finance

- Use `BigDecimal` and PostgreSQL decimal types.
- Define currency, precision and rounding explicitly.
- Issued invoices and posted financial records are immutable.
- Corrections use referenced adjustment entries.
- Verified payment allocations cannot exceed eligible available funds.
- Deposits remain liabilities until authorized accounting events change them.
- Provider payment receipt and beneficiary settlement are separate states.
- Pending payment must never be shown as verified collection.
- SaaS revenue, consumer subscription revenue, rent, deposits and advertising spend must remain distinguishable.

### SAS-DOM-003 — Tenancy and Legal Execution

Maintain separate state machines for:

- Application/offer.
- Capacity reservation.
- Agreement execution.
- Physical possession.
- Rent account/collection.
- Notice and renewal.
- Physical handover.
- Deposit settlement.
- Property readiness/publication.

The source specifies a default product deadline of **ten calendar days after actual move-in** for completing required lease signatures, subject to earlier applicable legal/contractual prerequisites.

Store original deadlines, signer evidence, completion timestamps and exception history without silently resetting deadlines.

### SAS-DOM-004 — Approvals

Approvals bind to the exact business action, aggregate version, amount, resource and authority.

Changed amounts, revoked permissions, expired delegation or materially changed content invalidate stale approval where policy requires.

Approval does not itself mean an external refund, signature, repair or advertisement has completed.

## 9. API Architecture

### SAS-API-001 — Transport

Use REST over HTTPS with canonical `/api/v1` routes.

Maintain authoritative OpenAPI contracts for FS-01 through FS-36.

Generate TypeScript clients from the canonical contract; do not edit generated DTOs manually.

### SAS-API-002 — Commands and Queries

- `GET` returns an authorized resource or projection.
- `POST` creates a resource or executes a documented domain command.
- `PATCH` applies a documented partial modification.
- `202 Accepted` means a durable asynchronous operation exists, not that an external provider succeeded.
- Version-sensitive mutations use strong ETags and `If-Match`.
- Idempotent business commands accept a scoped `Idempotency-Key`.
- Pagination uses opaque, scope-bound cursors.
- Return standardized `application/problem+json` errors with stable codes and correlation IDs.

Avoid unrestricted generic status-update APIs for approval, finance, signing and possession workflows.

### SAS-API-003 — Required API Families

```text
/api/v1/auth/*
/api/v1/me/*
/api/v1/public/*
/api/v1/workspaces/*
/api/v1/agreements/*
/api/v1/invoices/*
/api/v1/helpdesk-tickets/*
/api/v1/documents/*
/api/v1/operations/*
/api/v1/provider/*
/api/v1/police/*
/api/v1/admin/*
```

Every API operation must document scope, authentication, permission, entitlement, validation, concurrency, idempotency, response and error semantics.

Next.js must not create a parallel financial or entitlement API.

## 10. Frontend Architecture

Use the Next.js App Router with feature-oriented organization.

```text
frontend/
  src/
    app/
      (public)/
      (auth)/
      workspace/
      tenant/
      provider/
      police/
      admin/
    features/
      properties/
      inventory/
      listings/
      appointments/
      applications/
      tenancy/
      agreements/
      rental-finance/
      helpdesk/
      subscriptions/
      marketplace/
      reporting/
    components/
      ui/
      layout/
      forms/
    lib/
      api/
        browser.ts
        server.ts
      auth/
      validation/
      permissions/
      formatting/
```

### Frontend design rules

- Use Server Components where server rendering is appropriate.
- Use Client Components for interactive workflows.
- Maintain one typed backend client/transport strategy.
- Use URL-based filters for reports and searchable work queues.
- Isolate account/workspace contexts in query keys and cached state.
- Never trust frontend permissions as a security boundary.
- Provide pending, stale-version, failed, partial-success and retry states.
- Do not optimistically mark payments, approvals, signatures or move-ins successful.
- Meet the WCAG 2.2 AA accessibility target.
- Make mobile and desktop role journeys functionally equivalent.

Public rental projections may be cached with explicit invalidation. Private dashboards, sensitive documents, police records and restricted sales require request-specific authorization and no shared caching.

## 11. Database and Persistence Architecture

### SAS-DB-001 — Persistence Model

PostgreSQL is the authoritative relational data store.

Logical schema areas include:

```text
identity / sessions
workspaces / memberships
plans / subscriptions / entitlements / usage
properties / units / beds / assets
listings / inquiries / visits / applications
tenancies / allocations / possession
agreements / signatures / documents
charges / invoices / allocations / receipts
deposits / settlements
maintenance / tasks / approvals
notifications / provider delivery
consumer subscriptions / services / sales
police cases / verified reviews
audit / outbox / inbox / jobs
reporting projections
```

These are logical ownership areas; they need not become separate PostgreSQL schemas.

### SAS-DB-002 — Integrity

- Scope foreign keys by owning workspace where applicable.
- Use uniqueness constraints for logical business keys.
- Use optimistic versions for concurrent edits.
- Use explicit locking or conditional SQL for quota and financial conservation.
- Use PostgreSQL exclusion/interval constraints where suitable for allocations.
- Preserve immutable evidence and posted transaction history.
- Use effective-dated policy and pricing versions.
- Track financial source/issuer/payment purpose.
- Avoid redundant mutable counters as authoritative business truth.

### SAS-DB-003 — Migrations

Use Flyway SQL migrations with controlled execution during deployment.

Only one migration process applies schema changes for a release.

Application runtime credentials must not own tables or execute unrestricted schema modification.

Use expand/migrate/contract migration strategies for rolling compatibility.

## 12. Durable Jobs and Event Processing

### SAS-ASYNC-001 — Processing Architecture

Use a transactional outbox and PostgreSQL-backed durable work queue.

```text
Business Command
    |
Database Transaction
    ├── Domain Changes
    ├── Audit
    └── Outbox / Work Intent
    |
   COMMIT
    |
Durable Worker Claims Job
    |
Authorized Provider / Processing Adapter
    |
Verified Result / Reconciliation
    |
Authoritative Domain Update
```

Initial semantics:

**At-least-once execution with idempotent business effects.**

### SAS-ASYNC-002 — Reliability

Workers require:

- Concurrent-safe claims with lease expiry.
- Retry attempt counters and scheduled next attempts.
- Fencing/version validation.
- Bounded exponential backoff and jitter.
- Explicit terminal/blocked states.
- Provider-outcome reconciliation.
- Audited manual recovery.
- Graceful shutdown and cancellation.
- Correlation/causation identifiers.
- Current authority revalidation before consequential effects.

Avoid external provider calls inside long-running database transactions.

Kafka is an optional later extension when multiple durable consumers, replay requirements or event-stream scale justify it.

## 13. External Integration Architecture

Define provider-neutral ports and adapters.

| Integration | Required architectural behavior |
|---|---|
| SaaS payment | Server-priced orders, verified callbacks, idempotent subscription transitions |
| Rental payment | Payee/fee verification, purpose-scoped allocations, uncertain-state recovery |
| Consumer membership payment | Independent subscriber and entitlement ledger |
| E-signature | Authenticated requests, signer verification, document evidence retrieval |
| WhatsApp/SMS/email | Consent, templates, quotas, delivery status and deduplication |
| Social advertising | Content/version approval, provider capability checks and takedown reconciliation |
| Object storage | Authorized upload intents, validation/quarantine and expiring grants |
| Map/location | Licensed data, privacy rules and text-search fallback |
| Police verification | Approved authority modes, case-scoped identity, truthful official outcomes |
| AI provider | Permission-filtered retrieval, validated outputs and human-confirmed actions |

An integration adapter never becomes the independent authority for a Java-owned financial, tenancy or approval aggregate.

All unconfigured or unsupported external capabilities remain explicitly disabled, conditional or simulated in development.

## 14. SaaS Control Plane Architecture

### SAS-SAAS-001 — Workspace Plans

Support five database-configurable workspace plans:

| Plan | Initial rentable capacity |
|---|---:|
| Free | 5 |
| Silver | 50 |
| Gold | 200 |
| Platinum | 1,000 |
| Diamond | Contract-defined |

These are proposed initial catalog defaults, not final approved prices or terms.

Keep plan versions, effective dates, features, quotas, add-ons, discounts, grandfathering and restrictions in a versioned catalog.

### SAS-SAAS-002 — Entitlement Evaluation

Effective entitlements derive from:

- Subscriber and plan/version.
- Current subscription state.
- Purchased add-ons.
- Time-limited grants.
- Approved overrides.
- Workspace restrictions.
- Current usage and quota policy.

Quota allocation must be transactional.

A quota downgrade must not delete existing rental records or disable necessary corrections, receipts, helpdesk access or tenancy completion.

### SAS-SAAS-003 — Consumer Membership

Maintain a separate consumer catalog:

- `TENANT_FREE`
- `TENANT_PREMIUM`

Personal subscriptions belong to an account and follow that account across authorized tenancies.

Restricted sale buyer access additionally requires independently verified rental residence and an active paid Premium entitlement. Premium alone is insufficient.

Premium service requests use their separate personal entitlement policy and do not automatically expose requests to landlords.

## 15. Search, Media, AI and Reporting

### SAS-SEARCH-001 — Search

Start with indexed PostgreSQL queries and approved publication projections.

Support location, normalized rent, unit/bed type, amenities, occupancy/availability and other supported filters.

Final booking/allocation always rechecks authoritative current state.

Search indexes, if introduced later, are rebuildable derived projections rather than sources of truth.

### SAS-MEDIA-001 — Media

Keep public listing media separate from private agreement, verification and Premium sale media.

Uploads use authenticated resource-scoped intents and validation before publication.

The initial immersive tour format is linked 360° panoramas with accessible static/text alternatives. True geometric 3D reconstruction is conditional.

### SAS-AI-001 — AI

AI augments, but never replaces, authoritative domain logic.

Pipeline:

```text
Identity and entitlement
 → Resource visibility
 → Deterministic retrieval
 → Optional AI generation/ranking
 → Schema/evidence validation
 → Fresh authorization and state checks
 → User-visible proposal
 → Explicit confirmation
 → Normal authorized domain command
```

AI must not autonomously sign agreements, approve financial transactions, issue police findings, release deposits or alter occupancy.

Keep a non-AI fallback, scope-safe retrieval, usage metering, model evaluation, kill switches and provider privacy policies.

### SAS-REPORT-001 — Reporting

Define a versioned metric catalog and derive reports from authorized authoritative records.

Separate:

- Rent billed versus collected.
- Receipt timing versus allocations to due periods.
- Rent versus utilities and deposits.
- Active occupancy versus scheduled arrivals.
- Verified amounts versus pending/disputed amounts.
- Actual performance versus forecasts.
- Platform revenue versus landlord rental collections.

Large exports use durable background jobs, snapshot/freshness metadata and authorization rechecks.

## 16. Deployment Architecture

### Local Environment

Use Docker Compose with:

```text
edge
web
api
worker
db
migrate (one-shot)
```

Include configured object storage access and optional development adapters.

Use the same backend image for API and worker with separate entrypoint profiles.

### Production Environment

Select AWS or GCP through a deployment ADR.

An AWS option may use:

- CloudFront/WAF or approved HTTPS ingress.
- Managed container runtime such as ECS/Fargate.
- RDS PostgreSQL.
- S3 private object storage.
- Secrets Manager.
- CloudWatch and OpenTelemetry-compatible monitoring.
- Managed backup and recovery facilities.

A GCP deployment should map to equivalent supported managed services.

Use private networking, independent scaling of API/workers, least-privilege workload identities, encrypted backups and controlled migrations.

Kubernetes is conditional on operational needs.

Production availability, RPO/RTO, deployment region, provider compatibility and residency requirements must be confirmed before launch.

## 17. Non-Functional Requirements

| Quality attribute | Design requirement |
|---|---|
| Security | Zero unauthorized cross-workspace/account resource access |
| Financial correctness | No duplicate posted obligation, receipt or consumed deposit |
| Concurrency | Prevent invalid overlapping allocations and quota oversubscription |
| Reliability | Recover uncertain external operations without claiming false success |
| Availability | Stateless scalable APIs and independently recoverable workers |
| Maintainability | Acyclic modules, explicit ownership and contract tests |
| Observability | Traceable operations, structured logs, business metrics and audit |
| Accessibility | WCAG 2.2 AA target |
| Data integrity | Immutable posted records, constrained relationships and verified migrations |
| Recovery | Tested database/object restore, outbox replay and provider reconciliation |
| Performance | Explicit Phase 0 load model and representative benchmarks |
| Compatibility | Pinned dependency and contract/schema versions |

Proposed initial engineering performance targets from the source, subject to load-model approval:

- Ordinary internal API reads: p95 ≤ 500 ms.
- Indexed public search: ≤ 1 second.
- Bounded dashboard summary: ≤ 2 seconds.
- Accepted background job visibility: within 5 seconds.

These are unverified design targets, not demonstrated SLAs. External provider operations and large exports require separate targets.

## 18. Testing and Verification Architecture

### Unit Tests

Domain invariants, state transitions, entitlement evaluation, date calculations, rounding and approvals.

### Integration Tests

Real PostgreSQL/Testcontainers tests for:

- RLS and pooled-session isolation.
- Concurrent quota creation.
- Inventory/bed allocation constraints.
- Financial posting and refunds.
- Deposit conservation.
- Outbox/inbox replay.
- Duplicate billing runs.
- Signature deadline integrity.
- Provider callback deduplication.

### Contract Tests

OpenAPI validation, generated-client compatibility, standard error behavior and provider contracts.

### End-to-End Tests

Real browser → ingress → API → PostgreSQL paths covering owner, manager, tenant, consumer, provider, police and administrator scopes.

### Security Tests

Revoked grants, IDOR/BOLA, CSRF, private-document access, cached-data leakage, scope-changing requests, provider webhook replay and AI injection.

### Operational Tests

Process restarts, failed migrations, schema compatibility, backup/restore, retry exhaustion, uncertain payments and graceful worker shutdown.

Do not treat mocks or sandbox adapters as production-provider verification.

## 19. Architecture Decision Records

Maintain ADRs for:

- Java modular monolith versus documented Go alternative.
- Backend module ownership.
- Workspace/database isolation and RLS.
- Browser session and CSRF design.
- API/error/idempotency conventions.
- Financial and occupancy transaction boundaries.
- PostgreSQL outbox/worker approach.
- Provider callback and uncertain-outcome handling.
- AWS versus GCP and deployment region.
- Search/media privacy architecture.
- AI provider/retrieval/cost controls.
- Enterprise data isolation.
- Optional Go/service extraction.
- Observability and recovery objectives.

Each ADR must document context, options, chosen decision, rationale, consequences and reconsideration triggers.

## 20. Implementation Phases

| Phase | Architecture/implementation scope |
|---|---|
| 0 | Requirements traceability, repository assessment, ADRs, module/API/data ownership and workload model |
| 1 | Identity, workspace foundation, secure sessions, Free provisioning, quota base and Compose integration |
| 2 | Property, inventory, ownership, delegation and initial dashboards |
| 3 | Public listing search, leads, appointments, offers, applications and marketplace moderation |
| 4 | Tenancy, possession, agreements, digital signatures, tours and onboarding integration |
| 5 | Rental charges, utility billing, invoices, payments, reconciliation and financial reports |
| 6 | Maintenance, renewal, offboarding, deposits, reviews, police workflow and complete core operations |
| 7–7A | SaaS/consumer commerce extensions, Premium services, concierge, restricted sales and personal AI where configured |
| 8 | External communication, social publication approvals, controlled advertising and promotional integrations |
| 9–10 | Advanced analytics, AI, conditional brokerage, search optimization and justified service extraction |
| 11 | Enterprise capabilities and advanced integration/isolation work as explicitly gated |
| 12 | Full regression, accessibility, security, load, resilience, restore and release-readiness evidence |

Each phase must deliver integrated vertical slices rather than disconnected schemas, mockups or service scaffolds.

All source mandatory requirements must remain traceable even when sequenced into later phases. Conditional provider capabilities retain explicit enablement prerequisites.

## 21. Required Architecture Deliverables

Produce and maintain:

```text
docs/
  architecture/
    overview.md
    system-context.md
    container-architecture.md
    module-boundaries.md
    domain-model.md
    data-ownership.md
    tenancy-isolation.md
    security-architecture.md
    api-architecture.md
    event-and-job-contracts.md
    integration-architecture.md
    deployment-architecture.md
    nonfunctional-targets.md
    disaster-recovery.md
    version-compatibility.md
    adrs/
  requirements-matrix.md

contracts/
  openapi/
    lmt-v1.json
  events/

infra/
  compose/
  deployment/
```

Create C4 context/container/component diagrams, key state-machine diagrams, and sequence diagrams for the highest-risk workflows.

Maintain traceability:

**Requirement ID → Owning module → UI route → API operation → Aggregate/table → Transaction/event → Permission → Test → Evidence → Implementation status.**

## 22. Architecture Acceptance Gates

Architecture design is complete only when:

1. Every FS-01–FS-36 contract has an owning logical module.
2. Every mandatory source requirement is mapped or explicitly tracked.
3. Workspace, consumer, provider, police and platform access boundaries are documented.
4. Critical financial and capacity invariants have an enforcement strategy.
5. Transaction and asynchronous side-effect boundaries are explicit.
6. API and event contracts specify versioning, concurrency and failure behavior.
7. Provider-dependent functionality has documented prerequisites and fallback modes.
8. Deployment/runtime responsibilities, migration ownership and secrets boundaries are defined.
9. Non-functional targets are agreed and measurable.
10. Verification scenarios are traceable to required workflows.
11. Material architecture alternatives and unresolved commercial/legal decisions have ADRs or decision-register entries.
12. Planned architecture components are not falsely represented as implemented or production-verified.

**Final architectural direction:** A Java 21/Spring Boot modular monolith, Next.js presentation layer, PostgreSQL transactional core, durable Java workers and private object storage, with selective external adapters and evidence-based extraction only when justified.

This architecture must support the complete LMT rental lifecycle, configurable landlord SaaS plans, independent consumer Premium membership, secure cross-role workflows, financial correctness and future scaling without introducing unnecessary distributed complexity.

## Current implementation checkpoint — 2026-11-07

The proposed architecture above is not a completed implementation. The repository now has Next.js presentation, the Java 21/Spring Boot core, PostgreSQL/Flyway, a public catalogue and a local sandbox identity/session/Free workspace provisioning slice. [ADR 0002](architecture/adrs/0002-sandbox-identity-and-free-workspaces.md) records actual module/table ownership, transactional scope, session/CSRF and workspace row policies. General workers/storage, live providers, quota consumption, other domain modules and full deployment gates remain pending. See [release evidence](release-status.md) and [requirement statuses](requirements-matrix.md) for the exact boundary.

Account-control continuation: [ADR 0003](architecture/adrs/0003-account-profile-and-session-controls.md) records additive V4 profile versions, opaque session-management selectors, revocation tombstones, account-scoped command recovery and in-app notices. Full identity and Phase 1 remain incomplete.

Workspace membership continuation: [ADR 0004](architecture/adrs/0004-sandbox-workspace-membership.md) records the V5 invitation/member scope, workspace → commercial command dependency, atomic manager capacity and recipient-bound RLS. The in-app invitation inbox is functional; live delivery, general entitlements and property delegation remain pending.

Commercial continuation: [ADR 0005](architecture/adrs/0005-membership-entitlements-and-usage.md) records the version-bound membership policy subset, commercial usage-event ownership, owner-only snapshots and reconciled stock-versus-period metrics. Full entitlements and broader quota workflows remain pending.

Durable welcome continuation: [ADR 0006](architecture/adrs/0006-durable-welcome-operations.md) records V8 leased claims, fenced DB-only completion, bounded retries, authority checks, owner-scoped operation recovery and preserved event/message IDs. The existing workspace module remains the single owner; generic provider/import/storage jobs and production monitoring remain pending.

Commercial review continuation: [ADR 0007](architecture/adrs/0007-commercial-plan-review.md) records V9 explicit expiring review authority, operator bootstrap, bounded audited version projections and private admin routes. This is a catalogue-owned read capability, not workspace authority or full plan administration.

Unpublished draft continuation: [ADR 0008](architecture/adrs/0008-unpublished-plan-drafts.md) documents V10 explicit draft authority, inherited immutable commercial terms, actor/key recovery and per-plan version allocation. Public publication and subscriber transitions remain unavailable.

Editorial publication continuation: [ADR 0009](architecture/adrs/0009-reviewed-editorial-publication.md) records V11 expiring exact-version reviews, explicit publication authority, ordered effective dates, atomic capability inheritance through the commercial module and preservation of historical subscription references.

Editorial edit continuation: [ADR 0010](architecture/adrs/0010-editorial-draft-edits.md) records V12 independent edit authority, locked revision updates, immutable-runtime before/after evidence and invalidation of prior publication reviews.

Draft discard continuation: [ADR 0011](architecture/adrs/0011-preserved-draft-discard.md) records V13 independent discard authority, a terminal immutable state, evidence-preserving command recovery and serialization against publication/editing.

Offering availability continuation: [ADR 0012](architecture/adrs/0012-offering-availability.md) records V14 family-scoped withdrawal/restoration, independent authority and atomic command evidence, plus V15 availability-bound publication reviews. Published terms and subscriber references remain immutable.

Private storage foundation: [ADR 0013](architecture/adrs/0013-private-upload-quarantine.md) records V16 account-owned intents/RLS, an immutable private object interface and a guarded local quarantine adapter. Actual upload bytes persist; scanning, clean release, expiring download grants and production S3 remain unavailable. This is a Phase 1 foundation, not completion of FS-21 or the Premium vault.

Private storage recovery: [ADR 0014](architecture/adrs/0014-private-storage-recovery.md) records V17 subject-scoped integrity observations and immutable command results, strong review versions, exact-byte missing-file recovery and no-overwrite local file creation. Transfer receipts and quarantine remain independent of integrity and malware clearance; production storage/scan jobs/retrieval remain pending.

Durable scan preparation: [ADR 0015](architecture/adrs/0015-durable-scan-preparation.md) records V18 queue/attempt/command ownership, V19 subject constraints, atomic quarantine enqueue, fenced prerequisite workers and bounded recovery. Actual scan clearance remains unavailable; the worker never writes a clean document state. Existing workspace workers remain independently owned.

Entitlement layering continuation: [ADR 0016](architecture/adrs/0016-scoped-temporary-entitlement-overrides.md) records V20 independently scoped operator authority, expiring manager-onboarding overrides, immutable command evidence and membership-lock serialization. Existing Entitlements remains the sole evaluator; permissions, security restrictions and quota consumption remain separate.

### Manager evidence reconciliation (2026-11-09)

Commercial `ManagerUsageEvidence` supplies allocation/event integrity counts to both the owner usage projection and `ManagerCapacity.consume`. The usage projection remains owner-authorized and repeatable-read; consumption checks evidence under the existing grant row lock and membership transaction. The helper reads commercial-owned records only and does not expand member visibility for invitation recipients. Current sandbox reservations are transaction-local, so persisted reservations are flagged, not expired or repaired automatically. Existing membership audit and usage writes remain atomic; no new persistence owner or repair API is introduced. See DEC-034 and the release checkpoint for verification and pending scoped repair work.

### Reviewed capacity counter recovery (2026-11-09)

Commercial `CapacityRepairs` owns the bounded sandbox repair use case and V21 command evidence. Its scoped authority is distinct from entitlement administration and requires active workspace ownership; existing owner RLS supplies source access. Repairs serialize with membership operations and grant changes, validate source identity/event consistency, then atomically persist derived counters, repair revision, before/result and domain audit. No source records or allowance are rewritten. Read-only usage links to the authorized review screen. See ADR 0017 for policy and pending production/non-owner recovery.

### Owner repair-history projection (2026-11-09)

Commercial CapacityRepairHistory projects retained capacity_repair_command evidence through owner-authorized repeatable-read orchestration. V22 adds SELECT-only owner RLS and date/revision indexes without changing command INSERT authority. API cursors bind actor/workspace/date range; owner loss denies subsequent reads. Repair-grant revocation preserves owner historical access. UI displays minimal before/after evidence with independent loading/error/pagination state and request cancellation. See DEC-036 and the release checkpoint; general audit exports and production support remain pending.

### Independent sandbox email identity (2026-11-09)

V23 extends the existing identity-owned account/challenge schema for email-only accounts. The same Java challenge service, delivery worker and opaque session controls serve both channels; Next.js only submits typed proof requests. Email uniqueness handles concurrent proof consumption, domain normalization preserves local-part identity, and sandbox mode cannot authenticate live identities. Profile exposes nullable masked contacts. No linking/merge business engine or separate authentication backend was added. See ADR 0018 and DEC-037 for supported address policy and live-provider/linking prerequisites.

### Explicit sandbox email linking (2026-11-09)

Identity-owned EmailLinks/EmailLinkController extend the existing challenge and account/session modules. V24 adds purpose/account/profile bindings; Java owns recent-session checks, versioning, contact ownership locks, proof limits, atomic profile/command/audit/notice persistence and retry recovery. Public sign-in rejects linking proofs. Email sign-in creation and linking share contact locks, preventing concurrent ownership transfer. Next.js renders confirmations and forwards typed requests; no identity resolution is performed in the frontend. See ADR 0019 and DEC-038 for the narrow no-merge policy.

### Shared bidirectional sandbox contact linking (2026-11-09)

IdentityLinks and IdentityLinkController replace the email-specific class names while retaining the existing endpoints and persisted email operation identifiers. V25 adds link_mobile purpose/channel binding and the narrowly required mobile-column update grant. Both contact directions derive the writable column from an allowlisted channel and prohibit overwriting an existing contact. Mobile ownership races serialize with the established sign-in path. Workspace invitation discovery continues to derive from the now-proven contact; acceptance remains a separate authorized business command. Historical migrations/evidence are unchanged.

### Bound session reauthentication continuation

Identity-owned SessionReauthentication/ReauthenticationController and V26 reuse challenge delivery and durable sessions. `/account/reauthenticate` consumes OpenAPI 0.22.0 request/result contracts. Existing-contact proof refreshes recent authority, rotates the session/CSRF and preserves the absolute session cap. Audit/registry/proof/notice are transactional; servlet cookie rotation has explicit fail-closed recovery. See [ADR 0020](architecture/adrs/0020-bound-session-reauthentication.md).

### Recipient-owned security notice review

AccountControls owns the existing security notice read transition and exposes it through AccountController; V27 grants only read-state updates. The canonical `/me/notifications` projection reuses the legacy security-notifications store, actor-bound pagination and account RLS. NotificationInbox connects `/account/security` to generated OpenAPI 0.23.0 contracts. Strong preconditions, command locks, row locks and transactional audit preserve first-read evidence through retries/races. See [ADR 0021](architecture/adrs/0021-security-notice-read-state.md).

### Reviewed session batch revocation

AccountControls/AccountController coordinate selected-session preview and atomic confirmation using existing identity persistence. OpenAPI 0.24.0 supplies generated selection/preview/result contracts for `/account/security/selected`. Deterministic registry row locking, strong preview ETag, recent authentication and scoped command replay protect the transition; framework deletion, tombstones, audits and notice share one transaction. No schema/dependency change. See [ADR 0022](architecture/adrs/0022-reviewed-session-batch-revocation.md).

### Linked-contact recovery and authentication generation

ChallengeService and AccountControls retain identity ownership of recovery and session registration. V28 adds account generation/recovery cutoff and the dedicated challenge purpose. Generation locking/rechecks protect the verification-to-registration boundary; recovery atomically consumes proof, revokes framework/registry sessions and records audit/notice. Public Account payloads stay unchanged; internal VerifiedAccount carries generation. `/account/recovery` consumes generated OpenAPI 0.25.0 contracts and exposes honest session-check/fresh-sign-in recovery after uncertain cookie delivery. See [ADR 0023](architecture/adrs/0023-linked-contact-account-recovery.md).

### Recovery outcome continuity

V29 stores recovery completion evidence on the identity-owned challenge in the same transaction as recovery. OpenAPI 0.26.0 adds RecoveryChallenge read-grant fields and POST `/auth/recovery/outcome`; the public Account contract remains unchanged. Domain-separated HMAC grants are challenge/session-bound, short-lived and scoped to coarse no-store status, allowing recovery confirmation after cookie loss without conferring account access. See [ADR 0024](architecture/adrs/0024-protected-recovery-outcome.md).

### Email workspace invitation continuation

WorkspaceMembership/MembershipController retain ownership of invitations and membership transitions. V30 extends contact storage, recipient index and RLS; identity exposes its existing contact normalization function, while commercial ManagerCapacity remains the only quota writer. OpenAPI 0.27.0 extends the existing invitation request/response; membership screens render channel/masked destination and preserve explicit confirmation/acceptance. No second membership backend or new provider adapter. See [ADR 0025](architecture/adrs/0025-email-workspace-invitations.md).

### Local container topology

[ADR 0026](architecture/adrs/0026-local-compose-processes.md) defines the six-service local stack. [Compose runbook](../infra/compose/README.md) covers loopback ingress, runtime/migration credential separation, worker scheduling, persistent volumes, health and recovery. It preserves the native database-only setup; it is not production infrastructure.

Contact removal now uses retained-contact, current-profile reauthentication proof and atomic session/proof invalidation; see [ADR 0027](architecture/adrs/0027-retained-contact-removal.md). This remains a local sandbox identity capability.

Dedicated account notification routes reuse account-owned notices and enforce filtered chronological keyset pagination; see [ADR 0028](architecture/adrs/0028-account-notification-inbox.md). Tenant/workspace route placement grants no extra data access.

[ADR 0029](architecture/adrs/0029-enforced-account-notification-preferences.md) adds identity-owned account notification preferences and an explicit delivery interface used by the authorized workspace outbox worker. Personal opt-in copies remain separate from shared workspace messages and mandatory security notices.

[ADR 0030](architecture/adrs/0030-optional-notice-quiet-hours.md) documents optional quiet-hour deferral using the existing workspace outbox: shared notices commit immediately, pending personal copies recheck policy within 60 seconds, and policy waits do not spend failure retries.

[ADR 0031](architecture/adrs/0031-live-sign-in-delivery.md) adds opt-in live sign-in adapters, durable provider submission and mode-bound identities/sessions. Default Compose remains sandbox-only; [live configuration and acceptance gates](../infra/compose/LIVE-IDENTITY.md) are separate from completed fixture verification.


## Local container verification delivery — 2026-11-09

AUTH-001 / MARKET-001, FS-01/22: frontend and backend remain **Partial** overall. Compose now routes sandbox email to Mailpit and mobile codes to a dedicated durable SQLite SMS simulator. `/auth/configuration` adds `localContainers` in OpenAPI 0.33.0; login links to the correct inboxes. Durable dispatch, fenced outcome/audit updates and unknown-outcome recovery reuse the identity delivery store; no migration is added. Native private-inbox and opt-in live transport behavior remain separate. See [local inbox runbook](../infra/compose/LOCAL-INBOXES.md). This verifies local transport, not real contact ownership or live provider delivery. INT-03/04/09–12 remain separately tracked; broader phase gates are unchanged.


Account pending notification visibility uses the workspace-owned outbox projection and the existing identity preference interface, with no new queue or cross-module writes. OpenAPI 0.34.0 and V36 implement bounded recipient queries. See [ADR 0032](architecture/adrs/0032-personal-pending-notification-deliveries.md).


Essential manager access notices use the identity-owned transaction-mandatory interface invoked by authorized workspace membership commands. No second queue or cross-module table writes. V37/OpenAPI 0.35.0 add the category. See [ADR 0033](architecture/adrs/0033-workspace-access-notifications.md).


Owner invitation response notices reuse the identity-owned transaction interface with server-derived, currently authorized recipients, deterministic outcome events and audited suppression. V38/OpenAPI 0.36.0 add the category. See [ADR 0034](architecture/adrs/0034-invitation-outcome-notices.md).


Manager invitation expiry now uses bounded PostgreSQL reconciliation sharing membership advisory locks, with atomic system audit and an authorized-owner in-app notice. V39 and the Java jobs-only worker preserve existing RLS through a narrow function; no new external service. See [ADR 0035](architecture/adrs/0035-durable-invitation-expiry.md).


Expired invitation recovery now uses a versioned/idempotent owner command and a private shared invitation-creation routine. V40 enforces unique same-workspace lineage; existing membership locks and eligibility remain authoritative. See [ADR 0036](architecture/adrs/0036-expired-invitation-replacement.md).


Owner-only invitation expiry diagnostics read the existing workspace-owned invitation state at a shared database time and repeatable-read snapshot. No job side effects or new store. See [ADR 0037](architecture/adrs/0037-owner-invitation-expiry-diagnostics.md).

## Implemented property setup boundary — 2026-11-10

See [ADR 0038](architecture/adrs/0038-property-register-and-capacity.md): Java owns private property hierarchy and state transitions; commercial module owns rentable allocations/usage/grants. The Next.js property/unit routes use OpenAPI 0.40.0 through the existing one-origin session/CSRF boundary. V41 adds owner-scoped storage, transactional command recovery and immutable action history. This is draft setup with blockers, not completed authority, asset inventory, occupancy or rental accounting. Full architectural and integration gates remain tracked separately.

## Subscription dashboard projection — 2026-11-10

[ADR 0039](architecture/adrs/0039-subscription-dashboard.md) documents the authorized read-only commercial projection, contracted-plan preservation, repeatable-read consistency, private route/session boundary and explicit availability semantics. Existing subscription/capacity storage remains authoritative; dashboard reads do not introduce accounting mutations or providers.

## Sandbox upgrade transaction — 2026-11-10

[ADR 0040](architecture/adrs/0040-versioned-sandbox-subscription-upgrades.md) documents the commercial-owned quote/change/settlement transaction, V42–43 scope constraints, effective-grant fingerprint, membership/subscription/grant lock order and explicit sandbox-only provider boundary. This extends the subscription projection without adding a competing payment backend. Live callback/outbox dispatch remains pending; no external effects are made by the simulator.


## Scheduled subscription downgrades

[ADR 0041](architecture/adrs/0041-scheduled-subscription-downgrades.md) extends commercial with immutable review, boundary scheduling and a leased Java worker. V44 retains excess usage while lowering allowances; atomic capacity writers still guard increases. Central entitlement forecasting, version/fingerprint checks, reauthorization and transactional audit/notification preserve the existing module boundaries. Local paid-to-Free is operational; paid renewal/provider policy remains pending.


## Subscription cancellation

[ADR 0042](architecture/adrs/0042-subscription-cancellation.md) adds canonical cancellation endpoints and a dedicated UI using the existing V44 boundary engine. Immutable cancellation policy in the review distinguishes cancellation from ordinary downgrades without another subscription writer or migration. At application, Free continuation, capacity changes, cancellation audit and private notice commit together. Live money/retention policy and full signing continuity remain pending.


## Failed subscription payment recovery

[ADR 0043](architecture/adrs/0043-failed-upgrade-payment-recovery.md) adds V45 immutable local retry policy and attempt evidence to the existing commercial upgrade writer. Recovery reads are repeatable-read; retry intent and audit share the serialized subscription transaction. Unknown payments reconcile before another attempt. Upgrade failures preserve existing access and have no renewal grace; the renewal grace/restriction engine and live collection remain pending. No dependency or second payment backend was introduced.


## Durable subscription receipt recovery

[ADR 0044](architecture/adrs/0044-durable-subscription-settlement-recovery.md) separates immutable simulated payment receipt from retryable activation through V46 and a leased worker in the existing Java commercial module. Activation reuses the subscription writer, preserves receipt evidence on failure and fences reversals. UI/API distinguish paid evidence from activated grants. Live signed callback integration, renewal races and reversal compensation remain pending.


## Central layered entitlement evaluation

[ADR 0045](architecture/adrs/0045-centralized-layered-entitlements.md) extends the commercial engine with V47 immutable local inputs/revocations, active time/plan/mode scope, operational availability and protected corrections. Single-capability reads and domain commands share the evaluator; permissions and quota remain separate. Property mutations now serialize with entitlement configuration under the existing membership lock. Live add-on activation and full product availability remain pending.

Unit quota enforcement now reconciles scoped inventory, allocation mapping, counters and usage evidence under the existing authoritative command/grant locks. Contextual warnings use the private effective snapshot; see [ADR 0047](architecture/adrs/0047-unit-capacity-enforcement.md). V48 strengthens workspace-qualified evidence relationships. Bulk import remains unavailable pending FS-32 reservations and durable row recovery.

Counting definitions are now immutable and workspace-bound (V49), rather than selected globally by newest version. New sandbox workspaces use the independent-item definition; existing bindings remain unchanged. Commands and reconciliation share one quantity function. See [ADR 0048](architecture/adrs/0048-workspace-bound-unit-counting.md).

Workspace communication metering has its own immutable period/metric contracts and durable operation/evidence store (V50), separate from account authentication delivery and subscription payments. A local owner test uses actual Mailpit/SMS-inbox acceptance. Provider effects follow committed intent, and receipt completion serializes with reservations. Unknown outcomes remain held for reconciliation. See [ADR 0049](architecture/adrs/0049-communication-acceptance-metering.md).

## Communication recovery and amenity definitions continuation

V51 adds durable read-only local receipt reconciliation with owner reauthorization, bounded retries and fenced leases; unknown outcomes retain reservations until exact evidence settles them. Historical usage projects original contract periods. See [ADR 0050](architecture/adrs/0050-communication-evidence-recovery.md).

The inventory module owns workspace amenity definitions, retained normalized aliases and immutable version snapshots (V52–53). Configuration is separate from physical assessments and property authority. See [ADR 0051](architecture/adrs/0051-versioned-amenity-catalog.md). OpenAPI 0.53.0 covers both additions with generated frontend contracts.
