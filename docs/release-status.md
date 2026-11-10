# Catalog and communication recovery — checkpoint 2026-11-10

The request to implement all functional requirements remains **in progress**. This continuation delivers three integrated slices; it does not complete the platform or Phase 2. AMEN-001 and SAAS-QUOTA-003/004/005 remain Partial; all other missing mandatory work remains in the requirement matrix.

- Owner amenity catalog: `/workspace/inventory`, source-defined configurable defaults, normalized aliases, physical/feature/service classification, create/edit/retire, reviewed confirmation and immutable history. Catalog definitions are not installed inventory.
- Local communication recovery: usage page reviews a version-bound evidence-check request; durable workers query Mailpit/SMS and settle exact acceptance once without resending. Missing/expired/legacy evidence retains the reservation.
- Communication period history: authorized India-date overlap filters, stable pagination, used/reserved/unknown/failed counts and explicit whole-contract totals. Late receipts remain in their original contract.

Changes: OpenAPI **0.53.0**, regenerated TypeScript, inventory module and `/amenity-types` API, communication reconciliation and period APIs, V51–53, SMS exact-message lookup, UI, browser CI, ADRs 0050–0051. No new dependencies or production allowances.

Verification executed:

- Java 21 `./backend/mvnw -f backend/pom.xml -B -ntp verify`: **139 tests passed**, zero failures/errors/skips. Includes PostgreSQL alias races, scope/revocation, stale versions, idempotent recovery, worker fencing, immutable history and audit rollback.
- `npm --prefix web run generate` and `npm --prefix web run typecheck`: passed.
- `./scripts/compose-local.sh up -d --build --wait`: built frontend/backend, applied V51–53 to the actual local stack and reached healthy status.
- `node web/tests/communication-recovery.mjs`: passed both actual local inboxes after an isolated injected receipt-commit failure; exactly one message and one usage event after recovery.
- `node web/tests/amenity-catalog.mjs`: passed real create/edit/lost-response replay/retirement/history/alias rejection, keyboard and mobile/desktop axe checks. An initial test selector also matched Next's route announcer; scoping it to main fixed the test.

Run: start Compose, sign in to an owned workspace, then use the inventory link from properties or the billing usage page. Explicit sandbox communication contract configuration remains required. No local acceptance is claimed as live external delivery. Full screen-reader, low-bandwidth and production recovery verification remain unavailable in this checkpoint.

Final regression: `node web/tests/communication-allowances.mjs` and `node web/tests/property-management.mjs` passed on the V53 stack; final frontend typecheck, `python3 scripts/traceability.py` and `git diff --check` passed. All 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates remain preserved.

Remaining: delegated ownership/operating authority, unit assessments/assets/movements/handover, occupancy and the complete Free lifecycle, production communication contracts/providers and the other pending source requirements. The next dependency-ordered work is Phase 2 ownership/delegation and installed inventory assessment. Existing historical checkpoints below describe their state at the time; this checkpoint supersedes their pending local reconciliation/catalog notes.

# Communication allowances — checkpoint 2026-11-10

SAAS-QUOTA-003 / FS-05 / FS-32 now has a working **owner-authorized local email/SMS acceptance-metering journey**. Overall frontend/backend status remains **Partial**: live WhatsApp/other provider operations, production commercial contracts, delivered/attempted/charged metrics, operator reconciliation UI and complete historical reporting remain pending. No unavailable channel is advertised as included, and no local acceptance is described as external delivery or a charge.

Working journey: local operator configures an explicit immutable workspace/plan/channel/period/allowance contract → owner opens `/workspace/billing/usage?workspace={id}` → sees provider-accepted metric, period, used/reserved/remaining and unavailable channels → confirms one local test to the owner's linked contact → durable operation reserves one slot → worker reauthorizes and sends to Mailpit/SMS inbox → acknowledged acceptance consumes one unit and releases its reservation. The page polls actual state. Account/platform sign-in traffic is excluded from workspace usage. Essential tenancy/authentication access is not blocked by these diagnostic allowances.

Integrity: workspace-scoped reservations and receipt completion share one advisory lock; immutable event uniqueness prevents duplicate metering. Actor/workspace idempotency binds contract/contact version and replays the original queue result even after settlement. Request fields cannot establish recipient, money, quantity or paid eligibility. Dispatch intent commits before HTTP; contact/profile version, current subscription, mode, period and authority are rechecked at execution. Provider rejection or revoked work releases capacity. Unknown outcomes/expired leases remain held and are never blindly resent; late acceptance can resolve the hold once. Tests inject receipt outcomes only inside the integration harness, separately from real-container browser evidence.

Changes: V50 communication contracts, operations, unique events, private RLS, scoped claim/completion functions and audit; `CommunicationAllowances`, local worker and controller; `GET /api/v1/workspaces/{w}/usage/communications`, `POST .../communications/tests`; OpenAPI **0.51.0** and generated TypeScript; connected usage component, operator configuration CLI, CI browser test, ADR 0049 and traceability. Existing prices and plan allowances remain unchanged. No new dependency/provider approval or general messaging entitlement is introduced.

Executed verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- Java 21 Maven `-Dtest=CommunicationAllowancesIntegrationTest test`: **3 passed**, covering last allowance race, held unknown outcome, lease expiry, rejection release, late/duplicate acceptance, exact response replay, changed-body conflict, cross-scope/revoked access and contract overlap/immutability. An initial unnecessary row-lock privilege requirement was removed without expanding runtime write permissions. The focused suite passed again after final exact-response replay changes.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **134 passed**, zero failures/errors/skips, before the final replay-only response refinement verified by the focused suite.
- `./scripts/compose-local.sh up -d --build --wait`: Java/Next build, V49→V50 migration and healthy stack passed.
- `node web/tests/communication-allowances.mjs`: passed real Mailpit and SMS-inbox acceptance, explicit contract configuration, zero sign-in usage, reviewed confirmation, lost-response replay, exactly one inbox message/one consumed unit, exhausted allowance, reload persistence, unknown-channel display and mobile/axe.
- `python3 scripts/traceability.py`; `git diff --check`: passed with all 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: start local Compose; use `python3 scripts/configure-sandbox-communication.py --help` to configure an explicit local email/SMS allowance and period for a sandbox workspace; sign in with that channel's linked test contact; visit the usage route and review the local test. Logs: `/tmp/lmt-communication-full-tests.log`, `/tmp/lmt-communication-final-tests.log`, `/tmp/lmt-communication-browser.log`, `/tmp/lmt-communication-verified-compose.log`.

Recovery boundary: do not clear an unknown reservation or resend merely because a request timed out. Inspect the operation reference and trusted inbox evidence; the internal completion path can record late verified acceptance idempotently. An operator reconciliation interface/adapter is not shipped here, so unresolved unknowns remain a visible held state. Next resumable work is that scoped evidence-reconciliation flow, followed by approved provider-specific contracts/callback evidence for the remaining channels and metrics. Full production readiness and unrelated platform requirements are not claimed.

# Unit counting rules — checkpoint 2026-11-10

SAAS-QUOTA-002 / FS-05 / FS-32: implemented for the existing inventory setup slice, with **Partial** overall frontend/backend status because occupied-tenancy archive protection and resumable imports are not implemented. The chosen new-sandbox definition is one count per independently rentable whole unit or bed, with zero extra count for a shared-unit container. Weighted combinations and live shared commercial policy are not offered.

Working journey: create a **new sandbox workspace** → property setup → shared unit with physical bed capacity → **Separately rentable bed** linked to that unit → usage increases by one → reviewed bed archive releases one → restore reacquires capacity. Properties/buildings/floors count zero; occupant/tenancy/physical capacity does not multiply the quota. Draft, ready and maintenance-blocked items count. Inactive/unavailable inventory is not an automatic capacity release. Occupancy must not discount inventory, but occupancy itself is not implemented by this slice.

V49 binds definitions immutably to workspaces. Existing workspaces retain policy 1 and its shared-unavailable behavior; new sandbox workspaces bind policy 2, while new live workspaces retain shared-unavailable policy 1. New policy publication cannot silently change existing quantities. Bindings/definitions reject update/delete; changing an existing contract requires a reviewed migration. New bindings are audited within provisioning. The actual local upgrade retained 62 policy-1 workspaces; three subsequently created test workspaces bound policy 2.

Changed contracts/modules: OpenAPI **0.50.0** and regenerated TypeScript; commercial workspace-bound policy lookup and shared quantity function; authoritative create/restore/reconciliation use that same function; usage definitions; property hierarchy controls, parent-bed capacity validation and readable counting rules; V49 migration; ADR 0048, decision register, matrix and README. No package/version dependency change. Existing unit/bed classification cannot be changed to evade counts; active children block parent archive, and physical room capacity cannot shrink below active beds.

Executed verification:
- `npm --prefix web run generate` / `npm --prefix web run typecheck`: passed.
- Java 21 Maven `-Dtest=PropertyIntegrationTest,EntitlementEvaluationIntegrationTest test`: **11 passed**, including shared zero/bed one, four repeated archive/replay/concurrent-restore cycles, stale-version rejection, maintenance-blocked counting, parent capacity/archive guards, immutable binding, non-adoption of a newer policy and live shared denial.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **131 passed**, zero failures/errors/skips.
- `./scripts/compose-local.sh up -d --build --wait`: Java/Next builds, V48→V49 migration and healthy startup passed.
- `node web/tests/property-management.mjs`: real UI shared-container/bed counting and release, quota warnings, concurrency-safe rejection, preserved draft, keyboard/mobile/axe passed. `node web/tests/entitlement-evaluation.mjs` and `node web/tests/subscription-downgrade.mjs` also passed, retaining excess inventory through downgrade.
- `python3 scripts/traceability.py` / `git diff --check`: passed; 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: local Compose stack at `http://localhost:3100/login`; provision a new Free workspace and follow the shared-bed journey above. Existing workspaces deliberately do not adopt the new definition. Logs: `/tmp/lmt-counting-tests.log`, `/tmp/lmt-counting-full-tests.log`, `/tmp/lmt-counting-compose.log`.

Remaining dependencies: FS-32 per-source import/reservation/expiry/recovery must use this same bound quantity before enabling imports. Before occupancy is enabled, reviewed archive must enforce allocation/possession/tenancy closure guards; the current unused-inventory archive workflow is not evidence of occupied-tenancy protection. Production shared terms and contract migrations, performance at representative large-portfolio scale and manual screen-reader evaluation remain open. No unrelated platform requirement or phase is marked complete.

# Unit capacity enforcement — checkpoint 2026-11-10

SAAS-QUOTA-001 / FS-05 / FS-32: **working end-to-end for manual unit creation, restoration and archive**, with contextual usage warnings and concurrency/recovery evidence. Overall frontend/backend status remains **Partial**: bulk import and its reservations/expiry/resume contract are not implemented and remain unavailable. Shared counting and live Diamond contracts still require their recorded decisions. No production price or commercial allowance has been invented.

Working journey: local login → workspace → property hierarchy → see **4 of 5 units used**, reservations and remaining capacity → warning at 80% → fifth slot consumed → counted creation/restoration review blocked at capacity → backend independently rejects competing/sixth creation → reviewed archive releases capacity → the retained draft can proceed. Building/floor containers do not consume unit capacity; corrections and existing records are not deleted or globally disabled by quota exhaustion. Counts include drafts under the existing development whole-unit policy and do not reset monthly. Existing versioned defaults remain Free 5, Silver 50, Gold 200, Platinum 1,000 and contract-defined Diamond.

Changed surfaces: `/workspace/properties` and property detail; effective snapshot and owner usage projections; `RentableCapacity` transactional reconciliation; OpenAPI **0.49.0** and generated TypeScript; V48 scoped inventory/allocation/event foreign keys and index; ADR 0047, matrix and setup notes. Snapshot warnings are advisory, while authoritatively locked grant updates enforce capacity. Inventory, allocation mapping, per-allocation/net events and counters must reconcile before capacity changes. Corruption fails closed with `CAPACITY_RECONCILIATION_REQUIRED`; exhaustion returns `QUOTA_EXCEEDED`. No direct public increment/decrement API exists. Existing idempotency, rollback and audit are retained.

Executed verification:
- `npm --prefix web run generate` and `npm --prefix web run typecheck`: passed.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **130 tests passed**, zero failures/errors/skips. After the final resource-mapping guard, `-Dtest=PropertyIntegrationTest,EntitlementEvaluationIntegrationTest test`: **10 passed**.
- Tests cover create/create and create/restore last-slot races (exactly one succeeds), exhausted counter rejection, near/full/over-limit projections, archive release after allowance reduction, corrupted counters/mappings, retry after correction, audit-failure rollback, replay, cross-scope and revoked authorization.
- `./scripts/compose-local.sh up -d --build --wait`: Java/Next builds, additive V47→V48 migration and healthy services passed.
- `node web/tests/property-management.mjs`: real persisted 4/5 and 5/5 UI, backend sixth-unit denial, draft retention, non-counted container availability, archive recovery, replay and mobile/axe passed. `node web/tests/entitlement-evaluation.mjs` and `node web/tests/subscription-downgrade.mjs` passed, including retained six-unit inventory and blocked expansion after downgrade.
- `python3 scripts/traceability.py` and `git diff --check`: passed; all 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: start the local Compose stack, open `http://localhost:3100/login`, provision Free and create a property with four draft whole units. Open the property detail to see the warning, then add the fifth. Review the usage route for evidence. Logs: `/tmp/lmt-unit-quota-full-tests.log`, `/tmp/lmt-unit-quota-final-tests.log`, `/tmp/lmt-unit-quota-compose.log`.

Next concrete dependency: implement FS-32 preview/confirm, per-source idempotent import rows, durable execution/revocation checks, quota reservations, expiry, cancellation and partial recovery before enabling BULK_IMPORT. Shared-bed policy, production operator reconciliation, representative large-portfolio query plans and manual screen-reader verification remain open. This delivery does not mark unrelated tenancy, finance, helpdesk, marketplace, membership, AI or operational gates complete.

# Feature locks and effective snapshots — checkpoint 2026-11-10

SAAS-ENT-002 / SAAS-ENT-003 / FS-05: implemented for existing property setup, manager invitations and entitlement inspection. Overall coverage remains **Partial**: unbuilt essential tenancy workflows, live commercial offers and the full quota catalogue are not represented as complete.

- New active-member `GET /api/v1/workspaces/{w}/entitlements/snapshot` returns frontend-safe effective capabilities, limitation text, qualifying configured plan names, owner-only upgrade actions and aggregate quota counters. No payment history or private grant evidence. OpenAPI 0.48.0 and generated TypeScript. No migration or new dependency.
- Shared notices explain why an upgrade cannot resolve security/commercial restrictions or unimplemented capabilities. Configured local offers are labeled simulations. The backend remains authoritative for permissions, eligibility and atomic quota consumption.
- Request-local no-store snapshots refresh every 15 seconds, on focus/visibility and successful workspace mutations across tabs. Subscription changes therefore invalidate UI eligibility without storing workspace state globally or in browser storage. External changes converge through polling; snapshots are never authorization tokens. Logout and context changes abort reads and clear state.
- Property/invitation draft input survives eligibility refresh. Creation review disables when denied; existing correction/revocation paths retain their separate rules. Actual commands still re-evaluate within their transactions and return stable denial codes.

Verification: generated contracts and TypeScript checks passed; local Compose Java/Next build and healthy startup passed; real-browser entitlement journey passed including mobile/desktop axe, denial/recovery and property draft preservation after a new restriction. Focused PostgreSQL regression run: **8 tests passed**, zero failures/errors/skips (`-Dtest=EntitlementEvaluationIntegrationTest,SubscriptionIntegrationTest,EntitlementOverrideIntegrationTest test`). Tests verify owner/member/foreign/revoked access, no-store, grant changes, configured upgrade guidance and authoritative property mutation denial. Final rebuilt-stack browser regressions also passed: `node web/tests/entitlement-evaluation.mjs`, `node web/tests/property-management.mjs`, and `node web/tests/subscription-upgrade.mjs`. `python3 scripts/traceability.py` and `git diff --check` passed. See ADR 0046. Dedicated cross-tab browser automation, manual screen-reader checks, production and full-lifecycle verification remain outstanding.

Run: `./scripts/compose-local.sh up -d --build --wait`, then visit `/workspace/billing/entitlements?workspace={id}` or `/workspace/properties?workspace={id}` after local login/provisioning. Local grants use the existing `scripts/configure-sandbox-entitlement.py`; no money is collected. Next resumable work: extend the shared notices to each subsequently implemented capability and verify approved live commercial configuration before enabling production upgrade offers.

# Centralized feature evaluation — checkpoint 2026-11-10

SAAS-ENT-001 / FS-05 now has a **working centralized layered evaluator, single-capability API and decision inspector**. Overall frontend/backend status remains **Partial** for full commercial coverage: new add-on/grant inputs are explicitly local configuration, not verified paid purchases. Live add-on activation, complete commercial feature matrix and production grant administration remain pending.

Working journey: **Inspect feature eligibility** from the subscription dashboard (or usage link) → `/workspace/billing/entitlements?workspace={id}` → evaluate a capability → see authoritative eligibility/reason/source, plan/policy versions, operational availability and active supplemental evidence. The same evaluator guards real membership/property commands and subscription previews. Local operator configuration → commercial deny → actual property creation denied → audited revocation → creation allowed is verified against real persistence.

Evaluation combines authoritative active subscription/contracted version and versioned plan entitlements with scoped add-ons, timed grants and commercial overrides. Future, expired, revoked, wrong-plan and wrong-mode inputs do not grant. Deny wins over allow; workspace/security restrictions and operational availability remain hard gates. Protected corrections cannot be commercially denied, including through an older-policy input. PROPERTY_MANAGEMENT gates discretionary property/inventory expansion while corrections/releases retain their own eligibility and permissions. The source example capabilities are represented; unimplemented capabilities fail closed as FEATURE_UNAVAILABLE rather than becoming usable because of a grant. PROPERTY_MANAGEMENT describes the implemented register/inventory subset, not full property-domain completion.

Changes: V47 immutable local entitlement inputs/revocation and audit/RLS, policy 3 with operational/protected-correction metadata and preserved plan bindings; expanded `Entitlements`; `GET /api/v1/workspaces/{w}/entitlements/{capability}` and authenticated route allowlist; active-member read authorization separate from owner business writes; property mutation/configuration lock coordination; OpenAPI **0.47.0**, generated TypeScript, inspector/usage/dashboard integration; `scripts/configure-sandbox-entitlement.py`; ADR 0045, matrix, decisions, architecture, README and sequential Compose CI. No new dependency, price, quota increment, provider call or second business engine.

Integrity evidence: private active-member scope, unknown-code 422, foreign/revoked scope 404, RLS-protected immutable inputs and audited revocation, runtime SELECT-only input access, exact start/expiry and bound-plan filtering, current restriction/availability precedence and server command enforcement. A manager may inspect their workspace's capability but cannot obtain owner property-write authority. Grant changes serialize with existing subscription/membership/property operations. Configuration does not manufacture payment or consume capacity; resource permissions and atomic quotas remain independent. Policy/grant changes can invalidate reviewed subscription terms; paid receipt evidence remains in the existing recovery flow.

Executed verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed with the expanded source/contribution schemas.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **127 tests passed**, zero failures/errors/skips. An earlier run found the missing authenticated endpoint matcher; it was fixed, not bypassed. After the final legacy-policy protected-correction guard and dashboard wording, `-Dtest=EntitlementEvaluationIntegrationTest,SubscriptionIntegrationTest,EntitlementOverrideIntegrationTest test` passed **7 tests**.
- New tests cover add-on/timed union, commercial deny precedence, future/expired/revoked inputs, unimplemented feature blocking, workspace restrictions/suspension, old-policy correction protection, unknown code, scoped/revoked access, manager read versus denied mutation and actual property creation recovery. Existing upgrade expectations now explicitly distinguish operational from unavailable capabilities instead of asserting every catalogue code is implemented.
- `./scripts/compose-local.sh up -d --build --wait`: final Java/Next builds, additive V46→V47 migration and healthy stack passed.
- `node web/tests/entitlement-evaluation.mjs`: passed real operator configuration/revocation, typed inspector, actual property denial/success, add-on unable to enable unavailable AI, linked unknown-field error/recovery, keyboard and mobile/desktop axe. An initial test-only ambiguous selector was corrected to select the actual field-error link.
- `node web/tests/subscription-upgrade.mjs`; `node web/tests/property-management.mjs`: passed existing upgrade and property browser regression against the expanded engine.
- `python3 scripts/traceability.py`; `git diff --check`: passed with 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: see README's central eligibility/local input section. CLI inputs require explicit workspace, capability, kind, effect, expiry and reason; they bind the current plan and cannot target live workspaces. Revoke mistaken inputs and issue new reviewed configuration rather than editing evidence. Expiry is evaluated on reads/commands without a worker. Inspect audit before repeating an uncertain privileged CLI command. Apply V47 before coordinated runtime deployment; old quote fingerprints may need fresh review after policy 3. Next concrete work is verified live add-on activation and governed commercial grant configuration, followed by enabling each product capability only when its actual workflow is available.

Gap review: full owner/manager/tenant lifecycle, active tenancy/inventory/handover, rental finance, helpdesk, reviews/police, signing/exit, marketplaces/tours, personal memberships, remaining SaaS live/renewal/grace/refund, AI and ARCH/INT operational gates remain separately tracked. This does not implement those features or finish Phase 1/7. Full manual screen-reader/low-bandwidth, broad browser regression, performance/load, dependency/secret scans and production restore/provider drills were not rerun. No local input is a live paid entitlement or a production readiness claim.

# Subscription recovery — checkpoint 2026-11-10

SAAS-SUB-006 / FS-04/05 now has **working local durable receipt and activation recovery**. Overall frontend/backend status remains **Partial**. This is an explicitly owner-authenticated sandbox callback adapter, not a live signed provider webhook. Cancellation during renewal, live callback verification/reconciliation and completed reversal compensation/operator resolution remain unimplemented.

Working journey: existing upgrade → payment recovery → pending callback → record a simulated received payment → receipt commits before activation → browser can close → independent Java worker activates → reopen/read durable status. Duplicate callbacks replay without another payment. Activation failure preserves the paid receipt and prior plan; bounded worker retry or reviewed recovery applies without collection. Reversal records an immutable review case, fences pending activation and preserves existing access/records after activation; it does not claim a refund or automatically remove entitlements.

