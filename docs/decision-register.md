# Decision register

| ID | Decision / uncertainty | Disposition and prerequisite |
|---|---|---|
| DEC-001 | No executable application existed at initial inspection | Adopt Java 21, Maven, Next.js/npm and PostgreSQL. Existing specifications and IDE files preserved. Public catalogue is the first read-only integration slice; no phase declared complete. |
| DEC-002 | Full source references original FR child requirements but supplies only inclusive ranges and family crosswalks | Preserve all 256 child aliases plus FR-03 and family dispositions. Child-level priorities/acceptance cannot be reconstructed from ranges; locate original catalogue before claiming child-level coverage. |
| DEC-003 | No approved paid prices, tax, quota counting, communication allowances, add-ons or sales destination | Seed explicitly labelled specification baseline; nullable unknown prices and contract-defined capacities. No purchase, upgrade, contact or benefit claims. Review commercial configuration before live activation. |
| DEC-004 | Provider identity, payments, signing, messaging, police and AI approval unavailable | No provider simulation in this slice. Select providers and legal/data-processing policy in applicable phase; no official success or guarantees. |
| DEC-005 | Architecture document puts tours in phase 4 whereas unified source places them in phase 3 | Source unified roadmap wins. Existing design retained; this reconciliation controls future work. |
| DEC-006 | Heading “Reconciled Scope and Precedence” is absent from the supplied full source | Use the actual “Scope Reconciliation and Priority Rules” section plus explicit integration supersession, source catalogue crosswalk, audited FS cross-cutting rules, and supplemental delivery gates; do not infer missing source text. |
| DEC-007 | Runtime permissions | Separate migration and runtime PostgreSQL roles. Runtime catalogue role has SELECT only, no writes or DDL. Catalogue is public and global; workspace RLS is not applicable to this table. Private modules must add scoped isolation and tests before use. |
| DEC-008 | Production and workload decisions | AWS/GCP, region, residency, availability/RPO/RTO, alert owner and measurable load budgets remain unapproved. Local catalogue verification is not production readiness. |


## Foundation continuation

| ID | Decision / uncertainty | Disposition and prerequisite |
|---|---|---|
| DEC-009 | Mobile provider is unavailable | Use only local sandbox HMAC codes in an owner-only filesystem inbox. Never label this real mobile verification. Default runtime rejects live identity; select an approved provider before live rollout. |
| DEC-010 | Session and CSRF storage | Spring Session JDBC and Spring Security session CSRF; opaque cookies, rotation and persisted revocation. See ADR 0002 for limits, loopback guard and failure recovery. |
| DEC-011 | Workspace policy acknowledgements | `sandbox-use-v1` explicitly acknowledges local test use only. This is not an approved legal/privacy policy or production commercial agreement. |
| DEC-012 | Initial Free provisioning and grants | Atomically bind the current versioned Free plan. Owner is separate from manager usage; no unit-counting behavior exists yet. Full quota measurement and essential-lifecycle grants need the appropriate downstream slices. |
| DEC-013 | Changes to baseline display | V3 publishes Free version 2 to describe sandbox setup; V1 evidence is unchanged. Paid prices/features remain unconfigured, and personal membership remains independent/unimplemented. |
| DEC-014 | Identity/audit retention and production abuse controls | Sandbox inbox deletes expired code files; account/challenge/audit retention, distributed rate/abuse protection, contact-change recovery and live policies remain production blockers. |

DEC-004's absence of simulations describes the initial catalogue delivery only. The current slice adds explicitly simulated local SMS delivery. DEC-007's SELECT-only statement applies to the public catalogue: V2 adds narrowly granted writes to identity/workspace/session tables and RLS on private workspace tables.

## Account-control continuation

- DEC-015: Use separate management IDs and hashed session references; tombstones deny subsequent requests after remote revoke or logout. Recent sign-in defaults to five minutes. See ADR 0003.
- DEC-016: V4 requires pre-upgrade cookies to sign in again; account and workspace records are retained. Production registry/command/notification retention and external security notification channels remain pending decisions.

## Workspace membership continuation

