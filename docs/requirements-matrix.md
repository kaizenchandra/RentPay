# Requirement implementation matrix

Source: [unchanged full specification](../lmt-full-stack-prompt.md). This is an implementation inventory, not a completion claim. All 195 IDs retain their source business rules. FS references supply planned actors, inputs, outputs, permissions, transitions, notifications, recovery and acceptance criteria; actual routes, migrations and test evidence are recorded separately. Phase assignments below are initial sequencing and include cross-phase dependencies. Must unless the source explicitly qualifies a sub-capability; conditional provider enhancements are not silently dropped.

The public catalogue subset remains Partial for SAAS-PLAN-001–003. AUTH-001 and SAAS-REG-001–003 now have a real persisted sandbox identity/provisioning slice and remain Partial. No functional requirement is fully Implemented. SAAS-ENT-001–003 and SAAS-QUOTA-004/005 have Partial membership/usage subsets; MARKET-001 has a Partial sandbox email identity subset. Other rows retain their recorded statuses. SMS/email delivery is explicitly simulated through a private local inbox; session security, persistence and workspace authorization are real.

## Contract ownership and phase assignments

| Contract | Owner | Phase | Source contract / acceptance | Runtime/store |
|---|---|---|---|---|
| FS-01 | identity | 1 | [Identity, account recovery, and context selection](../lmt-full-stack-prompt.md#fs-01) | Planned Java / PostgreSQL; Next.js UI |
| FS-02 | workspace | 1 | [Workspace registration, membership, and setup](../lmt-full-stack-prompt.md#fs-02) | Planned Java / PostgreSQL; Next.js UI |
| FS-03 | commercial | 1 | [Versioned plans and public commercial catalog](../lmt-full-stack-prompt.md#fs-03) | Planned Java / PostgreSQL; Next.js UI |
| FS-04 | commercial | 7 | [Workspace subscriptions and SaaS billing](../lmt-full-stack-prompt.md#fs-04) | Planned Java / PostgreSQL; Next.js UI |
| FS-05 | entitlements | 1 | [Entitlements, reservations, usage, and limits](../lmt-full-stack-prompt.md#fs-05) | Planned Java / PostgreSQL; Next.js UI |
| FS-06 | platformadmin | 7 | [Platform administration and scoped support](../lmt-full-stack-prompt.md#fs-06) | Planned Java / PostgreSQL; Next.js UI |
| FS-07 | property | 2 | [Property hierarchy, readiness, and rentable capacity](../lmt-full-stack-prompt.md#fs-07) | Planned Java / PostgreSQL; Next.js UI |
| FS-08 | authorization | 2 | [Ownership, delegation, tasks, policies, and approval decisions](../lmt-full-stack-prompt.md#fs-08) | Planned Java / PostgreSQL; Next.js UI |
| FS-09 | inventory | 2 | [Amenity catalog, inventory movements, and handover snapshots](../lmt-full-stack-prompt.md#fs-09) | Planned Java / PostgreSQL; Next.js UI |
| FS-10 | discovery | 3 | [Rental listing preparation, publication, search, and moderation](../lmt-full-stack-prompt.md#fs-10) | Planned Java / PostgreSQL; Next.js UI |
| FS-11 | leads | 3 | [Inquiries, contact grants, messaging, negotiation, and shortlists](../lmt-full-stack-prompt.md#fs-11) | Planned Java / PostgreSQL; Next.js UI |
| FS-12 | appointments | 3 | [Visit scheduling, rescheduling, and waitlists](../lmt-full-stack-prompt.md#fs-12) | Planned Java / PostgreSQL; Next.js UI |
| FS-13 | applications | 3 | [Rental applications and existing-tenant identity onboarding](../lmt-full-stack-prompt.md#fs-13) | Planned Java / PostgreSQL; Next.js UI |
| FS-14 | tenancy | 4 | [Possession, active tenancy, renewals, inspections, and transfers](../lmt-full-stack-prompt.md#fs-14) | Planned Java / PostgreSQL; Next.js UI |
| FS-15 | agreements | 4 | [Agreement versions, signers, ten-day deadlines, and official documents](../lmt-full-stack-prompt.md#fs-15) | Planned Java / PostgreSQL; Next.js UI |
| FS-16 | rentalfinance | 5 | [Charge configuration, utility calculation, membership, and invoice issue](../lmt-full-stack-prompt.md#fs-16) | Planned Java / PostgreSQL; Next.js UI |
| FS-17 | rentalfinance | 5 | [Rental payments, receipts, payees, reconciliation, and expenses](../lmt-full-stack-prompt.md#fs-17) | Planned Java / PostgreSQL; Next.js UI |
| FS-18 | settlement | 6 | [Notice, lock-in, offboarding, deposit settlement, and refund](../lmt-full-stack-prompt.md#fs-18) | Planned Java / PostgreSQL; Next.js UI |
| FS-19 | helpdesk | 6 | [Helpdesk, incidents, maintenance, and announcements](../lmt-full-stack-prompt.md#fs-19) | Planned Java / PostgreSQL; Next.js UI |
| FS-20 | reporting | 2 | [Operational dashboards, charts, reporting, and exports](../lmt-full-stack-prompt.md#fs-20) | Planned Java / PostgreSQL; Next.js UI |
| FS-21 | documents | 6 | [Secure documents, sharing, consent, retention, and privacy requests](../lmt-full-stack-prompt.md#fs-21) | Planned Java / PostgreSQL; Next.js UI |
| FS-22 | communications | 8 | [Notifications, preferences, templates, and delivery recovery](../lmt-full-stack-prompt.md#fs-22) | Planned Java / PostgreSQL; Next.js UI |
| FS-23 | media | 3 | [Immersive tours and tour publication](../lmt-full-stack-prompt.md#fs-23) | Planned Java / PostgreSQL; Next.js UI |
| FS-24 | consumer | 7A | [Personal consumer membership and billing](../lmt-full-stack-prompt.md#fs-24) | Planned Java / PostgreSQL; Next.js UI |
| FS-25 | consumerexperience | 7A | [Personal premium tools, AI, vault, move planner, and concierge](../lmt-full-stack-prompt.md#fs-25) | Planned Java / PostgreSQL; Next.js UI |
| FS-26 | services | 7A | [Nearby providers, premium service ads, requests, quotes, and relocation](../lmt-full-stack-prompt.md#fs-26) | Planned Java / PostgreSQL; Next.js UI |
| FS-27 | sales | 7A | [Restricted property-sale marketplace](../lmt-full-stack-prompt.md#fs-27) | Planned Java / PostgreSQL; Next.js UI |
| FS-28 | police | 6 | [Police verification and authority portal](../lmt-full-stack-prompt.md#fs-28) | Planned Java / PostgreSQL; Next.js UI |
| FS-29 | reviews | 6 | [Verified resident reviews, public ratings, responses, and moderation](../lmt-full-stack-prompt.md#fs-29) | Planned Java / PostgreSQL; Next.js UI |
| FS-30 | advertising | 8 | [Offboarding-to-social ads, approval, spend, and channel reconciliation](../lmt-full-stack-prompt.md#fs-30) | Planned Java / PostgreSQL; Next.js UI |
| FS-31 | marketplace | 3 | [Broker policy, first-party promotion, and assisted-search operations](../lmt-full-stack-prompt.md#fs-31) | Planned Java / PostgreSQL; Next.js UI |
| FS-32 | jobs | 9 | [Imports, durable jobs, and operator recovery](../lmt-full-stack-prompt.md#fs-32) | Planned Java / PostgreSQL; Next.js UI |
| FS-33 | marketplace | 3 | [Marketplace administration, editorial content, optional referrals, and platform metrics](../lmt-full-stack-prompt.md#fs-33) | Planned Java / PostgreSQL; Next.js UI |
| FS-34 | reporting | 9 | [Locality insights, advanced reports, alerts, and forecasts](../lmt-full-stack-prompt.md#fs-34) | Planned Java / PostgreSQL; Next.js UI |
| FS-35 | ai | 7A | [Property and Service Recommendation Contracts](../lmt-full-stack-prompt.md#fs-35) | Planned Java / PostgreSQL; Next.js UI |
| FS-36 | ai | 9 | [Scoped AI Assistance, Evidence, Actions, and Operations](../lmt-full-stack-prompt.md#fs-36) | Planned Java / PostgreSQL; Next.js UI |

## Functional requirements

| ID / source objective | FS | Phase | Owner | Frontend | Backend | Actual evidence |
|---|---|---|---|---|---|---|
| [ADS-001 — Rental Listings and Property Discovery](../lmt-full-stack-prompt.md#ads-001--rental-listings-and-property-discovery) | FS-10 | 3 | discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-001 — Preference Profile and Natural-Language Property Search](../lmt-full-stack-prompt.md#ai-001--preference-profile-and-natural-language-property-search) | FS-35 | 7A | ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-002 — Candidate Selection, Ranking, and Availability Integrity](../lmt-full-stack-prompt.md#ai-002--candidate-selection-ranking-and-availability-integrity) | FS-35 | 7A | ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-003 — Explainable Recommendations, Comparisons, and Feedback](../lmt-full-stack-prompt.md#ai-003--explainable-recommendations-comparisons-and-feedback) | FS-35 | 7A | ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-004 — Helpdesk Assistant, Ticket Drafting, and Human Handoff](../lmt-full-stack-prompt.md#ai-004--helpdesk-assistant-ticket-drafting-and-human-handoff) | FS-19, FS-36 | 6, 9 | ai, helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-005 — Owner/Manager Copilot for Listings and Operations](../lmt-full-stack-prompt.md#ai-005--ownermanager-copilot-for-listings-and-operations) | FS-30, FS-36 | 8, 9 | advertising, ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-006 — Document Extraction and Source-Linked Agreement Summaries](../lmt-full-stack-prompt.md#ai-006--document-extraction-and-source-linked-agreement-summaries) | FS-15, FS-21, FS-36 | 4, 6, 9 | agreements, ai, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-007 — Financial and Maintenance Decision Support](../lmt-full-stack-prompt.md#ai-007--financial-and-maintenance-decision-support) | FS-20, FS-34, FS-36 | 2, 9 | ai, reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-008 — Nearby-Service and Move-Planning Recommendations](../lmt-full-stack-prompt.md#ai-008--nearby-service-and-move-planning-recommendations) | FS-26, FS-35, FS-36 | 7A, 9 | ai, services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-009 — Shared AI Gateway, Retrieval, Permission, Cost, and Action Controls](../lmt-full-stack-prompt.md#ai-009--shared-ai-gateway-retrieval-permission-cost-and-action-controls) | FS-35, FS-36 | 7A, 9 | ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AI-010 — Recommendation Quality, Feedback, Evaluation, and Administration](../lmt-full-stack-prompt.md#ai-010--recommendation-quality-feedback-evaluation-and-administration) | FS-35, FS-36 | 7A, 9 | ai | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-001 — Configurable Amenity and Item Catalog](../lmt-full-stack-prompt.md#amen-001--configurable-amenity-and-item-catalog) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-002 — Unit Inventory and Explicit Presence/Absence](../lmt-full-stack-prompt.md#amen-002--unit-inventory-and-explicit-presenceabsence) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-003 — Asset Details, Maintenance, and Lifecycle](../lmt-full-stack-prompt.md#amen-003--asset-details-maintenance-and-lifecycle) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-004 — Unit Comparison Matrix and Has/Does-Not-Have Filters](../lmt-full-stack-prompt.md#amen-004--unit-comparison-matrix-and-hasdoes-not-have-filters) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-005 — Tenant Handover, Acknowledgment, and Damage Review](../lmt-full-stack-prompt.md#amen-005--tenant-handover-acknowledgment-and-damage-review) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AMEN-006 — Inventory Dashboard, Listing Accuracy, and Decision Metrics](../lmt-full-stack-prompt.md#amen-006--inventory-dashboard-listing-accuracy-and-decision-metrics) | FS-09 | 2 | inventory | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [APPLY-001 — Rental Application and Tenant Onboarding](../lmt-full-stack-prompt.md#apply-001--rental-application-and-tenant-onboarding) | FS-13 | 3 | applications | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [APPT-001 — Unified Property Viewing Booking](../lmt-full-stack-prompt.md#appt-001--unified-property-viewing-booking) | FS-12 | 3 | appointments | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [APPT-002 — Rescheduling, Reminders, Calendar, and Virtual Visits](../lmt-full-stack-prompt.md#appt-002--rescheduling-reminders-calendar-and-virtual-visits) | FS-12 | 3 | appointments | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [APPT-003 — Premium Waitlist and Scheduling Assistance](../lmt-full-stack-prompt.md#appt-003--premium-waitlist-and-scheduling-assistance) | FS-12 | 3 | appointments | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [AUTH-001 — Authentication and Account Management](../lmt-full-stack-prompt.md#auth-001--authentication-and-account-management) | FS-01 | 1 | identity | Partial | Partial | [Foundation delivery](release-status.md); FS-01/02 subset; V2–V4; FoundationIntegrationTest; foundation.spec.ts + account.spec.ts; local SMS simulation only |
| [BILL-001 — Rent, Utilities, and Billing](../lmt-full-stack-prompt.md#bill-001--rent-utilities-and-billing) | FS-16 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [BOT-001 — AI Chatbot Assistance](../lmt-full-stack-prompt.md#bot-001--ai-chatbot-assistance) | FS-25 | 7A | consumerexperience | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [DASH-001 — Role-Based Dashboards and Analytics](../lmt-full-stack-prompt.md#dash-001--role-based-dashboards-and-analytics) | FS-20 | 2 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [DEP-001 — Security Deposits and Final Settlement](../lmt-full-stack-prompt.md#dep-001--security-deposits-and-final-settlement) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-001 — Rental Commitment Offer and Advance Disclosure](../lmt-full-stack-prompt.md#exit-001--rental-commitment-offer-and-advance-disclosure) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-002 — Initiate Offboarding and Review Outstanding Obligations](../lmt-full-stack-prompt.md#exit-002--initiate-offboarding-and-review-outstanding-obligations) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-003 — Lock-In Date Evaluation and Early-Exit Review](../lmt-full-stack-prompt.md#exit-003--lock-in-date-evaluation-and-early-exit-review) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-004 — Rent and Electricity Clearance](../lmt-full-stack-prompt.md#exit-004--rent-and-electricity-clearance) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-005 — Deposit Settlement, Agreement to Terms, and Final Closure](../lmt-full-stack-prompt.md#exit-005--deposit-settlement-agreement-to-terms-and-final-closure) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [EXIT-006 — Offboarding Administration, Audit, and Decision Metrics](../lmt-full-stack-prompt.md#exit-006--offboarding-administration-audit-and-decision-metrics) | FS-18 | 6 | settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-001 — Maintenance and Helpdesk](../lmt-full-stack-prompt.md#help-001--maintenance-and-helpdesk) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-001 — Tenant Issue Submission and Tracking](../lmt-full-stack-prompt.md#help-wf-001--tenant-issue-submission-and-tracking) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-002 — Manager and Owner Intake, Visibility, and Assignment](../lmt-full-stack-prompt.md#help-wf-002--manager-and-owner-intake-visibility-and-assignment) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-003 — Conversation, Clarification, and Status Lifecycle](../lmt-full-stack-prompt.md#help-wf-003--conversation-clarification-and-status-lifecycle) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-004 — Diagnosis, Repairs, Approvals, and Access Consent](../lmt-full-stack-prompt.md#help-wf-004--diagnosis-repairs-approvals-and-access-consent) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-005 — Resolution Proposal, Tenant Feedback, and Reopening](../lmt-full-stack-prompt.md#help-wf-005--resolution-proposal-tenant-feedback-and-reopening) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-006 — Escalations, Notifications, and Helpdesk Dashboard](../lmt-full-stack-prompt.md#help-wf-006--escalations-notifications-and-helpdesk-dashboard) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [HELP-WF-007 — Contracts, Security, Retention, and Workflow Integration](../lmt-full-stack-prompt.md#help-wf-007--contracts-security-retention-and-workflow-integration) | FS-19 | 6 | helpdesk | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [LEASE-001 — Rental Agreements and Digital Signing](../lmt-full-stack-prompt.md#lease-001--rental-agreements-and-digital-signing) | FS-15 | 4 | agreements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [LEASE-SIGN-001 — Deadline Trigger and Time Calculation](../lmt-full-stack-prompt.md#lease-sign-001--deadline-trigger-and-time-calculation) | FS-15, FS-21 | 4, 6 | agreements, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [LEASE-SIGN-002 — Prepare, Review, and Electronically Sign](../lmt-full-stack-prompt.md#lease-sign-002--prepare-review-and-electronically-sign) | FS-15, FS-21 | 4, 6 | agreements, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [LEASE-SIGN-003 — Reminders, Overdue Escalation, and Exceptions](../lmt-full-stack-prompt.md#lease-sign-003--reminders-overdue-escalation-and-exceptions) | FS-15, FS-21 | 4, 6 | agreements, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [LEASE-SIGN-004 — Evidence, Dashboard, Recovery, and Acceptance Tests](../lmt-full-stack-prompt.md#lease-sign-004--evidence-dashboard-recovery-and-acceptance-tests) | FS-15, FS-21 | 4, 6 | agreements, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-001 — Email Identity, Optional Federation, and Browser Notifications](../lmt-full-stack-prompt.md#market-001--email-identity-optional-federation-and-browser-notifications) | FS-01, FS-22 | 1, 8 | communications, identity | Partial | Partial | Sandbox email OTP sign-in, durable independent account and returning-workspace journey; V23, FoundationIntegrationTest and email-identity.spec.ts. Explicit unused-email linking to a mobile account is also supported; real delivery, broader linking/recovery and conditional federation/push remain incomplete. |
| [MARKET-002 — Complete Rental Attributes and PG/Co-Living Offers](../lmt-full-stack-prompt.md#market-002--complete-rental-attributes-and-pgco-living-offers) | FS-07, FS-09, FS-16 | 2, 5 | inventory, property, rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-003 — Advanced Search, Geo Queries, Ranking, and Index Integrity](../lmt-full-stack-prompt.md#market-003--advanced-search-geo-queries-ranking-and-index-integrity) | FS-10 | 3 | discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-004 — Rental Listing Authenticity, Freshness, and Moderation](../lmt-full-stack-prompt.md#market-004--rental-listing-authenticity-freshness-and-moderation) | FS-06, FS-10 | 3, 7 | discovery, platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-005 — Rental Leads, Contact Access, Messaging, and Visit Feedback](../lmt-full-stack-prompt.md#market-005--rental-leads-contact-access-messaging-and-visit-feedback) | FS-11, FS-12 | 3 | appointments, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-006 — Shortlist Sharing, Recently Contacted, and Change Alerts](../lmt-full-stack-prompt.md#market-006--shortlist-sharing-recently-contacted-and-change-alerts) | FS-11, FS-22 | 3, 8 | communications, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-007 — Conditional Broker Participation and Brokerage Disclosure](../lmt-full-stack-prompt.md#market-007--conditional-broker-participation-and-brokerage-disclosure) | FS-31 | 3 | marketplace | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-008 — Versioned Rent and Deposit Negotiation](../lmt-full-stack-prompt.md#market-008--versioned-rent-and-deposit-negotiation) | FS-11, FS-13 | 3 | applications, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-009 — First-Party Listing Promotion and Contact/Search Products](../lmt-full-stack-prompt.md#market-009--first-party-listing-promotion-and-contactsearch-products) | FS-04, FS-24, FS-31 | 3, 7, 7A | commercial, consumer, marketplace | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-010 — Assisted Search and Relationship-Manager Cases](../lmt-full-stack-prompt.md#market-010--assisted-search-and-relationship-manager-cases) | FS-25, FS-31 | 3, 7A | consumerexperience, marketplace | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-011 — Moving Quotes and Completed-Service Feedback](../lmt-full-stack-prompt.md#market-011--moving-quotes-and-completed-service-feedback) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-012 — Locality Information, Rent Insights, and Explainable Matching](../lmt-full-stack-prompt.md#market-012--locality-information-rent-insights-and-explainable-matching) | FS-10, FS-34 | 3, 9 | discovery, reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-013 — Official Documentation Services and Additional Verification Adapters](../lmt-full-stack-prompt.md#market-013--official-documentation-services-and-additional-verification-adapters) | FS-15, FS-21, FS-28 | 4, 6 | agreements, documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-014 — Rental Payee Onboarding, Fees, and Settlement Visibility](../lmt-full-stack-prompt.md#market-014--rental-payee-onboarding-fees-and-settlement-visibility) | FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MARKET-015 — Marketplace Operations, Content, Analytics, and Conditional Referrals](../lmt-full-stack-prompt.md#market-015--marketplace-operations-content-analytics-and-conditional-referrals) | FS-33 | 3 | marketplace | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MGR-WF-001 — Assignment Acceptance and Daily Operations](../lmt-full-stack-prompt.md#mgr-wf-001--assignment-acceptance-and-daily-operations) | FS-08, FS-14, FS-19 | 2, 4, 6 | authorization, helpdesk, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MGR-WF-002 — Vacancy Preparation and Leasing Operations](../lmt-full-stack-prompt.md#mgr-wf-002--vacancy-preparation-and-leasing-operations) | FS-10, FS-11, FS-12, FS-13, FS-15 | 3, 4 | agreements, applications, appointments, discovery, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MGR-WF-003 — Monthly Billing and Collection Workbench](../lmt-full-stack-prompt.md#mgr-wf-003--monthly-billing-and-collection-workbench) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MGR-WF-004 — Maintenance Work Orders and Preventive Inspections](../lmt-full-stack-prompt.md#mgr-wf-004--maintenance-work-orders-and-preventive-inspections) | FS-08, FS-14, FS-19 | 2, 4, 6 | authorization, helpdesk, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MGR-WF-005 — Handover, Move-Out, and Turnover Coordination](../lmt-full-stack-prompt.md#mgr-wf-005--handover-move-out-and-turnover-coordination) | FS-09, FS-14, FS-18 | 2, 4, 6 | inventory, settlement, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [MOVEIN-001 — Move-In and Occupancy](../lmt-full-stack-prompt.md#movein-001--move-in-and-occupancy) | FS-09, FS-14 | 2, 4 | inventory, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [NOTICE-001 — Notice, Renewal, and Move-Out](../lmt-full-stack-prompt.md#notice-001--notice-renewal-and-move-out) | FS-14, FS-18 | 4, 6 | settlement, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [NOTIFY-001 — Notifications and Documents](../lmt-full-stack-prompt.md#notify-001--notifications-and-documents) | FS-21, FS-22 | 6, 8 | communications, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-001 — Onboard a Property That Is Already Rented](../lmt-full-stack-prompt.md#occ-wf-001--onboard-a-property-that-is-already-rented) | FS-14, FS-16, FS-32 | 4, 5, 9 | jobs, rentalfinance, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-002 — Rented Portfolio and Tenancy Workspace](../lmt-full-stack-prompt.md#occ-wf-002--rented-portfolio-and-tenancy-workspace) | FS-14 | 4 | tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-003 — Recurring Occupied-Property Operations](../lmt-full-stack-prompt.md#occ-wf-003--recurring-occupied-property-operations) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-004 — Rent Revision and Mid-Tenancy Term Changes](../lmt-full-stack-prompt.md#occ-wf-004--rent-revision-and-mid-tenancy-term-changes) | FS-11, FS-15, FS-16 | 3, 4, 5 | agreements, leads, rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-005 — Household, Co-Tenant, and Bed Allocation Changes](../lmt-full-stack-prompt.md#occ-wf-005--household-co-tenant-and-bed-allocation-changes) | FS-14 | 4 | tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-006 — Meter, Utility, and Inventory Changes During Occupancy](../lmt-full-stack-prompt.md#occ-wf-006--meter-utility-and-inventory-changes-during-occupancy) | FS-09, FS-16 | 2, 5 | inventory, rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-007 — Occupied Repairs, Inspections, and Service Disruption](../lmt-full-stack-prompt.md#occ-wf-007--occupied-repairs-inspections-and-service-disruption) | FS-14, FS-19 | 4, 6 | helpdesk, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-008 — Unit Transfer and Temporary Relocation](../lmt-full-stack-prompt.md#occ-wf-008--unit-transfer-and-temporary-relocation) | FS-14 | 4 | tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-009 — Arrears, Disputes, and Agreement Expiry While Occupied](../lmt-full-stack-prompt.md#occ-wf-009--arrears-disputes-and-agreement-expiry-while-occupied) | FS-16, FS-17, FS-19 | 5, 6 | helpdesk, rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OCC-WF-010 — End-of-Tenancy Closure and Turnover Safeguards](../lmt-full-stack-prompt.md#occ-wf-010--end-of-tenancy-closure-and-turnover-safeguards) | FS-14, FS-18 | 4, 6 | settlement, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWN-WF-001 — Property Onboarding and Readiness](../lmt-full-stack-prompt.md#own-wf-001--property-onboarding-and-readiness) | FS-07, FS-08 | 2 | authorization, property | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWN-WF-002 — Operating Policies and Delegated Authority](../lmt-full-stack-prompt.md#own-wf-002--operating-policies-and-delegated-authority) | FS-07, FS-08 | 2 | authorization, property | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWN-WF-003 — Owner Approval Inbox and Exception Decisions](../lmt-full-stack-prompt.md#own-wf-003--owner-approval-inbox-and-exception-decisions) | FS-07, FS-08 | 2 | authorization, property | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWN-WF-004 — Portfolio Finance, Expenses, and Owner Statements](../lmt-full-stack-prompt.md#own-wf-004--portfolio-finance-expenses-and-owner-statements) | FS-17, FS-20 | 2, 5 | rentalfinance, reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWN-WF-005 — Ownership Change, Delegation Exit, and Property Closure](../lmt-full-stack-prompt.md#own-wf-005--ownership-change-delegation-exit-and-property-closure) | FS-08, FS-14, FS-21 | 2, 4, 6 | authorization, documents, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [OWNER-001 — Co-Owner and Property Manager Management](../lmt-full-stack-prompt.md#owner-001--co-owner-and-property-manager-management) | FS-08 | 2 | authorization | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PAY-001 — Digital Payments and Reconciliation](../lmt-full-stack-prompt.md#pay-001--digital-payments-and-reconciliation) | FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-001 — Portfolio Dashboard, Cards, and Layout](../lmt-full-stack-prompt.md#pm-dash-001--portfolio-dashboard-cards-and-layout) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-002 — Shared Timeframe and Date Filters](../lmt-full-stack-prompt.md#pm-dash-002--shared-timeframe-and-date-filters) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-003 — Authoritative Portfolio, Tenant, and Rent Metrics](../lmt-full-stack-prompt.md#pm-dash-003--authoritative-portfolio-tenant-and-rent-metrics) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-004 — Issues, Requests, and Tenant Service Dashboard](../lmt-full-stack-prompt.md#pm-dash-004--issues-requests-and-tenant-service-dashboard) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-005 — Recent and Upcoming Tenant Onboarding/Offboarding](../lmt-full-stack-prompt.md#pm-dash-005--recent-and-upcoming-tenant-onboardingoffboarding) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-006 — Collection and Request Drill-Down Action Queues](../lmt-full-stack-prompt.md#pm-dash-006--collection-and-request-drill-down-action-queues) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-007 — Trends, Property Summary, Export, and Reporting Quality](../lmt-full-stack-prompt.md#pm-dash-007--trends-property-summary-export-and-reporting-quality) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-008 — Backend Contracts, Security, and Data Freshness](../lmt-full-stack-prompt.md#pm-dash-008--backend-contracts-security-and-data-freshness) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-009 — Decision Overview and Interactive Chart Workspace](../lmt-full-stack-prompt.md#pm-dash-009--decision-overview-and-interactive-chart-workspace) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-010 — Collections Trend, Collection Efficiency, and Arrears Aging](../lmt-full-stack-prompt.md#pm-dash-010--collections-trend-collection-efficiency-and-arrears-aging) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-011 — Occupancy, Vacancy Duration, and Capacity Utilization](../lmt-full-stack-prompt.md#pm-dash-011--occupancy-vacancy-duration-and-capacity-utilization) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-012 — Leasing Funnel and Upcoming Lifecycle Workload](../lmt-full-stack-prompt.md#pm-dash-012--leasing-funnel-and-upcoming-lifecycle-workload) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-013 — Maintenance Demand, Resolution Performance, and Recurring Problems](../lmt-full-stack-prompt.md#pm-dash-013--maintenance-demand-resolution-performance-and-recurring-problems) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-014 — Property Operating Performance and Expense Analysis](../lmt-full-stack-prompt.md#pm-dash-014--property-operating-performance-and-expense-analysis) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-015 — Forecasts, Scenario Planning, and Decision Alerts](../lmt-full-stack-prompt.md#pm-dash-015--forecasts-scenario-planning-and-decision-alerts) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PM-DASH-016 — Metric Governance, Comparisons, and Decision Auditability](../lmt-full-stack-prompt.md#pm-dash-016--metric-governance-comparisons-and-decision-auditability) | FS-20, FS-34 | 2, 9 | reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-001 — Jurisdiction Configuration and Request Initiation](../lmt-full-stack-prompt.md#pol-ver-001--jurisdiction-configuration-and-request-initiation) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-002 — Applicant Information, Documents, and Correction](../lmt-full-stack-prompt.md#pol-ver-002--applicant-information-documents-and-correction) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-003 — Verified Police Accounts, Roles, and Case Access](../lmt-full-stack-prompt.md#pol-ver-003--verified-police-accounts-roles-and-case-access) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-004 — Police Intake, Background-Check Workflow, and Dashboard](../lmt-full-stack-prompt.md#pol-ver-004--police-intake-background-check-workflow-and-dashboard) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-005 — Official Outcome, Pass/Fail Mapping, and Decision Integrity](../lmt-full-stack-prompt.md#pol-ver-005--official-outcome-passfail-mapping-and-decision-integrity) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-006 — Applicant Tracking, Notifications, Review, and Tenancy Handoff](../lmt-full-stack-prompt.md#pol-ver-006--applicant-tracking-notifications-review-and-tenancy-handoff) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [POL-VER-007 — Integration, Security, Retention, and Operational Controls](../lmt-full-stack-prompt.md#pol-ver-007--integration-security-retention-and-operational-controls) | FS-21, FS-28 | 6 | documents, police | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [PROP-001 — Property and Unit Management](../lmt-full-stack-prompt.md#prop-001--property-and-unit-management) | FS-07, FS-09 | 2 | inventory, property | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-001 — Charge Catalog, Responsibility, and Billing Configuration](../lmt-full-stack-prompt.md#rent-fin-001--charge-catalog-responsibility-and-billing-configuration) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-002 — Electricity and Water Readings, Tariffs, and Shared Bills](../lmt-full-stack-prompt.md#rent-fin-002--electricity-and-water-readings-tariffs-and-shared-bills) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-003 — Maintenance, Water Service, and Clubhouse Membership Fees](../lmt-full-stack-prompt.md#rent-fin-003--maintenance-water-service-and-clubhouse-membership-fees) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-004 — Billing Run, Preview, Approval, and Invoice Issue](../lmt-full-stack-prompt.md#rent-fin-004--billing-run-preview-approval-and-invoice-issue) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-005 — Tenant Bills, Payments, Allocation, and Receipts](../lmt-full-stack-prompt.md#rent-fin-005--tenant-bills-payments-allocation-and-receipts) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-006 — Corrections, Disputes, Credits, and Invoice States](../lmt-full-stack-prompt.md#rent-fin-006--corrections-disputes-credits-and-invoice-states) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-007 — Move-In, Move-Out, and Membership Cutover](../lmt-full-stack-prompt.md#rent-fin-007--move-in-move-out-and-membership-cutover) | FS-16, FS-18 | 5, 6 | rentalfinance, settlement | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [RENT-FIN-008 — Billing Administration, Reports, APIs, and Controls](../lmt-full-stack-prompt.md#rent-fin-008--billing-administration-reports-apis-and-controls) | FS-16, FS-17, FS-20 | 2, 5 | rentalfinance, reporting | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [REVIEW-001 — Verified Residence Eligibility](../lmt-full-stack-prompt.md#review-001--verified-residence-eligibility) | FS-29 | 6 | reviews | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [REVIEW-002 — Review Submission, Stars, Editing, and Withdrawal](../lmt-full-stack-prompt.md#review-002--review-submission-stars-editing-and-withdrawal) | FS-29 | 6 | reviews | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [REVIEW-003 — Public Property Reviews and Rating Summary](../lmt-full-stack-prompt.md#review-003--public-property-reviews-and-rating-summary) | FS-29 | 6 | reviews | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [REVIEW-004 — Fair Moderation, Reporting, and Owner Responses](../lmt-full-stack-prompt.md#review-004--fair-moderation-reporting-and-owner-responses) | FS-29 | 6 | reviews | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [REVIEW-005 — Review Management, Data Contracts, and Dashboard Metrics](../lmt-full-stack-prompt.md#review-005--review-management-data-contracts-and-dashboard-metrics) | FS-29 | 6 | reviews | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADMIN-001 — Customer Management](../lmt-full-stack-prompt.md#saas-admin-001--customer-management) | FS-06 | 7 | platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADMIN-002 — Plan Management](../lmt-full-stack-prompt.md#saas-admin-002--plan-management) | FS-06 | 1 foundation / 7 full | platformadmin | Partial | Partial | Read-only review: /admin/plans, /admin/consumer-plans; V9; PlanReview; FoundationIntegrationTest; plan-review.spec.ts. Editorial draft creation: V10, PlanDrafts, plan-review.spec.ts; Reviewed editorial publication: V11, PlanPublication, PublicationIntegrationTest. Pricing/feature editing and full administration Pending. |
| [SAAS-ADMIN-003 — Subscription Operations](../lmt-full-stack-prompt.md#saas-admin-003--subscription-operations) | FS-06 | 7 | platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADMIN-004 — Platform Analytics](../lmt-full-stack-prompt.md#saas-admin-004--platform-analytics) | FS-06 | 7 | platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADMIN-005 — Support Access](../lmt-full-stack-prompt.md#saas-admin-005--support-access) | FS-06 | 7 | platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-001 — Auto-Publish Configuration](../lmt-full-stack-prompt.md#saas-ads-001--auto-publish-configuration) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-002 — Status-Driven Publishing](../lmt-full-stack-prompt.md#saas-ads-002--status-driven-publishing) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-003 — External Advertising](../lmt-full-stack-prompt.md#saas-ads-003--external-advertising) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-004 — Offboarding Trigger and Vacancy Advertising Draft](../lmt-full-stack-prompt.md#saas-ads-004--offboarding-trigger-and-vacancy-advertising-draft) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-005 — Channel Preview and Manager/Owner Approval](../lmt-full-stack-prompt.md#saas-ads-005--channel-preview-and-managerowner-approval) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-006 — Facebook/Instagram Connections and Supported Publishing](../lmt-full-stack-prompt.md#saas-ads-006--facebookinstagram-connections-and-supported-publishing) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-007 — Paid Campaign Budget, Housing Policy, and Launch Controls](../lmt-full-stack-prompt.md#saas-ads-007--paid-campaign-budget-housing-policy-and-launch-controls) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-008 — Delivery Reliability, Availability Sync, and Takedown](../lmt-full-stack-prompt.md#saas-ads-008--delivery-reliability-availability-sync-and-takedown) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ADS-009 — Advertising Dashboard, Leads, and Traceability](../lmt-full-stack-prompt.md#saas-ads-009--advertising-dashboard-leads-and-traceability) | FS-10, FS-30 | 3, 8 | advertising, discovery | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-BILL-001 — Subscription Checkout](../lmt-full-stack-prompt.md#saas-bill-001--subscription-checkout) | FS-04 | 7 | commercial | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-BILL-002 — Subscription Invoices](../lmt-full-stack-prompt.md#saas-bill-002--subscription-invoices) | FS-04 | 7 | commercial | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-BILL-003 — Payment Reconciliation](../lmt-full-stack-prompt.md#saas-bill-003--payment-reconciliation) | FS-04 | 7 | commercial | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-BILL-004 — Subscription Renewal](../lmt-full-stack-prompt.md#saas-bill-004--subscription-renewal) | FS-04 | 7 | commercial | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-BILL-005 — Separation of Funds](../lmt-full-stack-prompt.md#saas-bill-005--separation-of-funds) | FS-04 | 7 | commercial | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-ENT-001 — Centralized Feature Evaluation](../lmt-full-stack-prompt.md#saas-ent-001--centralized-feature-evaluation) | FS-05 | 1 | entitlements | Partial | Partial | Versioned membership eligibility now guards real invitation/acceptance transactions; V6/V7, Entitlements. Full feature matrix, add-ons, timed grants and overrides pending. |
| [SAAS-ENT-002 — Feature Lock UX](../lmt-full-stack-prompt.md#saas-ent-002--feature-lock-ux) | FS-05 | 1 | entitlements | Partial | Partial | Owner usage route explains membership restrictions, exhausted slots, unavailable measurement and unconfigured checkout. Full contextual feature locks and authorized paid upgrades pending. |
| [SAAS-ENT-003 — Effective Entitlement Snapshot](../lmt-full-stack-prompt.md#saas-ent-003--effective-entitlement-snapshot) | FS-05 | 1 | entitlements | Partial | Partial | Owner-only no-store entitlement/usage snapshot with policy and bound plan versions; real API/UI and isolation tests. Full effective grant layering pending. |
| [SAAS-NOTIFY-001 — WhatsApp Configuration](../lmt-full-stack-prompt.md#saas-notify-001--whatsapp-configuration) | FS-22 | 8 | communications | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-NOTIFY-002 — Transactional Email](../lmt-full-stack-prompt.md#saas-notify-002--transactional-email) | FS-22 | 8 | communications | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-NOTIFY-003 — Automated Notifications](../lmt-full-stack-prompt.md#saas-notify-003--automated-notifications) | FS-22 | 8 | communications | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-NOTIFY-004 — Failures and Recovery](../lmt-full-stack-prompt.md#saas-notify-004--failures-and-recovery) | FS-22 | 8 | communications | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-PLAN-001 — Public Pricing Page](../lmt-full-stack-prompt.md#saas-plan-001--public-pricing-page) | FS-03 | 1 | commercial | Partial | Partial | [Catalogue delivery](release-status.md); /pricing; GET public/plans; V1; CatalogIntegrationTest; catalog.spec.ts |
| [SAAS-PLAN-002 — Plan Details](../lmt-full-stack-prompt.md#saas-plan-002--plan-details) | FS-03 | 1 | commercial | Partial | Partial | [Catalogue delivery](release-status.md); /pricing; GET public/plans; V1; CatalogIntegrationTest; catalog.spec.ts |
| [SAAS-PLAN-003 — Plan Versioning](../lmt-full-stack-prompt.md#saas-plan-003--plan-versioning) | FS-03 | 1 | commercial | Partial | Partial | [Catalogue delivery](release-status.md); /pricing; GET public/plans; V1; CatalogIntegrationTest; catalog.spec.ts |
| [SAAS-QUOTA-001 — Unit Limit Enforcement](../lmt-full-stack-prompt.md#saas-quota-001--unit-limit-enforcement) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-QUOTA-002 — Unit Counting Rules](../lmt-full-stack-prompt.md#saas-quota-002--unit-counting-rules) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-QUOTA-003 — Communication Allowances](../lmt-full-stack-prompt.md#saas-quota-003--communication-allowances) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-QUOTA-004 — Usage Dashboard](../lmt-full-stack-prompt.md#saas-quota-004--usage-dashboard) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Partial | Partial | Manager stock and dated event history at canonical usage route; reconciled allocations/counters/memberships; India-date filters and pagination. Other resources and metered billing windows pending. |
| [SAAS-QUOTA-005 — Quota Exhaustion](../lmt-full-stack-prompt.md#saas-quota-005--quota-exhaustion) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Partial | Partial | Manager acceptance/release subset only; V5, ManagerCapacity, PostgreSQL last-slot race and revoke/reuse. Broader quotas and essential-lifecycle policy remain pending. |
| [SAAS-QUOTA-006 — Add-On Consumption](../lmt-full-stack-prompt.md#saas-quota-006--add-on-consumption) | FS-05, FS-32 | 1, 9 | entitlements, jobs | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-REG-001 — SaaS Customer Registration](../lmt-full-stack-prompt.md#saas-reg-001--saas-customer-registration) | FS-02 | 1 | workspace | Partial | Partial | [Foundation delivery](release-status.md); FS-01/02 subset; V2–V4; FoundationIntegrationTest; foundation.spec.ts + account.spec.ts; local SMS simulation only |
| [SAAS-REG-002 — Free Workspace Provisioning](../lmt-full-stack-prompt.md#saas-reg-002--free-workspace-provisioning) | FS-02 | 1 | workspace | Partial | Partial | [Foundation delivery](release-status.md); FS-01/02 subset; V2–V4; FoundationIntegrationTest; foundation.spec.ts + account.spec.ts; local SMS simulation only |
| [SAAS-REG-003 — Guided Setup](../lmt-full-stack-prompt.md#saas-reg-003--guided-setup) | FS-02 | 1 | workspace | Partial | Partial | [Foundation delivery](release-status.md); FS-01/02 subset; V2–V5; FoundationIntegrationTest; foundation.spec.ts + account.spec.ts + membership.spec.ts; local SMS simulation only |
| [SAAS-SUB-001 — Subscription Dashboard](../lmt-full-stack-prompt.md#saas-sub-001--subscription-dashboard) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-SUB-002 — Upgrade Subscription](../lmt-full-stack-prompt.md#saas-sub-002--upgrade-subscription) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-SUB-003 — Downgrade Subscription](../lmt-full-stack-prompt.md#saas-sub-003--downgrade-subscription) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-SUB-004 — Subscription Cancellation](../lmt-full-stack-prompt.md#saas-sub-004--subscription-cancellation) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-SUB-005 — Failed Subscription Payment](../lmt-full-stack-prompt.md#saas-sub-005--failed-subscription-payment) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SAAS-SUB-006 — Subscription Recovery](../lmt-full-stack-prompt.md#saas-sub-006--subscription-recovery) | FS-04, FS-05 | 1, 7 | commercial, entitlements | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SALE-001 — Owner Authority and Sale Listing Preparation](../lmt-full-stack-prompt.md#sale-001--owner-authority-and-sale-listing-preparation) | FS-27 | 7A | sales | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SALE-002 — Restricted Publication and Buyer Eligibility](../lmt-full-stack-prompt.md#sale-002--restricted-publication-and-buyer-eligibility) | FS-27 | 7A | sales | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SALE-003 — Premium Sale Discovery, Inquiries, and Viewings](../lmt-full-stack-prompt.md#sale-003--premium-sale-discovery-inquiries-and-viewings) | FS-27 | 7A | sales | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SALE-004 — Private Media, Moderation, and Sale Outcome Boundaries](../lmt-full-stack-prompt.md#sale-004--private-media-moderation-and-sale-outcome-boundaries) | FS-27 | 7A | sales | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SALE-005 — Seller Dashboard, Data Model, and Audit](../lmt-full-stack-prompt.md#sale-005--seller-dashboard-data-model-and-audit) | FS-27 | 7A | sales | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-001 — Nearby Services Directory](../lmt-full-stack-prompt.md#service-001--nearby-services-directory) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-001 — Nearby Ads, Categories, and Location Search](../lmt-full-stack-prompt.md#service-prem-001--nearby-ads-categories-and-location-search) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-002 — Provider Profile, Business Contact, and Publication](../lmt-full-stack-prompt.md#service-prem-002--provider-profile-business-contact-and-publication) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-003 — Submit a Service Request with Controlled Data Sharing](../lmt-full-stack-prompt.md#service-prem-003--submit-a-service-request-with-controlled-data-sharing) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-004 — Provider Response, Scheduling, and Request Tracking](../lmt-full-stack-prompt.md#service-prem-004--provider-response-scheduling-and-request-tracking) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-005 — Membership Expiry, Safety, and Administration](../lmt-full-stack-prompt.md#service-prem-005--membership-expiry-safety-and-administration) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [SERVICE-PREM-006 — Service Dashboard, Data Contracts, and Decision Metrics](../lmt-full-stack-prompt.md#service-prem-006--service-dashboard-data-contracts-and-decision-metrics) | FS-26 | 7A | services | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-ADMIN-001 — Consumer Operations and Entitlement Boundaries](../lmt-full-stack-prompt.md#ten-admin-001--consumer-operations-and-entitlement-boundaries) | FS-06, FS-24 | 7, 7A | consumer, platformadmin | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-PREM-001 — Enhanced Search, Alerts, and Comparison](../lmt-full-stack-prompt.md#ten-prem-001--enhanced-search-alerts-and-comparison) | FS-11, FS-25 | 3, 7A | consumerexperience, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-PREM-002 — Reusable Application Profile and Private Document Vault](../lmt-full-stack-prompt.md#ten-prem-002--reusable-application-profile-and-private-document-vault) | FS-21, FS-25 | 6, 7A | consumerexperience, documents | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-PREM-003 — Rental Search Assistant and Personal Insights](../lmt-full-stack-prompt.md#ten-prem-003--rental-search-assistant-and-personal-insights) | FS-25 | 7A | consumerexperience | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-PREM-004 — Concierge, Move Organizer, and Member Benefits](../lmt-full-stack-prompt.md#ten-prem-004--concierge-move-organizer-and-member-benefits) | FS-25 | 7A | consumerexperience | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-SUB-001 — Personal Subscription Catalog and Entitlements](../lmt-full-stack-prompt.md#ten-sub-001--personal-subscription-catalog-and-entitlements) | FS-24 | 7A | consumer | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-SUB-002 — Consumer Checkout and Billing Lifecycle](../lmt-full-stack-prompt.md#ten-sub-002--consumer-checkout-and-billing-lifecycle) | FS-24 | 7A | consumer | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-001 — Account Linking and Existing-Tenancy Access](../lmt-full-stack-prompt.md#ten-wf-001--account-linking-and-existing-tenancy-access) | FS-01, FS-02, FS-13 | 1, 3 | applications, identity, workspace | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-002 — Search-to-Application Journey](../lmt-full-stack-prompt.md#ten-wf-002--search-to-application-journey) | FS-10, FS-11, FS-12, FS-13 | 3 | applications, appointments, discovery, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-003 — Agreement Review and Move-In Acceptance](../lmt-full-stack-prompt.md#ten-wf-003--agreement-review-and-move-in-acceptance) | FS-09, FS-14, FS-15 | 2, 4 | agreements, inventory, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-004 — Monthly Dues, Payments, and Charge Disputes](../lmt-full-stack-prompt.md#ten-wf-004--monthly-dues-payments-and-charge-disputes) | FS-16, FS-17 | 5 | rentalfinance | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-005 — Living in the Property and Service Communication](../lmt-full-stack-prompt.md#ten-wf-005--living-in-the-property-and-service-communication) | FS-14, FS-19 | 4, 6 | helpdesk, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TEN-WF-006 — Renewal, Notice, Move-Out, and Aftercare](../lmt-full-stack-prompt.md#ten-wf-006--renewal-notice-move-out-and-aftercare) | FS-14, FS-18 | 4, 6 | settlement, tenancy | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TOUR-001 — Tour Authoring and Publication](../lmt-full-stack-prompt.md#tour-001--tour-authoring-and-publication) | FS-23 | 3 | media | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TOUR-002 — Interactive Viewing and Accessible Fallback](../lmt-full-stack-prompt.md#tour-002--interactive-viewing-and-accessible-fallback) | FS-23 | 3 | media | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [TOUR-003 — Media Privacy, Integrations, and Premium Notes](../lmt-full-stack-prompt.md#tour-003--media-privacy-integrations-and-premium-notes) | FS-23 | 3 | media | Pending | Pending | No code/test evidence; source-linked acceptance pending |
| [VISIT-001 — Inquiry and Visit Scheduling](../lmt-full-stack-prompt.md#visit-001--inquiry-and-visit-scheduling) | FS-11, FS-12 | 3 | appointments, leads | Pending | Pending | No code/test evidence; source-linked acceptance pending |

## Source aliases

Inclusive aliases preserved below. The supplied source contains a family-level crosswalk, not child-level acceptance text. Each alias is linked to its adopted atomic catalogue anchor; detailed child interpretation/priority is unresolved until the original child text is supplied or located. These entries do not invent unseen acceptance rules or create duplicate implementations. FR-03 has an attribute table and no supplied child IDs.

| Alias | Atomic destination anchor | Priority / interpretation / conditionality | Phase | Evidence |
|---|---|---|---|---|
| FR-01.01 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.02 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.03 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.04 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.05 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.06 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.07 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.08 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.09 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.10 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.11 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-01.12 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-02.01 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.02 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.03 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.04 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.05 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.06 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.07 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.08 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.09 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.10 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.11 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.12 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.13 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.14 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.15 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.16 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.17 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.18 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-02.19 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |
| FR-04.01 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.02 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.03 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.04 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.05 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.06 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.07 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.08 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.09 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.10 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.11 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.12 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.13 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.14 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.15 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.16 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.17 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.18 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.19 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-04.20 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.01 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.02 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.03 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.04 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.05 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.06 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.07 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.08 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.09 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.10 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.11 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-05.12 | MARKET-003 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-06.01 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.02 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.03 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.04 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.05 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.06 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.07 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-06.08 | MARKET-006 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 8 | Pending |
| FR-07.01 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.02 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.03 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.04 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.05 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.06 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.07 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.08 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.09 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.10 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.11 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.12 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.13 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-07.14 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.01 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.02 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.03 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.04 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.05 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.06 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.07 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.08 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.09 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-08.10 | MARKET-005 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-09.01 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.02 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.03 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.04 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.05 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.06 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.07 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.08 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.09 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.10 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.11 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-09.12 | MARKET-004 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7 | Pending |
| FR-10.01 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.02 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.03 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.04 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.05 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.06 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.07 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.08 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.09 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-10.10 | MARKET-008 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-11.01 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.02 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.03 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.04 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.05 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.06 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.07 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.08 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.09 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.10 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.11 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-11.12 | MARKET-009 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 7, 7A | Pending |
| FR-12.01 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.02 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.03 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.04 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.05 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.06 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.07 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.08 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.09 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.10 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.11 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.12 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.13 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.14 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.15 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-12.16 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-13.01 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.02 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.03 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.04 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.05 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.06 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.07 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.08 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.09 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.10 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.11 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.12 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.13 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.14 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.15 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-13.16 | MARKET-014 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 5 | Pending |
| FR-14.01 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.02 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.03 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.04 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.05 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.06 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.07 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.08 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.09 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-14.10 | MARKET-013 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4, 6 | Pending |
| FR-15.01 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.02 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.03 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.04 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.05 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.06 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.07 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.08 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.09 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.10 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.11 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.12 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.13 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.14 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.15 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.16 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.17 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.18 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.19 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-15.20 | OCC-WF-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 4 | Pending |
| FR-16.01 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.02 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.03 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.04 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.05 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.06 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.07 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.08 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.09 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.10 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-16.11 | MARKET-011 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 7A | Pending |
| FR-17.01 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.02 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.03 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.04 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.05 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.06 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.07 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.08 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.09 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-17.10 | MARKET-012 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3, 9 | Pending |
| FR-18.01 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.02 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.03 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.04 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.05 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.06 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.07 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.08 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.09 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.10 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.11 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-18.12 | MARKET-001 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 1, 8 | Pending |
| FR-19.01 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.02 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.03 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.04 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.05 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.06 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.07 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.08 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.09 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.10 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.11 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.12 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.13 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.14 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.15 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.16 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.17 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-19.18 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.01 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.02 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.03 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.04 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.05 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.06 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.07 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.08 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.09 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.10 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.11 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.12 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.13 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-20.14 | MARKET-015 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 3 | Pending |
| FR-03 | MARKET-002 | Source family crosswalk retained; child interpretation Pending (DEC-002) | 2, 5 | Pending |

## Architecture and integration gates

| Gate | Status | Evidence |
|---|---|---|
| ARCH-01 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-02 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-03 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-04 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-05 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-06 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-07 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-08 | Partial | V8 leased welcome claims, attempt history, fenced execution, bounded retries and owner recovery; OperationsIntegrationTest and operations.spec.ts. General jobs/provider reconciliation/monitoring remain pending. |
| ARCH-09 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-10 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-11 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-12 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-13 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| ARCH-14 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-01 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-02 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-03 | Partial | Existing provisioning intent plus V8 claim/attempt/notification/audit/recovery transactions verified. Other domain event/transaction contracts remain pending. |
| INT-04 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-05 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-06 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-07 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-08 | Pending | Catalogue subset only; see [architecture baseline](architecture.md) and [delivery](release-status.md). No full gate completion. |
| INT-09 | Partial | Six-service local Compose build/startup, migrations, health and persisted-session browser smoke; see [runbook](../infra/compose/README.md). Production, restore and full-domain gates remain open. |
| INT-10 | Partial | Six-service local Compose build/startup, migrations, health and persisted-session browser smoke; see [runbook](../infra/compose/README.md). Production, restore and full-domain gates remain open. |
| INT-11 | Partial | Six-service local Compose build/startup, migrations, health and persisted-session browser smoke; see [runbook](../infra/compose/README.md). Production, restore and full-domain gates remain open. |
| INT-12 | Partial | Six-service local Compose build/startup, migrations, health and persisted-session browser smoke; see [runbook](../infra/compose/README.md). Production, restore and full-domain gates remain open. |

## Per-requirement delivery record

Before implementing each Pending row, resolve its linked FS narrative and specific source block into concrete route/data/form validation, interaction, complete operation/DTO/errors, table ownership, permissions/entitlements/quota, transition guards, audit/outbox, notification recipients, recovery and negative-path acceptance tests. These details are currently source-defined designs, not implemented bindings. Do not advance status from this matrix alone.

## Foundation slice bindings (current)

| Requirement | Scope, input and interaction | Implemented API / persistence | Authority, state, notifications and recovery | Evidence / limitations |
|---|---|---|---|---|
| AUTH-001 | Account-owned `/login`, `/register`, `/verify-otp`, `/account`; name + Indian mobile + six-digit code; field validation and explicit sandbox disclosure | Challenge request/status/verify; session/CSRF/logout; profile/contexts; user_account, auth_challenge/delivery, Spring Session, domain_audit (V2) | Session-bound challenge, expiry/attempt/rate limits, origin/CSRF, session rotation/revocation; pending → delivered/expired/failed → consumed; private inbox only, safe 503; no workspace role needed | FoundationIntegrationTest + IdentityConfigurationTest + foundation.spec.ts. V4 adds `/account/profile` display-name editing with ETag, `/account/security` active-session list/confirmed revocation and in-app notices; account-scoped idempotency/audit and session tombstones. account.spec.ts verifies save/conflict/retry, keyboard/mobile and separate-browser revocation. Live SMS, verified contact recovery/linking, preferences, export/deletion and external security notifications pending. |
| SAAS-REG-001 | Authenticated sandbox account; `/create-workspace`; normalized name/type/city/state, exact Free version and sandbox-use-v1 acknowledgement | POST workspaces; profile, membership, settings, consent, audit, command result and outbox (V2) | Actor-derived scope; transactional idempotency; anonymous denied; no submitted role/actor authority; retries recover same workspace | Real local registration and cross-scope denial verified. Live verified-mobile registration, approved policies and broader profile fields remain pending. |
| SAAS-REG-002 | Free selection/confirmation with capacity and zero payment; `/setup?workspace=…` | WorkspaceProvisioning coordinates WorkspaceCommands + FreeProvisioning; immutable plan reference, subscription/grants and owner membership atomically | PostgreSQL actor-scoped lock + unique key; plan mismatch rolls back all rows; changed body conflicts; RLS plus owner check; durable welcome event → deduplicated in-app message | Concurrency, rollback, revocation, pool isolation and restart replay verified. Unit creation/quota consumption is not implemented, so full source acceptance remains Partial. |
| SAAS-REG-003 | Setup shows real persisted profile/subscription completion and outstanding property/unit/tenant/charges/preferences steps; context switching is tab-local | GET workspace setup + paginated account contexts; authorized persisted records determine completed steps | Current membership rechecked; no client completed boolean; no-store and noindex, request abort, cross-tab logout, unknown-outcome retry with same key/body | Desktop/mobile reload/context/logout tested. V5 adds owner member/invitation management, contact-bound account acceptance/decline, revocation and explicit manager context. membership.spec.ts and PostgreSQL last-slot/isolation tests verify this subset. Remaining setup steps, owner succession, live delivery and property delegation remain Pending. |

All prices, privacy and provider prerequisites retain the full source's conditions. No rental unit, tenancy, invoice, payment, agreement or police status is fabricated by this slice.

## Workspace membership continuation evidence

FS-02 now includes `/workspace/settings/members`, `/account/invitations` and `/workspace` with explicit workspace query context. Owner forms collect a validated Indian test mobile and confirm membership-only scope/expiry. Recipient actions bind identity contact, target version and current inviter authority; no workspace role is required to receive an invitation. APIs list members/invitations/context, create/get invitations, accept/decline, revoke invitations and revoke managers. V5 persists invitation deadlines/states, member versions, scoped command results and commercial allocation history with audit. Lists and status are durable in-app notifications; external delivery is unavailable. Exact command retry recovers unknown outcomes.

SAAS-REG-001–003 remain Partial; this is additional FS-02 membership evidence, not completion of registration/setup or tenant onboarding. AUTH-001 retains Partial for manager context selection and revocation enforcement. MGR-WF-001 remains Pending for actual property scope, time-bounded delegation, handover and daily operations. At that checkpoint SAAS-ENT-001–003 remained Pending; the following usage continuation advances only their membership subset to Partial.

| Requirement subset | UI/data/forms | API/use case and persistence | Permissions/transitions/recovery | Acceptance evidence / remaining |
|---|---|---|---|---|
| SAAS-QUOTA-005 manager capacity; FS-05 | Owner sees actual manager used/allowance; invitation says pending does not reserve; recipient sees QUOTA_EXCEEDED without fake success | ManagerCapacity reserves/consumes in the acceptance transaction; V5 manager_allocation retains committed/released history; workspace_grant remains commercial-owned | Owner can revoke at capacity/restriction; owner protected; last slot conditional update/constraint; duplicate commands release once; quota failure rolls back provisional member | FoundationIntegrationTest race/revoke/reuse, membership.spec.ts, foundation-recovery.mjs. Frontend/Backend Partial. Sandbox counting only; broader quota, billing restriction and essential lifecycle tests pending. |

ARCH-05/06 and INT-02–05 gain evidence from explicit commercial command ownership, scoped RLS, versioned commands and atomic local transactions. ARCH/INT completion statuses are unchanged. [ADR 0004](architecture/adrs/0004-sandbox-workspace-membership.md) and [release status](release-status.md) record migration, policy and actual checks.

## Membership entitlement and usage evidence

| Subset | UI / data / forms | API / persistence / authority | Transitions / recovery / acceptance |
|---|---|---|---|
| SAAS-ENT-001–003, FS-05 | `/workspace/billing/usage`; capability eligibility/reason, bound plan and policy versions, restriction explanations, informational pricing link with checkout unavailable | GET workspace entitlements/usage; V6 commercial policy/rules, V7 explicit plan-version capability mapping; owner permission remains separate; invitation/acceptance re-evaluate internally | Restricted or suspended additions denied; owner corrective revocation preserved; no-store/refresh. FoundationIntegrationTest verifies domain and cross-scope denial; membership.spec.ts verifies real screen. Frontend/Backend Partial; full grant layering pending. |
| SAAS-QUOTA-004/005, FS-05/32 | Actual current manager stock/remaining and unknown inventory usage; URL date fields; history counts, net delta and evidence references | GET usage/history; V6 immutable-to-runtime manager usage events; same transaction as commercial allocation/counters and membership/audit; owner-only RLS-backed projection | Commit/release once, backfill actual timestamps, null remaining on mismatch, bounded cursor/date validation, restart recovery. PostgreSQL India boundaries and 28-event pagination tested; browser schema/empty/filter/mobile/axe checks. Frontend/Backend Partial; generalized quota and resource metering pending. |

Traceability and full-source gap disposition are unchanged outside these subsets. See [ADR 0005](architecture/adrs/0005-membership-entitlements-and-usage.md) and the latest [release evidence](release-status.md).

## Durable operation foundation evidence

The welcome-operation subset contributes to SAAS-REG-003 (still Partial), FS-02/FS-32 infrastructure, ARCH-08 and INT-03. `/workspace/operations` displays scoped durable state, attempts, lease/retry dates and failure guidance. Typed list/detail/retry-failed/cancel-request APIs authorize current workspace owner; mutations require reason, ETag and idempotency. V8 extends the original outbox and retains notification identity; attempts and recovery reasons/results persist with audits. Execution revalidates and locks original creator authority. Revocation blocks rather than silently pending forever; retry is explicitly accepted, not delivered; cancellation preserves committed effects.

Evidence: OperationsIntegrationTest (real injected DB failures, bounded exhaustion/recovery, exclusive claims, lease fencing, revoked authority, cancellation, head-of-queue starvation); operations.spec.ts (desktop/mobile real APIs, labelled failed-state test fixture, keyboard confirmation, lost-response replay, schemas/axe/overflow); foundation-recovery.mjs (operation and one message survive DB restart). Frontend/backend foundation subset implemented; all parent requirements remain Partial or Pending as applicable. No OCC-WF-001 import, generic operator role, external notification or provider success is claimed. [ADR 0006](architecture/adrs/0006-durable-welcome-operations.md) records limits and runbook.

### Catalogue audience continuation — 2026-11-08

SAAS-PLAN-001 / FS-03 remains Partial on frontend and backend. `/pricing?audience=workspace|consumer&interval=monthly|annual` reads the existing typed public catalogue, preserves filters and presents the unpublished consumer catalogue as empty. Workspace capacity/setup actions are not shown as personal membership benefits. `CatalogIntegrationTest.audienceIsolationAppliesToListAndDetail` verifies same-code audience isolation using PostgreSQL; `catalog.spec.ts` verifies desktop/mobile keyboard navigation, refresh, empty state and axe. No personal membership entitlement, purchase, admin activation or new commercial term is implemented. See the current release checkpoint for commands and remaining scope.

### Commercial review continuation — 2026-11-08

SAAS-ADMIN-002 / FS-06 advances to Partial frontend/backend for read-only version review; FS-03 and SAAS-PLAN-003 remain Partial. A session plus an explicit current commercial-review grant allows `/admin/plans` and `/admin/consumer-plans` to read audience-scoped versions through GET `/admin/plans`. V9 persists operator grants and grant-change audit; successful paginated reads record domain audit. Filters/cursors are validated; versions remain immutable and unpublished terms remain excluded from public endpoints. Expiry/revocation denies the next request; refresh/focus clears stale UI, and no browser storage retains records. Tests cover grant denial/expiry/revocation, runtime write denial, pagination, schema, private headers, keyboard and axe. Create/edit/activate/deactivate, feature/add-on configuration and contract migrations remain Pending. No business prices or production authority approvals are invented.

### Editorial draft continuation — 2026-11-08

SAAS-ADMIN-002 / FS-06 and SAAS-PLAN-003 / FS-03 remain Partial. Authorized reviewers with a separate draft flag can use `/admin/plans` or `/admin/consumer-plans` to copy an existing published version via POST `/admin/plans/{id}/versions`. Forms validate name, explicit-offset future effective instant, disclosure and reason; source ETag and idempotency key bind the preview. Prices/limits/classification are inherited. V10 persists the draft, command/reason and audit atomically; unchanged public queries exclude it. Tests verify authority, revocation, CSRF, preconditions, duplicate/changed commands, cross-editor concurrency, inherited terms, public exclusion and lost-response browser recovery. Publication, existing-draft edits/discard, capability/price/quota changes and subscriber migration remain Pending. No external notification or provider effect is claimed.

### Reviewed editorial publication continuation — 2026-11-08

This supersedes earlier publication-Pending notes only for unchanged editorial terms. SAAS-ADMIN-002 / FS-06 and SAAS-PLAN-003 / FS-03 remain Partial. Private review routes now obtain an actor-bound expiring GET activation-preview and confirm POST activation with CSRF, reason, ETag and idempotency. V11 persists revision, review and command/audit evidence. The catalogue module publishes while the commercial module inherits source capability mappings in one transaction. Public queries select the version at its effective instant; prior subscription references remain unchanged. Tests verify cross-actor/stale/expired review denial, concurrency, audit-failure rollback, replay after expiry, actual effective-date transition, old/new workspace contract behavior and browser response-loss recovery. No paid checkout, price/tax/feature/quota editing, draft discard, deactivation, subscriber migration or production governance is implemented.

### Editorial draft-edit continuation — 2026-11-08

SAAS-ADMIN-002 / FS-06 and SAAS-PLAN-003 / FS-03 remain Partial. This supersedes earlier existing-draft-edit Pending notes only for name/disclosure/future effective instant. The private review UI now calls PATCH `/admin/plan-versions/{id}` with explicit edit authority, CSRF, ETag, reason and idempotency. V12 stores before/after terms and command evidence atomically with the update/audit. The revision trigger invalidates earlier publication reviews. Tests cover authority, stale/concurrent edits, audit rollback, replay, published-record rejection, public exclusion and real browser lost-response recovery. Prices, features, quota changes, discard/deactivation and subscriber migration remain Pending.

### Evidence-preserving discard continuation — 2026-11-08

SAAS-ADMIN-002 / FS-06 and SAAS-PLAN-003 / FS-03 remain Partial. This supersedes earlier draft-discard Pending notes for source-backed unpublished versions only. Private plan review confirms POST `/admin/plan-versions/{id}/discard` with separate authority, CSRF, exact ETag, reason and idempotency. V13 retains immutable discarded terms and command/audit evidence; no source or edit history is deleted. Publication reviews fail after discard; shared locking prevents publication/discard races. Tests cover permission, preconditions, rollback, duplicate recovery, retained history, terminal-state protection and desktop/mobile lost responses. Published deactivation, financial/feature/quota changes, migrations and production governance remain Pending.

## Offering availability continuation — 2026-11-09

SAAS-ADMIN-002 / FS-06 and SAAS-PLAN-001–003 / FS-03 remain Partial frontend/backend. This supersedes published-deactivation Pending notes only for catalogue withdrawal/restoration. `/admin/plans` and `/admin/consumer-plans` expose published offering state, whole-family effect, reason confirmation and exact-command recovery through GET/POST `/admin/plan-versions/{id}/availability`. V14 owns audience/code availability, independent grant and immutable-runtime command evidence; V15 binds publication review to availability revision. Current review/write authority, CSRF, ETag and idempotency are required. Withdrawal hides current/future versions without fallback or rewriting subscriptions; restoration selects the latest effective version. Workspace Free is protected under SAAS-REG-002. Tests cover audience isolation, revoked grants, rollback, competing updates, replay, stale approval, future publication, preserved contracts and desktop/mobile recovery. Notifications/retention, full prices/features/quotas/add-ons, subscriber operations and production governance remain pending; no provider behavior is simulated as live. See ADR 0012 and release evidence.

## Private upload foundation — 2026-11-09

Phase 1 object-storage interface, FS-21 and ARCH-06/10 advance to Partial frontend/backend foundations only. `/account/uploads` stages account-owned sandbox test documents through POST/GET `/documents/upload-intents`, GET `/documents/{id}` and POST `/documents/{id}/finalize`. V16 persists intent/digest/expiry/quarantine; private local objects survive retries and process restarts. Session, CSRF, account filters/RLS, exact intent ETag, size/digest/signature checks and audit protect the transition. Actor/key intent replay and row-locked transfer reconcile uncertain outcomes. There are no scan-success notifications: UI explicitly reports unavailable scanning and blocked access. Desktop/mobile tests and PostgreSQL isolation/rollback/concurrency evidence are recorded in release status.

No functional ID is marked Implemented by this foundation: NOTIFY-001, TEN-PREM-002 and all FS-21-linked tenancy/signature/police/marketplace/personal-vault workflows retain their existing status. Storage primitives do not implement those purposes. S3/encryption, scan jobs/format validation, clean release, authorized expiring retrieval, shares/revocation, orphan cleanup, retention/holds/privacy requests and production recovery remain Pending/Blocked on their recorded prerequisites. INT-03/04/05 foundations are Partial; INT-09–12 production deployment gates remain open.

## Private storage recovery continuation — 2026-11-09

Phase 1 storage, FS-21 and ARCH-06/10 remain Partial frontend/backend. `/account/uploads/{id}/storage` reviews historical integrity observations and confirms exact-byte missing-file restoration through GET `/documents/{id}/storage`, POST `.../check` and POST `.../restore`. V17 owns subject-scoped current observations and append-only command evidence; original V16 quarantine receipts are unchanged. Current identity, RLS/account filters, ETag, CSRF and actor-scoped idempotency protect check/restore. Concurrent checks reject stale revisions; byte mismatch denies restoration; post-write audit failure reconciles on exact retry. UI shows time basis, unavailable scan status, conflict/retry and operator investigation for changed files. Verification: UploadIntegrationTest, StorageModeTest and desktop/mobile storage-recovery browser tests; see release status for actual results. No external notification or scan success is simulated. Functional IDs including NOTIFY-001 and TEN-PREM-002 retain their earlier status: foundations do not establish their full document/vault journeys. Provider/scanner jobs, clean release, access grants, retention/holds/deletion and deployment gates remain tracked and incomplete.

## Durable scan-preparation continuation — 2026-11-09

FS-21, Phase 1 worker/storage foundations, ARCH-08/10 and INT-03 remain Partial frontend/backend. V18 atomically queues new quarantined uploads and backfills prior ones with current-time evidence; V19 enforces command/job subject consistency. GET `/documents/{id}/scan` and POST `.../retry` / `.../cancel` power `/account/uploads/{id}/scan`: bounded polling, actual blocker, latest five attempts/truthful total, recovery reason, exact-version confirmation and lost-response retry. The documents-owned worker claims with leases, rechecks subject/quarantine and stored bytes, records scanner unavailability, fences cancelled/expired claims and stops after five automatic attempts. Retry is 202 accepted, not completed scanning. Audit/history and quarantine survive recovery. Tests cover isolation, subject FK, lease races/takeover, backoff/exhaustion, cancellation, atomic failures and desktop/mobile recovery. No scan success, provider message or malware detection is simulated. NOTIFY-001, TEN-PREM-002 and other linked functional IDs retain their earlier statuses; actual scanner/format validation, clean release, sharing/retention/privacy and deployment gates remain pending or externally blocked. See release evidence and ADR 0015.

## Scoped temporary entitlement continuation — 2026-11-09

SAAS-ENT-001/002/003 / FS-05 and SAAS-ADMIN-002 / FS-06 remain Partial frontend/backend. The existing evaluator now combines subscribed capability inclusion with an active manager-onboarding allow/deny override, then applies subscription/workspace/security restrictions. `/admin/workspaces/{id}/member-entitlement` and its GET/POST API use independent scoped expiring authority, CSRF, exact expiry-aware ETag, reason and idempotency. V20 records grants/audit/current configuration and before/result commands with RLS. The owner usage snapshot displays source/revision/expiry, and the member screen explains/blocks discretionary onboarding while keeping corrections available. Both actual invitation creation and acceptance re-evaluate under the shared membership lock. Expiry/inherit returns plan eligibility; allow does not confer role, quota, paid status or restriction bypass. Tests cover scope, revocation/replay, expiry, concurrent updates, rollback, quota, corrective revocation and desktop/mobile recovery. No external notification/provider success is simulated. All other grant/add-on/capability layers, quota reservation recovery, production governance and paid lifecycle remain pending; see release evidence and ADR 0016.

### Manager quota evidence checkpoint — 2026-11-09

SAAS-QUOTA-004/005 (FS-05/32), frontend **Partial**, backend **Partial**: owner usage expands current active-manager, committed-allocation, event-balance, inconsistent-allocation, unmatched-event and unexplained-reservation counts. Existing owner-only usage/entitlement API and generated OpenAPI 0.16.1 models use a repeatable-read snapshot. `ManagerUsageEvidence` is shared with authoritative capacity consumption; every historical allocation must have its expected commit/release evidence, and sandbox persisted reservations require investigation. On inconsistency, remaining capacity is null and acceptance fails transactionally with CAPACITY_RECONCILIATION_REQUIRED. Refresh is read-only; no automated or owner counter repair is advertised. FoundationIntegrationTest verifies missing paired evidence despite equal totals, stranded reservation denial/rollback and preserved evidence; membership.spec.ts verifies real desktop/mobile evidence, private responses and refresh after isolated fixture restoration. Backend 44 tests and focused browser 6 tests passed. General reservation expiry, authorized operator repair, other resources, approved inventory counting and add-ons remain pending; no new requirement is marked Implemented.

### Reviewed counter recovery checkpoint — 2026-11-09

SAAS-QUOTA-004/005 (FS-05/32), SAAS-ADMIN-002 (FS-06): frontend **Partial**, backend **Partial**. Owner usage links to `/admin/workspaces/[id]/manager-capacity-repair`; GET/POST typed OpenAPI 0.17.0 review/confirm with reason, server-derived before/after counts, strong ETag and scoped idempotency. V21 owns audited separate operator grants, repair revision and durable before/result command evidence. Sandbox owner plus scoped repair authority required on read/write/replay. Locks serialize membership/capacity/grant changes; source membership identity and allocation/event integrity must agree; only counters change. Audit failures roll back. Unknown outcomes retain original command; stale review refreshes; denial protects source evidence. CapacityRepairIntegrationTest, strengthened FoundationIntegrationTest and mode test: 48 backend tests pass; 8 targeted desktop/mobile browser tests pass, then 2 final repair UI tests pass. Other resources, non-owner support, production governance, general reservation expiry and source-evidence repair remain pending. No complete functional ID or phase is declared implemented.

### Owner repair-history checkpoint — 2026-11-09

SAAS-QUOTA-004/005 (FS-05/32), related SAAS-ADMIN-002 audit subset (FS-06): frontend **Partial**, backend **Partial**. Canonical usage page shows retained counter-repair reasons/revisions/times and before/after counts using current owner authorization, URL dates and bounded pagination. GET usage/repairs, OpenAPI 0.18.0, CapacityRepairHistory and V22 SELECT-only owner RLS/indexes project existing durable commands; no new mutation or notifications. Repair-grant expiry does not remove owner historical access; ownership revocation does. Private data excludes operator identities and command internals. Failure states retain retry/refresh paths and abort obsolete requests. CapacityRepairIntegrationTest verifies 27 real repairs, scope/RLS, immutable-to-runtime evidence, India boundaries and cursor bindings; targeted desktop/mobile tests verify real history, grant-revocation continuity, empty dates, schema and axe. 49 backend and 8 browser tests passed. General audit search/export, other resource histories, non-owner support, production governance and reservation/expiry integration remain pending.

### Email identity checkpoint — 2026-11-09

MARKET-001 (FS-01/22) moves to **Partial frontend / Partial backend**; AUTH-001 remains **Partial**. `/login` channel selection → existing OTP verification → durable email identity/session → Free workspace/profile/security. V23 extends identity storage without changing existing mobile identities; OpenAPI 0.19.0 generates the channel and nullable masked-contact contracts. Existing Java challenge/queue/audit/session use cases own validation, expiry, rate limits, single use and unique contact creation. Local domain normalization preserves local-part spelling; no unverified or automatic account merging. Sandbox proof cannot access a live-mode identity. Delivery remains simulated in a private filesystem inbox. Source requirements for real email delivery, explicit linking/contact recovery, optional federation/push and production abuse/retention policy remain incomplete. See release checkpoint for actual backend/browser results and run instructions.

Email checkpoint verification: final Maven verify passed 52 backend tests; full Playwright regression passed 52 desktop/mobile browser tests. Generated-contract/type/build checks and traceability passed. No real email-provider or manual screen-reader verification is claimed.

### Explicit email-link checkpoint — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22): frontend **Partial**, backend **Partial**. Profile → `/account/identity-links` → request purpose-bound proof → exact-account/email/version confirmation → preserved account contexts and future email sign-in. POST `/me/identity-links/challenges` and `/me/identity-links`, OpenAPI 0.20.0, EmailLinks and V24. Recent durable session, CSRF, account/session/purpose/profile binding, unique normalized email, strong ETag and HMAC-fingerprinted idempotency protect mutations and retry. Proof attempts/expiry/conflict are explicit; audit failure rolls back contact, consumption, command and in-app notice. No external email is simulated as delivered. Replacement/merge/reverse-link/lost-contact recovery, live providers and conditional federation/push remain incomplete. Actual tests and run steps are recorded in release status.

Linking verification checkpoint: full backend suite 55 passed; expanded FoundationIntegrationTest 25 passed (overlapping suite, with one additional boundary case). Final fixed-build browser regression passed 54 working journeys; two new noscript assertions were corrected to check actual POST/URL privacy, and the final six-test email/link/fallback run passed. Earlier build/server mismatch failures were resolved by restart; see release status for all outcomes. Type/build/generation and traceability checks passed.

### Reverse mobile-link checkpoint — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22), with the existing FS-02 invitation journey: frontend **Partial**, backend **Partial**. Email-only profile → existing `/account/identity-links` route → mobile-specific proof/confirmation → same account/workspaces and future mobile sign-in. OpenAPI 0.21.0 adds optional channel to the existing linking request (omission preserves email compatibility); shared IdentityLinks/IdentityLinkController and V25 enforce purpose/channel/actor/session/profile binding. Recent sign-in, ETag, attempt/expiry rules, contact locks, uniqueness, HMAC idempotency and atomic audit/in-app notice remain. Matching invitations require an explicit acceptance after linking. Source requirements for replacement/unlinking/compromised-contact recovery, existing-account merging and live providers remain incomplete. See release checkpoint for actual backend/full-browser results.

Mobile-link verification: final clean Maven verify passed 59 backend tests; full fixed-build Playwright regression passed 58 desktop/mobile tests. Generated-contract/type/build and traceability checks passed. V25 upgraded the existing local database successfully. Live providers and manual accessibility/operational gates remain unverified.

### Current-session reauthentication continuation — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22): frontend **Partial**, backend **Partial**. `/account/reauthenticate` restores recent authority using a server-selected existing contact. OpenAPI 0.22.0 POST `/me/reauthentication/challenges` and `/verify`, identity use cases and V26 enforce actor/session/purpose/profile binding, CSRF, strong ETag, attempt/expiry/rate limits and single-use verification. Success rotates session/CSRF, preserves account/context and absolute lifetime, and atomically records registry/proof/audit/in-app notice. Request retries retain their key; uncertain verification offers session check/sign-in, not proof replay. PostgreSQL tests cover isolation, stale/expired/invalid proof, prior cookie/CSRF rejection and audit rollback; reauthentication.spec.ts covers real backend desktop/mobile journeys and lost-response guidance. Delivery remains simulated in the private inbox. Contact replacement/unlinking, compromised-contact recovery and live providers remain incomplete. Actual verification is recorded in release status and ADR 0020.

### Security notice read-state continuation — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22) remain **Partial frontend / Partial backend**. Account-owned `/account/security` lists real security events, per-notice unread/read time, whole-inbox unread counts and refresh/pagination. GET `/me/notifications` aliases existing notices; POST `/me/notifications/{id}/read` requires actor identity, CSRF, strong ETag and actor-scoped idempotency. V27 adds read time/version/index and narrow update privilege. State is unread → read with preserved first-read evidence; no workspace role or plan entitlement is needed. Existing content/time and historical notices remain intact. Audit/command/read state commit together; races produce stale conflicts, unknown responses retry the exact command, and scope changes abort requests. FoundationIntegrationTest covers wrong-recipient denial, missing/stale/CSRF/conflicting commands, replay, concurrency, audit rollback and complete-count pagination. notification-read.spec.ts covers keyboard, lost response, persistence, contract shape, mobile reflow and axe. No new simulation: existing notices are real; external delivery remains unavailable. Broader FS-22 preferences, category/quiet-hour rules, tenant/workspace inboxes and provider/metering workflows remain pending in their assigned phases. See release status for actual results.

### Reviewed session batch revocation continuation — 2026-11-09

AUTH-001 (FS-01) remains **Partial frontend / Partial backend**. Account-owned `/account/security/selected` lists real sessions, accepts 1–25 distinct other-session selections, previews exact labels/IDs/last verification and confirms with recent verification. POST `/me/session-revocation-preview` is read-only; POST `/me/session-revocations` binds selection to the strong preview If-Match and actor-scoped idempotency. Existing account_session/identity_command/Spring Session/audit/notice persistence is reused; no migration. No plan/workspace authority required. Active → revoked applies atomically to every reviewed target; current/unselected/new sign-ins remain valid. Per-session and batch audit plus one in-app notice commit with registry/framework changes. Stale/inaccessible/duplicate/current selections fail; unknown outcomes replay the same frozen command. FoundationIntegrationTest covers ownership, CSRF, preconditions, recent proof, races, rollback, replay and new-session preservation; session-batch.spec.ts covers actual browser sessions, explicit keyboard review, lost response, persistence, one notice, mobile reflow and axe. Broader identity recovery/contact replacement/live providers remain pending; this enhancement does not close AUTH-001 or Phase 1. Actual command results are in release status.

### Linked-contact account recovery continuation — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22) remain **Partial frontend / Partial backend**. Signed-out `/account/recovery` accepts an existing linked mobile/email, requests a dedicated expiring proof, shows masked target/expiry and requires explicit confirmation to end existing sessions. POST `/auth/recovery/challenges` and `/auth/recovery/verify`, generated OpenAPI 0.25.0 and V28 reuse identity challenges/queue/session/audit/notice persistence. Request behavior does not disclose account existence; correct proof cannot create or merge an account. Recovery consumes proof, revokes sessions, advances authentication generation/profile version/cutoff and records notice atomically. Prior proof issuance and stale verified generations cannot restore revoked access. Original account/workspaces persist. Unknown verification offers session check/fresh sign-in, not proof replay; requests retain the same key on uncertainty. Tests cover linked-email recovery, same-workspace access, old sessions/codes, purpose/session binding, unknown-contact non-creation, confirmation, wrong/expired proof, rollback and stale-registration interleaving; browser tests cover keyboard, responsive/axe, same account, one notice and lost response. Delivery is simulated in the private inbox. Lost-all-contact assistance, contact replacement/unlinking, account merge, live delivery/notifications and production policy remain incomplete. See release status for actual results.

### Recovery outcome continuity — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22), frontend/backend **Partial**. `/account/recovery` now reads committed outcome after an uncertain verification response without proof replay. OpenAPI 0.26.0 RecoveryChallenge grants and POST `/auth/recovery/outcome` authorize a coarse status read even without the rotated cookie. Grants are challenge/session-bound, memory-only, constant-time checked and valid fifteen minutes; origin/CSRF and no-store remain. V29 completion evidence commits with recovery and historical backfill uses only actual recovery audit events. Status reads cannot authenticate, expose account identity or repeat effects. not_completed warns that an in-flight operation may still finish. Tests cover expired/wrong/cross grants, no account authority, cookie-loss completion, failed/expired state, durable single audit and completion constraints/rollback. Browser tests clear cookies after a lost response and check the real outcome while account access stays denied. No live identity/provider or broader recovery completion claim is made.

### Email workspace invitation continuation — 2026-11-09

SAAS-REG-003 / MARKET-001 (FS-02 with FS-01/22 identity linkage) remain **Partial frontend / Partial backend**. Owner `/workspace/settings/members` accepts exactly one mobile/email, reviews contact/scope/expiry and creates a durable invitation; matching email account `/account/invitations` explicitly accepts before gaining manager membership. Existing APIs are extended in OpenAPI 0.27.0, WorkspaceMembership and V30, retaining old mobile request fingerprints. Normalized email, recipient RLS/query isolation, strong ETag, owner authority, entitlements, expiry, scoped idempotency and shared last-slot capacity apply. States pending → accepted/declined/revoked/expired remain distinct from property authority; no slot is reserved merely by inviting. Existing audit/command and capacity evidence stay atomic. Unknown creation responses retry the frozen command; expired/stale/revoked records have explicit recovery. FoundationIntegrationTest covers email/API/RLS isolation, normalization/replay, explicit acceptance, shared email/mobile quota race, duplicates/invalid contact, expiry and revoke; email-invitation.spec.ts verifies real backend UI, lost response, one record, email sign-in, acceptance, quota, revocation and keyboard/mobile/axe. No external email/SMS is delivered. MGR-WF-001 property delegation and live provider workflows remain pending. Actual checks are recorded in release status.