Changes: V46 scoped immutable callback inbox and receipt/job state, bounded fenced claim/failure functions; `SubscriptionSettlements`, controller and worker; existing commercial writer reused for activation without inserting a second paid event; `SettlementView`; callback/retry-activation endpoints and optional typed receipt in existing change/recovery projections; OpenAPI **0.46.0** and generated TypeScript; payment-recovery UI and upgrade link; shared reversal/pending-change guards. No new dependency, price, external payment call or competing backend. ADR 0044, matrix, architecture, decisions, README and sequential Compose CI are aligned. Apply V46 then coordinate API/worker rollout before exposing the callback controls.

| Requested scenario | Delivered / remaining |
|---|---|
| Successful payment with browser closed | Tested local paid callback delivered while the page is closed, then worker restart/activation and reopened status. Real provider integration pending. |
| Repeated callback | Immutable scoped event-key replay, conflicting outcome rejection and concurrent duplicate test; one paid event/activation. |
| Pending payment | Authoritative payment_unknown without capacity activation; delayed pending cannot undo received/applied evidence. |
| Payment reversed | Durable reversal_review case, activation fencing, preserved evidence/access and discretionary-change hold. Refund/credit/compensation and operator resolution pending. |
| Payment succeeded but activation failed | Receipt commits first; injected audit failure rolls back activation only. Bounded retry/review recovery never collects again. |
| Cancellation during renewal | Pending: renewal collection/mandates and approved race policy do not exist. Competing cancellation during an upgrade settlement is separately tested, not counted as renewal coverage. |
| Concurrent upgrades | Existing transactional/unique one-open-change protection retained; queued receipt reserves that same slot. |

Integrity evidence: session/CSRF/current owner checks before callback replay, exact change/receipt ETags, immutable evidence and scope foreign keys, membership/subscription/grant locks, worker original-authority recheck and lease fencing. Quote validity uses receipt time for delayed activation while current policy/offer/subscription/entitlements are revalidated. A later paid callback cannot erase a reversal. Existing payment and tenant/rental/personal boundaries remain intact. Domain conflicts stay review work rather than triggering another charge or invented refund.

Executed verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed after fixing an optional callback-label TypeScript narrowing error.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **124 tests passed**, zero failures/errors/skips, including 12 upgrade/recovery tests. New tests cover durable paid evidence across activation rollback, exhausted-retry/manual recovery, duplicate callback concurrency, scoped denial, revoked authority, reversal before/after activation, delayed processing after quote expiry and competing cancellation.
- `./scripts/compose-local.sh up -d --build --wait`: Java/Next builds, additive V45→V46 migration and final healthy stack passed.
- `node web/tests/subscription-settlement-recovery.mjs`: passed real local login/persistence, pending callback, paid callback with page closed, duplicate event replay, actual worker restart/activation, one paid event, reopened result, reversal review preserving Silver access, mobile/desktop axe and keyboard. The test uses an authorized local callback driver, not a bank/provider.
- `node web/tests/subscription-payment-recovery.mjs` and `node web/tests/subscription-upgrade.mjs`: passed existing cooldown/retry/lost-response, unknown reconciliation, priced upgrade and accessibility regression after the shared UI/backend changes.
- `python3 scripts/traceability.py`; `git diff --check`: passed with all 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: see README's local receipt/activation recovery section. Stop neither API nor worker during normal use; refresh persisted status rather than paying again. Worker runs every five seconds after ten-second startup delay, with 60-second leases and at most five claims per retry cycle. Resolve transient faults before reviewed activation retry. Reversal or commercial-term conflict remains a visible operator-review case; never force state, clear a hold or rewrite financial evidence. Next concrete work is approved operator reconciliation/compensation and renewal intent/mandate lifecycle with verified provider callbacks and cancellation-race policy.

Gap review: full owner/manager/tenant lifecycle, active tenancy/inventory, rental finance, helpdesk, reviews/police, signing/exit, marketplaces/tours, personal memberships, remaining SaaS renewal/refund/grace, AI and ARCH/INT operational gates remain separately tracked. No complete Phase 7 or production readiness claim. Full manual screen-reader/low-bandwidth, broad browser regression, dependency/secret scanning, load/performance and production provider/backup/restore drills were not rerun. All payment outcomes here remain explicitly simulated.

# Failed subscription payment — checkpoint 2026-11-10

SAAS-SUB-005 / FS-04/05 now has **working local failed-upgrade payment recovery**. Overall frontend/backend status is **Partial**. The repository still lacks renewal collection and an approved renewal grace/restriction policy; those transitions are not implemented. Failed upgrades preserve the existing plan, so their grace status is explicitly **not applicable**, with no fabricated countdown. Payment outcomes remain clearly simulated; no bank/provider is contacted.

Working journey: local upgrade → simulated failure → **Review failed payment recovery** (also linked from the dashboard) at `/workspace/billing/subscription/payment-recovery?workspace={id}` → read persisted failure, current plan/access, retry attempts, cooldown, quote/recipient/expiry, grace explanation and restrictions → wait/refresh → explicitly confirm retry of the same request → reconcile pending/unknown outcome → verified simulated success activates once. Cancelled confirmation creates no attempt; reload and lost-response replay retain durable status. Unknown payment never authorizes another attempt.

Changes: `SubscriptionUpgrades` owns recovery projection and bounded retry; `SubscriptionUpgradeController` adds `GET .../subscription/payment-recovery` and `POST .../changes/{id}/retry-payment`; new route/component and failed-upgrade/dashboard links; OpenAPI **0.45.0** and generated contracts; **V45** immutable effective-dated local retry policy, policy binding/attempt/failure metadata and per-attempt outcome uniqueness. New local policy permits three attempts total per confirmed change, 30-second cooldown and manual confirmation only. Existing historical events are unchanged; legacy attempt history is unknown and failed legacy requests need fresh terms. No new dependency or second payment writer. ADR 0043, architecture, matrix, README, decisions and sequential Compose CI are aligned.

Authorization/financial/recovery evidence: current owner and sandbox checks; strong If-Match and scoped idempotency; existing subscription locks serialize competing retries; quote/subscription/offer/policy/entitlement/pending-change revalidation; audit and attempt increment commit atomically. Duplicate failure does not reset cooldown or add an event. Unknown reconciles on the same attempt; applied cannot be reversed by a later failed signal. One successful simulated settlement per change remains enforced. Retry intent alone creates no payment event or capacity change. Existing subscriptions/records are not restricted or deleted by failed discretionary upgrades; independent security status remains visible. An injected audit failure rolls back retry and the same key safely recovers.

Executed verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **120 tests passed**, zero failures/errors/skips. After final current-policy eligibility/live-mode projection changes, the same invocation with `-Dtest=SubscriptionUpgradeIntegrationTest test` passed **8 tests**. Coverage includes cooldown, replay, missing/stale version, concurrent retry, exhaustion, unknown reconciliation, scoped/revoked authority, legacy history and injected audit rollback.
- `./scripts/compose-local.sh up -d --build --wait`: final Java/Next builds, additive V44→V45 migration and healthy stack passed.
- `node web/tests/subscription-payment-recovery.mjs`: passed real local login/persistence, failed-upgrade link, grace/restriction display, unchanged Free access, actual 30-second cooldown, cancelled confirmation, committed-response loss/replay producing exactly one retry, unknown state/reconciliation after reload, one activation and desktop/mobile axe/keyboard checks. No test clock override or real payment.
- `node web/tests/subscription-upgrade.mjs`: passed existing priced-quote, confirmation, unknown-outcome/reload reconciliation, activation and accessibility regression after the shared backend/UI changes.
- `python3 scripts/traceability.py`; `git diff --check`: passed; 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: use the configured local sandbox upgrade in README, simulate failed, open recovery and refresh after the displayed retry time. Exhausted/expired/changed/legacy failures require a new reviewed quote; unknown outcomes require reconciliation. Do not force statuses/counters or rewrite payment events. New versioned policy rows govern future requests; never edit applied migrations or historical policies.

Next concrete step: implement configured renewal invoice/payment intents and reviewed grace, restricted-capability and recovery policy, then verified provider settlement/callback/mandate retry integration. This remains required SAAS-SUB-005/006 work; no completed renewal-grace or Phase 7 claim. Appropriate live reminders and account-wide abuse/rate limits also remain pending.

Gap review: full owner/manager/tenant lifecycle, active tenancy/inventory, rental accounting, helpdesk, signing/exit, reviews/police, marketplaces/tours, memberships, SaaS renewal/refunds, AI and remaining ARCH/INT gates remain separately tracked. No new behavior claims for unimplemented essential tenant journeys. Full manual screen-reader/low-bandwidth, broad browser regression, load/performance, dependency/secret scans and production provider/backup/restore drills were not rerun. Local simulation is not live commerce or production readiness.

# Subscription cancellation — checkpoint 2026-11-10

SAAS-SUB-004 / FS-04/05 now has a **working end-to-end local sandbox paid cancellation**. Overall frontend/backend status remains **Partial**: live cancellation, actual refunds/credits, approved retention/deletion policy and signing-provider continuity are not implemented. The local worker performs real durable transitions; the prior paid upgrade settlement is explicitly simulated.

Working journey: **Subscription dashboard → Review subscription cancellation** at `/workspace/billing/subscription/cancellation?workspace={id}` → review effective billing boundary, access/feature/quota consequences, excess usage, retention requirements and refund/credit applicability → explicit confirmation → persisted schedule → worker reauthorization/application → Free continuation without deleting records. Pending cancellation can be withdrawn or recovered; withdrawal is visibly distinct from paid access ending. Uncertain response retry uses the original key. History survives reload and contains reviewed policy and actual application time.

Changed contracts/modules: canonical `GET/POST .../subscription/cancellation`, `POST .../cancellation/reviews`, `POST .../cancellation/{id}/withdraw` and `/retry`; `SubscriptionCancellationController`; shared commercial boundary engine and private identity notices; OpenAPI **0.44.0** and generated TypeScript; dedicated route/shared accessible review component/dashboard link. Optional immutable cancellation terms extend V44 JSON snapshots; **no new migration, dependency, price or separate subscription writer**. ADR 0042, matrix, decision register, architecture, README and sequential Compose CI are updated. Deploy API/worker together before exposing the new flow.

Integrity evidence: authenticated current-owner scope, strong subscription/request versions, exact cancellation review required, immutable policy/retention snapshot, scoped idempotency replay even after application, one open shared transition, fenced withdrawal and reauthorization after revocation. Plan/allowance changes, request application, cancellation audit and private notice commit atomically. Injected audit failure preserves paid access and retries safely. Existing usage/membership/property records remain; Free expansion is blocked above allowance. Refund/credit statuses are explicitly not_applicable_sandbox with null amounts, never fake processed refunds. No tenant/rental/personal payment data is allocated or changed.

Executed verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **117 tests passed**, zero failures/errors/skips. Initial restricted Docker access prevented container tests; rerun with Docker access passed. After final withdrawal-notice wording, the same command with `-Dtest=SubscriptionDowngradeIntegrationTest test` passed **7 tests**, including all new cancellation cases and existing downgrade recovery.
- `./scripts/compose-local.sh up -d --build --wait`: final Java/Next container builds and healthy services passed; existing V44 database accepted new immutable cancellation snapshots.
- `node web/tests/subscription-cancellation.mjs`: passed real local login/upgrade, retained six-unit inventory, cancellation details, excess warnings, confirmation cancellation, actual withdrawal, same-key lost-response replay, durable reload, actual worker application after restart, blocked expansion, Free dashboard, private notice, keyboard/axe/mobile checks. Only its isolated test workspace's clock was shortened; no live money/provider call.
- `node web/tests/subscription-downgrade.mjs`: passed the existing shared-screen downgrade journey after the cancellation changes, including actual boundary worker processing, retained inventory, lost-response recovery and accessibility checks.
- `python3 scripts/traceability.py`; `git diff --check`: passed with 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved.

Run/demo: use the local sandbox upgrade instructions in README, then the cancellation link from the dashboard. Normal cancellation waits for the persisted period end. Recovery follows ADR 0041/0042: refresh, resolve transient faults then retry, or withdraw and create a new review when policy/authority changes. Never force applied state, rewrite historical terms or delete excess records. Next concrete step is reviewed live cancellation/refund/credit and retention configuration plus provider settlement/mandate integration before enabling live workspaces.

Gap review: full source Scenario 13 (cancellation during partial agreement signing) cannot be certified until signing/provider workflows exist; no agreement/document mutation is introduced. Full owner/manager/tenant lifecycle, occupancy/inventory handover, rental finance, helpdesk, reviews/police, signing/exit, marketplaces/tours, personal memberships, SaaS renewal/refunds, AI and remaining ARCH/INT gates remain separately tracked. No Phase 7 or production readiness claim. Full manual screen-reader/low-bandwidth, broad browser regression, performance/load, dependency/secret scanning and production restore/provider drills were not rerun.

# Scheduled subscription downgrades — checkpoint 2026-11-10

SAAS-SUB-003 / FS-04/05 now supports an **end-to-end local sandbox paid-to-Free downgrade**. Overall frontend/backend status remains **Partial**: paid-to-paid renewal settlement and approved live policy are not implemented. No real money is collected/refunded; this is not completion of Phase 7.

Working journey: subscription dashboard → `/workspace/billing/subscription/downgrade?workspace={id}` → authoritative quota/feature comparison and excess warnings → expiring version-bound review → confirm persisted schedule → actual worker applies Free at the recorded billing boundary → retained records, zero remaining capacity when over limit, explicit excess and private notice. Cancellation, uncertain-response same-key retry, reload recovery and reviewed worker retry are integrated. Above-limit records are preserved; no automatic deletion operation is introduced.

Changes: commercial `SubscriptionDowngrades`, controller, `DowngradeWorker`, identity `SubscriptionNotices`; V44 immutable reviews/request lifecycle/idempotency/RLS and leased recovery; OpenAPI **0.43.0** and generated TypeScript; five downgrade endpoints; centralized time-aware entitlement review; upgrade conflict guard; usage/dashboard excess display; retained-over-limit capacity repair. ADR 0041, README, architecture, decisions, matrix and sequential Compose CI are updated.

Integrity evidence: owner/cross-scope/revoked-authority checks, strong ETags, scoped idempotency, exact policy/version/usage confirmation, membership/subscription/grant locks, fenced leases and one open request. Applying plan, allowances, audit and private notice is atomic. Existing used/reserved counts and underlying records survive. Atomic capacity predicates still block expansion. An injected audit failure rolls back application; restart/retry applies once. Exhausted crashed leases become review_required instead of remaining stranded.

Actual final verification:
- `npm --prefix web run generate` and `npm --prefix web run typecheck`: passed.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **114 tests passed**, zero failures/errors/skips. An earlier regression exposed late-boundary policy evaluation; corrected to evaluate at max(boundary, database now), then reran the full suite successfully.
- `./scripts/compose-local.sh up -d --build --wait`: Java/Next builds, additive V44 migration and healthy stack passed.
- `node web/tests/subscription-downgrade.mjs`: final rerun passed real local login/API records, six-unit excess, feature/confirmation review, cancellation, committed-response loss/replay, persisted scheduling, actual worker application after restart, six retained units, blocked expansion, Free dashboard excess, private notice, keyboard/axe and mobile reflow. Only this isolated test workspace's billing clock is shortened.
- `python3 scripts/traceability.py` and `git diff --check`: passed after documentation updates; all 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo and operational recovery: see README's local downgrade section. Normal requests use the actual persisted billing end. Retry exhausted transient failures through the reviewed UI; changed authority/policy requires cancel and fresh review. Never force state or delete excess data. Next concrete step is configured paid-to-paid renewal pricing and verified settlement, including the full-source Gold 120 → Silver scenario, before enabling those targets.

Gap review: no completion claim for owner/manager/tenant lifecycle, occupancy/inventory/handover, rental finance, helpdesk, reviews/police, signing/exit, marketplaces, tours, personal memberships, AI or remaining ARCH/INT gates. Those remain separately tracked; this task adds no deletion of their records but cannot verify unimplemented full journeys. Manual screen-reader/low-bandwidth, complete browser regression, load/performance, dependency/secret scanning, real provider and production backup/restore drills were not rerun. Live pricing/refund/grace and renewal policies require approved configuration and provider evidence.

# Versioned subscription upgrades — checkpoint 2026-11-10

SAAS-SUB-002 / FS-04/05 now has an **end-to-end local sandbox upgrade flow**. Full live-commerce status remains **Partial**. No real money is collected and no live settlement is claimed. The existing subscription dashboard links to `/workspace/billing/subscription/upgrade?workspace={id}`.

Implemented journey: configure explicit test terms → select source/target offer → server-priced ten-minute quote → review current/requested plan, capacity, newly eligible implemented features, interval, recipient, credit/tax/final INR amount and effective-time rule → confirm a persisted awaiting-payment request → simulate failed/unknown/successful payment → reconcile the same unknown request → atomically apply successful simulated settlement, subscribed plan/version/period, capacity and audit. Pending/failed/unknown do not activate. Applied time and updated dashboard/capacity are server-backed. Source contract history and used/reserved counts are retained. No extra paid features are invented merely because the target is Silver/Gold.

Changes: `SubscriptionUpgrades` / `SubscriptionUpgradeController`; canonical `change-quotes`, `changes`, authorized status and explicitly sandbox-only outcome endpoints; OpenAPI **0.42.0** and generated TypeScript; central `Entitlements.preview` and fingerprint checks; subscription dashboard period projection; V42 immutable offers/quotes/payment events, durable requests/idempotency and subscription terms; additive V43 workspace foreign keys/paid-term constraints. The new route handles explicit confirmation/cancel, stale/expired review, pending/unknown outcome, safe same-key retry, reload recovery and private context changes. CLI `scripts/configure-sandbox-upgrade.py` accepts explicit operator test amounts/rates/intervals; it does not alter published prices. README/ADR 0040/architecture/decisions/matrix and sequential Compose CI are aligned.

Integrity evidence: actor/workspace/operation-bound keys; strong subscription/change If-Match; exact quote policy/effective-grant fingerprint; membership/override lock, subscription row lock and ordered grant locks; one open checkout and one successful simulated settlement per change. Expired/changed quotes and oversized configured totals fail closed. Credit uses configured unused paid-base time, two-decimal HALF_UP arithmetic; taxes are test configuration, not legal policy. Purpose-separated immutable payment events never enter rental/personal ledgers. Injected audit failure rolls back payment, plan and quota changes together while preserving the earlier awaiting request. Current authority is checked again at execution/replay; live workspaces reject the simulator. No external effect occurs before durable intent.

Actual verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- Full `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **107 tests passed**, zero failures/errors/skips before the final fingerprint/range guards. Final focused `SubscriptionUpgradeIntegrationTest,SubscriptionIntegrationTest,FoundationIntegrationTest`: **62 passed**, including effective-grant invalidation and V43. Final `-Dtest=SubscriptionUpgradeIntegrationTest test`: **5 passed**, including the configured-total guard. Initial failures identified and fixed PostgreSQL timestamp binding and runtime allowance-update permission; no errors were hidden or assertions weakened.
- `./scripts/compose-local.sh up -d --build --wait`: actual Java/Next builds, V41→V42→V43 additive upgrade and health passed. Final build rechecked after guards.
- `node web/tests/subscription-upgrade.mjs`: passed real captured-SMS login, explicit test quote/tax/credit display, cancellation with no request, committed-response loss/replay, persisted unknown outcome after reload, reconciliation, exactly one activation/capacity increase, dashboard integration, keyboard, axe and desktop/mobile reflow. The test creates an explicitly labeled test offer and isolated workspace. No real payment/provider call.
- `python3 scripts/traceability.py`; `git diff --check`: passed with all 195 IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT gates retained.

Run/demo: start the existing local Compose stack, use the explicit sample test configuration command in README, then **Subscription dashboard → Review subscription upgrade**. Existing test offers from the browser verification are visible to matching local Free versions. Quote/confirmation does not collect anything. Use simulated outcomes only on this test environment. Expired pending requests must be resolved as failed before a fresh quote; uncertain network results retry the original key; an applied request cannot be reversed by a later failed outcome. Full setup and operator withdrawal/recovery instructions are in README and ADR 0040.

Remaining/prerequisites: approved live pricing/merchant/tax/proration/credit rules, real provider order creation and authenticated callbacks/API reconciliation with durable external dispatch/recovery, production invoice/receipt/credit accounting, renewal lifecycle, scheduled activation when selected, contract-defined Diamond capacity, billing delegation and operational readiness. The mode question received no answer; implementation proceeded with the explicitly stated local simulation assumption, not inferred live authorization. No production prices, tax obligations, legal approvals, credits or real paid entitlements are invented. Other mandatory product/ARCH/INT gaps remain as tracked. Full browser regression, manual screen-reader/low-bandwidth, load/performance, dependency/secret scanning and production backup/provider drills were not rerun. Next concrete step is the selected live provider and reviewed commercial configuration using the same durable intent/verified-settlement invariants; never enable the sandbox outcome endpoint in production.

# Subscription dashboard — checkpoint 2026-11-10

SAAS-SUB-001 / FS-04/05 now has a working private dashboard for the implemented Free subscription lifecycle. **Full requirement status remains Partial** because paid billing/add-ons/channel metering/payment history are not implemented. Every requested section is present with authoritative data or an explicit unavailable/not-applicable explanation; missing data is never presented as zero, an empty paid ledger or verified payment success.

Working journey: existing real sign-in → owner workspace → **Subscription dashboard** at `/workspace/billing/subscription?workspace={id}`. The page shows contracted plan/version, subscription/workspace status, persisted start, Free billing/renewal/invoice applicability, add-on coverage, reconciled unit/manager capacity, email/WhatsApp coverage, published comparison baselines, payment-history coverage and calculated effective entitlements. Safe refresh clears stale details, supports retry and ignores obsolete requests. The contract remains the originally subscribed version after public catalogue changes.

Backend/API: owner-authorized `GET /api/v1/workspaces/{w}/subscription`, read-only repeatable-read `SubscriptionDashboard`, OpenAPI **0.41.0** and regenerated TypeScript. Reuses commercial usage/entitlements and the public catalogue interface; no duplicate billing tables, migration, new dependency, mutation or provider call. Current owner membership is the implemented billing authority. Managers/outsiders/revoked owners get privacy-safe 404; anonymous users get 401. No-store API/private noindex route; anonymous HTML contains no private snapshot. Rental and personal payments are excluded. Existing provisioning/usage audit and transaction recovery remain unchanged; reading creates no payment, quota change or financial audit event.

Verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- Targeted `-Dtest=SubscriptionIntegrationTest test`: 2 passed initially. Full verification exposed a timing-sensitive test fixture (its supposed newer plan could predate the migration seed); corrected fixture timestamp. Final `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **104 tests passed**, zero failures/errors/skips.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual Java/Next builds and health checks; no migration needed.
- `node web/tests/subscription-dashboard.mjs`: passed real captured-SMS login/provisioning/navigation, required sections, null/unavailable semantics, private API/HTML, cross-workspace denial, failure/refresh recovery, keyboard refresh, desktop/mobile axe and reflow. Added to the existing sequential Compose CI job.
- `python3 scripts/traceability.py`; `git diff --check`: passed, retaining 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates.

