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