- DEC-017: Sandbox manager counting uses active MANAGER accounts per workspace; OWNER and pending invitations are excluded. Acceptance reserves/consumes capacity atomically. Production counting policy remains unapproved; no live membership is enabled by this choice.
- DEC-018: Invitation lifetime defaults to 168 hours (configurable 1–720). Exact deadline is durable and acceptance checks it without relying on cleanup jobs. Production expiry/abuse/retention policy remains open.
- DEC-019: Workspace membership has no implicit property/signing/spending/payout authority. Current owner controls invitations and revocation; owner succession and property delegation remain separate pending workflows. In-app inbox/status is available; external delivery is not configured. See ADR 0004.

## Membership usage continuation

- DEC-020: Separate current manager stock from selected-date event deltas; no monthly reset. Unimplemented inventory measurement returns null, not zero. Counter/ledger/membership discrepancies require reviewed operator reconciliation and cannot silently produce available capacity.
- DEC-021: Commercial policy version 1 covers supported sandbox membership only and binds explicit plan-version grants. Full add-ons, temporary grants, overrides and paid state handling remain pending. Corrective owner revocation remains available independently of commercial availability. See ADR 0005.

## Durable welcome continuation

- DEC-022: Extend the existing workspace outbox for the real welcome handler instead of creating a second queue. Five attempts per automatic cycle, default 30-second lease and bounded retry backoff are development execution defaults, not commercial allowances or SLA promises.
- DEC-023: Recovery remains current-owner scoped and requires original creator authority for replay. No implicit authority handover or arbitrary job payload is accepted. Cancellation cannot recall a committed notification. Production alert ownership, retention and generalized operator roles remain open. See ADR 0006.

## DEC-024 — Commercial review before publication

Use a separate, expiring, operator-issued commercial-review grant for the read-only Phase 1 plan-version screen; workspace roles never imply it. V9 audits grant changes and the API audits successful reads. Local bootstrap is sandbox-only, capped at 24 hours; these are development controls, not an approved production role policy. Production administrator verification/MFA, approver governance, grant notifications/retention and publication approval remain explicit decisions. See ADR 0007.

## DEC-025 — Editorial drafts before commercial activation

Allow explicitly granted authors to copy a published source into an unpublished draft with name, disclosure and future effective-instant proposals. Inherit prices, currency, capacities and baseline flags; no fabricated price/tax/eligibility policy is introduced. The version-backed endpoint's source ID is documented in OpenAPI. Publication governance, financial/configuration editing and draft lifecycle remain pending; see ADR 0008.

## DEC-026 — Source-preserving editorial publication

Publication is a separately granted operation with a five-minute technical review expiry. Permit only source-backed editorial drafts with unchanged financial/capacity/classification terms and a future instant after the latest published source. Append to the timeline; do not migrate subscribers. Copy source capability mappings through the owning commercial module. Deactivation, broader configuration, retention/notifications, production identity assurance and second-approver governance remain open; see ADR 0009.

## DEC-027 — Independent editorial edit authority

Add false-by-default draft-edit authority rather than expanding existing creation/publication grants. Edit only name, disclosure and future effective instant; preserve source creation and financial terms. Every save advances revision and stales publication reviews. Use exact-version conflict rejection, not automatic merges. Draft discard, deactivation, full commercial configuration and production role governance remain pending; see ADR 0010.

## DEC-028 — Retain discarded draft evidence

Discard is a separate terminal state, not deletion or public-offer deactivation. Require explicit expiring discard authority, exact version and reason. Retain source/terms/edit/review history, version number and effective-instant uniqueness; provide no implicit restore or date reuse. New work starts from the latest published source. Broader deactivation, retention and production governance remain pending; see ADR 0011.

## DEC-029 — Offering availability is independent of contracts

Withdraw/restore the entire audience/code family, including future versions, with explicit separate authority and durable evidence. Keep workspace Free selectable under SAAS-REG-002; reject withdrawal in both the use case and database. Bind publication reviews to availability revision so their stated public effect cannot silently change. This supersedes earlier deactivation-pending notes only for catalogue availability; subscription cancellation, prices, features, quotas, add-ons and production governance remain pending. See ADR 0012.

## DEC-030 — Fail-closed upload foundations before document release