Remaining: real paid billing period/renewal and calculable invoice estimates; active add-on subscriptions; payer-owned email/WhatsApp usage/allowances; priced eligible upgrades; durable SaaS payment history/pagination. These require their owning commercial/communications workflows, not dashboard fixtures. Source comparison plans are unavailable for purchase; no plan activation or payment collection is added. No separate billing delegation is implemented. Broader FS-04 change/cancel/refund/provider recovery and FS-05 metering gates remain incomplete. No full SAAS-SUB-001/Phase 7/production-readiness claim.

Run/demo: start the existing local stack, sign in at http://localhost:3100/login and select **Subscription dashboard** in an owner workspace. Use **Refresh subscription** after a transient error. Next resumable work is the authoritative SaaS billing/add-on and channel-usage data workflows, then extending this read model to recorded periods/estimates/payment pages. See ADR 0039 and the requirement matrix. Full browser regression, manual screen-reader/low-bandwidth, performance/load, provider sandboxes, dependency/secret scanning and production backup/recovery were not rerun for this bounded dashboard change.

# Property register, hierarchy and rentable capacity — checkpoint 2026-11-10

**The requested complete property-management scope is not finished.** This checkpoint delivers a working owner-only property setup slice for PROP-001 / OWN-WF-001 / MARKET-002, FS-07 with FS-05 capacity integration. Frontend/backend statuses remain **Partial**. The property scope checkpoint in the requirements matrix explicitly lists the remaining FS-08/09 and cross-phase work; none is silently downgraded to optional.

Working journey: existing real email/mobile sign-in → Free workspace → Properties → create/edit address, coordinates and house rules → add/edit building/floor/whole-unit hierarchy → review configuration readiness → archive/restore unused spaces → inspect commercial usage and retained history. Unit links open `/workspace/units/{id}`. Proposed rent/deposit/maintenance remain decimal-string configuration; no recurring rent, tenancy, occupancy, listing or verification is fabricated. Property activation is blocked by missing authoritative prerequisites.

Changes:
- Canonical routes `/workspace/properties`, `/workspace/properties/{id}`, `/workspace/units/{id}`, owner navigation and measured rentable usage. RHF forms, field-error links, explicit confirmation, URL filter, bounded pagination, safe retained input, unknown-outcome replay and mobile layouts.
- OpenAPI **0.40.0**, generated TypeScript models, `PropertyController` / `Properties`, private property/space reads and create/edit/review/archive/restore commands. Existing errors now include the property module so domain conflicts do not fall through to a misleading security response.
- Additive **V41**, property/space/composite parent constraints, command deduplication, immutable history, owner RLS, commercial-owned rentable allocations/events/counting policy and commercial policy version 2. `RentableCapacity` updates grant usage atomically; existing policy versions and historical records are retained.
- ADR 0038, architecture, decisions, README, matrix and sequential Compose browser CI updated. Existing unrelated changes preserved; no dependencies or providers added.

Actual verification:
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **102 tests passed**, zero failures/errors/skips. The initial run exposed an outdated two-capability assertion; the final test compares complete inherited sets and passed. Property tests exercise migration, private owner scope, cross-property parent denial, duplicate/replayed commands, missing/stale If-Match, shared-policy gate, decimal persistence, last-slot race, release/restore reconciliation and injected audit failure with full rollback/recovery.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual Java/Next image builds, V40→V41 upgrade and service health. Rebuilt/rechecked after final route/error changes.
- `node web/tests/property-management.mjs`: passed real captured-SMS login, UI property/unit creation/editing/detail navigation, persistence after reload, configuration readiness, committed-response loss and same-key recovery, archive/restore usage, network failure/refresh, keyboard confirmation, axe WCAG tags and 390px reflow. Added to sequential Compose CI; hosted CI has not run here.
- `python3 scripts/traceability.py`; `git diff --check`: passed; **195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates** retained.

Permission/concurrency/recovery: current workspace owner scope is required, including before replay. Workspace/property IDs select records but do not grant access; RLS and composite foreign keys enforce isolation. Manager membership alone cannot access property data. No verified legal ownership/signing/payout authority is implied. Quota allocation/release, property state, usage events, history, audit and deduplication result commit in the same transaction. Existing-resource confirmations bind to the current version. Audit failure rolls back capacity and inventory; retry with the same key recovers. Readiness and physical capacity are separate from allocation/possession/financial closure.

Remaining mandatory property work: co-owner evidence/authority, accepted property delegation, effective-dated operating/signing/payee policies and approval/task workflows; amenity catalogue, explicit item assessments, serialized/quantity assets and movements, matrix/filter/export, immutable handover/disputes; private unit galleries, meters/charge references, authoritative allocation and occupancy/history, tenancy/financial archive blockers, activation, notifications and property dashboards. Existing hierarchy archive checks are not occupied-property closure checks. Shared-bed counting is **Blocked** on the unanswered business-policy choice; other listed work remains **Pending**, not blocked by that choice. No live property integration or simulated successful verification is claimed.

Run/demo: use the existing local stack at http://localhost:3100/login, create/select a Free workspace and choose **Properties**. README has exact recovery steps. Next concrete implementation is FS-08 property authority/delegation and FS-09 catalogue/assessment/stock-movement vertical slices, followed by readiness prerequisites; retain activation blockers until these and the allocation/finance interfaces exist.

Full-source gap review: broader owner/manager/tenant operations, active tenancy, inventory/handover, dashboards, finance, complete helpdesk, residence reviews, police, signing/exit, marketplace aliases, tours/appointments, independent memberships, paid SaaS, AI and remaining ARCH/INT gates are still incomplete. No phase or production-readiness claim. Full browser regression, manual screen-reader/low-bandwidth verification, representative performance/query plans, dependency/secret scanning and backup/restore/production drills were not rerun for this checkpoint.

# Owner invitation expiry diagnostics — checkpoint 2026-11-10

Implemented owner-facing expiry processing transparency for AUTH-001 / NOTIFY-001, FS-01/02/22; broader requirements remain **Partial**. Members and invitations now displays a real persisted-state snapshot: total invitations, open deadlines, overdue pending records awaiting expiry, recorded expiry, and oldest-overdue deadline/age. Definitions and snapshot time distinguish these metrics from worker health or message receipt.

Added owner-authorized read-only `GET /api/v1/workspaces/{w}/invitation-expiry`, OpenAPI **0.39.0** and generated frontend types. The query uses current owner/RLS scope, one database time and repeatable-read state. No migration, quota effect, job mutation, external service or new plan gate. The UI handles loading, zero backlog, network error, refresh and obsolete-request cancellation. Failures never become zero counts. Existing expiry worker/audit and reviewed replacement remain authoritative.

Actual verification:
- Maven `-Dtest=FoundationIntegrationTest test` with documented PostgreSQL/Testcontainers settings: **56 passed**, zero failures/errors/skips. New tests cover empty/open/due/recorded reconciliation, age/null reset, accepted-state denominator, private cache, anonymous/recipient/manager/cross-workspace/revoked-owner denial.
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual Java/Next builds and runtime health; no new migration required.
- `node web/tests/invitation-expiry.mjs`: passed visible backlog with the actual worker stopped, API network failure without a false zero, refresh recovery, worker restart/completion, private expiry notice, replacement confirmation/retry/acceptance, keyboard/axe/reflow. This expanded test runs in the existing sequential Compose CI job.
- `python3 scripts/traceability.py`; `git diff --check`: passed with 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Demo: open Members and invitations as a workspace owner at http://localhost:3100/login and inspect Invitation expiry processing. Refresh after the worker pass. If overdue work persists, follow the README operator steps to inspect worker/database state and restore service; never delete history to clear the count. See [ADR 0037](architecture/adrs/0037-owner-invitation-expiry-diagnostics.md).

Remaining/next: durable per-record failure/retry-exhaustion recovery and queue-age alerts, pre-expiry reminders and wider event coverage. These diagnostics are invitation-specific, not a general operations dashboard. Other foundation/provider, property/inventory/readiness, discovery/tours, agreements/signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplace, AI and ARCH/INT gates remain incomplete. Full repository regression, hosted CI, manual screen-reader/low-bandwidth, representative high-volume query plans/load and production operational checks were not rerun. No complete-phase or production-readiness claim.

# Reviewed expired-invitation replacement — checkpoint 2026-11-10

Implemented the next expiry recovery slice for AUTH-001 / NOTIFY-001 (FS-01/02/22); overall requirements remain **Partial**. Owners can select **Review replacement** on an expired invitation, review the stored masked contact/workspace/scope/duration, and confirm a new invitation. The original remains expired. Cancellation has no effect; lost responses retain the exact command for retry.

Backend/API: authenticated `POST /api/v1/workspaces/{w}/invitations/{id}/replace-expired`, strong If-Match, scoped idempotency, current owner authority and centralized eligibility. Contact/channel come from the authorized original. A shared private creation routine preserves existing active-member/pending-contact checks without crossing public command idempotency namespaces. V40 provides unique same-workspace lineage and prevents self-replacement. The workspace lock plus database constraint permits one replacement under competing keys. No manager slot is reserved; recipient acceptance independently checks capacity. Exact replay returns the prior resource, while changed confirmation conflicts. OpenAPI **0.38.0**, generated clients and invitation lineage projection are updated.

Actual verification:
- Maven `-Dtest=FoundationIntegrationTest test` against PostgreSQL/Testcontainers: **55 passed**, zero failures/errors/skips, including unexpired rejection, outsider/recipient denial, missing/stale version, competing-key single replacement, no premature allocation, mobile/email preservation, exact replay and changed-confirmation conflict. Rerun after internal creation isolation also passed.
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed, including final response header/error documentation.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual backend/frontend builds, V40 upgrade and runtime health.
- `node web/tests/invitation-expiry.mjs`: passed actual worker expiry after restart, review cancellation with no record creation, mobile owner confirmation, lost-response replay, exactly one linked replacement, retained old record and recipient acceptance, keyboard/axe/reflow. This expanded test already runs in sequential Compose CI.
- `python3 scripts/traceability.py`; `git diff --check`: passed; all source IDs/contracts/aliases retained.

Demo: at http://localhost:3100/login, sign in as the workspace owner and open Members and invitations. For an expired record, select Review replacement and confirm. The recipient receives a new account-inbox invitation and must accept it. No external SMS/email is sent. See [ADR 0036](architecture/adrs/0036-expired-invitation-replacement.md).

Remaining/next: expiry-worker diagnostics/recovery, pre-expiry reminders and further recipient-safe notification coverage. General foundation/provider prerequisites, property/inventory/readiness, discovery/tours, agreements/signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, AI and ARCH/INT gates remain incomplete. Full repository regression, hosted CI, manual screen-reader/low-bandwidth and production operational checks were not rerun. No phase completion, live invitation delivery or production-readiness claim.

# Durable manager invitation expiry — checkpoint 2026-11-10

Implemented persisted invitation expiry and authorized-owner in-app expiry notices for AUTH-001 / NOTIFY-001, FS-01/02/22; overall requirements remain **Partial**.

- V39 adds terminal expired state, due index and a restricted bounded expiry function. It processes at most 25 due pending invitations, shares the existing workspace membership advisory lock, skips contention and rechecks state/deadline. No membership or manager allocation is created/released.
- Jobs-only Java worker reconciles every 30 seconds after a 60-second startup delay. Expiry/version, system audit and current-owner notice commit atomically. Notification failure rolls back; due records remain available after recovery/restart. Revoked-owner notice suppression is audited, and repeated runs do not duplicate the notice.
- Invitation UI shows actionable expired/replacement guidance. OpenAPI **0.37.0** documents existing expired projection versus durable processing and 409/412 behavior. No new browser command or external transport.

Actual verification:
- Maven `-Dtest=FoundationIntegrationTest test` with documented PostgreSQL/Testcontainers environment: **54 tests passed**, zero failures/errors/skips. New coverage includes injected notice failure rollback, simultaneous expiry workers and acceptance, one version/audit/notice, private recipient handling, revoked-owner suppression and replacement acceptance.
- `npm --prefix web run generate`; `npm --prefix web run typecheck`: passed.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual backend/frontend builds, V39 upgrade and service health.
- `node web/tests/invitation-expiry.mjs`: passed real worker expiry after restart, desktop owner/mobile invitee guidance, private owner notice, zero expiry quota consumption, replacement invitation acceptance, keyboard/axe/reflow. The test explicitly advances only its own invitation deadline; the real worker performs expiry. Added to sequential Compose CI.
- `python3 scripts/traceability.py`; `git diff --check`: passed with 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Demo: use existing owner/recipient accounts at http://localhost:3100/login and inspect invitation deadlines on the membership/invitation screens. Due invitations show expired immediately; after the worker pass the still-authorized inviter sees an expiry notice at `/account/notifications`. Send a new invitation only if access is still needed; its acceptance rechecks capacity. For the automated bounded test, run `node web/tests/invitation-expiry.mjs` against the local stack. See [ADR 0035](architecture/adrs/0035-durable-invitation-expiry.md).

Remaining: pre-expiry reminders, recipient expiry notices, queue-age/retry-exhaustion UI and broader provider/event coverage. Persistent database/notice failures require operator investigation through worker logs; this slice does not claim full job operations recovery. Live identity/invitation prerequisites and other foundation, properties/inventory/readiness, discovery/tours, agreements/signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces and AI/ARCH/INT gates remain incomplete. Full repository regression, hosted CI, manual screen-reader/low-bandwidth and production operations were not rerun. No phase completion or production-readiness claim.

Next resumable step: strengthen expiry-job observability/recovery and source-defined reminder delivery without bypassing recipient authority or creating duplicate effects.

# Owner invitation outcome notices — checkpoint 2026-11-10

Implemented acceptance/decline notifications for the original inviting owner (AUTH-001 / NOTIFY-001, FS-01/02/22; overall status remains **Partial**). Account notification routes label these **Invitation outcome**, retain read/filter controls and link to current authorized workspaces. No invitee contact/profile is included.

Backend: authorized membership response transaction calls the identity-owned notification interface. Current inviter authority is rechecked under the existing workspace lock; revoked-inviter declines remain allowed but notice suppression is audited. Replay does not duplicate or backfill notices. A deterministic outcome event, existing account/event uniqueness and command idempotency preserve one outcome. Membership/capacity, both parties' applicable notices, command result and audit commit atomically; notice-write failure rolls back with retryable 503. V38 adds the category; OpenAPI **0.36.0** and generated types are updated. No external provider or new business endpoint.

Actual checks:
- Maven `-Dtest=FoundationIntegrationTest test` with documented PostgreSQL/Testcontainers settings: **53 passed**, zero failures/errors/skips. Includes outsider denial, decline/accept replay, contact omission, recipient isolation, revoked-inviter suppression/no backfill after restoration, notice failure rollback and successful retry.
- `npm --prefix web run generate` and `npm --prefix web run typecheck`: passed.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual backend/frontend builds, V38 upgrade and runtime health.
- `node web/tests/workspace-access-notices.mjs`: passed extended desktop/mobile decline with lost response/replay, owner acceptance/decline notices, persisted read state, private recipient notices, revoked-resource denial, keyboard and axe/reflow. This test already runs in the sequential Compose CI job.
- `python3 scripts/traceability.py` and `git diff --check`: passed. All 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained.

Demo: use the owner and invitee test accounts at http://localhost:3100/login. Send a manager invitation, decline or accept from the recipient, and open `/account/notifications` as the owner. Optional setup opt-in and quiet hours do not suppress this direct in-app invitation response. See [ADR 0034](architecture/adrs/0034-invitation-outcome-notices.md).

Next resumable work: persisted invitation-expiry processing and recipient-safe expiry notifications/recovery. Expiry reminders, broader notification events, live invitation/provider prerequisites, remaining foundation work and the full property/tenancy/finance/helpdesk/review/police/exit/dashboard/commerce/marketplace/AI roadmap remain incomplete. Full repository regression, hosted CI, manual screen-reader/low-bandwidth and production operational checks were not rerun. No complete phase, live sending or production-readiness claim.

# Essential workspace access notices — checkpoint 2026-11-10

Implemented another AUTH-001 / NOTIFY-001 event-delivery slice (FS-01/02/22), with both requirements still **Partial** overall. Accepted manager membership and owner revocation now create account-owned in-app access notices. `/account/notifications` and its existing tenant/workspace aliases label them **Workspace access**, retain read/filter controls and link to currently authorized workspaces.

Changes: identity-owned `WorkspaceAccessNotices` internal interface; workspace membership transaction integration; migration **V37** for the category; OpenAPI **0.35.0** and regenerated types. No new external provider, browser command or queue. Notices bypass optional setup opt-in/quiet hours; recipient scope is restored after the internal call. Membership/capacity/notice/audit commit atomically. Stable event references and command replay suppress duplicate notices. Revocation leaves account history available without restoring workspace authority.

Actual verification:
- Maven `-Dtest=FoundationIntegrationTest test`: **52 tests passed**, zero failures/errors/skips, including existing quota/race/isolation checks and new notice rollback/replay/privacy checks. The first new test expected 500 for an injected database failure; corrected to the application's documented retryable 503 and reran successfully.
- `npm --prefix web run generate`, `npm --prefix web run typecheck`: passed.
- `./scripts/compose-local.sh up -d --build --wait`: passed actual Java/Next builds, V37 upgrade and service health.
- `node web/tests/workspace-access-notices.mjs`: passed desktop/mobile UI acceptance → access notice → revoke with lost response/replay → exactly one revocation notice; recipient privacy, revoked-resource denial, persisted read state, keyboard, axe and reflow. Added to the existing sequential Compose CI job.
- Traceability and whitespace checks passed. Full-repository regression, hosted CI, manual screen-reader/low-bandwidth and production operational checks were not rerun and are not claimed passed.

Demo: at http://localhost:3100/login, sign in as an owner, create a Free workspace, and invite a second test account. Accept in that account, then inspect `/account/notifications`. Revoke from the owner membership screen; the recipient retains the notice while workspace access returns 404. Email/mobile test login uses the existing Mailpit/SMS inboxes. See [ADR 0033](architecture/adrs/0033-workspace-access-notifications.md).

Next resumable notification work: invitation outcome/expiry coverage and recipient-safe owner notifications. Live invitation/provider prerequisites remain unchanged. Wider foundation, property/inventory, discovery/leases, finance/helpdesk/privacy, reviews/police/exit, dashboards, SaaS/personal commerce, marketplaces and AI requirements remain tracked and incomplete; no phase completion or production-readiness claim.

# Account pending notification deliveries — checkpoint 2026-11-10

Delivered the next recorded foundation notification slice: `/account/notifications` and its existing aliases now show pending optional workspace-setup notices separately from delivered account history. AUTH-001 / NOTIFY-001 / MARKET-001 (FS-01/22) remain **Partial** overall.

- Shows real queued/running/deferred/failed/blocked state, next-check time, snapshot/count, empty/loading/error states and bounded paging. Quiet-hour deferral explains shared-notice continuity. Existing preference and workspace recovery links preserve exact confirmation, authorization, ETag and idempotency controls.
- Added authenticated read-only `GET /api/v1/me/notification-deliveries`, OpenAPI **0.34.0** and generated TypeScript contract. Workspace module owns the projection; identity supplies current consent through its interface. No new mutation, provider, queue or financial behavior.
- Current recipient, active owner membership, consent/effective date and nonterminal state filter every page/count. Cursor is actor/purpose-bound, not authority. V36 adds a partial recipient/outbox index; existing durable worker state and audit remain the source of truth.

Actual verification:

| Command/check | Result |
|---|---|
| Maven `-Dtest=OperationsIntegrationTest test` with documented PostgreSQL/Testcontainers environment | PASS: 7 tests, zero failures/errors/skips |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS |
| `./scripts/compose-local.sh up -d --build --wait` | PASS: actual images/frontend build, V36 upgrade and healthy runtime services |
| `node web/tests/quiet-hours.mjs` | PASS: desktop/mobile pending visibility and authorized links, network error/refresh recovery, quiet-hour release after worker restart, disappearance after delivery, existing lost-response/keyboard/axe/reflow checks |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH, 12 INT retained |

Integration tests cover anonymous denial, cross-account visibility/cursor denial, malformed cursor, disjoint multi-page records/count, pre-consent exclusion, opt-out, revoked owner access, failed/completed states and deferred-state projection. Initial tests caught missing authenticated security-route registration; it was corrected and both backend and browser checks rerun successfully. Full repository regression, manual screen-reader/low-bandwidth checks, hosted CI and production operations were not rerun and are not claimed passed.

Run/demo: start the Compose stack, enable optional workspace-setup notices and quiet hours containing the current IST time, create a workspace, and open http://localhost:3100/account/notifications. Review the pending entry, follow preferences to disable quiet hours, then refresh after the next worker check. Delivery moves into existing history without another message. See [ADR 0032](architecture/adrs/0032-personal-pending-notification-deliveries.md).

Next resumable scope: broaden source-defined notification event/category coverage with recipient authorization and real delivery acceptance; current account view covers only optional workspace setup. Provider activation/receipts, assisted all-contact-loss recovery, other foundation gaps and full property/inventory/discovery/leasing/finance/helpdesk/reviews/police/exit/dashboards/membership/marketplace/AI roadmap remain incomplete. No phase completion or production-readiness claim; no external provider prerequisites were fabricated.

## Local email and SMS inbox containers — 2026-11-09

AUTH-001 / MARKET-001 (FS-01/22) remain **Partial** overall; this bounded sandbox delivery slice is implemented. Email verification is captured by Mailpit and mobile verification by a separate SMS simulator with durable SQLite storage. Login/verification show inbox links; OpenAPI 0.33.0 exposes the runtime `localContainers` flag. No new migration or dependency is required. Existing live adapters and native private-file delivery remain separate.

