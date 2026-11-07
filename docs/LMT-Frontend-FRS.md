# LMT Frontend Functional Requirements Specification

## Contents

1. Purpose, roles and context
2. Frontend platform, shared validation and state behavior
3. Workspace and personal entitlement baselines
4. Route inventory and navigation
5. FS-01 through FS-36 screen contracts
6. All 195 detailed functional requirements
7. Cross-module acceptance scenarios
8. Requirement and original catalogue traceability
9. Phase coverage, unresolved decisions and completeness checklist

## Purpose and scope

This specification extracts the browser-facing requirements from **LMT — Final Full-Stack SaaS Development Prompt**, supplied as `Pasted text(20261107-051837).txt`. It covers public discovery, account and workspace onboarding, landlord and manager operations, tenant self-service, personal membership, providers, police verification and platform administration.

The scope is frontend behavior: screens, navigation, fields, validation feedback, actions, visible state transitions, permissions and entitlement affordances, confirmations, notifications, accessibility and acceptance outcomes. Backend implementation, database schemas, workers, infrastructure and deployment are excluded. Authoritative business outcomes remain interface dependencies: the frontend displays and responds to them, but does not calculate authority, settle money, verify identity or manufacture provider success.

All **195 original functional requirement IDs** and **36 FS contract IDs** are retained. Original FR catalogue aliases are retained separately. No new feature IDs, field limits, routes, prices, legal rules or acceptance thresholds are invented. A route described only as a tab, panel or named screen remains a named surface; abbreviated source routes are not silently expanded into new canonical URLs.

### Reading each requirement

Each requirement consists of its original ID and business rules, its linked FS screen contracts, and the cross-cutting frontend rules. The FS contracts supply roles, routes, inputs, actions, states and acceptance outcomes shared by the linked requirements. Only the relevant subset applies; a link does not make every operation in a contract applicable to every requirement. Source line references identify the attachment location.

Unqualified baseline behavior is **Must**, following the source's audited convention. Explicit Should, Could, optional, conditional, provider-dependent and commercial-approval qualifiers remain in force. Phase numbers indicate delivery sequencing, not permission to omit mandatory scope. Acceptance statements describe required behavior, not evidence that a product has been implemented or tested.

### Governing scope rules

- Free includes the complete essential rental lifecycle, baseline signing and deadline tracking, permitted manual agreement handling, billing/payment records, basic maintenance and historical documents. Corrections, required receipts, disputes, notices and tenancy completion must not become upgrade gates.
- Workspace subscriptions and personal consumer membership are independent. Rental payments, workspace SaaS bills and personal membership bills must remain visually and functionally distinguishable.
- A capability entitlement does not grant access to another person's or property's records. Ownership shares do not imply operational, signing, refund or payout authority.
- Published tours and ordinary viewing requests remain basic discovery. Premium adds convenience and does not guarantee availability, acceptance, maintenance completion or legal preference.
- Restricted sale discovery requires both verified qualifying rental residence and active **paid personal Premium**; a workspace paid plan, a trial or an unverified account is insufficient.
- Core helpdesk and essential tenancy functions work without Premium or AI. AI drafts and proposals require the normal user confirmation and authority; generated text is not evidence of an executed action.
- Police verification must distinguish approved live authority processing from assisted tracking and sandbox use. Social publication, payments, signatures and delivery must show actual provider-confirmed states.
- Physical possession, allocation, agreement execution, invoice/payment status and financial closure are separate states. Agreement expiry does not imply departure, and handover does not imply all money has settled.
- Prices, allowances without explicit defaults, jurisdiction rules, provider readiness and operational staffing remain configuration or open decisions. The source does not establish legal compliance.

## Roles and context

| Context | Roles and frontend boundary |
|---|---|
| Public and personal account | Visitor/prospective tenant, applicant, active/former tenant, co-tenant and authorized occupant; personal tools do not require workspace membership. |
| Workspace and property | Workspace owner/billing administrator, landlord, co-owner, property manager, authorized approver and scoped financial/operational staff. Select workspace, property and tenancy explicitly. |
| Maintenance | Assigned maintenance worker/vendor when enabled; only the assigned work and permitted evidence. |
| Service provider | Provider-business participants use the dedicated provider portal; personal service requests are not automatically visible to landlords. |
| Police authority | Separately verified authority users and assigned cases; ordinary landlord/support roles do not confer investigative access. |
| Platform | Granular commercial, support, moderation, editorial, consumer operations and AI operations permissions; temporary support access has reason, scope and expiry. |
| Conditional participants | Broker participation, approved concierge/relationship managers and other conditional providers retain their source approval and staffing gates. |

A single account can hold multiple roles in different contexts. Context switching clears incompatible data, drafts and subscriptions; reauthentication must recheck the active context. Unknown, zero and not-applicable values must remain distinguishable.


## Frontend platform and presentation requirements

### Frontend

- Next.js with App Router.
- TypeScript.
- React using appropriate Server and Client Components.
- Tailwind CSS.
- shadcn/ui and accessible Radix-based components.
- React Hook Form and Zod.
- TanStack Query where justified.
- Responsive, mobile-first UI.
- Accessible component system targeting WCAG 2.2 AA.
- OpenAPI-generated TypeScript contracts where appropriate.

### User experience

Build a modern, polished, intuitive SaaS-style web application suitable for Indian landlords, managers, and tenants.

### Visual Design

Establish a consistent design system with:

- Typography scale.
- Color tokens and semantic status colors.
- Spacing and layout scales.
- Border radius and elevation conventions.
- Accessible interaction and focus states.
- Consistent form controls and data tables.
- Responsive cards, dialogs, drawers, and navigation.
- Appropriate empty states and skeleton loaders.
- Consistent icons and illustrations where useful.
- Clear visual hierarchy and progressive disclosure.

Use shadcn/ui as a foundation, customizing it into a coherent LMT product design rather than leaving default components visually inconsistent.

Avoid unnecessary animations, excessive decorative elements, and visual clutter.

### Responsive Layout

Implement responsive layouts for:

- Mobile phones.
- Tablets.
- Laptops.
- Desktop monitors.

For desktop landlord/manager experiences, prioritize efficient portfolio and operational workflows.

For mobile browsers, prioritize clear actions, readable forms, touch-friendly controls, and compact navigation.

A responsive web application is the required baseline. Installable PWA functionality is conditional on an explicit product decision.

### Accessibility

Target WCAG 2.2 AA.

Include:

- Keyboard navigation.
- Visible focus indicators.
- Accessible labels.
- Screen-reader-friendly status announcements.
- Sufficient contrast.
- Error descriptions linked to fields.
- Accessible dialogs and menus.
- Non-color-only status indicators.
- Responsive text resizing.
- Touch targets suitable for mobile devices.

### Required UX States

Every important page and operation must handle:

- Initial loading.
- Empty results.
- Successful completion.
- Validation errors.
- Authorization failure.
- Expired session.
- Network failure.
- Server failure.
- Conflicting edits.
- Pending asynchronous operations.
- Retry and recovery.
- Unsaved changes.
- Partial completion.

Do not display fake financial, payment, occupancy, or availability data as actual records.

## Cross-cutting frontend behavior

- Resolve account, workspace, property, tenancy and special-role context explicitly. Prospective tenants may have account-only access; provider and police portals use separate scoped authority. Hide unauthorized navigation but enforce security again on the API. Switching context clears incompatible cached data, drafts and subscriptions without leaking the previous context.
- Every linked screen must show its authoritative data, primary/secondary actions, required fields, field-level constraints, current state, permitted next actions and relevant evidence. Client validation mirrors server rules for usability; server remains authoritative. Label optional fields and distinguish unknown, not applicable and zero values.
- Forms support accessible labels/help/errors, keyboard operation, mobile layout, draft recovery where appropriate, unsaved-change protection, double-submit prevention and field-error focus. Use safe server-confirmed success; do not optimistically assert payments, signatures, quotas, allocations, decisions or refunds.
- Implement loading/skeleton, genuine empty, forbidden/not found, expired session, network/offline, server failure, pending asynchronous, stale/conflicting version, partial completion and retry/recovery states. Preserve nonsensitive unsent inputs during retry; never cache sensitive documents/secrets in browser storage by default.
- Persist validated list/search/date filters in URLs where appropriate. Give stable pagination and sorting with total/matched counts under the same authorized scope. Report mismatched timestamps/coverage, rather than showing stale data as current. Charts require accessible tables and source-record drill-downs.
- For money/terms/publication/destructive actions, display the exact version/amount/recipient/scope/effective date before confirmation and show the resulting reference/status. Reauthentication and approvals follow the domain policy. Long jobs show progress, committed versus pending results and safe recovery.
- Use generated OpenAPI types where appropriate; never duplicate accounting, eligibility or transition rules exclusively in React. API responses may include `allowedActions` for usability, but these are not reusable security grants.

### Shared components and integration

Implement:

- Reusable accessible components.
- Typed API integration.
- Validated forms.
- Consistent server-error presentation.
- Optimistic UI only where safe and recoverable.
- Query invalidation after successful mutations.
- Responsive tables and mobile card layouts.
- URL-driven filters and pagination where useful.
- Route-level loading and error states.
- Role-aware navigation.
- Safe session expiry handling.
- Secure upload flows.
- Explicit payment and signing pending states.
- User confirmation for destructive and financial operations.

Use semantic HTML, accessible interaction patterns, and correct keyboard handling.

Avoid hydration inconsistencies, unnecessary client-side rendering, excessive network requests, and oversized frontend bundles.

### Validation and authoritative results

Client validation mirrors the defined server rules. Show accessible field errors, conditional requiredness and current allowed actions. Exact field lengths/ranges and unresolved schemas must remain unspecified until supplied; do not invent them. Values returned by the service remain authoritative, including amounts, eligibility, deadlines, quotas and transition guards.

Financial and destructive confirmations show exact amount, recipient, scope, version and effective date. Stale versions require refresh/review; a previous approval cannot authorize changed terms. Preserve safe nonsensitive input during recovery and prevent duplicate submission. Never treat browser redirects, streaming text or a local optimistic update as payment, signature, refund, publication or official-decision success.

### Browser integration boundary with original integration IDs

**INT-04:** Resolve identity/context through the private session response. Use the session-bound CSRF token for applicable state-changing requests, same-origin domain API URLs and the specified credentials, idempotency and version headers. After session rotation reload CSRF state. On 401 retain safe drafts and request reauthentication without automatic mutation replay; on 403/privacy-safe 404 clear inaccessible cached records. Include actor/context, resource and filters in query identity; abort incompatible in-flight requests on logout/context change. Do not persist sensitive drafts or tokens in browser storage by default. SSR/proxy behavior must preserve the user's actual authority.

**INT-05:** Treat 201 as a committed resource, 202 as accepted/pending rather than provider success, and 200/204 according to the defined result. Handle 400 malformed request, 422 field errors, 401 session expiry, 403 forbidden, privacy-safe 404, 409 business conflict, 412 stale version, 428 missing precondition, 429 rate limiting and retryable 503 dependency unavailability distinctly. Display stable business reasons including `QUOTA_EXCEEDED`, `APPROVAL_STALE`, `ALLOCATION_CONFLICT`, `PROVIDER_OUTCOME_UNKNOWN`, `SIGNATURE_EVIDENCE_PENDING` and `VERIFICATION_REQUIRED`. Respect retry guidance. Poll with bounded backoff; a timeout must not create another payment/publication operation. Money uses decimal strings plus currency, with local dates distinct from timestamped instants. Pagination/count/as-of semantics must match the response; page length is not an invented total.

**INT-06 and INT-08:** Bind screens to the canonical typed operations and generated contracts; distinguish configured mock/sandbox behavior from real integration. The representative API snippets in the source are not a complete implemented API. This FRS does not prescribe server implementation.

### Localization

Present INR and Asia/Kolkata business time; distinguish date-only business dates from instants. Support Indian phone, address and PIN fields, relevant identity fields and regional-language readiness. State/local rules are configurable; do not infer a uniform Indian tenancy, tax, registration or verification policy.


## Workspace entitlement baseline

### Plans

Implement five subscription tiers:

1. Free
2. Silver
3. Gold
4. Platinum
5. Diamond

The frontend reads the configured catalogue and provides authorized catalogue administration.

Do not hardcode plan restrictions throughout frontend or backend business logic.

### Proposed Subscription Feature Matrix

These are initial defaults, subject to commercial approval.

| Feature | Free | Silver | Gold | Platinum | Diamond |
|---|---|---|---|---|---|
| Rentable units | 5 | 50 | 200 | 1,000 | Custom |
| Properties | Within unit quota | Multiple | Multiple | Multiple | Multiple |
| Core rental lifecycle | Full | Full | Full | Full | Full |
| Rent and utility billing | Yes | Yes | Yes | Yes | Yes |
| Payment and deposit records | Yes | Yes | Yes | Yes | Yes |
| Tenant self-service portal | Yes | Yes | Yes | Yes | Yes |
| Agreements | Baseline e-sign + permitted manual fallback | Baseline e-sign + enhanced workflow | Advanced e-sign workflow | Advanced e-sign workflow | Custom workflow |
| Listing creation | Manual | Automatic | Automatic | Advanced | Advanced |
| Tenant helpdesk | Basic | Full | Full | Advanced | Enterprise |
| Email notifications | Basic | Included quota | Higher quota | Higher quota | Custom |
| WhatsApp notifications | No | Included quota | Higher quota | Higher quota | Custom |
| Managers | 1 | 5 | 20 | 100 | Custom |
| Analytics | Basic | Standard | Advanced | Portfolio BI | Enterprise BI |
| AI chatbot | No | No | Limited | Advanced | Custom |
| Integrations/API | No | No | Limited | Advanced | Enterprise |
| White-label branding | No | No | No | Optional | Optional |
| Support | Self-service | Standard | Priority | Enhanced | Contractual |

### Plan Principles

**Free Plan**

Designed for small landlords managing up to five rentable units.

Must include the complete essential tenancy lifecycle:

- Property and unit management.
- Manual listings.
- Inquiries and visits.
- Tenant onboarding.
- Agreement drafts and document storage.
- Move-in and move-out.
- Rent and utility billing.
- Manual or supported digital payment recording.
- Deposit tracking.
- Basic maintenance ticket handling.
- In-app notifications.
- Basic dashboards and reports.
- Historical records and document access.

Free users must not be forced to upgrade to correct a financial record, complete move-out, issue required receipts, or obtain necessary historical documentation.

**Silver Plan**

Designed for landlords and small property management businesses managing up to 50 units.

Add:

- Automated advertisement publishing.
- Full tenant helpdesk.
- WhatsApp notifications.
- Transactional email notifications.
- Expanded manager access.
- Automated rent reminders.
- Standard analytics.
- Higher document storage and communication allowances.
- Scheduled operational automation.

**Gold Plan**

Designed for growing operators managing up to 200 units.

Add:

- Advanced financial dashboards.
- Automated payment follow-ups.
- Bulk property and unit import.
- Advanced maintenance assignment.
- SLA tracking.
- Extended notification automation.
- Limited AI helpdesk assistance.
- Advanced roles and approval workflows.
- Enhanced reporting and exports.

**Platinum Plan**

Designed for professional property management organizations managing up to 1,000 units.

Add:

- Multi-property operational automation.
- Advanced approval policies.
- Portfolio-level analytics and BI.
- Advanced AI assistance.
- Higher communication quotas.
- Integration APIs.
- Custom reporting.
- Enhanced access management.
- Operational audit and monitoring capabilities.

**Diamond Plan**

Designed for large real-estate operators and enterprise customers.

Support configurable enterprise contracts with:

- Custom capacity.
- Custom entitlements.
- Negotiated service levels.
- Advanced reporting.
- Enterprise integrations.
- Optional SSO.
- Custom branding.
- Higher or contract-defined API limits.
- Enterprise support.
- Optional dedicated infrastructure or data isolation where justified.

Do not assume Diamond automatically means physically unlimited resources.

All Diamond limits must have enforceable operational safeguards even when commercially represented as unlimited.

### Commercial Configuration

Create configurable subscription pricing rather than inventing fixed prices.

Support:

- Monthly subscriptions.
- Annual subscriptions.
- Free subscriptions.
- Optional trial periods.
- Promotional discounts.
- Plan upgrades.
- Scheduled downgrades.
- Cancellations.
- Subscription renewals.
- Failed payments.
- Billing grace periods.
- Refunds and credits.
- Add-on purchases.
- Taxes and invoice adjustments based on verified applicable requirements.

Plan prices, effective dates, taxes, discounts, allowances, and add-on rates must come from versioned commercial configuration.

Historical invoices must retain the commercial terms effective when issued.

## Personal membership entitlement baseline

### Product Boundaries and Consumer Plan Matrix

Support prospective tenants, applicants, and active rental tenants. A person may buy a membership before having a tenancy or workspace membership. Use a distinct consumer catalog with initial plan codes `TENANT_FREE` and `TENANT_PREMIUM`; names, prices, allowances, and commercial terms are versioned configuration. Do not reuse landlord plan codes or invent prices.

| Capability | Tenant Free | Tenant Premium |
|---|---|---|
| Public search, listing details, photos and floor plans | Included where published | Included |
| Published self-guided 360°/3D tours | Included where available | Included; saved tour notes and comparison tools |
| In-person appointment request, rescheduling and cancellation | Included | Included; waitlists and scheduling assistance |
| Standard scheduled video viewing | Available when host offers it | Included plus concierge coordination within allowance |
| Favorites and saved searches | Basic configurable allowance | Expanded allowance and configurable alerts |
| Listing comparison | Basic side-by-side details | Expanded shortlist, personal notes and total-cost comparisons |
| Applications, agreements, rent payments, receipts, disputes and notices | Essential access included | Essential access included |
| Maintenance reporting and emergency guidance | Included | Included; platform support benefits do not override emergency triage |
| Personal document reuse | Standard tenancy-specific upload | Private reusable application profile and revocable document sharing |
| Property recommendations | Structured preference matching and factual explanations | Natural-language search, richer grounded comparison and preference assistance |
| Rental search assistant | Standard filters/helpdesk | Metered AI assistance and human handoff |
| Move-in/move-out organizer | Existing tenancy workflow | Personal checklists, reminders and concierge requests |
| Platform support | Standard | Priority platform support under published operating hours and targets |
| Private flat/house sale listings | No buyer access | Only with independently verified rental residence and active paid membership |
| Nearby business directory | Existing basic discovery/contact | Basic directory plus Premium nearby-service ads, business contacts and tracked requests |
| Partner offers | Public offers where available | Eligible member offers with explicit terms, subject to approved partners |
| Personal rental expense insights | Receipts/statements | Categorization, budgeting and exports using authorized records |

Published tours and ordinary appointment booking are core discovery features, not mandatory payment barriers. Premium must provide additional convenience. Do not paywall rent receipts, existing agreements, dispute submission, maintenance reporting, notices, account privacy controls, or essential tenancy records.

Premium membership does not guarantee property availability, an appointment, application acceptance, a rent discount, maintenance completion, tenancy approval, or preferential legal treatment. It does not grant landlord administration or another occupant's data. Premium members do not displace confirmed appointments or bypass host availability.

## Route inventory and navigation

Routes below are source-specified, not an invented final routing design. The source's illustrative App Router tree is supplemented by FS-01 through FS-36. Route groups do not form URL segments. Named tabs/screens without an explicit path retain that designation.

Implement separate navigation for marketing, SaaS subscription management, workspace operations, rental tenants, and platform administrators.

Suggested structure:

```text
src/app/
  (marketing)/
    page.tsx
    pricing/
    features/
    contact/

  (auth)/
    login/
    register/
    verify-otp/

  (onboarding)/
    create-workspace/
    choose-plan/
    setup/

  (workspace)/
    workspace/
      dashboard/
      properties/
      units/
      listings/
      visits/
      applications/
      tenants/
      tenancies/
      agreements/
      billing/
        subscription/
        usage/
        invoices/
        payment-methods/
      rental-finance/
        rent/
        utilities/
        payments/
        deposits/
      maintenance/
      notifications/
      reports/
      settings/
        members/
        permissions/
        integrations/
        organization/

  (rental-tenant)/
    tenant/
      dashboard/
      payments/
      documents/
      maintenance/
      notices/
      services/

  (platform-admin)/
    admin/
      dashboard/
      workspaces/
      plans/
      subscriptions/
      invoices/
      usage/
      support/
      audit/

  api/
```

The exact URL organization may be refined to avoid namespace conflicts.

The SaaS subscription billing interface and landlord rental billing interface must be visibly distinguishable.

### Canonical route reconciliation

- Canonical workspace helpdesk is `/workspace/helpdesk`; legacy `/workspace/maintenance` links may redirect to the same module. `/tenant/helpdesk` and `/tenant/maintenance` share the same underlying cases and permissions.
- `/tenant/bills` is the invoice/obligation view; `/tenant/payments` is the transaction/receipt view. `/workspace/billing` is SaaS billing; `/workspace/rental-finance` is rental accounting. They never share invoice purpose or mutable balances.
- `/tenant/membership` is personal consumer billing. `/provider` and `/police` are dedicated scoped portals; they are not landlord workspace roles. `/admin` is platform administration with explicit granular privileges.
- New routes in these contracts extend the earlier illustrative App Router tree, which is not an exhaustive screen list. Reuse shared components/domain clients while preserving different safe projections.

### Additional consumer and operations surfaces

Add public listing tour/booking routes, `/tenant/appointments`, `/tenant/membership`, `/tenant/membership/billing`, `/tenant/shortlists`, `/tenant/vault`, `/tenant/insights`, `/tenant/concierge`, `/tenant/move-planner`, `/workspace/.../tours`, and `/admin/consumer-plans`, `/admin/consumer-subscriptions`, `/admin/concierge`. Prospective users can access these consumer routes without an active tenancy; navigation must not misrepresent them as occupants.

### Frontend Design Requirements

Implement:

- Modern SaaS marketing website.
- Responsive pricing cards.
- Subscription selection wizard.
- Clear unit-limit indicators.
- Usage progress bars.
- Upgrade/downgrade dialogs.
- Subscription payment states.
- Plan-specific feature affordances.
- Role-appropriate navigation.
- Workspace switching.
- Billing alerts.
- Accessible forms and dialogs.
- Mobile-friendly dashboards.
- Empty, loading, offline, unauthorized, and error states.

Plan selection must never create a misleading impression that payment succeeded before backend confirmation.

Do not render fake financial figures as actual workspace activity.

## Shared frontend screen contracts

These contracts retain the original FS IDs. Service-state and acceptance references define what users must be shown or prevented from doing; they do not assign backend implementation to the frontend.

### FS-01 — Identity, account recovery, and context selection

**Actors and authorization:** Account owner; no workspace membership required for registration or personal profile.

**Frontend screens, fields, validation and actions:** Routes `/login`, `/register`, `/verify-otp`, `/account/profile`, `/account/security`. Inputs: contact channel/value, OTP or provider authorization result, display name, locale, consent versions. Show verified contacts, sessions and available authorized contexts. Actions: request/verify challenge, link verified identity, revoke session, recover account, change contact with reauthentication. Validate phone/email formats, challenge expiry and attempt limits; never display account existence in unauthenticated errors.

**Visible states, notifications and recovery dependencies:** Challenge delivery and contact/security-change notifications; challenge verification commits atomically, limits attempts and consumes single-use proof. Logout/revocation invalidates cached authority. Provider outages return retryable status without issuing a fake session.

**Acceptance outcomes:** UI invalid/expired challenge and recovery journeys; API replay, account-link takeover and revoked-session rejection; integration tests for unique identity and attempt counters.

### FS-02 — Workspace registration, membership, and setup

**Actors and authorization:** Authenticated account and authorized workspace administrator; invite acceptance is account-scoped until membership is established.

**Frontend screens, fields, validation and actions:** Routes `/create-workspace`, `/choose-plan`, `/setup`, `/workspace/settings/members`. Inputs: organization/name/type, city/state, selected plan version, required acknowledgments and invited contact/scope. Show setup completion, membership state and invite expiry; resume persisted steps. Active workspace switching clears previous query caches.

**Visible states, notifications and recovery dependencies:** Provisioning status, invitations and member-revocation events. Failure exposes resumable provisioning state; duplicate request returns the same workspace rather than another grant.

**Acceptance outcomes:** Complete registration/setup E2E; cross-workspace mutations, duplicate provisioning, expired/forwarded invites and safe switch tests.

### FS-03 — Versioned plans and public commercial catalog

**Actors and authorization:** Public read of active offerings; platform commercial administrators control draft/active versions.

**Frontend screens, fields, validation and actions:** Routes `/pricing`, `/tenant/membership`, `/admin/plans`, `/admin/consumer-plans`. Show billing intervals, currency, prices/tax disclosures, effective dates, quotas, feature definitions and conditional availability. Admin edits versioned draft terms, previews differences and schedules publication; subscriber UI never displays internal contract-only prices.

**Visible states, notifications and recovery dependencies:** Catalog activation audit and cache invalidation; concurrent activation guarded by version; historical terms never silently rewritten.

**Acceptance outcomes:** Pricing UI compares correct audience/interval; unpublished/private price exclusion, grandfathered contracts and concurrent version activation tests.

### FS-04 — Workspace subscriptions and SaaS billing

**Actors and authorization:** Workspace billing administrator; provider callbacks use integration authentication, not user sessions.

**Frontend screens, fields, validation and actions:** Routes `/workspace/billing/subscription`, `/workspace/billing/invoices`, `/workspace/billing/payment-methods`. Show current contract, next renewal, add-ons, priced upgrade/downgrade effects, tax/proration, cancellation effective date, grace/recovery and invoice/payment status. Require confirmation of an unexpired quote; pending checkout is not paid.

**Visible states, notifications and recovery dependencies:** Confirmed settlement activates grants once; uncertain payment reconciles later. Scheduled downgrade preserves inventory, cancellation preserves records, and reversal/activation failure has a durable recovery task.

**Acceptance outcomes:** UI full change/cancel/recovery flow; duplicate/out-of-order callbacks, concurrent upgrades, above-limit downgrade, expired quotes and rent-versus-SaaS isolation tests.

### FS-05 — Entitlements, reservations, usage, and limits

**Actors and authorization:** Authenticated resource actor plus domain permission; grants do not independently confer record authority.

**Frontend screens, fields, validation and actions:** Routes `/workspace/billing/usage`, billing settings and contextual limit indicators. Show period boundaries, quota/count definition, committed/reserved usage, remaining allowance, feature reason and authorized upgrade action. Keep necessary corrections/history available; explain blocked discretionary operations.

**Visible states, notifications and recovery dependencies:** Recalculate/invalidate after subscription/grant changes; reconcile partial imports and expired reservations; dead jobs cannot strand allowances indefinitely.

**Acceptance outcomes:** Limit UI and permission-specific upgrade UX; last-slot concurrency, duplicate metering, archive/restore bypass, partial reservation recovery and grant revocation tests.

### FS-06 — Platform administration and scoped support

**Actors and authorization:** Explicit platform roles; separate commercial, support, moderation and authority administration permissions.

**Frontend screens, fields, validation and actions:** Routes `/admin/workspaces`, `/admin/subscriptions`, `/admin/support`, `/admin/audit`, consumer administration. Search with scoped fields; display status, commercial activity, allowed adjustments and support access expiry. Require reason and any second approval before sensitive adjustments or temporary access.

**Visible states, notifications and recovery dependencies:** Grant expiry/revocation is checked on access/download/job execution; privileged changes notify authorized recipients where appropriate. Failed adjustments remain pending, never represented as settled money.

**Acceptance outcomes:** Admin restricted-field UI; support expiry, no police/tenant-document access by default, audit immutability and adjustment authority tests.

### FS-07 — Property hierarchy, readiness, and rentable capacity

**Actors and authorization:** Property-scoped owner/manager; read model distinguishes legal ownership from workspace billing ownership.

**Frontend screens, fields, validation and actions:** Routes `/workspace/properties`, `/workspace/properties/{id}`, `/workspace/units/{id}` and readiness tabs. Forms: address, building/floor, type, area/unit, capacity, unit identifier, amenity/charge references, operational state. Show history, readiness blockers and availability; archive action previews active obligations.

**Visible states, notifications and recovery dependencies:** Readiness/availability events update listing/search projections; archival fails with actionable blockers rather than deleting tenancies.

**Acceptance outcomes:** Property creation/readiness E2E; duplicate identifier, shared-bed capacity, cross-workspace parent and occupied-archive rejection tests.

### FS-08 — Ownership, delegation, tasks, policies, and approval decisions

**Actors and authorization:** Authorized owners/approvers; managers only within current category, amount and property scope.

**Frontend screens, fields, validation and actions:** Routes `/workspace/settings/permissions`, property ownership/policy tabs, `/workspace/tasks`, `/workspace/approvals`. Inputs: authority type, effective dates, limits, policy version, request reason/evidence. Show requester/approver, before-after values, missing approvers, task blockers and expiry; accept/decline invitations and request clarification.

**Visible states, notifications and recovery dependencies:** Assignment/revocation/clarification/decision events notify responsible parties. Stale/materially changed requests return conflict and request renewed approval; handover preserves historical actors.

**Acceptance outcomes:** Inbox and permission-editor E2E; conflicting co-owners, revoked manager during edit, stale approval amount, self-approval policy and task reassignment tests.

### FS-09 — Amenity catalog, inventory movements, and handover snapshots

**Actors and authorization:** Scoped property managers/owners; tenants see authorized supplied-item records, workers only assigned assets.

**Frontend screens, fields, validation and actions:** Routes `/workspace/inventory`, unit inventory and tenant handover tabs. Inputs: item category, explicit presence/absence/unknown, quantity/condition, owner/shared scope, serial/asset tag where relevant, movement source/destination and evidence. Matrix supports all/any item filters and usable-quantity thresholds.

**Visible states, notifications and recovery dependencies:** Repair/transfer/handover events link to maintenance and listings; missing/broken items create review tasks, not deductions. Partial transfers retain accurate source/destination state.

**Acceptance outcomes:** Has/absent/unknown matrix and handover review; concurrent transfer, negative stock, shared asset deduplication, tenant-owned privacy and inventory-history tests.

### FS-10 — Rental listing preparation, publication, search, and moderation

**Actors and authorization:** Public safe projection; authorized advertiser edits; independent platform listing moderators verify/report.

**Frontend screens, fields, validation and actions:** Routes `/listings`, `/listings/{id}`, `/workspace/listings`, `/admin/listing-reviews`. Forms include publication-safe property attributes, price components, dates, media, advertiser class and authority evidence. Search exposes multi-locality, map/radius where configured, normalized filters and stable sorting; review screens show exact badge meaning.

**Visible states, notifications and recovery dependencies:** Approved publish/withdraw/occupancy events refresh search; jobs reconcile stale indexes, expired promotions and freshness. Duplicate/fraud signals create review cases rather than automatic criminal claims.

**Acceptance outcomes:** Owner-publish/search E2E; draft/private-sale exclusion, stale-index recheck, duplicate candidate review, truthfully scoped badges and withdrawn listing cache tests.

### FS-11 — Inquiries, contact grants, messaging, negotiation, and shortlists

**Actors and authorization:** Authenticated seeker and authorized advertiser; account-scoped private notes and explicit share grants.

**Frontend screens, fields, validation and actions:** Routes `/tenant/inquiries`, `/workspace/leads`, `/tenant/shortlists`, listing contact/offer dialogs. Show lead stage, offer version/expiry, disclosed contact allowance, message audience and consent. Inputs: message, move preferences, terms/counteroffer, share recipient/scope; support blocking/reporting and post-visit feedback.

**Visible states, notifications and recovery dependencies:** Inquiry/offer/alert delivery; failed reveal reconciles reservation. An accepted offer may create an explicit hold but cannot execute a lease or payment. Revoked shares/blocks invalidate future access.

**Acceptance outcomes:** Messaging/offer/shortlist E2E; concurrent contact unlock, stale offer accept, gated-content sharing, spam retries and actual-event conversion tests.

### FS-12 — Visit scheduling, rescheduling, and waitlists

**Actors and authorization:** Seeker, assigned host and approved concierge; appointment access does not confer unit access rights.

**Frontend screens, fields, validation and actions:** Routes `/listings/{id}/appointments`, `/tenant/appointments`, `/workspace/visits`. Show timezone, open slots, duration/buffers, confirmation policy and participant-specific instructions. Inputs: mode, window, party size, accommodations, reschedule reason and entry consent where applicable.

**Visible states, notifications and recovery dependencies:** Confirmation/reminder/calendar events; failed delivery does not cancel appointment. Slot offers expire safely; unavailable/withdrawn units trigger tracked cancellation/reconfirmation.

**Acceptance outcomes:** Booking/reschedule E2E; last-slot race, hold expiry, old-slot preservation, declined entry and private video-link authorization tests.

### FS-13 — Rental applications and existing-tenant identity onboarding

**Actors and authorization:** Applicant owns draft; scoped owner/manager reviews; co-applicant documents use individual consent.

**Frontend screens, fields, validation and actions:** Routes `/apply/{listingId}`, `/tenant/applications`, `/workspace/applications`, tenant-invitation acceptance. Fields: desired move date, household, required documents/consents and proposed terms. Show pending clarifications, holds, verification scope and next action; save/resume/withdraw without repeated submissions.

**Visible states, notifications and recovery dependencies:** Submission/clarification/decision notifications; concurrent approvals cannot overbook. Duplicate-account or imported-data conflicts create review tasks and preserve provenance.

**Acceptance outcomes:** Save/resume/clarification E2E; forwarded invitation, co-applicant privacy, conflicting approvals and withdrawal-versus-approval race tests.

### FS-14 — Possession, active tenancy, renewals, inspections, and transfers

**Actors and authorization:** Scoped operational owner/manager and tenant participants; preserve historic authorized access.

**Frontend screens, fields, validation and actions:** Routes `/workspace/rented-units`, `/workspace/tenancies/{id}`, `/tenant/tenancies/{id}`. Display distinct allocation/possession/agreement/financial states; collect actual handover dates, inventory/readings/keys, occupant changes, renewal terms, inspection consent and transfer source/destination. Show blocked prerequisites and disputed acknowledgments.

**Visible states, notifications and recovery dependencies:** Move-in triggers signing obligation; handover triggers readiness; inspection/transfer/renewal events update tasks. Correction of actual facts requires reason/version; notification failure cannot erase possession.

**Acceptance outcomes:** Existing/new tenant and transfer E2E; bed/co-tenant changes, delayed exit, temporary relocation, overlapping allocations and historic-access tests.

### FS-15 — Agreement versions, signers, ten-day deadlines, and official documents

**Actors and authorization:** Authorized signatories and scoped preparer; official providers verified independently.

**Frontend screens, fields, validation and actions:** Routes `/workspace/agreements/{id}`, `/tenant/documents`, signature review. Inputs: template/jurisdiction, accepted terms, signer identities/authority, correction/exception reason. Show document version, required signatures, exact deadline and separate stamping/registration/artifact status; permit declined/correction paths.

**Visible states, notifications and recovery dependencies:** Days 3/7/9 reminders and overdue escalation; signed provider outcome/evidence retrieval reconcile separately. Provider outage cannot invent execution or reset deadline; prior legal prerequisites take precedence.

**Acceptance outcomes:** End-to-end multi-party signing with provider sandbox; duplicate callbacks, ten-day boundary, partial-signature edit, missing artifact and manual-alternative labeling tests.

### FS-16 — Charge configuration, utility calculation, membership, and invoice issue

**Actors and authorization:** Authorized billing preparer/approver; tenants read obligations and dispute under own financial scope.

**Frontend screens, fields, validation and actions:** Routes `/workspace/rental-finance/rent`, `/utilities`, `/memberships`, `/invoices`, `/tenant/bills`. Forms: schedule, charge basis/rate/period, meter evidence, allocation rule, facility enrollment, billing preview and reasoned adjustment. Show separate charge categories, estimates, payees, preview totals and immutable issued lines.

**Visible states, notifications and recovery dependencies:** Recurring billing operations; failed batch items resumable; final readings true up estimates through adjustments. Changes after approval return stale-preview conflict; delivery failure never duplicates issue.

**Acceptance outcomes:** Full category invoice E2E; shared-meter residual rounding, no-enrollment fee exclusion, concurrency/retry, corrected reading, multiple payees and immutable invoice tests.

### FS-17 — Rental payments, receipts, payees, reconciliation, and expenses

**Actors and authorization:** Payer under tenancy obligation, verified payee administrator and authorized financial staff; provider event authentication.

**Frontend screens, fields, validation and actions:** Routes `/tenant/payments`, `/workspace/rental-finance/payments`, `/workspace/expenses`, owner statements. Show itemized payment/fees/allocations, recipient, pending versus verified status, receipt, payout status, approved/incurred/paid expenses and statement basis. Manual report/expense forms collect reference, amount/date, source evidence and approval.

**Visible states, notifications and recovery dependencies:** Provider pending/refund/payout reconciliation; manual claims require authorized verification. Expense approval alone does not mean paid or tenant-liable. Failed artifact delivery is recoverable without recharging.

**Acceptance outcomes:** Mixed-category/partial payment and expense E2E; duplicate/late callbacks, old arrears/advance allocation, payee change, delayed payout and cross-purpose isolation tests.

### FS-18 — Notice, lock-in, offboarding, deposit settlement, and refund

**Actors and authorization:** Affected tenant initiates; authorized owner/manager reviews; financial approver posts adjustments.

**Frontend screens, fields, validation and actions:** Routes `/tenant/notices`, `/workspace/tenancies/{id}/offboarding`, `/workspace/rental-finance/deposits`. Inputs: requested departure, affected occupants, final readings, clause/evidence, line-item acceptance/dispute and refund recipient. Show accepted lock-in terms, provisional/final dues, deposit usage/holdback, approvals and distinct handover/closure milestones.

**Visible states, notifications and recovery dependencies:** Notice/reminder/approval/refund events; actual handover releases eligible capacity independently of money. Date changes recompute proposal without overwriting posted history; expired refunds/holdbacks surface tasks.

**Acceptance outcomes:** Tenant exit through final statement E2E; lock-in boundary, unlawful automatic retention prevention, no double deposit consumption, disputed deductions, pending utility and refund retry tests.

### FS-19 — Helpdesk, incidents, maintenance, and announcements

**Actors and authorization:** Tenant/occupant subject, assigned manager/worker and authorized owner; sensitive complaints use restricted alternate path.

**Frontend screens, fields, validation and actions:** Routes `/tenant/helpdesk`, `/workspace/helpdesk`, `/workspace/announcements`. Fields: category, asset/unit, severity, description/evidence, message audience, assignment, estimate, entry window, resolution and feedback. Show queue age, blockers, current actor, shared history and distinct internal notes; allow reopen/escalation.

**Visible states, notifications and recovery dependencies:** Durable assignment/visit/resolution notifications; tenant confirmation differs from administrative closure. Owner approval differs from entry permission; inactive managers trigger fallback task routing.

**Acceptance outcomes:** Raise-to-repair-to-reopen E2E; private-note exclusion, manager complaint routing, above-limit work, entry denial, former tenant access and notification retry tests.

### FS-20 — Operational dashboards, charts, reporting, and exports

**Actors and authorization:** Scoped workspace financial/operational viewers; each metric has its own permitted fields and source scope.

**Frontend screens, fields, validation and actions:** Routes `/workspace/dashboard`, `/reports`, `/owner-statements`. Shared from/to presets, planning horizon, property/manager/category filters, accessible charts/tables and drill-down. Display metric definition/version, denominator, date basis, generatedAt/asOf, unknown coverage and pending data; retain filters through navigation/export.

**Visible states, notifications and recovery dependencies:** Data freshness/rebuild status and permission-aware cache invalidation; export rechecks permission at execution/download. Unsupported metric or missing source is unavailable rather than zero.

**Acceptance outcomes:** Exact known-fixture chart/card/drilldown/export reconciliation; month boundary, co-tenant deduplication, refunds, unknown history, stale responses and cross-scope aggregate tests.

### FS-21 — Secure documents, sharing, consent, retention, and privacy requests

**Actors and authorization:** Subject/authorized resource participant; purpose-specific grants and limited support authority.

**Frontend screens, fields, validation and actions:** Routes `/tenant/documents`, `/account/privacy`, permitted workspace document tabs. Show versions, scan/processing status, grants/expiry, access context and retention limitations. Inputs: classified file metadata, sharing recipient/purpose/duration, export/deletion request and explicit acknowledgments. Private IDs never enter query logs.

**Visible states, notifications and recovery dependencies:** Scan/export/delete jobs track operation progress and justified retention/hold exceptions; revoke grants on authoritative policy changes. Never promise recall of downloaded files or deletion from an authority already lawfully receiving evidence.

**Acceptance outcomes:** Upload/retry/view/revoke/privacy-request E2E; MIME spoofing, cross-record URL access, expired links, quarantine, retention holds and export field redaction tests.

### FS-22 — Notifications, preferences, templates, and delivery recovery

**Actors and authorization:** Account owner for preferences; authorized workspace rule admin; provider callbacks verified separately.

**Frontend screens, fields, validation and actions:** Routes `/tenant/notifications`, `/workspace/notifications`, notification settings/templates for authorized admins. Show event summary, read status and delivery where relevant; edit channel/category/quiet-hour preferences and approved rules. Sensitive details remain behind authorized deep links.

**Visible states, notifications and recovery dependencies:** Delivery has bounded retry and operator recovery; essential transactions commit even during messaging outage. Invalid consent/template/quota creates explicit failure and supported in-app fallback.

**Acceptance outcomes:** Notification preference/read/deep-link E2E; deduplication, consent expiry, wrong-recipient leakage, failed-provider recovery and workspace versus consumer metering tests.

### FS-23 — Immersive tours and tour publication

**Actors and authorization:** Authorized property publisher; public rental viewers or eligible private-sale viewers under separate projection.

**Frontend screens, fields, validation and actions:** Routes unit tour editor, `/listings/{id}/tour`, and personal tour notes. Inputs: scene uploads, labels, hotspots, floor-plan reference, capture date and approved publication scope. Show processing/version errors, preview and accessible static fallback; notes private by default.

**Visible states, notifications and recovery dependencies:** Media processing retries/cleanup; publication updates search/listing projections; privacy report triggers moderation. Failed replacement leaves valid published version or disclosed unavailable state.

**Acceptance outcomes:** Author/view/navigate/fallback E2E; malicious embed, partial upload, private-note access, archived-media publication and gated-sale tour tests.

### FS-24 — Personal consumer membership and billing

**Actors and authorization:** Account owner independent of workspace/tenancy membership; restricted consumer billing support.

**Frontend screens, fields, validation and actions:** Routes `/tenant/membership`, `/tenant/membership/billing`, `/admin/consumer-subscriptions`. Show personal plan/version, paid-through, actual benefits, usage and cancellation/refund state. Purchase quote confirms price/terms; separate landlord billing from consumer billing visually.

**Visible states, notifications and recovery dependencies:** Consumer reconciliation/renewal/grace and entitlement invalidation; workspace switching cannot change personal membership. Expiry preserves authorized past records/ongoing request support.

**Acceptance outcomes:** Purchase/cancel/recovery E2E without workspace membership; duplicate callbacks, failed activation, paid-through boundaries and rent/SaaS separation tests.

### FS-25 — Personal premium tools, AI, vault, move planner, and concierge

**Actors and authorization:** Account-scoped grants for personal tools; workspace-funded helpdesk AI uses the workspace entitlement and permitted tenancy records, while personal Premium AI uses consumer grants. Never require both paid plans for the same funded capability. AI and human assistance have narrower resource/tool scope than account administration.

**Frontend screens, fields, validation and actions:** Routes `/tenant/shortlists`, `/tenant/vault`, `/tenant/insights`, `/tenant/concierge`, `/tenant/move-planner`. Forms: preferences, saved notes, selected document grants, planner tasks, assistance request. Show provider availability, usage, pending confirmed actions and service hours; maintain non-AI fallback.

**Visible states, notifications and recovery dependencies:** Alerts/concierge assignment and metered AI jobs; provider outage/quota retains basic workflow. Scope/grant revocation invalidates future retrieval, with disclosed lawful submitted-copy retention.

**Acceptance outcomes:** Vault share/revoke and assistance E2E; prompt injection, unconfirmed mutation, cross-account insight, unavailable staffing and quota-exhaustion fallback tests.

### FS-26 — Nearby providers, premium service ads, requests, quotes, and relocation

**Actors and authorization:** Public directory projection; Premium ad/request actors and provider-business participants; no landlord automatic visibility.

**Frontend screens, fields, validation and actions:** Routes `/tenant/services`, `/tenant/services/requests`, `/provider`, `/admin/service-reviews`. Inputs: locality/optional GPS, category, request details, selected shared contact/address, preferred windows, quote and response. Show verified-contact meaning, service area, estimate/exclusions and consent preview; phone click is not delivery.

**Visible states, notifications and recovery dependencies:** Delivery/clarification/schedule/completion events; request-only versus accepted scheduling remains explicit. Membership expiry restricts new ads/actions while retaining own case history/cancellation support.

**Acceptance outcomes:** Nearby request/provider quote E2E; GPS denial, out-of-area, duplicate request, exact-address consent, quote changes, provider suspension and expired membership continuity tests.

### FS-27 — Restricted property-sale marketplace

**Actors and authorization:** Seller authority for management; buyer needs verified qualifying residence AND active paid personal Premium.

**Frontend screens, fields, validation and actions:** Routes `/workspace/sales`, `/tenant/property-sales`, `/admin/sale-reviews`. Seller forms capture asset/scope, ownership evidence, price/disclosures and occupancy. Buyer receives gated search/media/inquiries, contact consent and viewing request; ineligible viewers receive no listing-specific teasers.

**Visible states, notifications and recovery dependencies:** Publication/withdrawal/membership eligibility changes invalidate discovery/cache; notifications generic with gated deep link. Media provider must support privacy or be excluded.

**Acceptance outcomes:** Owner review-to-gated buyer inquiry E2E; Free/trial/unverified/paid-workspace denial, cross-workspace safe projection, SSR/CDN/media leakage and occupancy-consent tests.

### FS-28 — Police verification and authority portal

**Actors and authorization:** Subject/requester scope plus separately verified authority membership and case assignment; no general landlord/support investigative access.

**Frontend screens, fields, validation and actions:** Routes `/tenant/verifications`, workspace case panel, `/police/cases`, authority administration. Forms: jurisdiction-specific packet, notice/consent basis, correction, assignment, checklist/source reference and permitted decision reason. Show live versus assisted/sandbox mode, outcome provenance and review channel.

**Visible states, notifications and recovery dependencies:** Case routing/clarification/decision events with privacy-minimized notifications. Source outage produces incomplete review; corrected result supersedes, never erases, previous outcome. Approved authority participation gates live operation.

**Acceptance outcomes:** Applicant/officer case flow in labeled sandbox; no self-assigned officer, wrong jurisdiction, unassigned/revoked access, incomplete-source decision, forged upload and adverse-outcome appeal tests.

### FS-29 — Verified resident reviews, public ratings, responses, and moderation

**Actors and authorization:** Independently verified current/former resident; authorized owner response; independent platform moderation.

**Frontend screens, fields, validation and actions:** Routes `/tenant/reviews`, public property reviews, `/workspace/reviews`, `/admin/review-moderation`. Inputs: 1–5 overall stars, text, optional category stars, display identity, response/report/appeal. Show verification meaning, publication/revision state, rating counts and private eligibility evidence status.

**Visible states, notifications and recovery dependencies:** Publication/edit/removal events update rating projections; appeal/response notification excludes private rent/identity details. Management changes cannot reset rating history.

**Acceptance outcomes:** Verified review-to-response/appeal E2E; nonresident denial, repeated tenancy weighting, missing category averages, owner suppression prevention and privacy projection tests.

### FS-30 — Offboarding-to-social ads, approval, spend, and channel reconciliation

**Actors and authorization:** Authorized publication approver plus separate financial authority for paid spend; integration credentials workspace-scoped.

**Frontend screens, fields, validation and actions:** Routes `/workspace/advertising`, `/workspace/settings/integrations/social`. Show vacancy readiness, creative/version, destination accounts, organic/paid mode, approved spend, schedule, per-channel state and links. Preview/reject/change/approve exact payload; reconnect and manual-cleanup controls are visible.

**Visible states, notifications and recovery dependencies:** Physical handover creates draft only. Approval and readiness checked before dispatch; timeout outcomes reconcile before retry. Occupancy requests pause/remove with tracked confirmation/manual action, not an assumed atomic external deletion.

**Acceptance outcomes:** Draft-approval-partial-live E2E; changed media invalidates approval, organic cannot spend, expired token, unknown result, duplicate callback, race with allocation and private-data exclusion tests.

### FS-31 — Broker policy, first-party promotion, and assisted-search operations

**Actors and authorization:** Conditional broker authority; approved catalog purchasers; scoped assigned relationship manager.

**Frontend screens, fields, validation and actions:** Routes `/workspace/promotions`, conditional broker profile/authority screens and assisted-search queues. Capture broker authorization/disclosure, promotion product/listing/schedule, and assistance preferences/consent. Show active product benefits, target availability and actual staff coverage.

**Visible states, notifications and recovery dependencies:** Verified purchase activates once, stale/unavailable listing stops promotion; handoff preserves case scope. User confirmation required for assistance commitments.

**Acceptance outcomes:** Conditional broker rejection, sponsored label/filter integrity, duplicate product payment, expired authority and assisted-search privacy/confirmation tests.

### FS-32 — Imports, durable jobs, and operator recovery

**Actors and authorization:** Scoped operator for affected resource; personal/authority jobs use their own owner/scope, not arbitrary workspace access.

**Frontend screens, fields, validation and actions:** Routes `/workspace/imports`, `/workspace/operations` and context-specific job status. Upload template/file, map columns, preview per-row errors/duplicates/quota, confirm and view progress/retry report. Preview/export masks sensitive fields; operator sees actual committed/rejected rows.

**Visible states, notifications and recovery dependencies:** Job results reflect current actor/system authority; cancellation reports committed versus pending work and releases unused reservations. Dead-letter recovery requires authorized action and reason; safe restarts never duplicate financial/inventory mutations.

**Acceptance outcomes:** Partial import/resume E2E; malformed/malicious rows, quota exhaustion, revoke during execution, crash-after-commit, reservation expiry and duplicate source tests.

### FS-33 — Marketplace administration, editorial content, optional referrals, and platform metrics

**Actors and authorization:** Scoped platform moderators, editors, commercial admins; advertisers only their own performance.

**Frontend screens, fields, validation and actions:** Routes `/admin/marketplace`, `/admin/geography`, `/admin/content`, conditional referral operations and platform analytics. Forms: geography alias/version, editorial audience/schedule, fraud/rule reason, referral eligibility/reward policy. Show queue provenance, source metric definitions and case status.

**Visible states, notifications and recovery dependencies:** Publication scheduling, report/rule audit and reward reversal/reconciliation. Registration/view/contact/rental conversion events remain distinct and exclude declared test/staff/bot traffic.

**Acceptance outcomes:** Editorial review/publish E2E; gated-content leaks, metric-cohort reconciliation, source version history, double referral reward and unauthorized moderation tests.

### FS-34 — Locality insights, advanced reports, alerts, and forecasts

**Actors and authorization:** Authorized reports plus licensed/public data; conditional advanced entitlement and sufficient privacy-safe source coverage.

**Frontend screens, fields, validation and actions:** Routes locality comparison, dashboard analytics/alerts and saved scenarios. Select metric/cohort/dates, explicit scenario assumptions and alert thresholds; show sample count, provenance, cash/accrual basis, uncertainty and actual/estimated series. Accept opt-out/reset for behavioral recommendations.

**Visible states, notifications and recovery dependencies:** Projection refresh, alert dedup/cooldown and data-coverage checks. Missing data is unavailable; stale source marks forecast stale; alert follow-up still needs explicit business action authority.

**Acceptance outcomes:** Known formula/sample tests, percentage denominator boundaries, scenario no-mutation, opt-out, duplicate alerts, sparse cohort privacy and actual-versus-estimate reconciliation tests.

### FS-35 — Property and Service Recommendation Contracts

**Actors and authorization:** Seekers for public safe rental matching; personal Premium for configured AI parsing/assistance; private sale access still needs verified residence plus active paid membership, and service ads follow SERVICE-PREM. Candidate eligibility is enforced before retrieval/ranking and again before delivery/action.

**Frontend screens, fields, validation and actions:** `/listings`, `/tenant/recommendations`, comparison and services panels. Inputs: natural-language request or structured budget/location/date/type/capacity/amenities, must-have versus preference weights, optional personalization consent and feedback. Show parsed editable filters, ranked actual listings, source-linked reasons/trade-offs/unknowns, sponsored labels, no-match options and manual fallback. Do not auto-relax hard constraints or display generated quality/safety guarantees.

**Visible states, notifications and recovery dependencies:** Request states received, parsing/awaiting-filter-confirmation where needed, retrieving, ranking, ready, no-matches, stale, failed/fallback and expired. Provider operations are bounded and reconcile metering after disconnect. Feedback changes personal preferences only on authorized action, never listing facts. Recommendation creation does not send inquiries, book visits or contact providers.

**Acceptance outcomes:** Deterministic ranking baseline plus evaluation fixtures for exact constraints, cold start, sparse/unknown data, duplicate units, changed availability, private sale isolation, source-backed explanation, misleading sponsored claims, denied personalization, provider failure and cost limits. Test frontend filter confirmation and click-through to ordinary authoritative detail/booking APIs.

### FS-36 — Scoped AI Assistance, Evidence, Actions, and Operations

**Actors and authorization:** Account/workspace capability funding is explicit; each user/manager can retrieve only the data already permitted by its domain policy. AI operations admins control rollout/configuration without universal conversation access. No AI police decision, lease signature or autonomous financial approval authority.

**Frontend screens, fields, validation and actions:** Existing helpdesk/listing/report/document screens plus `/admin/ai-operations`. Inputs: supported question/task, selected authorized record IDs and user-confirmed draft/proposal. Show automated-assistance label, source/version evidence, unknowns, editable draft/diff, exact proposed action/recipient/cost if applicable, confirmation expiry and manual/human alternative. Streaming text is not proof an action succeeded.

**Visible states, notifications and recovery dependencies:** Queued/running/validated/awaiting-confirmation/executed/rejected/cancelled/failed/fallback states; quota reservations and bounded retries recorded durably. Operation cancellation does not erase provider usage already incurred. User-confirmed mutations produce the normal domain audit trail; repeated confirmations do not repeat effects. Kill switches and provider degradation leave core forms/search/reports usable.

**Acceptance outcomes:** Cross-scope prompt-injection tests, adversarial retrieved text, revoked access during generation, fabricated sources/amounts, denied tool/actions, stale proposal, idempotent confirmation, privacy-safe logs, provider timeout, double-meter prevention and model-version regression. Validate efficacy against the declared baseline and record actual evidence separately from functional implementation status.

## Detailed functional requirements

Specific business rules, original acceptance criteria and conditional qualifications are retained below. Backend-only labeled blocks have been replaced with their frontend-facing result dependencies. In mixed business narratives, authorization, reconciliation and audit references are constraints on displayed outcomes, not requirements to build server services in the browser. All linked screens also inherit the common validation, loading, empty, forbidden, pending, conflict and recovery behavior.

### SAAS-REG-001 — SaaS Customer Registration

**Source location:** attachment line 2208.

**Linked frontend screen contracts:** FS-02. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Provide a registration experience collecting:

- Full name.
- Verified mobile number.
- Email address where required.
- Landlord or organization name.
- Customer type.
- City and state.
- Consent and required policy acknowledgments.

Use progressive onboarding and clear form validation.

**Authoritative result dependency:** Show verified-contact and registration outcomes; retries must not create duplicate workspaces. The resulting account receives only its authorized workspace.

**Acceptance Criteria**

A registered landlord receives an isolated workspace and can proceed to subscription selection without gaining access to another workspace.

### SAAS-REG-002 — Free Workspace Provisioning

**Source location:** attachment line 2240.

**Linked frontend screen contracts:** FS-02. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Display the Free plan as an actual selectable plan, without requiring a payment method unless a separately approved commercial policy requires it.

**Authoritative result dependency:** Show actual provisioning progress, partial failure and retry. Provisioning completes only when the workspace, owner access and Free subscription/entitlements are confirmed.

**Acceptance Criteria**

A new Free customer can create and manage rental units up to the configured allowance without purchasing a paid subscription.

### SAAS-REG-003 — Guided Setup

**Source location:** attachment line 2264.

**Linked frontend screen contracts:** FS-02. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Provide onboarding steps for:

1. Workspace profile.
2. Subscription confirmation.
3. First property.
4. First unit.
5. Optional existing tenant.
6. Rent and charge configuration.
7. Notification preferences.

Allow saving progress and returning later.

**Authoritative result dependency:** Resume authoritative setup progress; local step clicks alone do not establish completion.



### SAAS-PLAN-001 — Public Pricing Page

**Source location:** attachment line 2292.

**Linked frontend screen contracts:** FS-03. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Create a responsive `/pricing` page with:

- Free, Silver, Gold, Platinum, Diamond cards.
- Monthly/annual billing selection.
- Unit limits.
- Feature comparisons.
- Included communication quotas.
- Overage/add-on disclosures.
- Tax and payment disclosures.
- Upgrade actions.
- Enterprise sales/contact action.
- Accessible feature explanations.

**Authoritative result dependency:** Read the active, audience-appropriate catalogue; do not invent prices or expose unpublished commercial configuration.



### SAAS-PLAN-002 — Plan Details

**Source location:** attachment line 2317.

**Linked frontend screen contracts:** FS-03. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Display a detailed comparison of features and allowances.

**Authoritative result dependency:** Display current plan details and configured benefits from the catalogue.



### SAAS-PLAN-003 — Plan Versioning

**Source location:** attachment line 2329.

**Linked frontend screen contracts:** FS-03. Apply the linked frontend contracts and the specific rules below together.

**Authoritative result dependency:** Show versioned effective terms; preserve historical contracted pricing and invoice terms.



### SAAS-SUB-001 — Subscription Dashboard

**Source location:** attachment line 2345.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend Route**

`/workspace/billing/subscription`

Display:

- Current plan.
- Subscription status.
- Billing period.
- Next renewal.
- Next invoice estimate where determinable.
- Active add-ons.
- Unit usage.
- Email/WhatsApp usage.
- Available upgrades.
- Payment history.

**Authoritative result dependency:** Display authoritative subscription, usage and billing-period state.



### SAAS-SUB-002 — Upgrade Subscription

**Source location:** attachment line 2370.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Show:

- Current versus requested plan.
- New capacity.
- Newly available features.
- Effective date.
- Proration or credit.
- Taxes and final payable amount.
- Confirmation.

**Authoritative result dependency:** Confirm a current priced upgrade quote. Pending payment is not activation; show authoritative payment and grant activation/recovery results.



### SAAS-SUB-003 — Downgrade Subscription

**Source location:** attachment line 2394.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Display features and quotas that will change.

Warn about current usage exceeding the target plan.

**Authoritative result dependency:** Display eligibility, effective billing boundary and over-limit restrictions; preserve inventory and historical access.



### SAAS-SUB-004 — Subscription Cancellation

**Source location:** attachment line 2410.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Provide cancellation details, effective date, access consequences, and confirmation.

**Authoritative result dependency:** Show cancellation effective date and retained access according to policy; cancellation must not silently delete historical records.



### SAAS-SUB-005 — Failed Subscription Payment

**Source location:** attachment line 2422.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Display payment failure, retry options, grace-period status, and restrictions.

**Authoritative result dependency:** Show payment failure, retry/recovery and configured grace/restriction state without withholding essential correction and tenancy-completion paths.



### SAAS-SUB-006 — Subscription Recovery

**Source location:** attachment line 2436.

**Linked frontend screen contracts:** FS-04; FS-05. Apply the linked frontend contracts and the specific rules below together.

Handle:

- Successful charge with browser closed.
- Repeated payment callback.
- Payment pending.
- Payment reversed.
- Subscription payment succeeded but entitlement activation failed.
- Cancellation during renewal.
- Concurrent upgrade requests.

Subscription transitions must be idempotent and auditable.

### SAAS-ENT-001 — Centralized Feature Evaluation

**Source location:** attachment line 2456.

**Linked frontend screen contracts:** FS-05. Apply the linked frontend contracts and the specific rules below together.

Use the authoritative entitlement decision for capability visibility and actions.

Example capabilities:

```text
PROPERTY_MANAGEMENT
RENTAL_LIFECYCLE
AUTO_ADVERTISE
TENANT_HELPDESK_ADVANCED
EMAIL_NOTIFICATIONS
WHATSAPP_NOTIFICATIONS
AI_CHATBOT
ADVANCED_REPORTING
BULK_IMPORT
PUBLIC_API
CUSTOM_BRANDING
```

**Authoritative result dependency:** Read effective capability decisions derived from subscription, plan version, add-ons, time-limited grants, overrides and restrictions. Hidden navigation is not proof of authorization.



### SAAS-ENT-002 — Feature Lock UX

**Source location:** attachment line 2494.

**Linked frontend screen contracts:** FS-05. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

For unavailable capabilities:

- Display a clear limitation.
- Explain the required plan.
- Provide an upgrade action to an authorized workspace billing administrator.
- Avoid disrupting in-progress essential tenancy workflows.

**Authoritative result dependency:** Handle stable entitlement-denial codes with the applicable limitation and recovery/upgrade action.



### SAAS-ENT-003 — Effective Entitlement Snapshot

**Source location:** attachment line 2511.

**Linked frontend screen contracts:** FS-05. Apply the linked frontend contracts and the specific rules below together.

Read the frontend-safe effective feature and quota configuration.

Cache carefully and invalidate after subscription changes.

High-risk actions require a fresh authoritative entitlement outcome before presenting success.

### SAAS-QUOTA-001 — Unit Limit Enforcement

**Source location:** attachment line 2525.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

Default allowances:

- Free: 5 rentable units.
- Silver: 50.
- Gold: 200.
- Platinum: 1,000.
- Diamond: Contract-defined.

**Frontend**

Show usage such as:

`4 of 5 units used`

Display limit warnings before users reach capacity.

**Authoritative result dependency:** Handle authoritative capacity rejection during creation/import/restoration. If two users compete for one remaining slot, at most one success is displayed.



### SAAS-QUOTA-002 — Unit Counting Rules

**Source location:** attachment line 2551.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

Define units separately from properties, floors, tenancies, and occupants.

For shared accommodation, define whether the billable quantity is:

- Physical rentable units.
- Separately rentable beds.
- A configurable weighted combination.

Document and consistently enforce the chosen commercial definition.

Recommended baseline: count each independently rentable inventory item once, with explicit rules for shared beds.

Define how draft, archived, inactive, and occupied inventory contributes to quotas.

Do not let users bypass limits through rapid archive/restore operations.

### SAAS-QUOTA-003 — Communication Allowances

**Source location:** attachment line 2571.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

Meter:

- WhatsApp messages.
- Transactional emails.
- SMS where included.
- Other billable provider operations.

Define whether usage is measured by:

- Provider accepted.
- Successfully delivered.
- Attempted.
- Charged by provider.

Use the contractually selected metric consistently.

### SAAS-QUOTA-004 — Usage Dashboard

**Source location:** attachment line 2591.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

**Frontend Route**

`/workspace/billing/usage`

Display resource consumption by billing period, including limits, remaining allowance, and projected overages where supported.

**Authoritative result dependency:** Show authoritative usage aggregates with their actual billing windows.



### SAAS-QUOTA-005 — Quota Exhaustion

**Source location:** attachment line 2605.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

Define behavior for exhausted quotas:

- Reject additional discretionary actions.
- Allow plan upgrades or add-ons.
- Preserve already committed operations.
- Permit critical administrative corrections.
- Avoid duplicate communication charges.
- Explain restrictions without exposing internal provider errors.

### SAAS-QUOTA-006 — Add-On Consumption

**Source location:** attachment line 2618.

**Linked frontend screen contracts:** FS-05; FS-32. Apply the linked frontend contracts and the specific rules below together.

Support optional resource packs such as:

- Additional units.
- WhatsApp message bundles.
- Email allowances.
- Document storage.
- AI usage credits.

Only enable add-ons approved in the commercial catalog.

### SAAS-BILL-001 — Subscription Checkout

**Source location:** attachment line 2642.

**Linked frontend screen contracts:** FS-04. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Display subscription order summary, discount, applicable tax, final amount, supported payment methods, and payment status.

**Authoritative result dependency:** Show confirmed, failed, pending and unknown checkout outcomes from the correct billing domain; browser completion alone cannot activate paid access.



### SAAS-BILL-002 — Subscription Invoices

**Source location:** attachment line 2656.

**Linked frontend screen contracts:** FS-04. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Allow authorized customers to view and download SaaS invoices and receipts.

**Authoritative result dependency:** Display the issued subscription invoice, original commercial terms, taxes/adjustments where applicable, and its actual status.



### SAAS-BILL-003 — Payment Reconciliation

**Source location:** attachment line 2668.

**Linked frontend screen contracts:** FS-04. Apply the linked frontend contracts and the specific rules below together.

Show verified reconciled provider status; a browser return or callback claim alone is not confirmed payment.

Do not mark a subscription paid solely because the browser returned from checkout.

### SAAS-BILL-004 — Subscription Renewal

**Source location:** attachment line 2676.

**Linked frontend screen contracts:** FS-04. Apply the linked frontend contracts and the specific rules below together.

Support recurring renewal if the selected provider and merchant account allow it.

Otherwise support explicit renewal payments.

Model mandate consent, failures, cancellation, retries, and eligibility.

### SAAS-BILL-005 — Separation of Funds

**Source location:** attachment line 2686.

**Linked frontend screen contracts:** FS-04. Apply the linked frontend contracts and the specific rules below together.

Do not mix SaaS subscription revenue with:

- Rental collections.
- Tenant security deposits.
- Property maintenance funds.
- Co-owner settlements.

Use distinct payment purposes, provider references, accounting records, and beneficiary configurations.

### SAAS-NOTIFY-001 — WhatsApp Configuration

**Source location:** attachment line 2703.

**Linked frontend screen contracts:** FS-22. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Provide an authorized settings page showing:

- WhatsApp availability by plan.
- Sender/provider status.
- Template and use-case availability.
- Opt-in/consent status.
- Message usage.
- Delivery history.

**Authoritative result dependency:** Show connection/template/consent health and quota/provider failures; credentials remain masked and provider-controlled.



### SAAS-NOTIFY-002 — Transactional Email

**Source location:** attachment line 2724.

**Linked frontend screen contracts:** FS-22. Apply the linked frontend contracts and the specific rules below together.

Support:

- Rent reminders.
- Due invoices.
- Receipts.
- Appointment notifications.
- Application updates.
- Agreement notifications.
- Maintenance updates.
- Move-out notices.

Use approved sender identities and an email provider integration.

### SAAS-NOTIFY-003 — Automated Notifications

**Source location:** attachment line 2741.

**Linked frontend screen contracts:** FS-22. Apply the linked frontend contracts and the specific rules below together.

Allow eligible workspace users to configure supported notification rules.

Examples:

- Rent reminder before due date.
- Overdue reminder.
- Payment received notification.
- Visit confirmation.
- Ticket assignment.
- Agreement signing reminder.
- Renewal reminder.

**Authoritative result dependency:** Show configured rule status and actual delivery outcomes; essential business actions do not depend on successful notification delivery.



### SAAS-NOTIFY-004 — Failures and Recovery

**Source location:** attachment line 2761.

**Linked frontend screen contracts:** FS-22. Apply the linked frontend contracts and the specific rules below together.

Handle provider downtime, expired consent, rejected templates, delivery failures, duplicate events, quota exhaustion, and subscription downgrade.

Notification failure must not roll back unrelated successful financial transactions.

### SAAS-ADS-001 — Auto-Publish Configuration

**Source location:** attachment line 2773.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Provide a setting allowing eligible landlords to automatically publish vacant rentable units.

Require explicit workspace authorization and a selected publication policy.

**Authoritative result dependency:** Display entitled auto-publication configuration and authoritative listing eligibility/publication state.



### SAAS-ADS-002 — Status-Driven Publishing

**Source location:** attachment line 2787.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

Support controlled transitions such as:

- Unit becomes available: create or activate an eligible listing.
- Unit receives a valid hold: apply the configured listing policy.
- Unit becomes occupied: withdraw or mark the listing unavailable.
- Unit becomes available after move-out: republish when approved conditions are satisfied.

Prevent duplicate active listings for the same independently rentable inventory item.

### SAAS-ADS-003 — External Advertising

**Source location:** attachment line 2800.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

Provide the Facebook and Instagram publishing workflow below in addition to LMT discovery publication. Live channel activation remains dependent on verified supported APIs, account eligibility, app permissions/review and customer authorization. Other platforms remain conditional on explicit configuration.

Distinguish an organic property post from a paid advertising campaign. The offboarding workflow can prepare either, but organic-content approval must never authorize paid spend. Do not claim publication to unsupported destinations, personal profiles, groups or marketplaces simply because Facebook/Instagram integration exists. Unsupported channels remain clearly disabled or provide an explicitly manual export flow.

### SAAS-ADS-004 — Offboarding Trigger and Vacancy Advertising Draft

**Source location:** attachment line 2810.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for workflow; eligible workspace owner/manager. **Phases:** 6 for offboarding events; 8 for external publishing automation.

**Flow:** Actual tenant possession handover → independently rentable unit/bed released → vacancy/readiness evaluation → draft advertisement package → manager/owner review → approved publication job → per-channel confirmation and live links.

Use the authoritative physical-handover/capacity-release event, not a notice submission, planned departure, account deactivation or completed deposit refund. Outstanding financial settlement does not alone prove continued occupancy or prevent recording a genuine vacancy. A co-tenant exit that leaves the same rentable unit occupied must not advertise the whole unit as vacant. For bed-level rentals, advertise only the released eligible bed under the configured inventory model.

Enable the automation per workspace/property with allowed channels, approver policy, default template, contact method and publication timing. An enabled trigger creates or updates a reviewable draft; **it does not immediately post externally**. Readiness checks must cover repairs/cleaning, safe access, authoritative availability, pricing, current amenities/media and authorized listing eligibility. If readiness fails, keep a blocked draft with tasks and reason codes.

Record a vacancy episode/cycle identifier and deduplicate events across retries. Repeated offboarding events cannot create duplicate active campaigns. If possession is corrected, the unit becomes held/allocated, or move-out is reversed before publication, cancel or re-evaluate the draft and notify reviewers.

**Acceptance:** A completed eligible move-out creates one advertising draft and an approval task. A future notice, partial co-tenant exit, disputed unconfirmed handover or blocked unit cannot silently launch an unavailable-unit ad.

### SAAS-ADS-005 — Channel Preview and Manager/Owner Approval

**Source location:** attachment line 2826.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized advertising preparer and property approver. **Phase:** 8.

Provide `/workspace/advertising` with Draft, Awaiting approval, Changes requested, Approved, Scheduled, Publishing, Live/Partially live, Failed, Paused and Closed views. Link drafts from move-out completion, vacancy tasks and unit records. Show a channel-specific preview of caption/headline, images/video, amenities, rent/deposit/disclosed charges, availability date, permitted location detail, public contact, landing URL and booking/tour calls to action.

Generate content from reviewed listing/inventory data, not from departing tenants' documents or assumptions. Exclude names, private contact details, possessions, verification results, access codes and sensitive interior information. Check media publication rights, current condition, stale tour captures and material price/amenity changes. Optional AI copy drafting must use approved facts and remain subject to human review; no fabricated amenities, availability, ratings or guarantees.

Configure whether an authorized manager **or** owner may approve, or whether both/another required signer must approve. A manager may approve only within explicit delegated advertising authority; otherwise route to the owner. Enforce requester/approver separation where configured. Provide approve, reject, request changes, schedule and withdraw actions with comments and audit history.

Approval binds to a specific immutable content/media version, property/unit/vacancy episode, channel/account, publication schedule/expiry and organic-versus-paid mode. For paid campaigns include the separate budget approval below. Changes to material content, destination, audience, spend or availability invalidate affected approvals. Recheck authority, plan entitlement, account connection, approval validity and unit availability immediately before dispatch.

**Acceptance:** Draft generation never publishes. Only an authorized approval of the exact package can enqueue it. An expired delegation, changed price/media, reassigned destination or new occupancy blocks stale approval use and surfaces the next action.

### SAAS-ADS-006 — Facebook/Instagram Connections and Supported Publishing

**Source location:** attachment line 2842.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for adapters and connection UX; live publishing requires eligible accounts/permissions. **Phase:** 8.

Add `/workspace/settings/integrations/social` for authorized account connection, destination selection, connection health, granted capabilities, expiry/reconnection and disconnect. Use approved OAuth/business authorization flows; never request social-media passwords. Encrypt tokens server-side, redact them from logs and audit connection changes. Do not share tokens across workspaces or assume a connected account belongs to a property without explicit destination assignment.

Enable connection and publication controls only for verified provider capabilities, account types, permissions, formats, quotas and supported operations. Configure Facebook Page publishing and eligible Instagram professional-account publishing only where supported by the selected official integration. Paid placements use separately authorized advertising-account capabilities. A connection for one operation is not proof of permission for all operations.

Maintain a capability matrix per connected destination. Generate a supported payload for each channel, validate media/links and poll/reconcile asynchronous upload/processing jobs. Where a desired format or destination is unsupported, explain it before approval rather than pretending it was posted. Third-party marketplaces/groups and additional networks require their own supported adapter and permissions.

Provider documentation to verify during implementation: [Meta Instagram content publishing](https://developers.facebook.com/docs/instagram-platform/content-publishing/), [Meta Pages posts](https://developers.facebook.com/docs/pages-api/posts/), and [Meta Marketing API](https://developers.facebook.com/docs/marketing-api/). These are implementation verification references, not a claim that a live integration or current permission set has already been validated.

**Acceptance:** An authorized approved package publishes to each selected supported destination and stores its provider object ID and available permalink. Unsupported accounts, expired authorization, app-review restrictions and denied scopes produce truthful blocked/reconnect states. Disconnect prevents new dispatch and displays any external live content that still requires operator action.

### SAAS-ADS-007 — Paid Campaign Budget, Housing Policy, and Launch Controls

**Source location:** attachment line 2858.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must if paid ads are enabled; authorized property advertising approver and financial approver. **Phase:** 8; advanced optimization in 9–10 only if approved.

Keep organic publication and paid campaign launch visibly separate. Paid setup includes verified ad account/payee responsibility, campaign objective, eligible placements, approved geography/audience configuration, currency, daily/lifetime spending limits, schedule, total authorized commitment, billing ownership and an approval expiry. Defaults must not start billable campaigns. Do not debit tenant rent, security deposits or consumer membership balances for advertising.

Require explicit review/approval of the spend envelope before launch; a manager exceeding their limit routes to the authorized financial approver. Show that provider delivery review and billing are external processes; local approval does not guarantee acceptance or exact delivery time. Record provider spend reporting freshness and any billing variance rather than claiming a perfectly instantaneous hard stop where the provider cannot guarantee it.

Verify current housing-ad classification, prohibited/discriminatory targeting restrictions and required disclosures for the applicable geography and provider. Apply required categories and supported audience controls; do not invent a universal rule or allow workarounds around provider restrictions. Do not use police outcomes, tenant identity documents, sensitive traits or private rental-payment histories to target or exclude housing audiences.

Budget increases, expanded audience/scope, changed paid dates or new campaigns require the relevant refreshed approval. No autonomous paid boosting, budget optimization or extra-platform expansion from a prior organic approval. Provide authorized pause/cancel controls and reconciliation of actual spend against the approved envelope.

**Acceptance:** A zero/unapproved/missing budget cannot launch paid delivery. Repeated requests cannot create duplicate billable campaigns. Provider rejection, spend uncertainty and exceeded/approaching limits are visible and routed to an authorized operator, without silently purchasing more reach.

### SAAS-ADS-008 — Delivery Reliability, Availability Sync, and Takedown

**Source location:** attachment line 2874.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and integration operators. **Phase:** 8.

Show each channel delivery independently: queued, preparing-media, awaiting-provider-review where applicable, publishing, live, outcome-unknown, failed, paused/withdrawal-requested, removed/unavailable, or action-required. Overall approval and per-channel outcomes are separate; Facebook success with Instagram failure is Partially live, not complete success or a rollback of the successful post.

Show per-channel retry, rate-limit, reconciliation and uncertain-outcome states. If publication may have succeeded, preserve its operation/reference while checking the outcome; do not create a duplicate post. Keep unresolved outcomes visible for authorized manual recovery.

Before each dispatch, check current availability and authorization. After a unit is held, allocated, occupied, withdrawn or its material terms change, trigger the approved channel policy: pause paid delivery; edit/mark unavailable or remove organic content where the API permits; otherwise create an urgent manual action with the external link. A stop request is not confirmed provider removal. Confirm state through supported APIs and show last checked time.

A cancelled/reoccupied listing must not be shown as validly republished by stale pending work; show reconciliation/corrective status. Race conditions with external providers must leave a tracked corrective action; do not claim that external posts vanish atomically with a local transaction. The LMT landing page must immediately reflect authoritative availability and prevent invalid bookings even if an external ad persists.

**Acceptance:** Replayed events do not create duplicate posts/campaigns; partial delivery retries only the unresolved channel; unknown outcomes are reconciled; occupied inventory stops new publication and initiates tracked takedown/pause. Inability to remove externally is visible rather than falsely marked successful.

### SAAS-ADS-009 — Advertising Dashboard, Leads, and Traceability

**Source location:** attachment line 2890.

**Linked frontend screen contracts:** FS-10; FS-30. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for status/audit; channel analytics and lead integrations depend on approved provider capabilities. **Phase:** 8; enhanced reporting in 9–10.

Show unit/property, vacancy date, readiness, preparer/approver, approved version, channels/accounts, mode, schedule, publication state, live links, last sync, provider error/rejection and next action. Cards cover drafts awaiting approval, vacancy-to-approval/publication delay, units with no live advertisement, channel failures, ads still live after allocation and paid spend where enabled.

Where authorized provider insights exist, display impressions, reach, clicks and spend with provider date range/attribution window and freshness. Do not sum cross-channel reach as unique people or fabricate unavailable metrics. Cost per lead requires verified spend and a defined deduplicated attributed-lead count; zero leads means N/A, not zero acquisition cost.

Link posts to LMT listing/tour/appointment pages with privacy-safe campaign attribution. Supported lead-form integrations require separate permissions/notice and deduplicate contacts through the existing inquiry/appointment workflow. A click is not a lead, an appointment is not a lease, and correlation is not proof the ad caused a move-in. Do not scrape social comments or profiles to create tenant records.

Expose advertising draft, version, approval, connection, destination, publication, budget/spend and attribution information through the linked screens. Actions include draft/review/approve/schedule, publication-status review, pause and reconciliation. Show immutable approved creative and operation references; tenant-private material remains excluded.

**Acceptance:** A manager can trace offboarding → readiness → draft → approval → provider result → lead/booking and eventual takedown. Paid analytics reconcile to provider statements within disclosed reporting delays; exports contain no social credentials or departing tenant private data.

### SAAS-ADMIN-001 — Customer Management

**Source location:** attachment line 2925.

**Linked frontend screen contracts:** FS-06. Apply the linked frontend contracts and the specific rules below together.

**Frontend**

Support:

- Customer directory.
- Workspace search.
- Workspace status.
- Current plan.
- Unit usage.
- Billing state.
- Customer activity summary.
- Audited administrative actions.

**Authoritative result dependency:** Read and act only within explicit platform permissions; show auditable reasoned actions and resulting customer/subscription states.



### SAAS-ADMIN-002 — Plan Management

**Source location:** attachment line 2946.

**Linked frontend screen contracts:** FS-06. Apply the linked frontend contracts and the specific rules below together.

Allow authorized platform administrators to:

- Create plan definitions.
- Version entitlements.
- Configure limits.
- Manage prices.
- Activate/deactivate offerings.
- Configure add-ons.
- Control eligibility and effective dates.

Changes must not silently rewrite active contracts.

### SAAS-ADMIN-003 — Subscription Operations

**Source location:** attachment line 2962.

**Linked frontend screen contracts:** FS-06. Apply the linked frontend contracts and the specific rules below together.

Support authorized manual adjustments, payment investigation, billing support, and promotional grants.

Require reason codes and audit history.

Sensitive adjustments should support additional authorization where appropriate.

### SAAS-ADMIN-004 — Platform Analytics

**Source location:** attachment line 2972.

**Linked frontend screen contracts:** FS-06. Apply the linked frontend contracts and the specific rules below together.

Display:

- Total workspaces.
- Active Free workspaces.
- Paying workspaces by plan.
- Subscription revenue.
- Monthly recurring revenue.
- Annual recurring revenue.
- Upgrades.
- Downgrades.
- Churn.
- Trial conversion where applicable.
- Unit utilization.
- Communication usage.
- Subscription payment failures.

Define each metric precisely and distinguish confirmed revenue from estimates.

### SAAS-ADMIN-005 — Support Access

**Source location:** attachment line 2994.

**Linked frontend screen contracts:** FS-06. Apply the linked frontend contracts and the specific rules below together.

Platform administrators must not automatically obtain unrestricted access to all landlord and rental tenant records.

Implement scoped, time-limited, reason-coded support access where authorized.

Protect sensitive financial and identity information.

### MARKET-001 — Email Identity, Optional Federation, and Browser Notifications

**Source location:** attachment line 3049.

**Linked frontend screen contracts:** FS-01; FS-22. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for verified email sign-in; Conditional for Google/Apple federation and browser push. **Roles/phases:** All account users; Phase 1, optional push in 8.

Extend mobile OTP identity with verified email sign-in/recovery using the selected secure identity provider or a documented expiring single-use link/OTP flow. Define enumeration protection, retry limits, session revocation and reauthentication for sensitive changes. Never link two accounts solely because an unverified provider email matches: require verified identity proof and a safe explicit account-linking process. Preserve multiple tenant/owner contexts and personal subscriptions after a verified link/merge, with audit and recovery.

Where configured, support Google/Apple login through approved OAuth/OIDC flows, provider subject IDs and consent. Handle hidden/changed email and provider disconnect without losing account ownership or bypassing recovery. Optional KYC is tied to a purpose-specific workflow; phone/email verification is not police or residence verification.

For optional browser push, request permission after a user action, support denial/device unsubscribe, persist only necessary tokens, and keep sensitive details behind authenticated links. PWA installation is not mandatory for core notifications or browser use.

**Acceptance:** Expired/replayed email tokens fail; unverified account matching cannot take over an account; disconnected social login has a safe recovery path; denied push leaves in-app notifications functional.

### MARKET-002 — Complete Rental Attributes and PG/Co-Living Offers

**Source location:** attachment line 3063.

**Linked frontend screen contracts:** FS-07; FS-09; FS-16. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for structured rental/PG inventory. **Roles/phases:** Authorized advertisers and seekers; Phases 2–3, billing in 5.

Extend property types to apartments, independent houses, villas, studios, PG/co-living and shared accommodation. Record carpet/built-up area distinctly with original unit and canonical conversion, BHK/room configuration, floors, furnishing, building age, accessibility, parking allocation, lift, power backup, water supply, shared facilities and truthful optional facing details. Unknown fields remain unknown, not false/default claims.

Display rent, deposit, maintenance, other recurring charges, brokerage if applicable, negotiability, lease length, notice and available-from date separately. Capture permitted pet/smoking/occupancy/house rules without embedding discriminatory or unsupported eligibility filters. Any accommodation eligibility restriction needs an explicitly reviewed applicable policy, not inferred sensitive traits or private screening data.

For PG/shared listings, model physical property/room/bed, offered allocation type, capacity, shared bathrooms/facilities, meal inclusion/options and associated charge terms. Distinguish per-bed, per-room and whole-unit rent; do not compare them as equivalent prices or list one room plus its beds as additive stock. Meal enrollment/service terms follow the existing optional-charge pattern. Preserve independently rentable inventory/quota and overlapping-occupancy constraints.

**Acceptance:** A seeker can understand which space and recurring cost the offer covers; filterable attributes match inventory and publication snapshots; room/bed reservations cannot double-allocate capacity; photos/features of a common area are labeled shared.

### MARKET-003 — Advanced Search, Geo Queries, Ranking, and Index Integrity

**Source location:** attachment line 3077.

**Linked frontend screen contracts:** FS-10. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for structured search; Conditional for configured external map/geocoding data. **Roles/phases:** Public rental seekers; Phase 3, tuning in 9–10.

Provide location autocomplete, multi-locality selection, city/landmark search, optional radius/map bounds and synchronized list/map views. Add filters for property type/BHK, normalized rent range, furnishing, applicable house rules, amenities, available date, reviewed verification scope, advertiser type and listing recency. Combine filters consistently and show authoritative matching counts, active-filter chips and zero-result alternatives; do not silently broaden the query without disclosure.

Sort by price/recency with stable tie-breakers and pagination; relevance ranking must use disclosed legitimate factors such as query match, availability, distance and freshness. Sponsored placement is separately labeled and cannot bypass filters, moderation or visibility. Hide exact occupied-home coordinates where policy requires approximate location.

Search reflects authoritative publication, price, availability and occupancy. Recheck stale results before detail, inquiry or booking. Public results must exclude private SALE listings, unapproved drafts and gated services. Disclose freshness rather than implying instantaneous updates.

**Acceptance:** Counts, pagination and map results agree under the same filters; stale indexed availability cannot finalize an invalid inquiry/booking; failed map/geocoder service leaves textual locality search usable; currency/area/rent-unit comparisons are correct.

### MARKET-004 — Rental Listing Authenticity, Freshness, and Moderation

**Source location:** attachment line 3091.

**Linked frontend screen contracts:** FS-10; FS-06. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must. **Roles/phases:** Authorized advertiser, platform listing moderator and reporter; Phases 2–3.

Create rental-listing review with draft, submitted, correction-required, approved, published, paused, expired, rejected and removed states, linked to unit availability but not replacing it. Check verified advertiser contact, documented authority where required, completeness, media rights and publication policy. Verification badges must state exactly what was checked, by whom/process and when; contact verification does not imply legal title or quality certification.

Provide candidate duplicate detection using normalized location/unit attributes and media similarity where justified; suspicious price/contact/media patterns are review signals, not automatic fraud verdicts. Allow legitimate multi-unit buildings and authorized advertisements without wrongly merging distinct inventory. Review fake listings, impersonation and unauthorized brokerage reports with reasoned decisions, correction/resubmission and appeal. Risk decisions are audited and do not expose reporters/evidence unnecessarily.

Record last advertiser-confirmed availability, independent verification time and update history. Request periodic reconfirmation using configured intervals; stale listings become flagged/paused/expired under policy. An edit must not reset “newly listed” history to manipulate ranking. Safe listing removal preserves inquiries, signed documents and financial records.

**Acceptance:** Required checks gate publication; changed material content triggers re-review as configured. Rejected users can correct/appeal; a property becoming occupied withdraws eligibility regardless of promotion payment. Staff cannot manufacture verified badges or hide history through delete/recreate.

### MARKET-005 — Rental Leads, Contact Access, Messaging, and Visit Feedback

**Source location:** attachment line 3105.

**Linked frontend screen contracts:** FS-11; FS-12. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for core leads/messaging; Conditional for paid direct-contact expansion. **Roles/phases:** Seeker and authorized advertiser; Phase 3, monetized extensions in 7A–8.

Provide tenant interest/inquiry and an advertiser lead inbox with New, Contacted, Interested, Not proceeding, Converted and Closed states, assigned handler, next action, communication history and permitted follow-up. Keep these separate from applications, appointments and actual occupancy. Allow advertiser acceptance/decline with non-sensitive reasons and tenant withdrawal/block/report. Detect duplicate/spam submissions through idempotency and configurable cooldown; do not delete genuine subsequent questions.

Offer in-app communication and advertiser-published approved contact methods with consent and role restrictions. If the catalog enables a direct-contact unlock allowance, specify scope (person/listing/advertiser), reset period, repeat-unlock rule and per-account event ledger. Transactionally authorize/reserve/consume the allowance when a permitted reveal succeeds; retries, duplicate clicks and failed reveals must not consume multiple credits. Contact is never disclosed first and checked afterward. Previously authorized transaction contacts remain available under the published policy; revoking future discovery cannot erase essential ongoing tenancy communication.

Provide a free inquiry/ordinary visit route even when expanded direct-contact access is paid. Phone-link clicks are not completed calls. Users can submit private post-visit feedback such as interested/not interested and factual observations; this is not a public verified-resident property review. Actual lead conversion links to verified tenancy/move-in events, not a sales agent's optimistic status.

**Acceptance:** Concurrent unlocks cannot exceed the approved quota; failed disclosure returns/reconciles reserved capacity; a blocked advertiser cannot keep messaging; staff see only assigned leads; submitted visit feedback cannot inflate REVIEW ratings.

### MARKET-006 — Shortlist Sharing, Recently Contacted, and Change Alerts

**Source location:** attachment line 3119.

**Linked frontend screen contracts:** FS-11; FS-22. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must within existing basic/Premium allowances. **Roles/phases:** Seekers; Phase 3 basic, Phase 7A expanded.

Persist favorites/saved filters across devices and provide recently contacted listings from actual inquiry/reveal history. Add optional shared shortlists with invited viewers or explicitly public-link scope, revocation/expiry and owner-controlled editing. Share only public rental fields the recipient may access; never copy private notes, contact unlocks, identity data, restricted SALE listings or Premium service ads into an unrestricted share page.

Support notifications for matching new listings and material rent/availability changes, with frequency/quiet hours, opt-out, deduplication and current visibility rechecks. A favorite does not reserve a unit. Removed listings show an honest unavailable state while retaining only authorized private notes/history; plan downgrade preserves read/export access under existing policy instead of deleting personal data.

**Acceptance:** Favorites synchronize correctly, revoked shares fail safely, ineligible recipients cannot gain gated content and duplicate events do not spam price-change alerts.

### MARKET-007 — Conditional Broker Participation and Brokerage Disclosure

**Source location:** attachment line 3131.

**Linked frontend screen contracts:** FS-31. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Conditional; required only if broker-inclusive mode is enabled. **Roles/phases:** Verified authorized agent/broker, owner and scoped moderator; Phase 9.

Implement a platform participation setting defaulting to owner/authorized-manager listings. If enabled, onboard broker identity/business profile, permitted regions and documented property-specific listing authority with effective dates/revocation. A broker's paid plan does not establish ownership or authorization. Separate agent listing/lead access from property-manager operational access.

Label owner-direct, property manager and broker-advertised offers accurately. Disclose brokerage amount/basis, payer, payment trigger, applicable tax/terms and whether negotiable before a seeker commits. “Zero brokerage” must have an actual applicable basis. Revoke publication/lead actions when authority expires, retaining permitted history. Resolve owner/broker duplicate-ad disputes without automatically erasing legitimate property records.

**Acceptance:** Disabled broker mode blocks agent publication; enabled mode enforces authority and disclosure. An agent cannot masquerade as an owner, see another agent's leads or gain tenant-document/financial access. Brokerage collection remains conditional on an approved payment scope.

### MARKET-008 — Versioned Rent and Deposit Negotiation

**Source location:** attachment line 3143.

**Linked frontend screen contracts:** FS-11; FS-13. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must. **Roles/phases:** Applicant and authorized owner/manager; Phase 3, agreement handoff in 4.

Add offers/counteroffers for rent, deposit, requested start/date, duration and explicitly negotiable charges/conditions. Store author, immutable version, expiry, proposed terms and states draft, submitted, countered, accepted, rejected, withdrawn and expired. A tenant or manager cannot alter an already accepted version; owner approval limits apply to concessions.

Acceptance creates an accepted-terms snapshot linked to the application/agreement draft. It does not automatically sign the lease, collect funds or grant possession. Any temporary hold must be separately explicit, expiry-bound and capacity-safe. Acceptance must revalidate availability and conflicting offers; multiple negotiations may coexist, but incompatible finalized allocations cannot.

**Acceptance:** Stale/expired offer acceptance fails clearly; concessions outside delegation route to approval; the agreement uses the accepted terms exactly; changing an accepted amount requires a new reviewed version and any required approval.

### MARKET-009 — First-Party Listing Promotion and Contact/Search Products

**Source location:** attachment line 3155.

**Linked frontend screen contracts:** FS-31; FS-04; FS-24. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for catalog/advertiser experience when these products are offered; commercial activation conditional. **Roles/phases:** Authorized workspace billing user and platform catalog admin; Phases 7–8.

Define optional LMT marketplace promotion packages separately from Facebook/Instagram spend and from the base workspace plan. Specify duration, eligible listing/property, placement inventory, permitted channels, quota/allowance, prices, taxes and cancellation/refund policy. Implement server-priced purchase, verified activation, immutable invoices, promotions/discount code eligibility and expiry through existing SaaS/consumer billing domains with explicit product purpose.

Label sponsored/featured results, preserve filter/availability/moderation requirements and cap placements under policy. Purchase cannot guarantee a tenant, contact volume or ranking outside disclosed rules. Paused/occupied/removed listings stop promotional exposure; any refund/remaining-time treatment follows the accepted terms, not an invented automatic refund.

Contact-access or assisted-search packs use the personal consumer catalog and existing usage/grant model. Show what is already included to prevent selling duplicate entitlements or charging twice. Financial reporting distinguishes workspace subscriptions, promotion purchases and consumer benefits without creating a new unscoped wallet.

**Acceptance:** Failed/unverified payment cannot activate promotion; duplicate callbacks do not double-grant duration/credits. A promoted unavailable listing stays unavailable, and changing package pricing cannot rewrite historical terms.

### MARKET-010 — Assisted Search and Relationship-Manager Cases

**Source location:** attachment line 3169.

**Linked frontend screen contracts:** FS-25; FS-31. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must when included in an active offered consumer plan; staffing-dependent activation. **Roles/phases:** Eligible seeker, assigned platform relationship manager and supervisor; Phase 7A.

Extend concierge requests with an assisted-search case: budget, locations, property needs, move date, approved communications and service allowance/expiry. Assign a trained available relationship manager with time-bounded, purpose-limited access; distinguish this platform service role from a landlord's property manager.

Support candidate shortlists, rationale, seeker feedback, visit coordination, task handoff, inactivity escalation and closure/reopening. Share approved preferences and selected listings only; the agent cannot browse police files, hidden applications or full financial histories. No signing, submission, payment, rent negotiation acceptance or property visit commitment without appropriate user authorization.

Show actual staffing coverage, limits and expected response targets. Plan expiry follows a disclosed continuation/handoff policy for open requests; do not promise a guaranteed home or staffed service that is unavailable.

**Acceptance:** The assigned manager sees only the case scope, tenant confirmation controls commitments, absences have explicit handover and completed assistance is not falsely counted as a completed tenancy.

### MARKET-011 — Moving Quotes and Completed-Service Feedback

**Source location:** attachment line 3183.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for request/feedback extensions; Conditional for automatic booking/payment. **Roles/phases:** Eligible service requester, approved provider and moderator; Phase 7A.

Extend SERVICE-PREM to packers/movers and relocation quotes, with origin/destination locality, preferred moving date, rough inventory/load, access/lift requirements and optional images. Reveal exact addresses only as necessary with consent. Support provider quotes, included/excluded costs, revision acceptance and agreed schedule using existing request states; avoid broadcasting household contents to unrelated providers.

Allow one review/feedback record per requester and completed service case with actual service evidence, stars/description, editing policy, provider response and reporting/moderation. Label verification scope truthfully; a contact click or unconfirmed completion claim alone is insufficient. Keep service feedback separate from resident property reviews.

A future service checkout needs explicit merchant/payee role, cancellation/refund, provider settlement, dispute and fee rules. Until enabled, do not present request confirmation as paid booking or fabricate a payment receipt.

**Acceptance:** Quote scope/version survives scheduling; private addresses remain scoped; cancelled/unfulfilled cases cannot create fake verified-completion reviews; complaints and existing request history remain available after entitlement expiry.

### MARKET-012 — Locality Information, Rent Insights, and Explainable Matching

**Source location:** attachment line 3197.

**Linked frontend screen contracts:** FS-34; FS-10. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Should; data/provider-dependent features Conditional. **Roles/phases:** Rental seekers and authorized report viewers; Phase 9.

Add locality comparison and nearby transit/schools/hospitals/facilities from licensed/current approved sources, with provenance, dates and coverage. Map distance and travel time are different measures; only show travel estimates if a configured provider supplies them. No inferred safety/quality guarantee from a locality label.

Show locality rent trends/comparables only where sufficient eligible data exists. Separate asking prices from accepted rents, normalize room/bed/whole-unit, size/furnishing and time period, remove duplicate observations, and disclose sample size, distribution, freshness and limitations. Suppress small/private cohorts and use aggregated authorized data; do not expose another landlord's individual executed rent or tenant payment record. Estimates are decision support, not guaranteed valuation.

Recommend similar properties from explicit preferences and current permitted listing attributes; optional behavioral personalization requires consent/control and an explanation such as budget/locality/amenity match. Provide reset/opt-out and a useful non-personalized fallback. Do not infer sensitive traits or use police/background-check data for ranking. Sponsored matches are labeled separately.

Locality community reviews are a conditional module with its own participation, moderation/privacy and authenticity policy; do not present property-resident reviews as evidence of living throughout a locality or seed invented opinions.

**Acceptance:** Insufficient data yields unavailable/limited insights, recommendations respect filters/visibility, private sale catalog remains gated, and opt-out stops new personalization without breaking search.

### MARKET-013 — Official Documentation Services and Additional Verification Adapters

**Source location:** attachment line 3213.

**Linked frontend screen contracts:** FS-15; FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Conditional for provider-specific services; core agreement/verification remains as already required. **Roles/phases:** Authorized applicant/signatories/service operators; Phases 4 and 6.

Where a verified official process supports it, add jurisdiction-versioned stamp-duty estimation with required input fields, effective date, calculation source and a distinction between estimate and official assessed/paid amount. Unsupported jurisdictions require review, not a guessed rate. E-stamping/registration adapters track submission, references, fees/receipts, appointments and actual issued evidence; e-signing alone must not mark registration complete. Optional physical agreement delivery needs consented delivery address, document version, dispatch/tracking, delivery evidence and failure handling.

Employment/reference checks require an explicitly authorized service, purpose-specific consent/other verified basis, scoped data collection, approved contact/source verification, factual outcome and correction/review route. Keep these distinct from POL-VER official police checks and REVIEW residence eligibility. Missing/unresponsive evidence yields incomplete, not an automatic adverse finding or opaque tenant risk score. Do not contact an employer/reference from an unreviewed AI-generated message or imply official verification without provenance.

**Acceptance:** Estimates are labeled, duplicate provider callbacks/fees reconcile, and failed delivery does not alter agreement validity. Sensitive check reports are visible only to approved recipients and excluded from listing search, recommendation systems and general AI retrieval.

### MARKET-014 — Rental Payee Onboarding, Fees, and Settlement Visibility

**Source location:** attachment line 3225.

**Linked frontend screen contracts:** FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for accurate payment status/fee disclosure; Conditional for executing provider-supported payouts. **Roles/phases:** Authorized payee/financial administrator and payer within scope; Phase 5.

Validate the authorized rental recipient through the configured payment-provider/merchant process; document whether the platform collects, routes or merely records payments. Verify payee-account changes with additional authority and tenant notification, preserving prior references. Do not assume a platform can pool or split funds just because the UI has multiple co-owners.

Before checkout disclose payment-method/provider fees, applicable configured taxes, amount paid, beneficiary and whether fees are refundable. Do not silently charge a different amount after approval. Model payer payment confirmation separately from recipient settlement: pending payout, processing, settled, failed, reversed or reconciled as provided, with provider references, gross/net/fee breakdown and effective dates.

Handle provider reconciliation, delayed settlement, refund after settlement and reversed payouts using explicit financial events and authorized review. Payment receipt does not prove the landlord's bank has received settlement; label each state and do not duplicate rent collection on payout callbacks. Sensitive beneficiary bank data remains masked and access controlled.

**Acceptance:** A successful tenant payment with delayed landlord payout remains accurately represented; retries cannot issue multiple payouts; totals reconcile with provider records and existing RENT-FIN ledgers. Unsupported payout execution remains disabled while manual/status evidence is honestly tracked.

### MARKET-015 — Marketplace Operations, Content, Analytics, and Conditional Referrals

**Source location:** attachment line 3239.

**Linked frontend screen contracts:** FS-33. Apply the linked frontend contracts and the specific rules below together.

**Priority:** Must for marketplace operations/metrics; Should for content tooling; Conditional for reward programs. **Roles/phases:** Scoped platform admin/support, advertisers and seekers; Phases 3, 7–9.

Provide separate platform queues for rental listing review/fraud, verified advertiser cases, geography/category maintenance, lead/contact complaints, promotion disputes and assisted-search staffing. Route platform service complaints separately from property helpdesk; record jurisdiction/scope and escalate without granting support unrestricted landlord/tenant access. All rule/configuration changes require audit, reason and scoped administrative permission.

Manage cities/localities, aliases/boundaries and provider metadata with stable IDs and correction history. Add versioned FAQ/help/locality-guide/promotional content with draft/review/publish/schedule/expire states, accessible rendering, sanitized links/media and clear source/update dates. Editorial content is not verified tenant advice, legal clearance or platform inventory. Moderators cannot use a CMS page to reveal gated sale/service details.

Define platform metrics for published/pending/stale listings, unique listing impressions/views, search-to-detail, inquiry/contact, visit and actual-rental conversion, listing vacancy duration, locality demand, promotion effectiveness, subscription/product revenue, verification/fraud queues and platform support performance. Distinguish event counts/unique users/cohorts and define bot/test/staff exclusion and privacy-safe attribution; search impressions are not qualified leads. Use authorized aggregate cross-workspace reporting, while advertiser reports remain property-scoped. Do not mix platform commerce revenue with rent/deposit receipts.

If a referral program is approved, implement versioned eligibility, attribution window, qualified event, anti-self-referral/duplicate controls, reward cap, pending/earned/reversed/redeemed states, expiry, invoice/credit treatment and complaint handling. Do not offer unconfigured cash rewards or count a signup as earned reward before the required verified event. Financial reward execution is conditional on approved funding/provider/legal rules.

**Acceptance:** Admin changes are attributable and reversible where appropriate; metrics reconcile to the defined events; staff cannot manufacture conversions or paid rewards; referral retries and refunds cannot mint duplicate benefits; published content cannot bypass visibility/privacy controls.

### AUTH-001 — Authentication and Account Management

**Source location:** attachment line 3302.

**Linked frontend screen contracts:** FS-01. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Mobile OTP authentication through a supported provider.
- Secure account registration and login.
- Session management and logout.
- Account recovery.
- Role and property context switching.
- Profile management.
- Notification preferences.
- Session/device management.
- Additional verification for sensitive operations.
- Account export/deletion requests where applicable.

Protect privileged actions through server authorization and appropriate reauthentication.

### PROP-001 — Property and Unit Management

**Source location:** attachment line 3321.

**Linked frontend screen contracts:** FS-07; FS-09. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Multiple properties per landlord.
- Property hierarchy: property, building, floor, unit, tenancy.
- Property creation, editing, and archival.
- Co-owner association.
- Manager assignment.
- Property addresses and map references.
- Unit types and configurable categories.
- Area, capacity, furnishing, bathrooms, balconies, and amenities.
- Furniture and appliance inventory.
- Unit images and galleries.
- Rent, deposit, maintenance, utilities, and additional charges.
- House rules.
- Unit availability.
- Unit history and tenant history.
- Validation of duplicate identifiers.
- Status-based restrictions on destructive changes.

Support unit statuses such as draft, available, held, onboarding, occupied, notice served, maintenance blocked, and archived.

Where shared accommodation is enabled, distinguish a physical unit from separately allocated beds.

Show allocation conflicts and do not present overlapping exclusive allocations as successful.

### ADS-001 — Rental Listings and Property Discovery

**Source location:** attachment line 3350.

**Linked frontend screen contracts:** FS-10. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Listing creation from eligible units.
- Listing preview.
- Publish, pause, edit, expire, renew, and withdraw.
- Public listing search.
- City/locality/landmark search.
- Optional map integration.
- Rent, furnishing, availability, and amenity filters.
- Sorting and pagination.
- Favorites and saved searches where included in the release scope.
- Detailed listing pages.
- Inquiry and visit actions.
- Reporting of inaccurate or suspicious listings.

Never fabricate availability, reviews, or verification badges.

Automatically reconcile published listing eligibility with authoritative unit allocation state.

### VISIT-001 — Inquiry and Visit Scheduling

**Source location:** attachment line 3373.

**Linked frontend screen contracts:** FS-11; FS-12. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Inquiry submission.
- Conversation history.
- Visit availability.
- Appointment requests.
- Landlord/manager assignment.
- Confirmation and rejection.
- Rescheduling and cancellation.
- Completion and no-show tracking.
- Visit reminders.
- Visitor count and contact instructions.
- Conflict-safe slot reservation.

A confirmed visit must not imply reservation of the rental unit.

Handle authoritative scheduling conflicts, including another user taking a slot during booking.

### APPLY-001 — Rental Application and Tenant Onboarding

**Source location:** attachment line 3395.

**Linked frontend screen contracts:** FS-13. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Application creation.
- Save/resume.
- Move-in preferences.
- Household information.
- Primary tenant and co-tenant records.
- Authorized occupants.
- Guarantor details where applicable.
- Consent-based document uploads.
- Verification statuses.
- Application review.
- Clarification requests.
- Approval, rejection, and withdrawal.
- Configured temporary unit holds.
- Approved tenancy terms.
- Existing-tenant onboarding.
- Opening balances.
- Duplicate-account resolution.
- Conflict handling when a unit is allocated to another applicant.

Separate applicant approval from agreement execution, deposit payment, physical move-in, and tenancy activation.

### LEASE-001 — Rental Agreements and Digital Signing

**Source location:** attachment line 3422.

**Linked frontend screen contracts:** FS-15. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Configurable state-appropriate agreement templates.
- Reviewable agreement draft generation.
- Owner, tenant, and authorized signatory selection.
- Agreement term review.
- Correction requests.
- Document versioning.
- Electronic signing through an approved provider.
- Signature status tracking.
- Pending signature reminders.
- Failed/expired/declined signature recovery.
- Signed-document retrieval.
- Agreement renewal and amendments.
- Offline document upload where permitted.

Model agreement generation, acceptance, electronic signing, stamping, registration, and notarization/witnessing as distinct activities.

Executed agreement versions must remain immutable.

If agreement content changes after one party has signed, invalidate or supersede the affected signing workflow according to the provider and legal process.

Do not assume electronic signing alone fulfills all legal requirements.

### MOVEIN-001 — Move-In and Occupancy

**Source location:** attachment line 3450.

**Linked frontend screen contracts:** FS-14; FS-09. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Move-in scheduling.
- Inspection checklists.
- Property condition evidence.
- Appliance/furniture inventory acknowledgment.
- Initial meter readings.
- Key and access-card handover.
- Deposit/advance payment checks.
- Tenant/manager acknowledgment.
- Disputed condition items.
- Contract start versus actual occupancy dates.
- Authorized occupant updates.

Maintain traceable occupancy history.

### BILL-001 — Rent, Utilities, and Billing

**Source location:** attachment line 3470.

**Linked frontend screen contracts:** FS-16. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Recurring rent schedules.
- Configurable billing cycles.
- Rent invoices.
- Prorated charges.
- Late fees where legally and contractually applicable.
- Rent increases.
- Water and electricity billing.
- Meter readings with evidence.
- Shared meter allocation.
- Estimated readings and adjustments.
- Additional charges.
- Credits and waivers.
- Bill corrections.
- Charge disputes.
- Partial and advance payments.
- Payment allocation.
- Receipts and statements.

Display authoritative decimal monetary amounts and currency; do not use imprecise client arithmetic to establish balances.

Define deterministic rounding and allocation rules.

Do not use floating-point arithmetic for financial calculations.

### PAY-001 — Digital Payments and Reconciliation

**Source location:** attachment line 3500.

**Linked frontend screen contracts:** FS-17. Apply the linked frontend contracts and the specific rules below together.

Integrate an eligible payment gateway/provider supporting the approved payment methods.

Support, where commercially and technically available:

- UPI.
- Google Pay, PhonePe, Paytm, and other eligible UPI applications through supported payment flows.
- Net banking.
- Eligible debit and credit cards.
- Manual payment reporting with approval.

Implement:

- Payment intent/order creation.
- Server-generated payment references.
- Idempotency.
- Payment status lookup.
- Provider callback/webhook verification.
- Reconciliation.
- Unknown and pending payment outcomes.
- Retry without duplicate collection.
- Receipts.
- Refund requests.
- Refund status tracking.
- Manual transfer/cash verification.

Do not trust the browser return URL as payment confirmation.

Display verified collections from authoritative results.

Do not store UPI PINs, card CVVs, or banking credentials.

Treat payment eligibility, settlement structure, co-owner payout rules, and recurring mandates as provider- and business-dependent requirements.

### DEP-001 — Security Deposits and Final Settlement

**Source location:** attachment line 3537.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Deposit collection.
- Separate deposit accounting.
- Deposit adjustments.
- Proposed deductions.
- Evidence attachments.
- Approval workflow.
- Tenant disagreement/disputes.
- Refund recipient verification.
- Partial/full refunds.
- Final settlement generation.
- Refund references and reconciliation.

Do not automatically use a security deposit to pay rent without the appropriate agreement and authorization.

### HELP-001 — Maintenance and Helpdesk

**Source location:** attachment line 3557.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Ticket creation.
- Category and severity.
- Description and attachments.
- Emergency guidance.
- Assignment.
- Scheduling.
- Tenant entry consent.
- Status updates.
- Comments and communication.
- Expense estimates.
- Approval thresholds.
- Completion evidence.
- Reopening.
- Escalation.
- Feedback.
- Duplicate/recurring issue tracking.

A maintenance visit must not be treated as authorized entry without the necessary consent or other lawful authority.

### BOT-001 — AI Chatbot Assistance

**Source location:** attachment line 3582.

**Linked frontend screen contracts:** FS-25. Apply the linked frontend contracts and the specific rules below together.

Provide a clearly identified automated assistant for supported tenant/helpdesk questions.

Capabilities may include:

- Knowledge-base answers.
- Ticket categorization.
- Ticket drafting.
- Authorized ticket status lookup.
- Human support handoff.

Requirements:

- Enforce tenant and property data isolation.
- Use approved knowledge sources.
- Never invent payment or maintenance status.
- Require user confirmation before record changes.
- Prevent unauthorized tool execution.
- Avoid treating retrieved content as trusted instructions.
- Prevent autonomous agreement signing, financial approvals, evictions, or deposit deductions.
- Display appropriate emergency limitations.

Use an external model provider only when configured and authorized.

Provide a functional non-AI helpdesk fallback.

### NOTICE-001 — Notice, Renewal, and Move-Out

**Source location:** attachment line 3611.

**Linked frontend screen contracts:** FS-18; FS-14. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Notice submission.
- Configurable notice conditions.
- Acknowledgment.
- Notice date changes or withdrawal.
- Renewal offers.
- Revised tenancy terms.
- Agreement amendments.
- Move-out scheduling.
- Final inspection.
- Meter readings.
- Keys/access return.
- Final bills.
- Deposit settlement.
- Tenant handover.
- Financial closure.
- Unit availability update.

Represent notice, termination, physical possession handover, and financial closure as distinct states.

Do not imply that clicking a web action authorizes eviction, lockout, disconnection of utilities, or removal of belongings.

### OWNER-001 — Co-Owner and Property Manager Management

**Source location:** attachment line 3638.

**Linked frontend screen contracts:** FS-08. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Owner invitations.
- Property-specific ownership records.
- Manager invitations.
- Scoped access control.
- Delegation start/end dates.
- Permission changes.
- Revocation.
- Approval limits.
- Signing authority.
- Financial authority.
- Conflict escalation.
- Sensitive-action audit trails.

Revoked managers must lose server-side permissions immediately according to the session/authorization revocation design.

In-progress edits must fail safely if authority is removed before committing.

### DASH-001 — Role-Based Dashboards and Analytics

**Source location:** attachment line 3661.

**Linked frontend screen contracts:** FS-20. Apply the linked frontend contracts and the specific rules below together.

**Landlord Dashboard**
- Total properties and units.
- Occupied/vacant units.
- Occupancy rate.
- Expected and collected rent.
- Overdue balances.
- Deposits held.
- Expenses.
- Cash-flow indicators.
- Open tickets.
- Agreement expirations.
- Upcoming move-outs.

**Manager Dashboard**
- Assigned properties.
- Visit requests.
- Pending applications.
- Onboarding tasks.
- Due collections.
- Maintenance workload.
- Approval requests.
- Inspections and handovers.

**Tenant Dashboard**
- Active tenancy.
- Upcoming rent.
- Outstanding bills.
- Payment history.
- Deposit status.
- Agreement status.
- Maintenance tickets.
- Notices and renewals.

Define exact calculations, filters, time periods, access boundaries, and freshness for every metric.

Do not combine billed, reported, received, verified, disputed, and refunded amounts into misleading totals.

### SERVICE-001 — Nearby Services Directory

**Source location:** attachment line 3702.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

Implement:

- Service categories.
- Locality search.
- Optional geolocation.
- Distance and service area.
- Provider profile.
- Contact details.
- Call and direction actions.
- Favorites where in scope.
- Inaccurate-listing reporting.

Clearly distinguish recommended, promoted, and independently verified providers.

The baseline scope is provider discovery and contact.

Premium service requests, provider quotes/responses and explicitly agreed scheduling are included by SERVICE-PREM-001 through SERVICE-PREM-006. Automatic booking, provider payment and commissions remain separate conditional scope.

### NOTIFY-001 — Notifications and Documents

**Source location:** attachment line 3724.

**Linked frontend screen contracts:** FS-21; FS-22. Apply the linked frontend contracts and the specific rules below together.

Implement:

- In-app notifications.
- Email/SMS integration adapters.
- Event-based notification generation.
- Delivery status.
- Notification preferences.
- Deep links.
- Secure document access.
- Document versioning.
- Document download.
- Expiring access URLs.
- Access audit records for sensitive documents.

Use asynchronous processing for notification delivery where justified.

Ensure critical business transactions do not depend on successful delivery of an SMS or email.

### OWN-WF-001 — Property Onboarding and Readiness

**Source location:** attachment line 3789.

**Linked frontend screen contracts:** FS-07; FS-08. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner and scoped manager. **Phase:** 2.

**Flow:** Create property/address and hierarchy → record ownership and verified authority status → configure rentable items, meters, inventory and opening condition → configure rent/deposit/charge policies and authorized payee → assign manager if required → review readiness → activate operations.

Support draft, setup-in-progress, review-required, active, operationally-restricted, and archived property states. Keep readiness separate from unit occupancy and listing publication. Required configuration must be visible as a checklist; allow saving incomplete setup. Imported occupied properties use existing-tenant onboarding and audited opening balances rather than fabricated new move-ins.

**Acceptance:** Activation enforces configured prerequisites, never invents verified ownership, and cannot bypass workspace quotas. Archiving cannot erase active tenancies or unpaid obligations; an archive request reports blockers and safe alternatives. Sensitive ownership evidence remains private.

### OWN-WF-002 — Operating Policies and Delegated Authority

**Source location:** attachment line 3801.

**Linked frontend screen contracts:** FS-07; FS-08. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/co-owner and manager. **Phase:** 2; advanced configurable approval chains in 9–10.

**Flow:** Define property-level rent cycles, deposit rules, maintenance responsibilities, spending limits, allowed concessions, approval/signing authority, notice conditions, escalation contacts and publication permissions → review → activate an effective-dated version → notify affected authorized users.

Separate ownership percentage, operational authority, signing authority, financial approval authority and payout entitlement. Policies must state their scope and effective dates. A policy change cannot retroactively amend an executed agreement or issued invoice. Define required signers or decision rules for co-owned properties; do not infer unanimous, majority or unilateral authority from share percentage alone.

**Acceptance:** A manager within a recorded limit may act only within that category and scope; above-limit actions become pending approval. Self-approval is allowed only by explicit policy and authority. Conflicting owner decisions become a visible unresolved state, not last-write-wins.

### OWN-WF-003 — Owner Approval Inbox and Exception Decisions

**Source location:** attachment line 3813.

**Linked frontend screen contracts:** FS-07; FS-08. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/approver. **Phases:** 2–6 as associated features arrive.

**Flow:** Receive a request for an application exception, rent concession, agreement change, repair estimate, expense, waiver, deduction or refund → inspect amount/terms/evidence and current authority → approve, reject or request clarification → notify initiator → execute through the underlying authorized use case.

Show before/after values, request version, requester, affected property/tenant, reasons, attachments, decision deadline and resulting action. Approval is permission to execute a specific version, not proof of completed payment or work. Material changes invalidate prior approval. Revalidate permission and limits at execution; withdrawn, expired or revoked approvals cannot be used.

**Acceptance:** Concurrent decisions are handled deterministically; duplicate approval submissions produce one decision; a stale approval cannot authorize revised financial terms; approval alone never marks a refund paid.

### OWN-WF-004 — Portfolio Finance, Expenses, and Owner Statements

**Source location:** attachment line 3825.

**Linked frontend screen contracts:** FS-17; FS-20. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for records/statements; payment execution to vendors/owners is conditional. **Phases:** 5–6.

**Flow:** Review expected/verified collections and deposit liabilities → review expense evidence and allocation → approve expenses within authority → record verified payment references → generate property/period owner statement → investigate discrepancies and issue traceable adjustments.

Support expense category, vendor, property/unit or shared allocation, incurred date, amount/tax fields where applicable, invoice attachment, funding source, approval, reported/verified payment status and correction history. Record opening balances and management fees only from approved terms. Distinguish approved expense, paid expense, payable, rent revenue, platform charges, deposit liability and owner distribution; do not count deposits as income.

Owner distribution previews require explicit payout rules; ownership share alone is insufficient authority to transfer money. Actual bank payouts, commissions, vendor checkout and automated fee collection remain conditional integrations. Baseline manual records and statements must work without them.

**Acceptance:** Statement totals reconcile to source transactions and visible adjustments; unauthorized owners cannot view unrelated property finances; shared expense allocations sum correctly; paid records cannot be silently overwritten.

### OWN-WF-005 — Ownership Change, Delegation Exit, and Property Closure

**Source location:** attachment line 3839.

**Linked frontend screen contracts:** FS-08; FS-14; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; currently authorized owner and successor/manager within validated scope. **Phases:** 2 and 6.

**Flow:** Request authority change → review evidence, effective date, scope and outstanding obligations → approve by current authorized policy → assign successor and transfer tasks → notify affected users → revoke old access and retain historical attribution.

Handle manager exit, co-owner changes and property archival as distinct operations. Preserve agreements, consent, payments and audit authorship. Pending approvals and unsent jobs must revalidate the current authority; do not silently transfer an old signer’s authority to a new person. Payee/account changes require additional verification, audit and clear tenant notification; do not collect bank credentials in free-text notes.

A transfer between separate SaaS workspaces is a conditional controlled migration with explicit authorization, data mapping and retention rules; ordinary ownership editing must never move records across workspaces.

**Acceptance:** Revocation removes access at the server, including background work, exports and downloads according to the access design. In-flight provider results reconcile to their original records without regranting revoked access. Active tenant history remains available under valid permissions.

### MGR-WF-001 — Assignment Acceptance and Daily Operations

**Source location:** attachment line 3853.

**Linked frontend screen contracts:** FS-08; FS-14; FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; invited manager and delegating owner. **Phase:** 2.

**Flow:** Receive invitation → verify identity and accept scope → review properties, authority limits and opening handover → acknowledge discrepancies → activate delegation → use daily assigned work queue.

Display scope, start/end dates, unavailable actions, pending visits/applications, occupancy handovers, due bills/readings, maintenance, inspections and owner approvals. Permit reassignment only by authorized users; escalation does not broaden permissions. Support absence coverage through explicit time-bounded delegation rather than shared accounts.

**Acceptance:** An unaccepted/expired invitation grants no property access. One manager belonging to multiple workspaces sees correctly scoped queues and no cached cross-workspace data. Delegation expiry prevents new commitments and surfaces unassigned work.

### MGR-WF-002 — Vacancy Preparation and Leasing Operations

**Source location:** attachment line 3865.

**Linked frontend screen contracts:** FS-10; FS-11; FS-12; FS-13; FS-15. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped manager and approving owner. **Phases:** 2–4.

**Flow:** Inspect vacant unit → identify repair/cleaning/readiness tasks → verify inventory, pricing and media/tour completeness → obtain required publication approval → publish → respond to inquiries → schedule/complete appointments → review application completeness → request clarification → recommend/approve within authority → coordinate agreement and move-in.

Allow internal readiness notes separate from public listing details. Communicate current availability accurately and pause ineligible listings. A manager who may conduct visits cannot automatically approve an application, change rent or sign an agreement. Keep rejection/clarification reasons auditable and shared only with appropriate parties.

**Acceptance:** A unit blocked for maintenance cannot be advertised as immediately ready; competing holds/approvals obey capacity invariants; partial application/signature status does not imply occupancy; existing VISIT/APPT and TOUR implementations are reused.

### MGR-WF-003 — Monthly Billing and Collection Workbench

**Source location:** attachment line 3877.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped manager with financial permissions. **Phase:** 5.

**Flow:** Review due billing period → capture/validate meter readings → preview recurring and exceptional charges → resolve anomalies/required approvals → issue invoices → trigger permitted reminders → verify reported payments or reconcile provider results → allocate receipts → escalate arrears/disputes → publish period summary.

Record reading dates, evidence, previous readings, unit conversions/rollovers where configured, estimates and correction reasons. Flag missing/outlier readings and prevent duplicate billing runs. Show disputed amounts separately from collectible balances under configured policy. Manual collection records require method/reference, actual collection date, recorder and applicable verifier; never use a screenshot alone as verified provider payment.

**Acceptance:** Rerunning a period cannot issue duplicate invoices. Posted invoice corrections use adjustments/credits with history. A manager cannot waive charges, change payees, issue refunds or mark provider funds received without the required authority and evidence.

### MGR-WF-004 — Maintenance Work Orders and Preventive Inspections

**Source location:** attachment line 3889.

**Linked frontend screen contracts:** FS-08; FS-14; FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for basic maintenance/inspection workflow; advanced recurring automation/SLA by entitlement. **Phases:** 6 and 9.

**Flow:** Triage ticket or scheduled inspection → assess urgency → assign authorized worker/vendor → obtain estimate and spending approval → request entry permission/schedule → perform work and capture evidence → verify cost/completion → obtain tenant feedback/acknowledgment → close or reopen.

Add preventive inspection schedules for configured assets/common areas, recurring defects, inventory condition updates, warranty/service history where recorded, and owner escalation. Inspection scope, notice requirements, entry consent and exceptions must be explicit; a recurring schedule is not permission to enter an occupied unit. Tenant acknowledgment, manager verification and vendor completion are separate facts; allow a disputed completion state and a documented closure policy.

**Acceptance:** Workers receive only their assigned job details and necessary contact/access instructions; estimates above delegated limits cannot be committed without approval; rejection/no response to entry requests follows a visible rescheduling/escalation path; emergency guidance does not imply unauthorized entry rights.

### MGR-WF-005 — Handover, Move-Out, and Turnover Coordination

**Source location:** attachment line 3901.

**Linked frontend screen contracts:** FS-14; FS-18; FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped manager, authorized owner and affected tenant. **Phases:** 4 and 6.

**Flow:** Schedule move-in/out → prepare checklist and participants → capture condition/inventory/meter evidence → record keys/cards and acknowledgments/disputes → separate physical handover from finance → prepare settlement/approvals → coordinate repairs/cleaning → inspect readiness → release/relist eligible capacity.

Provide an outstanding-items checklist for missing keys, damaged inventory, open tickets, unbilled utilities, disputed deductions, unpaid dues and pending refunds. Unpaid balance alone must not automatically prevent a factual physical handover record. Actual unit release follows possession and availability rules, not an unverified refund or approval event.

**Acceptance:** Evidence remains attributed/versioned; disputed items remain visible; unit availability cannot overlap old/new occupancy; a closed handover does not falsely close unsettled finances; new listing publication obeys owner authorization and readiness.

### TEN-WF-001 — Account Linking and Existing-Tenancy Access

**Source location:** attachment line 3913.

**Linked frontend screen contracts:** FS-01; FS-02; FS-13. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; applicant/rental tenant and authorized inviter. **Phases:** 1, 3 and 4.

**Flow:** Register/verify contact → browse/apply or accept a tenancy invitation → verify invited identity/relationship → review tenant/occupant role and authorized records → activate portal access → manage multiple authorized tenancies through explicit context selection.

Do not grant tenancy access merely because a typed phone/email matches a record. Use authenticated, expiring, single-purpose invitations or an authorized recovery/relinking flow; handle duplicate accounts and changed numbers without exposing another person’s records. Existing tenants may be onboarded without a new public application, but must review imported terms and opening balances, with disputed/unconfirmed values marked explicitly.

**Acceptance:** Forwarded/expired/consumed invitations cannot grant unintended access. Primary tenant, co-tenant, guarantor and authorized occupant have distinct permissions. Co-tenants do not automatically receive each other's identity documents or personal subscriptions.

### TEN-WF-002 — Search-to-Application Journey

**Source location:** attachment line 3925.

**Linked frontend screen contracts:** FS-10; FS-11; FS-12; FS-13. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; prospective tenant/applicant. **Phase:** 3.

**Flow:** Search/filter → inspect property details, disclosed charges and tour → book/manage viewing → shortlist → apply → save/resume and share required documents with consent → answer clarification → receive approval/rejection/withdrawal status → review offered terms.

Show which actions are requests, confirmed appointments, temporary holds and approved applications. Present missing/expired documents, hold expiry, application status history, changes to advertised/offered terms and clear next steps. Allow withdrawal and contact-preference changes. Explain when another applicant takes the unit without leaking their identity.

**Acceptance:** Free users can complete ordinary discovery/application without personal Premium. An approved application cannot auto-sign an agreement or trigger a payment without the appropriate separate action. Retry and network recovery preserve drafts and avoid duplicate submissions.

### TEN-WF-003 — Agreement Review and Move-In Acceptance

**Source location:** attachment line 3937.

**Linked frontend screen contracts:** FS-15; FS-14; FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized tenant/signatory and occupants as permitted. **Phase:** 4.

**Flow:** Review parties, unit, dates, rent/deposit/charges and responsibilities → request corrections → accept/sign the appropriate immutable version → view separate stamping/registration progress → review required payments → schedule handover → inspect inventory/readings/condition → acknowledge or dispute → record actual move-in.

Allow partial acknowledgment with disputed items rather than forcing a false all-items acceptance. Make the latest active agreement and previous signed versions accessible under retention policy. Do not force tenants to treat bank transfer reporting as verified receipt, or signing as proof of possession.

**Acceptance:** Agreement amendments invalidate affected signature requests appropriately; the tenant sees the version they signed; disputes and evidence survive activation; UI distinguishes contract start, signature completion, possession and financial readiness.

### TEN-WF-004 — Monthly Dues, Payments, and Charge Disputes

**Source location:** attachment line 3949.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant/co-tenant within assigned financial scope. **Phase:** 5.

**Flow:** Receive invoice → inspect charge breakdown/readings/adjustments → pay through supported provider or report manual payment → see pending/verified/failed status → receive receipt/allocation → review statement → raise dispute or request correction with evidence.

Show partial/advance payments, credits, deposits separately, payment allocation, due dates and refund status. If split household obligations are configured, show each person's authorized allocation; do not assume co-tenants owe equal shares or expose private payer details. Notify users of independently verified payee changes. Avoid duplicate payment requests during uncertain provider outcomes.

**Acceptance:** Tenant-reported payment does not mark an invoice paid until verified; a dispute persists and follows acknowledgment/review/resolution states; the tenant can view reasons and adjustments. Premium expiry cannot disable payment, receipt download or dispute access.

### TEN-WF-005 — Living in the Property and Service Communication

**Source location:** attachment line 3961.

**Linked frontend screen contracts:** FS-19; FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; active tenant/authorized occupant within scope. **Phase:** 6.

**Flow:** Report issue/request with evidence → track assignment and scheduled work → grant/decline/propose entry window → exchange comments → review completion → acknowledge/reopen/dispute. Receive relevant property announcements, inspection requests and operational contacts through the portal.

Add property-scoped announcements with author authority, audience, effective/expiry dates and delivery/read status. Do not expose occupant contact lists to other residents. Read receipt is not legal acceptance, consent to entry or agreement amendment. Support requests to correct contact details, update authorized occupants, or initiate a co-tenant replacement/exit through review; these cannot directly rewrite executed tenancy obligations.

**Acceptance:** Tenant can decline a proposed appointment/entry time and see the next action; emergencies receive clear guidance without a false instant-resolution promise; a landlord's downgrade does not erase open tickets, essential announcements or contact instructions.

### TEN-WF-006 — Renewal, Notice, Move-Out, and Aftercare

**Source location:** attachment line 3973.

**Linked frontend screen contracts:** FS-18; FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant and authorized owner/manager. **Phase:** 6.

**Flow:** Receive renewal offer → accept/reject/request revised terms or submit notice → review acknowledged effective dates → request changes/withdrawal where permitted → schedule inspection/handover → review final readings/charges/deductions → dispute or accept settlement → verify refund recipient → return keys and record possession handover → track refund/payment completion → retain authorized records.

Explicitly track offered/accepted renewal terms and the associated new agreement version. A tenant notice request must not automatically become an eviction instruction. After physical departure, show remaining financial obligations, dispute tasks and refund status without representing the tenant as still physically occupying the unit. Provide secure historical receipts/agreements and account privacy/export requests under retention policy.

**Acceptance:** Notice changes revalidate scheduling and billing; no deposit deduction is silently accepted; refunds reconcile independently of move-out; co-tenant departure does not automatically terminate everyone’s tenancy; former tenants retain only authorized historical access.

### PM-DASH-001 — Portfolio Dashboard, Cards, and Layout

**Source location:** attachment line 4019.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; workspace owner, authorized portfolio admin and scoped property manager. **Phases:** 2, 4–6 as source workflows become available.

**Frontend:** `/workspace/dashboard` is the shared landing screen with role-filtered content, not a second competing dashboard. Provide:

1. A persistent filter bar with timeframe, custom dates, property/building, manager, and relevant status filters.
2. Portfolio cards: total managed properties, total rentable units/beds, occupied capacity, vacant capacity, and active rental tenants.
3. Rent cards: billed rent due in period, number of tenants with unpaid rent due in period, verified rent collected in period, payments allocated to the period's rent, remaining collectible period rent, and overdue rent as of the cutoff.
4. Request cards: issues/requests raised, distinct tenants raising them, currently open requests, overdue service tasks, and resolved requests.
5. Tenant lifecycle panels: latest onboarded, upcoming onboarding, latest offboarded, and upcoming offboarding.
6. Action queues: overdue collections, pending payment verification, unresolved disputes, required approvals, urgent maintenance, missing onboarding prerequisites and overdue move-outs.
7. Property-wise summary table and optional trend charts backed by the same metric definitions.

Show currency as INR with appropriate formatting, counts as counts, and clear labels for mixed time bases. Include loading, empty, partial-data, unauthorized, stale and failed states. An unavailable total is “Unavailable,” never a misleading zero. On mobile, retain readable cards and accessible filters with card/list alternatives to wide tables.

**Acceptance:** A user can identify a portfolio issue, drill into the contributing records, perform an authorized action, and return to the same filtered view. Managers see only assigned scope; the dashboard cannot expose hidden property totals through aggregate counts.

### PM-DASH-002 — Shared Timeframe and Date Filters

**Source location:** attachment line 4039.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; all dashboard users. **Phases:** 2 and 5–6.

Provide a dropdown with **This month**, **Last month**, **Last 3 months**, **Last 6 months**, **This year**, and **Custom range**, plus explicit **From date** and **To date** fields. Default to This month. Presets must display their resolved dates so their meaning is unambiguous.

Use these initial, configurable presentation definitions:

| Selection | Start | End |
|---|---|---|
| This month | First day of current calendar month | Today |
| Last month | First day of previous calendar month | Last day of previous calendar month |
| Last 3 months | First day of the month two months before current month | Today |
| Last 6 months | First day of the month five months before current month | Today |
| This year | January 1 of current calendar year | Today |
| Custom range | Selected From date | Selected To date |

Label the trailing presets “including current month” in helper text. The KPI “rent due this month” must include all issued rent with due dates in the full current calendar month, including upcoming due dates. When the This month filter resolves to month-to-date, display that full-month due KPI as a separately labeled **Full-month due / remaining** companion; never silently include future dues in a month-to-date total. The primary filtered due metric follows the selected interval. Distinguish financial year from calendar year; a future financial-year preset requires an explicit label and definition.

Interpret dates in the workspace timezone, initially Asia/Kolkata. Treat the selected end date as inclusive in the UI and convert timestamp queries into `[start-of-from-date, start-of-day-after-to-date)` instants. Due dates and billing dates that are local dates must retain their local-date semantics. Validate required dates, From ≤ To, timezone and documented maximum reporting range. Do not silently truncate ranges.

Store filters in URL query parameters for shareable/reload-safe views; validate them server-side and never use a URL property ID as authority. Apply committed filters consistently to cards, charts, tables and exports. Provide Apply and Reset controls and announce filter changes accessibly. Cancel or ignore stale in-flight responses when users change filters quickly.

Portfolio and open-balance cards are **as-of** snapshots at the earlier of selected To date and now; flow metrics use the selected interval. Future ranges may show scheduled events/projected dues where available, explicitly marked as forecasts, but must never imply future actual collections or occupancy. Historical snapshots require effective-dated records/events; if unavailable before migration cutover, show the coverage limitation instead of substituting current data.

**Acceptance:** All visible dates and cutoffs are returned by the backend. Boundary-day, year rollover, leap day, timezone, invalid-range and rapid-filter-change tests pass. A last-month report must not show current occupancy as if it were last month's occupancy.

### PM-DASH-003 — Authoritative Portfolio, Tenant, and Rent Metrics

**Source location:** attachment line 4068.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized operational/financial viewers. **Phases:** 2, 4–6.

Implement a shared metric/query service and document its formulas. Default all figures to authorized properties within the selected workspace; optional property/manager filters can only narrow that scope. Prevent fan-out joins from multiplying invoices, payments, occupants or tickets.

| Metric | Definition / required distinction |
|---|---|
| Total properties | Distinct non-archived managed property records effective at the snapshot cutoff; show draft/inactive breakdown separately. Do not count buildings/floors as properties. |
| Total rentable capacity | Independently rentable units or beds under the established inventory model at cutoff. Label units and beds separately; do not count both a shared room and its beds as additive capacity. This is an operational count, distinct from the SaaS billable quota count. |
| Occupied / vacant capacity | Capacity with active possession/allocation at cutoff versus eligible unallocated capacity. Held, repair-blocked and otherwise unavailable capacity are separate categories; occupied plus vacant need not equal total without those categories. |
| Total active rental tenants | Distinct person identities with effective primary-tenant/co-tenant relationships and active possession at cutoff. Deduplicate a person across multiple authorized tenancies. Show authorized occupants separately; applicants and future scheduled tenants are excluded. |
| Tenants whose rent falls due in period | Distinct contracting tenant identities responsible under the recorded obligation model for posted, non-void rent charges due inside the selected interval. Label this as all due tenants, regardless of whether paid. |
| Tenants with unpaid rent due in period | Distinct responsible tenant identities with a positive remaining rent obligation for charges due in the selected interval at cutoff. Count people once; provide a separate affected-tenancy count. Do not infer equal household liability or count a merely authorized occupant as owing rent. For joint liability, one joint unpaid tenancy can contribute multiple responsible people but its amount is counted once. |
| Billed rent due in period | Sum of posted rent charge components whose due date is within the interval, reduced by applicable posted credits/waivers and corrected through the authoritative ledger. Exclude utilities, deposits, fees and SaaS/consumer membership charges; expose them as separate breakdowns. |
| Verified rent collected in period | Gross verified rental receipts attributed/allocated to rent with received/settled time inside the interval, regardless of invoice due period. Show rent refunds/reversals in the interval separately and define net rent collected = gross receipts − those refunds/reversals. Exclude pending, failed, unverified manual reports, deposits, utilities and platform subscription receipts. Unallocated receipts appear separately until their purpose/allocation is established. |
| Rent paid toward selected-period dues | Verified rent payment/advance allocations applied to the selected due-date cohort by cutoff, less reversed allocations. This can include money received before the selected period. It is distinct from collections received in the period. |
| Rent still to collect for period | Sum of positive remaining balances for the selected rent-due cohort after effective credits/waivers and verified allocations at cutoff. Apply rounding at documented ledger precision. Surplus/advance credit is shown separately rather than as negative “rent left.” |
| Total overdue rent as of cutoff | Positive remaining rent balances whose due date has passed at cutoff, including older periods. Same-day dues are “due today” until the configured end-of-day rule; any contractual grace policy must be explicit. Split disputed and undisputed amounts without erasing either from the ledger. |

Never calculate “rent left” by subtracting cash received in the selected period from rent billed in that period: receipts may settle old dues or represent advances. Derive remaining amounts from invoice/charge allocations, and show the reconciliation bridge between net billed obligations, allocations, outstanding and unapplied credit. Refunds and payment reversals reopen obligations only according to their recorded allocation/adjustment effect, not because every outgoing refund must create new debt.

Use confirmed provider effective timestamps and verified manual payment dates; record verification time separately. A receipt verified later can legitimately restate a historical operational report. Include report-generation time, cutoff and applicable source version; use immutable report snapshots when a reproducible issued statement is needed.

**Acceptance:** Totals reconcile to source-ledger drill-downs for split invoices, co-tenants, partial payments, advance payments, old-debt receipts, discounts, disputes, refunds and reversals. No payment or joint obligation is double-counted.

### PM-DASH-004 — Issues, Requests, and Tenant Service Dashboard

**Source location:** attachment line 4096.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped operational users. **Phase:** 6; advanced SLA automation follows plan entitlements.

Show total issues/requests created in the selected interval, distinct tenant requesters, category/severity distribution, open backlog at cutoff, requests resolved in the interval, reopened requests, and overdue tasks under an actual configured target. Distinguish maintenance tickets, service/administrative requests, billing disputes and occupant-change requests by source and category; preserve links to the authoritative workflow rather than copying them into a parallel ticket system.

Count “tenants who raised an issue” from recorded requester person IDs, deduplicated across included categories. Manager-created requests on behalf of a tenant must retain both creator and affected/requesting tenant; do not attribute unrelated manager inspections to a tenant. Clearly identify unlinked/anonymous records rather than inventing person identities. Define whether reopened requests count as a new request: baseline counts the original case once and reopening as an event.

Provide filters for property, tenant, issue type, severity, status, assignee, created date and target date. Cards for open backlog are snapshots, while created/resolved counts are flows and must be labeled accordingly. Do not fabricate SLA breaches where no target exists.

**Acceptance:** One tenant submitting three tickets contributes three requests and one distinct tenant. A case created earlier but still open is present in backlog without increasing this period's created count. A reopened case is not simultaneously counted as currently resolved; historical resolution events remain auditable.

### PM-DASH-005 — Recent and Upcoming Tenant Onboarding/Offboarding

**Source location:** attachment line 4110.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and scoped support. **Phases:** 3–4 and 6.

Provide four separately labeled panels, each with sortable, paginated View all:

- **Latest onboarded:** Actual completed move-ins/possession activations inside the selected interval, newest first. Display person/household, property/unit/bed, actual move-in date, manager and remaining onboarding exceptions. Imported historical tenants retain their original move-in date and a separate import date; importing an old tenancy does not fabricate a new move-in.
- **Upcoming onboarding:** Approved/scheduled move-ins within an explicit planning horizon, including planned date, tenant/application, property/unit, readiness, agreement state, required payment verification, handover appointment and blocking tasks. Approved application, expected arrival and confirmed possession remain distinct. Include “Needs schedule” and “Overdue planned move-in” views.
- **Latest offboarded:** Actual possession handovers completed inside the selected interval, newest first. Show actual exit date, property/unit, outstanding rent, deposit/refund status and financial closure separately. Co-tenant exit and whole-tenancy exit must be distinguishable.
- **Upcoming offboarding:** Acknowledged notice/planned exits within the planning horizon with expected date, notice status, inspection, key return, final readings, settlement preparation, refund review and successor readiness. Withdrawn or cancelled notices leave the active plan while retaining audit history.

The global reporting filter governs completed-event panels. Upcoming panels use a visibly separate **Planning horizon** dropdown: Next 7 days, Next 30 days (default), Next 90 days, or Custom dates. Display its resolved dates beside the panel; do not silently overload the historical reporting range. Preserve the planning filter in the URL and in exports. Planned events are not actual counts and must not inflate active tenant totals. Include delayed/cancelled events and missing-date items through explicit views rather than dropping them silently.

Rows expose only necessary personal information and offer authorized actions: inspect prerequisites, schedule/reschedule, request an approval, view the tenant record, open handover, or track settlement. Creating a task or sending a reminder is not onboarding/offboarding completion.

**Acceptance:** A future scheduled move-in appears only in upcoming views, not as an active tenant; a completed physical exit appears offboarded even with a pending refund; notice withdrawal and rescheduled dates update panels without duplicate events. Tenant-level counts deduplicate people while event/tenancy counts are labeled separately.

### PM-DASH-006 — Collection and Request Drill-Down Action Queues

**Source location:** attachment line 4129.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/admin/manager. **Phases:** 5–6.

Clicking a card opens a filtered, paginated detail view preserving timeframe, as-of cutoff and property scope. Collection rows show tenant/tenancy, property/unit, invoice/period, due date, net rent due, verified allocations, outstanding, disputed amount, days overdue and next follow-up. Group joint obligations to avoid misleading repeated amounts when multiple people appear.

Provide actions only when permitted: view invoice/statement, send an approved reminder, review manual payment evidence, record a verified allocation through the payment workflow, request a waiver/adjustment, review dispute, or escalate. Opening a dashboard must not trigger collections, message sending or financial mutations. Bulk actions require selected recipients/items, preview, permission revalidation, deduplication, channel consent/quota checks, confirmation and per-item result reporting.

Request queues support authorized assignment, scheduling, clarification and escalation; onboarding/offboarding queues route to their existing workflows. Sensitive actions require the applicable approval and cannot be performed by editing a KPI or aggregate total.

**Acceptance:** A successful mutation refreshes affected cards/rows without changing filters. Concurrent payment confirmation suppresses an obsolete collection action at commit. Bulk partial failure reports each outcome and safely retries failed items without duplicating successful actions.

### PM-DASH-007 — Trends, Property Summary, Export, and Reporting Quality

**Source location:** attachment line 4143.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for summary and basic export; advanced analytics by plan. **Phases:** 5–6; enhanced analytics in 9–10.

Provide a property-wise table with authorized property/unit/tenant counts, billed rent due, period receipts, rent allocated to period dues, remaining period rent, overdue rent, open requests, completed move-ins/outs and planned events in the separately labeled horizon. Support server-side sorting/pagination and View property actions. State that person counts across rows are not additive when the same person has multiple tenancies; the portfolio distinct total must be calculated independently.

Charts may show billed rent, collected rent and outstanding trends using clearly labeled date bases, plus request volume and move-in/out trends. If comparison is enabled, state whether it uses previous equal-length dates or corresponding calendar period; show both date ranges and avoid percentage change when the baseline is zero. Chart tables must provide an accessible equivalent.

Export permitted detail/summary data with timezone, report dates, planning horizon, currency, metric definitions, cutoff/generated time and scope. Protect exports against spreadsheet formula injection, enforce size limits or background generation, audit access, and use expiring authorized download links. Revalidate permissions at job execution and download; hide sensitive fields by role. Aggregates and exported detail must use a consistent snapshot or disclose source freshness differences.

**Acceptance:** Exported amounts/counts reconcile with the filtered dashboard at the stated snapshot; unauthorized properties are absent even from totals. Pagination does not change grand totals. Archived/deleted-history rules and opening-balance cutover limitations are explicit. Query/index performance is verified on representative portfolio size.

### PM-DASH-008 — Backend Contracts, Security, and Data Freshness

**Source location:** attachment line 4157.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; implementation requirement. **Phases:** 2, 4–6.

Reporting screens submit validated reporting dates/timezone, planning dates, authorized property/manager filters, pagination and allowlisted sorting. Arbitrary client-defined metric expressions are not supported.

Display resolved intervals, cutoff, generated-at time, currency, metric-definition version, authorized scope, coverage warnings and section availability. Totals cover all matching authorized records, not only the loaded page. Do not permit editing computed totals; show freshness/reconciliation limitations.

Scope cached reporting data by workspace, effective permission, filters, metric version and snapshot. Refresh after relevant payments/reversals, tenancy events, request changes or permission changes. Polling/live indicators must not imply stronger freshness than actually provided.

**Acceptance:** Modified workspace/property/manager IDs, revoked access and cached responses cannot disclose unauthorized aggregates or tenant information. Simultaneous report reads and financial mutations produce a consistent snapshot or a clear freshness marker, never undocumented mixed totals.

### PM-DASH-009 — Decision Overview and Interactive Chart Workspace

**Source location:** attachment line 4171.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized property admin/owner and scoped managers. **Phases:** 5–6 for core charts; advanced analytics in 9–10.

Add an Analytics section to the existing dashboard, organized into Collections, Occupancy, Leasing, Maintenance, Property Performance, and Planning. Prefer a focused overview with drill-down tabs rather than loading every chart at once. Provide line/area charts for trends, bars for comparisons, stacked bars for distributions, a leasing funnel, and operational calendar/heatmap views where appropriate. Use a donut only for a small part-to-whole breakdown; do not use decorative 3D charts or dual axes that obscure scale.

Each visual must show title, unit/currency, date basis, interval/granularity, applicable filters, as-of/generated time, legend, denominator or sample size where relevant, and a short “What this helps decide” description. Tooltips must explain exact values. Include accessible tabular data and keyboard-operable drill-down. Use consistent colors for verified, pending, overdue, disputed and forecast data, with text/pattern cues in addition to color.

Support daily, weekly and monthly granularity appropriate to the selected range, server-side aggregation, synchronized property/manager filters, optional property comparison, and a clear Reset. Persist personal chart/filter preferences without granting additional permissions. Empty, incomplete, zero and unavailable data are distinct states. Disable misleading comparisons where source coverage differs.

**Acceptance:** Clicking a chart point/bar opens the contributing records with the exact scope and date basis. Chart and table totals reconcile. Graph rendering remains usable on mobile, with reduced-motion and screen-reader support. Viewing a chart never triggers business mutations.

### PM-DASH-010 — Collections Trend, Collection Efficiency, and Arrears Aging

**Source location:** attachment line 4185.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized financial viewers. **Phases:** 5–6.

**Visuals:** Monthly/daily rent collection line or grouped bar chart; due-cohort collection progress; overdue aging stacked bars by property; top outstanding-tenancy table; payment-method mix for verified receipts. Show rent refunds/reversals separately and offer gross/net collection views with clear labels.

**Metrics and calculations:**

- **Due-cohort collection rate:** Verified net rent allocations applied to obligations due in the selected interval ÷ net rent obligations in that same cohort × 100, evaluated at cutoff. Cap applied allocations per obligation at its collectible amount; show excess/unallocated credit separately. A zero denominator is N/A, not 100%.
- **On-time collection rate:** Rent allocations effective no later than the applicable due deadline ÷ net obligations in the selected due cohort whose due deadline has elapsed × 100. Do not include not-yet-due obligations in the denominator; use only the policy's explicitly defined grace treatment.
- **Arrears aging:** Remaining overdue rent grouped by 1–7, 8–30, 31–60, 61–90 and over 90 days past due at cutoff. Due-today and future dues are separate. Split disputed versus undisputed amounts; the bins must reconcile to overdue rent.
- **Overdue tenancy rate:** Tenancies with positive overdue rent ÷ tenancies with elapsed rent obligations at cutoff. Provide people count separately; do not call this a tenant credit score.
- **Days to collect:** Median and 90th percentile of days between due date and settlement for fully settled obligations in the defined cohort. Show sample size and unresolved count; excluding unpaid invoices must be disclosed. Do not silently treat partial payment as full settlement.
- **Pending verification exposure:** Amount and count of unverified manual reports or uncertain provider payments, excluded from verified collections.

**Decision actions:** Inspect old arrears, prioritize approved follow-ups, resolve allocation discrepancies, and identify properties needing payment verification. Recommendations must link to invoices and respect disputes/consent; they cannot automatically impose fees or collect funds.

**Acceptance:** Old-debt receipts and advances cannot inflate current-cohort collection rates. Refunds, waivers, joint obligations and partial allocations reconcile without double counting. Aging bins sum exactly to the selected overdue total.

### PM-DASH-011 — Occupancy, Vacancy Duration, and Capacity Utilization

**Source location:** attachment line 4206.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized operational viewers. **Phases:** 4–6.

**Visuals:** Occupancy trend, property occupancy comparison, occupied/vacant/held/repair-blocked capacity stacked bars, unit/bed availability calendar, and vacancy-duration distribution. Provide counts and percentages together.

**Metrics and calculations:**

- **Snapshot occupancy rate:** In-possession rentable capacity ÷ total in-service rentable capacity at cutoff. Total in-service capacity includes vacant, held and repair-blocked capacity; show those components. Exclude only capacity explicitly removed from service for the relevant period and disclose exclusions.
- **Period occupancy rate:** Occupied capacity-days ÷ in-service capacity-days over the interval. Use effective-dated allocation/possession and inventory changes, not an average of today's property percentages.
- **Available-to-let capacity:** Unallocated capacity that passes operational readiness/publication eligibility; distinguish future scheduled availability.
- **Current vacancy age:** Days since the last possession handover or initial ready-for-letting date, using a stated convention. Record maintenance-blocked days separately rather than resetting the vacancy clock on listing edits.
- **Time to re-let:** Days from prior possession handover to next actual possession start for completed turnovers; show median, sample size and currently unfilled units separately.
- **Illustrative vacancy revenue opportunity:** Ready-but-vacant capacity-days × approved effective-dated daily asking/base rent under a documented calculation. Label as an estimate, exclude missing-price items visibly, and never book it as receivable, revenue or a guaranteed loss.

Use units and separately rentable beds consistently; never count both. Portfolio rates use summed numerators/denominators, not an unweighted average of property rates. Unknown historic availability produces a coverage warning.

**Decision actions:** Investigate prolonged vacancies, unblock repair/readiness tasks, improve listing completeness and coordinate upcoming turnover. Do not automatically reduce rent or release occupied capacity.

**Acceptance:** New/retired inventory, shared beds, repair restrictions, temporary relocation and delayed exits yield correct denominators and no occupancy above physical capacity. A price estimate never contaminates the rental ledger.

### PM-DASH-012 — Leasing Funnel and Upcoming Lifecycle Workload

**Source location:** attachment line 4229.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for stage counts/workload; cohort conversion analytics Should, delivered in Phase 9. **Phases:** 3–6 and 9.

**Visuals:** Inquiry → confirmed visit → completed visit → submitted application → approved application → executed agreement → actual move-in funnel; weekly onboarding/offboarding workload bars; agreement-expiry calendar and renewal outcome chart.

Separate **stage activity in the interval** from **conversion of an acquisition cohort**. A stage-activity funnel is not a conversion rate because stages may refer to different people/time periods. For conversion, choose and display a cohort anchor such as first inquiry date, identity/deduplication rule, observation cutoff and eligible lead set. Direct applications/existing-tenant imports bypassing stages must appear separately, not as impossible conversion above 100%.

Show stage counts, confirmed-visit no-show rate, median time between eligible stages, pending approvals, missing onboarding prerequisites, scheduled handovers, overdue planned arrivals/exits and agreement expirations in the planning horizon. An executed agreement is not an actual move-in. Scheduled exits are not guaranteed vacancy.

**Renewal metrics:** Offers sent, pending responses, accepted/declined offers and completed renewals. Acceptance rate = accepted offers ÷ decided offers, with undecided count shown. Completed renewal rate uses a disclosed eligible tenancy cohort and authoritative renewed agreement/effective-term event; do not count an offer alone as a renewal.

**Decision actions:** Resolve application bottlenecks, assign visit/handover coverage, prepare renewal offers and coordinate expected turnover. Any recommended staffing counts must reflect recorded tasks and capacity assumptions, not invented staffing guarantees.

**Acceptance:** Repeated visits/applications and co-applicants follow a consistent deduplication model; withdrawn applications and late conversions remain traceable; imports do not inflate marketing success; historical reports distinguish complete from still-maturing cohorts.

### PM-DASH-013 — Maintenance Demand, Resolution Performance, and Recurring Problems

**Source location:** attachment line 4247.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for volume/backlog charts; advanced target/SLA analytics by workspace entitlement. **Phases:** 6 and 9.

**Visuals:** Created-versus-resolved request trends; backlog aging by severity; issue-category bars; property/category heatmap with accessible table; assignment workload; repair cost trend; recurring-asset problem list.

**Metrics and calculations:**

- **First response time:** Request creation to first qualifying human/service response, excluding automatic acknowledgments. Display median and 90th percentile, sample size and unanswered cases.
- **Resolution time:** Creation to recorded resolution; define how reopening affects the final-resolution measure. Show unresolved aging separately. If business-hour clocks or paused states are used, disclose calendar, pause rules and both elapsed and measured durations.
- **Target compliance:** Requests meeting the applicable configured response/resolution target ÷ eligible requests whose target evaluation is due, with unresolved breached cases included and missing-target cases excluded visibly.
- **Reopen rate:** Distinct resolved cases subsequently reopened within a specified observation window ÷ resolved cases with sufficient observation time. Show immature cases separately.
- **Recurring incidents:** Cases linked to the same asset/unit and issue category within a configured window; treat this as a review signal, not proof of contractor fault.
- **Repair cost:** Approved incurred expenses and verified paid amounts shown separately, linked to work orders. Cost per occupied unit/bed-month uses a disclosed denominator and avoids double-counting shared expense allocations.

Break workload down by assignment, property and severity while respecting manager/vendor access. Do not rank workers by speed alone when severity, access delays or assignment complexity differ. Priority tenant subscriptions cannot alter emergency triage or fabricate service targets.

**Decision actions:** Reassign overloaded queues within authority, approve stalled repairs, identify preventive maintenance opportunities and investigate recurring failures. Notifications and recommendations do not authorize entry or expenditure.

**Acceptance:** Reopened cases, missing targets, unanswered requests, paused access and bulk duplicates cannot falsely improve performance. Costs reconcile to approved expense records; estimates are not reported as paid repairs.

### PM-DASH-014 — Property Operating Performance and Expense Analysis

**Source location:** attachment line 4270.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for revenue/expense breakdown and source reconciliation; advanced comparative analytics by plan. **Phases:** 5–6 and 9–10.

**Visuals:** Property financial comparison bars, rent-receipt-to-operating-cash-surplus waterfall, expense category breakdown, budget-versus-actual chart where approved budgets exist, and occupancy/collections/maintenance comparison table.

Define two separate reporting bases where supported:

- **Operating cash surplus:** Verified rent cash receipts and other explicitly included property operating receipts, less refunds and verified paid operating expenses in the interval. Show included categories and treatment of taxes/fees. Exclude deposits, owner capital contributions/distributions, loan proceeds/principal and asset sale proceeds.
- **Accrual operating result:** Recognized operating revenue less incurred operating expenses, only when a validated recognition/accrual model exists. Do not label billed invoices minus cash expenses as profit or NOI.

Do not present full net profit, investment return, cap rate or ROI without the required acquisition values, capital expenditure, debt, taxes, valuation dates and approved accounting definitions. Use “operating cash surplus” for the baseline, not an unsupported profitability claim.

For expense variance, compare actual amounts on the selected cash/incurred basis with approved effective-period budgets. Show absolute variance and percentage only with a nonzero budget. Missing budgets are N/A. Shared costs require a documented allocation method and reconcile back to the original expense once.

Add **effective rent per occupied capacity-month** using net rent obligations attributed to the service period ÷ occupied capacity-months on a documented day-weighted basis. Do not equate this with asking rent, period cash receipts or investment yield. Exclude or flag incomplete service-period data.

**Decision actions:** Review high operating costs, investigate low collections relative to occupancy, examine repeated repairs and compare like-for-like properties. Show property size, unit type, data coverage and sample context rather than claiming a fair ranking from raw totals alone.

**Acceptance:** Deposits and platform membership revenue never inflate rental performance; expense allocations reconcile; cash and accrual bases cannot be mixed silently; absent budgets or denominators do not produce fabricated percentages.

### PM-DASH-015 — Forecasts, Scenario Planning, and Decision Alerts

**Source location:** attachment line 4293.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for factual operational alerts; forecasts and scenarios Should for entitled advanced analytics. **Phases:** 6 for alerts, 9–10 for forecasts.

Provide rule-based alerts for approaching agreement expiry, prolonged vacancy, overdue balances, unresolved high-severity requests, missing readings, incomplete handovers and stalled approvals. Rules have owner, scope, threshold, schedule, severity, deduplication/cooldown and acknowledgment/snooze/resolution history. An alert must link to its evidence and distinguish a rule breach from a prediction. Seed only clearly labeled configurable defaults; do not invent universal commercial thresholds.

For entitled forecasts, show the next 30/60/90 days of contracted scheduled rent, expected approved expense commitments, planned occupancy changes and expected deposit refunds as separate components. Contracted dues are not a promise of cash receipt; scenarios may apply user-selected collection assumptions and must display those assumptions. Separate confirmed records, tentative plans and estimates. Unknown future expenses must not be represented as zero cost.

Allow view-only what-if adjustments to vacancy duration, approved future rent assumptions, collection timing and planned repairs. Compare scenarios with the baseline; no scenario edits may alter invoices, agreements, actual rent schedules, unit allocations or payment records. Record saved scenario owner/version and refresh against current permissions.

If statistical predictions are introduced, document training/data window, model version, validation error, uncertainty and limitations. A simple schedule projection must not be presented as AI-predicted certainty. Do not generate opaque tenant creditworthiness or eviction recommendations from dashboard activity.

**Acceptance:** Alerts do not spam on every refresh or send unauthorized reminders. Forecasts are labeled throughout charts/exports; actual and estimated series remain separate; users can inspect assumptions and source events; stale or incomplete input produces warnings rather than confident recommendations.

### PM-DASH-016 — Metric Governance, Comparisons, and Decision Auditability

**Source location:** attachment line 4309.

**Linked frontend screen contracts:** FS-20; FS-34. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; reporting implementation and authorized metric administrators. **Phases:** 0, 5–6 and 9–10.

Maintain a versioned metric catalog containing code, business question, definition/formula, source fields/events, date basis, timezone, numerator/denominator, exclusions, reporting basis, allowed dimensions, freshness target, permission/plan requirements and accountable owner. Version material formula changes and disclose when they change comparisons or previously issued reports.

Provide data-quality indicators for missing meter readings, unidentified payment allocations, incomplete opening balances, missing expense approvals, unavailable historical snapshots, unlinked requester identities and delayed integrations. Include the affected count/amount where authorized. Never silently replace missing data with zero or hide excluded records solely to improve a KPI.

Display comparisons with explicit prior-period dates, percentage-point changes for rates, absolute changes for amounts/counts and percentage changes only where mathematically meaningful. For zero/negative baselines, prefer labeled absolute movement rather than misleading growth percentages. Avoid performance rankings where too little or non-comparable data exists; show the chosen sample threshold and context.

Audit privileged changes to metric configuration, alert thresholds, shared dashboards, budgets and saved report/scenario definitions. Ordinary viewers cannot edit financial source records through reporting configuration. Basic operational charts and definitions remain available on Free; advanced customization, cohorts, forecasts and portfolio BI follow catalog entitlements without removing access to underlying essential records.

**Acceptance:** Each chart can identify its metric-definition version and source cohort. Historical issued exports are reproducible from snapshots or explicitly marked as restated reports. Permission changes apply to saved/shared views and scheduled reporting jobs immediately under the revocation design.

### POL-VER-001 — Jurisdiction Configuration and Request Initiation

**Source location:** attachment line 4365.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and affected tenant/applicant. **Phases:** 0, 3–4 and 6.

**Flow:** Select tenancy/application and person → determine property jurisdiction from an approved jurisdiction directory → show the applicable process and configured official channel → complete required details/documents → obtain required acknowledgments → review → submit → receive a traceable case/reference.

Support tenant-initiated and authorized owner/manager-assisted requests, with the initiating actor distinguished from the person being checked. Create individual subjects for co-tenants rather than treating one household member's result as everyone's result. Present case purpose, receiving authority, information shared, official fees if configured, and expected next steps. Do not ask for unnecessary identity fields or unrelated personal contacts.

Validate subject identity linkage, property/address, requested scope, required fields, jurisdiction and duplicate active requests. Version forms and record which jurisdiction rules applied at submission. Routing may require authority confirmation; nearest station or user-selected geography alone is not sufficient proof of jurisdiction.

**Acceptance:** A request cannot be submitted on behalf of an unrelated person or property. Retries create one case, and the tenant can see what was submitted about them. Unknown/unsupported jurisdiction produces an actionable unavailable/manual-process state, not an invented police reference or approval.

### POL-VER-002 — Applicant Information, Documents, and Correction

**Source location:** attachment line 4379.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; subject tenant/applicant and authorized requesting party with limited scope. **Phases:** 3–4 and 6.

Provide an applicant wizard with identity/address details, tenancy/property details, prior-address or other background information only where required by the configured official process, and secure evidence upload. Support save/resume, document validation, readable review before submission, declaration/consent where applicable, and a record of the lawful processing basis and notice version. Do not hardcode Aadhaar or another identifier as universally mandatory or permit unsupported identity-database lookups.

Store documents in private encrypted storage with file restrictions, scanning/quarantine where configured, expiring authorized access and audit logs. Mask identifiers in ordinary screens, notifications and logs. Police-only attachments and sensitive investigation notes must never be shared into general workspace documents, rental reports, public listings or AI retrieval indexes.

After submission, changes create a versioned correction/clarification response. Do not silently alter evidence already reviewed by an officer. Track which version each decision used. Identity mismatch or inconsistent details is a clarification/review issue, not an automatic finding of criminality.

**Acceptance:** The subject can review submitted information and respond to authorized clarification requests. Document substitution after submission is traceable, sensitive fields are masked appropriately, and other tenants/landlords cannot access the case through modified IDs or storage paths.

### POL-VER-003 — Verified Police Accounts, Roles, and Case Access

**Source location:** attachment line 4393.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; participating authority administrator, assigned officer and supervisor. **Phases:** 1 for access architecture; 6 for complete portal.

Provide a distinct `/police` portal. Police accounts cannot obtain privileges by selecting “police” during public registration or presenting a badge image alone. Provision accounts only through an approved authority onboarding process with identity/employment validation, authority/station membership, jurisdiction, role, effective dates and auditable approval. Require MFA and defined session/reauthentication controls; support authority-managed identity federation where officially available.

Separate intake/routing officer, investigating officer, decision-authorized officer, supervisor and authority administrator permissions according to the approved authority's actual process. One person may hold multiple roles only where the recorded policy permits it. Workspace admins, landlords and LMT support personnel cannot assign themselves police permissions or issue official outcomes.

Use a separate authority access boundary. An assigned officer accesses only the authorized case packet, not all landlord workspace properties, rent ledgers or tenant histories. Cross-station routing/reassignment requires authorized action, reason and audit; withdrawn/expired assignments and employment revocation remove access. Platform technical administration does not imply investigative document access. Any emergency support access is separately authorized, narrowly scoped and audited.

**Acceptance:** Verified police affiliation alone does not grant access to every case. Wrong-jurisdiction, unassigned, revoked and landlord accounts are denied. Access to documents, exports, background jobs and decision actions uses the same current case authority checks.

### POL-VER-004 — Police Intake, Background-Check Workflow, and Dashboard

**Source location:** attachment line 4407.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized authority users. **Phase:** 6; advanced workload analytics in 9–10.

Provide queues for received/unassigned, assigned, in progress, clarification required, ready for decision, completed and administratively closed cases. Filters include received/date range, jurisdiction/station, assignee, case status, target date and permitted subject/reference search. Dashboard cards show volume, unassigned backlog, aging, clarification delays, decisions issued and due/overdue cases only where a genuine configured target exists.

**Flow:** Acknowledge receipt → validate jurisdiction/completeness → assign officer → conduct checks through authorized official channels → record check completion/evidence references → request clarification if needed → record findings → route for supervisory review when required → issue outcome.

Checklists and permitted sources must be approved/configured by the authority, not improvised by LMT. Record check type, actor, time, source/reference, completeness and restrictions on disclosure. Do not scrape private systems, infer criminal records from names/social media, treat a name match as a confirmed identity match, or synthesize a background-check result with AI. Record source unavailable/uncertain as incomplete, never as a clean or adverse finding.

Support internal notes separate from applicant-visible questions, appointments where an official process requires them, paused/waiting states, authorized reassignment, escalation and evidence integrity. Notification success is not proof that an applicant received or answered a legal notice.

**Acceptance:** Every completed check and decision has a responsible authorized officer and supporting case version. A supervisor can return a case for further work without erasing prior history. Dashboard totals respect authority scope and do not disclose case subjects across stations.

### POL-VER-005 — Official Outcome, Pass/Fail Mapping, and Decision Integrity

**Source location:** attachment line 4423.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; decision-authorized officer/supervisor and permitted recipients. **Phase:** 6.

Keep workflow status separate from outcome. Suggested workflow states are draft, submitted, received, assigned, in-progress, clarification-required, under-review, completed, withdrawn and administratively-closed. Outcomes may include **Verified / Passed**, **Not cleared / Failed**, **Inconclusive / Further action required**, or an authority-specific terminology. Configure the allowed vocabulary and mapping with the authority. A jurisdiction that provides only receipt/acknowledgment must show **Submission acknowledged**, never Passed.

Require decision authority, required check completion, subject identity resolution, evidence references, decision date, applicable validity/review date only where officially defined, reason codes and permitted explanatory text. An adverse result requires a documented basis and the configured review/approval process; do not equate an adverse verification status with a conviction or universal unsuitability to rent. Internal detailed findings and applicant/landlord-visible summaries must follow explicit disclosure permissions.

Official decisions must be versioned and immutable once issued. Corrections/review outcomes supersede an earlier decision with reason, issuer and timestamps; preserve history. Use an approved digital signature/seal or verified official-channel response where supported. Do not display a fabricated police seal, signature or certificate. External receipt verification needs authenticated callbacks or documented provenance checks; a PDF uploaded by a landlord is **External result awaiting authenticity verification** until validated through the approved process.

**Acceptance:** Only authorized decision-makers can record official outcomes. Missing checks cannot silently pass; an unavailable source cannot silently fail. Concurrent submissions cannot create contradictory current decisions. A certificate/result download corresponds to the issued decision version and exposes only permitted fields.

### POL-VER-006 — Applicant Tracking, Notifications, Review, and Tenancy Handoff

**Source location:** attachment line 4437.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; subject applicant/tenant, authorized requester and relevant authority officer. **Phases:** 3–4 and 6.

Provide `/tenant/verifications` and property/application/tenancy-linked verification panels. Show case reference, receiving authority/channel, submitted date, current public status, pending applicant action, scheduled appointment if any, permitted outcome summary and authorized downloadable result. Distinguish **Not requested**, **Not supported**, **Submitted but not acknowledged**, **In progress**, **Official outcome issued** and **External evidence unverified**.

Use privacy-minimized notifications with authenticated deep links, not sensitive findings in email/SMS/WhatsApp. Support clarification responses, factual corrections, status inquiries and the officially configured reconsideration/appeal channel. Record delivery and response separately. A landlord cannot close an applicant's appeal or edit the subject's response. Withdrawal and deletion requests follow the applicable authority process; never promise recall of material already submitted to police.

Verification status may be a configurable onboarding prerequisite only where the verified jurisdiction/business policy requires it. Show a blocked prerequisite and authorized next steps; do not automatically reject an application, evict an existing tenant, disable utilities, publish an adverse badge, withhold deposit funds or remove core tenant access because of a pending/adverse outcome. Housing decisions remain separate authorized, reviewable workflows with an explicit basis. A police outcome is not a guarantee of safety or a substitute for consent/authority in other workflows.

**Acceptance:** The subject receives permitted information and can request correction/review through the configured process. A stale or subsequently corrected result does not silently remain the active prerequisite. Workspace downgrade, consumer subscription expiry and unpaid rent cannot expose or alter police outcomes.

### POL-VER-007 — Integration, Security, Retention, and Operational Controls

**Source location:** attachment line 4451.

**Linked frontend screen contracts:** FS-28; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for safe workflow architecture; official integrations and live authority operation conditional on approved arrangements. **Phases:** 0, 1 and 6; release verification in 12.

Support three explicitly labeled modes: (1) authorized authority users operating the LMT police portal, (2) approved official API/integration, and (3) assisted external/manual submission tracking. Display which mode is actually active. In mode 3, application tracking is not official receipt or completion until supported by verified evidence. Never mark sandbox responses as live police verification.

Display recoverable uncertain authority/provider outcomes and distinguish official/provider references from LMT case IDs. Official fees, where configured, show payee, receipt, refund and reconciliation state. Do not invent a verification fee or guaranteed turnaround; payment is never evidence of a passed verification.

Add/refine `PoliceAuthority`, `PoliceJurisdiction`, `AuthorityMembership`, `VerificationCase`, `VerificationSubject`, `VerificationSubmissionVersion`, `CaseAssignment`, `VerificationCheck`, `ClarificationRequest`, `VerificationDecision`, `DecisionReview`, `ExternalVerificationReference` and `CaseAccessAudit`. Keep police authority identity separate from workspace membership. The case shares only a purpose-limited packet from the linked workspace/person; subject access and authority access are explicit additional policies, not blanket cross-workspace exceptions.

Provide authorized intake, assignment, checks, clarification, decision and review actions; subject/requester submission, tracking and correction; and only permitted result disclosure. Do not offer unrestricted bulk subject lookup. Sensitive views/exports remain purpose-scoped; investigative data must not appear in consumer AI, public search, reviews or general analytics.

Define and enforce authority-specific retention, access expiration, legal hold, deletion/anonymization and backup handling with official approval. Preserve necessary audit/integrity evidence without indefinite blanket retention. Account for reassignment, officer departure, incident response and delayed callbacks after closure.

**Acceptance:** No tenant, landlord, manager, platform support or officer can gain unauthorized case visibility through IDs, caches, exports, media links or async jobs. Non-live deployments cannot issue apparently official results. Authorized government record access is never assumed merely because an API adapter or portal screen exists.

### REVIEW-001 — Verified Residence Eligibility

**Source location:** attachment line 4504.

**Linked frontend screen contracts:** FS-29. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; verified current/former rental tenant, with restricted platform moderation/support for exceptions. **Phases:** 4 and 6.

A reviewer must authenticate and be linked to a primary-tenant or co-tenant relationship at the target property with recorded actual possession during that person's occupancy interval. The residence record must have sufficient verification evidence, such as an acknowledged handover and validated tenancy identity linkage. An approved application, visit, future move-in, signed agreement alone, paid subscription, or invitation alone does not prove the person lived there.

Former tenants remain eligible after move-out according to the published review policy and record-retention rules. Imported historical tenancies can qualify through a documented evidence-verification flow; an owner-entered record or self-declared residence alone must not immediately enable reviews. Show pending verification and an appeal/support path without exposing other occupants' records. An owner cannot unilaterally manufacture “verified resident” status or revoke established review eligibility to suppress criticism.

Exclude owners/managers reviewing their own managed property and accounts with a recorded conflicting financial/management relationship under the review policy. Non-contracting occupants are not automatically eligible in the initial scope; any later extension requires independently verified residence and explicit policy. Co-tenants with individually verified residence may each review. Do not require rent to be fully paid, a dispute to be resolved, or a favorable landlord acknowledgment before an otherwise verified tenant can review.

**Acceptance:** The backend derives property/person eligibility from authoritative records and ignores client-provided verified flags. A prospective visitor cannot review; a verified former tenant can; tenant Premium does not bypass proof; a hostile owner cannot block a review by changing a current dashboard label. Eligibility revocation for genuinely invalid evidence follows an audited platform process.

### REVIEW-002 — Review Submission, Stars, Editing, and Withdrawal

**Source location:** attachment line 4518.

**Linked frontend screen contracts:** FS-29. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; eligible reviewer. **Phases:** 6.

**Frontend:** `/tenant/reviews`, a Review this property action in current/historical tenancy screens, and a review form linked to the target property's public profile. Display the property, residence-verification status, publication policy and selected public identity before submission.

Collect a required overall integer star rating from 1 to 5, a review body with configured visible length limits, and optional category ratings for condition/cleanliness, amenities, maintenance responsiveness, location/accessibility and value for rent. Optional categories must allow Not rated; do not turn omissions into zero. Support draft save/resume, clear validation, submission confirmation, moderation status, revision history, edit and withdrawal. Written reviews are plain text or safely sanitized content; restrict links and do not enable public document uploads in the baseline.

Use one active published review per person per property as the initial anti-duplication policy. The reviewer may update it after later residence, but another tenancy, bed transfer, account alias or repeat submission must not create additional rating weight. Store the supporting tenancy/residence references privately. Enforce the uniqueness policy transactionally and submit idempotently. Preserve original creation, last edit and publication timestamps.

Keep published and proposed revisions distinct. An edited review undergoing moderation cannot silently overwrite the currently approved version; apply the declared visibility policy and show the author the status. Withdrawal removes the review from public display and aggregates, retaining only the audit/history permitted by policy. Other users cannot edit the review. Reviewer deletion/privacy requests follow explicit retention and anonymization rules.

**Acceptance:** Invalid/out-of-range stars are rejected server-side; racing submissions create one review; edits cannot increase review count; withdrawn reviews stop contributing to ratings; tenancy/private identity fields never leak in the review payload. An eligible reviewer with an unpaid balance can still submit.

### REVIEW-003 — Public Property Reviews and Rating Summary

**Source location:** attachment line 4534.

**Linked frontend screen contracts:** FS-29. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; public viewers and authorized tenant users. **Phases:** 6, extending discovery from Phase 3.

Provide a Reviews section on the public property profile and linked unit listings. Clearly label it **Property reviews** so visitors do not mistake a building-level rating for a specific unit's condition. Show the overall rating, number of published verified-resident reviews, 1–5-star distribution, optional category averages/counts, review date/edit indicator, and a **Verified resident** badge. Explain that the badge verifies evidence of residence, not the truth of every statement or a property quality guarantee.

Calculate overall average = sum of overall stars on active published eligible reviews ÷ their count. Display a consistently rounded value, for example one decimal place, while retaining full precision for calculations. Category averages use only reviews with that category rated. Do not include drafts, pending/unpublished revisions, rejected, withdrawn or removed reviews. No reviews means **No reviews yet**, not zero stars or a fabricated default rating. Never weight by paid plan or assign additional weight to multiple tenancy records.

Provide pagination and sorting by newest, highest and lowest rating; star filters and an accessible distribution table. Default to a transparent ordering rule such as newest published. Owner responses are clearly separated from the tenant review. Show reviewer-selected display name or a privacy-preserving public label, without exposing legal name, exact unit/bed, contact details, rent balance, document evidence or exact residence dates by default. Platform moderation retains a private authenticated identity link even when public identity is minimized; do not promise anonymity against inference.

**Acceptance:** Aggregate ratings reconcile to the published review set after publication, edit, withdrawal or removal; cache invalidation prevents stale removed content/ratings persisting beyond the documented refresh window. Category counts differ correctly from overall review count when ratings are omitted. Filtering displayed reviews must not silently redefine the labeled all-property summary.

### REVIEW-004 — Fair Moderation, Reporting, and Owner Responses

**Source location:** attachment line 4548.

**Linked frontend screen contracts:** FS-29. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; reviewer, authorized property representative and explicitly privileged platform moderator. **Phase:** 6.

Model review states such as draft, submitted, under-review, published, rejected, withdrawn and removed, with reason codes and versioned decisions. Provide a published content policy covering harassment, threats, spam, conflicts of interest, private information, irrelevant content and manipulated/duplicate reviews. Low ratings or criticism alone are not grounds for rejection. Automated checks may flag content, but consequential disputed cases must have a human-review/escalation path.

Users and owners can report a review with a reason and evidence; a report does not automatically remove it or alter its stars. Provide acknowledgment, case status, moderator decision, reviewer notification and appeal. Urgent privacy/threat cases may be temporarily hidden under a documented policy; record who acted, why and when, and update rating visibility consistently. Moderators must be independent of the reviewed property's workspace management permissions.

Allow an authorized owner/manager representative to publish one official response thread per review under a defined edit policy, subject to the same privacy/content controls. Responses cannot disclose tenant documents, payment disputes or private contact details, and cannot change the tenant's rating. Multiple owners must coordinate through explicit response authority rather than impersonate tenants. Preserve response audit history; reviewed-property staff cannot approve, suppress, edit or delete tenant reviews merely because they manage the property.

Do not allow paid placement to masquerade as a verified review, selectively ask only satisfied tenants for feedback, offer rewards contingent on a positive rating, or make deposit release/maintenance access conditional on review removal. Any neutral review invitation uses a consistent eligibility rule, configured cooldown, opt-outs and permitted channels. Reviews are not an emergency reporting channel; offer a link to the existing helpdesk/reporting workflow without silently converting public allegations into financial actions.

**Acceptance:** A negative policy-compliant review can be published; an owner report goes to platform moderation; abusive/private content has a reasoned removal and appeal path; owner responses are scoped and auditable. Landlord subscription changes cannot remove unfavorable reviews or purchase a higher rating.

### REVIEW-005 — Review Management, Data Contracts, and Dashboard Metrics

**Source location:** attachment line 4564.

**Linked frontend screen contracts:** FS-29. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized operational viewers and platform moderation staff. **Phase:** 6; advanced trends in 9–10.

Add `/workspace/reviews` for authorized property representatives to view published feedback, respond and report; `/admin/review-moderation` for separately privileged review cases/appeals; and public property review endpoints plus authenticated eligibility/submission/edit/withdraw/report endpoints. Public users receive only approved publication-safe projections. A former tenant's review access is identity/residence-history based and does not require current landlord workspace membership.

Add or extend `PropertyReview`, `PropertyReviewVersion`, `ResidenceVerification`, `ReviewCategoryRating`, `ReviewResponse`, `ReviewReport` and `ReviewModerationDecision` as needed. Link to existing user/property/tenancy records with validated references, scoped authorization, unique active-review constraints, audit records, idempotency and optimistic concurrency. Tenant eligibility evidence stays private and only minimally necessary data is available to moderators. Keep public property ratings attached to the actual property through legitimate management changes; a routine manager change must not reset unfavorable history. Cross-workspace transfers/duplicate-property merges require explicit controlled mapping and audited review preservation, not automatic data reassignment.

Extend the property-admin dashboard with average verified rating, published review count, star distribution, optional category ratings, new published reviews in the selected interval, unanswered reviews, and trends where sample size permits. Overall rating is an all-published-reviews snapshot unless explicitly labeled as a period/cohort rating. Trend charts state whether they show newly published reviews' ratings or historical as-of averages; do not mix the two. Show counts beside averages and avoid comparing properties on tiny samples without context.

Review content may suggest a service issue for manual follow-up, but must not automatically prove asset damage, determine tenant liability, penalize a tenant or alter an invoice. If an authorized user creates a helpdesk task from feedback, preserve provenance and avoid copying unnecessary private content.

**Acceptance:** Owners see only authorized property response tools, not private residence-verification evidence or moderation controls. Modified IDs cannot submit for another person/property. Public rating queries, exports and caches never leak identity evidence. Dashboard ratings use the same metric definitions and published versions as public property pages.

### AMEN-001 — Configurable Amenity and Item Catalog

**Source location:** attachment line 4611.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/property administrator. **Phase:** 2.

Provide catalog categories and editable defaults: refrigerator/fridge, separate freezer, television, dining/study/coffee table, chair, sofa, air conditioner, bed, mattress, wardrobe, washing machine, microwave, gas stove, water purifier, geyser/water heater, ceiling fan, lights, curtains and other configured items. Keep a refrigerator and a standalone freezer distinct, with search aliases such as “fridge”; do not duplicate inventory because of spelling variants.

Separate physical items from non-inventory features/services such as balcony, parking entitlement, lift access, water supply and internet availability. Identify whether an amenity is unit-exclusive or shared at property/building/common-area level. Shared access does not mean an appliance is physically installed inside every unit.

Each catalog definition includes stable code, name, category, description, tracking mode (serialized asset, quantity-based item, feature or service), permitted units of measure, expected details, and active/retired status. Retiring a type preserves existing records and historical reports. Catalog changes require permission and audit; do not hardcode category names throughout business logic.

**Acceptance:** Admins can add a custom item, search aliases, and distinguish AC, refrigerator, furniture and services. Existing historical items remain readable after catalog retirement; duplicate identifiers are prevented within the chosen catalog scope.

### AMEN-002 — Unit Inventory and Explicit Presence/Absence

**Source location:** attachment line 4625.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager; tenant read access to their authorized unit. **Phases:** 2 and 4.

Add an Amenities & Inventory tab to each unit and active-tenancy workspace. For each relevant catalog item record assessed status: **Present**, **Absent**, **Not assessed**, or **Not applicable**, with assessment date, assessor and optional evidence/reason. For present physical items record quantity, owner (landlord/tenant/third party), exclusive/shared access, installed location, condition, usable/unusable status and inclusion in the rental offering. Do not interpret a missing database row as confirmed absence; treat it as Not assessed.

Distinguish **present but broken**, **temporarily removed for repair**, **promised but not supplied**, **tenant-owned**, and **shared access**. Physical presence, working condition, contractual provision and service availability are separate facts. A promised AC is not an installed AC, and a broken refrigerator is not a missing refrigerator. Temporary absence follows an asset movement/service record and must be visible in the inventory.

For quantity-tracked items, maintain whole-number quantities and distinguish total, usable and damaged quantities. Enforce nonnegative quantities and consistent totals. For features/services, use availability/status fields instead of fabricated physical quantities. Store explicit assessments even for Absent/Not applicable and preserve superseded assessments in history.

**Acceptance:** A manager can state that Unit A has one working refrigerator, one TV, four chairs and no AC; Unit B has one broken AC and two chairs; Unit C has not been assessed. All three states are distinguishable. Inventory modifications are validated and audited server-side, and tenants cannot silently edit landlord-owned inventory.

### AMEN-003 — Asset Details, Maintenance, and Lifecycle

**Source location:** attachment line 4639.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager and authorized maintenance staff. **Phases:** 2, 5–6.

For individually tracked appliances or furniture, support asset tag, optional serial number, brand/model, purchase/install dates, purchase value where authorized, supplier, warranty expiry, supporting documents, photos, condition and service history. Do not require a serial number for items that do not have one. Tenant views exclude private purchase/financial details unless intentionally shared.

Track acquisition, installation, inspection, repair, replacement, transfer, storage and retirement/disposal events. Record event date, source/destination, quantity or asset identity, actor, reason, evidence and relevant approval. A serialized asset can have only one active physical location at an instant; quantity-based transfers must conserve stock and reject insufficient source quantity. Pending delivery/transfer is not completed installation.

Link maintenance tickets and inspection findings to the actual item/asset. Record fault, downtime, service result, replaced parts where relevant, and next service/warranty date. Repair expense/payment remains in the existing approved expense ledger; an asset record cannot mark an expense paid. Replacing an appliance preserves the old asset and its tenant handover history.

**Acceptance:** Moving a TV from Unit A to Unit B updates both inventories atomically after actual handover; competing transfers cannot double-assign it. A refrigerator sent for repair remains traceable as temporarily absent. Returning it records condition and installation without duplicating inventory or losing service history.

### AMEN-004 — Unit Comparison Matrix and Has/Does-Not-Have Filters

**Source location:** attachment line 4653.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/admin/manager. **Phases:** 2 and 6.

Provide `/workspace/inventory` with a unit-by-amenity matrix: rows are properties/units and configurable columns are selected amenities such as fridge, TV, table, chair, sofa and AC. Cells show assessed status, quantity and condition summary, with clear distinct indicators for shared, tenant-owned, temporarily absent and unassessed items. Offer an accessible table/list alternative on mobile.

Support property/building/floor/unit, occupied/vacant, manager, item category, ownership, condition and assessment-age filters. Add explicit predicates **Has item**, **Does not have item**, **Not assessed**, **Below required quantity**, **Needs repair**, and **Promised but missing**. Multi-item selection must support clearly labeled **All selected** and **Any selected** matching.

Define Has as actual eligible presence under the selected exclusive/shared and ownership filters; provide a separate **Working items only** option. Does not have must mean confirmed absence, not unknown. A quantity requirement must specify threshold (for example at least four usable chairs); not meeting it is distinct from having no chairs. Display current results and optionally an as-of view only where effective-dated history supports it.

Every result links to the unit and relevant item record. Allow authorized export with filters, assessment date, ownership and data coverage. Batch assessment/import must preview changes, validate rows, respect permissions and record partial failures safely; it must not silently mark all blanks Absent.

**Acceptance:** Users can find “occupied units without an AC,” “units with a working refrigerator and TV,” “units with fewer than four usable chairs,” and “units whose furniture has not been assessed.” Counts match the displayed authorized unit set, including filters and pagination-independent totals.

### AMEN-005 — Tenant Handover, Acknowledgment, and Damage Review

**Source location:** attachment line 4669.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; owner/manager and affected tenant. **Phases:** 4 and 6.

Generate an immutable, dated move-in inventory snapshot with supplied items, quantities, condition/photos, included services and acknowledged exceptions. Allow item-level acceptance, missing-item reports, correction requests and disputes; do not require tenants to falsely accept all items. Capture mid-tenancy deliveries/removals/replacements as separate acknowledged change records rather than rewriting the signed move-in checklist.

At move-out compare the move-in snapshot plus authorized subsequent changes against actual return inspection. Flag missing quantities, changed condition, normal-use observations, damage evidence and unresolved repair cases for review. Contractual responsibility and wear-and-tear treatment require verified policy; inventory difference alone is not proof of tenant liability.

Link proposed deductions to the existing approval/dispute/settlement workflow with evidence and valuation rationale. Do not automatically charge replacement cost, deduct deposits, or mark an allegation accepted. Tenant-owned items must not become landlord inventory or subject to landlord asset-return demands without explicit authority.

**Acceptance:** A replaced TV is compared against its documented replacement, not demanded in addition to the old one. Disputed move-in condition remains visible at exit. Missing/damaged items generate review tasks, never automatic financial deductions. Former tenants retain their authorized handover snapshots under retention policy.

### AMEN-006 — Inventory Dashboard, Listing Accuracy, and Decision Metrics

**Source location:** attachment line 4683.

**Linked frontend screen contracts:** FS-09. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for core counts/accuracy; advanced cost/service analytics by plan. **Phases:** 2, 3 and 6; advanced analytics in 9–10.

Extend the property-management dashboard with:

- Units with/without each selected amenity, plus Not assessed and Not applicable counts; show the authorized occupied-unit or all-unit denominator explicitly.
- Amenity coverage rate = units confirmed present ÷ applicable assessed units. Display unassessed units separately; do not imply full portfolio coverage when many units are unknown.
- Working coverage using an explicit usable-quantity requirement, and separate shared-access coverage.
- Item quantities by property/category/condition; count physical serialized assets once, not once for every unit sharing access.
- Promised-but-missing items, broken appliances, pending replacements and inspection discrepancies needing review.
- Upcoming warranty/service dates and overdue service tasks only when recorded dates/policies exist.

Use property comparison bars, a unit/amenity matrix and condition-distribution bars with accessible underlying tables. Provide actionable drill-downs to assessment, repair, replacement approval and handover review. Asset cost/value charts may use recorded values but must not invent depreciation or current market value.

Published listing amenities must come from an explicitly reviewed offering snapshot linked to current inventory/features. Do not advertise a tenant-owned fridge as landlord-provided, a shared washing machine as unit-exclusive, or an unavailable appliance as working. Physical changes should flag affected listings for correction or apply an explicitly authorized publication policy; private asset serial numbers, purchase receipts and tenant belongings never appear in public listings.

**Acceptance:** Dashboard totals reconcile to the unit matrix and asset register. Shared equipment does not inflate physical totals. Unassessed units do not appear in “does not have” results. A removed/broken promised appliance creates a visible service/listing discrepancy without silently changing agreed tenancy terms.

### HELP-WF-001 — Tenant Issue Submission and Tracking

**Source location:** attachment line 4729.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authenticated tenant/authorized occupant within tenancy scope. **Phase:** 6.

Provide `/tenant/helpdesk` with **Raise issue / Request help**, My open requests, Awaiting my response, Scheduled visits, Resolved and Closed history. Support categories such as electrical, plumbing/water, appliance/furniture, cleanliness/common-area maintenance, access/security, billing clarification, documents/administration and other configured requests. Billing disputes route to or link with the existing financial dispute workflow; a helpdesk status alone cannot change an invoice.

Capture tenancy/property/unit context, subject, description, category, observed urgency, optional affected amenity/asset, occurrence time, preferred contact and optional supporting photos/video within configured limits. Allow a common-area issue without inventing a unit asset. Derive permissions from the logged-in relationship; do not let the user attach an unrelated tenancy by changing a request field.

Show applicable emergency guidance and verified operational contacts for urgent hazards, explaining actual monitoring hours; ticket submission is not a guaranteed emergency response. Provide a confirmation/reference, submitted timestamp, current responsible team, visible next step and response target only where configured. Allow save/retry without duplicate tickets and make upload/delivery errors recoverable.

**Acceptance:** A tenant can submit a leaking tap or broken AC with evidence and later view its actual progress. An occupant can use only permitted categories/data scope. Free/expired Premium users retain access; inability to upload a photo does not necessarily block a valid textual issue.

### HELP-WF-002 — Manager and Owner Intake, Visibility, and Assignment

**Source location:** attachment line 4743.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; property-scoped manager and owner/authorized operational approver. **Phase:** 6.

Provide `/workspace/helpdesk` with list/board views for New/unassigned, Assigned to me, Awaiting tenant, Awaiting approval, Scheduled, In progress, Resolution review, Reopened and Closed. Filters include property/unit, tenant where authorized, category, severity, assignee, created date, last update, status and due/target date. Show reference, summary, tenant-visible status, age, next action and unresolved blocker.

Route new cases to the assigned property manager; if none exists or delegation is inactive, route to the authorized owner/backup queue and flag unassigned ownership. Notify the responsible recipients through configured channels independently of saving the ticket. Owners with operational authority can oversee their property cases, respond directly or reassign within authority; ownership percentage or workspace billing ownership alone is not unrestricted access to every tenant's sensitive complaint.

Triage includes acknowledge, confirm/correct category and severity with reason, ask clarification, link related incidents, assign/reassign, and identify required spending/entry approvals. Preserve the tenant's original urgency and text. Sensitive complaints about the assigned manager require a restricted alternate escalation path to an authorized owner/platform process; do not automatically disclose the allegation to the person complained about. The platform support route handles its defined scope and must not promise authority to resolve every housing dispute.

**Acceptance:** Both the tenant and appropriate manager/owner can see the same ticket's shared progress. Unassigned cases stay visible rather than disappearing. Revoked managers cannot continue viewing or changing cases, and ownership changes hand over tasks explicitly with audit history.

### HELP-WF-003 — Conversation, Clarification, and Status Lifecycle

**Source location:** attachment line 4757.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; requester, authorized property team and assigned worker within limited scope. **Phase:** 6.

Model the core status lifecycle as submitted → acknowledged/triaged → assigned → in-progress → resolution-proposed → resolved → closed, with explicit branches for awaiting-tenant, awaiting-owner-approval, awaiting-vendor/parts, visit-scheduled, reopened, cancelled and duplicate-linked. Refine state names consistently with the existing MaintenanceTicket model; do not maintain two competing status fields for the same fact.

Each transition requires an allowed actor, reason/required fields, version check, timestamp, audit event and any notification. Track responsibility, assignment, scheduled visit and blocker separately where they need independent lifecycles. An internal assignment or estimate approval is not resolution.

Provide a chronological shared conversation with messages, attachments, status changes and visit proposals. Distinguish tenant-visible replies from explicitly marked internal operational notes. Staff must preview the audience before sending; internal notes and vendor-only details must not leak to the tenant or unrelated owners, and tenant messages cannot be silently rewritten. Corrections/redactions retain a policy-controlled audit trail.

Support clarification requests with a clear question and next step. Record tenant response and restore the appropriate working state. Any service-target pause for waiting on a party requires an actual configured policy and reason; do not pause clocks automatically to hide delays. Avoid broad group-chat access to all co-tenants; participants receive only appropriate case visibility and authorized invitations.

**Acceptance:** Reloading shows authoritative persisted history. Concurrent edits cannot overwrite assignment/status unnoticed. Waiting, scheduled and approved states do not imply work completed; every tenant-facing update is truthful and auditable.

### HELP-WF-004 — Diagnosis, Repairs, Approvals, and Access Consent

**Source location:** attachment line 4773.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; assigned manager/owner, authorized worker/vendor and affected tenant. **Phase:** 6.

**Flow:** Diagnose/remotely clarify → identify asset and proposed work → prepare estimate → request owner approval if needed → assign worker → propose time/access window → obtain required entry consent → perform work → capture evidence and expense references → submit resolution.

Support in-house resolution as well as vendor assignment. Workers see only assigned job details and necessary consented contact/location instructions, not tenancy finance, police documents or the full resident directory. A business in the nearby-services directory is not automatically authorized for maintenance access; assignment and sharing must be explicit.

Spending approval follows current delegated limits and the approved estimate/version. Changing scope/cost reopens approval when necessary. Permission to spend, permission to enter and tenant liability are separate: approving a repair does not authorize entry or automatically create a tenant bill. Use existing expense, billing and damage-dispute workflows for any legitimate charge.

Allow the tenant to accept, decline or propose another visit window. Track cancellation/no-show, access not granted, parts unavailable and follow-up visits without losing the case. Emergency entry or work follows a recorded lawful policy and authorized human action; urgency is not a blanket software bypass. Replaced assets update the inventory/service history and authorized handover evidence.

**Acceptance:** An above-limit repair waits for approval, and a scheduled worker cannot treat an unaccepted entry request as permission. Cost changes are visible; tenant bills require their own valid basis. Work evidence and asset changes link back to the ticket without duplicating expenses.

### HELP-WF-005 — Resolution Proposal, Tenant Feedback, and Reopening

**Source location:** attachment line 4789.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized resolver and requester/affected tenant. **Phase:** 6.

Require a resolution summary, work completed, completion time, relevant evidence, asset/expense references and any follow-up limitations before proposing resolution. Notify the tenant with **Issue resolved**, **Still not resolved / Reopen**, and **Need clarification** actions. Provider/manager completion and tenant acknowledgment are distinct recorded facts.

A tenant can explain unresolved symptoms or dispute work quality and attach evidence. Reopening retains the original case/history, increments a reopen counter and routes to the responsible manager with escalation where appropriate. Similar recurring issues may be linked to an incident/asset problem, but must not erase the original request or artificially inflate resolved counts.

Use an explicit resolution/closure policy. Any timeout-based administrative closure requires a disclosed response window, reminders and closure reason, must not count as tenant-confirmed satisfaction, and must preserve a permitted reopen/support path. Do not automatically close urgent/unresolved safety issues merely because a timer elapsed. Allow requester cancellation with reason where suitable; cancellation is not proof the issue was repaired. Duplicate cases link to a canonical ticket while preserving requester access and notifications.

Optional feedback covers handling/communication and actual outcome, with privacy controls; do not fabricate positive ratings or require favorable feedback to receive maintenance. Complaint/escalation remains available after closure under retention policy.

**Acceptance:** A manager cannot claim tenant acceptance by clicking resolved. A reopened ticket appears in active queues and historical metrics accurately. Closed cases retain evidence, costs, conversations and tenant responses; duplicate closure does not delete another person's access to relevant progress.

### HELP-WF-006 — Escalations, Notifications, and Helpdesk Dashboard

**Source location:** attachment line 4805.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for essential visibility/manual escalation; advanced service-target automation by plan. **Phase:** 6; advanced analytics in 9–10.

Provide escalation for unassigned cases, unanswered clarification, excessive inactivity, repeated reopening, spending approval delays and urgent issues, using configured targets where available. Allow the tenant to request escalation with reason; route it to an authorized next-level owner/manager or applicable platform support queue without automatically broadening document access.

Notify relevant participants on submission, assignment, clarification, visit proposal/change, approval outcome where shareable, progress, proposed resolution, reopening and closure. Use durable event processing, preferences/consent, rate limits and deduplication; failed email/WhatsApp delivery cannot erase the case or count as an acknowledged message. In-app status remains the source users can inspect.

Extend the property-admin dashboard with new/open/reopened cases, distinct requesting tenants, unassigned and waiting cases, severity/category breakdown, oldest outstanding cases, upcoming visits, blocked approvals, first human response time, resolution time, tenant-confirmed versus administrative closure and configured overdue targets. Reuse PM-DASH definitions, show time basis and sample sizes, and include unresolved cases rather than biasing statistics toward fast closures.

**Acceptance:** Owners can identify and act on a neglected request, tenants see who is responsible, and duplicate reminders do not spam users. Dashboard totals reconcile to the case queue, role scope and actual status events; no target is invented where none is configured.

### HELP-WF-007 — Contracts, Security, Retention, and Workflow Integration

**Source location:** attachment line 4819.

**Linked frontend screen contracts:** FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; implementation requirement. **Phase:** 6; release verification in 12.

Extend `MaintenanceTicket` and existing assignment/approval models with participant visibility, message audience, case event history, clarification, visit/entry consent, resolution proposal, feedback, escalation and incident links as needed. Reuse asset inventory, repairs/expenses, billing disputes, notification delivery, appointments and document storage. Personal service requests remain separate records with only explicitly authorized links.

Provide the authorized helpdesk actions: create/read/list, acknowledge/triage, assign, reply, clarify, propose/confirm visits, request approval, update work, propose resolution, accept/reopen, cancel/link duplicate, escalate and permitted archive/export. Handle file validation, private access, concurrency and repeat-submission outcomes.

A former tenant retains access to permitted cases about their own prior tenancy, particularly unresolved billing/repair disputes and handover issues, without visibility into later occupants. Allow authorized follow-up on those cases; new unrelated active-tenancy requests require the appropriate current relationship. Workspace cancellation or personal Premium expiry must not silently destroy essential case history. Define retention/redaction/deletion according to the existing privacy policy rather than retaining all photos/messages indefinitely.

AI may suggest categories or draft replies only within authorized knowledge and confirmed actions; it cannot invent completed repairs, owner approval or tenant consent. A functional human-operated helpdesk must remain available without AI/provider connectivity.

**Acceptance:** Tenant, manager, owner and worker each receive only their allowed case projection. Modified IDs, message-audience flags, exports, cache keys and attachment URLs cannot bypass scope. Integration events preserve a single source of truth for ticket, expense, invoice and asset states.

### RENT-FIN-001 — Charge Catalog, Responsibility, and Billing Configuration

**Source location:** attachment line 4870.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/financial manager, with tenant visibility of agreed charges. **Phases:** 2 and 5.

Provide a configurable charge catalog with stable codes and categories for base rent, electricity consumption/fixed charges, maintenance/common-area charges, water, clubhouse/gym/pool membership where offered, parking, internet, waste collection, approved one-time charges and permitted late fees. Label refundable deposits separately and account for them as liabilities rather than rental income.

For each applicable tenancy/unit charge, configure payee/issuer, liable party or explicit joint-liability rule, source agreement/enrollment, recurrence, service period, billing date, due date, calculation basis, currency, rounding, proration, tax treatment where verified, effective start/end dates, discounts/waivers, required approvals and permitted payment allocation priority. Owner-paid society fees must not automatically be passed to tenants without an agreed valid basis.

Support fixed, metered, shared-allocation, per-person/capacity-based and one-time calculation methods only where configured and disclosed. Store rate/policy versions and prevent ambiguous overlapping schedules. Changes apply prospectively or through explicit approved corrections; never silently recalculate issued invoices using a new rate.

**Acceptance:** A manager can configure a unit's rent, utilities and agreed fees independently. Unapproved categories, expired schedules or landlord obligations cannot appear as tenant debt. Lock-in discounts apply only to their agreed components; deposit and platform fees do not enter rent totals.

### RENT-FIN-002 — Electricity and Water Readings, Tariffs, and Shared Bills

**Source location:** attachment line 4884.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped meter/billing operator and affected tenant. **Phase:** 5.

Capture meter identity, previous/current reading, timestamps, unit of measure, reader, photo/evidence and actual/estimated flag. Link readings to the meter-installation history and billing interval. Validate outliers, duplicate readings, negative consumption, reset/rollover/replacement and mismatched dates through explicit review rather than silently accepting or rejecting every lower reading.

For individual metering, calculate consumption from validated readings and the effective tariff version, including configured slab/fixed/service components where applicable. Do not hardcode universal electricity/water rates or assume resale markups are allowed. Where bills originate from a utility provider or society, record original bill/reference, period, amount, evidence and whether the tenant pays directly or the authorized owner collects it.

For shared meters or bills, define the allocation rule and denominator: approved submeter consumption, occupants, area, equal participating units or another agreed method. Record participation dates and exclusions. Allocated shares plus any explicit owner/common-area remainder must reconcile exactly to the distributable source amount using deterministic residual rounding. Vacant capacity must not silently transfer its share to occupied tenants unless the agreed rule allows it.

Estimated usage remains clearly labeled; reconcile actual readings/provider bills through adjustments with references, not a second full bill. Prevent duplicate billing when both a meter calculation and a provider bill represent the same consumption. A direct-to-provider obligation must not create an LMT collection request without authorization or count as landlord cash received.

**Acceptance:** Meter replacement, shared water allocation, mid-period move-in/out and estimate true-up produce explainable charges. A tenant can inspect their calculation/evidence without seeing other occupants' private details. Allocations reconcile to the source and cannot be charged twice.

### RENT-FIN-003 — Maintenance, Water Service, and Clubhouse Membership Fees

**Source location:** attachment line 4900.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and enrolling tenant. **Phase:** 5; lifecycle completion in 6.

Distinguish recurring property/society maintenance fees from individual repair expenses and tenant-caused damage claims. A maintenance ticket or approved repair does not automatically authorize billing the tenant. Pass-through or one-time repair charges require an agreed responsibility basis, supporting evidence and applicable approval/dispute workflow.

Support fixed water service charges separately from water consumption. Display both only when the agreement/tariff actually includes both. Parking and other facility charges require a valid allocation/enrollment and its effective dates.

For clubhouse or similar optional membership, record facility/issuer, eligible members, plan/service description, price, billing cycle, enrollment acceptance, activation/end date, included benefits, renewal policy, suspension/cancellation conditions and proration/refund terms. Require explicit enrollment/renewal authority; do not auto-enroll all occupants or infer consent from property occupancy or Premium purchase. If a fee is a mandatory disclosed tenancy charge rather than optional membership, label that basis clearly.

Prevent billing after an effective cancellation/end date except for separately documented valid final charges. Facility access status and financial balance are separate; any access change follows the approved facility policy and must not disable home access, essential utilities or basic tenancy workflows. Store external facility-provider references when the platform only tracks externally managed membership.

**Acceptance:** An enrolled tenant receives the correct clubhouse fee and can see its terms; a non-enrolled tenant is not charged. Membership cancellation, move-out and family-member changes affect only valid periods/participants. Repair expense, recurring maintenance and membership fees remain distinct invoice categories.

### RENT-FIN-004 — Billing Run, Preview, Approval, and Invoice Issue

**Source location:** attachment line 4916.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized billing preparer and financial approver. **Phase:** 5.

**Flow:** Select property/tenancy and billing period → gather active charge schedules, readings, memberships and approved adjustments → calculate draft lines → validate missing inputs/anomalies → preview tenant-wise and property totals → obtain required approval → issue immutable invoices → deliver in-app notifications and configured external notices.

Allow scheduled recurring billing and authorized manual runs. A missing reading or unapproved charge must create a visible exception. Under an explicit policy, issue ready charges and later supplemental utility invoices, or wait for all prerequisites; do not silently replace missing usage with zero. Track issued/supplemental coverage by source charge and service interval so repeated jobs cannot duplicate it.

An invoice includes unique issuer-scoped number, issuer/payee, authorized tenant/tenancy and unit reference, issue/due dates, service period(s), itemized category/description, quantity/rate/calculation reference, discounts, credits, applicable verified taxes, total, currency, approved payment instructions and document version/reference. Number allocation must be transactional; failed drafts should not masquerade as issued invoices. Define number scope and legally applicable fiscal/format requirements before production.

A combined tenant bill may present several obligations, but amounts for different legal issuers/payees must remain separately identified and invoiced/collected appropriately. Do not merge an unrelated clubhouse operator's revenue into a landlord invoice without an approved agency/billing model.

**Acceptance:** Preview totals equal issued totals for the approved version; changed source data invalidates stale approval. Concurrent/scheduled/retried runs issue each eligible charge once. Partial run failures retain per-tenancy results and safely resume failed items without reissuing successful invoices.

### RENT-FIN-005 — Tenant Bills, Payments, Allocation, and Receipts

**Source location:** attachment line 4932.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant/co-tenant within financial scope and authorized collection staff. **Phase:** 5.

Provide `/tenant/bills` with current dues, upcoming issued bills, overdue/disputed balances, category breakdown, invoice downloads, payment history and statements. Clearly distinguish invoice amount, credits/waivers, verified allocations, pending payments, remaining balance and advance credit. Provide mobile-readable bill lines and source utility/fee explanations.

Allow supported digital checkout and manual payment reporting through existing payment adapters. If paying multiple bills or categories together, show the allocation before confirmation and preserve it server-side under the approved allocation policy. Permit partial payment and advance credit where supported; do not automatically divert money toward disputed charges or unrelated subscriptions. Joint obligations are one receivable unless explicit split obligations exist; never duplicate the amount per co-tenant.

Use server-authoritative amount/payee validation, idempotent orders, signed callbacks, reconciliation and uncertain-payment recovery. Unverified cash/bank-transfer claims and screenshots remain pending review. Successful payment creates an immutable receipt referencing payment method/provider/manual reference, verified amount/date and allocations; an invoice or payment attempt is not a receipt.

Handle overpayments, failed/late callbacks, duplicate callbacks, refunds and reversals with traceable allocations. Unallocated credit remains distinct from collected rent; category-specific dashboards use actual rent/utility/maintenance/membership allocations. Refunds require approval and reconciliation, not merely a marked-complete UI action.

**Acceptance:** A combined payment of rent, electricity, water and maintenance produces the correct category allocations and one coherent payment/receipt trail without counting every component as rent. Pending payment does not falsely clear dues; a retry does not collect twice; refunds/reversals update only affected obligations.

### RENT-FIN-006 — Corrections, Disputes, Credits, and Invoice States

**Source location:** attachment line 4948.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; affected tenant and authorized financial approver. **Phases:** 5–6.

Keep invoice lifecycle (draft, approval-required, issued, voided/cancelled under policy), collection state (unpaid, partly-paid, paid, overpaid/credit), dispute state and overdue status distinct. Derive collection/overdue status from dates and ledger entries; do not let staff arbitrarily mark invoices paid or erase debt by changing status.

After issue, correct amounts through approved credit/debit adjustments, credit notes or cancellation/reissue where permitted, retaining original references and audit. Enforce reason, supporting evidence, actor authority and version concurrency. Wrong meter reading, duplicate fee, wrong tariff, incorrect enrollment and mistaken liable party each need a traceable resolution path. Previously collected money and receipts must reconcile after correction.

Let the tenant dispute specific lines with explanation/evidence and track submitted, acknowledged, under-review, clarification-required, upheld/adjusted or rejected-with-reason outcomes. Display disputed and undisputed amounts separately without silently deleting obligations. Reminders, payment allocation and any collection pause follow the configured dispute policy. Fees on disputed or overdue balances require a verified contractual/legal basis; never invent compounding or blanket penalties.

**Acceptance:** A dispute preserves the invoice and evidence, and an approved reduction creates a reconcilable credit/refund or balance adjustment. Two operators cannot apply the same waiver twice. Financial closure cannot hide unresolved disputes or uncertain payment outcomes.

### RENT-FIN-007 — Move-In, Move-Out, and Membership Cutover

**Source location:** attachment line 4962.

**Linked frontend screen contracts:** FS-16; FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped billing manager and affected tenant. **Phases:** 5–6.

Integrate actual possession dates, contractual billing boundaries, opening balances, lock-in discounts, approved transfers, final meter readings and membership start/end dates. Use the accepted proration/day-count rules; actual move-out and contractual rent liability may differ, so show the basis rather than automatically charging only one or the other.

For onboarding an existing tenant, establish the external-to-LMT billing cutover and confirmed opening obligations/credits. For unit transfers, allocate periods/usage and membership changes to the correct source/destination without duplicating or erasing past invoices.

At offboarding produce a final itemized statement for rent, electricity, water, maintenance, clubhouse and other valid charges, plus credits, payments, proposed/approved deposit offsets and refund/holdback status. Stop future recurring generation from the effective schedule end; unresolved final provider bills remain disclosed estimates/pending obligations with a true-up workflow. Use EXIT requirements for reviewed lock-in/retention terms, not an automatic deduction.

**Acceptance:** Final dues reconcile across categories, approved deposit allocations conserve the deposit balance, an ended membership does not keep renewing, and the new occupant is not charged the previous tenant's consumption. Physical handover and financial closure remain distinct.

### RENT-FIN-008 — Billing Administration, Reports, APIs, and Controls

**Source location:** attachment line 4976.

**Linked frontend screen contracts:** FS-16; FS-17; FS-20. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/property admin/manager and tenant within scope. **Phases:** 5–6; advanced analytics in 9–10.

Add a billing workbench under `/workspace/rental-finance` for charge setup, tariff versions, memberships, missing readings, run previews, approvals, issued invoices, collection/verification, disputes, adjustments and final statements. Dashboard breakdowns show billed, verified allocated collection, outstanding, overdue and disputed amounts **separately for rent, electricity, maintenance, water, clubhouse and other categories**. Show cash receipt time versus charge service/due period explicitly.

Provide property/tenant/category/issuer filters, existing date presets/custom dates, aging, trend charts, arrears queues and permission-scoped exports. Aggregate only compatible currencies/payees under documented grouping. Total rental charges may include utilities, but a card named Rent collected must include only rent allocations. Direct-to-provider payments, deposits and platform subscription revenue must be labeled separately rather than inflating landlord collection metrics.

Reuse `Charge`, `Invoice`, `InvoiceLine`, `PaymentAllocation`, `MeterReading`, `DepositTransaction` and adjustment models; add versioned `ChargeSchedule`, `UtilityTariff`, `SharedBillAllocation`, `FacilityMembership`, `BillingRun` and per-source charge-generation keys where necessary. Use proper decimal types, deterministic rounding, scoped foreign keys, transactional uniqueness, immutable posted records and audited access.

Provide authorized charge configuration, membership enrollment/cancellation, reading validation, preview/approval/issue, invoice/receipt access, payment/reporting, allocation, disputes, corrections and final reconciliation. Amounts and authority remain server-controlled. Show issued invoices even if notification delivery fails; retry must not duplicate collection.

**Acceptance:** Dashboard/export amounts reconcile to invoices, adjustments and allocations. Tenant/manager scope applies to every source document and aggregate. Missing provider/tax/accounting configuration is visible and blocks affected production actions rather than producing fabricated receipts or compliance claims.

### LEASE-SIGN-001 — Deadline Trigger and Time Calculation

**Source location:** attachment line 5025.

**Linked frontend screen contracts:** FS-15; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant/signatories and authorized owner/manager. **Phase:** 4.

Trigger the signing-completion obligation from the authoritative actual possession/move-in event for the tenancy, not registration, application approval, planned move-in, SaaS onboarding or invitation acceptance. If the correct agreement has already been fully executed before move-in, mark the obligation satisfied with its evidence rather than creating another signature request.

Default calculation: `due_at = actual_move_in_at + 10 calendar days` in the workspace timezone, with ten calendar days defined explicitly as the same local clock time ten dates later. For example, a recorded move-in on August 1 at 14:00 Asia/Kolkata produces an August 11 at 14:00 deadline. Show the exact timestamp/timezone to users, not just an ambiguous “Day 10.” Where legacy records have only a date, apply a documented date-only convention and label the timestamp precision; never fabricate an exact actual possession time.

Record trigger event, effective move-in date/time, calculation policy version, original deadline, any earlier binding deadline, and calculated current deadline. Completion at or before the applicable deadline is on time; after it is late. Store provider event time and receipt time separately for delayed callbacks. Correcting a genuinely incorrect move-in event requires authority, reason and an auditable recalculation; editing a field or re-running onboarding cannot silently reset the clock.

**Acceptance:** Duplicate move-in events create one obligation. Planned-date changes do not shift a deadline anchored to actual possession. Already-signed agreements are recognized, exact boundary tests pass, and current/imported tenancies with historic incomplete execution show overdue/review status instead of a fresh ten-day allowance.

### LEASE-SIGN-002 — Prepare, Review, and Electronically Sign

**Source location:** attachment line 5039.

**Linked frontend screen contracts:** FS-15; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; required tenant/co-tenant/owner/authorized signatories and scoped manager. **Phase:** 4.

**Flow:** Prepare agreement and verify terms/parties → identify required signers and authority → permit corrections → freeze a reviewable document version → create authenticated provider signature requests → notify signers → collect required signatures → reconcile provider outcome → retrieve/validate executed document and audit evidence → mark execution complete.

Provide agreement preview/download, version number, rent/deposit/lock-in/discount terms, required signers, due date/countdown, signed/pending parties and correction/decline actions in `/tenant/documents` and the workspace agreement screen. A signature from one tenant does not satisfy another required co-tenant or owner's obligation. Signing order and witnesses follow the approved agreement process; ownership percentage alone does not grant signing authority.

Use a configured eligible e-signature provider with appropriate signer authentication, consent and tamper-evident evidence. A typed name, uploaded signature image, checkbox or landlord clicking “signed” must not substitute for the required provider-backed execution. Track draft, review-required, ready-to-send, sent, viewed where reliably available, partially-signed, fully-signed, declined, expired, cancelled and failed states independently of deadline status.

Do not allow content edits to an in-flight/executed version. Material changes supersede affected requests, preserve previous evidence and request all necessary new signatures. The original completion deadline remains visible; editing terms or resending requests does not automatically extend it. Separate execution, stamping, registration and possession states.

**Acceptance:** Full execution requires all policy-required valid signatures on the same approved document version and verified evidence. A browser redirect or user claim cannot mark the lease executed. Signers can request correction without silently accepting the current terms.

### LEASE-SIGN-003 — Reminders, Overdue Escalation, and Exceptions

**Source location:** attachment line 5055.

**Linked frontend screen contracts:** FS-15; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; pending signers, assigned manager and authorized owner. **Phase:** 4; broader reporting in 6.

Provide a configurable reminder schedule with initial defaults: initial signing request, and reminders on calendar days 3, 7 and 9 after the actual move-in trigger, plus an overdue escalation immediately after the deadline. Use permitted channels and consent/preferences; in-app tasks remain available if external delivery is unavailable. Skip elapsed reminders when importing or recovering a case and deduplicate by obligation, signer and reminder stage. Stop unnecessary signature reminders after completion, cancellation or supersession.

Escalate missing signatures to the responsible signer and manager, and unresolved blockers to the authorized owner. Show who is pending and whether the blocker is document correction, declined terms, identity mismatch, provider outage, insufficient approved provider funding, missing owner authority or inaccessible signing link. Do not automatically blame the tenant when the owner has not signed or the provider failed.

An overdue state is a compliance/operations task, not authority to impose an unagreed fee, evict, lock out, shut off services, seize deposits, forge a signature or remove rent/maintenance access. Declining or disputing agreement terms routes to human review; the system cannot coerce acceptance by preventing essential tenancy actions.

Allow a documented exception/extension request with reason, evidence, approver and revised operational target, subject to applicable policy. Preserve the original deadline and original compliance result; an extension does not retroactively make late completion on time or override an earlier legal requirement. If electronic signing is not legally/provider-supported, route to an approved alternative and record **Alternative execution pending/completed**, never **E-signed** without evidence. Manual/offline execution needs authorized authenticity review and its actual method/date.

**Acceptance:** No reminder duplicates or reminders to already-completed signers; provider failure creates a recoverable task. Overdue and approved-exception histories remain visible. Operational exceptions cannot erase missed-deadline evidence.

### LEASE-SIGN-004 — Evidence, Dashboard, Recovery, and Acceptance Tests

**Source location:** attachment line 5071.

**Linked frontend screen contracts:** FS-15; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized operational users and signatories within scope. **Phases:** 4 and 6; release checks in 12.

Persist provider request/document IDs, immutable document hash/version, signer identity/authority references, individual signing timestamps, verified completion timestamp, certificate/audit trail where supplied, callback verification and retrieval state. Use authenticated webhooks, idempotency, out-of-order event handling, status reconciliation and protected storage. If signed-artifact retrieval fails after provider completion, show **Provider reports signed — evidence retrieval pending** and reconcile; do not falsely claim a verified downloadable agreement or lose a potentially timely execution event.

Provide signature request, signer status, resend/reconcile, correction/supersession and exception-review actions. Show the completion obligation, reminder history and approved exceptions. Recheck permitted access before sending/viewing and never expose another tenant’s agreement.

Add dashboard cards/queues for leases awaiting signatures, due within 3 days, overdue against original deadline, partially signed, provider/evidence failures, and permitted alternative execution. Show days remaining/overdue, pending signers, responsible manager and next action. On-time completion rate uses obligations whose original deadlines have elapsed, includes incomplete overdue obligations in the denominator, and reports approved alternatives/exceptions separately. Do not inflate compliance by resetting deadlines or excluding unresolved cases.

Test at least:

1. Actual move-in triggers one ten-calendar-day deadline; planned onboarding and duplicate events do not create/reset it.
2. All signers complete before/on/after the exact boundary; late-arriving verified provider events use actual signing time with preserved evidence.
3. An already executed correct version needs no duplicate signature request, while a new material amendment requires its own explicitly defined workflow.
4. A missing owner/co-tenant signature prevents full execution even if the primary tenant signed.
5. Agreement correction supersedes affected requests without erasing partial signatures or resetting the original deadline.
6. Duplicate/out-of-order callbacks, outage, link expiry and artifact retrieval failure recover without false completion or duplicate provider charges.
7. Reminders target only pending parties and escalate overdue cases without unauthorized financial or tenancy penalties.
8. Historic imported tenancy, corrected move-in timestamp, extension and legally required earlier deadline retain an honest audit trail.
9. Free-plan tenant signing remains available under configured provider funding; Premium purchase is not an execution prerequisite.
10. Alternative execution is labeled by its real method; inaccessible or unsigned uploaded documents cannot satisfy the obligation.

**Phase gates:** Phase 0 defines execution prerequisites, timezone/date-only conventions and provider/funding policy. Phase 4 implements LEASE-SIGN-001 through LEASE-SIGN-004 end to end. Phase 6 integrates operational queues, agreement exceptions and historical cases. Phase 12 verifies signatures, boundary dates, evidence retrieval, recovery, privacy and production-provider readiness. Missing legal/provider capability remains a visible blocker to electronic completion, not a simulated signature.

### EXIT-001 — Rental Commitment Offer and Advance Disclosure

**Source location:** attachment line 5108.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and tenant/signatories. **Phases:** 3–5.

Allow configurable rental offers, including six-calendar-month commitment options with a 10% or 20% discount on a defined base-rent component. Store offer/version ID, standard base rent, discount rate, eligible charge components, discounted rent, currency/rounding rule, commitment start and end, notice period, post-commitment price, renewal behavior, deposit requirements and early-exit policy. Do not infer that utilities, deposit or maintenance charges are discounted unless explicitly included.

Before agreement acceptance, show the undiscounted and discounted monthly amounts, commitment dates, total expected discount over the term where calculable, normal notice obligations, early-exit scenarios, proposed deposit consequences and exceptions. Keep a signed/accepted version of the terms, consent timestamps and supporting agreement reference. Later offer edits cannot rewrite a tenant's existing terms. An unaccepted draft is not an enforceable rental commitment in the application.

Record whether an early-exit proposal is no charge, partial deposit retention, full eligible-deposit retention, a separately stated compensation amount, or discount recovery under a reviewed policy. Define allowed combinations and caps explicitly; default to no stacking of deposit retention, recovered discounts and remaining-period rent for the same loss. Do not invent legal validity or recovery rights from a plan label.

**Acceptance:** A tenant sees exactly what the 10%/20% offer changes and what early departure may cost before agreeing. Switching SaaS/Premium plans cannot modify the rental discount. Financial schedules and agreement versions refer to the same accepted offer.

### EXIT-002 — Initiate Offboarding and Review Outstanding Obligations

**Source location:** attachment line 5122.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant/authorized co-tenant within scope and authorized owner/manager. **Phase:** 6.

Provide **Request move-out / Initiate offboarding** in the active tenancy portal. Collect proposed possession-handover date, notice date, optional departure reason, affected people/unit/bed, contact preference and forwarding/refund details only as needed. Show the accepted agreement, commitment end, notice requirements, discount and potential early-exit consequence before submission.

Display an itemized preliminary statement: posted rent through the applicable charge boundary, outstanding invoices, verified payments/advances, electricity readings and posted/unbilled utility charges, maintenance/other contractual charges, credits, deposit liability and any separately labeled proposed early-exit adjustment. Mark future/estimated amounts and unresolved disputes; do not present an estimate as a final payable amount or receipt.

Submitting notice must remain possible with unpaid dues or disputed terms. A tenant can acknowledge receipt of the policy without being forced to agree with every proposed deduction. Provide **Agree to proposed settlement**, **Request clarification**, and **Dispute a line item** at the appropriate review step. Never add a new forfeiture clause retroactively as a mandatory offboarding checkbox.

**Flow:** Draft request → submitted → owner/manager review → dates acknowledged or clarification required → preliminary settlement → inspection/final readings → final settlement review → physical handover and financial closure tracked separately. Existing notice state and settlement state remain authoritative rather than duplicated in this request.

**Acceptance:** Duplicate requests are idempotent and conflicting active requests are resolved explicitly. A co-tenant cannot terminate the entire household's tenancy without authority. Unpaid amounts or refusing a deduction do not prevent recording notice or actual possession handover.

### EXIT-003 — Lock-In Date Evaluation and Early-Exit Review

**Source location:** attachment line 5138.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/financial approver and tenant reviewer. **Phase:** 6.

Evaluate early exit against the accepted commitment definition, not an assumed 180-day period. Use calendar months, a documented month-end rule, workspace timezone/local dates and an explicit charge/possession boundary. Store the computed commitment-end date in the signed terms. A recommended technical interval is `[commitment start, commitment end)`: departure on/after that end is not early under that definition, subject to the actual accepted agreement.

The notice submission date alone does not prove early departure. Evaluate the requested handover date for an estimate and the recorded effective departure/handover event for final determination according to the accepted policy. Notice-shortfall and lock-in breach are distinct concepts. Notice changes, withdrawal, approved extensions, temporary relocation and tenant replacement require explicit recalculation; do not restart a lock-in automatically after a unit transfer or rent revision.

Before proposing retention, identify the signed clause/version, qualifying event, allowed amount/cap, eligible deposit balance, responsible parties, required evidence, available exceptions and approval authority. Exceptions may include owner-approved release, landlord-initiated termination, unresolved serious habitability/service issues, agreed replacement tenant or an approved hardship waiver, as defined by the reviewed policy. They are review paths, not invented universal legal entitlements.

If the applicable terms or enforceability are unknown, mark the deduction **Requires review** and prevent automatic posting. The manager may prepare a recommendation but needs the configured financial authority to approve it. Full retention must be labeled **Proposed security deposit retention — early exit**, with a reason and calculation; do not tell the tenant the money is already “lost” while the decision is pending or disputed.

**Acceptance:** Leaving before the defined six-month end triggers a reviewable proposal, while leaving on/after it does not falsely trigger early-exit retention. Corrected dates and approved exceptions revise the proposal without silently rewriting posted history. Pending review cannot become an automatic final deduction through a scheduled job.

### EXIT-004 — Rent and Electricity Clearance

**Source location:** attachment line 5154.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped manager/owner and affected tenant. **Phases:** 5–6.

Calculate final rent using the contract's charge end, agreed proration, approved concessions and existing invoice allocations. Record final electricity meter date/reading/photo, prior billed reading, tariff version, shared-meter allocation where applicable and any applicable approved fixed charges. Identify meter replacement/rollover and disputed or estimated readings using the existing utility workflow.

If a provider bill is delayed, show a provisional utility estimate and an explicit later-adjustment process. A temporary deposit holdback is separate from a charge or forfeiture and requires a permitted basis, amount, review/release date and communication. Do not hold the entire deposit indefinitely merely because a small final bill is unknown. Allocate final payments and approved deposit offsets to specific obligations rather than changing a total balance manually.

Provide payment initiation/reporting, pending verification, receipts, revised statements and dispute actions. A screenshot or browser return cannot clear dues; verified payment/reconciliation is required. Define clearance markers separately for rent, electricity, other charges, deposit settlement and unresolved disputes. Authorize write-offs/waivers with reasons rather than marking unpaid amounts paid.

**Acceptance:** A corrected final meter reading adjusts the existing settlement without duplicate electricity billing. Advances/credits reduce the correct obligation. The system shows incomplete/provisional clearance honestly and supports a verified true-up after physical exit.

### EXIT-005 — Deposit Settlement, Agreement to Terms, and Final Closure

**Source location:** attachment line 5168.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized financial approver and tenant/signatories as required. **Phase:** 6.

Produce a versioned settlement with individually reviewable lines: available deposit balance, proposed/approved rent offsets, electricity offsets, other substantiated deductions, separately approved early-exit retention, waived amounts, refundable balance, any permitted temporary holdback and remaining tenant payable. Each line links to its ledger source or proposed claim, clause/evidence, approval and tenant response.

Maintain an allocation invariant: total deposit applied/retained/refunded/reserved cannot exceed the actual available deposit liability after prior transactions. A reserved holdback remains a liability, not revenue or a paid refund. If a retention proposal consumes the entire eligible deposit, rent/electricity cannot also consume the same money. Show separately any other legally payable obligations and prohibit double recovery. Approved retention is not automatically rent collected and must not inflate the admin rent-collection charts.

Tenant review distinguishes acknowledgment, agreement, partial acceptance, clarification and dispute. Store the exact settlement version/hash, accepted lines, actor, timestamp and required signing/acknowledgment evidence. Material changes invalidate prior acceptance for affected amounts and require a new review. Do not treat silence, terms-page access or a submitted notice as acceptance. Resolve disputed sums through the configured authorized process; never erase the tenant's dispute to force closure.

Post approved settlement transactions idempotently through the deposit/payment ledger. Refunds require verified recipient details, authorization, provider/manual reconciliation and separate pending/paid/failed states. A valid final full-retention case may show **Refundable deposit: ₹0**, **Approved early-exit retention: ₹X**, and the documented basis; the result must follow the reviewed agreement and applicable rules rather than a blanket non-refundable-deposit assumption.

Physical handover, capacity release and financial closure remain separate. A fully retained deposit does not itself prove rent/electricity clearance, and financial clearance does not prove the tenant has vacated. Financial closure requires all obligations/refunds/holdbacks to be resolved, or a documented approved exception leaving a visible residual state. Preserve statements, dispute records and receipts for authorized former-tenant access.

**Acceptance:** Two simultaneous settlement actions cannot consume the same deposit twice. Reversals/refunds remain traceable; disputed/provisional items are not labeled final; every zero-refund outcome is supported by the approved settlement. Releasing capacity triggers advertising readiness, not automatic external ad publication before approval.

### EXIT-006 — Offboarding Administration, Audit, and Decision Metrics

**Source location:** attachment line 5186.

**Linked frontend screen contracts:** FS-18. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager and authorized tenant. **Phase:** 6.

Extend `/tenant/notices`, `/tenant/tenancies/{id}` and `/workspace/tenancies/{id}/offboarding` with commitment details, requested/acknowledged/actual dates, rent/electricity clearance, proposed versus approved deductions, missing evidence, approval tasks, tenant response, refund/holdback status and final closure actions. Link to existing notice, agreement, inspection, inventory, payments, deposit and dispute records.

Add dashboard queues for requested exits, exits within a commitment period, uncleared rent/electricity, missing final readings, settlements awaiting tenant review, disputed deductions, holdbacks nearing review date and refunds pending. Totals distinguish proposed retention, approved posted retention and actual rent collected. Do not present disputed/proposed amounts as earned revenue or performance gains.

Provide versioned commitment/early-exit review and settlement acknowledgment through existing notice, settlement, deposit, invoice, reading, approval and dispute screens. Actions cover requesting exit, recalculating dues, reviewing/approving/waiving claims, accepting/disputing settlement and authorized refund/offset requests. Show conflicts and pending execution separately from approval.

**Acceptance:** A manager without financial approval scope cannot finalize forfeiture or waivers. A tenant can see how each amount was calculated and retain access after departure. Changing a catalog discount or deposit policy cannot retroactively change this tenancy's accepted commitment.

### OCC-WF-001 — Onboard a Property That Is Already Rented

**Source location:** attachment line 5249.

**Linked frontend screen contracts:** FS-14; FS-32; FS-16. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and invited tenant. **Phases:** 2–5.

**Flow:** Select an existing property/unit → record the real tenancy and possession dates → identify primary/co-tenants and occupants → upload historical agreement and recorded verification status → configure billing cutover → import opening receivables, advances and deposit liability → review totals/evidence → invite the tenant → activate ongoing operations.

Record import provenance, as-of date, original references and whether each opening item is confirmed or disputed. Historical amounts must use dedicated opening entries with evidence; do not fabricate provider-confirmed payments or newly executed agreements. Define which periods are managed externally and which LMT will bill. Import retries must be idempotent and missing documents must remain visible exceptions, not invented completion.

**Acceptance:** The unit is correctly occupied without a new listing/application/signature workflow. LMT does not bill an already imported period twice. Opening deposits, advances and arrears reconcile separately. Tenant acknowledgment is recorded without treating it as acceptance of disputed balances. Workspace capacity limits cannot be bypassed by importing occupancy.

### OCC-WF-002 — Rented Portfolio and Tenancy Workspace

**Source location:** attachment line 5261.

**Linked frontend screen contracts:** FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager and the tenant viewing their own tenancy. **Phases:** 4–6.

Provide `/workspace/rented-units` with filters for property, manager, unit/bed, tenancy dates, agreement expiry, unpaid/disputed amounts, notice, inspections and open repairs. Show occupancy count separately from capacity and total unit count. Provide an active-tenancy workspace with overview, authorized occupants, agreement versions, financial account, deposits, meters, maintenance, inspections, announcements, changes and timeline tabs.

Display next rent due, last verified receipt, recorded deposit balance, upcoming approved changes, agreement dates, manager contact, next inspection, pending approvals and incomplete onboarding. Each metric must link to its source records and define its freshness. Show only role-permitted fields; an operational manager need not see every identity document or owner financial report.

**Acceptance:** Occupied units cannot be presented as vacant merely because an agreement expired or a tenant is overdue. Tenant views expose only their authorized records, while owner/manager actions depend on current delegation. Basic dashboard access does not require Premium.

### OCC-WF-003 — Recurring Occupied-Property Operations

**Source location:** attachment line 5273.

**Linked frontend screen contracts:** FS-16; FS-17. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and affected tenant. **Phases:** 5–6.

**Flow:** Generate period work items → identify missing meter readings, upcoming charges/renewals, overdue invoices and required inspections → validate inputs → run billing → reconcile payments → resolve disputes/approvals → record period review and outstanding carry-forward items.

Use the existing billing workbench and task system. Define billing-run uniqueness by workspace, tenancy, charge schedule and period. A rent amount change, waived fee, duplicate reading or missed job must not silently generate a second rent charge. Work queues must retain unresolved items across period boundaries.

If a period-review or accounting-lock policy is enabled, preserve posted history and route later corrections through dated adjustment records. A “period reviewed” flag must not imply all money is received or all disputes are resolved.

**Acceptance:** Job retries, month boundaries, partial payments, advance allocations and disputed charges produce reconciled balances. Notifications are deduplicated and do not control whether billing commits. Rental and platform subscription accounting remain separate.

### OCC-WF-004 — Rent Revision and Mid-Tenancy Term Changes

**Source location:** attachment line 5287.

**Linked frontend screen contracts:** FS-11; FS-15; FS-16. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/signatories, scoped manager and affected tenant. **Phases:** 5–6.

**Flow:** Propose revised rent/charges/responsibilities → record contractual basis, required notice and effective date → request required approval/acceptance → create agreement amendment when required → activate future schedule → notify parties and show the change in statements.

Keep proposed, awaiting-approval/acceptance, scheduled, effective, rejected, withdrawn and superseded states. Record whether acknowledgment or consent is required by the configured verified policy; do not assume delivery of a notification creates consent. Handle increases, decreases, temporary concessions, recurring charge changes and expiry of concessions. Compute proration from documented rules.

**Acceptance:** Unapproved changes cannot affect invoices. Effective-dated schedules cannot overlap ambiguously. Already issued invoices remain immutable except for traceable corrections. Concurrent revisions require conflict resolution. Ending a concession restores the explicitly agreed schedule, not an invented rate.

### OCC-WF-005 — Household, Co-Tenant, and Bed Allocation Changes

**Source location:** attachment line 5299.

**Linked frontend screen contracts:** FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; affected tenants and authorized owner/manager. **Phases:** 4 and 6.

**Flow:** Submit add/remove/replace occupant or co-tenant request → review eligibility, capacity and required documentation/consent → determine agreement and financial impact → approve/amend as required → apply on effective date → update only authorized access and allocation.

Distinguish an authorized occupant from a liable/signing tenant and a guarantor. Do not infer debt responsibility, equal rent shares or deposit entitlement from residence alone. Retain historical relationships. If only one bed changes occupants, leave the other bed allocations and tenancy obligations intact. Requests to sublet or change the contracting tenant require explicit policy and authority; no automatic delegation of tenancy rights.

**Acceptance:** Occupancy cannot exceed configured capacity; a household member cannot remove another contracting tenant unilaterally; an exiting co-tenant loses current private access according to the access policy while retaining permitted historical records. Existing personal subscriptions are neither transferred nor cancelled by household changes.

### OCC-WF-006 — Meter, Utility, and Inventory Changes During Occupancy

**Source location:** attachment line 5311.

**Linked frontend screen contracts:** FS-09; FS-16. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager and tenant acknowledgment/dispute where relevant. **Phases:** 5–6.

**Flow:** Record asset/meter replacement or allocation-rule proposal → capture old closing reading/condition and new opening reading/condition → attach evidence and approval → apply an effective-dated relationship → calculate subsequent charges → allow review/correction.

Handle meter replacement, reset/rollover, faulty/estimated readings, shared-meter allocation changes, appliance replacement and repair/service history. Separate physical asset identity from display number and record which unit/tenancy used it during each period. A new occupant must not inherit the previous occupant's unallocated consumption without an explicit valid adjustment.

**Acceptance:** Meter replacement cannot create negative or duplicate consumption accidentally. Estimates are disclosed and reconcile through adjustments when actual readings arrive. Inventory updates preserve move-in evidence and responsibility disputes. Changing shared allocations cannot silently rewrite prior bills.

### OCC-WF-007 — Occupied Repairs, Inspections, and Service Disruption

**Source location:** attachment line 5323.

**Linked frontend screen contracts:** FS-19; FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; scoped owner/manager, tenant and assigned worker. **Phase:** 6.

**Flow:** Identify defect or inspection need → assess severity and affected units → request access/notify under configured policy → approve scope/cost → schedule work → track disruption, mitigation and progress → record completion evidence → tenant review/dispute → restore operational readiness.

Track essential-service disruption incidents with start/end time, affected areas, responsible contact, updates and unresolved actions. Provide factual status and emergency guidance; do not invent resolution times. Repair restrictions may block new rentals while the current occupant remains in possession. Where temporary accommodation, concessions or compensation are agreed, record their approval and accounting treatment rather than automatically promising entitlement.

**Acceptance:** Entry permission is checked independently of repair approval. An overdue rent balance or expired subscription cannot automatically deny emergency reporting or authorize service disconnection. An incident closure cannot falsely mark every related ticket resolved. Occupancy stays accurate during repairs or temporary absence.

### OCC-WF-008 — Unit Transfer and Temporary Relocation

**Source location:** attachment line 5335.

**Linked frontend screen contracts:** FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must for same-workspace recorded transfers; cross-workspace transfer remains conditional. **Phase:** 6.

**Flow:** Request move from one unit/bed to another → verify destination availability, authority and agreed terms → review rent/deposit/utility implications → approve and amend/create the required tenancy agreement → schedule paired handovers → record actual source exit and destination possession → reconcile associated balances.

Represent the source and destination allocations independently with a linked transfer case. Reuse existing occupancy controls and do not delete/recreate history to simulate a move. Reserve destination capacity transactionally; preserve source possession until handover is recorded. Define authorized overlap and proration explicitly. Temporary relocation must state whether the original allocation remains reserved and whether return is expected.

Moving deposit responsibility or balances requires explicit approved ledger movements with source references; do not simply edit a unit ID on past invoices or payments. Partial failure, cancellation or a missed move date must leave a recoverable case and accurately held capacity.

**Acceptance:** Competing destination requests cannot both succeed; source and destination histories stay traceable; temporary relocation does not accidentally relist the original home; a transfer does not duplicate rent or lose deposit liability. Different-workspace data migration is not enabled by this workflow.

### OCC-WF-009 — Arrears, Disputes, and Agreement Expiry While Occupied

**Source location:** attachment line 5349.

**Linked frontend screen contracts:** FS-16; FS-17; FS-19. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and affected tenant. **Phases:** 5–6.

**Flow:** Detect arrears, unresolved charge dispute or agreement expiry → distinguish verified facts from proposed actions → send authorized reminders/request discussion → record any approved payment arrangement or renewal discussion → monitor outcomes → escalate to an authorized human process.

Support promise-to-pay records and agreed installment schedules without marking the underlying amount paid. Define the interaction with original due dates and fees through approved terms. Track disputed balances separately; prevent duplicate reminders or attempts to collect amounts already verified as paid. Agreement expiry with possession retained is an exception requiring review, not automatic vacancy or automatic renewal.

**Acceptance:** The system never authorizes eviction, lockout, utility cutoff or removal of belongings through a delinquency status. It does not silently release occupied capacity or manufacture renewed legal terms. Any external legal notice/process is separately reviewed and recorded, not inferred from automated reminders.

### OCC-WF-010 — End-of-Tenancy Closure and Turnover Safeguards

**Source location:** attachment line 5361.

**Linked frontend screen contracts:** FS-18; FS-14. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized owner/manager and exiting tenant. **Phase:** 6.

Use the existing notice, renewal, move-out and settlement workflows. Add a rented-unit closure checklist covering possession evidence, keys/access cards, final meter reading, pending utility estimates, open repairs, disputes, deposit liability, verified refund/collection status and retained documents.

Maintain separate milestones: notice acknowledged, physical handover complete, capacity released, readiness inspected, financial account closed, and history retained. Future listings may advertise an explicitly qualified availability date under policy, but confirmed current possession must not be contradicted. If exit is delayed, surface affected reservations/appointments for human resolution; never promise guaranteed availability from a notice alone.

**Acceptance:** Physical departure can be recorded despite unpaid/disputed balances; financial closure cannot occur while unresolved balances/refunds remain without an explicitly authorized adjustment. New occupants cannot inherit old obligations by default. Relisting requires authoritative availability and readiness, not just settlement approval or agreement expiry.

### SERVICE-PREM-001 — Nearby Ads, Categories, and Location Search

**Source location:** attachment line 5404.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; active personal Premium tenant. **Phase:** 7A, extending the Phase 6 directory.

Add a Premium service-ad feed at `/tenant/services`, with category chips/search, locality/PIN-code selection, optional consented geolocation, distance/service-area filters, opening hours where supplied, advertised price/range where supplied, and provider/offer availability. Support all requested categories and synonyms such as maid/domestic help, tiffin/meals, carpenter/carpentry and electrician/electrical work without duplicating providers.

Let the user select a saved tenancy locality or enter a service location; never assume the location of the user's active workspace. Browser geolocation is optional. Denial or unsupported devices must leave locality/manual selection usable. Store precise coordinates only where needed and permitted. A radius match is not enough if the provider does not serve that locality; evaluate service-area eligibility separately from distance.

Each card shows business/display name, category, service-area description, approximate distance only when reliably calculable, ad/offer summary, price basis and exclusions, valid dates, contact availability, listing-verification meaning and request action. Use **Sponsored** only for genuinely promoted placements under an approved arrangement; distinguish sponsorship from verified identity and service quality. Do not fabricate providers, ratings, discounts, travel times or “available now” claims.

**Acceptance:** A tenant can find laundry/tiffin near their chosen locality without granting GPS access. Out-of-area, paused and expired offers are excluded or clearly unavailable. Premium status is checked server-side; an unpaid/ineligible account cannot fetch the restricted ad feed merely by changing UI routes.

### SERVICE-PREM-002 — Provider Profile, Business Contact, and Publication

**Source location:** attachment line 5418.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; Premium viewers, approved business representative and authorized platform catalog moderator. **Phase:** 7A.

Provider profiles display approved business phone number with tap-to-call, optional approved messaging/business website links, services offered, service areas, hours, description, languages where supplied, price/estimate basis, cancellation/contact terms and active offers. Make contact numbers readable as well as clickable. A call click is a contact action, not an automatically submitted request or confirmed appointment.

Use a minimal `/provider` business portal or explicitly operated platform onboarding workflow to submit business details, prove control of the contact channel, agree to publication, configure service areas/categories, manage approved ads and receive requests. Business-profile/contact verification is not a guarantee of qualifications, police clearance or work quality. Display professional credentials only when supplied and reviewed under a defined verification process; do not invent them.

Moderate new/changed profiles and ads through draft, submitted, review-required, active, paused, rejected, expired and withdrawn states. Verify contact ownership/publication permission before exposing numbers. Detect/report spam, impersonation, misleading offers and unsafe content. Material business-contact, service-area or offer changes require the configured re-review; retain old versions for existing request history.

Do not expose private worker phone numbers, identity documents or home addresses simply because a business employs them. Assigned worker details require appropriate consent and purpose-limited sharing. One provider may serve multiple workspaces; provider accounts are separately scoped and cannot browse tenant/workspace records.

**Acceptance:** Premium users can see and call the approved business number. A provider cannot publish another person's number without the required verification/permission, edit another business's ad or access tenants who have not sent it a request. Moderation actions are auditable and unavailable contacts have a report/correction path.

### SERVICE-PREM-003 — Submit a Service Request with Controlled Data Sharing

**Source location:** attachment line 5434.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; eligible Premium tenant and selected provider. **Phase:** 7A.

Provide **Request service** on an ad/profile. Collect category/service, short description, preferred date/time windows, location sufficient to check coverage, preferred contact method and optional relevant photos. Category-specific fields may include laundry quantity/pickup preference, cleaning scope, tiffin dietary preferences provided voluntarily, or repair issue details. Do not require unnecessary sensitive personal information.

Before submission, preview the destination business and exactly which contact/address/attachment fields will be shared. Require affirmative sharing consent. Default to locality/approximate service area; disclose exact address/unit and access instructions only when needed for the agreed visit and authorized by the requester. Never send lease documents, rent balances, police results, private residence proof or the user's full tenant profile.

Revalidate membership, provider/ad status, category, service coverage and submitted fields server-side. Create a durable request with unique reference, offer/version snapshot, consent record and idempotency key. A provider lacking an enabled request-delivery channel must show contact-only/unavailable-request status rather than a request falsely marked delivered. Queue permitted notifications independently from request creation and show delivery failure/retry honestly.

**Acceptance:** One click/retry creates one request for the intended provider. A tenant can review their own submitted request and sharing history. Invalid/inactive ads and unsupported areas are rejected clearly. Sending a request does not collect payment, guarantee provider acceptance or grant permission to enter the rented home.

### SERVICE-PREM-004 — Provider Response, Scheduling, and Request Tracking

**Source location:** attachment line 5448.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; requester and assigned provider representative. **Phase:** 7A.

Provide `/tenant/services/requests` and a provider inbox with states submitted, delivery-pending/failed, acknowledged, clarification-required, quoted, declined, accepted-for-scheduling, scheduled, in-progress, completion-reported, completed, cancelled and disputed as appropriate. Keep request state, communication delivery and any future payment state separate. Do not require every category to use every state; configure permitted transitions with responsible actors.

Providers can request clarification, decline with reason, propose price/scope and available windows, or accept for scheduling. The tenant reviews a dated quote/version and explicitly confirms price/scope/slot where offered; no automatic commitment from a request alone. Material quote/time changes need renewed confirmation. Quotes must separate known price, variable estimate, taxes/extra costs where supplied and exclusions; never pretend an estimate is a fixed final bill.

Show conversation/attachment history with actors/timestamps, notification preferences and privacy controls. Support cancellation, rescheduling, provider no-response, no-show, completion evidence and complaints. Distinguish provider-reported completion from tenant acknowledgment or dispute; use a disclosed closure policy rather than silently assuming tenant satisfaction. Show any agreed cancellation terms without automatically charging a fee.

A scheduled service visit still requires occupant-authorized entry/access instructions and relevant house/common-area rules. Providers cannot access property locks, occupant directories or tenancy administration. Tenant-created service requests must not automatically charge the landlord or replace the landlord's maintenance obligations; offer a separate explicit maintenance-ticket path where relevant.

**Acceptance:** Both parties can follow the same request history and resolve changed quotes/windows without double scheduling. A provider cannot mark a payment received through this request-only scope. Unanswered/failed delivery is visible, cancellations are auditable, and a complaint does not disappear when an ad expires.

### SERVICE-PREM-005 — Membership Expiry, Safety, and Administration

**Source location:** attachment line 5464.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; requester, scoped provider and platform service moderator. **Phase:** 7A; release verification in 12.

Check personal Premium entitlement on restricted ad/profile access and new requests. Where the broader consumer catalog provides trials/grants, use its effective entitlement rules; unlike the paid-only SALE catalog, do not silently introduce a separate residence or paid-only verification rule. Make eligibility clear in pricing and UI.

When membership expires, stop new Premium ad discovery/requests according to the catalog, but retain the user's own existing request history and necessary business contact, communication, cancellation/rescheduling, complaint and agreed-visit details. This continuity does not grant access to unrelated ads or new providers. Public directory contact remains governed by its existing visibility policy.

Providers receive only requests addressed to their business and assigned staff scope. Protect contact endpoints, requests, photos and exports with authorization, rate limits, private storage and audited access. A global service catalog is platform/provider-scoped; personal service requests are requester/provider-scoped, not arbitrarily owned by a landlord workspace. Landlords/managers cannot view personal requests or contact disclosures merely because they manage the home.

Provide reporting/blocking, provider suspension, support escalation and retention/deletion controls. Suspending a provider stops new ads/requests while preserving affected users' support and history. Explain that urgent hazards require appropriate emergency/maintenance contact rather than an unmonitored service request. Do not imply that a platform listing certifies a domestic worker or guarantees safety.

**Acceptance:** Subscription expiry does not strand a confirmed service request or remove complaint access. Another tenant, landlord or provider cannot read it via guessed IDs, caches or exports. A blocked/suspended provider cannot continue receiving new requests or discover other tenants.

### SERVICE-PREM-006 — Service Dashboard, Data Contracts, and Decision Metrics

**Source location:** attachment line 5480.

**Linked frontend screen contracts:** FS-26. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; tenant, scoped provider and authorized platform service administrators. **Phase:** 7A; optional analytics in 9–10.

Tenant dashboard shows open requests, awaiting clarification/quote confirmation, upcoming scheduled visits, overdue responses under a defined target and completed/cancelled/disputed requests. Provider dashboard shows assigned incoming requests, response/quote queues and upcoming confirmed work. Platform service operations show active/expired ads by category/locality, coverage gaps, invalid-contact reports, response/delivery failures, complaints and suspension queues. Personal service details must not appear in landlord property dashboards without separate explicit authorization.

Define metrics from actual events: requests submitted, provider acknowledgment rate, median first human response time with unanswered count, quoted-to-confirmed conversion with denominator, cancellations/no-shows, completion acknowledgments and unresolved complaints. Phone-link clicks are not calls completed or successful jobs. Count unknown/partial states honestly and do not imply provider revenue without a separate authorized payment system.

Extend the existing `ServiceProvider` model with `ProviderMembership`, `ProviderContactVerification`, `ServiceArea`, `ServiceAdvertisement`, `ServiceAdVersion`, `ServiceRequest`, `ServiceRequestMessage`, `ServiceQuoteVersion`, `ServiceAppointment`, `ServiceSharingConsent` and moderation records where needed. Reuse existing category, document, notification and scheduling infrastructure while keeping public directory visibility distinct from Premium ad access.

Provide gated nearby-ad search/detail/contact and requester submit/list/clarify/confirm/cancel/complaint actions; provider ad/request/quote/schedule actions; and separately privileged moderation. Handle coverage errors, stale state, duplicate submissions and entitlement denials without inventing status changes.

**Acceptance:** Reports reconcile to source request events, new requests respect entitlement and provider status, and every party sees only its own permitted queue. Provider paid advertising fees, tenant service checkout and commissions remain disabled unless separately specified and approved.

### SALE-001 — Owner Authority and Sale Listing Preparation

**Source location:** attachment line 5527.

**Linked frontend screen contracts:** FS-27. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; verified authorized property owner/house owner; explicitly delegated preparer where allowed. **Phases:** 2 for authority foundations; 7A for sale workflow.

Provide `/workspace/sales` and a **List this flat/house for sale** action on eligible property/unit records. A sale asset may be a flat/unit or an entire house/property; require an explicit scope and validate that the advertiser has authority over that scope. A room/bed allocation or manager assignment is not proof of authority to sell an entire house.

Review seller identity, ownership/agency evidence and required co-owner approvals through a documented process before publication. Store sensitive evidence privately. Verification status must distinguish submitted, under-review, verified-for-listing, rejected and expired/review-required. “Verified-for-listing” means evidence passed the platform's stated review, not guaranteed title or a legal ownership certificate. A manager may prepare a draft under delegation, but cannot authorize sale publication merely from ordinary rental-management permission.

Capture title, property type, sale scope, locality and privacy-controlled location, area/unit of measure, configuration, asking price/currency, negotiability, included fixtures/amenities, seller-provided disclosures, occupancy/tenancy status, proposed sale conditions, availability for viewing, photos/tour, approved contact method and listing expiry. Validate price and dimensions; do not fabricate valuation, title clearance or investment returns. Use rent/yield information only where authorized, substantiated and safely disclosed; never expose tenant identities or private payment histories.

**Acceptance:** A verified owner can prepare a sale ad for their rented flat/house. A manager without sale authority or an unrelated owner cannot publish it. Changing scope, seller, price, occupancy representation or material disclosures requires re-review under the publication policy.

### SALE-002 — Restricted Publication and Buyer Eligibility

**Source location:** attachment line 5541.

**Linked frontend screen contracts:** FS-27. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; eligible buyer, authorized seller and narrowly scoped platform moderator. **Phase:** 7A.

Implement a sale lifecycle of draft, ownership-review-required, awaiting-owner/co-owner-approval, ready-to-publish, live-restricted, paused, under-offer, sold, expired, withdrawn and rejected. Only an approved publication snapshot enters the restricted catalog. Under-offer does not mean the property has legally transferred or the renter has vacated.

For every buyer-facing request enforce:

`authenticated account + verified qualifying rental-residence evidence + active paid TENANT_PREMIUM entitlement + eligible published sale listing`.

Use the existing residence-verification capability from REVIEW-001: independently verified current or former primary/co-tenant residence, within retention/verification policy. An applicant, visitor, unverified imported tenant, ordinary occupant, email/phone verification alone or Premium checkout without residence proof does not qualify. Police clearance is a separate process and is not automatically required or implied. Do not require the buyer to belong to the seller's landlord workspace; authorize a deliberately curated marketplace projection, not the underlying workspace records.

Interpret active paid access from verified personal subscription records and paid-through dates. A cancellation scheduled for period end retains access while paid entitlement is active; expiry, suspension, reversal/revocation under the published billing policy and loss of valid verification remove discovery access. Free trials or promotional grants alone do not qualify for this paid-only catalog unless the product requirement is explicitly changed; the default must enforce active paid membership.

The seller and authorized sale preparer may manage their own listing without buyer Premium/residence eligibility. Platform review access is separately privileged/audited and cannot serve as general buyer access. Guests and ineligible accounts see only a generic eligibility/upgrade explanation with no listing-specific title, price, address, media, count, teaser, contact or metadata leakage.

**Acceptance:** A verified tenant without paid Premium, a paid Premium applicant without verified residence and a landlord with Diamond workspace access are denied buyer content. An eligible verified Premium tenant can discover listings across participating workspaces without acquiring workspace membership or document access.

### SALE-003 — Premium Sale Discovery, Inquiries, and Viewings

**Source location:** attachment line 5561.

**Linked frontend screen contracts:** FS-27. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; eligible buyer and scoped seller/representative. **Phase:** 7A.

Add `/tenant/property-sales` with locality, asking-price, property-type, configuration, area, occupancy and supported amenity filters, sorting and pagination. Provide authenticated detail pages, saved sale listings and property comparison under the same gate. Show explicitly **Currently rented/occupied** and the seller's disclosed proposed possession/tenancy arrangements; do not advertise vacant possession merely because the property is for sale.

Allow an eligible buyer to submit an inquiry, ask for seller clarification, express non-binding interest and request a viewing. Require explicit consent before sharing buyer contact details with the seller. Seller inboxes receive only the inquiry's necessary fields, not the buyer's rental documents, exact residence history, police result or private finances. Include report-listing actions for misleading descriptions, unauthorized sales or privacy concerns.

Reuse the appointment scheduler and conflict controls. Viewing an occupied home requires the existing occupant-entry notice/consent or other recorded lawful authority process independently of the owner's sale approval. Avoid publishing an occupied home's security/access instructions or private unit-level location in broad search results. Provide controlled location/meeting instructions only when needed by authorized participants.

Recheck Premium and verification before discovery, new inquiries, saving/comparison and appointment requests. After access expires, stop new discovery/actions and remove private listing previews from saved lists. Retain access to the buyer's own previously submitted inquiry records, consent history and minimal confirmed appointment cancellation/support details under a defined transaction-history policy; this must not expose the full live catalog or grant new viewing requests. Existing appointments require explicit status handling, not silent cancellation or unauthorized entry.

**Acceptance:** Inquiries reach only the correct seller, denied buyers cannot bypass the gate via direct URLs, and confirmed visit records never override occupant consent. Selling a rented property cannot terminate the occupant's tenancy or publish their personal information automatically.

### SALE-004 — Private Media, Moderation, and Sale Outcome Boundaries

**Source location:** attachment line 5577.

**Linked frontend screen contracts:** FS-27. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; eligible buyers, authorized sellers and platform moderators. **Phase:** 7A; security review in 12.

Serve sale details and media through authenticated authorization-aware endpoints/private storage. Do not rely on a hidden navigation item, robots.txt, unguessable listing ID or a long-lived public media URL. Prevent content leakage through search indexes, public sitemaps, Open Graph/social previews, unauthenticated Next.js server rendering, prefetch, CDN caching, API errors, notifications, exports, thumbnails and analytics payloads. Cache by effective eligibility and authorized content scope or disable shared caching for sensitive responses. Expiring media access needs an explicit short validity window and revocation policy; do not promise recall of content already downloaded by an authorized viewer.

Private sale tours must use an access-controlled delivery mode. If an external tour provider cannot enforce the required privacy, exclude that tour rather than embed a publicly retrievable private-sale asset. Do not automatically reuse publicly published sale details or publish these listings through the external rental-ad adapters. Notifications use generic text and an authenticated deep link with eligibility rechecked on opening.

Moderation must support ownership/publication review, duplicate ads, fraud/privacy reports, appeals, suspension and evidence audit. Sellers may pause/withdraw their listing and record under-offer/sold status with evidence/reason appropriate to the platform workflow. A self-reported Sold status is a marketplace status, not proof of registration, title transfer, tenancy termination or deposit transfer.

Baseline scope is advertising, gated discovery, inquiries and viewing coordination. Booking deposits for purchase, escrow, brokerage commissions, sale consideration, title due diligence and conveyancing are separate conditional capabilities requiring explicit commercial/legal design. Do not execute them through the rent/deposit payment module.

**Acceptance:** Expired/ineligible accounts cannot retrieve sale details/media through backend or cache paths. A public rental ad cannot accidentally carry the private sale price, seller contact or occupied-home photos. Listing withdrawal removes discovery visibility while preserving authorized case/history records.

### SALE-005 — Seller Dashboard, Data Model, and Audit

**Source location:** attachment line 5593.

**Linked frontend screen contracts:** FS-27. Apply the linked frontend contracts and the specific rules below together.

**Priority/roles:** Must; authorized sale administrators and eligible buyers within their scope. **Phase:** 7A; advanced analytics in 9–10.

Seller dashboard shows sale listings by status, ownership-review blockers, pending approvals, listing expiry, authorized inquiries, scheduled viewings, reports and last material update. Provide aggregate views/inquiry counts only from permitted telemetry; do not expose the identities of all viewers or their residence evidence. Keep sale asking prices, rental income and SaaS revenue separate in reporting. A sale-marked property must not disappear from operational tenancy dashboards while occupants/obligations remain.

Extend existing listing/ownership/approval/appointment modules with distinct `PropertySaleListing`, `SaleListingVersion`, `SaleAuthorityReview`, `SalePublicationApproval`, `SaleInquiry` and `SaleInterest` concepts as needed. Reference the underlying sale asset and owner authority privately, with a deliberately limited marketplace projection. Reuse personal consumer entitlement and residence-verification services; do not copy their flags into client-editable listing records.

Provide seller draft/review/approval/publication, eligible-buyer search/detail/media, inquiry, viewing coordination and permitted status changes. Show the approved version and stale-state/eligibility errors. Seller management rights cannot be reused to browse unrelated private sale listings.

**Acceptance:** The seller can trace authority review → owner approval → restricted publication → inquiry → viewing → outcome. Owner/manager changes invalidate or re-review sale authority without exposing records to an unauthorized successor. Existing rent agreements, deposits and occupied status remain intact regardless of a sale listing's status.

### AI-001 — Preference Profile and Natural-Language Property Search

**Source location:** attachment line 5648.

**Linked frontend screen contracts:** FS-35. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must for preference-based matching; generative parsing conditional on configured AI/consumer entitlement. Seekers; Phase 3 baseline, Phase 7A AI extension.

**Frontend:** Add Find a suitable home to `/listings` and `/tenant/recommendations`. Collect locality/allowed radius, monthly rent budget, optional total recurring-cost budget, move date, property/room/bed type, household capacity, minimum lease period, furnishing, desired amenities, accessibility requirements and optional commute destination. Let users label each preference **Must have**, **Prefer**, or **No preference**, and change priorities. Avoid collecting sensitive traits that are irrelevant to a home search.

Support natural-language input such as “A furnished 1BHK below ₹18,000 near my office, with a fridge and parking, available next month.” Parse it into an editable structured query and highlight uncertain meanings: budget basis, exact date, radius and shared versus private amenities. Ask only for genuinely missing hard constraints; do not silently invent a location, assume every charge is included or widen the budget.

**Authoritative result dependency:** Validate interpreted enums, ranges and dates before use; parsed model output is an editable proposal. Support ephemeral search, preference reset/deletion and explicit consent for optional behavioral personalization; avoid unnecessary precise location collection.

**Acceptance:** User-confirmed hard constraints match the executed query. Denied personalization/GPS and provider failure leave conventional search available. A malformed model response cannot insert an unauthorized filter, SQL expression or hidden account characteristic.

### AI-002 — Candidate Selection, Ranking, and Availability Integrity

**Source location:** attachment line 5662.

**Linked frontend screen contracts:** FS-35. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must; authorized seekers. Phase 3 deterministic baseline, Phases 7A/9 measured enhancements.

**Frontend:** Show ranked candidates with a clear label such as Matches your preferences, missing/unknown fields, current availability and permitted price breakdown. Give controls for priority changes, excluding a result, and viewing ordinary sorted results. Do not show an uncalibrated model score as “95% chance you will like it” or a safety rating.

**Authoritative result dependency:** Show only eligible authorized candidates satisfying hard constraints. Unknown hard-constraint data is excluded or placed in a separately labeled Needs confirmation section at the user’s choice.

Start with transparent rule-based scoring using configured weights for confirmed preference matches, price fit, location/commute where reliably available, amenities and listing freshness. Keep household/bed/unit pricing comparable. Store component contributions and missing-data flags. ML/semantic reranking is optional and must outperform the declared baseline in evaluation without weakening eligibility; do not require a generative model just to sort rows.

A reranker may reorder approved candidate IDs only. Validate its response against the candidate set and fetch current listing facts again before rendering/contact/booking. No hallucinated property IDs, inferred vacancy, fabricated reviews or generated travel times. De-duplicate the same rentable inventory and provide useful diversity within the user's constraints; disclose sponsored placements separately and never let payment bypass filters or masquerade as organic quality.

**Acceptance:** Occupied/unpublished/ineligible properties never become recommended because a model prefers them. No-match responses explain which constraints limit results and propose changes for user approval. Concurrent occupancy changes prevent stale recommendations from creating conflicting bookings/allocations.

### AI-003 — Explainable Recommendations, Comparisons, and Feedback

**Source location:** attachment line 5678.

**Linked frontend screen contracts:** FS-35. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must for factual explanations; richer conversational explanations follow consumer entitlement. Phase 3 baseline, Phase 7A extension.

**Frontend:** Each recommendation includes Why it matches, Trade-offs, Unknown details and last listing-confirmation time. Example: “Within your stated rent budget; refrigerator listed as supplied; parking availability needs confirmation.” Link every factual reason to the relevant listing field, amenity record, permitted review aggregate or licensed map result. Compare shortlisted options by rent, deposit, recurring fees, actual supplied amenities, move-in readiness and verified information scope.

Do not equate owner-contact verification with title/quality verification or a resident review with a guarantee. Clearly distinguish disclosed rent from estimated total monthly cost, asking price from accepted rent, and user preference from an established fact. Summaries of review patterns must disclose sample size and avoid identifying reviewers or turning isolated claims into certainty.

**Authoritative result dependency:** Explanations use approved source/version evidence and verified numerical values. Feedback options include useful/not useful, too expensive, wrong locality, missing amenity and no longer available; feedback must not infer protected traits.

**Acceptance:** A user can trace each recommendation reason to accessible evidence and correct preferences. Unavailable source evidence yields an explicit unknown. Feedback cannot modify the actual listing or its rating, and shared comparisons cannot leak private SALE/service content to an ineligible recipient.

### AI-004 — Helpdesk Assistant, Ticket Drafting, and Human Handoff

**Source location:** attachment line 5692.

**Linked frontend screen contracts:** FS-36; FS-19. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must when the capability is offered; tenant and scoped manager. Phase 7A personal help, Phase 9 workspace helpdesk AI; manual baseline in 6.

Use approved property instructions, help articles and the user's authorized ticket facts to answer questions, draft a clear issue description, suggest category/urgency and summarize prior conversation. Show AI-generated draft/status explanations, evidence links and an easy human-handoff action. Never claim a worker was assigned, repair completed, payment received or entry approved without authoritative evidence.

Retrieve only accessible cases/knowledge. Internal manager notes, another tenant's messages, police evidence and unrelated documents are excluded from the tenant assistant. A manager assistant's summary follows that manager's actual scope, not platform-wide access. Escalate uncertain or urgent hazards to the existing emergency/human guidance rather than providing unsafe repair instructions or promising immediate response.

Creating/sending a ticket or reply requires preview, explicit confirmation and a fresh authorized existing command. AI may suggest priority but cannot suppress emergency reports, close disputes, approve repairs, grant entry or mark tenant satisfaction. Human agents receive only the consented conversation/structured summary needed for handoff.

**Acceptance:** A confirmed draft creates one ticket through HELP-WF, and provider failure leaves the original form functional. Malicious text in a ticket cannot cause secret disclosure, cross-case retrieval or autonomous actions.

### AI-005 — Owner/Manager Copilot for Listings and Operations

**Source location:** attachment line 5706.

**Linked frontend screen contracts:** FS-36; FS-30. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must when included in offered workspace AI features; scoped owner/manager. Phase 9, expanded Phase 10.

Offer Draft description, Summarize property tasks, Suggest listing improvements and Draft response actions on existing screens. Draft rental/social copy from verified publication-safe property attributes and current inventory; identify missing photos, stale availability, unreadable descriptions or undisclosed charges as review tasks. Show editable diffs and source facts before acceptance.

Summarize overdue operational tasks and approved next steps from existing work queues. An optional image model may suggest objects/condition labels only from authorized listing/inspection photos, visibly tentative and never overwriting AMEN inventory without human assessment. It must not infer people’s identity, belongings ownership, structural safety or tenant liability from an image.

Approval/advertising workflows remain authoritative: saving a draft, approving creative, publishing externally and approving paid budget are separate commands. AI cannot bypass owner approval, alter a quote, change availability, send messages or launch spending autonomously. No private tenant belongings, police reports or signed documents enter generated public copy.

**Acceptance:** Claimed amenities/prices match reviewed source records; generated text cannot add a pool, discount, review score or guaranteed return. All accepted changes use normal version/concurrency guards, and stale proposals require regeneration/review.

### AI-006 — Document Extraction and Source-Linked Agreement Summaries

**Source location:** attachment line 5720.

**Linked frontend screen contracts:** FS-36; FS-21; FS-15. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Conditional on approved document processing/provider policy; authorized document participants. Phase 9.

Support optional OCR/extraction of rent, deposit, parties, dates, notice, utilities and agreed lock-in clauses from authorized rental documents or invoices. Display the source page/region or text span next to each proposed field, confidence/needs-review indicator where meaningful, and before/after values. A human must validate ambiguous names, currencies, dates and amounts before a structured record is changed.

Provide plain-language, source-linked agreement/bill summaries and highlight where user questions cannot be answered from the document. A summary is not legal advice, signature evidence, stamping/registration status or authority to enforce forfeiture. Do not silently rewrite signed clauses, trigger deductions, or replace the original document with AI output.

Allow only approved document categories with minimal provider disclosure, consent/processing basis and defined retention. Police records, identity verification evidence, payment credentials and unrelated tenant documents are excluded from general-purpose AI. Treat embedded instructions, links and hidden document text as untrusted data, not system commands. OCR/extraction output passes normal schema/validation and review before submission.

**Acceptance:** Incorrect OCR cannot become a posted invoice or altered agreement automatically. Every extracted term links to its source version, denied documents never reach the provider, and deletion/revocation propagates to derived indexes and retained AI artifacts under policy.

### AI-007 — Financial and Maintenance Decision Support

**Source location:** attachment line 5734.

**Linked frontend screen contracts:** FS-36; FS-20; FS-34. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must for entitled analysis features; scoped financial/operational viewers. Phases 9–10.

Add Ask about this report and Review unusual items on reporting screens. The assistant may explain already computed KPIs, identify unusually high utility usage, repeated appliance faults, duplicate-looking expenses, unresolved approvals and prolonged vacancy, and propose investigation tasks. Provide the actual comparison period, baseline, sample/coverage, amount and source rows; insufficient history produces no confident anomaly claim.

Metric calculations, forecasting inputs and account balances remain in deterministic reporting services. The model can summarize approved aggregates and selected authorized records; it cannot issue arbitrary SQL or change the metric definition. Proposed price comparisons use MARKET-012's normalized, sufficient permitted data, with uncertainty and no guaranteed return.

An anomaly is a review signal, not proof of tenant fraud, provider fault or debt. Do not generate tenant creditworthiness/criminality/suitability scores, automatic application rejection, deposit forfeiture, eviction recommendations or discriminatory pricing. No automated payments, waivers, fees, rent increases, ledger adjustments or changes to maintenance priority from these suggestions.

**Acceptance:** Numeric narrative reconciles to the selected report snapshot, false-positive dismissal is recorded without altering source transactions, and any user-confirmed task is authorized/idempotent. Forecast/actual and disputed/verified amounts remain distinct.

### AI-008 — Nearby-Service and Move-Planning Recommendations

**Source location:** attachment line 5748.

**Linked frontend screen contracts:** FS-35; FS-26; FS-36. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must for offered Premium assistance; eligible service requester. Phase 7A, enhancements in 9.

Recommend laundry, cleaning, tiffin, movers or repair businesses from approved current provider profiles using the user's selected locality, service need, required date/window, explicit budget and supported coverage. Ask for clarification where necessary; do not assume that an advertiser within a radius serves the address or has confirmed availability. Show business verification scope, disclosed pricing/estimate basis and separate sponsored placement.

Suggest a moving checklist from the user's actual approved timeline, pending agreements, handover tasks and elected services. A generated plan cannot change a lease deadline or grant entry. Provider selection/request still goes through explicit sharing consent, request preview and SERVICE-PREM commands; service recommendation does not submit a request, book or pay automatically.

Do not expose a requester's landlord/tenant records to businesses. Personal service histories remain private from property managers unless explicitly authorized. Recommend a maintenance/helpdesk path where the issue belongs to landlord operations without silently transferring the private request.

**Acceptance:** Expired/blocked/out-of-area businesses never appear as valid matches, exact address is shared only by consent, membership expiry preserves existing service cases but does not allow new gated discovery, and no fabricated ratings/discounts or provider availability appear.

### AI-009 — Shared AI Gateway, Retrieval, Permission, Cost, and Action Controls

**Source location:** attachment line 5762.

**Linked frontend screen contracts:** FS-35; FS-36. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must before any AI feature is enabled. Phase 1 design, Phase 7A first consumer inference, Phase 9 workspace expansion.

Render or stream authorized AI responses and user-confirmed action results; the frontend never holds model-provider secrets or grants tool authority. AI is optional infrastructure and cannot become an additional business-state authority.

Use the pipeline: validate request/identity → enforce feature entitlement and select funding owner → apply resource-level visibility/hard constraints → retrieve minimal permitted facts → call configured capability/model with bounded context → validate output/schema/evidence/candidate IDs → recheck access/staleness → deliver labeled result or safe fallback. Public rental candidates and private sale/provider/authority data have separate retrieval policies; never retrieve forbidden content and rely on the model to hide it.

Use permission-scoped recommendation results tied to current source versions. Invalidate incompatible results on grants, publication, source, language or model/prompt changes. Do not expose private content through shared recommendation caches.

Do not treat listings, documents, messages, provider output or retrieved text as trusted instructions. Restrict tools to explicit typed operations; no arbitrary shell, SQL, network fetching, secret retrieval or generated URL execution. For actions, create an expiring proposal containing exact operation/arguments/resource versions. User confirmation binds to that proposal, followed by fresh authorization, validation and idempotency through the existing domain command. Signing, police decisions and autonomous financial approvals remain prohibited AI actions even if a broad chat instruction asks for them.

Configure provider retention/training settings, data-processing basis, allowed categories/regions and redaction before live processing. No private data training by default. Record minimal request metadata, model/prompt version, evidence references, latency/cost/usage and result status; do not log full sensitive prompts/results without an approved limited retention purpose. Honor user deletion/opt-out and derived-data removal under policy.

Enforce per-owner token/request/cost allowances, timeout, concurrency, cancellation and bounded retry. Reconcile usage even when the browser disconnects; do not bill two owners or double-count retried actions. A failed model call must return structured filters, existing reports, manual forms or human handoff. Never invent a recommendation or business completion merely to avoid an empty response.

**Acceptance:** Injection, cross-account/workspace access, source revocation during inference, replayed confirmations and malformed outputs cannot disclose unauthorized data or mutate records. Every advertised AI capability has a kill switch, quota handling and tested non-AI fallback.

### AI-010 — Recommendation Quality, Feedback, Evaluation, and Administration

**Source location:** attachment line 5782.

**Linked frontend screen contracts:** FS-35; FS-36. Apply shared contract rules and this requirement's business rules together.

**Priority/roles/phases:** Must before production AI launch; authorized AI operations/product reviewers. Phases 7A/9, release verification in 12.

Provide `/admin/ai-operations` for capability/provider configuration, prompt/model versions, approved data categories, quotas/costs, latency/failure rates, safety incidents and rollout/rollback. Do not let ordinary admins inspect every tenant conversation; review datasets use consented/synthetic/de-identified data and purpose-limited reviewer access. Provider/model upgrades follow regression and staged rollout rather than silent drift.

Build a fixed evaluation set spanning budget/locality/date/amenity constraints, unknown prices, no matches, PG/bed comparisons, cold-start users, inaccessible listings, revoked grants, sparse reviews, multilingual/ambiguous requests, misleading source text and malicious instructions. Compare to structured-search/deterministic ranking before adopting semantic or learned reranking.

Measure hard-constraint violation rate, inaccessible/stale candidate rate, supported factual claims, top-k relevance using reviewed judgments/explicit feedback, diversity within constraints, no-match recovery, user correction rate, latency, fallback and cost per successful task. Critical access/permission and hard-constraint regressions block release; define numeric quality/cost/latency thresholds before evaluation and report actual results. Do not optimize only clicks or sponsored conversion at the expense of fit/privacy, or call a click a successful tenancy.

Use explicit feedback for iteration with opt-out and retention rules. Separate anonymous aggregate product analytics from personal recommendation history; do not infer sensitive attributes from preferences or complaint/payment data. Confidence language must reflect calibrated evidence/coverage, not a raw LLM probability invented in prose.

**Acceptance:** A versioned evaluation report records baseline comparison, failures, known limits and rollout decision. Tests detect unsupported facts, polluted retrieval, stale data and denied actions; a failed evaluation can disable the feature without disabling ordinary rental workflows.

### TOUR-001 — Tour Authoring and Publication

**Source location:** attachment line 5850.

**Linked frontend screen contracts:** FS-23. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Authorized landlord/manager; Must. Tour publication is an owner-controlled listing capability, separate from the viewer's consumer membership.

**Frontend:** `/workspace/properties/{propertyId}/units/{unitId}/tours`. Create a tour, upload supported panoramic scenes, connect navigable hotspots, add room labels and descriptions, attach a floor-plan reference, preview, publish, unpublish, replace, and archive versions. Display processing progress, capture date, unit association, and validation errors.

**Authoritative result dependency:** Handle media type/size/dimension and configured storage/processing quota errors, ownership and publication-consent checks. Show draft, uploading, processing, ready, published, failed and archived. Keep the published version stable while its replacement processes. Basic manual authoring is available on all workspace plans within configured storage limits; paid plans may expand capacity/analytics.

**Acceptance:** Only an authorized publisher can expose a tour for an eligible listing; failed processing does not expose partial assets or destroy the last valid version. One unit's tour cannot be attached to an unrelated workspace's listing. Tours must use actual property media, with edited/staged content disclosed.

### TOUR-002 — Interactive Viewing and Accessible Fallback

**Source location:** attachment line 5862.

**Linked frontend screen contracts:** FS-23. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Visitor/prospective tenant/rental tenant; Must.

**Frontend:** `/listings/{listingId}/tour`. Provide interactive panoramic navigation, room hotspots, zoom/fullscreen where supported, floor-plan navigation, room descriptions, capture/update date, and a persistent book-appointment action. Support mobile touch and keyboard navigation. Provide static images, textual room descriptions, and ordinary listing navigation when immersive rendering is unavailable or the visitor chooses a low-data mode.

**Authoritative result dependency:** Show only published authorized media; private previews require current permission and expiry. Public tour viewing must not require paid membership. Handle expired media access and rate limits.

**Acceptance:** A visitor can navigate multiple rooms and book a viewing from the tour. A device without supported graphics still exposes equivalent property information and appointment actions. Unpublished/removed tours cease to be accessible through application-controlled publication URLs, subject to documented cache expiry; public media already downloaded cannot be recalled.

Distinguish a navigable 360° panorama tour from a geometric 3D model. Implement linked 360° scenes as the mandatory baseline. True mesh/digital-twin rendering, measurements, VR, and external provider embeds are conditional enhancements; do not label panorama-only content as an accurate measured 3D model. Show the format truthfully in the UI.

### TOUR-003 — Media Privacy, Integrations, and Premium Notes

**Source location:** attachment line 5876.

**Linked frontend screen contracts:** FS-23. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Publishers and viewers; Must for privacy/notes, conditional for third-party integrations.

Support reporting outdated/inaccurate tours and private information visible in imagery. Provide publisher review and replacement/redaction workflows. Require authority to publish occupied interiors; avoid exposing personal documents, faces, valuables, or access/security details. Strip unnecessary location metadata. Keep user notes private unless explicitly shared.

Provider embeds require a configured allowlist, validated URLs, restrictive content security policy, safe iframe permissions, provider privacy disclosures, and fallback content. Do not fetch arbitrary user-supplied URLs server-side. External credentials and paid provider contracts are explicit dependencies.

Premium users can bookmark rooms, attach private notes, and compare saved tour observations. Expiry disables premium editing/expansion according to policy while preserving read/export access to personal notes. Aggregate tour analytics for eligible publishers without exposing individual viewer histories without authorization.

**Acceptance:** Malicious upload/embed inputs are rejected; cross-account notes access fails; a privacy report reaches an authorized moderation queue; provider downtime leaves a usable listing.

### APPT-001 — Unified Property Viewing Booking

**Source location:** attachment line 5890.

**Linked frontend screen contracts:** FS-12. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Prospective tenant/applicant and authorized host; Must for all consumer plans. This expands the existing VISIT module rather than creating a second appointment system.

**Frontend:** `/listings/{listingId}/appointments`, `/tenant/appointments`, and `/workspace/visits`. Select in-person or hosted video viewing, available date/time, timezone, party size, accessibility needs, and contact preference. Show duration, location/meeting instructions, host confirmation policy, and cancellation terms before submission. Verify contact details through the configured identity flow.

**Authoritative result dependency:** Show authoritative host/property availability, hours, holidays/blackouts, lead time, duration, buffers, capacity, holds and expiry. Support host-confirmed and explicitly enabled instant-confirm booking. States: requested, held, confirmed, declined, reschedule-pending, cancelled, completed and no-show, with actors/reasons. A request is not a confirmed slot; a confirmed visit is not a rental reservation.

**Acceptance:** Availability and authorization are rechecked during commit. Concurrent requests cannot overbook a host/resource, including across properties and booking modes. Expired holds release capacity. Idempotent retries return the existing appointment. The tenant and host see authoritative state after reload.

### APPT-002 — Rescheduling, Reminders, Calendar, and Virtual Visits

**Source location:** attachment line 5902.

**Linked frontend screen contracts:** FS-12. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Appointment participants; Must for lifecycle/reminders/calendar export; conditional for external calendar sync and integrated video providers.

Support tenant/host rescheduling proposals, acceptance, cancellation reasons, no-show/completion recording, and service disruptions. Preserve the original confirmed slot until an approved replacement is reserved atomically, or clearly communicate an explicitly chosen cancellation-first policy. Handle unit withdrawal or new occupancy by cancelling or reconfirming affected future appointments with an audit trail.

Appointment confirmation and notification delivery are separate visible outcomes. Include in-app confirmation/reminders; use email/SMS/WhatsApp only where consent, configuration, and channel funding permit. Offer ICS calendar export with stable event identity and updates/cancellations; external two-way calendar synchronization is conditional. Store instants correctly and display the selected timezone, defaulting to Asia/Kolkata.

Provide host-managed meeting instructions and secure meeting-link access for confirmed video appointments. Provider-generated links are conditional on integration. Do not expose private links or full participant contact details on public pages. Recording is off by default and requires explicit consent/policy if enabled.

**Acceptance:** Duplicate reminders are deduplicated; failed delivery does not cancel a booking; unauthorized users cannot obtain meeting links; rescheduling cannot consume two confirmed slots or silently lose the original.

### APPT-003 — Premium Waitlist and Scheduling Assistance

**Source location:** attachment line 5916.

**Linked frontend screen contracts:** FS-12. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Premium consumer and assigned platform concierge; Must.

Allow joining a listing-specific waitlist, choosing preferred windows, and receiving expiry-bound slot offers. Allocate offers transactionally using a published ordering policy. Premium scheduling assistance creates a tracked request to coordinate a viewing; it cannot force host acceptance. Offer conversion follows the same availability, authorization, and idempotency checks as ordinary appointments.

**Acceptance:** Concurrent waitlist acceptances cannot overbook; expired offers release capacity; users can leave the waitlist; membership expiry retains existing confirmed appointments. Any concierge access is scoped, time-limited, and audited.

### TEN-SUB-001 — Personal Subscription Catalog and Entitlements

**Source location:** attachment line 5926.

**Linked frontend screen contracts:** FS-24. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Prospective or active rental tenant; Must.

**Frontend:** `/tenant/membership` shows Free/Premium comparison, monthly/annual pricing when configured, included quotas, support hours/targets, renewals, cancellation/refund terms, and dependencies on host/provider availability. Do not offer checkout for unconfigured prices or unavailable benefits.

**Authoritative result dependency:** Show account-owned membership independent of workspace and occupancy, including versioned terms, eligibility, time-limited grants, trial rules when enabled, usage and effective entitlements. Membership follows the person across authorized tenancies; co-tenants do not inherit it automatically.

**Acceptance:** A person without a workspace can subscribe; switching workspace does not change personal membership; landlord upgrades do not grant consumer Premium automatically and landlord downgrades do not cancel a consumer subscription. Sponsored benefits are conditional explicit grants, not inferred access.

### TEN-SUB-002 — Consumer Checkout and Billing Lifecycle

**Source location:** attachment line 5938.

**Linked frontend screen contracts:** FS-24. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Subscription holder and scoped billing support; Must.

Implement server-priced checkout, verified payment callbacks, payment status recovery, invoices/receipts, monthly/annual periods, renewal/mandate consent where supported, cancellation at the configured effective date, failed-payment/grace handling, refunds/credits, reversals, and account billing history. Use draft/incomplete, trialing where enabled, active, past-due, suspended, and cancelled/expired states with explicit transition rules. Retain the paid-through date and cancellation schedule separately.

Use a distinct payment purpose `TENANT_SUBSCRIPTION`, alongside `WORKSPACE_SUBSCRIPTION`, `RENT`, and `DEPOSIT`. Consumer and workspace subscriptions may reuse provider adapters, but must retain separate subscriber identity, invoices, payment allocations, reconciliation, and metrics. Never debit rent/deposit balances to buy Premium or let one purpose's callback settle another purpose's invoice.

**Acceptance:** Browser return does not activate Premium. Duplicate callbacks activate it once; closed-browser success recovers; refunds/reversals apply the published entitlement policy; cancellation is visible and does not delete tenancy data. Essential workflows remain usable after expiry.

### TEN-PREM-001 — Enhanced Search, Alerts, and Comparison

**Source location:** attachment line 5950.

**Linked frontend screen contracts:** FS-11; FS-25. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Premium consumer; Must.

Implement expanded favorites, named shortlists, private notes, saved search criteria, configurable immediate/digest alerts, and side-by-side comparison of rent, deposit, recurring charges, amenities, availability, location, and tour notes. Show known and unknown costs separately; compute totals only from disclosed values and documented assumptions. Optional commute calculations require a configured maps provider and user-selected destination.

Apply configured quotas transactionally, deduplicate alerts, honor opt-outs/quiet hours, and link only to currently visible listings. Premium alone does not expose unpublished/private properties. The explicitly published restricted SALE catalog requires both verified rental residence and active paid membership; it does not grant access to other private workspace data. Do not invent reviews, availability, verification, travel times, or “exclusive” stock.

**Acceptance:** Matching alerts respect preferences and current listing access; retries do not resend the same event; comparison does not imply unknown charges are zero; saved personal data cannot be read by other users.

### TEN-PREM-002 — Reusable Application Profile and Private Document Vault

**Source location:** attachment line 5962.

**Linked frontend screen contracts:** FS-25; FS-21. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Premium consumer; Must.

Provide a private profile with household/preferences and reusable documents, version history, expiry reminders, and explicit per-application sharing. Show recipient workspace, selected fields/documents, purpose, and access duration before granting access. Reusing a document never automatically submits an application or implies verification.

Personal vault objects belong to the consumer account. Landlords receive only explicitly granted application-scoped access or an authorized submission snapshot. Revocation stops future shared access but must explain any lawful retention of already-submitted application records; do not promise recall of downloaded copies. Support export/deletion requests and disclose retention rules.

**Acceptance:** One consent does not grant access to other workspaces; grants expire and revoke server-side; account recovery cannot bypass verification; membership expiry preserves access/export while restricting premium additions according to policy. Free tenancy-specific uploads remain available.

### TEN-PREM-003 — Rental Search Assistant and Personal Insights

**Source location:** attachment line 5974.

**Linked frontend screen contracts:** FS-25. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Premium consumer; Must for capability and fallback; external AI requires configuration.

Provide metered assistance for search preferences, listing comparison, move planning, and authorized account questions. Use current approved listing data and label uncertainty. Carry over the existing AI confirmation, injection defense, privacy, and human-handoff controls. Consumer AI use is metered against the personal subscription, independently of a landlord's AI allowance.

Provide personal expense categories, budgets, reminder preferences, and exports from the user's authorized rental/payment records. Cross-workspace insights aggregate only records the user may access; never expose co-tenants' private payments or landlord ledgers. Show reported, verified, refunded, disputed, and projected amounts separately. Consumer insights cannot modify authoritative rent invoices.

**Acceptance:** The assistant cannot sign agreements, submit payments/applications without authorized confirmation, approve deductions, or infer access from Premium status. Quota/provider failure retains standard search/helpdesk. Insights reconcile to authorized source records.

### TEN-PREM-004 — Concierge, Move Organizer, and Member Benefits

**Source location:** attachment line 5986.

**Linked frontend screen contracts:** FS-25. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Premium consumer and assigned platform support; Must for organizer/support requests; partner benefits conditional on approved arrangements.

Implement personal moving checklists, task due dates, reminders, and concierge requests with category, allowance, assignment, progress, response targets, and closure/reopening. Show actual service hours and response targets from configuration. Priority applies to platform support, not emergency maintenance severity or landlord obligations. Do not promise 24/7 staffing unless operationally provided.

Where approved partners exist, display offer eligibility, location, expiry, exclusions, redemption instructions, usage limits, and support responsibility. Deduplicate redemptions. Never fabricate partner discounts or include automatic service booking, commissions, insurance, credit reporting, lending, or payments to providers without a separately approved integration and commercial scope. The SERVICE-PREM requirements authorize tracked service requests and explicitly agreed scheduling, without automatic payment or guaranteed fulfillment.

**Acceptance:** Requests persist and reach an authorized queue; exhausted allowances produce clear alternatives; expiry follows a disclosed policy for open requests; existing tenancy maintenance stays available; unavailable partner offers are not advertised as purchasable benefits.

### TEN-ADMIN-001 — Consumer Operations and Entitlement Boundaries

**Source location:** attachment line 5998.

**Linked frontend screen contracts:** FS-06; FS-24. Apply the linked frontend contracts and the specific rules below together.

**Roles/priority:** Authorized platform administrator; Must.

Manage consumer plan versions, prices, quotas, promotional grants, payment investigations, reason-coded credits, feature availability, and consumer support access. Track consumer subscriptions, revenue, renewal failures, usage and cancellation separately from landlord SaaS metrics. Define metric periods and treatment of refunds/trials.

Evaluate access using resource-specific policies: public listing publication for public tours; authenticated participant/host authority for appointments; account ownership for personal membership/vault/notes; explicit grants for sharing; and workspace/property/tenancy permissions for rental records. Premium is an additional entitlement, never a substitute for those checks.

Personal membership, notes, vault, insights and consumer support remain account-scoped. Workspace associations require explicit valid grants; switching landlord workspaces must not transfer ownership of personal data.

Channel funding must be explicit: workspace operational messages use workspace allowance; personal Premium alerts/concierge messages use consumer or configured platform allowance. Authentication and essential in-app notices must not depend on a landlord's premium messaging quota. Never silently charge both ledgers for one event.

**Acceptance:** Neither membership nor support role enables cross-account/cross-workspace data access. Changed/expired entitlements are invalidated promptly and rechecked during mutations. Administrators cannot silently change historical contracts.

## Cross-module acceptance and scope conditions

These source scenarios supply required user-visible outcomes, including concurrent use and provider failures. Internal test mechanisms are outside frontend implementation scope; acceptance is against the integrated interface.

### External Advertising Acceptance Scenarios

1. An actual eligible unit handover creates one draft, while future notice or a remaining co-tenant prevents a false whole-unit vacancy ad.
2. Repair-blocked inventory creates a blocked review task; once ready, it still requires explicit manager/owner approval before external publication.
3. Changed price, media, destination, delegation or availability invalidates stale approval before dispatch.
4. Facebook succeeds while Instagram fails; status is Partially live and retry does not duplicate the Facebook post.
5. Timeout after potential provider creation reconciles the uncertain outcome before another create request.
6. Unsupported accounts/formats, missing scopes, token expiry and disconnection produce truthful recovery paths without password collection.
7. Organic approval cannot initiate paid spend; unauthorized budget increases or duplicate jobs cannot create extra paid campaigns.
8. Allocation/occupancy occurring before, during or after publication prevents further dispatch and creates confirmed pause/removal or visible manual cleanup tasks.
9. Provider rejection, unavailable analytics and takedown limitations are shown honestly; landing-page availability stays authoritative.
10. Published copy/media/lead records exclude the departed tenant's identity, documents and belongings, and every action remains workspace/property authorized.

### Scope Reconciliation and Priority Rules

- The supplied catalogue's exclusion of sales applies to that catalogue only. Preserve the separately requested SALE-001 through SALE-005 restricted sale marketplace; keep it distinct from public rental discovery.
- Do not downgrade existing LMT Must requirements to optional because the catalogue calls property management, payments or lifecycle P1/P2. Its P0/P1/P2 labels are source priorities, not a replacement for LMT's requirements or phase gates.
- Default LMT to direct owner/authorized manager rental listings. Broker participation is an explicitly configurable conditional extension, disabled until the participation/authorization/disclosure policy is approved. Never advertise a broker-listed property as owner-direct.
- Keep core inquiry, ordinary visit booking, applications and essential tenancy functions usable on Free. Optional contact-access allowances, promotions and assisted-search packages must not silently paywall existing tenant/landlord communication or replace the consumer/workspace plan models.
- Maps, federated login, official stamping/registration, authorized employment/reference checks, physical document delivery, payment settlement integrations and other external capabilities require supported providers and approved configuration. Missing access must be visible as Conditional/Blocked, not mocked as live.
- Existing Premium service requests and explicitly agreed scheduling remain authorized; service checkout/commissions and automatic booking remain conditional. Referral rewards and locality community reviews also need explicit policies before activation.
- The modules below are logical additions to the modular monolith, not instructions to create separate microservices or a duplicate marketplace.

### Marketplace Integration Contracts and End-to-End Acceptance

Extend existing `Listing`, `Inquiry`, `VisitAppointment`, `RentalApplication`, identity, plan, notification and reporting modules. Introduce only missing concepts such as `AdvertiserAuthority`, `ListingReviewCase`, `ListingFreshnessCheck`, `LeadStageEvent`, `ContactAccessGrant`, `ContactUsageReservation`, `RentalOfferVersion`, `SharedShortlistGrant`, conditional `BrokerProfile`, `PromotionOrder`, `AssistedSearchCase`, `GeoLocality`, `MarketInsightSnapshot`, `ContentVersion` and conditional `ReferralRewardLedger`. Avoid duplicated financial or capacity invariants.

Add or extend `/listings`, listing details/contact/messages, seeker shortlists and offers, `/workspace/leads`, `/workspace/listing-reviews`, `/workspace/promotions`, `/admin/marketplace`, `/admin/geography`, `/admin/content` and scoped assisted-search queues. Server-side permission, publication visibility and entitlement checks apply to every detail/search/export/async operation, not only navigation. Public discovery is a curated listing projection, not access to workspace tables.

Verify at least:

1. Owner publishes only after required checks; a rejected listing is corrected/reviewed; stale/occupied listings stop appearing despite cached search or purchased promotion.
2. Seeker combines localities/attributes, compares normalized offers, sends an inquiry, obtains allowed contact access, messages and books a visit without purchasing mandatory Premium.
3. Concurrent contact requests consume allowance exactly once; blocked contacts and private shortlists never leak through shares, previews or logs.
4. PG bed/room inventory, shared amenities and per-bed pricing remain distinct and capacity-safe.
5. Offers/counteroffers expire and require authority; accepted terms transfer to the lease without double allocation or implicit signing/payment.
6. Disabled brokers cannot publish; enabled brokers need per-property authority and truthful brokerage disclosure.
7. Paid promotions cannot override approval/availability or launch external paid ads; duplicate callbacks cannot double-grant credits/duration.
8. Assisted-search agents coordinate only with user authorization; absent staffing produces an honest unavailable/limited offer.
9. Locality statistics with insufficient or private data do not invent market rents; recommendation opt-out and approximate-location privacy work.
10. Payer success and delayed beneficiary settlement remain separate; payment fees and provider references reconcile without duplicate rental receipts.
11. Conditional stamp/registration, reference-check, moving/payment, reward and content workflows remain labeled by actual provider/configuration readiness.
12. Owner → moderated listing → search/inquiry → visit/negotiation → application/verification → agreement/move-in completes as one integrated journey with existing role workflows.

### Shared Workflow Contract

Every workflow must specify the initiating role, prerequisites, responsible actor, permitted transitions, required evidence, approval authority, deadline if configured, resulting records, notifications, exception path, and completion criteria. Persist these details server-side; a dashboard checkbox cannot substitute for completion of the underlying business action.

Provide role-specific work queues with assigned-to-me, awaiting-my-approval, awaiting-tenant, overdue, blocked, and completed views. Every task links to its source record and shows its reason, next action, responsible person, due date, and audit history. Task states are open, in-progress, awaiting-input, awaiting-approval, blocked, completed, and cancelled. Task completion and business state are distinct; derive completion from authoritative events where possible. Deduplicate event-generated tasks and reschedule reminders safely.

Permission is determined by account identity, workspace membership where applicable, property/tenancy scope, delegated authority, effective dates, approval limits, business state, and applicable entitlement. A landlord workspace owner is not automatically the legal owner/signatory of every property. A property owner is not automatically a workspace billing administrator. Tenant Premium never grants property-management permissions.

Basic approvals, assignment, inspection records, expense records, and handover are core operational functionality. Paid plans may extend automation, configurable multi-stage policies, bulk actions, SLA analytics, and reporting; they must not block essential approval or lifecycle completion.

### Role Journeys and Handoffs

| Stage | Landlord / authorized owner | Property manager | Applicant / rental tenant | Authoritative outcome |
|---|---|---|---|---|
| Portfolio setup | Register workspace, record property/ownership, set policies and delegation | Accept scoped assignment, inspect baseline, flag missing information | No private access before invitation/application | Approved property baseline and active delegated scope |
| Prepare vacancy | Approve pricing, deposit, eligibility and publication | Inspect readiness, inventory, media/tour and listing completeness | Browse published details and tours | Rentable inventory ready and authorized listing published |
| Acquisition | Decide reserved approval items and exceptions | Respond to inquiries, host visits, review applications | Book viewing, apply, provide consent/documents, answer clarification | Audited application decision and capacity-safe hold |
| Agreement | Confirm authorized signatories and final terms | Prepare/review draft and coordinate signing | Review/correct terms and sign or upload permitted documentation | Immutable agreement version and separately tracked legal formalities |
| Move-in | Approve exceptions within authority | Collect evidence/readings, confirm funds status, hand over keys | Acknowledge condition/inventory, record disputes and actual handover | Traceable possession/occupancy activation |
| Recurring operation | Review collections, expenses, approvals and portfolio issues | Run billing/readings, reconcile evidence, handle maintenance and inspections | Pay/report payments, dispute charges, request repairs and consent to access | Auditable rental ledger and tracked service delivery |
| Renewal / notice | Approve new terms or termination decisions | Coordinate offers, notice changes and scheduling | Accept/reject offer or submit/change notice | New authorized agreement terms or scheduled exit |
| Move-out / turnover | Approve deductions, refunds and exceptions | Inspect, record handover, prepare settlement and readiness tasks | Review/dispute settlement, return access, confirm refund details | Separate physical handover, financial closure and unit readiness |
| Delegation / ownership change | Authorize scope change and successor | Hand over assigned work and lose revoked permissions | Receive verified contact/payment instructions where relevant | Effective-dated authority and uninterrupted record history |

### Mandatory Role-Workflow Acceptance Journeys

1. Owner onboards a property, records authority/policies, delegates a manager, approves an above-limit repair and sees a reconciled expense statement.
2. A manager accepts limited scope, prepares a vacancy/tour, hosts a visit, processes an application and requests owner approval for a rent concession before agreement execution.
3. An existing tenant redeems a secure invitation, disputes an imported opening balance and uses the portal without applying again or buying Premium.
4. A new tenant completes discovery, appointment, application, agreement review, payment and move-in with a disputed inventory item retained.
5. Monthly billing handles missing/incorrect readings, duplicate jobs, partial payments, pending provider outcomes, charge disputes and approved adjustments without rewriting history.
6. A maintenance estimate exceeds the manager's authority; approval is required; the tenant declines the first entry window; rescheduling and repair evidence reach the same tracked ticket.
7. Concurrent co-owner decisions, changed approval amounts and revoked manager access cannot authorize stale or unauthorized operations.
8. A tenant declines a renewal, submits and changes notice, moves out with an unpaid/disputed balance, and tracks a partial refund after physical handover.
9. A co-tenant exits while others remain; access and agreements change only through the authorized workflow, with no leakage of private records.
10. A manager leaves while visits, repairs and approvals remain pending; tasks transfer explicitly, old access is revoked, and provider callbacks still reconcile safely.
11. A property changes authorized owners/payees without rewriting prior agreements or financial attribution; the tenant receives verified updated instructions.
12. A property with an active tenancy or unresolved obligations cannot be destructively archived; a safe retained/limited-access state remains available.
13. Owner, manager and tenant can each complete their essential role journey on mobile with keyboard/screen-reader support and recover from interrupted submissions.

### Additional Chart and Decision-Metric Acceptance Scenarios

1. Verify every chart series against a deterministic table/ledger fixture, including granularity changes and cross-property totals.
2. Assert collection rates for partial payment, old arrears, advance allocation, credits, refunds, zero obligations and not-yet-due rent.
3. Verify weighted occupancy across new/retired inventory, beds, repairs and temporary relocation; property percentages must not be averaged unweighted.
4. Test leasing cohorts with repeated visits, direct applications, imported tenancies and conversions occurring after the reporting interval.
5. Test maintenance performance with unresolved overdue cases, missing targets, reopening, paused entry access and small samples.
6. Reconcile operating cash surplus and expense allocations while excluding deposits, owner contributions and non-operating items; missing budgets remain N/A.
7. Forecast from disclosed assumptions and incomplete source data without modifying authoritative records or presenting predictions as actual results.
8. Verify previous-period comparisons, zero denominators, percentage-point changes, stale projections and metric-version changes.
9. Verify chart-point drill-down, accessible data tables, mobile interactions, authorized export and cached/saved-view isolation.
10. Verify alert deduplication, snooze/reopen behavior, evidence links and current permission checks before any explicitly requested follow-up action.

### Required Dashboard Acceptance Dataset and Scenarios

Create deterministic fixtures and automated checks with known expected values:

1. Multiple properties, shared beds, co-tenants and one person with two tenancies: verify distinct people, tenancy counts and occupied capacity without fan-out duplication.
2. A month containing partial payments, a prior-month arrears receipt, an advance applied to current dues, a credit, a deposit receipt and a utility charge: assert each rent KPI exactly, including why period cash receipts differ from dues paid.
3. Payment pending, duplicate webhook, reversal, rent refund and later manual verification: verify state-specific inclusion, historical restatement rules and ledger reconciliation.
4. Month/year boundaries, inclusive To date, leap day and custom invalid ranges: verify backend-resolved dates and snapshot semantics.
5. Current-month future due date: show it in the labeled full-month companion, not silently in month-to-date dues; upcoming planned events must not appear as actual activity.
6. One tenant creates three requests, a prior-period case remains open, and a resolved case reopens: assert created count, distinct requesters, resolution events and backlog independently.
7. Future move-in, imported historic tenant, overdue planned arrival, withdrawn notice and physically departed tenant with pending refund: verify all four lifecycle panels and planning-horizon behavior.
8. A manager scoped to one property cannot see other properties in cards, charts, drill-downs, counts, exports, caches or URL-manipulated requests.
9. Changing filters rapidly, partial backend failure and stale projections show the correct range and honest availability/freshness rather than zero or old data presented as current.
10. Card-to-detail navigation, bulk reminder preview, payment arriving before action commit, partial retry, and export retain filters and enforce current authority.
11. Historical period before imported data coverage is reported as incomplete rather than reconstructed from current balances or occupants.
12. Keyboard/screen-reader and mobile users can apply custom dates, read charts as tables, open lifecycle queues and return to their filtered dashboard.

### Police-Verification Acceptance Scenarios and Phase Gates

Test at least:

1. Unsupported jurisdiction and no official integration produce clear assisted/unavailable states, not a fabricated clearance.
2. Request retries, co-tenant subjects and changed application records do not duplicate or merge unrelated cases.
3. A public user, landlord or platform admin cannot self-register as police; revoked/unassigned officers cannot read or decide cases.
4. Cross-jurisdiction routing requires authority and leaves an audit trail; police access is limited to the case packet.
5. Submitted document corrections preserve the reviewed version; sensitive attachments are excluded from general document lists, notifications and AI indexes.
6. Missing data, source downtime and unresolved identity matches yield pending/inconclusive review, not automatic pass/fail.
7. Only the authorized decision-maker can issue a result; required supervisory approval and concurrent-decision guards are enforced.
8. A landlord-uploaded certificate remains unverified until approved authenticity checks succeed; acknowledgement alone never becomes Passed.
9. An adverse decision has permitted reasons, review/correction access and no automatic eviction, public labeling or loss of basic tenant services.
10. Superseded decisions update prerequisite/status displays without deleting original evidence; duplicate/out-of-order callbacks reconcile safely.
11. Officer departure, subject data request, retention expiry and integration uncertainty preserve the approved authority/security policy.
12. Applicant and officer workflows are usable on mobile/keyboard, with accessible case statuses and honest unavailable/partial states.

**Phase 0:** Record jurisdiction/process sources, participating authority approval, lawful processing basis, evidence/decision vocabulary, portal operating mode and live-integration prerequisites. Do not make unsupported legal assumptions.

**Phase 1:** Establish separate authority identity, MFA/access controls, private evidence storage and audit foundations.

**Phases 3–4:** Implement applicant/requester forms, case linkage, document/version/consent flow, status tracking and policy-based onboarding prerequisite interfaces.

**Phase 6:** Complete POL-VER-001 through POL-VER-007 in an explicitly configured authorized or sandbox mode, including officer queues, decisions, review and applicant tracking. Production official outcomes require real authority onboarding and approved operating procedures; record missing arrangements as launch blockers for the live verification feature rather than faking completion.

**Phases 9–10:** Add approved workload/reporting integrations where entitled and authorized; official decision integrity and applicant rights cannot be paid upgrades.

**Phase 12:** Verify case isolation, privacy, evidence integrity, decision provenance, callback recovery, access revocation and authority-specific operational readiness before live enablement.

### Verified-Review Acceptance Scenarios and Phase Gates

Test at least:

1. Applicant, visitor, future tenant and unverified imported resident cannot publish; an independently verified current/former tenant can.
2. Two verified co-tenants can each review, while one person's repeated tenancies or concurrent submissions cannot multiply their rating weight.
3. A reviewer edits or withdraws a review; versions, publication, aggregates and caches remain consistent.
4. Missing category ratings are excluded rather than treated as zero; no reviews displays no rating; every distribution reconciles to published count.
5. A negative review survives an owner objection unless a moderator finds an actual policy violation; removal/temporary hiding and appeal remain auditable.
6. Owner responses cannot expose private financial/identity details or modify stars; workspace managers cannot impersonate platform moderators.
7. Revoked current workspace access, physical move-out, unpaid rent or landlord/consumer subscription expiry does not invalidate legitimate historic residence by itself.
8. Public endpoints never reveal verification documents, exact unit identity or hidden private fields; malicious text and cross-property IDs are rejected safely.
9. Property management changes preserve valid reviews; new landlords cannot reset rating history through ordinary property edits.
10. Review forms, star selectors, rating summaries, sorting and moderation status are usable on mobile, with keyboard navigation and screen-reader labels.

**Phase 0:** Define residence evidence, conflict-of-interest policy, moderation responsibilities, review limits and retention.

**Phases 3–4:** Prepare public property profile/review surfaces and secure tenancy/residence-verification links; do not fabricate review content while the feature is incomplete.

**Phase 6:** Deliver REVIEW-001 through REVIEW-005 end to end with current/former tenant access, verified reviews, official responses, moderation, aggregates and acceptance scenarios.

**Phases 9–10:** Add entitled review trend/feedback analytics without paywalling review submission, truthful ratings or fair moderation.

**Phase 12:** Verify abuse resistance, privacy, eligibility integrity, accessibility, aggregation and moderation recovery in release testing.

### Amenity acceptance scenarios

Automated acceptance coverage must include:

1. Present versus Absent versus Not assessed versus Not applicable, including broken and temporarily removed items.
2. Refrigerator/freezer aliases, configurable catalog types and retired catalog history.
3. Multiple chairs/tables, usable-quantity thresholds, negative/insufficient stock rejection and all/any filters.
4. Shared property appliances and tenant-owned furniture without misleading public listings or duplicate asset totals.
5. Concurrent serialized-asset transfers and retried quantity imports without double assignment or duplication.
6. Move-in acknowledgment, mid-tenancy replacement, disputed damage and move-out comparison without automatic deductions.
7. Cross-workspace/property access rejection, private serial/purchase-data protection and tenant read scope.
8. Inventory dashboard/matrix/export reconciliation, unknown-data coverage and mobile/keyboard usability.

### Helpdesk Acceptance Scenarios and Phase Gates

Test at least:

1. Tenant submits a plumbing issue with photos; assigned manager acknowledges, replies, schedules with consent, records repair and obtains tenant resolution feedback.
2. A property with no active manager routes the ticket to its authorized owner; a revoked manager loses case access and open work is reassigned explicitly.
3. A broken AC estimate exceeds delegation limits, so owner approval is required; a tenant declining the first visit window can reschedule without the ticket being marked resolved.
4. Missing parts, owner approval delay or absent tenant response remains a visible blocker; target pauses/overdue labels follow actual configuration.
5. A tenant rejects proposed resolution and reopens the same case; repeated issues link to the asset/incident and escalate without losing history.
6. A billing complaint links to the financial dispute workflow and cannot change invoices through a helpdesk status edit.
7. Sensitive complaints/internal notes/worker-only details use separate visibility; a complaint against a manager has an alternate restricted escalation route.
8. Provider completion, tenant-confirmed resolution, administrative closure, cancellation and duplicate linkage remain distinguishable in dashboards and exports.
9. Concurrent status updates, retried submissions, failed notification delivery and unsafe file uploads are handled without duplicate cases or data exposure.
10. A worker cannot view tenant documents/finances or enter without authority; repair approval does not automatically charge the tenant.
11. Free/non-Premium tenants and former tenants with unresolved own cases retain the defined helpdesk access; landlord/consumer plan expiry does not erase essential records.
12. Mobile/keyboard users can submit, reply, review visit times, read updates, reopen and escalate; automated assistance does not invent resolution or replace emergency guidance.

**Phase 2:** Establish property/manager routing and delegation foundations.

**Phase 4:** Establish tenancy participation and occupant access scope.

**Phase 6:** Deliver HELP-WF-001 through HELP-WF-007 as part of the complete core tenant/manager/owner experience, with real persistence, source-of-truth states and all acceptance journeys.

**Phases 9–10:** Add entitled dispatch/SLA analytics and AI assistance while retaining basic assignment, reply, repair coordination, escalation and reopening on Free.

**Phase 12:** Verify authorization, private message/file visibility, audit, concurrency, task handover, recovery, privacy and accessibility before release.

### Rental Billing Acceptance Scenarios and Phase Gates

Use deterministic fixtures and test at least:

1. One monthly invoice with rent ₹10,000, electricity ₹1,200, maintenance ₹1,000, water ₹300 and an enrolled clubhouse fee ₹500 totals ₹13,000 before any separately applicable configured adjustments/taxes. No clubhouse enrollment means no ₹500 charge. Values are test fixtures only.
2. A verified ₹11,200 payment with explicit allocations of ₹10,000 rent and ₹1,200 electricity clears those lines, leaves ₹1,800 under the fixture above, and reports rent collected as ₹10,000 rather than ₹11,200.
3. Electricity/water meter replacement, outlier readings, estimated-to-actual correction and shared-bill rounding reconcile without duplicate consumption charges.
4. Duplicate/concurrent monthly runs and partially failed batches create no repeated rent or membership obligations.
5. Pending/duplicate payment callbacks, manual verification, advance credit, partial payment and payment reversal preserve accurate balances and receipts.
6. Optional clubhouse enrollment, cancellation, mid-period membership and new tariffs obey effective dates and accepted terms.
7. Maintenance repair approval alone cannot create a tenant charge; a disputed utility line remains traceable while other dues are paid.
8. Multiple payees, direct-to-utility payments and shared household obligations cannot inflate landlord receipts or duplicate debts.
9. Issued-invoice correction, waiver and refund reconcile through versioned adjustments without rewriting original documents.
10. Existing-tenant import, unit transfer and move-out stop duplicate billing, preserve cutover and finalize all configured categories without charging the next occupant for old dues.
11. Invoice/receipt access, bulk exports, provider callbacks and scheduled jobs reject cross-workspace or unauthorized financial actions.
12. Mobile/keyboard users can review breakdowns, pay/report a payment, download a receipt, dispute a line and follow final settlement.

**Phase 2:** Prepare charge/meter/facility configuration linked to property inventory and authority.

**Phase 5:** Deliver RENT-FIN-001 through RENT-FIN-006 and RENT-FIN-008 for core recurring billing, invoices, utility/member calculations, payments and reporting.

**Phase 6:** Complete RENT-FIN-007 and all onboarding/transfer/offboarding and dispute closure scenarios.

**Phase 12:** Verify ledger reconciliation, date/rounding boundaries, concurrent jobs, invoice integrity, payment recovery, privacy and current applicable billing requirements before release.

### Offboarding and Lock-In Acceptance Scenarios

1. **Illustrative discount only:** Base rent ₹10,000 becomes ₹9,000 at 10% or ₹8,000 at 20%, excluding electricity unless the signed offer explicitly includes it. These are test fixtures, not production pricing.
2. **Calendar boundary:** A signed six-month interval starting January 1 and ending July 1 treats June 30 departure as early and July 1 departure as outside lock-in under that precise interval; test month-end and leap-year start cases separately.
3. **Proposed full retention:** A verified ₹20,000 deposit and a reviewed early-exit clause produce a ₹20,000 proposal. It remains pending until required review/approval and settlement handling; no automatic ledger write occurs on notice submission.
4. **No double consumption:** If ₹20,000 is finally approved for retention, the same deposit cannot also offset ₹2,000 rent and ₹500 electricity. Show any separately valid remaining dues; avoid double recovery and apply approved caps/exceptions.
5. **Normal departure:** At/after commitment completion, early-exit retention is zero; valid rent/utilities/damage adjustments still follow ordinary settlement rules. Deposit refund is not automatically the whole deposit if other approved obligations remain.
6. **Payment and utility uncertainty:** Pending rent payment, estimated electricity, corrected readings and later provider confirmation update the proper balances without duplicate billing or false clearance.
7. **Date/term changes:** Changed notice date, approved waiver, landlord-initiated termination, transfer and replacement tenant follow the versioned review policy, not a hardcoded penalty.
8. **Dispute and consent:** A tenant submits notice, disputes a deduction and returns possession without being forced to accept a new clause. A revised settlement cannot reuse acceptance of a materially different version.
9. **Concurrency:** Repeated finalization, payment callbacks, deposit offsets and refund requests do not double-charge or double-spend the deposit.
10. **Separation:** Rental lock-in is independent of SaaS/consumer plans, and retention never counts as rent receipts or authorizes eviction/utility cutoff.

**Phase 0:** Verify jurisdiction-specific lock-in/discount/retention terms, exceptions, approvals, consent requirements and calculation boundaries before enabling live enforcement.

**Phases 3–5:** Implement EXIT-001 offers/disclosure and accepted agreement linkage, discounted rent schedules and final-dues calculation foundations.

**Phase 6:** Complete EXIT-002 through EXIT-006 with the existing notice/move-out/settlement journeys and all acceptance cases. Unknown enforceability blocks automatic retention, not tenant notice submission or honest physical handover recording.

**Phase 12:** Verify date boundaries, financial reconciliation, deposit conservation, disputes, audit, permissions and payment recovery before release.

### Active-Tenancy Lifecycle and State Boundaries

Use the operational journey:

`existing-tenancy onboarding or completed move-in → active occupancy → recurring operations and approved changes → renewal or notice → possession handover → financial closure and retained history`.

Do not implement this as one status field. Maintain distinct authoritative dimensions:

| Dimension | Example states / facts | Required boundary |
|---|---|---|
| Property/unit operation | Active, repair-restricted, archived | Does not determine whether an occupant has returned possession |
| Capacity allocation | Held, allocated, released, expired | Enforced per independent unit/bed and time interval |
| Actual occupancy | Scheduled, in possession, handover pending, handed over | Derived from recorded possession events, not payment status |
| Agreement | Draft, partially signed, executed, amended, expired/terminated as recorded | Expiry does not prove a person has vacated |
| Rental account | Current, overdue, disputed balance, refund pending, financially closed | Unpaid balance does not erase a physical move-out |
| Notice/renewal | None, proposed, acknowledged, changed, withdrawn, completed | Notice alone does not release capacity |
| Listing | Unpublished, published, paused, withdrawn | Future availability must be explicit and never imply current vacancy |

Agree on precise implementation states and transitions in the domain model. A derived dashboard label such as “occupied / notice acknowledged / refund pending” is not an independent mutable source of truth. Account for future scheduled allocation separately from current possession, with policies for delayed exits and overlapping commitments.

### Active-Tenancy Data, Contracts, and Acceptance Evidence

Test at least these integrated scenarios:

1. Import an occupied unit mid-month with arrears, advance rent and deposit; retry import and billing without duplicates.
2. Display a partly occupied property and separately allocated beds with accurate availability and role permissions.
3. Schedule a rent increase, reject a concurrent conflicting revision, and correct an issued invoice without rewriting it.
4. Replace a faulty/shared meter mid-period and reconcile estimates without charging either occupant twice.
5. Add or replace a co-tenant while retaining the other occupants' agreement, balances and private records.
6. Keep a home occupied during essential repairs/temporary absence and record consent, disruption updates and agreed adjustments.
7. Race two requests for one transfer destination; allow only one and recover a partially completed paired handover.
8. Retain source capacity during temporary relocation and preserve authorized return arrangements.
9. Handle overdue rent or an expired agreement without automatically changing possession, legal rights or listing availability.
10. Record physical handover with a disputed deduction and pending refund, then close finances only after verified settlement or authorized adjustment.
11. Detect an exit delay affecting future availability and route existing commitments for resolution without double allocation.
12. Verify all critical mutations, asynchronous jobs, reports and exports against workspace, property and tenant authorization.

### Nearby-Service Acceptance Scenarios and Phase Gates

Test at least:

1. Premium user discovers all configured categories, views approved business numbers and submits a request; Free directory behavior remains available as previously specified.
2. GPS denied/manual locality selection still works; proximity outside a provider's service area does not falsely indicate service availability.
3. An expired/paused offer, unverified business contact or contact-only provider cannot be represented as a successfully delivered service request.
4. Duplicate submission/notification retries create one request and do not repeatedly disclose contact details.
5. The tenant reviews shared data; the provider receives no lease, police, rent or unrelated tenant information.
6. Quote revisions, proposed dates, explicit confirmation, cancellation, no-response and disputes retain a consistent auditable history.
7. A scheduled worker has no independent right to enter; tenant service requests do not automatically charge the landlord or create payment transactions.
8. Premium expiry preserves existing request management/complaint/contact history while restricting new Premium discovery/actions.
9. Provider/staff revocation and suspension enforce business scope across APIs, private media, messages, exports and caches.
10. No contact click or provider completion claim is reported as verified service delivery/payment without its required evidence; mobile/keyboard workflows remain usable.

**Phase 6:** Preserve the basic nearby-services directory and prepare provider/category/location foundations.

**Phase 7A:** Deliver SERVICE-PREM-001 through SERVICE-PREM-006 with real approved providers, contact verification, ads, service requests, provider response portal, consent, membership controls and acceptance tests. Hide unsupported offers instead of seeding fake live businesses; test fixtures must be labeled.

**Phases 9–10:** Add entitled coverage/response analytics or explicitly approved partner integrations. Automated booking, payments and commissions remain separate scope.

**Phase 12:** Verify location privacy, provider/request isolation, abuse controls, delivery recovery, accessible journeys and historical-request continuity before live launch.

### Restricted Sale Listing Acceptance Scenarios and Phase Gates

Test at least:

1. Verified tenant + active paid Premium can view; guest, Free tenant, trial-only user, unverified paid user and paid-workspace owner without buyer eligibility cannot.
2. Verified former tenant with active paid membership follows the stated residence policy; a phone-verified applicant does not qualify.
3. Expired/reversed membership or revoked residence eligibility blocks new content fetches, saved previews, exports and media access within documented access-link limits.
4. Direct URLs, SSR/prefetch, CDN caches, notification previews, search/sitemaps and public rental-ad integrations cannot leak gated sale content.
5. A manager may draft but cannot publish without explicit sale authority and required owner/co-owner approval; purported ownership alone is not enough.
6. A buyer can view a restricted listing from another workspace without access to its private tenant/owner/financial records.
7. Occupied-property viewings require the separate occupant-entry process; sale approval or buyer Premium does not bypass it.
8. Inquiry contact sharing is consented and scoped; after membership expiry, own historical transactions remain accessible only under the limited history policy.
9. Sold/under-offer/withdrawn status updates do not terminate tenancy, transfer deposit liability or process sale payments.
10. Mobile/keyboard access, eligible-account errors, seller review, expiry, concurrent updates and moderation recovery behave consistently.

**Phase 0:** Define seller authority verification, buyer residence criteria, private-media architecture and limited sale scope.

**Phases 2–4:** Reuse property/ownership, residence, approval and occupied-viewing foundations.

**Phase 7A:** Deliver SALE-001 through SALE-005 together with the paid consumer membership gate, seller workflow, private catalog and acceptance tests. Do not advertise this Premium benefit until its verification, privacy and moderation requirements are operational.

**Phase 12:** Verify full eligibility matrix, cache/media isolation, consent, authority revocation and separation from public rental advertising before live launch.

### Capability Availability and Product Rules

| Capability | Baseline | Entitled AI extension |
|---|---|---|
| Property matching | Free structured filters, deterministic matching and truthful listing comparison | Personal Premium natural-language search, richer explanation, preference-aware shortlist assistance |
| Tenant helpdesk | Free manual ticket creation, human handling and approved guidance | Workspace-funded helpdesk AI where that workspace is entitled; personal support assistant where independently entitled |
| Owner/manager assistance | Existing manual listing, inventory, approval and reporting workflows | Workspace-plan AI drafting, summarization, anomaly suggestions and analysis according to configured catalog |
| Nearby services | Existing public directory and Premium service requests | Premium service matching within actual provider coverage and approved ad visibility |
| Document handling | Manual review, secure uploads and official execution | Explicitly authorized OCR/extraction and source-linked summaries, with human validation |

Personal AI use is funded/metered by consumer entitlements; workspace AI by workspace entitlements. Select one funding owner per operation. Never require both subscriptions for the same authorized capability, bill both ledgers, or sell an unavailable provider benefit. Basic search, helpdesk, invoices and records remain functional when AI is disabled, unavailable or over quota.

### AI phases and acceptance scenarios

**Phase 0:** Define AI use cases, entitlement/funding, provider data policy and evaluation targets. **Phase 3:** Deliver structured property matching and explanations with no provider dependency. **Phase 7A:** Add entitled natural-language search, conversational comparison, Premium service/move assistance and the shared gateway/evaluation gates. **Phase 9:** Add workspace helpdesk/listing/document/report assistance where configured. **Phase 10:** Expand only measured advanced insights. **Phase 12:** Re-run privacy, prompt-injection, action-authority, factuality, quality, latency and cost evaluation.

Required scenarios: impossible budget/no matches; required amenity unknown; property becomes occupied during inference; private sale listing appears in public retrieval; user opts out of personalization; malicious listing/document instruction; revoked document grant; malformed model output; expired confirmation; service provider out of area; wrong charge total in generated prose; tenant versus workspace quota; provider outage; repeated action submission; and insufficient-data forecasts. Every case must preserve domain authority and provide an honest fallback.

### Mandatory Additional Acceptance Scenarios

1. A visitor views a published tour without Premium and books an ordinary viewing after identity verification.
2. An unpublished/private tour, notes, or vault document cannot be accessed by changing an ID or media path.
3. Tour processing/provider failure and unsupported graphics leave a usable listing and static fallback.
4. Two consumers race for one host slot; at most one confirms, even across different properties.
5. Rescheduling, cancellation, waitlist acceptance, and expired holds cannot double-book or lose an existing confirmed slot silently.
6. A prospective tenant buys Premium without workspace membership or an active tenancy.
7. Duplicate/pending consumer payment callbacks, browser closure, refunds, and entitlement activation failures recover without duplicate charges or grants.
8. Tenant membership cannot pay a rent invoice or a landlord subscription; billing events reconcile by purpose and subscriber.
9. Landlord downgrade leaves personal Premium active; personal expiry leaves rent, documents, existing appointments, disputes and maintenance usable.
10. A profile shared with Workspace A remains private from Workspace B; revoked/expired grants stop new access.
11. Consumer premium AI and personal insights cannot expose other tenants or workspace-private records.
12. Concurrent allowance use does not exceed a configured quota; the same communication is not billed twice or to two owners.
13. Consumer plan changes preserve historical terms; cancellation and failed renewal enforce disclosed paid-through/grace rules.
14. Screen-reader/keyboard and low-bandwidth journeys complete discovery, tour fallback, booking and membership cancellation.

## Mandatory Failure and Edge Cases

Implement automated acceptance coverage for the following scenarios.

### Scenario 1 — Free Unit Quota

A Free workspace already has five counted units.

The owner attempts to create another.

Expected:

- Frontend displays the limit.
- Backend rejects unauthorized creation.
- No additional unit is persisted.
- Existing tenancy workflows remain functional.
- Upgrade options are shown.

### Scenario 2 — Concurrent Unit Creation

Two requests arrive when only one unit slot is available.

Expected:

- At most one new counted unit is committed.
- Counters remain consistent.
- Both requests cannot bypass the quota.

### Scenario 3 — Silver Upgrade

A Free customer upgrades to Silver.

Expected:

- Checkout uses server-authoritative pricing.
- Payment is verified.
- Entitlements are activated once.
- Capacity increases to 50 units.
- Existing data remains unchanged.

### Scenario 4 — Downgrade Above Limit

A Gold customer with 120 units requests Silver.

Expected:

- Frontend explains the mismatch.
- Backend applies the approved scheduled downgrade policy.
- No existing unit, tenancy, payment, or document is deleted.
- New capacity-increasing operations are restricted until compliant.

### Scenario 5 — WhatsApp Quota Exhausted

A Silver workspace reaches its messaging allowance.

Expected:

- Discretionary messages follow the configured quota policy.
- Messages are not silently billed beyond approved limits.
- In-app notification remains available where supported.
- The usage dashboard reflects authoritative consumption.

### Scenario 6 — Subscription Payment Uncertain

Payment provider reports pending while the customer closes the browser.

Expected:

- No premature activation.
- Verified reconciliation updates the subscription later.
- Refresh/retry does not duplicate payment or subscription creation.

### Scenario 7 — Workspace Isolation

A landlord from Workspace A attempts to access Workspace B's property using a modified resource ID.

Expected:

- Access denied.
- No private data disclosed.
- Security event recorded where appropriate.

### Scenario 8 — User in Multiple Workspaces

A manager belongs to two organizations.

Expected:

- User can switch authorized workspaces.
- Roles and subscription entitlements are recalculated.
- Data and cached queries do not leak between workspaces.

### Scenario 9 — Expired Subscription

A paid subscription expires.

Expected:

- Applicable access restrictions are enforced.
- Historical financial records remain protected and available according to policy.
- Tenant essential operations are not silently destroyed.
- Subscription recovery is offered.

### Scenario 10 — Plan Configuration Change

An administrator changes the Silver unit quota.

Expected:

- Plan versioning is respected.
- Existing subscriptions follow their contractual migration policy.
- Historical invoices remain unchanged.

### Scenario 11 — Provider Notification Failure

WhatsApp provider is unavailable during rent billing.

Expected:

- Rent invoices are committed normally.
- Notification attempts are persisted and retried according to policy.
- No duplicate charge or billing record is created.

### Scenario 12 — Rental Tenant Access During Landlord Downgrade

A landlord downgrades while an active tenant has outstanding rent and an open maintenance ticket.

Expected:

- Essential records are retained.
- The tenant can access necessary documents and obligations.
- Disabling premium automation does not invalidate the tenancy.

### Scenario 13 — Subscription Cancellation During Agreement Signing

A landlord schedules cancellation while an agreement is partially signed.

Expected:

- Existing agreement versions remain intact.
- The signing workflow follows explicitly defined provider and retention policies.
- No unauthorized document mutation occurs.

### Scenario 14 — Quota Reservation Failure

A bulk import reserves 30 unit slots but only 20 units are successfully created.

Expected:

- Usage reflects actual counted units.
- Unused reservations are released or expire safely.
- Repeated imports do not create duplicate inventory.

### Scenario 15 — Subscription and Rental Payment Confusion

A rental tenant pays monthly rent while the landlord's SaaS renewal is overdue.

Expected:

- The transactions remain financially separate.
- Tenant rent cannot settle the SaaS invoice.
- SaaS payment events cannot mark rental invoices paid.

### Scenario 16 — Tenant Admin Support Access

A support operator investigates a subscription billing problem.

Expected:

- Only necessary SaaS billing information is available.
- Rental tenant identity documents and payment details are not exposed without separately authorized access.
- Privileged access is auditable.

Preserve all original LMT concurrency, agreement, payment, authorization, maintenance, and move-out exceptional scenarios in addition to these SaaS scenarios.

## Traceability

### Functional requirement to screen contract index

| Original ID | Requirement | Frontend contracts |
|---|---|---|
| SAAS-REG-001 | SaaS Customer Registration | FS-02 |
| SAAS-REG-002 | Free Workspace Provisioning | FS-02 |
| SAAS-REG-003 | Guided Setup | FS-02 |
| SAAS-PLAN-001 | Public Pricing Page | FS-03 |
| SAAS-PLAN-002 | Plan Details | FS-03 |
| SAAS-PLAN-003 | Plan Versioning | FS-03 |
| SAAS-SUB-001 | Subscription Dashboard | FS-04, FS-05 |
| SAAS-SUB-002 | Upgrade Subscription | FS-04, FS-05 |
| SAAS-SUB-003 | Downgrade Subscription | FS-04, FS-05 |
| SAAS-SUB-004 | Subscription Cancellation | FS-04, FS-05 |
| SAAS-SUB-005 | Failed Subscription Payment | FS-04, FS-05 |
| SAAS-SUB-006 | Subscription Recovery | FS-04, FS-05 |
| SAAS-ENT-001 | Centralized Feature Evaluation | FS-05 |
| SAAS-ENT-002 | Feature Lock UX | FS-05 |
| SAAS-ENT-003 | Effective Entitlement Snapshot | FS-05 |
| SAAS-QUOTA-001 | Unit Limit Enforcement | FS-05, FS-32 |
| SAAS-QUOTA-002 | Unit Counting Rules | FS-05, FS-32 |
| SAAS-QUOTA-003 | Communication Allowances | FS-05, FS-32 |
| SAAS-QUOTA-004 | Usage Dashboard | FS-05, FS-32 |
| SAAS-QUOTA-005 | Quota Exhaustion | FS-05, FS-32 |
| SAAS-QUOTA-006 | Add-On Consumption | FS-05, FS-32 |
| SAAS-BILL-001 | Subscription Checkout | FS-04 |
| SAAS-BILL-002 | Subscription Invoices | FS-04 |
| SAAS-BILL-003 | Payment Reconciliation | FS-04 |
| SAAS-BILL-004 | Subscription Renewal | FS-04 |
| SAAS-BILL-005 | Separation of Funds | FS-04 |
| SAAS-NOTIFY-001 | WhatsApp Configuration | FS-22 |
| SAAS-NOTIFY-002 | Transactional Email | FS-22 |
| SAAS-NOTIFY-003 | Automated Notifications | FS-22 |
| SAAS-NOTIFY-004 | Failures and Recovery | FS-22 |
| SAAS-ADS-001 | Auto-Publish Configuration | FS-10, FS-30 |
| SAAS-ADS-002 | Status-Driven Publishing | FS-10, FS-30 |
| SAAS-ADS-003 | External Advertising | FS-10, FS-30 |
| SAAS-ADS-004 | Offboarding Trigger and Vacancy Advertising Draft | FS-10, FS-30 |
| SAAS-ADS-005 | Channel Preview and Manager/Owner Approval | FS-10, FS-30 |
| SAAS-ADS-006 | Facebook/Instagram Connections and Supported Publishing | FS-10, FS-30 |
| SAAS-ADS-007 | Paid Campaign Budget, Housing Policy, and Launch Controls | FS-10, FS-30 |
| SAAS-ADS-008 | Delivery Reliability, Availability Sync, and Takedown | FS-10, FS-30 |
| SAAS-ADS-009 | Advertising Dashboard, Leads, and Traceability | FS-10, FS-30 |
| SAAS-ADMIN-001 | Customer Management | FS-06 |
| SAAS-ADMIN-002 | Plan Management | FS-06 |
| SAAS-ADMIN-003 | Subscription Operations | FS-06 |
| SAAS-ADMIN-004 | Platform Analytics | FS-06 |
| SAAS-ADMIN-005 | Support Access | FS-06 |
| MARKET-001 | Email Identity, Optional Federation, and Browser Notifications | FS-01, FS-22 |
| MARKET-002 | Complete Rental Attributes and PG/Co-Living Offers | FS-07, FS-09, FS-16 |
| MARKET-003 | Advanced Search, Geo Queries, Ranking, and Index Integrity | FS-10 |
| MARKET-004 | Rental Listing Authenticity, Freshness, and Moderation | FS-10, FS-06 |
| MARKET-005 | Rental Leads, Contact Access, Messaging, and Visit Feedback | FS-11, FS-12 |
| MARKET-006 | Shortlist Sharing, Recently Contacted, and Change Alerts | FS-11, FS-22 |
| MARKET-007 | Conditional Broker Participation and Brokerage Disclosure | FS-31 |
| MARKET-008 | Versioned Rent and Deposit Negotiation | FS-11, FS-13 |
| MARKET-009 | First-Party Listing Promotion and Contact/Search Products | FS-31, FS-04, FS-24 |
| MARKET-010 | Assisted Search and Relationship-Manager Cases | FS-25, FS-31 |
| MARKET-011 | Moving Quotes and Completed-Service Feedback | FS-26 |
| MARKET-012 | Locality Information, Rent Insights, and Explainable Matching | FS-34, FS-10 |
| MARKET-013 | Official Documentation Services and Additional Verification Adapters | FS-15, FS-28, FS-21 |
| MARKET-014 | Rental Payee Onboarding, Fees, and Settlement Visibility | FS-17 |
| MARKET-015 | Marketplace Operations, Content, Analytics, and Conditional Referrals | FS-33 |
| AUTH-001 | Authentication and Account Management | FS-01 |
| PROP-001 | Property and Unit Management | FS-07, FS-09 |
| ADS-001 | Rental Listings and Property Discovery | FS-10 |
| VISIT-001 | Inquiry and Visit Scheduling | FS-11, FS-12 |
| APPLY-001 | Rental Application and Tenant Onboarding | FS-13 |
| LEASE-001 | Rental Agreements and Digital Signing | FS-15 |
| MOVEIN-001 | Move-In and Occupancy | FS-14, FS-09 |
| BILL-001 | Rent, Utilities, and Billing | FS-16 |
| PAY-001 | Digital Payments and Reconciliation | FS-17 |
| DEP-001 | Security Deposits and Final Settlement | FS-18 |
| HELP-001 | Maintenance and Helpdesk | FS-19 |
| BOT-001 | AI Chatbot Assistance | FS-25 |
| NOTICE-001 | Notice, Renewal, and Move-Out | FS-18, FS-14 |
| OWNER-001 | Co-Owner and Property Manager Management | FS-08 |
| DASH-001 | Role-Based Dashboards and Analytics | FS-20 |
| SERVICE-001 | Nearby Services Directory | FS-26 |
| NOTIFY-001 | Notifications and Documents | FS-21, FS-22 |
| OWN-WF-001 | Property Onboarding and Readiness | FS-07, FS-08 |
| OWN-WF-002 | Operating Policies and Delegated Authority | FS-07, FS-08 |
| OWN-WF-003 | Owner Approval Inbox and Exception Decisions | FS-07, FS-08 |
| OWN-WF-004 | Portfolio Finance, Expenses, and Owner Statements | FS-17, FS-20 |
| OWN-WF-005 | Ownership Change, Delegation Exit, and Property Closure | FS-08, FS-14, FS-21 |
| MGR-WF-001 | Assignment Acceptance and Daily Operations | FS-08, FS-14, FS-19 |
| MGR-WF-002 | Vacancy Preparation and Leasing Operations | FS-10, FS-11, FS-12, FS-13, FS-15 |
| MGR-WF-003 | Monthly Billing and Collection Workbench | FS-16, FS-17 |
| MGR-WF-004 | Maintenance Work Orders and Preventive Inspections | FS-08, FS-14, FS-19 |
| MGR-WF-005 | Handover, Move-Out, and Turnover Coordination | FS-14, FS-18, FS-09 |
| TEN-WF-001 | Account Linking and Existing-Tenancy Access | FS-01, FS-02, FS-13 |
| TEN-WF-002 | Search-to-Application Journey | FS-10, FS-11, FS-12, FS-13 |
| TEN-WF-003 | Agreement Review and Move-In Acceptance | FS-15, FS-14, FS-09 |
| TEN-WF-004 | Monthly Dues, Payments, and Charge Disputes | FS-16, FS-17 |
| TEN-WF-005 | Living in the Property and Service Communication | FS-19, FS-14 |
| TEN-WF-006 | Renewal, Notice, Move-Out, and Aftercare | FS-18, FS-14 |
| PM-DASH-001 | Portfolio Dashboard, Cards, and Layout | FS-20, FS-34 |
| PM-DASH-002 | Shared Timeframe and Date Filters | FS-20, FS-34 |
| PM-DASH-003 | Authoritative Portfolio, Tenant, and Rent Metrics | FS-20, FS-34 |
| PM-DASH-004 | Issues, Requests, and Tenant Service Dashboard | FS-20, FS-34 |
| PM-DASH-005 | Recent and Upcoming Tenant Onboarding/Offboarding | FS-20, FS-34 |
| PM-DASH-006 | Collection and Request Drill-Down Action Queues | FS-20, FS-34 |
| PM-DASH-007 | Trends, Property Summary, Export, and Reporting Quality | FS-20, FS-34 |
| PM-DASH-008 | Backend Contracts, Security, and Data Freshness | FS-20, FS-34 |
| PM-DASH-009 | Decision Overview and Interactive Chart Workspace | FS-20, FS-34 |
| PM-DASH-010 | Collections Trend, Collection Efficiency, and Arrears Aging | FS-20, FS-34 |
| PM-DASH-011 | Occupancy, Vacancy Duration, and Capacity Utilization | FS-20, FS-34 |
| PM-DASH-012 | Leasing Funnel and Upcoming Lifecycle Workload | FS-20, FS-34 |
| PM-DASH-013 | Maintenance Demand, Resolution Performance, and Recurring Problems | FS-20, FS-34 |
| PM-DASH-014 | Property Operating Performance and Expense Analysis | FS-20, FS-34 |
| PM-DASH-015 | Forecasts, Scenario Planning, and Decision Alerts | FS-20, FS-34 |
| PM-DASH-016 | Metric Governance, Comparisons, and Decision Auditability | FS-20, FS-34 |
| POL-VER-001 | Jurisdiction Configuration and Request Initiation | FS-28, FS-21 |
| POL-VER-002 | Applicant Information, Documents, and Correction | FS-28, FS-21 |
| POL-VER-003 | Verified Police Accounts, Roles, and Case Access | FS-28, FS-21 |
| POL-VER-004 | Police Intake, Background-Check Workflow, and Dashboard | FS-28, FS-21 |
| POL-VER-005 | Official Outcome, Pass/Fail Mapping, and Decision Integrity | FS-28, FS-21 |
| POL-VER-006 | Applicant Tracking, Notifications, Review, and Tenancy Handoff | FS-28, FS-21 |
| POL-VER-007 | Integration, Security, Retention, and Operational Controls | FS-28, FS-21 |
| REVIEW-001 | Verified Residence Eligibility | FS-29 |
| REVIEW-002 | Review Submission, Stars, Editing, and Withdrawal | FS-29 |
| REVIEW-003 | Public Property Reviews and Rating Summary | FS-29 |
| REVIEW-004 | Fair Moderation, Reporting, and Owner Responses | FS-29 |
| REVIEW-005 | Review Management, Data Contracts, and Dashboard Metrics | FS-29 |
| AMEN-001 | Configurable Amenity and Item Catalog | FS-09 |
| AMEN-002 | Unit Inventory and Explicit Presence/Absence | FS-09 |
| AMEN-003 | Asset Details, Maintenance, and Lifecycle | FS-09 |
| AMEN-004 | Unit Comparison Matrix and Has/Does-Not-Have Filters | FS-09 |
| AMEN-005 | Tenant Handover, Acknowledgment, and Damage Review | FS-09 |
| AMEN-006 | Inventory Dashboard, Listing Accuracy, and Decision Metrics | FS-09 |
| HELP-WF-001 | Tenant Issue Submission and Tracking | FS-19 |
| HELP-WF-002 | Manager and Owner Intake, Visibility, and Assignment | FS-19 |
| HELP-WF-003 | Conversation, Clarification, and Status Lifecycle | FS-19 |
| HELP-WF-004 | Diagnosis, Repairs, Approvals, and Access Consent | FS-19 |
| HELP-WF-005 | Resolution Proposal, Tenant Feedback, and Reopening | FS-19 |
| HELP-WF-006 | Escalations, Notifications, and Helpdesk Dashboard | FS-19 |
| HELP-WF-007 | Contracts, Security, Retention, and Workflow Integration | FS-19 |
| RENT-FIN-001 | Charge Catalog, Responsibility, and Billing Configuration | FS-16, FS-17 |
| RENT-FIN-002 | Electricity and Water Readings, Tariffs, and Shared Bills | FS-16, FS-17 |
| RENT-FIN-003 | Maintenance, Water Service, and Clubhouse Membership Fees | FS-16, FS-17 |
| RENT-FIN-004 | Billing Run, Preview, Approval, and Invoice Issue | FS-16, FS-17 |
| RENT-FIN-005 | Tenant Bills, Payments, Allocation, and Receipts | FS-16, FS-17 |
| RENT-FIN-006 | Corrections, Disputes, Credits, and Invoice States | FS-16, FS-17 |
| RENT-FIN-007 | Move-In, Move-Out, and Membership Cutover | FS-16, FS-18 |
| RENT-FIN-008 | Billing Administration, Reports, APIs, and Controls | FS-16, FS-17, FS-20 |
| LEASE-SIGN-001 | Deadline Trigger and Time Calculation | FS-15, FS-21 |
| LEASE-SIGN-002 | Prepare, Review, and Electronically Sign | FS-15, FS-21 |
| LEASE-SIGN-003 | Reminders, Overdue Escalation, and Exceptions | FS-15, FS-21 |
| LEASE-SIGN-004 | Evidence, Dashboard, Recovery, and Acceptance Tests | FS-15, FS-21 |
| EXIT-001 | Rental Commitment Offer and Advance Disclosure | FS-18 |
| EXIT-002 | Initiate Offboarding and Review Outstanding Obligations | FS-18 |
| EXIT-003 | Lock-In Date Evaluation and Early-Exit Review | FS-18 |
| EXIT-004 | Rent and Electricity Clearance | FS-18 |
| EXIT-005 | Deposit Settlement, Agreement to Terms, and Final Closure | FS-18 |
| EXIT-006 | Offboarding Administration, Audit, and Decision Metrics | FS-18 |
| OCC-WF-001 | Onboard a Property That Is Already Rented | FS-14, FS-32, FS-16 |
| OCC-WF-002 | Rented Portfolio and Tenancy Workspace | FS-14 |
| OCC-WF-003 | Recurring Occupied-Property Operations | FS-16, FS-17 |
| OCC-WF-004 | Rent Revision and Mid-Tenancy Term Changes | FS-11, FS-15, FS-16 |
| OCC-WF-005 | Household, Co-Tenant, and Bed Allocation Changes | FS-14 |
| OCC-WF-006 | Meter, Utility, and Inventory Changes During Occupancy | FS-09, FS-16 |
| OCC-WF-007 | Occupied Repairs, Inspections, and Service Disruption | FS-19, FS-14 |
| OCC-WF-008 | Unit Transfer and Temporary Relocation | FS-14 |
| OCC-WF-009 | Arrears, Disputes, and Agreement Expiry While Occupied | FS-16, FS-17, FS-19 |
| OCC-WF-010 | End-of-Tenancy Closure and Turnover Safeguards | FS-18, FS-14 |
| SERVICE-PREM-001 | Nearby Ads, Categories, and Location Search | FS-26 |
| SERVICE-PREM-002 | Provider Profile, Business Contact, and Publication | FS-26 |
| SERVICE-PREM-003 | Submit a Service Request with Controlled Data Sharing | FS-26 |
| SERVICE-PREM-004 | Provider Response, Scheduling, and Request Tracking | FS-26 |
| SERVICE-PREM-005 | Membership Expiry, Safety, and Administration | FS-26 |
| SERVICE-PREM-006 | Service Dashboard, Data Contracts, and Decision Metrics | FS-26 |
| SALE-001 | Owner Authority and Sale Listing Preparation | FS-27 |
| SALE-002 | Restricted Publication and Buyer Eligibility | FS-27 |
| SALE-003 | Premium Sale Discovery, Inquiries, and Viewings | FS-27 |
| SALE-004 | Private Media, Moderation, and Sale Outcome Boundaries | FS-27 |
| SALE-005 | Seller Dashboard, Data Model, and Audit | FS-27 |
| AI-001 | Preference Profile and Natural-Language Property Search | FS-35 |
| AI-002 | Candidate Selection, Ranking, and Availability Integrity | FS-35 |
| AI-003 | Explainable Recommendations, Comparisons, and Feedback | FS-35 |
| AI-004 | Helpdesk Assistant, Ticket Drafting, and Human Handoff | FS-36, FS-19 |
| AI-005 | Owner/Manager Copilot for Listings and Operations | FS-36, FS-30 |
| AI-006 | Document Extraction and Source-Linked Agreement Summaries | FS-36, FS-21, FS-15 |
| AI-007 | Financial and Maintenance Decision Support | FS-36, FS-20, FS-34 |
| AI-008 | Nearby-Service and Move-Planning Recommendations | FS-35, FS-26, FS-36 |
| AI-009 | Shared AI Gateway, Retrieval, Permission, Cost, and Action Controls | FS-35, FS-36 |
| AI-010 | Recommendation Quality, Feedback, Evaluation, and Administration | FS-35, FS-36 |
| TOUR-001 | Tour Authoring and Publication | FS-23 |
| TOUR-002 | Interactive Viewing and Accessible Fallback | FS-23 |
| TOUR-003 | Media Privacy, Integrations, and Premium Notes | FS-23 |
| APPT-001 | Unified Property Viewing Booking | FS-12 |
| APPT-002 | Rescheduling, Reminders, Calendar, and Virtual Visits | FS-12 |
| APPT-003 | Premium Waitlist and Scheduling Assistance | FS-12 |
| TEN-SUB-001 | Personal Subscription Catalog and Entitlements | FS-24 |
| TEN-SUB-002 | Consumer Checkout and Billing Lifecycle | FS-24 |
| TEN-PREM-001 | Enhanced Search, Alerts, and Comparison | FS-11, FS-25 |
| TEN-PREM-002 | Reusable Application Profile and Private Document Vault | FS-25, FS-21 |
| TEN-PREM-003 | Rental Search Assistant and Personal Insights | FS-25 |
| TEN-PREM-004 | Concierge, Move Organizer, and Member Benefits | FS-25 |
| TEN-ADMIN-001 | Consumer Operations and Entitlement Boundaries | FS-06, FS-24 |

### Original catalogue alias crosswalk

The original FR identifiers below remain source aliases rather than duplicate features. The ranges are inclusive. Their group-level interpretation and conditional disposition are retained; catalogue coverage does not assert implementation or testing.

| Source identifiers | Adopted destination and disposition |
|---|---|
| FR-01.01–FR-01.12 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-02.01–FR-02.19 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-03 (attribute table; no child IDs supplied) | PROP/UNIT/AMEN and MARKET-002/003. Add structured area units, building age/accessibility, negotiability, brokerage disclosure and permitted occupancy conditions. |
| FR-04.01–FR-04.20 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-05.01–FR-05.12 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-06.01–FR-06.08 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-07.01–FR-07.14 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-08.01–FR-08.10 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-09.01–FR-09.12 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-10.01–FR-10.10 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-11.01–FR-11.12 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-12.01–FR-12.16 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-13.01–FR-13.16 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-14.01–FR-14.10 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-15.01–FR-15.20 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-16.01–FR-16.11 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-17.01–FR-17.10 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-18.01–FR-18.12 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-19.01–FR-19.18 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-20.01–FR-20.14 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |

Additional unnumbered source modules map as follows: PG/co-living → MARKET-002; tenant matching/locality comparison → MARKET-012; broker management → MARKET-007; negotiation → MARKET-008; referral program and content management → MARKET-015; inventory, document vault, applications, lifecycle and disputes → their existing LMT modules. Optional source modules remain explicitly Conditional, not silently omitted.

### Expanded original source aliases

The attachment supplies group-level destination mappings. Each original child ID is expanded here without inventing a more precise atomic mapping or priority that the attachment does not provide. FR-03 has no supplied child IDs.

| Source alias | Source-defined adopted destination and disposition |
|---|---|
| FR-01.01 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.02 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.03 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.04 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.05 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.06 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.07 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.08 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.09 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.10 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.11 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-01.12 | AUTH/ROLE, workspace registration, consumer identity and MARKET-001. Email sign-in is added; Google/Apple are conditional; identity verification remains purpose-specific. |
| FR-02.01 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.02 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.03 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.04 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.05 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.06 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.07 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.08 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.09 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.10 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.11 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.12 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.13 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.14 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.15 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.16 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.17 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.18 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-02.19 | PROP/UNIT/ADS, AMEN, TOUR, MARKET-002/004. Listing deletion means safe removal/retention, not deletion of tenancy or financial history. Broker authorship follows MARKET-007. |
| FR-03 | PROP/UNIT/AMEN and MARKET-002/003. Add structured area units, building age/accessibility, negotiability, brokerage disclosure and permitted occupancy conditions. |
| FR-04.01 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.02 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.03 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.04 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.05 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.06 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.07 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.08 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.09 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.10 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.11 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.12 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.13 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.14 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.15 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.16 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.17 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.18 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.19 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-04.20 | ADS and MARKET-003. Advanced filters, counts, ranking, geo search and index consistency; broker filter appears only if participation is enabled. |
| FR-05.01 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.02 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.03 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.04 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.05 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.06 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.07 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.08 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.09 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.10 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.11 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-05.12 | ADS, TOUR, REVIEW, TEN-PREM-001 and MARKET-002/003/006/012. Public sharing must not expose restricted sale content or private workspace data. |
| FR-06.01 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.02 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.03 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.04 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.05 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.06 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.07 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-06.08 | TEN-PREM-001 and MARKET-006. Basic/expanded allowances follow catalog; add cross-device/contact history and consented shortlist sharing. |
| FR-07.01 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.02 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.03 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.04 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.05 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.06 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.07 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.08 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.09 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.10 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.11 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.12 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.13 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-07.14 | VISIT/APPLY and MARKET-005/007/009. Core inquiries are included; gated direct-contact expansion is conditional on catalog, with transactional usage rules. |
| FR-08.01 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.02 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.03 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.04 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.05 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.06 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.07 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.08 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.09 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-08.10 | VISIT/APPT/MGR-WF and MARKET-005: existing scheduling plus private post-visit feedback. |
| FR-09.01 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.02 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.03 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.04 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.05 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.06 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.07 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.08 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.09 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.10 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.11 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-09.12 | MARKET-004/007, PRIV and platform admin. Listing authenticity is separate from review residence verification and POL-VER. |
| FR-10.01 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.02 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.03 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.04 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.05 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.06 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.07 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.08 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.09 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-10.10 | APPLY/OWN-WF/TEN-WF and MARKET-008: versioned offers/counteroffers supplement existing conflict-safe selection. |
| FR-11.01 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.02 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.03 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.04 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.05 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.06 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.07 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.08 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.09 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.10 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.11 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-11.12 | SAAS-PLAN/SUB/BILL/ENT/QUOTA, TEN-SUB, MARKET-005/009/010. Promotion/contact/search services use distinct approved products and cannot double-charge existing entitlements. |
| FR-12.01 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.02 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.03 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.04 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.05 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.06 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.07 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.08 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.09 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.10 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.11 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.12 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.13 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.14 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.15 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-12.16 | LEASE, LEASE-SIGN, EXIT and MARKET-013. Jurisdiction-specific duty calculation, official integrations and physical delivery are conditional; baseline lease signing remains mandatory under its defined exceptions. |
| FR-13.01 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.02 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.03 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.04 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.05 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.06 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.07 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.08 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.09 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.10 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.11 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.12 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.13 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.14 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.15 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-13.16 | RENT-FIN, PAY/DEP and MARKET-014. Payment fees/payee/payout reconciliation are explicit; payout execution requires approved provider arrangements. |
| FR-14.01 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.02 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.03 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.04 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.05 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.06 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.07 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.08 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.09 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-14.10 | POL-VER, applicant documents and MARKET-013. Employment/reference checks are conditional authorized services, not inferred police clearance or an automated rejection score. |
| FR-15.01 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.02 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.03 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.04 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.05 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.06 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.07 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.08 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.09 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.10 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.11 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.12 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.13 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.14 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.15 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.16 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.17 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.18 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.19 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-15.20 | OWN-WF/MGR-WF/TEN-WF/OCC-WF/AMEN/HELP-WF/EXIT/RENT-FIN. Retain existing mandatory lifecycle scope; closure/relisting remains possession/readiness-based. |
| FR-16.01 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.02 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.03 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.04 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.05 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.06 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.07 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.08 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.09 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.10 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-16.11 | SERVICE-PREM and MARKET-011. Add moving quotes and completed-service feedback; explicit scheduling is included, while checkout/automatic booking is conditional. |
| FR-17.01 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.02 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.03 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.04 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.05 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.06 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.07 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.08 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.09 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-17.10 | MARKET-003/012. Map/provider and locality-review inputs conditional; recommendations and rent estimates require truthful sources and coverage. |
| FR-18.01 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.02 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.03 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.04 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.05 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.06 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.07 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.08 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.09 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.10 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.11 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-18.12 | NOTIFY, SAAS-NOTIFY, APPT, LEASE-SIGN, POL-VER and MARKET-001/015. Browser push is optional opt-in; promotional preferences remain independent of necessary operational messages. |
| FR-19.01 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.02 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.03 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.04 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.05 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.06 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.07 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.08 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.09 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.10 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.11 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.12 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.13 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.14 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.15 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.16 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.17 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-19.18 | SAAS-ADMIN, existing domain moderation, MARKET-004/007/009/015. Platform operations do not grant unrestricted tenant data access. |
| FR-20.01 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.02 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.03 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.04 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.05 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.06 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.07 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.08 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.09 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.10 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.11 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.12 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.13 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |
| FR-20.14 | PM-DASH, SAAS-ADMIN, SAAS-ADS-009 and MARKET-015. Distinguish platform marketplace funnel/revenue from workspace rental financial metrics. |

## Phase coverage and unresolved decisions

### Frontend phase coverage

| Source phase | Frontend scope |
|---|---|
| 0 | Resolve source decisions, authority/process gates and screen/contract coverage. |
| 1 | Secure identity, context selection, workspace registration and Free setup. |
| 2 | Portfolio, ownership/delegation, readiness and inventory. |
| 3 | Listings/search, tours, appointments, applications and onboarding. |
| 4 | Agreements, baseline signatures/deadlines, move-in and occupancy. |
| 5 | Rental bills, utilities, payments, deposits and reports. |
| 6 | Essential tenant operations, helpdesk, reviews, police workflow, ongoing occupancy and exit/settlement. |
| 7 | Paid workspace plans, billing, usage and platform operations. |
| 7A | Personal membership, Premium tools, restricted sale and nearby-service experience. |
| 8 | Silver communications, automation and approved social advertising. |
| 9–10 | Entitled advanced workflows, reporting, portfolio analysis and AI. |
| 11 | Approved Diamond enterprise configuration and optional capabilities. |
| 12 | End-to-end, permission, accessibility, privacy and recovery verification. |

Requirement-specific phases and conditions take precedence over this navigation summary. No backend architecture or deployment deliverables are assigned by this FRS.

### Decisions that remain unresolved

Clarify or configure:

- Separate workspace and consumer monthly/annual prices, consumer trials/refunds/grace rules, paid-through behavior, and consumer tax/invoice configuration.
- Premium consumer allowances, alert delivery funding, concierge staffing/service hours, supported geographies, and actual available partner offers.
- Tour capture/storage/processing limits, publication consent, panorama versus true 3D scope, provider contracts and cache removal policy.
- Appointment duration/buffers, instant confirmation eligibility, cancellation/rescheduling and waitlist ordering rules.
- Personal document sharing/retention, membership-expiry access, and personal data deletion policies.
- Property readiness prerequisites, co-owner decision/signing rules, manager spending limits, delegation coverage and authority handover.
- Expense allocation, management fee and owner statement rules; any separately approved payout execution scope.
- Inspection notice/entry policies, dispute/acknowledgment deadlines, announcement audiences, occupant-change approval and post-move-out record access.
- Existing-tenancy import/cutover and opening-balance confirmation, effective-dated rent changes, meter estimates and utility adjustments.
- Transfer/temporary-relocation overlap and proration, deposit movement authority, occupied-agreement expiry handling and delayed-exit commitments.
- Dashboard timezone/preset semantics, historical reporting coverage, joint-obligation tenant counting, overdue/grace definitions, metric freshness targets, planning horizons and permitted export fields.
- Reporting cash/accrual basis, expense classifications, approved budgets, leasing cohorts, service target calendars, comparable-property/sample rules, forecast assumptions and alert thresholds.
- Amenity catalog, serialized versus quantity tracking, shared/tenant-owned item treatment, inventory assessment cadence, required quantities and move-in/exit condition-review policies.
- Residence evidence and imported-tenancy review verification, public reviewer identity options, conflict-of-interest handling, review submission/edit limits, moderation staffing/appeals, invitations and retention.
- Police jurisdiction/process, participating authority agreement, officer identity/onboarding, permitted checks/data sources, document/processing basis, decision vocabulary, supervisory approval, result disclosure, official fees, review channels, retention and live-integration availability.
- Social advertising destinations/accounts, current API permissions and supported formats, manager/owner approval rules, organic versus paid mode, approved budgets, applicable housing-ad policy, live-post cleanup and lead/analytics attribution rules.
- Rental lock-in start/end and month-end rules, 10%/20% offer eligibility, notice/proration, verified early-exit compensation/retention basis and caps, exceptions, prohibition of double recovery, utility holdback review and settlement acceptance/dispute procedures.
- Ten-day signature deadline/date-only conventions, earlier execution prerequisites, required signers, eligible e-signature provider, baseline funding, reminders, evidence retrieval, historical imports and approved alternative execution.
- Sale-listing ownership/co-owner authority review, qualifying current/former tenant evidence, paid-only buyer entitlement, occupied-property disclosures/viewing consent, private media access and historical inquiry access after membership expiry.
- Nearby-service provider contact/publication verification, service areas, Premium ad visibility versus public directory, request delivery channels, quote/scheduling terms, location/contact sharing and post-expiry request continuity.
- Charge liability/payees, billing cycles, tariff/slab and shared-utility allocation rules, verified tax treatment, invoice numbering, proration, dispute/payment allocation, facility membership enrollment/cancellation and direct-provider payment tracking.
- Helpdesk routing/backup ownership, sensitive-complaint access, triage/response targets, service hours, message audiences, entry consent, resolution review/reopening windows and former-tenant case retention.
- Marketplace email/federation identity rules, broker participation/authority, public contact versus optional unlock quota, listing freshness/moderation, PG offer units, negotiation expiry and first-party promotion terms.
- Locality-data licensing/sample thresholds, recommendation consent, assisted-search staffing, official-document/reference-check providers, beneficiary settlement model, referral eligibility/funding and editorial approval.
- Free plan commercial policy.
- Unit count definition for shared accommodation.
- Unit count treatment for draft and archived inventory.
- Email, WhatsApp, SMS, AI, and storage allowances.
- Messaging provider eligibility and charges.
- Whether add-on purchases are supported.
- Downgrade effective-date rules.
- Grandfathering rules for older plans.
- Grace periods and recovery policies.
- Overage charging policy.
- Paid subscription tax and invoicing rules.
- Which rental payment methods can be offered on Free.
- Payment gateway commercial arrangements.
- External listing distribution agreements.
- AI provider/model capability routing, data-processing/training/retention policy, deterministic ranking baseline, user preference consent, hard-constraint semantics, quality/cost/latency gates, explanation evidence, scoped tool allowlist and budget owner.
- Enterprise isolation options.
- Cancellation, data retention, and export policies.
- State-specific legal and agreement-signing requirements.

Use reasonable configurable defaults for engineering progress where safe, but never silently invent legal or commercial obligations.

## Frontend acceptance and completeness checklist

For each original requirement ID, track the applicable screen/component, source priority/condition, role/scope, linked FS contract, fields/validation, visible states, entitlement/quota reason, primary success path, failure/recovery path and actual acceptance evidence separately. Screen coverage does not mean backend or live-provider completion.

- Validate role-appropriate navigation and direct-link denial, including switching accounts/workspaces and revoked authority during an open form.
- Exercise success, invalid input, empty data, pending work, offline/server failure, expired sessions, stale versions, partial completion and safe retry wherever applicable.
- Confirm monetary, signing, approval, inventory, availability and official-decision outcomes only from authoritative responses. Check browser closure and return, duplicate clicks and delayed outcomes.
- Verify keyboard/screen-reader operation, field-error focus, accessible chart tables, mobile reflow and low-bandwidth/static tour fallback.
- Check Free lifecycle continuity, personal/workspace entitlement independence, quota exhaustion, paid-through changes and privacy on restricted sale/service screens.
- Verify source-specific acceptance scenarios with real service contracts or explicitly labeled sandbox prerequisites. Do not claim live service readiness from a simulation.

This is a requirements extraction. No application code was implemented, no product acceptance tests were executed, and no unspecified business or legal decision was resolved by assumption.