Implement account-owned, sandbox-only actual upload staging behind `PrivateObjectStore`; production remains unavailable until private S3, scanner and access policy are configured. A successful transfer means quarantined, never clean. Fifteen-minute intents, 1 MiB files and 20 intents/account/hour are development safety bounds, not approved commercial quotas. Local objects may survive metadata rollback for exact-intent reconciliation. Retention/cleanup, encrypted production storage, scanning, sharing, clean release and document-purpose authorization remain explicit prerequisites. See ADR 0013.

## DEC-031 — Storage observations are separate from transfer and scan evidence

Preserve immutable quarantine receipts while recording separately versioned private integrity observations. Allow subject-confirmed restoration only from a current missing-file review and exact original bytes; never overwrite mismatching evidence or infer malware clearance. Use create-new hard links in the POSIX sandbox adapter to avoid replacing a competing destination. No new provider or production policy is selected. Remote adapters must introduce durable dispatch/reconciliation instead of network I/O inside these local transactions. See ADR 0014.

## DEC-032 — Durable scan preparation stays separate from scan clearance

Automatically queue account-owned scan preparation with quarantine and audit. Persist real storage-prerequisite outcomes and explicit scanner unavailability; introduce no clean/infected simulation. Use fenced local-worker leases, five attempts per cycle, preserved history and reasoned cancellation/recheck. Existing quarantined objects get current-time backfill evidence. Actual scanner selection/configuration, privacy/data handling, clean-release criteria and production monitoring remain open. Advance independent Phase 1 entitlements/quotas while these provider prerequisites remain blocked. See ADR 0015.

## DEC-033 — Narrow, scoped temporary overrides before general commercial policy

Enable local-only allow/deny/inherit for WORKSPACE_MEMBER_ADD with explicit account/workspace operator grants and a maximum 24-hour effect. Preserve corrective revocation, subscribed terms, capacity and restriction precedence. Operator permission expiry/revocation is separate from an already-issued effect's expiry. These bounds are sandbox safety controls, not approved production prices, benefits or override policy. General capabilities/add-ons/allowance layering and production governance remain open. See ADR 0016.

- DEC-034: Under DEC-017, manager reservation and consumption commit together, so any persisted nonzero manager reservation requires investigation rather than being treated as an ordinary pending invitation. Reconciliation also checks each allocation's commit/release event pairing; matching totals alone do not establish integrity. Acceptance fails closed without rewriting evidence. Operator repair requires a separate scoped authority, explicit repair policy, exact-version review and audit; entitlement-override authority does not confer repair permission. This policy applies only to the existing sandbox manager flow, not future durable reservations.

- DEC-035: Sandbox manager counter repair requires a separate scoped expiring repair grant AND active workspace ownership. It derives counts only from mutually consistent membership/allocation/event evidence, preserves allowance and historical records, and records reason/before/result atomically with audit. It does not allow source-evidence reconstruction or non-owner support access. Missing source integrity blocks correction. Production governance and general reservation recovery remain unresolved. See ADR 0017.

- DEC-036: Current workspace owners may read retained manager counter-repair history without an active operator repair grant. Expose only revision, reason, counters, allowance and time; omit operator identity and command internals. Ownership is rechecked on each page; repair authority is unaffected. Reasons are disclosed as owner-visible before confirmation. This is scoped operational transparency, not blanket platform audit access.

- DEC-037: Email sign-in uses the existing local sandbox challenge/delivery/session pipeline. Domain case is normalized; ASCII local-part case/dots/plus suffixes are preserved. Email-only accounts are distinct from mobile accounts, with no implicit linking or invitation transfer. Sandbox verification cannot authenticate live-mode accounts. This proves local workflow behavior, not real mailbox control. Live email provider, internationalized address policy and safe identity-link/merge recovery remain pending. See ADR 0018.

- DEC-038: Permit explicit sandbox linking of an unused email to a recent-authenticated mobile account only. Require purpose/account/session/profile-bound OTP, exact profile version and idempotent confirmation. Serialize email sign-in/link ownership claims; reject existing-account email conflicts without merging. Preserve original account/contexts and mobile contact; emit durable in-app security notice. Contact replacement, reverse linking, unlinking, lost-contact recovery and merging need separate policy/workflows. See ADR 0019.