Run `./scripts/compose-local.sh up -d --build --wait`; login at http://localhost:3100/login, email inbox at http://localhost:18025, SMS inbox at http://localhost:8026. Mailpit's usual port belongs to another project and was preserved. See [local inbox runbook](../infra/compose/LOCAL-INBOXES.md).

Evidence:
- Maven identity/delivery regression (`LiveIdentityIntegrationTest,LiveOtpTransportTest,IdentityConfigurationTest,FoundationIntegrationTest`): 57 passed, zero failures. Additional configuration guard run: 4 passed, including rejection of local inbox mode in live identity.
- OpenAPI generation, TypeScript typecheck, actual frontend/backend/SMS Docker builds: passed. All seven runtime services healthy; migration job completed.
- `signin-channels.mjs`: desktop/mobile registration and returning-account login for both channels passed using codes read from the real inbox containers.
- `local-inbox-recovery.mjs`: durable SMS restart, same-body duplicate suppression, changed-body conflict, cross-origin write denial and unavailable-inbox unknown outcome with no authentication passed.
- `compose-smoke.mjs`: actual UI, authorization denial, provisioning, private upload and session persistence after service recreation passed on rerun. First run raced inbox startup after the outage test; recovery test now waits for inbox health before returning.
- Contact removal, notification inbox and notification preferences browser regressions passed with the shared container-code reader. Quiet-hours regression passed on an isolated rerun; its first run overlapped smoke-test service recreation and received a transient proxy response. These shared-stack destructive tests must run sequentially as configured in CI.
- Traceability: 195 functional IDs / 36 FS / 257 aliases / 14 ARCH / 12 INT preserved. `git diff --check` passed.

Recovery commits dispatch before network I/O, fences completion updates and audits outcomes. Unknown deliveries are not blindly resent. Code expiry/single-use/session binding remain backend-enforced. Container inboxes never establish live contact ownership; real email/SMS activation remains dependent on provider credentials, sender approval and external operational evidence. No wider phase completion is claimed. Next step: run the documented local flow with test contacts; broader AUTH-001 and unrelated roadmap gaps remain tracked below.

# SMS/email sign-in integration — checkpoint 2026-11-09

## Delivered implementation and external gate

Implemented an end-to-end **live-mode sign-in/registration code path** using Twilio SMS and Resend email adapters. **Real sending is not activated or verified:** provider credentials, approved SMS sender/template, verified email sender and TLS files were not supplied. The default local environment remains sandbox-only. AUTH-001 / MARKET-001 (FS-01/22) remain Partial, with live acceptance blocked on those external inputs.

- `/login` fetches public runtime identity configuration, offers SMS/email contact verification and shows appropriate sandbox/live/unavailable messaging. `/verify-otp` displays provider submission versus unknown/failure state and actual server expiry; no live code is exposed in browser/local files. `/account` supports a verified live account without presenting sandbox provisioning as available.
- OpenAPI **0.32.0**, generated types: added `/auth/configuration` and live delivery/mode states. Java verifies codes and creates/reuses live-mode accounts, rotates Secure/HttpOnly sessions and enforces CSRF/origin, expiry, attempts, session revocation, per-contact/session limits and an explicit global hourly live request cap. Cross-mode challenge/account/session authentication is denied; no sandbox account conversion/merge.
- **V35** persists challenge mode, dispatch fencing/time, provider reference and sanitized outcome. The API commits durable intent before the worker sends; HTTP calls occur outside database transactions. Fixed provider hosts, bounded response bodies, timeouts, no redirects, worker-only provider secrets and separate OTP key are used.
- Email retries use stable provider idempotency. Retry-safe rejection/transient paths have bounded backoff/jitter; ambiguous SMS outcomes and crash-lost acknowledgements remain unknown rather than being blindly resent. Recovery decisions and dispatch/outcome audits contain IDs/status only. Provider acceptance is never labeled handset/mailbox delivery.
- Added a separate opt-in TLS Compose overlay, private live-secret preparation and startup prerequisite checks. Default sandbox database/volumes are retained. Real live stack was not started because prerequisites are missing. Live account recovery/linking and workspace provisioning retain separate gates; carrier delivery callbacks and reconciliation UI are not implemented here.

## Actual verification

| Command | Result |
|---|---|
| Backend Maven `verify` with documented OrbStack/Testcontainers environment | PASS: **91 tests**, zero failures/errors/skips |
| Focused `LiveIdentityIntegrationTest,LiveOtpTransportTest,IdentityConfigurationTest` after final recovery/origin hardening | PASS: **6 tests** |
| `LiveOtpTransportTest` after explicit failed-provider status handling | PASS |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS |
| `./scripts/compose-local.sh build`; `./scripts/compose-local.sh up -d --wait` | PASS: actual Java/Next images, V35 upgrade and sandbox service health |
| `node web/tests/compose-smoke.mjs`; `node web/tests/contact-removal.mjs` | PASS: existing real sandbox journeys and desktop/mobile identity regression |
| `node web/tests/signin-channels.mjs` | PASS: desktop/mobile mobile/email registration, returning-account login, runtime banner, keyboard submission and logout |
| Live overlay `docker compose ... config --no-env-resolution --quiet` with explicit test HTTPS origin | PASS: configuration syntax; no real credentials loaded or messages sent |
| `./scripts/compose-live.sh config --quiet` without required files | Expected fail-closed: missing `.local/live/runtime.env` |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: all IDs/contracts/aliases retained |

Live integration tests use **real HTTPS, PostgreSQL and actual adapter serialization against isolated HTTP fixture servers** for both channels; they verify Secure/HttpOnly cookies, authenticated profile access, challenge isolation and lost SMS dispatch recovery. Transport tests verify safe failure classification and stable email idempotency. These fixtures are simulated provider responses, not real SMS/email receipts. No approved-provider sandbox/live account test, real inbox/handset delivery, manual screen-reader/low-bandwidth check, remote CI, production scan/restore/capacity or full native browser suite is claimed passed.

## Activate and resume

Follow [live identity setup](../infra/compose/LIVE-IDENTITY.md): run `python3 scripts/prepare-live-identity.py`, supply the actual provider secrets and approved sender configuration under ignored `.local/live/`, provide TLS, choose a request cap, and validate/start with `./scripts/compose-live.sh`. Do not paste keys into chat. The provider-selection question remains unanswered; Twilio/Resend are implementation defaults, not evidence of provider approval or purchase.

The next required acceptance step is a controlled real SMS and real email sign-in at the configured HTTPS origin, including returning-account verification and provider-log inspection. Carrier receipts/reconciliation, broader live recovery/linking, production abuse/retention and all previously tracked rental/product/ARCH/INT gaps remain incomplete. No full Phase 1 or production-readiness claim. See [ADR 0031](architecture/adrs/0031-live-sign-in-delivery.md).

---

# Optional notification quiet hours — checkpoint 2026-11-09

## Delivered scope

**NOTIFY-001 / FS-22 remains Partial frontend/backend.** Implemented configurable quiet hours for the existing optional personal workspace setup notice. All prior uncommitted slices remain preserved.

- `/account/notification-preferences` adds enabled/start/end controls, distinct HH:mm validation, explicit Asia/Kolkata interpretation, overnight support and exact interval confirmation. Start inclusive, end exclusive. Equal times are rejected. Quiet hours default off; legacy clients omitting the group preserve current settings.
- OpenAPI **0.31.0**, generated types and **V34** persist/expose validated quiet policy. Existing scoped authorization, CSRF, If-Match, idempotency and security-change notice apply.
- Existing outbox worker commits the shared workspace notice immediately. During quiet hours only the personal copy remains pending, with `QUIET_HOURS_DEFERRED` and durable next-check time. Creator authority, current consent and quiet policy are checked on each execution. Restart resumes the pending record.
- Policy rechecks occur at the earlier of quiet-hour end or 60 seconds. Deferral records audit/attempt evidence and releases the lease without consuming a failure-cycle attempt. Actual failures retain bounded retry/recovery. Duplicate completion cannot create another notice. Essential security notices remain immediate.
- Workspace operations UI explains partial delivery and recheck timing. No extra queue, broker or external provider was introduced. V34 applied over existing V33 data.

## Actual verification

| Command | Result |
|---|---|
| Backend Maven `verify` with the documented OrbStack/Testcontainers environment | PASS: **87 tests**, zero failures/errors/skips |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS |
| `./scripts/compose-local.sh build`; `./scripts/compose-local.sh up -d --wait` | PASS: Java/Next images/build, V34 upgrade and healthy services |
| `node web/tests/quiet-hours.mjs` | PASS: desktop/mobile policy persistence, actual pending delivery, immediate shared/security notices, worker restart, changed-policy release, lost-response recovery, keyboard/axe/reflow |
| `node web/tests/notification-preferences.mjs` | PASS: existing desktop/mobile opt-in/opt-out regression |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: 195 IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT preserved |

Unit coverage checks same-day/overnight and exact start/end India-time boundaries. PostgreSQL tests check incomplete/malformed/equal-time rejection, replay and legacy update preservation; seven consecutive quiet deferrals remain pending with zero charged failure attempts, shared notice exists once, and later delivery is deduplicated. Browser tests use the real minute-scale recheck and restart the worker. Manual screen-reader/low-bandwidth checks, full native browser suite, hosted CI, security/dependency scans, live providers and production restore/capacity drills were not rerun and are not claimed passed.

## Run and next work

Open http://localhost:3100/account/notification-preferences, enable optional workspace setup notices and quiet hours, then choose an interval containing the current IST time. Create a workspace. Its shared notice appears immediately; `/workspace/operations?workspace=…` explains why the personal copy is pending. Disable quiet hours and the personal notice should arrive on the next check within 60 seconds. Existing history remains; cancellation cannot recall a shared notice already delivered. See [ADR 0030](architecture/adrs/0030-optional-notice-quiet-hours.md).

Next independent notification work is account-scoped pending-delivery visibility/recovery beyond the workspace-owner operations screen, followed by additional source-defined event/category coverage. Other channels, provider consent/callbacks and metering remain incomplete. Repeated attempt retention and high-volume queue capacity remain production decisions. Full-source gaps remain in assisted all-contact-loss recovery, general quota recovery, production identity, properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT gates. No Phase 1 completion or production-readiness claim.

---

# Enforced notification preferences — checkpoint 2026-11-09

## Delivered scope

**NOTIFY-001, FS-22 remains Partial frontend/backend.** Added a working optional account notification preference and connected it to the existing authorized workspace setup outbox handler. Prior contact-removal and inbox work is preserved.

- `/account/notification-preferences`: default-off opt-in, current version, explicit review/confirmation, loading/error/stale handling, unsaved-change warning and exact-command recovery after an unknown response. Inbox navigation links settings and displays security/workspace setup category.
- `GET/PATCH /me/notification-preferences`, OpenAPI **0.30.0**, generated client. Account-owned scope, CSRF, validated boolean, strong ETag and idempotency; no plan gate or client-supplied account authority.
- **V33** adds preference/activation timestamp with account RLS, notice category/source-event fields and recipient/event uniqueness. Preference changes commit command evidence, audit and an essential security notice together. Historical read-command replay defaults to the security category.
- Worker checks current consent and event creation time under the same preference lock used by updates. No past-event backfill; one personal copy per event. Disabling after claim suppresses the personal copy. Shared workspace notice and essential security notices remain unaffected. Suppression/delivery receive distinct audit actions; no external provider or charge is represented.
- Added Compose browser verification to CI. V33 upgraded the existing V32 stack without discarding data.

## Actual verification

| Command | Observed result |
|---|---|
| Backend Maven `verify` using the documented OrbStack/Testcontainers environment | PASS: **83 tests**, zero failures/errors/skips |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS |
| `./scripts/compose-local.sh build`; `./scripts/compose-local.sh up -d --wait` | PASS: backend/frontend images, Next build, V33 upgrade, healthy services |
| `node web/tests/notification-preferences.mjs` | PASS: desktop/mobile real opt-in → workspace → worker → inbox; opt-out, shared/security continuity, lost-response recovery, persisted preference, keyboard/axe/reflow |
| `node web/tests/notification-inbox.mjs` | PASS: existing desktop/mobile inbox regression |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained |

New backend coverage proves default off, cross-account preference isolation, missing/stale preconditions, validation, exact replay and changed-body conflict. Worker tests prove old events are not backfilled, repeated completion does not duplicate notices, and preference revocation after claim is honored while shared delivery succeeds. Native full browser suite, manual screen-reader/low-bandwidth checks, hosted CI, dependency/secret scans, live providers and production restore drills were not rerun and are not claimed passed.

## Run and next step

Open http://localhost:3100/account/notification-preferences. Enable and confirm, create a new sandbox Free workspace, then inspect `/account/notifications`. Disable and create another workspace: its setup/shared notice remains, while no additional personal setup notice is delivered. Existing notice history is retained. See [ADR 0029](architecture/adrs/0029-enforced-account-notification-preferences.md).

Next notification slice is quiet-hour scheduling for optional account delivery with durable deferral/recovery; it must preserve security notices, recheck consent and avoid consuming retry attempts merely because delivery is deferred. Other categories, email/SMS/WhatsApp/push, provider consent/callbacks and metering remain incomplete. No live provider claim, Phase 1 completion or production-readiness claim.

Full-source gap review retains assisted all-contact-loss recovery, general quota recovery, production identity, properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and remaining ARCH/INT gates.

---

# Account notification inbox — checkpoint 2026-11-09

## Delivered scope

**NOTIFY-001 / MARKET-001, FS-22 remain Partial frontend/backend.** Added a dedicated, real account-security inbox at `/account/notifications`, `/tenant/notifications` and `/workspace/notifications`. All routes show only the authenticated account's notices; route placement grants no workspace or tenancy access. Existing `/account/security` embedded notices remain supported. Earlier uncommitted contact-removal changes are preserved.

- All/unread/read filter is retained in the URL. Shows occurrence/read times in IST, filtered total, account-wide unread count, freshness, empty/error states and durable read actions. Unknown mutation outcomes retain the command for exact retry; filter changes abort obsolete requests and reset paging.
- OpenAPI **0.29.0** and generated client add `status` to both GET notification routes. API validates filters and binds opaque cursors to actor/filter. Newest-first ordering uses occurrence time with UUID tie-breaker. Filtered totals and unread count share a repeatable-read transaction with scoped records.
- **V32** adds history/unread indexes. Existing RLS, notification records, strong read-state versions, idempotency log and audit remain the authority. No historical event/read evidence is rewritten.
- Added account/security navigation and private/no-store, noindex coverage for the tenant route. Compose CI now runs the new browser journey.

## Actual verification

| Command | Result |
|---|---|
| Backend Maven `verify` with README OrbStack/Testcontainers environment | PASS: **81 tests**, no failures/errors/skips |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS |
| `./scripts/compose-local.sh build`; `./scripts/compose-local.sh up -d --wait` | PASS: actual images, Next build, V31→V32 upgrade, healthy services |
| `node web/tests/notification-inbox.mjs` | PASS: desktop/mobile real routes, empty/read filters, uncertain-read recovery, URL/reload, private cache, unauthenticated denial, keyboard/axe/reflow |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: 195 IDs, 36 FS, 257 aliases, 14 ARCH, 12 INT preserved |

Backend coverage includes latest-event ordering, equal timestamp pagination, no duplicate/missing pages after marking a notice read, filtered totals versus global unread count, invalid filters and cross-actor/filter cursor denial. The first browser run exposed an ambiguous test selector matching the Next route announcer; narrowing it to the application alert produced a passing rerun. Native full browser suite, manual screen-reader/low-bandwidth tests, hosted CI, live providers, dependency/secret scans and production restore drills were not rerun and are not claimed passed.

## Run and remaining work

Open http://localhost:3100/account/notifications after sandbox sign-in. Change a profile display name to create a real security notice, select Unread, mark it read, then select Read to inspect retained history. The tenant/workspace notification routes show the same account inbox, clearly labeled as such.

[ADR 0028](architecture/adrs/0028-account-notification-inbox.md) documents scope and paging semantics. Channel/category/quiet-hour controls are still incomplete: only essential account-security notices are currently exposed, so this delivery does not add inert preference switches or suppress those notices. Next concrete notification slice is an account-owned optional event/delivery policy with enforced preferences and consent, leaving workspace-owned broadcasts independent. Assisted all-contact-loss recovery, general quota recovery and production identity also remain open.

Full-source gap review preserves unfinished properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration gates. No Phase 1 completion or production-readiness claim.

---

# Retained-contact removal — checkpoint 2026-11-09

## Delivered scope

**AUTH-001 / MARKET-001 remain Partial frontend/backend (FS-01/22, Phase 1).** Implemented removal of one linked sign-in contact after fresh current-session proof through the contact being kept. Existing account/workspace access is preserved; no implicit merge or transfer. This is a functioning local sandbox slice, not live identity verification.

- New `/account/identity-links/remove` screen displays masked target/retained contact, account/version and immediate consequences, with keyboard-focused confirmation, retry-safe uncertain response handling, loading/errors, last-contact guidance and navigation to reauthentication/linking.
- OpenAPI **0.28.0**, generated client: `POST /me/identity-links/unlink`. Validated channel, authenticated actor, CSRF/origin, strong profile ETag and actor-scoped idempotency. A generic recent login or proof through the removed contact is insufficient.
- **V31** adds channel/profile-version proof binding to existing session records. Reauthentication records it; removal requires both contacts and a matching fresh proof. Existing PostgreSQL last-contact constraint remains authoritative.
- Identity module atomically removes contact, increments profile/authentication generation, advances proof invalidation cutoff, invalidates outstanding challenges, revokes other sessions, and writes command result/audit/security notice. Account-generation locks serialize competing removals and registration/linking/reauthentication. Exact retries do not duplicate audit or effects.
- Runtime containers updated and V31 applied over the existing V30 database. Existing volumes and sandbox records retained. CI Compose job now includes the new browser journey.

## Actual verification

| Command | Observed result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **80 tests**, no failures/errors/skips |
| `npm --prefix web run generate`; `npm --prefix web run typecheck` | PASS: generated contracts and strict types |
| `./scripts/compose-local.sh build`; `./scripts/compose-local.sh up -d --wait` | PASS: actual images, frontend build, V31 upgrade and healthy services |
| `node web/tests/contact-removal.mjs` | PASS: desktop/mobile real UI → link → retained proof → removal, lost-success replay, durable profile, other-session denial, keyboard/axe/reflow |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: all 195 IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained |

Backend tests cover last-contact protection, required retained-channel proof, missing/stale preconditions, wrong-account profile, exact replay/changed-body conflict, proof invalidation and one audit event. Simultaneous opposite-contact removals produce exactly one success, leaving exactly one contact. Existing regression tests cover identity recovery, invitation scopes/quotas, storage, catalogue and operations. The native 72-test browser suite, manual screen-reader/low-bandwidth tests, live providers, hosted CI, security scanning and production restore drill were not rerun; these are not claimed passed.

## Run and next work

At http://localhost:3100, link both contacts, open `/account/reauthenticate` and prove the contact you will keep, then visit `/account/identity-links/remove` within five minutes. Review and confirm. After an uncertain response use **Retry same removal**. Removing the last contact is blocked. A later sign-in using the removed contact can create a separate account; confirmation discloses this behavior.

See [ADR 0027](architecture/adrs/0027-retained-contact-removal.md). Assisted recovery with no accessible contacts and reviewed account-merge policy remain unresolved; production identity/notification delivery, abuse/retention policy, notification preferences and general reservation recovery remain incomplete. Next independent foundation slice is account-owned notification preferences with enforceable delivery behavior. Full-source gap review retains all outstanding property/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboard, workspace/personal commerce, marketplace, advertising/AI and ARCH/INT gates. No Phase 1 completion or production-readiness claim.

---

# Full-stack local Docker Compose — checkpoint 2026-11-09

## Delivered scope

Added `infra/compose/compose.yaml` for **edge, web, API, worker, migrations and PostgreSQL**, preserving the root database-only Compose workflow and all existing product changes. INT-09–12 are **Partial**; no functional requirement or product phase is newly marked complete. API routes, OpenAPI contracts and database migrations are unchanged.

- Pinned multi-stage frontend/backend images, standalone Next.js, non-root read-only application runtimes, internal networking and only loopback port 3100 published.
- Separate database bootstrap/migration/runtime roles and file-backed local secrets. One-shot migrations gate startup; runtime checks schema compatibility without DDL credentials.
- API disables domain scheduled jobs; worker enables them, denies business HTTP and has internal database/scheduler health checks. PostgreSQL sessions and shared private quarantine/inbox survive runtime recreation.
- Explicit container-local sandbox identity exception remains restricted to the sole local profile and loopback HTTP origins; a new test rejects production/public-origin use.
- Added reproducible browser smoke and a CI Compose job. CI is configured; hosted execution has not been observed here.

## Actual verification

| Command | Result |
|---|---|
| `./scripts/compose-local.sh config --quiet` | PASS |
| `./scripts/compose-local.sh build` | PASS: both final images; Java compilation and Next build/typecheck |
| `./scripts/compose-local.sh up -d --wait` | PASS: five healthy long-lived services; migration exits 0 on fresh V1–V30 initialization and rerun |
| `node web/tests/compose-smoke.mjs` | PASS: real proxy UI, worker sandbox code delivery, Free provisioning, scope denial, blocked public health/download, persistent session/workspace/private upload across recreation |
| Backend Maven `verify` with documented OrbStack/Testcontainers environment | PASS: **78 tests**, zero failures/errors/skips |
| One-shot migration with deliberately nonexistent DB role | PASS: exits nonzero; no schema write |
| `python3 scripts/traceability.py`; `git diff --check` | PASS: 195 IDs, 36 FS, 257 aliases, 14 ARCH, 12 INT preserved |

Initial validation found and fixed a missing contract file in the frontend build context, Nginx temporary paths on the read-only filesystem, and host publication requiring a separate ingress network. Final checks above use the corrected configuration. The prior 72-test native browser suite was not rerun for this packaging change; the new Compose browser test exercises the changed deployment boundary.

## Run and remaining work

Run `./scripts/compose-local.sh build` then `./scripts/compose-local.sh up -d --wait`; open **http://localhost:3100/login**. [Runbook](../infra/compose/README.md) explains local code retrieval, secrets, persistence, shutdown and recovery. Sandbox records remain for inspection.