- DEC-039: Extend the explicit sandbox contact-linking workflow to an unused Indian mobile on an email-only account. Reuse recent-session authority, exact-profile proof binding, contact ownership serialization, idempotent outcome/audit/notice and no-merge policy. Keep historical email request compatibility by defaulting an omitted channel to email. Matching mobile invitations become reviewable, not automatically accepted. Replacement, unlinking and compromised-contact recovery remain separate pending workflows.

- DEC-040: Restore recent authority with a purpose/account/session/profile-bound proof against an existing sandbox contact. Rotate the current session and CSRF token without extending its original absolute lifetime. Verification is single-use, with session-check/sign-in recovery for uncertain outcomes; servlet rotation is not database-transactional and failures remain closed. Replacement/unlinking and compromised-contact recovery are separate pending policy/workflows. See ADR 0020.

- DEC-041: Extend identity-owned security notices with immutable first-read time and recipient-scoped versioned/idempotent commands. Do not infer past read status or emit recursive read notifications. Canonical account inbox API aliases the existing store; broader FS-22 preferences/delivery and role-specific inboxes remain pending. Read status is not legal acceptance. See ADR 0021.

- DEC-042: Offer atomic revocation of an explicitly reviewed selection of 1–25 other sessions. Bind server preview to actor/current browser/target versions, require recent verification, and preserve exact-command retries. This is a bounded operational batch, not a commercial session cap or a claim to block future sign-in. New/unselected/current sessions remain available; broader compromised-account recovery stays pending. See ADR 0022.

- DEC-043: Enable signed-out sandbox recovery through a purpose-bound proof for an already linked contact. Require explicit existing-session revocation confirmation; preserve identity/workspaces and reject unknown-contact registration. Serialize proof/session registration with recovery using account generation and invalidate earlier proof issuance via recovery cutoff. Lost cookie/response recovery is session check or fresh ordinary sign-in. No assisted all-contact-loss, contact replacement, account merge or live provider policy is selected. See ADR 0023.

- DEC-044: Provide a fifteen-minute, challenge-specific memory-only bearer grant for coarse recovery outcome reads after cookie loss. Do not expose identity or authentication authority; use POST/no-store and origin/CSRF protection. Record completion in the recovery transaction and backfill only from committed audit evidence. Never infer completion from consumption or retry recovery from a status read. The limit is a sandbox safety bound; live policy remains pending. See ADR 0024.

- DEC-045: Extend existing manager invitations to one email or mobile recipient using identity's normalization rules. Preserve mobile API/fingerprint compatibility; match current account contacts in queries and RLS, but require explicit acceptance and the existing shared quota transaction. Do not merge accounts or infer property/financial authority. Delivery stays in-app sandbox; live email/SMS remain unavailable. See ADR 0025.

## Local Compose deployment — 2026-11-09

Use a distinct `lmt-stack` project with local sandbox identity and private filesystem storage. No new live provider is selected. Production identity/TLS, encrypted object storage/scanning, managed secrets, tested backup/restore, capacity targets and alert ownership remain unresolved deployment prerequisites. See ADR 0026 and `infra/compose/README.md`.

## Contact removal — 2026-11-09

Accepted sandbox policy: require both contacts and fresh proof of the retained contact; revoke other sessions and outstanding proofs. Removed contact may subsequently establish a separate account through ordinary sign-in, never an implicit merge/transfer. This consequence is disclosed before confirmation. Production provider/retention and assisted all-contact-loss/merge policies remain unresolved. See ADR 0027.

## Account inbox and preferences boundary — 2026-11-09

Ship real account-owned security notice filtering on account/tenant/workspace navigation surfaces. Do not mute essential security notices or claim unconnected channel/category/quiet-hour settings are effective. Optional account delivery events/channels and their consent/essential-message policy must be implemented with preferences. Workspace operational notices remain separately scoped. See ADR 0028.

## Optional workspace setup account copy — 2026-11-09

Opt-in defaults off. Only events created during the current enabled period qualify, and consent is rechecked at execution. No historical backfill, paid-plan gate, external delivery or charge. Shared workspace notices and essential security notices remain unaffected. Quiet hours and other channels/categories retain separate implementation and provider/consent decisions. See ADR 0029.

## Optional quiet hours — 2026-11-09

Use explicit Asia/Kolkata wall-clock times, half-open intervals and disabled-by-default configuration. Equal start/end is invalid. Reuse existing durable work with at most one policy check/event/minute to observe preference changes; avoid cross-module queue writes or a new broker. Production retention/capacity policy for repeated attempt evidence remains open. No quiet-hour suppression of security/shared notices. See ADR 0030.

## Live sign-in delivery implementation — 2026-11-09

Twilio SMS + Resend email are implementation defaults while the requested provider-selection question remains unanswered; no provider purchase or approval is inferred. Live activation requires operator credentials, approved Indian sender/template/route, verified email domain/address, TLS and an explicit hourly request ceiling. Worker-only provider secrets and a separate live database are required. Uncertain SMS submissions are not automatically duplicated; email uses provider idempotency. Carrier receipt callbacks, reconciliation UI, broader live identity/workspace policies, retention and production abuse controls remain pending. See ADR 0031 and `infra/compose/LIVE-IDENTITY.md`.


### Local verification inbox selection — 2026-11-09

User selected Mailpit for sandbox email and an SMS container for mobile codes. Use digest-pinned Mailpit HTTP capture plus a separate dependency-free Node/SQLite SMS simulator. No external carrier/provider is implied. Local host ports are 18025 and 8026; 8025 belongs to another project and remains untouched. Sandbox-only activation fails closed in live identity. Native private-file delivery is preserved. Mailpit supports hour/day retention, so email retention is one hour; SMS retention is six minutes, while all OTPs expire five minutes after request. See `infra/compose/LOCAL-INBOXES.md`.

## Property capacity and activation — 2026-11-10

Superseded for new sandbox workspaces by SAAS-QUOTA-002 (2026-11-10): select the recommended independent-item baseline, policy 2, counting beds without double-counting shared containers. Existing workspaces retain policy 1 and its shared-unavailable behavior. Bindings are immutable; no historical contract is silently changed. Production shared counting remains unapproved. Whole-unit development counting includes all nonarchived drafts; property/building/floor containers consume no rentable allowance. See ADR 0048.

Verified property authority, signing/approval/payee policy and relevant jurisdictional requirements are not inferred from workspace ownership or an address. Property setup can be saved and reviewed, but activation is unavailable pending the necessary workflows. No maps provider or legal verification provider is selected by this change. Manual coordinates require both bounded latitude and longitude. See ADR 0038 and the property scope checkpoint in the requirements matrix.

## Subscription dashboard availability — 2026-11-10

For the existing `active_free` contract, recurring billing dates, paid renewal and next invoice are not applicable, not fabricated zero-value invoices. Add-ons, communication metering and SaaS payment history are unavailable until their owning workflows exist. Published alternatives are comparison baselines, not purchasable upgrades. Current owners are the implemented billing administrators; manager membership does not grant billing access. Separate delegated billing authority remains future work. See ADR 0039; no commercial prices/provider approval inferred.

## Upgrade verification mode — 2026-11-10

No paid prices, tax/proration approval or payment provider configuration was available. An optional mode question was sent; no answer was received during implementation. Proceeded with explicitly labeled local simulation as stated in progress updates, consistent with sandbox adapters in repository instructions. Live purchases remain unavailable. Test prices/taxes are explicit operator inputs, not seeded commercial defaults. The initial supported effective-time policy is immediate after successful sandbox settlement, with no automatic renewal. Paid-period test credit uses unused base time and HALF_UP rounding; production credit/tax policy remains a decision. See ADR 0040.


## Scheduled downgrade continuation — 2026-11-10

The implemented local rule applies paid-to-Free at the existing subscription period end, retains excess usage and blocks new capacity consumption. It does not impose a security suspension, delete records, collect a fee or infer a refund. This is a sandbox policy, not approved live commercial terms. Paid-to-paid targets require renewal pricing/verified settlement and remain unavailable; the Gold 120 → Silver source scenario remains pending. Feature review uses known policy at the later of boundary/current time and rejects subsequently changed terms. See ADR 0041 for worker recovery and capacity integrity. Existing essential lifecycle and production gaps remain tracked under their original IDs.