No production readiness claim: live identity/providers, TLS, encrypted object storage/scanning, managed secrets, actual backup/restore drills, capacity/alert ownership and full-domain regression gates remain open. Next deployment step is an isolated backup/restore drill using both database and private files; product implementation resumes from the preceding requirement checkpoint below.

---

# Email manager invitations — checkpoint 2026-11-09

## Delivered scope

Owner chooses email → reviews exact target/scope/expiry → saves durable invitation → recovers lost creation response → email-account recipient explicitly accepts → shared manager quota is consumed → owner revokes and recipient loses access. **SAAS-REG-003 / MARKET-001 remain Partial frontend/backend**, using FS-02 and existing FS-01/22 identity linkage. Phase 1 and full manager/property delegation remain incomplete.

## Changes and safeguards

- `/workspace/settings/members` supports mobile/email channel selection and email validation; `/account/invitations` renders masked destination for either channel. Recipient matching never substitutes for explicit acceptance. Confirmation and uncertain-response recovery reuse the existing workflow.
- OpenAPI **0.27.0** extends InviteManager to exactly one mobile/email and adds channel/maskedDestination to invitation results; maskedMobile is null for email. Existing mobile request format and idempotency fingerprints remain valid. Generated frontend types updated; no dependency versions changed.
- WorkspaceMembership/MembershipController remain the single invitation writers. They reuse identity contact normalization and existing member/capacity transitions. **V30** adds email storage/index, exactly-one-contact constraint and participant RLS; it applied successfully over local V29 with historical mobile rows retained.
- Owner authority, workspace sandbox mode, entitlements, pending-duplicate guards, expiry, strong versions, idempotency and explicit acceptance remain enforced server-side. Both API queries and database RLS match only the recipient's current account contact. No email-only account receives a fabricated mobile or implicit account merge.
- Pending invitations reserve no capacity. Email/mobile acceptances share the existing serialized manager-capacity operation. One last slot has one winner; manager membership grants no owner/property/signing/spending/payout authority. Revocation preserves records and releases capacity through the commercial interface.
- Existing command/audit persistence and retry behavior are reused. Email normalization preserves local-part spelling and lowercases the domain. Contact fields are masked in listings; exact entered contact appears only in the owner's confirmation.
- No email or SMS is sent. The durable recipient inbox is real; identity proof delivery remains the private sandbox inbox, not live verification.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **77 backend tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types and production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **72 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

New PostgreSQL tests cover domain normalization/idempotent replay, recipient API and runtime RLS denial, absence of membership before acceptance, no owner/admin rights afterward, a mixed email/mobile last-slot race, invalid/both-channel requests, pending duplicates, expired/revoked acceptance and contact constraints. New browser tests cover lost successful creation response, one persisted invitation, email sign-in, explicit acceptance, capacity display, revoke/denied access, generated contract schemas, keyboard confirmation, responsive layout and axe. Existing mobile invitations remain in full regression against restarted services and one unchanged frontend build.

## Run and next resumable work

At `/workspace/settings/members?workspace=…`, choose **Invitation contact channel → Email**, enter a test email and review/confirm. The recipient signs in using that email or an explicitly linked account, opens `/account/invitations`, reviews scope and accepts. Check usage and revoke through existing controls. Use **Retry same action** after an uncertain response. See README and [ADR 0025](architecture/adrs/0025-email-workspace-invitations.md).

Remaining Phase 1 work includes contact replacement/unlinking, assisted all-contact-loss recovery and reviewed merge policy; notification preferences, live identity/notification providers, production abuse/retention and general reservation recovery remain incomplete. Property-scoped manager delegation is still pending. Full-source mandatory gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration deployment gates.

Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real providers and restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: protected recovery outcome checks

# Protected recovery outcome checks — checkpoint 2026-11-09

## Delivered scope

Uncertain recovery response → protected outcome check without the rotated cookie → durable completion/failure state → separate session check or fresh sign-in. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend.** This closes an outcome-visibility gap in the existing sandbox recovery flow; broader contact recovery and Phase 1 remain incomplete.

## Changes and safeguards

- `/account/recovery` adds **Check recovery outcome** after an uncertain verification. It distinguishes completed, not-yet-completed, failed, expired and locked proof outcomes. A not-yet-completed read warns that an in-flight request may still finish. No status read replays recovery or claims cookie delivery.
- OpenAPI **0.26.0** adds RecoveryChallenge outcome grant/expiry fields, RecoveryOutcome models and POST `/api/v1/auth/recovery/outcome`. Generated frontend contracts are updated; existing public Account payloads remain unchanged. No dependency versions changed.
- **V29** adds completion evidence and a purpose/consumption constraint. Completion commits with session revocation, generation, audit and notice. Historical backfill uses only committed recovery audit records, not consumed proof assumptions. V29 applied over the existing local V28 database.
- Challenge-specific HMAC read grants are derived with a separate purpose prefix and original session binding, expire fifteen minutes after request and are retained only in page memory. They travel in POST bodies, never URLs. General challenge-status responses do not return them.
- Outcome reads require the exact unexpired grant, origin/CSRF protection and no-store. They work with a fresh anonymous cookie but return only challenge ID, coarse status and completion time. They reveal no account/contact/workspace data and confer no authentication authority. The endpoint stays unavailable outside sandbox mode.
- The UI keeps a completed recovery distinct from a usable browser session. Losing/reloading the page loses its memory-only grant; ordinary sign-in with a new code remains the fallback. No repeat recovery is inferred from failure or pending status.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | Final PASS: **74 backend tests**, no failures/errors/skips; executable jar. First run had one PostgreSQL connection-loss error in OfferingAvailabilityIntegrationTest; the new identity tests passed. Full rerun passed. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `docker compose up -d --wait db`, `./scripts/run-api-local.sh` | PASS after local DB unavailability: existing named-volume schema V28 retained; V29 applied and API started. No volume deletion or migration rewrite. |
| `npm run test:e2e -- --workers=1` in web | PASS: **70 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

A read-only SQL reconciliation of the upgraded local database found **0 committed recovery audits missing completion evidence**.

New PostgreSQL cases cover stable request grants, incorrect/cross-challenge/expired grant denial, coarse no-store completion after cookie loss, continued denial of account access, no duplicate recovery audit, failed/expired states and completion constraints. Existing audit rollback now asserts absent completion evidence. The browser lost-response case clears every cookie before checking the real outcome and verifies that account access remains denied. Existing full desktop/mobile regression includes keyboard, mobile reflow and axe recovery checks.

## Run and next resumable work

Use established local startup and the sandbox account-recovery journey. After an uncertain verification response, select **Check recovery outcome** without closing the page. A completed result confirms committed recovery; use **Check current session** separately or sign in with a new code. See README and [ADR 0024](architecture/adrs/0024-protected-recovery-outcome.md).

Next identity work remains contact replacement/unlinking and assisted all-contact-loss recovery with explicit safeguards and reviewed merge policy. Notification preferences/category/quiet-hour controls, general delivery/providers, live identity, production abuse/retention and general quota reservation recovery remain incomplete. Full-source mandatory gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration deployment gates.

Identity delivery remains sandbox-only; outcome evidence is real database state. Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real providers and restore/deployment drills were not run. Restoring the existing local database service is not a backup-restore drill or production-readiness evidence.

---

## Historical report: linked-contact account recovery

# Linked-contact account recovery — checkpoint 2026-11-09

## Delivered scope

Signed-out browser → existing linked contact → recovery-specific proof → explicit session-revocation confirmation → same account/workspaces with a new session → recovery notice. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend.** This implements the sandbox linked-contact recovery subset; recovery with no remaining contact, contact replacement/merge and Phase 1 are not complete.

## Changes and safeguards

- `/account/recovery` is reachable from login. It provides contact validation, masked target/expiry, explicit confirmation, loading/error/pending/expired-proof paths, request retry and uncertain-verification guidance. Sensitive proof fields stay in memory and forms use POST. Signed-in users must sign out before recovery.
- OpenAPI **0.25.0** adds POST `/api/v1/auth/recovery/challenges` and `/auth/recovery/verify`, complete typed schemas and generated frontend contracts. Public sign-in/account payloads remain compatible; internal VerifiedAccount carries server-owned generation. No dependency versions changed.
- **V28** adds `auth_generation`, `auth_recovered_at` and the distinct recovery challenge purpose. It applied successfully over the existing local V27 database without rewriting previous migrations/records.
- Recovery requests behave independently of account existence. Only correct expiring, attempt-limited, session/purpose-bound proof reaches account lookup. No unknown-contact account creation, implicit link or merge occurs. Origin/CSRF protection includes both recovery endpoints.
- The recovery transaction consumes proof, revokes existing session registries/framework rows, advances generation/profile version/cutoff, and records audit plus one in-app notice. Original account ID/workspaces are retained. Earlier sign-in/recovery codes are rejected by the cutoff. Profile-bound proofs become stale.
- Generation locking and registration rechecks prevent a proof verified before recovery from registering afterward. Already-running framework saves cannot bypass revoked registry tombstones. New codes issued afterward can sign in; this does not secure a contact still controlled by someone else.
- Recovery state commits before browser session establishment/cookie delivery. If registration or delivery fails, old sessions may be revoked without a usable new cookie. The UI offers session check or ordinary sign-in with a new code, never blind consumed-proof replay. Database/audit failure before commit rolls back recovery effects.
- Code delivery remains the private local sandbox inbox. No SMS/email, real contact ownership, residence verification or live recovery provider is claimed.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | Final PASS: **72 backend tests**, no failures/errors/skips; executable jar. Initial run had one test ordering failure: it expired a proof before waiting for delivery. Corrected fixture ordering; full rerun passed. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **70 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

New PostgreSQL tests cover linked-email recovery/same workspace, old-session denial, earlier sign-in/recovery proof rejection, purpose/session isolation, explicit confirmation, wrong/expired proof, unknown-contact non-creation, audit rollback and the deterministic pre-recovery-proof/post-recovery-registration interleaving. Browser tests use actual sessions and backend state for mobile recovery, keyboard confirmation, contract validation, same identity, security notice, axe/reflow and deliberately lost successful response guidance. Full regression runs against restarted local services on one unchanged build.

## Run and next resumable work

Use existing local API/web startup. From a signed-out browser, open `/login` → **Recover an existing account and end its sessions**. Enter a previously linked contact, request the dedicated code, retrieve it from the private local inbox and confirm ending existing sessions. Review the resulting security notice. After an uncertain result, check the current session or request a fresh ordinary sign-in code. See README and [ADR 0023](architecture/adrs/0023-linked-contact-account-recovery.md).

Next identity work remains contact replacement/unlinking and assisted recovery when every linked contact is lost, with explicit safeguards and reviewed merge policy. Notification preferences/category/quiet-hour controls, general delivery/providers, live identity, production abuse/retention and general quota reservation recovery remain incomplete. Full-source mandatory gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration deployment gates.

Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real providers and restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: reviewed selected-session sign-out

# Reviewed selected-session sign-out — checkpoint 2026-11-09

## Delivered scope

Account security → select other sessions → review server-confirmed exact selection → atomic sign-out → recover a lost response → retain current browser access. **AUTH-001 (FS-01) remains Partial frontend/backend.** This extends the session-management subset; contact replacement/lost-contact recovery and Phase 1 remain incomplete.

## Changes and safeguards

- `/account/security/selected` and navigation from account security provide selection, paginated loading, exact review, keyboard focus/confirmation, explicit empty state, stale/recent-verification recovery and same-command retry. No optimistic revocation is displayed.
- OpenAPI **0.24.0** adds POST `/api/v1/me/session-revocation-preview` and `/me/session-revocations`, complete request/preview/result contracts and regenerated frontend types.
- Existing AccountControls/AccountController own the operation. A selection contains 1–25 distinct other-session IDs; this bounds a transaction, not a commercial or account-wide session limit. No dependencies, configuration or migrations changed; existing V27 state remains in use.
- Preview is read-only, with a strong ETag bound to actor/current browser/target versions. Confirmation requires CSRF, recent authentication, exact If-Match and actor-scoped idempotency. Selected/current registry rows are locked in deterministic order before validation and writes.
- Any stale/inaccessible/current/duplicate target rejects the whole command. Framework session deletion, registry tombstones, per-session audit, batch audit/command and one in-app notice commit together. Audit failure restores all affected session state. Exact replay returns the prior outcome without duplicate notices.
- Only reviewed sessions end. The current session, unselected sessions and new sign-ins after preview remain available. This does not prevent someone holding a valid sign-in contact from signing in again and is not a replacement for compromised-contact recovery.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **68 backend tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **66 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

New PostgreSQL tests prove wrong-account denial, CSRF/missing/stale preconditions, duplicate/current-target rejection, recent-authentication enforcement, exact replay/changed-key conflict, competing commands, audit rollback and preservation of sign-ins created after preview. Browser coverage uses three actual browser sessions, explicit review and focus, keyboard confirmation, a deliberately lost successful response, one notice, subsequent denial of revoked sessions, current-session preservation, mobile reflow and axe. Existing suites run against restarted local services and one unchanged frontend build.

## Run and next resumable work

Sign in in more than one browser, open `/account/security` → **End selected sessions**, select the other sessions and review before confirming. If recent verification is required, verify the current session then refresh/review. Use **Retry same batch sign-out** after an uncertain response. See README and [ADR 0022](architecture/adrs/0022-reviewed-session-batch-revocation.md).

Next identity work remains contact replacement/unlinking and compromised/lost-contact recovery with explicit safeguards and reviewed merge policy. Notification preferences/category/quiet-hour controls, general delivery/providers, live identity, production abuse/retention and general quota reservation recovery remain incomplete. Full-source mandatory gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration deployment gates.

No new simulated behavior was introduced; identity proof delivery remains sandbox-only. Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real providers and restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: security notification review

# Security notification review — checkpoint 2026-11-09

## Delivered scope

Account security → unread count and notice review → mark as read → recover an uncertain response → reload preserved read history. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend.** This completes the bounded read-state slice for existing account security notices, not general notifications/preferences or Phase 1.

## Changes and safeguards

- `/account/security` now renders NotificationInbox with unread/read labels, first-read time, full-inbox counts, paging, refresh and server-confirmed outcomes. Keyboard activation, empty/error/loading/conflict and unknown-outcome recovery use the existing accessible UI conventions.
- OpenAPI **0.23.0** adds canonical GET `/api/v1/me/notifications` and POST `/me/notifications/{id}/read`; the legacy GET security-notifications route remains compatible. Generated contracts include notice read time/version/ETag and unread count. No dependencies changed.
- Existing identity-owned AccountControls/AccountController remain the writers. **V27** adds nullable read time, version, account/id index and narrowly scoped update grants. It applied successfully over the existing local V26 database without rewriting history. Existing notices remain unread until explicitly reviewed.
- Recipient predicates and RLS prevent other accounts from seeing or changing notices. CSRF, strong notice preconditions, scoped idempotency, command locks and row locking handle concurrent requests and retries. Read state, audit and command evidence commit atomically. A repeated read preserves first-read time; no recursive notification is emitted.
- Unread totals cover every matching record in the transaction, not only the loaded page. Pagination retains stable ID order. Read status is not consent/legal acceptance or external delivery evidence. Existing security notices remain available without workspace administration or a paid plan.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **65 backend tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated types, strict typecheck and production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **64 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

New PostgreSQL tests cover recipient isolation, CSRF, missing/stale preconditions, exact replay, changed-command conflict, concurrent reads, audit rollback and whole-inbox counts/foreign cursors. New browser coverage exercises a real profile-change notice, keyboard read action, deliberately lost successful response, same-command recovery, reload persistence, generated contract shapes, mobile reflow and axe. Existing account/linking/reauthentication and other frontend flows run in full regression on the unchanged build.

## Run and next resumable work

Use the existing API/web startup. After a profile change, open `/account/security`, review Security notifications and select **Mark as read**. Refresh or reload to see retained first-read time. Use **Retry same read update** if a response is lost; a stale-version error requires **Refresh notifications**. See README and [ADR 0021](architecture/adrs/0021-security-notice-read-state.md).

Next identity work remains contact replacement/unlinking and compromised/lost-contact recovery with explicit safeguards and reviewed merge policy. General notification preferences/category/quiet-hour controls, tenant/workspace inbox navigation, delivery rules/providers and metering retain their assigned scope. Phase 1 also retains live identity/notifications, production abuse/retention and general reservation recovery gaps. Full-source mandatory gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and architecture/integration deployment gates.

No new simulated behavior was introduced; the existing identity proof delivery is still sandbox-only. Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real provider verification and restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: current-session sandbox reauthentication

# Current-session sandbox reauthentication — checkpoint 2026-11-09

## Delivered scope

Aged session → recent-verification recovery link → existing-contact proof → session/CSRF rotation → resume protected contact operations with the same account/workspaces. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend. Phase 1 is not complete.** No contact replacement, account merge or live identity verification is claimed.

## Changes and safeguards

- `/account/reauthenticate`, security navigation and shared recent-authentication error recovery connect to real Java use cases. Existing contacts are selected server-side; no contact supplied by the browser establishes authority.
- OpenAPI **0.22.0** adds POST `/api/v1/me/reauthentication/challenges` and `/verify`, typed request/result schemas and regenerated frontend contracts. No dependencies changed.
- `SessionReauthentication` / `ReauthenticationController`, challenge reuse and **V26** bind proof to account, session, purpose and profile version. V26 applied successfully over the existing local V25 database; old migrations and records were preserved.
- Strong profile ETag, CSRF, expiry/attempt/rate limits and durable session locks apply. Successful verification rotates the session identifier, clears CSRF, refreshes recent authentication, preserves the original absolute session deadline and records one in-app security notice. The security screen labels registry authentication dates **Last verified**.
- Registry/proof/audit/notice updates are transactional. Servlet rotation is not part of the database transaction: a rollback after rotation can require sign-in. Lost successful responses offer session-check/sign-in recovery, never replay of consumed proof. Challenge requests retain their idempotency key on uncertain responses.
- Sandbox delivery remains a private local inbox. No real SMS/email or real contact ownership is claimed. No secrets are persisted by the frontend.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **62 backend tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **62 desktop/mobile browser tests** on the unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

PostgreSQL tests cover aged-session recovery, same-account/workspace preservation, rejected previous cookie and stale CSRF, actor/purpose isolation, stale profile, invalid/expired proof and audit-failure rollback. Browser tests cover the protected-operation recovery link, contract validation, keyboard verification, mobile reflow/axe and lost-response guidance. Full regression uses one unchanged frontend build against restarted real local services.

## Run and next resumable work

Use established API/web startup, sign in and open `/account/security` → **Verify current session**. Choose an existing contact, request the proof, read its private local inbox code and submit **Verify and rotate session**. Return to the protected account operation. For an unknown verification outcome, check the current session or sign in again. See README and [ADR 0020](architecture/adrs/0020-bound-session-reauthentication.md).

Next identity work remains contact replacement/unlinking and compromised/lost-contact recovery with explicit safeguards; existing-account merging needs reviewed policy. Live identity/security notifications, conditional federation/push, production abuse/retention gates and general quota reservation/expiry remain incomplete. Full-source gaps remain across properties/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment readiness. This checkpoint does not close those mandatory requirements.

Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real providers and operational restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: explicit sandbox mobile linking

# Explicit sandbox mobile linking — checkpoint 2026-11-09

## Delivered scope

Recent email sign-in → profile → request a mobile-specific linking proof → confirm exact contact/account/profile version → recover a lost response once → use mobile sign-in to return to the same account/workspaces. Matching manager invitations become visible for review and still require explicit acceptance. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend**, with the existing FS-02 invitation flow verified. Phase 1 is not complete.

## Changes and safeguards