## Cancellation continuation — 2026-11-10

Local policy `sandbox-paid-cancellation-v1` ends simulated paid access at the stored period boundary and continues on Free. This does not close the workspace or revoke existing members. Shared request status `cancelled` means withdrawn, while `applied` means the paid-to-Free transition committed. Immutable review terms track retention and explicit refund/credit non-applicability; null amounts do not represent a processed zero-value refund. No production retention duration, legal hold, refund eligibility, credit accounting, mandate cancellation or signing-provider behavior is inferred. Those decisions and implementations remain prerequisites for live cancellation. See ADR 0042.


## Failed payment continuation — 2026-11-10

Local upgrade retry policy `sandbox-upgrade-retry-v1` explicitly allows three total manually confirmed attempts and a 30-second cooldown after definitive failure. It is development configuration, not approved live merchant or renewal policy. Upgrade failures do not make an existing subscription delinquent: grace is not applicable, no countdown or access reduction is invented. Legacy retry history is unknown; a failed legacy request needs fresh terms. Approved renewal pricing/collection, grace periods, restricted capabilities, reminder/mandate behavior and recovery remain separate decisions/implementation prerequisites. See ADR 0043.


## Subscription receipt recovery continuation — 2026-11-10

Keep durable payment evidence separate from activation success. Local callback simulation uses owner session/CSRF and never claims provider signature verification. Receipt-valid quotes may activate after expiry if delayed; changed commercial terms need review. Reversals create preserved review cases and hold discretionary subscription changes without automatic deletion, refund or entitlement removal. Approved live provider verification, reversal compensation/operator resolution and cancellation-during-renewal policy remain explicit prerequisites. The tested cancellation-versus-upgrade guard does not substitute for renewal coverage. See ADR 0044.


## Entitlement layering continuation — 2026-11-10

Central precedence is plan/add-on/timed allows, deny-wins commercial overrides, then subscription/workspace/security and operational gates. Current protected corrections cannot be denied by historical-policy inputs. Source example codes are represented without advertising unbuilt features; PROPERTY_MANAGEMENT identifies the implemented register/inventory subset. New supplemental inputs are privileged local configuration bound to the current plan, never evidence of a paid add-on. Live purchase/activation, complete commercial matrix and production governance remain prerequisites. See ADR 0045.

### Unit capacity warning and import dependency — 2026-11-10

Use 80% of the effective grant as a UI warning threshold, not a new commercial quota. Keep Free/Silver/Gold/Platinum defaults and contract-defined Diamond unchanged. Existing whole-unit development counting remains in force; shared counting approval is unresolved. Bulk imports remain unavailable until FS-32 reservation/expiry, scoped durable execution, per-source outcomes and partial recovery are implemented. Fail closed on inventory/evidence mismatch; production operator reconciliation must preserve audited history and is not automated by this change. See ADR 0047.

### Communication measurement — 2026-11-10

Only an explicitly configured local `provider_accepted` metric is supported for workspace email/SMS tests. No default commercial allowances or prices are inferred. Periods, channel and quantity must be configured explicitly by the local operator. Delivery, attempted and charged metrics require distinct trusted evidence and are not selectable yet; WhatsApp/other providers are unavailable. Sign-in traffic is platform/account-owned and excluded from workspace metering. Live policy, providers, invoice correlation, callback verification and operator reconciliation UI remain prerequisites. See ADR 0049.

### Recovery and catalog continuation — 2026-11-10

Local owner reconciliation now supersedes the earlier pending reconciliation-UI note: exact retained acceptance evidence can resolve an unknown operation without resend. Missing evidence, legacy fingerprints and production providers still require future resolution policy. Period history uses complete contract totals selected by overlap, not prorated date totals (ADR 0050).

Amenity definitions retain stable codes and all historical spelling reservations. Source defaults are editable configuration, not installed inventory. Owners can administer this catalog; delegated property administration and physical assessments remain pending (ADR 0051). No new production commercial or legal policy is inferred.