- Shared `IdentityLinks` / `IdentityLinkController` replace the email-specific class names. Existing POST `/api/v1/me/identity-links/challenges` and `/me/identity-links` are extended, not duplicated. **OpenAPI 0.21.0** adds the optional email/mobile channel; omitted channel retains historical email behavior and persisted email command evidence remains valid. No dependency versions changed.
- V25 extends challenge purpose/channel binding to `link_mobile`, preserves required actor/session/profile bindings and adds the narrowly required mobile-column update privilege. It was applied successfully to the existing local database. Prior migrations and account/command records were preserved.
- Recent durable-session checks, strong profile ETags, purpose-specific single-use proofs, attempt/expiry limits, contact ownership locks, unique contacts and HMAC idempotency apply in both directions. The writable contact is derived from an allowlisted channel; an existing contact cannot be overwritten. Existing account conflicts are rejected without merge or takeover.
- Mobile sign-in creation and mobile linking share the same contact lock. Successful confirmation preserves the account ID, records one mobile-link audit/command and emits one in-app security notice atomically. The existing email sign-in remains available. No phone number is fabricated for an email-only account.
- Profile and `/account/identity-links` adapt labels/validation/confirmation to the missing contact. Full target, account and profile version are reviewed; unknown outcomes retry the same frozen command. Browser secrets remain in memory only. Sandbox delivery is still the private local inbox; no SMS or email is sent and no real contact ownership is claimed.
- Linking a mobile alone grants no workspace membership. Existing invitation selection now sees the proven sandbox contact; the normal acceptance command still enforces scope, entitlements and manager capacity.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp clean verify` | PASS: **59 backend tests**, no failures/errors/skips; executable jar. Clean removed obsolete renamed class artifacts. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **58 browser tests**, full desktop/mobile regression on one unchanged build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

New PostgreSQL tests prove original email-account/workspace preservation, no invitation visibility before linking, visibility without membership after linking, explicit acceptance and subsequent mobile sign-in to the same account. They also cover purpose/channel constraint denial, sign-in misuse of linking proofs, cross-actor denial, existing mobile ownership conflict, expiry and simultaneous mobile sign-in/linking with exactly one contact owner. Existing email-link audit rollback, stale/recent-session/attempt/replay tests passed through the shared implementation. Browser tests cover reverse-link confirmations, keyboard focus, lost successful response, one security notice, masked profile schema, same account/workspace on return, mobile reflow and axe. Existing no-JavaScript URL-privacy checks also passed.

## Run and next resumable work

Use established local API/web startup. Sign in to the email-only account to keep and open `/account/profile` → **Link an unused mobile for sign-in**. Enter a test Indian mobile in `+91` format, request a separate linking proof, read it from the private local inbox, review and confirm. Use **Retry same linking step** for an uncertain outcome. Sign out and sign in using that mobile to return to the same account. Review matching invitations at `/account/invitations`; none is accepted automatically. Both local services were restarted for verification. See README and the follow-up in [ADR 0019](architecture/adrs/0019-explicit-sandbox-email-link.md).

Next identity work remains contact replacement/unlinking and compromised/lost-contact recovery with explicit safeguards and a reviewed merge policy where two accounts already exist. Live identity/security notifications, conditional federation/push and production retention/abuse gates remain incomplete. General quota reservation/expiry and remaining Phase 1 gates are still tracked. Full-source gaps remain across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment readiness.

Manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, real provider tests and operational restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: explicit sandbox email linking

# Explicit sandbox email linking — checkpoint 2026-11-09

## Delivered scope

Recent mobile sign-in → profile → request a separate linking proof → review target email/current account/profile version → confirm → recover a lost response without duplicating the link → sign in with that email and retain the same account/workspaces. **AUTH-001 / MARKET-001 (FS-01/22) remain Partial frontend/backend.** This adds an unused email to a sandbox mobile account; it does not merge existing accounts or establish real mailbox control.

## Changes and safeguards

- V24 binds linking challenges to purpose, account and profile version. Existing session binding and OTP limits remain. V24 applied successfully to the existing local database; prior deployed migrations were preserved. Linking and email sign-in creation share contact ownership locks, with database uniqueness as the final guard.
- `EmailLinks`, `EmailLinkController`, POST `/api/v1/me/identity-links/challenges` and `/api/v1/me/identity-links`; **OpenAPI 0.20.0** and generated TypeScript. `/account/identity-links` is linked from the eligible profile. No new dependency or competing authentication engine.
- Durable current-session/recent-sign-in checks apply before request, confirmation and replay. A shared session-registry row lock serializes revocation. Profile row locking and strong ETag reject stale approval. Linking proofs cannot sign in, and sign-in proofs cannot link. Proof ownership/session/purpose/version are rechecked in Java.
- Only unused-email addition is supported. Existing account email conflicts are rejected after proof without exposing or merging the other account. Successful contact update, proof consumption, command outcome, audit and one in-app security notice commit together. Invalid attempts persist; infrastructure/audit failure rolls back all success effects.
- Exact key/body retries return the original result; changed confirmation conflicts. Confirmation fingerprints use HMAC rather than a plain brute-forceable code hash. UI confirms exact email/account/version, preserves uncertain submissions in memory, clears incompatible state and warns about unsaved work. No OTP is stored in browser storage.
- Login, OTP and linking forms use POST as their native fallback, preventing contact/code fields from entering URLs when scripts fail. JavaScript remains required for the working application flow; this is not a claim of no-JavaScript sign-in support.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **55 tests**, no failures/errors/skips; executable jar. |
| Same Maven environment with `-Dtest=FoundationIntegrationTest test` after adding a further boundary test | PASS: **25 focused tests**, including linking expiry, attempt lockout and recent-auth request denial. This overlaps the full suite and is not 25 additional independent cases. No backend implementation changed afterward. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types and final production build. |
| `npm run test:e2e -- --workers=1` on the final fixed build | **54 journeys passed; 2 new tests stopped at a noscript text assertion.** All JavaScript-enabled desktop/mobile flows passed. |
| `npm --prefix web run test:e2e -- tests/email-identity.spec.ts --workers=1` after correcting the new privacy tests to verify actual native submission | PASS: **6 focused browser tests**, including the two no-JavaScript cases. Same application build; both outstanding assertions are resolved. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

The earlier browser attempt encountered four failures after a frontend rebuild was mistakenly performed while the test server was running. The server was stopped/restarted on one fixed build; all four affected journeys passed on the subsequent full run. The later noscript visibility/text assertions relied on text that Playwright filters from that element; the final tests verify the actual requirement directly: native POST requests contain no contact query parameters. No failed run is represented as passing.

Backend evidence covers preserved account/workspace identity, purpose/cross-actor denial, stale profile, existing-email conflict, exact/changed replay, recent authentication on replay, unique concurrent ownership claims, audit rollback, schema binding and OTP expiry/attempt boundaries. Browser evidence covers both confirmations with keyboard focus, lost successful response/single notice, profile/context preservation on linked email sign-in, mobile reflow, axe and POST/no-query fallback. Security notifications are real in-app records; no external email is sent.

## Run and next resumable work

Use the established local startup commands. Sign in using the mobile account to keep, open `/account/profile` → **Link an unused email for sign-in**, request a linking code and read it from the existing private sandbox inbox. Review and confirm the link. Use **Retry same linking step** after an uncertain response; reload/request a new proof after stale/expired state or sign in again when recent authentication is required. Both local services ran for verification. See [ADR 0019](architecture/adrs/0019-explicit-sandbox-email-link.md) and README.

Next identity work: reverse linking for email-only accounts, contact replacement/unlinking and compromised/lost-contact recovery, each preserving proven ownership and existing contexts. Two-account merging still needs explicit policy. Live providers/external security notices, conditional federation/push and production abuse/retention gates remain incomplete. General quota reservations/expiry and remaining Phase 1 gates also remain tracked.

Full-source gaps remain across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment readiness. Manual screen-reader/low-bandwidth tests, dependency/secret scans, remote CI, live provider tests and operational restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: independent sandbox email identity

# Independent sandbox email identity — checkpoint 2026-11-09

## Delivered scope

Choose Email on login → request an expiring sandbox OTP → verify through the existing session flow → create a durable independent account → provision a Free workspace → view masked profile → log out and return to the same workspace with the same email. **MARKET-001 (FS-01/22) is now Partial frontend/backend; AUTH-001 remains Partial.** Email delivery is simulated through the private local inbox, not sent to or verified against a real mailbox. Phase 1 remains incomplete.

## Changes and safeguards

- V23 adds unique email identity storage and email challenge support while retaining all mobile account/challenge data. Email-only accounts have no invented phone number. Existing mobile invitations still require the corresponding mobile account. V23 applied successfully to the existing local sandbox database; no historical migration was rewritten.
- Existing `ChallengeService`, `IdentityController`, `AccountControls`, `/auth/challenges`, `/auth/challenges/{id}/verify` and `/me/profile` now support the channel and nullable masked contacts. **OpenAPI 0.19.0**, regenerated TypeScript, `/login` channel selection and contact-neutral verification/profile copy. No new dependency or second identity backend.
- Java revalidates supported ASCII email inputs. Domain case is normalized; local-part case, dots and plus suffixes are retained. Requests bind the normalized contact/channel and session to their idempotency key. Request/status responses mask the contact and do not expose account existence.
- Five-minute expiry, five verification attempts, five requests per contact/session per fifteen minutes, single-use proof, durable delivery intent, audit, session rotation/CSRF and logout/revocation remain in the existing implementation. Concurrent email proofs converge on one account through database uniqueness. Expected verification failures retain attempt/consumption evidence.
- Sandbox proof cannot authenticate an existing live-mode account. Display-name matching never links identities. Email accounts are independent of mobile accounts; explicit linking/merge and contact-change recovery remain unavailable. UI states this before sign-in, and sandbox messaging says no SMS or email is sent.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | Final PASS: **52 backend tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- --workers=1` in web | PASS: **52 browser tests**, complete desktop/mobile regression against rebuilt API/web and PostgreSQL. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT preserved. |

New PostgreSQL evidence covers normalized request replay, session-bound challenge denial, email-only profile, no mobile invitation assignment, consumed proof rejection, returning workspace access, mobile account separation, simultaneous email verification converging on one identity, malformed input, expiry, attempt lockout, request throttling and denial of sandbox proof against an isolated live-mode identity. Existing mobile/account/authorization/concurrency tests remain passing. Browser evidence covers email channel selection, private inbox proof, persisted Free workspace, masked profile schema, logout/return, axe and responsive layout; the full suite also verifies prior mobile, quota, catalogue, operations and storage journeys.

## Run and next resumable work

Use the established README local API/web startup. At `/login`, select **Email**, use a test address and retrieve the challenge code from the private local inbox. After verification, create/select a workspace. Sign out and use the same email local-part spelling to return. Both local services were restarted for verification. See [ADR 0018](architecture/adrs/0018-sandbox-email-identity.md) and the README sandbox email section.

Next identity work: explicit verified-contact linking and recovery with reauthentication, ownership-conflict/merge policy and preserved contexts; live email/SMS delivery needs approved providers and operational policies. Federation and browser push remain conditional. General quota reservations/expiry, remaining entitlement layers and other Phase 1 gates are still tracked. Full-source gaps remain across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment readiness. No source scope was removed.

Manual screen-reader/low-bandwidth testing, dependency/secret scans, remote CI, real email-provider tests and operational restore/deployment drills were not run. Sandbox tests do not establish real mailbox ownership or production readiness.

---

## Historical report: owner-visible repair history

# Owner-visible capacity repair history — checkpoint 2026-11-09

## Delivered scope

Owner opens Usage and eligibility → reviews retained repair reasons and before/after counts → filters by India calendar dates → pages through older revisions. History remains available after the operator repair grant expires or is revoked; current workspace ownership is required on every request. **SAAS-QUOTA-004/005 (FS-05/32) and the related SAAS-ADMIN-002 audit subset (FS-06) remain Partial frontend/backend.** No phase or full functional ID is declared complete.

## Changes and safeguards

- GET `/api/v1/workspaces/{w}/usage/repairs`, `CapacityRepairHistory`, existing owner authorization and repeatable-read orchestration; OpenAPI **0.18.0** and generated TypeScript. The projection returns only revision, reason, previous/corrected counters, allowance at repair and timestamp. Operator identities, request keys, hashes and raw command JSON are omitted.
- `/workspace/billing/usage` includes repair history using the existing URL date filters, truthful totals and freshness, genuine empty/loading/error states, refresh and older-page retry. Requests are aborted and old results ignored on context/filter changes. Repair confirmation now explains that the reason is retained in owner-visible history.
- V22 adds a SELECT-only owner RLS policy and workspace/time plus unique workspace/revision indexes over existing durable command evidence. It adds no repair/write authority, new history records or fabricated timestamps. V22 applied successfully to the existing local database; prior migrations were preserved.
- Pagination is 25 entries, newest repair revision first, with cursors bound to actor/workspace/date filters. Dates are inclusive Asia/Kolkata, default latest 30 days and limited to 366 days. Each page reports its own as-of/count, rather than claiming a frozen cross-request snapshot. Reads remain no-store. Existing repair idempotency means retries create no duplicate history.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **49 tests**, no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contract, strict types, production build. |
| `npm run test:e2e -- tests/capacity-repair.spec.ts tests/membership.spec.ts --workers=1` in web | PASS: **8** desktop/mobile journeys against rebuilt API/web and real PostgreSQL. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT gates retained. |

Backend coverage creates 27 real reviewed repairs from isolated drift fixtures, verifies 25+2 pagination without duplicate revisions, actor/workspace/filter cursor binding, malformed/range errors, India midnight boundaries, co-owner visibility without a repair grant, cross-workspace denial, revoked ownership denial, private response policy, payload minimization and runtime inability to update evidence. Browser coverage verifies the real repair reason/counts appear once, history survives repair-grant revocation, date-filtered empty state, schema and axe checks. Historical time boundary changes are isolated test fixtures, not runtime history editing.

## Run and next step

Start the established local services and use the README reviewed repair instructions if an authorized sandbox counter correction is needed. Open `/workspace/billing/usage?workspace=<workspace UUID>` and scroll to **Manager counter repair history**. No repair grant is needed to read history as an active owner. Both services were restarted for verification.

Next work remains the general reservation/expiry foundation tied to an actual owning operation and explicit counting/consumption policy, plus remaining independent Phase 1 identity and entitlement gaps. General audit search/export, non-owner support access, production governance and source-evidence repair remain pending. Full-source gaps remain across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates. Scanner/live identity/provider prerequisites remain tracked. Full browser regression, manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI, large-history performance and restore/deployment drills were not run. No production-readiness claim is made.

---

## Historical report: reviewed manager capacity repair

# Reviewed manager capacity repair — checkpoint 2026-11-09

## Delivered scope

Separately authorized workspace owner → inspect proposed counter correction → enter reason → confirm exact evidence version → durable counter/audit/command commit → recover lost response once → resume previously blocked invitation acceptance. **SAAS-QUOTA-004/005 (FS-05/32) and SAAS-ADMIN-002 (FS-06) remain Partial frontend/backend.** Phase 1 is not complete.

## Changes and safeguards

- V21 adds scoped expiring repair grants and issuance audit, `workspace_grant.repair_revision`, and runtime-append-only repair commands containing before/result/reason. Existing migrations are preserved. V21 applied to the existing local sandbox database during startup.
- `CapacityRepairs`, `CapacityRepairController`, GET/POST `/api/v1/admin/workspaces/{id}/manager-capacity-repair`; OpenAPI **0.17.0**, regenerated TypeScript and `/admin/workspaces/[id]/manager-capacity-repair`. Existing owner usage links to repair review. No dependency versions changed.
- Requires BOTH current independent capacity-repair authority and active sandbox workspace ownership. Runtime cannot issue grants. `scripts/grant-local-capacity-repair-operator.py` is the explicit local bootstrap. Non-sandbox operation fails closed. Entitlement-override grants are not repair grants.
- Only counter drift is repairable, using server-derived source totals and reservation target zero under DEC-017. Membership/allocation identity correspondence and historical event pairs must agree; missing evidence, unmatched events or source usage over allowance blocks correction. Source evidence, allowance and memberships are never rewritten by repair.
- Authority locks serialize grant revocation; membership and grant-row locks serialize capacity changes. ETag binds reviewed evidence and repair revision. Scoped idempotency replays original outcomes only after current authority checks. Counter update, reason/before/result command and domain audit commit together. Failed audit rolls back all changes.
- UI exposes current/proposed counts, workspace, reason and immediate effect, with focused confirmation, unsaved-input warning, context reset, schema validation, explicit denial/conflict states and same-command recovery for unknown outcomes. Private responses remain no-store.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | Final PASS: **48 tests**, no failures/errors/skips, executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types and final production build. |
| `npm run test:e2e -- tests/capacity-repair.spec.ts tests/membership.spec.ts --workers=1` in web | PASS: **8** real API/PostgreSQL desktop/mobile journeys. |
| `npm run test:e2e -- tests/capacity-repair.spec.ts --workers=1` after final UI context/title edits and rebuilt restart | PASS: **2** desktop/mobile repair journeys. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT retained. |

PostgreSQL evidence includes grant/owner/cross-workspace denial, exact replay and changed payload, revoked grant on replay, missing/stale precondition, two simultaneous confirmations (one success, one stale), audit-failure rollback, source-membership mismatch denial and real invitation acceptance after authorized repair. Browser evidence includes initial denial, explicit local grant, exact-count confirmation, keyboard focus, lost successful response/single revision, schema/cache checks, restored owner usage, revoked authority, mobile reflow and axe. Isolated tests inject drift with the migration role; the application performs the reviewed repair. Fixtures are not production incidents.

## Run and next step

Use established README local startup instructions. A trusted local operator runs `python3 scripts/grant-local-capacity-repair-operator.py <account UUID> <workspace UUID> --reason "Investigated sandbox counter drift"`, then the currently authorized owner opens the printed route. The screen only offers a repair when source evidence supports it. Use **Retry same repair** after an uncertain outcome, and refresh/review after a stale confirmation. `--revoke` revokes subsequent access. See [ADR 0017](architecture/adrs/0017-reviewed-manager-capacity-repair.md). Both local services were restarted for verification.

Next Phase 1 work remains general durable quota reservations/expiry and their real owning-business-operation integration, plus effective grants/add-ons using approved counting/consumption rules. Counter source-evidence repair, non-owner support access, production approval governance and alerting remain pending. This workflow is sandbox-only; it does not change pending-invitation counting or enable production operations.

Full-source gap review remains open across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates. Scanner/live identity/provider prerequisites remain recorded in earlier checkpoints. Full browser regression, manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI and operational restore/deployment drills were not run in this bounded slice. No production-readiness claim is made.

---

## Historical report: manager quota reconciliation evidence

# Manager quota reconciliation evidence — checkpoint 2026-11-09

## Delivered journey and scope

Owner reviews current capacity → expands source-backed reconciliation evidence → sees unavailable remaining capacity when evidence disagrees → refreshes after external investigation. Manager invitation acceptance independently rejects inconsistent commercial evidence before reserving capacity. **SAAS-QUOTA-004/005, FS-05/32 remain Partial frontend/backend**; this is a bounded Phase 1 integrity/visibility slice, not completion of general quota recovery.

## Changes

- `ManagerUsageEvidence` checks committed allocation totals, usage-event balance, each allocation's required commit/release event pair and events without a matching workspace allocation. `ManagerCapacity` uses that check under the existing grant-row and membership transaction locks. Equal aggregate totals can no longer hide a missing historical event pair.
- Sandbox DEC-017 acceptance reserves and consumes within one transaction; pending invitations never reserve. A persisted nonzero manager reservation is therefore unexplained, yields `CAPACITY_RECONCILIATION_REQUIRED`, and leaves remaining capacity unknown. No counters, memberships or historical events are silently repaired.
- Existing owner-authorized GET `/workspaces/{w}/usage` and `/entitlements` expose aggregate evidence through OpenAPI **0.16.1** and regenerated TypeScript. `/workspace/billing/usage` adds accessible expandable evidence and explicit refresh/investigation guidance. No new endpoint, migration, dependency, authority grant or public counter mutation was added. Existing membership audit/idempotency transactions remain intact; denied consumption rolls back the provisional membership.
- Evidence is a current repeatable-read snapshot, independent of the history date filters. Rentable-item measurements remain unavailable. Owner scope and existing private no-store responses remain in force; evidence contains aggregate counts, not other members' personal data.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 44 tests; no failures/errors/skips; executable jar. |
| `npm --prefix web run generate`, `npm --prefix web run typecheck`, `npm --prefix web run build` | PASS: generated contracts, strict types, production build. |
| `npm run test:e2e -- tests/membership.spec.ts --workers=1` in `web` | PASS: 6 desktop/mobile tests against rebuilt API/web and PostgreSQL; schema, private cache policy, keyboard journey, axe, actual invitations/acceptance/revocation, lost-response retry and reconciliation refresh. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 functional IDs, 36 FS, 257 aliases, 14 ARCH and 12 INT gates preserved. |

PostgreSQL tests deliberately inject isolated counter drift, a missing historical commit/release pair with unchanged aggregate balance, and a stranded reservation. They assert truthful evidence, null remaining capacity, distinct denial, rollback of provisional membership and no new usage events. Existing suite retains last-slot concurrency, idempotent release and cross-scope denial checks. Browser drift is injected/restored only for a newly created test workspace through the local migration role; this fixture is **not an implemented operator repair feature**.

## Run and remaining work

Use the established README local startup commands, sign in to the sandbox, create a workspace and open **Usage and eligibility → Reconciliation evidence**. Both local services were restarted for verification. A normal workspace reports matching counts; the automated tests exercise drift safely in isolated fixtures. Do not alter production records to demonstrate this state.

Next: implement a separately authorized, version-bound and audited operator repair workflow with an explicit safe repair policy; general durable quota reservation/expiry/reconciliation and add-ons remain pending. Do not reuse entitlement-override authority as counter-repair authority. Existing invitation counting policy stays unchanged.

Full-source gap review remains open across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates. Scanner/live identity/provider and production governance prerequisites remain recorded in earlier checkpoints. Full browser regression, manual screen-reader/low-bandwidth checks, dependency/secret scans, remote CI and restore/deployment drills were not rerun in this bounded slice. No production-readiness claim is made.

---

## Historical report: scoped temporary entitlement overrides

# Scoped temporary entitlement overrides — checkpoint 2026-11-09

## Delivered journey and status

Explicitly scoped operator → review manager-onboarding configuration → confirm temporary allow/deny or inherit → persist and audit once → recover a lost response → owner sees source/expiry and contextual restriction → authoritative invitation/acceptance enforces the result. **SAAS-ENT-001/002/003 / FS-05** and **SAAS-ADMIN-002 / FS-06** remain **Partial** frontend/backend. No phase or full functional requirement is declared complete.

## Changes and safeguards

- **V20** adds independently scoped operator grants with expiry/revocation/audit, current capability override revisions, subject/member/operator RLS and append-only-runtime before/result command evidence. Runtime cannot issue operator grants. The local bootstrap is `scripts/grant-local-entitlement-operator.py`; it validates existing sandbox account/workspace identifiers.
- GET/POST `/api/v1/admin/workspaces/{id}/member-entitlement`, `EntitlementOverrides`, **OpenAPI 0.16.0** and generated TypeScript. The private admin page confirms workspace UUID, effect, reason and Asia/Kolkata expiry. Current authority, CSRF, expiry-aware ETag and actor-scoped idempotency are mandatory.
- Existing `Entitlements` remains the single evaluator: plan inclusion → active temporary override → subscription availability → workspace/security restrictions. No new permission engine or entitlement counter API. Only WORKSPACE_MEMBER_ADD is configurable; corrective revocation is protected by the schema and remains operational.
- Override changes share the existing membership lock, so onboarding and administrative changes serialize. A separate authority lock shared with the grant-audit trigger serializes operator revocation without granting runtime write privileges on authority. Configuration, command snapshots and audit commit together; failures roll back.
- Owner `/workspace/billing/usage` displays effective source/expiry; its typed snapshot also carries the override revision. `/workspace/settings/members` explains denied onboarding and disables invitation review, while existing correction controls remain. Both invitation creation and pending acceptance independently re-evaluate in Java; client disabling does not establish the invariant.
- Expiry automatically falls back to plan evaluation; inherit records a new configuration revision. No price, subscription reference, manager allowance, existing membership or personal membership is rewritten by an override. An allow cannot grant record authority, bypass restrictions or consume an unavailable quota slot.
- Administration fails closed outside sandbox mode. The maximum 24-hour effect and 1–24 hour local authority bootstrap are development safety controls, not approved production commercial policy. No dependency versions changed.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **43 backend tests**, no failures/errors/skips; executable jar. Socket path is this machine's OrbStack configuration. |
| Same Maven environment with `-Dtest=EntitlementOverrideIntegrationTest test` | PASS: **2 tests**, strengthened after the full run to exercise actual revocation/capacity release while onboarding is denied. No implementation changed afterward. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: contract drift check, strict types and production build. |
| `npm run test:e2e -- tests/entitlement-override.spec.ts --workers=1` | PASS: **2** desktop/mobile override journeys. |
| `npm run test:e2e -- --workers=1` | PASS: **46 browser tests** against the final API/build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved. |

PostgreSQL evidence covers owner-versus-operator scope, cross-workspace denial, operator grants not conferring workspace usage access, both invitation paths, atomic membership rollback on denial, restriction/security precedence, quota exhaustion despite allow, expiry/inherit fallback, stale expiry ETags, concurrent changes, audit rollback, exact replay/changed body, revoked authority on replay and schema protection for corrective capability. Non-sandbox administration is tested fail-closed. Browser evidence covers actual scoped bootstrap, keyboard confirmation, lost successful response, one recorded revision, schema conformance, owner limitation/source UI, backend denial and restoration, revoked authority and accessibility scans.

V20 applied successfully to the existing local database. No historical migration was rewritten. Dependency/secret scans, remote CI, production governance, manual screen-reader/low-bandwidth checks and operational restore/deployment drills were not run. No production-readiness claim is made.

## Run and next resumable work

Use [README scoped override instructions](../README.md#scoped-temporary-entitlement-overrides) and [ADR 0016](architecture/adrs/0016-scoped-temporary-entitlement-overrides.md). Start the established local API/web commands if their sessions have ended; both ran for verification. A trusted operator grants one account authority over one workspace, then opens the printed admin route. Supply effect/reason/expiry, review and confirm. Retry uncertain outcomes with **Retry same override**; refresh/review after conflicts or expiry. Revoking operator authority blocks further commands but does not automatically undo previously issued effects.

Next Phase 1 work: general quota reservation/expiry/reconciliation integration and remaining effective-grant/add-on layers using explicit counting policy. Other capabilities, production overrides/approvals, paid billing and live identity remain incomplete. Scanner/provider integration, clean release/download grants, production private storage and retention/privacy workflows remain explicitly blocked/pending as recorded previously.

Full-source gap review remains open across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates. This bounded entitlement slice removes none of that mandatory scope.

---

## Historical report: durable scan preparation

# Durable scan preparation — checkpoint 2026-11-09

## Delivered journey and status

Account-owned upload → atomic quarantine and scan-preparation intent → real worker attempt → visible blocked prerequisite → reasoned cancellation/recheck → safe recovery after a lost response. **Phase 1 storage/worker foundations, FS-21, ARCH-08/10 and INT-03 remain Partial** frontend/backend. No functional ID or phase is declared complete. This operation does not perform malware scanning or implement the Premium vault.

## Changes and safeguards

- **V18** adds documents-owned durable jobs, attempts and subject-RLS command evidence. New upload/quarantine/queue/audit changes commit together. Existing quarantined uploads are backfilled with current-time queue/audit evidence, not fabricated historical scans. **V19** adds command/job account consistency as a forward migration; V18 was already applied locally and was preserved.
- `ScanOperations`, `ScanWorker`, `ScanController`; GET `/api/v1/documents/{id}/scan`, POST `.../retry` and `.../cancel`; **OpenAPI 0.15.0**, generated TypeScript. No dependency changes. Worker defaults/properties are recorded in ADR 0015.
- `/account/uploads/{id}/scan`, linked from private uploads, shows status, blocker, latest five attempts/truthful total, update/lease/eligible times in Asia/Kolkata, RHF/Zod reason validation, exact-version confirmation and bounded polling. Unknown outcomes preserve the same key/reason/version; account changes clear private state.
- Worker claims use skip-locked rows, unique lease tokens and persisted attempts. Completion reauthorizes the account/upload/quarantine relationship and checks the lease before/after bounded local integrity work. Cancellation and expired claims cannot later commit. Temporary failures back off with jitter; automatic cycles stop after five attempts. Explicit rechecks preserve cumulative history and reset only the cycle count.
- Storage loss/corruption/unavailability and missing subject authority are distinct blockers. Intact bytes produce **SCANNER_UNAVAILABLE**, never clean. The current state machine has no successful/clean result. No file bytes are sent to an external scanner and no downloads/sharing are enabled.
- Current session, subject authorization, CSRF, strong ETag, reason, scoped idempotency and database subject constraints protect recovery. Recheck returns **202 + Location** for durable acceptance; GET supplies current worker state. Audit failures roll back commands and queue changes. Quarantine remains immutable.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **40 tests**, no failures/errors/skips; executable jar, including final V19 constraint. Socket is machine-specific. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: no drift, strict types, production build. |
| `npm run test:e2e -- tests/scan-status.spec.ts --workers=1` | PASS: **2** desktop/mobile scan-preparation journeys. |
| `npm run test:e2e -- --workers=1` | PASS: **44 tests** on the final API/worker and production frontend build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved. |

PostgreSQL tests cover atomic upload enqueue/rollback, subject denial and composite foreign key, CSRF, missing/stale versions, competing claims, cancellation fencing, changed-key confirmation conflicts, current authorization on replay, expired-lease takeover, transient failure/backoff, five-attempt exhaustion and retained history across cycles. Recovery audit failure leaves the old job status intact. Failure/clock manipulation is confined to isolated test fixtures. Browser tests use actual uploads and the real scheduled worker, observe unavailable scanning, lose a successful cancellation response, retry, reload, accept a 202 recheck and verify two recorded attempts, contract shape, blocked downloads and accessibility scans.

The initial compile-only package command failed because an assumed JdbcClient `optionalRow` method is not present in the pinned dependency. It was replaced with the repository's supported list/optional pattern; complete verification subsequently passed. That failed compile is not passing evidence. V18/V19 applied successfully to the existing local database. Manual screen-reader/low-bandwidth checks, external scanner contracts, restore drills, dependency/secret scans, remote CI and deployment readiness were not verified.

## Run, blocked dependencies and next work

Follow [README durable scan preparation](../README.md#durable-scan-preparation) and [ADR 0015](architecture/adrs/0015-durable-scan-preparation.md). Use established local API/web commands if their sessions have ended; both ran for verification. Upload a test file, choose **Scan preparation**, and review the blocker/attempts. Supply a reason before reviewing cancellation or recheck. Use **Retry same scan command** after an uncertain response, or refresh/reconfirm after a stale version. Do not alter job counts, delete mismatching evidence or manually clear quarantine.

Actual malware/format-validation adapters, versioned scan evidence, clean release/expiring access, private encrypted S3 storage, shares, retention/holds/privacy deletion, orphan cleanup and production alerting remain Pending/Blocked on approved configuration and policy. The current worker is real prerequisite processing, not a simulated scanner. Next independent Phase 1 work: remaining centralized effective-grant layers and quota reservation/recovery, while scanner/provider prerequisites stay explicit. Paid commercial configuration and live identity are still incomplete.

Full-source gap review: property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates remain open. No mandatory requirement, alias or source phase has been dropped or marked complete by this foundation.

---

## Historical report: private storage integrity and recovery

# Private storage integrity and recovery — checkpoint 2026-11-09

## Delivered journey and status

Account subject → review a quarantined file → check actual private bytes → inspect a timestamped integrity result → confirm restoration of a missing file using the original bytes → recover a lost response without duplicate effects. **Phase 1 storage foundations, FS-21 and ARCH-06/10 remain Partial** frontend/backend. No functional ID or phase is declared complete; NOTIFY-001, TEN-PREM-002 and other document-purpose workflows retain their prior status.

## Changes and integrity

- **V17** adds subject-scoped current storage observations and command journals, composite upload/account foreign keys and RLS. Original V16 upload/receipt/quarantine evidence is unchanged. No historical migration was rewritten.
- GET `/api/v1/documents/{id}/storage`, POST `.../storage/check`, POST `.../storage/restore`; `StorageRecovery` use case/controller; **OpenAPI 0.14.0** and generated TypeScript. The existing storage interface now supports bounded integrity inspection. No dependency or configuration changes.
- `/account/uploads` links to `/account/uploads/{id}/storage`. The UI shows recorded time (Asia/Kolkata), scope and unchecked/intact/missing/mismatch/unavailable outcomes; checks are explicitly separate from malware clearance. Restoring requires exact original-file selection and confirmation; stale versions require refresh, unknown outcomes preserve the original key/file/version in memory.
- Current session, account filters/RLS, CSRF, strong review ETag and actor-scoped idempotency protect operations. Commands lock the upload row; competing checks cannot silently overwrite a review. Replay reauthorizes and returns the original timestamped result; refreshing reads the latest saved observation, not continuous monitoring.
- Missing-file restore rechecks storage and exact original size/SHA-256, refuses different bytes, and preserves quarantine. The original intent and missing review predate object I/O. Observation, command and audit commit atomically; a matching object after an audit failure is reconciled on retry. Restorative I/O is bounded local filesystem work; a future remote provider requires durable dispatch before integration.
- The local adapter publishes its private temporary file with create-new hard linking, avoiding atomic-move replacement of a competing destination. It reads bounded bytes without following object symlinks. Unsupported filesystem operations fail closed; the adapter remains local-sandbox only.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **37 backend tests**, no failures/errors/skips, executable jar. OrbStack socket is machine-specific. |
| Same Maven environment with `-Dtest=UploadIntegrationTest,StorageModeTest test` | PASS: **5 tests** after strengthening concurrent restoration-retry coverage; no implementation changes after the full run. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: no contract drift, strict types and production build. |
| `npm run test:e2e -- tests/storage-recovery.spec.ts --workers=1` | PASS: **2** desktop/mobile recovery journeys. |
| `npm run test:e2e -- --workers=1` | PASS: **42 browser tests** on the final API/build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved. |

Backend evidence covers cross-account read/check/restore denial, subjectless RLS, CSRF/preconditions, competing checks, stale restoration, mismatched/symlink object protection, exact replay/changed confirmation conflict, session revocation, preserved quarantine and audit-failure recovery. Two concurrent retries after physical restoration produce one durable restore command/audit. Missing/mismatching objects are isolated test fixtures, not live data loss. Browser tests remove only their own freshly created sandbox file, reject a wrong original, confirm with keyboard, lose a successful response, retry once and verify bytes, schema, persisted status, blocked download and accessibility scans. No malware pass is simulated.

V17 applied to the existing local database successfully. Full restore drills, encrypted remote object storage, power-loss guarantees, manual screen-reader/low-bandwidth testing, dependency/secret scans and remote CI were not run; this is not production readiness.

## Run and next resumable work

Follow [README recovery instructions](../README.md#check-or-recover-a-private-sandbox-file) and [ADR 0014](architecture/adrs/0014-private-storage-recovery.md). Start the established local API/web commands if their sessions have ended; they were running for verification. Open `/account/uploads`, choose **Review stored file**, then **Check stored file**. If storage reports a missing file, choose the original bytes and **Confirm restoration**. After an unknown outcome use **Retry same storage command**. Do not delete mismatching evidence to force restoration.

Scanner-job state/leases/retry contracts, configured scanning/full-format validation, production S3/encryption, clean release/expiring downloads, shares/revocation, retention/holds/privacy deletion and orphan cleanup remain Pending/Blocked on their documented prerequisites. This continuation implements integrity reconciliation from the prior checkpoint; scanner-operation work remains open. Next concrete slice: durable scan-operation status and recovery with honest unavailable-provider handling, before permitting any clean release. General entitlements/quotas and remaining identity gates also remain tracked.

Full-source gap review remains open across property/readiness/inventory, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, paid workspace/personal membership, marketplaces, advertising/AI and ARCH/INT deployment gates. No mandatory scope was removed or relabelled complete.

---

## Historical report: private upload quarantine

# Private upload quarantine — checkpoint 2026-11-09

## Delivered journey and status

Sandbox account → select a test file → review exact file/type/size → create a short-lived private intent → transfer actual bytes → inspect durable quarantine → recover a lost response or resume after reload. `/account/uploads` is linked from the account screen. **Phase 1 storage foundations, FS-21 and ARCH-06/10 are Partial**. No functional requirement ID is declared complete: NOTIFY-001, TEN-PREM-002 and other FS-21-linked domain workflows retain their prior status. This is not a Premium vault or a tenancy/police document workflow.

## Changes and safeguards

- **V16** adds account-owned upload intents, exact fingerprints/length/type, expiry, quarantine state, subject RLS, scoped uniqueness and immutable quarantined evidence. No delete privilege is granted to runtime.
- Documents module, `PrivateObjectStore` interface and local-only POSIX quarantine adapter. Non-sandbox storage fails closed. Files persist outside web assets with private permissions; original filenames are labels, never object paths.
- POST/GET `/api/v1/documents/upload-intents`, GET `/api/v1/documents/{id}`, POST `/api/v1/documents/{id}/finalize`; **OpenAPI 0.13.0**, generated TypeScript and binary transfer through the existing same-origin API helper. No dependency upgrades.
- Current opaque session, CSRF, account/RLS filters, exact intent ETag and actor-scoped create idempotency are required. Transfer binds its logical operation to the intent ID and SHA-256; concurrent identical retries return one quarantined result/audit. Expired unfinished intents reject transfer; status ETag changes at expiry.
- Authorization precedes bounded byte reading. Size, digest and declared-format header mismatches fail. Header validation alone is not malware or full-format clearance. The persisted/UI status explicitly says scanning is unavailable, and no download/sharing/clean state exists.
- The durable intent precedes private file writes. Audit failure rolls back metadata but can leave a matching private object; exact retry reconciles it. UI preserves the confirmed file/key in memory, warns on unsaved navigation, ignores obsolete account responses and supports original-file reselection after reload. No sensitive browser-storage drafts.
- Fixed existing shared error-advice precedence so field validation returns canonical 422 with field errors instead of Spring Boot's generic 400. Public catalogue advice is unchanged.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **35 tests**, no failures/errors/skips; executable jar. Machine-specific OrbStack socket. |
| Same Maven environment with `-Dtest=UploadIntegrationTest,StorageModeTest test` | PASS: **3 tests** after final expiry-ETag correction. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: unchanged regenerated contract, strict types and production build. |
| `npm run test:e2e -- --workers=1` | PASS: **38 tests**, before final expiry-ETag/obsolete-response refinements and the additional reload-resumption case. |
| `npm run test:e2e -- tests/uploads.spec.ts --workers=1` | PASS: **4 tests** on the final code/build, including reload resumption and keyboard confirmation. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved. |

PostgreSQL tests verify cross-account denial, RLS without subject context, CSRF, missing/stale ETags, size/fingerprint/header mismatch, expiry, concurrent retry, preserved private bytes after audit rollback, single quarantine audit, immutable rows and file permissions. Production-mode test rejects local storage. Browser tests exercise actual bytes, lost successful response, persisted status, schema conformance, blocked retrieval, spoofed-file errors, keyboard confirmation/focus, and unfinished-intent resumption with wrong-file rejection. Accessibility scans pass on desktop/mobile; no manual screen-reader claim.

The first backend run exposed the 400/422 advice precedence bug; it was fixed and suites rerun. Initial browser assertions matched both the application error and Next's route announcer; the test now targets the application error, and verification passed. A build command invoked from the repository root failed because the package lives in `web`; subsequent builds used the correct directory. Failed attempts are not passing evidence. V16 applied successfully to the existing local database without rewriting earlier migrations.

## Run, limitations and next step

Follow [README private staging](../README.md#private-upload-staging-sandbox-only) and [ADR 0013](architecture/adrs/0013-private-upload-quarantine.md). Start the existing API/web commands, sign in with test details and open `/account/uploads`. The final test run uses running local API/web services. Retry uncertain results with **Retry same upload**. After reload use **Resume** on an unexpired awaiting intent and select the original file. Keep `.local/quarantine` with the local database; do not erase objects to repair failed commands.

**External/remaining prerequisites:** private encrypted S3-compatible storage, scanning and full-format validation, durable scanner jobs, clean release, purpose-specific expiring retrieval/sharing, retention/holds/privacy requests and orphan cleanup. Local technical bounds (1 MiB, 15 minutes, 20 intents/hour) are not commercial policy. No live provider, automatic cleanup, retention promise or production deployment is enabled. Restore drills, power-loss durability, dependency/secret scans, remote CI, manual screen-reader and low-bandwidth verification remain unverified.

Next concrete slice: scan-operation state and recoverable private-storage reconciliation with explicit unavailable-provider behavior, before any clean release/download grant; keep general entitlement/quota and identity gaps tracked. Full-source gaps remain open across property/inventory, discovery/tours/appointments, signing/occupancy, finance, active tenancy/helpdesk/privacy, reviews/police, exit, dashboards, workspace/personal commerce, marketplaces, advertising/AI and ARCH/INT deployment gates. No phase is complete.

---

## Historical report: offering availability

# Published offering availability — checkpoint 2026-11-09

## Delivered journey and status

Authorized operator → inspect published offering → confirm withdrawal/restoration across its audience/code → persist and audit once → recover an uncertain response → see public catalogue reflect the confirmed state. **SAAS-ADMIN-002 / FS-06** and **SAAS-PLAN-001–003 / FS-03** remain **Partial** frontend/backend. Mandatory workspace Free stays selectable under SAAS-REG-002. No full requirement or phase is declared complete.

## Changes and integrity

- **V14** adds family-scoped availability/revision, independent false-by-default management authority, grant audit and durable command snapshots. **V15** binds publication approval to availability revision; applied migrations were not rewritten.
- GET/POST `/api/v1/admin/plan-versions/{id}/availability`, `OfferingAvailability`, publication preview changes, **OpenAPI 0.12.0**, generated TypeScript and private `/admin/plans` / `/admin/consumer-plans` confirmation UI. `CatalogService` filters withdrawn families after latest-version selection; `CatalogController` keeps its established boundary.
- Withdrawals cover current and future published versions without older-version fallback. Workspace/consumer codes are isolated. Existing subscription references, capability grants, prices and money are unchanged. Free withdrawal fails both business and database checks.
- Current explicit authority, CSRF, strong ETag, reason and actor-scoped idempotency protect writes. Shared locks serialize publication/availability changes. Audit failure rolls back state and journal; concurrent updates yield one success and one stale response. Revoked actors cannot replay prior results.
- Publication preview discloses whether the offer will remain hidden. A changed availability revision rejects the old approval, requiring fresh review. Private UI preserves exact confirmation for uncertain retries and supports conflict refresh. Browser tests default to one worker because catalogue availability is shared local state.

## Actual verification

| Command | Result |
|---|---|
| `DOCKER_HOST=unix:///Users/mwgbh/.orbstack/run/docker.sock TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **32 tests**, no failures/errors/skips; executable jar. Socket path is this machine's OrbStack configuration. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: generated contract unchanged, types and production build. |
| `npm run test:e2e -- --workers=1` | PASS: **36 browser tests** before the final availability-bound publication preview change. |
| `npm run test:e2e -- tests/plan-review.spec.ts tests/zz-offering-availability.spec.ts --workers=1` | PASS: **12 desktop/mobile tests** on the final production build, including lost-response recovery and accessibility scans. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates preserved. |

Initial Docker checks failed because the default socket was absent; explicit OrbStack configuration resolved that. Adding a second backend test exposed a global command-count assertion shared across tests; scoping it to the test actors corrected the isolation issue and the full suite passed. The first final browser attempt failed with connection-refused because interrupted service sessions had stopped; both services were restarted and all 12 tests passed. No failed attempt is counted as passing evidence.

Backend evidence includes audit rollback, audience isolation, last-write conflict, exact replay/changed-body conflict, future publication while withdrawn, latest-version restoration, preserved Free subscription/eligibility, protected Free, revoked access and availability-bound approvals. V14/V15 applied successfully to the existing local database. Browser suites restore their temporary availability changes. Full restore drills, manual screen-reader/low-bandwidth checks, production governance, dependency/secret scans and remote CI remain unverified. No production-readiness or live-provider claim is made.

## Run and next resumable work

Use [README availability instructions](../README.md#withdraw-or-restore-a-published-offering) and [ADR 0012](architecture/adrs/0012-offering-availability.md). The local grant requires `--allow-offering-management`; inspect at `/admin/plans`. Retry uncertain outcomes with **Retry same availability change**. API and built web were restarted for the final verification; use established README startup commands if their sessions have ended.

Next Phase 1 implementation: private storage/upload quarantine and authorized retrieval foundations, alongside tracked general entitlement/quota gaps. Broader commercial prices/taxes/features/quotas, new plan families, add-ons, subscription lifecycle and production approval governance remain Pending. Live identity and provider approvals remain external prerequisites. This checkpoint supersedes earlier offering-deactivation Pending notes only for catalogue withdrawal/restoration.

Full-source gap review: property/inventory/readiness, discovery/tours/appointments, signing/occupancy, rental finance, active tenancy/helpdesk/privacy, reviews/police, notices/exit, dashboards, paid workspace/personal memberships, marketplaces, advertising/AI and ARCH/INT deployment gates remain tracked and incomplete. Existing source IDs/aliases and the dependency-ordered roadmap are preserved.

---

## Historical report: draft discard

# Evidence-preserving draft discard — checkpoint 2026-11-09

## Delivered journey and status

Separately authorized operator → review an unpublished draft and permanence warning → confirm reason and exact version → discard once → recover a lost response → inspect the retained terminal record. **SAAS-ADMIN-002 / FS-06** and **SAAS-PLAN-003 / FS-03** remain **Partial**. This is unpublished-draft discard, not deactivation of public offerings.

## Changes and integrity

- **V13** adds terminal `discarded` state, database protection against editing/deleting discarded versions, independent false-by-default discard authority and grant audit, and durable discard command evidence.
- POST `/api/v1/admin/plan-versions/{id}/discard`, `PlanDraftDiscard`, **OpenAPI 0.11.0** and generated TypeScript. Current authority, CSRF, ETag, reason and scoped idempotency are required; creation/edit/publication authority does not imply discard.
- Actor/plan/row locking serializes discard with editing and publication. Only one terminal transition wins. The state, revision, original terms snapshot, reason, result and audit commit together; failure rolls everything back.
- Private review routes focus the confirmation, explain permanence/evidence retention, block duplicate submission and retain the exact key/body after an uncertain response. Discarded rows remain visible without edit, publication or discard controls.
- Terms, source provenance and prior edit/review evidence remain intact. Version numbers and effective-instant uniqueness are retained. Public catalogue selection, existing subscriptions and money are unchanged. The linked public `CatalogController` needs no modification for this lifecycle action.

## Verification evidence

Completed implementation-run checks on 2026-11-08:

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **30 tests**, no failures/errors/skips; executable jar. |
| `npm run generate`, `npm run typecheck`, `npm run build` in `web` | PASS. |
| `npm run test:e2e -- tests/plan-review.spec.ts --workers=2` | PASS: **10** desktop/mobile administration journeys. |
| `npm run test:e2e -- --workers=2` | PASS: **34** browser tests. |

After the interruption, the 2026-11-09 continuation checked persisted Surefire reports (30 passing tests) and Playwright's passing last-run artifact. Temporary `/tmp` logs were no longer present. TypeScript, regenerated-contract hash comparison, `python3 scripts/traceability.py` and `git diff --check` were rerun successfully. Backend/browser suites were not rerun solely for this documentation checkpoint; no implementation code changed during the continuation.

PostgreSQL verification covers independent authority, CSRF, missing/stale confirmation, audit-failure rollback, concurrent duplicate replay, changed-body conflict, retained terms/audit, stale publication rejection, public exclusion, immutable discarded rows, revoked access and a discard/publication race. Browser tests create labelled sandbox drafts and recover a lost successful response to one terminal revision, preserving terms and removing mutation actions. Other foundation and catalogue journeys remain covered by the passing full suite.

V13 applied to the existing local database during the implementation run. Full restore drills, manual screen-reader/low-bandwidth testing, production role governance, retention/notifications, dependency/secret scans and remote CI remain unverified. This is not production readiness.

## Run and next step

Follow [README draft discard](../README.md#discard-an-unpublished-draft-without-deleting-evidence) and [ADR 0011](architecture/adrs/0011-preserved-draft-discard.md). The local operator must explicitly include `--allow-draft-discard`. API/web are currently stopped; use the established README startup commands. Retry uncertain outcomes with **Retry same discard**. A new proposal must use the latest published source and a distinct effective instant; discarded evidence is not restored or overwritten.

Next: policy-approved handling of published-offering availability and broader commercial configuration, while keeping storage, general entitlements/quotas and remaining Phase 1 prerequisites visible. Price/tax/feature/quota editing, new plan families, add-ons, subscriber migration and production approval controls remain Pending. Full-source rental, inventory, finance, dashboards, helpdesk/trust, signing/exit, marketplace/membership, SaaS/AI and ARCH/INT deployment gaps remain open. No full requirement or phase is declared complete.

---

## Historical report: editorial draft edits

# Editorial draft-edit continuation — 2026-11-08

## Delivered journey and status

Explicitly authorized editor → open an unpublished source-backed draft → compare persisted terms → confirm proposed text/date changes and reason → save a new revision → recover an uncertain response without another update. Earlier publication reviews are rejected after an edit. **SAAS-ADMIN-002 / FS-06** and **SAAS-PLAN-003 / FS-03** remain **Partial**; only editorial fields are editable.

## Changes and safeguards

- **V12** adds independent, false-by-default `can_edit_draft` authority and grant audit, plus actor/key edit journals containing exact requests, before/after projections, results and reasons. Runtime gains updates only on name, disclosure and effective date; published protection remains.
- PATCH `/api/v1/admin/plan-versions/{id}`, `PlanDraftEdits`, **OpenAPI 0.10.0** and generated TypeScript. Current edit authority, CSRF, strong ETag, valid future instant, unique effective date and source-backed draft state are required.
- Actor and audience/code locks serialize edits with authority changes and publication. The database revision trigger invalidates earlier reviews. Concurrent editors produce one success and one 412; no last-write-wins overwrite occurs.
- Private review routes expose **Edit draft** separately from creation/publication. The shared form preloads current terms, offers persisted-term comparison, focuses the heading, confirms proposed fields and warns about invalidated reviews. Unknown-response retry preserves the exact key/body. Save messages describe recorded commands and refresh current state.
- Source provenance, prices, limits, classification, capabilities, published versions and existing subscriptions are unchanged by editing. Audit failure rolls back both terms and revision. Before/after and command evidence have no runtime update/delete privilege.

## Actual verification

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **29 tests**, no failures/errors/skips; executable jar. |
| `npm run generate`, generated-file SHA comparison, `npm run typecheck`, `npm run build` in `web` | PASS: no contract drift, strict types and production build. |
| `npm run test:e2e -- tests/plan-review.spec.ts --workers=2` | PASS: **8** desktop/mobile review/draft/publication/edit journeys. |
| `npm run test:e2e -- --workers=2` | PASS: **32** browser tests on the final build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all functional IDs, aliases, FS contracts and ARCH/INT gates preserved. |

`DraftEditIntegrationTest` covers independent authority, CSRF, missing precondition, date validation, audit-trigger failure rollback, replay/changed-body conflict, before/after evidence, stale publication review, concurrent editors, inherited money/capacity, public exclusion, published-edit rejection and revoked access. Browser tests edit real labelled sandbox drafts and recover a deliberately lost committed response to exactly one revision, including keyboard focus. Existing accessibility/private-header and foundation journeys remain in the passing suite.

V12 applied successfully to the existing local database on API restart. This is not a restore drill or a production-readiness claim. Manual screen-reader/low-bandwidth testing, production role governance, retention/notifications, dependency/secret scans and remote CI remain unverified. No new provider or financial operation was introduced.

## Run and next step

See [README draft editing](../README.md#edit-an-unpublished-editorial-draft) and [ADR 0010](architecture/adrs/0010-editorial-draft-edits.md). A trusted sandbox operator must include `--allow-draft-editing`; existing create/publish grants do not acquire edit authority automatically. API/web are running locally. On a stale result, close the old proposal, refresh and review current terms; do not overwrite another editor's work.

Next: evidence-preserving draft discard, followed by policy-approved deactivation and broader commercial configuration. Price/tax/feature/quota editing, new plan families, add-ons, subscriber migration and production approval controls remain Pending. Other Phase 1 and full-source rental, inventory, finance, dashboard, helpdesk/trust, signing/exit, marketplace/membership, SaaS/AI and ARCH/INT deployment gaps remain open. No full requirement or phase is declared complete.

---

## Historical report: reviewed editorial publication

# Reviewed editorial publication continuation — 2026-11-08

## Delivered journey and status

Separately authorized publisher → review source versus exact draft → confirm reason, unchanged terms and future effective instant → publish once → recover a lost response → public catalogue selects the new version at its effective instant. Existing subscribers retain their original contract references. **SAAS-ADMIN-002 / FS-06** and **SAAS-PLAN-003 / FS-03** remain **Partial**; the delivered subset is unchanged editorial publication.

## Changes and integrity

- **V11** adds publication authority (false by default), database-maintained revisions, expiring actor-bound review records and publication command journals. Published update/delete protection remains; runtime may update only publication state.
- GET `/admin/plan-versions/{id}/activation-preview` and POST `/admin/plan-versions/{id}/activate`; **OpenAPI 0.9.0** and generated client types. Separate authority, CSRF, exact ETag, reason, scoped idempotency and an unexpired review are required.
- `PlanPublication` validates source provenance, unchanged money/capacity/classification, future effective time and latest-published-source ordering. Competing publication invalidates another draft's approval. Row revisions invalidate changed draft reviews.
- The commercial module's `EditorialPlanCapabilities` copies source mappings in the publication transaction. State, capability mappings, command result and audit commit together. No historical subscription is updated and no payment/provider effect occurs.
- Private review routes compare source/proposal, show INR and Asia/Kolkata dates, focus newly opened forms and preserve exact confirmations for uncertain-result retry. Publication success states its effective instant; it does not claim immediate paid activation. Existing no-store public queries switch versions without a scheduled external job.

## Actual verification

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **28 tests**, zero failures/errors/skips; executable jar. |
| `npm run generate`, `npm run typecheck`, `npm run build` in `web` | PASS: generated contracts, strict types, final production build. |
| `npm run test:e2e -- --workers=2` | PASS: **30** desktop/mobile tests before the final focus improvement. |
| `npm run test:e2e -- tests/plan-review.spec.ts --workers=2` | PASS: **6** review/draft/publication tests on the final build, including initial review focus. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: all 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained. |

PostgreSQL tests verify actor-bound/expired/stale reviews, missing confirmation, CSRF, independent authority, competing publications, changed-body conflict, duplicate replay after review expiry, revocation and immutable published terms. An injected audit-write failure rolls back publication, capability inheritance and result evidence before a safe retry. The test crosses the real effective-date boundary, verifies new-workspace capabilities, and confirms an existing workspace retains its old plan version.

Browser tests create clearly labelled, future-effective local sandbox versions, lose the successful publication response and retry the same confirmation. They preserve currently effective public offerings; these are not live paid offers. Keyboard, axe and mobile layout checks pass; the focused mobile publication screenshot was visually inspected. V11 applied successfully to the existing local database. Manual screen-reader, full restore drills, production role governance, dependency/secret scans and remote CI remain unverified.

## Run and next step

Use [README reviewed publication](../README.md#reviewed-editorial-publication) and [ADR 0009](architecture/adrs/0009-reviewed-editorial-publication.md). The sandbox operator must explicitly supply `--allow-publication`; draft creation alone does not authorize it. Local API/web are running. Correct published editorial terms with a new version; this slice has no rollback/deactivation action that rewrites evidence.

Next: complete existing-draft editing/discard and its approval invalidation, then source-approved financial/feature/quota configuration and publication policy. Paid checkout, add-ons, deactivation, subscriber migrations, production approval/MFA and retention/notifications remain Pending. Other Phase 1 gaps and the full rental, inventory, finance, dashboard, helpdesk/trust, signing/exit, marketplace/membership, SaaS/AI and ARCH/INT deployment backlog remain unchanged. No full requirement or phase is declared complete.

---

## Historical report: unpublished editorial drafts

# Unpublished editorial draft continuation — 2026-11-08

## Delivered journey and requirement status

Explicit draft-authority holder → review a published source → propose name, disclosure and future effective instant → confirm inherited terms and reason → save an unpublished version → recover a lost response with the same command key. **SAAS-ADMIN-002 / FS-06** and **SAAS-PLAN-003 / FS-03** remain **Partial**. This is editorial draft creation, not full configuration editing or publication.

## Changed surfaces and invariants

- `/admin/plans` and `/admin/consumer-plans` expose draft creation only when the backend returns explicit write eligibility. React Hook Form/Zod validates input; confirmation binds source ETag, exact proposed fields and reason. Unknown-response retries freeze the original body/key; no sensitive draft is persisted in browser storage.
- POST `/api/v1/admin/plans/{id}/versions`, **OpenAPI 0.8.0**, regenerated TypeScript. `id` selects the immutable published source version in the current catalogue. The server inherits audience/code, prices, currency, allowances and baseline classification; UI input cannot establish those terms.
- **V10** adds false-by-default draft authority and audited grant flags, durable actor/key command evidence and a runtime draft-only insert guard. `PlanDrafts` commits the draft, command record and audit atomically. Existing migrations remain unchanged.
- Actor locks coordinate authority changes and command replay; audience/code locks allocate distinct version numbers across editors. The existing effective-date uniqueness constraint remains authoritative. Missing/stale source confirmation, duplicate effective dates, invalid inputs and changed-key bodies retain distinct HTTP outcomes.
- Public catalogue and subscriptions are not written by this operation. Draft insertion cannot publish an offering; runtime inserts with published state are rejected. No external provider effect or financial transaction was introduced.

## Actual verification

| Command | Result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **27 tests**, zero failures/errors/skips; executable jar. |
| `npm run generate`, `npm run typecheck`, `npm run build` in `web` | PASS: generated types, strict TypeScript and final production build. |
| `npm run test:e2e -- tests/plan-review.spec.ts --workers=2` | PASS: **4** desktop/mobile review/draft journeys. |
| `npm run test:e2e -- --workers=2` | PASS: **28** browser tests on the final build. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: 195 IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates retained. |

PostgreSQL tests verify missing write authority, CSRF, 428/412 preconditions, concurrent duplicate replay, changed-body conflict, effective-date conflicts, past-date rejection, concurrent editors, inherited money/capacity, audit, revoked access and runtime publication denial. Browser tests create real labelled sandbox drafts, abort the response after a successful backend commit, and retry to recover exactly one version. These drafts remain unpublished. The final suite also covers existing public catalogue, identity, membership, usage and operation-recovery journeys.

V10 was applied to the existing local database on API restart. Full restore drills, manual screen-reader/low-bandwidth evaluation, production authority approval, dependency/secret scans and remote CI were not executed for this slice. No production readiness claim is made.

## Run and next step

Follow [README unpublished editorial drafts](../README.md#unpublished-editorial-drafts). A trusted local operator must explicitly add `--allow-draft-creation` to the sandbox grant command; ordinary reviewers cannot create drafts. API/web are running locally. [ADR 0008](architecture/adrs/0008-unpublished-plan-drafts.md), the matrix and decision register record scope and recovery.

Next: complete the draft lifecycle and reviewed publication, with exact-version confirmations, approved commercial configuration and immutable-contract transition tests. Prices/taxes, features/quotas, add-ons, editing/discarding existing drafts, activation/deactivation and subscriber migration remain Pending. Other Phase 1 storage/identity/grant/quota gaps and the full-source rental, finance, inventory, dashboard, helpdesk/trust, signing/exit, marketplace, membership/SaaS, AI and ARCH/INT deployment work remain open.

---

## Historical report: commercial plan review

# Commercial plan review continuation — 2026-11-08

## Delivered journey and status

Explicitly granted commercial reviewer → `/admin/plans` or `/admin/consumer-plans` → inspect real draft/published/historical versions and terms → follow bounded pages → lose access after grant revocation or expiry. Workspace owners are denied unless separately granted review authority.

**SAAS-ADMIN-002 / FS-06** is **Partial** for this Phase 1 prerequisite. **FS-03 / SAAS-PLAN-003** remain Partial. This is read-only review; creation, draft editing, activation/deactivation, feature/add-on configuration and subscriber migrations remain Pending. No full plan-administration or Phase 1 completion is claimed.

## Changes and evidence

- Additive **V9** persists explicit account grants, expiry/revocation, reasons and grant-change audit. Runtime access to grants is SELECT-only; published terms remain immutable. No grant is seeded. The sandbox-only operator script issues/revokes bounded grants for existing local accounts.
- New catalogue-owned `PlanReview` and GET `/api/v1/admin/plans`; **OpenAPI 0.7.0**, generated TypeScript. Lists are audience-scoped, UUID-paginated at 25 rows and audited in the same transaction as the projection. Every page rechecks current authority. Public `CatalogController` and publication filtering remain unchanged.
- Private `/admin/plans` and `/admin/consumer-plans` use no-store/noindex, keyboard disclosures, empty/error/forbidden/retry states, access-expiry clearing and focus/refresh reauthorization. No tenant, subscriber or police records are exposed.

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: **26 tests**, no failures/errors/skips; executable jar. |
| `npm run generate`, `npm run typecheck`, `npm run build` in `web` | PASS: generated contracts, strict TypeScript, production build. |
| `npm run test:e2e -- tests/plan-review.spec.ts --workers=2` | PASS: **2** new desktop/mobile journeys. |
| `npm run test:e2e -- --workers=2` | PASS: **26** full browser tests. |
| `python3 scripts/traceability.py`, `git diff --check` | PASS: original IDs/aliases/gates retained; no whitespace errors. |

PostgreSQL tests cover owner denial, grant expiry/revocation, read audit, grant-change audit, runtime write denial, pagination/reauthorization and private draft exclusion. Browser tests grant/revoke only their own sandbox accounts and check the generated schema, keyboard, axe, overflow and private headers. The mobile screenshot was visually inspected. Initial browser tests used an ambiguous alert locator; scoping to the page error resolved the test issue. Local startup applied V9 to the existing database; this does not constitute a backup/restore drill.

## Limitations and run instructions

Use [README local commercial review](../README.md#local-commercial-plan-review) and [ADR 0007](architecture/adrs/0007-commercial-plan-review.md). API/web are running locally for verification. No financial operation or new provider adapter was introduced. Production administrator verification/MFA, approved role governance, grant notifications/retention, restore drills, manual screen-reader testing, backend dependency/secret scans and remote CI remain unverified. Revocation applies to subsequent requests; previously viewed data cannot be recalled.

Next concrete step: authorized draft editing and exact-version publication review with immutable contract preservation, scoped idempotency and concurrent activation tests. General grants/quotas, private storage and the remaining Phase 1 scope stay open. Full-source gap tracking for rental, inventory, dashboard, finance, helpdesk, trust, signing/exit, marketplace, membership, SaaS, AI and ARCH/INT deployment work remains unchanged.

---

## Historical report: public catalogue audiences

# Catalogue audience continuation — 2026-11-08

## Delivered scope

`/pricing` now lets visitors select **Workspace plans** or **Personal memberships**, preserving the audience and monthly/annual interval in navigation and refresh URLs. The typed server helper reads the existing public API with the selected audience. Personal membership has separate wording and no workspace capacity or provisioning action. The current consumer catalogue is genuinely empty: no personal prices, benefits or records were fabricated.

This extends the **SAAS-PLAN-001 / FS-03 public catalogue subset**, still **Partial**. Personal membership purchase, account entitlements and `/tenant/membership` remain pending. This does not advance paid membership implementation or complete Phase 1.

## Changes and verification

- Changed `/pricing`, its metadata and typed catalogue helper. Existing OpenAPI audience/interval contracts, `CatalogController`, persistence and publication audit remain unchanged; no migration or dependency change was needed.
- Added a real PostgreSQL regression using the same plan code in both audiences: list/detail isolate the terms, annual pricing stays a decimal string and workspace-only plans return 404 for consumer requests. The test offering exists only in the isolated Testcontainers database.
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify`: **PASS, 24 tests**, no failures/errors/skips.
- `npm run typecheck` and `npm run build` in `web`: **PASS**.
- `npm run test:e2e -- tests/catalog.spec.ts --workers=2` in `web`: **PASS, 6 desktop/mobile tests**, real API empty state, audience/interval navigation and refresh, keyboard, axe and overflow checks. An initial new test selected the global header link; it now explicitly selects the audience navigation.
- `python3 scripts/traceability.py` and `git diff --check`: **PASS**. The entire browser suite, manual screen reader, outage/restore, dependency/secret scans and remote CI were not rerun for this read-only change.

## Run and remaining work

Follow [README setup](../README.md), open `/pricing?audience=consumer&interval=annual`, and switch audiences using the labelled navigation. Local API/web processes were started for verification. The public read adds no financial operation, authorization grant or recovery command; published-version immutability and existing retry/error states remain in place.

Next Phase 1 work remains explicit commercial-role administration and reviewed plan version publication, effective-grant layers, quota reservation recovery and private storage. Prior full-source gap tracking remains unchanged: properties/inventory, rental lifecycle, discovery/marketplace aliases, finance/helpdesk/trust/exit, memberships, SaaS, advertising/AI, dashboards and ARCH/INT deployment gates remain incomplete. See the [requirements matrix](requirements-matrix.md).

---

## Historical report: durable welcome operations

# Durable welcome-operation continuation — 2026-11-08

## Delivered scope

Workspace owner → **Background operations** → inspect durable delivery and attempt history → review an exact-version retry or cancellation with a reason → recover an uncertain response without duplicating delivery. This advances the **SAAS-REG-003 / FS-02 foundation**, with the applicable **FS-32** recovery contracts, **ARCH-08** and **INT-03** remaining **Partial**. General imports, provider jobs and Phase 1 are not complete.

## Changes and safeguards

- `/workspace/operations?workspace=…`, linked from setup, uses the real owner-authorized API. OpenAPI **0.6.0** and generated TypeScript describe workspace operation lists, detail, retry and cancellation.
- `WelcomeOperations` and the existing welcome worker persist claims, bounded retries, expiring leases, attempts, cancellation and audit evidence. **V8** extends the existing outbox and preserves prior messages; historical attempts are not fabricated.
- Claims use PostgreSQL locking; expired/stale workers cannot commit. Delivery and its unique notification commit together. Revoked creator authority blocks delivery without starving later work. Five failed attempts exhaust a cycle; authorized manual retry preserves history.
- Recovery requires CSRF, current owner authority, exact ETag, scoped idempotency and a reason. Missing and inaccessible operation IDs return the same privacy-safe code. Retry acceptance is not delivery success; cancellation cannot recall a delivered message.
- UI includes bounded polling, attempt/status guidance, keyboard confirmation, safe in-memory input, conflict recovery and same-key retry after an unknown response. Only an in-app welcome message is delivered; no live provider behavior is claimed.

## Verification evidence

The preceding implementation run passed **23 backend tests**, **22 desktop/mobile browser tests**, the production build, generated-contract drift check and database restart recovery. Two targeted operation browser tests passed after the final UI wording changes. Browser failure recovery uses a labelled test-only failed-state fixture; backend tests separately inject actual PostgreSQL write failures. Restart recovery checks stable operation identity and exactly one welcome notification.

Final continuation checks after normalizing inaccessible-operation responses:

| Command | Actual result |
|---|---|
| `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./backend/mvnw -f backend/pom.xml -B -ntp verify` | PASS: 23 tests, zero failures/errors/skips; executable jar. |
| `npm run typecheck` in `web` | PASS. |
| `python3 scripts/traceability.py` | PASS: all IDs, contracts, aliases and gates retained. |
| `git diff --check` | PASS. |

The first verification attempts could not initialize PostgreSQL because Docker was stopped; restarting the local runtime resolved the prerequisite. Browser/build/restart results above belong to the preceding implementation run; they were not rerun for this final backend privacy-response change. Local API/web servers are not currently running.

Traceability retains all **195 functional IDs, 36 FS contracts, 257 aliases, 14 ARCH and 12 INT gates**. No lint script exists. Manual screen-reader testing, production recovery targets, remote CI, dependency/secret scanning and full monitoring/alert ownership remain unverified. No financial workflow was changed.

## Run and resume

Follow [README setup](../README.md), then open workspace setup → **Background operations**. Review [ADR 0006 and its recovery runbook](architecture/adrs/0006-durable-welcome-operations.md), the [matrix](requirements-matrix.md) and [decision register](decision-register.md).

Next Phase 1 work remains authorized entitlement version administration/effective grants, general quota reservation and expiry recovery, private storage and remaining identity/platform administration. Full-source gaps remain visible across property/inventory, discovery/tours/appointments and marketplace aliases, signing/possession/active tenancy, rental finance, helpdesk/privacy/reviews/police/exit, paid workspace/personal memberships, sale/services, advertising/AI, dashboards and architecture/integration deployment gates. Welcome delivery does not establish these workflows.

---

## Historical report: entitlement and usage

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
