# RentPay / LMT

LMT (Landlord, Manager, Tenant) is being built in the existing **RentPay** repository. The current delivery includes a PostgreSQL-backed catalogue and a **local sandbox identity → session → Free workspace provisioning → persisted setup** journey, plus profile-name editing, active browser-session revocation and sandbox manager invitations. It is not a completed rental SaaS or a live SMS/email integration.

For the full Docker stack, run `./scripts/compose-local.sh up -d --build --wait`, then open http://localhost:3100/login. Email codes arrive in [Mailpit](http://localhost:18025); mobile codes arrive in the [SMS test inbox](http://localhost:8026). These are local test deliveries, with no external email or carrier SMS. See [local inbox setup](infra/compose/LOCAL-INBOXES.md). Native development below retains the private file inbox.

Start at `/login` (or `/register`). Choose Mobile or Email and verify a test contact using the private local inbox, choose/create a Free workspace, and revisit its setup through `/account`. Workspace membership, subscription binding, allowances, audit, retry records and in-app welcome delivery are durable. Context is explicit per tab, and unauthorized workspaces return privacy-safe 404. `/pricing` retains public versioned plan comparison; unknown paid prices remain unknown.

Live SMS/email, rental properties/units, tenancy operations, payments and personal memberships remain unavailable. AUTH-001 and SAAS-REG-001–003 are **Partial**, because their complete source requirements extend beyond this slice.

## Run locally

Requires Java 21, Node version in `.node-version`, npm and Docker Compose. Local credentials in Compose/scripts are development-only. Database, API and web bind to loopback; do not deploy this configuration as production.

From repository root:

```sh
docker compose up -d --wait db
./scripts/run-api-local.sh
```

In a second terminal:

```sh
cd web
npm ci
npm run generate
npm run build
npm run start
```

Open http://localhost:3000/login (catalogue: http://localhost:3000/pricing). Use a test name and an Indian-format test mobile number. The verification screen shows a challenge UUID. Retrieve its local code explicitly from the repository root:

```sh
python3 scripts/read-sandbox-code.py CHALLENGE_UUID
```

The script reads only the local owner-only inbox; no code is sent by SMS, returned from an API or printed to application logs. Codes expire after five minutes. `.local/` contains the private sandbox secret/inbox and is ignored by Git. `run-api-local.sh` creates it with restrictive permissions and activates only the `local` profile. Do not reuse test verification as proof of a real person's phone.

 API: http://127.0.0.1:8080/api/v1/public/plans. Next.js rewrites browser `/api/v1/*` to the API. For a different internal API address set server-only `API_INTERNAL_ORIGIN` during build and execution. Private browser requests use same-origin cookies and session-bound CSRF tokens. The public server-side catalogue fetch forwards no identity headers and never accesses PostgreSQL directly.

The database volume survives restarts. Flyway applies versioned migrations; the runtime database role has SELECT-only catalogue access plus narrowly granted session/identity/workspace writes and row policies on workspace data. Stop local processes with Ctrl-C and run `docker compose stop` to retain data. Do not remove the volume to resolve a migration failure; inspect and add a forward migration.

## Verify

```sh
python3 scripts/traceability.py
cd backend
./mvnw -B -ntp verify
```

Run Maven with Java 21 (`JAVA_HOME=$(/usr/libexec/java_home -v 21)` on macOS if needed). Integration tests require Docker and use isolated PostgreSQL containers, not the local app database.

```sh
cd web
npm ci
npm run generate
npm run typecheck
npm run build
npx playwright install chromium
npm run test:e2e
node tests/recovery.mjs
node tests/foundation-recovery.mjs
```

E2E tests require the running API, database and built web application. They exercise real persisted data through the same-origin API and page. SMS/email delivery is simulated through the private filesystem inbox; test workspaces are labelled sandbox. The recovery scripts temporarily stop/restart only the local Compose database and leave its volume intact. Browser failure-recovery checks are documented in the release report. Automated accessibility scans do not replace manual screen-reader verification.

## Status and sources

- [Delivery and actual validation](docs/release-status.md)
- [Requirements matrix: all 195 IDs](docs/requirements-matrix.md)
- [Decisions and external dependencies](docs/decision-register.md)
- [Architecture baseline](docs/architecture.md), [initial implementation ADR](docs/architecture/adrs/0001-initial-catalogue-slice.md)
- [Canonical implemented API](contracts/openapi/lmt-v1.json)
- [Full source, unchanged](lmt-full-stack-prompt.md), [frontend FRS](docs/LMT-Frontend-FRS.md), [backend FRS](docs/LMT-Backend-FRS.md)

Phase 0 and Phase 1 remain incomplete. Next: effective entitlements and broader quota reservation/consumption, and storage/general job foundations before proceeding to property/unit creation. Live mobile verification requires an approved provider and policy. No rental, signing, financial, consumer, official-provider, AI or production-readiness claim is made.

### Account controls

After sandbox sign-in, open `/account/profile` to change the display name or `/account/security` to inspect and revoke other browser sessions. Remote revocation requires a recent sign-in (five minutes by default); Sign out ends the current session. Profile edits and revocations produce durable in-app security notices; email/SMS notifications are not configured. A stale profile edit requires explicit reload/review; an uncertain outcome can retry the same command safely.

V4 preserves records but requires sessions created before this migration to sign in again. Browser labels are reported hints, not verified devices. See [ADR 0003](docs/architecture/adrs/0003-account-profile-and-session-controls.md) and [release evidence](docs/release-status.md).

### Workspace invitations

From workspace setup, open **Members and invitations** (`/workspace/settings/members?workspace=…`). Invite a test mobile contact and confirm membership-only scope. In a separate browser, sign in with that contact and open `/account/invitations` to accept or decline. Accepted managers can select `/workspace?workspace=…`; owner administration remains private. The owner can revoke membership and release its manager slot.

Pending invitations do not hold capacity in this sandbox policy; acceptance atomically checks the configured allowance. Owners are excluded from manager usage. Invitations expire after 168 hours by default (`lmt.workspace.invitation-hours`, range 1–720). No SMS/email is sent. Live contact proof, approved counting/expiry policy and property delegation remain pending. V5 preserves existing account/workspace/session data. See [ADR 0004](docs/architecture/adrs/0004-sandbox-workspace-membership.md).

For bounded local browser verification: `npm run test:e2e -- --workers=1` in `web`. The extended `node tests/foundation-recovery.mjs` checks persisted invitation replay after a real database restart.

### Usage and eligibility

Workspace owners can open `/workspace/billing/usage?workspace=…` from setup or member administration. It shows current manager capacity, reconciliation, versioned membership eligibility and manager commit/release history. Date filters apply only to history and use inclusive Asia/Kolkata calendar dates; capacity does not reset monthly. Rentable-item usage is explicitly unavailable until inventory measurement exists.

V6/V7 preserve previous migrations and backfill manager events from actual allocation timestamps. The restart test now verifies accepted manager usage. Full add-ons/overrides/time-limited grants, paid billing and inventory quotas remain pending; see [ADR 0005](docs/architecture/adrs/0005-membership-entitlements-and-usage.md).

### Background operations and recovery

Open workspace setup → **Background operations** (`/workspace/operations?workspace=…`) to inspect welcome delivery and its recorded attempts. The current handler saves an in-app workspace message only. Pending/running jobs refresh for a bounded period; failed or blocked jobs expose reviewed retry/cancellation with an explanation and exact version.

Automatic retries stop after five attempts per cycle. Leases default to 30 seconds (`lmt.workspace.lease-seconds`, 1–300); expired claims can be recovered after process/DB interruption. Retry requires the original creator's current owner authority. Completed messages cannot be recalled. V8 preserves previous events/messages; older completed jobs may have no recorded attempt history. See [ADR 0006 and recovery runbook](docs/architecture/adrs/0006-durable-welcome-operations.md).

The operations browser test deliberately injects a failed-state fixture into its own local sandbox workspace. `OperationsIntegrationTest` separately verifies real database delivery failures and lease recovery. Neither is a simulated live provider success.

### Public catalogue audiences

Use `/pricing?audience=consumer&interval=annual` to inspect personal offerings separately from workspace plans. Audience and interval links preserve each other. The current personal catalogue has no published offerings and shows an explicit empty state; no personal purchase or benefits are enabled. Both audiences use the public catalogue API and its effective published versions.

### Local commercial plan review

After signing in with a sandbox account, obtain its `account.id` from the authenticated `/api/v1/auth/session` response. A trusted local operator can grant one hour of **read-only** review:

```sh
python3 scripts/grant-local-commercial-review.py ACCOUNT_UUID --reason 'Review local catalogue definitions'
```

Open `/admin/plans` or `/admin/consumer-plans`. Workspace owners have no automatic access. Revoke with the same command plus `--revoke`; `--hours` accepts 1–24. The script targets only the local Compose database and requires an existing sandbox account. It records operator attribution/reason. Review alone never enables draft creation or publication. See [ADR 0007](docs/architecture/adrs/0007-commercial-plan-review.md) for authority, audit and remaining production prerequisites.

### Unpublished editorial drafts

A local operator can add `--allow-draft-creation` to the commercial grant command to explicitly enable draft creation. Open `/admin/plans`, choose a published source and review changes to its name, disclosure and future effective instant. Prices and limits are inherited; no offer or subscription changes. Confirm to save an unpublished version. If the response is interrupted, use **Retry same draft**. Do not submit a new proposal while the result is uncertain. Publication requires a separate review and authority; price/feature/quota editing remains unavailable. See [ADR 0008](docs/architecture/adrs/0008-unpublished-plan-drafts.md).

### Reviewed editorial publication

Grant publication separately with `--allow-publication` on the local commercial grant script (include `--allow-draft-creation` too only when needed). In `/admin/plans`, choose **Review publication** on a source-backed editorial draft. Compare the source/proposal, inherited terms and future effective date, supply a reason, then confirm. Reviews expire after five minutes. Retry an uncertain result with **Retry same publication**; do not issue a new command while its outcome is unknown.

Publication preserves existing subscription versions. Public pricing selects the new version only when its effective instant arrives and the offering is available. A competing publication or changed draft requires a fresh review/new draft. Publication does not enable paid checkout or financial/feature edits. Tests retain clearly labelled, future-effective sandbox versions; they are not live commercial offers. See [ADR 0009](docs/architecture/adrs/0009-reviewed-editorial-publication.md).

### Edit an unpublished editorial draft

A sandbox operator must explicitly include `--allow-draft-editing` on the commercial grant command. In `/admin/plans` or `/admin/consumer-plans`, choose **Edit draft**, compare its persisted terms, then confirm the proposed name/disclosure/effective-date changes and reason. Every save invalidates earlier publication reviews. On a stale-version error, close the proposal and refresh before reviewing current terms. After a connection failure, use **Retry same draft** with the preserved confirmation. Prices/limits remain unchanged; published versions cannot be edited. See [ADR 0010](docs/architecture/adrs/0010-editorial-draft-edits.md).

### Discard an unpublished draft without deleting evidence

A sandbox operator must explicitly include `--allow-draft-discard` on the commercial grant command. Choose **Discard draft** in the private plan review, read the terminal-state warning, enter a reason and confirm. The row remains visible with its terms and audit evidence, but cannot be edited or published. This is not deactivation of a public offer. If the result is uncertain, use **Retry same discard**. A new proposal must use a distinct effective instant because retained versions keep their existing uniqueness constraints. See [ADR 0011](docs/architecture/adrs/0011-preserved-draft-discard.md).

### Withdraw or restore a published offering

Include `--allow-offering-management` in the local commercial grant command. In `/admin/plans` or `/admin/consumer-plans`, open **Offering availability** on a published version. Review the effect across all current and future versions of that audience/code, enter a reason and confirm. Read-only reviewers can inspect state. Workspace Free is protected so onboarding remains available. Existing subscriptions and entitlements are preserved; restoration does not enable paid checkout.

After an uncertain response use **Retry same availability change**; after a known conflict refresh and review current state. Publication previews disclose withdrawal, and availability changes invalidate earlier publication reviews. See [ADR 0012](docs/architecture/adrs/0012-offering-availability.md). Grant flags are explicit: include every authority needed when renewing a grant.

Run the browser suite with its default single worker (`npm run test:e2e`): availability tests temporarily change shared sandbox offerings and restore their changes. Do not run simultaneous suites against the same local database.

### Private upload staging (sandbox only)

After local sign-in, choose **Private upload staging** from `/account` or visit `/account/uploads`. Select a test PDF, PNG or JPEG up to 1 MiB, review the filename/size/type and confirm. The Java API creates a 15-minute account-owned intent, receives the actual file, validates its length/fingerprint/header and persists quarantine status. Twenty new intents/account/hour is a local safety limit.

**Scanning is unavailable. No downloads, sharing or clean/verified document state are enabled.** This staging screen is not a tenancy document vault or Premium benefit. Use test files only. Local bytes are private under `.local/quarantine`, outside frontend assets; retain this directory with the database when preserving test data. No automatic deletion or retention guarantee is implemented.

On a lost response choose **Retry same upload**. After reload, refresh status; use **Resume** and reselect the original file for an unexpired awaiting intent. Expired intents require a new upload confirmation. A retained object after a database/audit failure is reconciled by retrying its same intent. Do not manually erase quarantine to repair an operation. See [ADR 0013](docs/architecture/adrs/0013-private-upload-quarantine.md) for scope, recovery and production prerequisites.

### Check or recover a private sandbox file

On `/account/uploads`, choose **Review stored file** for a quarantined upload. **Check stored file** compares its current bytes with the original fingerprint and records an observation time. **Refresh recorded status** reads the latest saved observation; it does not perform another check. An intact result does not clear malware scanning or enable access.

For **Stored file missing**, select the exact original file and confirm restoration. Changed objects cannot be overwritten here: preserve them for operator investigation. If a response is lost, choose **Retry same storage command**; a database failure after restoring bytes can be reconciled with that same command. Quarantine and the original upload receipt remain unchanged. See [ADR 0014](docs/architecture/adrs/0014-private-storage-recovery.md).

### Durable scan preparation

Each quarantined upload now queues one durable preparation operation. Existing quarantined files receive current-time queue records when V18 runs. Choose **Scan preparation** from `/account/uploads` to see status, blocking reason, attempt history and next eligible time. The real local worker verifies storage prerequisites; because no malware scanner is configured, an intact file reaches **blocked / SCANNER_UNAVAILABLE**, not clean or downloadable.

Use a recovery reason to review cancellation or a prerequisite recheck. Cancelling preserves the file and all history. Rechecking is accepted asynchronously (202); it may immediately block again while the scanner remains unavailable. Retry an uncertain response with **Retry same scan command**; refresh after a stale-version error. Automatic retries are bounded to five attempts per cycle. The page polls pending work at most ten times and offers manual refresh.

Defaults are a one-second worker interval and a 30-second lease; the `lmt.documents.scan-*` properties are documented in [ADR 0015](docs/architecture/adrs/0015-durable-scan-preparation.md). These are local execution defaults, not service guarantees. Do not manually clear quarantine or edit job/attempt records to bypass a blocker.

### Scoped temporary entitlement overrides

A trusted local operator can grant a signed-in sandbox account authority over one sandbox workspace:

```sh
python3 scripts/grant-local-entitlement-operator.py ACCOUNT_UUID WORKSPACE_UUID --reason 'Review sandbox manager eligibility'
```

The command prints `/admin/workspaces/WORKSPACE_UUID/member-entitlement`. Grants default to one hour; `--hours` accepts 1–24, and `--revoke` revokes authority. Workspace owners and catalogue administrators have no automatic access.

On that page, propose **Temporarily allow**, **Temporarily deny**, or **Inherit subscribed plan** for manager onboarding. Temporary effects require an expiry in the next 24 hours, entered in Asia/Kolkata, plus a reason and confirmation. Use **Retry same override** after an uncertain response. Refresh/review after a conflict or expiry. Revoking operator authority does not undo an already-issued override; its own expiry or a new authorized change controls its effect.

Owners see the effective source/expiry at `/workspace/billing/usage?workspace=…`; the membership screen explains an onboarding restriction before submission. Overrides never confer roles, enlarge quotas, bypass security restrictions or prevent corrective manager revocation. Administration is unavailable outside sandbox mode. See [ADR 0016](docs/architecture/adrs/0016-scoped-temporary-entitlement-overrides.md).

### Manager capacity evidence

At `/workspace/billing/usage?workspace=<workspace UUID>`, workspace owners can expand **Reconciliation evidence** to inspect current manager/allocation/event counts. Missing event pairs and unexplained persisted reservations make remaining capacity unavailable and block new acceptance with `CAPACITY_RECONCILIATION_REQUIRED`. Use **Refresh usage** to recheck after an authorized investigation. A separately authorized sandbox counter-repair screen is available when source evidence agrees; it never rewrites historical evidence. Pending invitations do not reserve manager slots under the documented sandbox counting policy. See [release status](docs/release-status.md) for actual tests and remaining recovery work.

### Reviewed sandbox manager capacity repair

After established local API/web startup, a trusted local operator can issue one workspace-scoped grant:

```sh
python3 scripts/grant-local-capacity-repair-operator.py <account-uuid> <workspace-uuid> --reason "Investigated sandbox counter drift"
```

Open the printed route while signed in as that workspace's active owner. Review the server-derived correction, provide a reason and confirm. Ownership alone or entitlement-override authority does not qualify. Default authority lasts one hour (`--hours` permits 1–24); `--revoke` removes access. A reconciled workspace needs no repair, and inconsistent source evidence blocks correction. **Retry same repair** recovers an unknown response without repeating the correction. Production mode and non-owner support repair are unavailable. See [ADR 0017](docs/architecture/adrs/0017-reviewed-manager-capacity-repair.md) and [release status](docs/release-status.md) for tests and prerequisites.

### Reviewing manager counter repair history

Active workspace owners can read **Manager counter repair history** on `/workspace/billing/usage?workspace=<workspace-uuid>`. The existing date form filters both capacity events and counter repairs using inclusive Asia/Kolkata dates. **Load older repairs** pages through retained revisions; **Refresh usage** retrieves current authorized history. Reasons and before/after counts remain readable after a repair grant expires, while workspace ownership is still required. This is counter correction history, not rental finance or a new capacity grant.

### Sandbox email sign-in

On `/login`, choose **Email**, enter test details and request a code. Retrieve the challenge code from the existing private local inbox exactly as for mobile OTP. No email is sent and no real mailbox ownership is established. After verification you can provision a Free workspace, use the profile/security controls and return with the same email contact. The domain is case-insensitive; local-part spelling/case is retained. Email and mobile accounts are independent, and mobile invitations require the corresponding mobile account. Do not expect existing mobile workspace access when signing in with a new email. Live delivery, account merging and contact replacement remain unavailable. Mobile accounts can explicitly link an unused email as described below. See [ADR 0018](docs/architecture/adrs/0018-sandbox-email-identity.md).

### Explicit sandbox email linking

Sign in using the mobile account you want to keep. Within five minutes (or the configured reauthentication interval), open `/account/profile` → **Link an unused email for sign-in**. Review the target email, request a separate linking code, retrieve it from the private inbox, then review and confirm the account/email link. A sign-in code cannot replace a linking code. The existing account and workspaces remain intact, and future email sign-in returns to that same account. An in-app security notice records the link.

Use **Retry same linking step** after an unknown response. If the profile changes or the code expires, reload and request a new proof. If recent verification is required, use Verify your current session, then return and start a new linking proof. No real email is sent. This flow cannot replace contacts or merge an email account that already exists; email-only accounts can now add an unused mobile through the same profile route. See [ADR 0019](docs/architecture/adrs/0019-explicit-sandbox-email-link.md).

### Link a mobile to an email-only account

Sign in using the email account to keep, open `/account/profile` → **Link an unused mobile for sign-in**, enter an Indian mobile in `+91` format and request a separate linking code. Review and confirm the contact/account/version after reading the code from the private local inbox. No SMS is sent. Mobile sign-in then returns to the same account and workspaces. Any matching manager invitation becomes available for explicit review in `/account/invitations`; linking alone does not grant membership. Existing mobile accounts cannot be merged or taken over. The same recent-sign-in, expiry, attempts, retry and audit rules apply in both directions.

### Verify an existing session again (sandbox)

Open `/account/security` → **Verify current session**, or follow the recovery link after a recent-verification error. Select an existing contact, request its code, read the private local inbox and submit **Verify and rotate session**. The account/workspaces stay unchanged; the session cookie and CSRF token rotate and a durable security notice is recorded. The original absolute session limit is preserved. Security session dates now show **Last verified**.

If the verification response is lost, use **Check current session** or sign in again; do not replay the consumed proof. Database/audit rollback after servlet rotation can also require sign-in. No SMS/email is delivered. See [ADR 0020](docs/architecture/adrs/0020-bound-session-reauthentication.md).

### Review security notices

At `/account/security`, Security notifications shows the total unread count and each notice's read time. **Mark as read** retains the notice in history. If the response is lost, use **Retry same read update**; after a stale-version response, **Refresh notifications** loads current state. Counts include every page. Read status is not consent or legal acceptance. These are existing in-app security events; no external messaging provider or preference policy is enabled by this feature. See [ADR 0021](docs/architecture/adrs/0021-security-notice-read-state.md).

### End a reviewed set of other sessions

Open `/account/security` → **End selected sessions**. Select up to 25 other sessions, choose **Review selected sign-out**, inspect the exact session IDs/browser hints and confirm. Recent verification is required; use **Verify current session** and then refresh/review again if prompted. Your current session, unselected sessions and new sign-ins after the review remain available. This does not prevent someone with a valid sign-in contact from signing in again.

After an uncertain response, use **Retry same batch sign-out**. A stale selection requires refresh and a new review; the backend never partially ends a selection. No new migration/configuration is required. See [ADR 0022](docs/architecture/adrs/0022-reviewed-session-batch-revocation.md).

### Recover through an existing linked contact (sandbox)

From a signed-out browser, open `/login` → **Recover an existing account and end its sessions**. Enter a mobile/email already linked to the account, request a recovery-specific code, read the private local inbox and explicitly confirm ending existing sessions. Recovery preserves the account and workspaces, rotates into a new session and records a security notice. Earlier sign-in/recovery codes are invalidated. No SMS/email is sent; this is local sandbox proof.

If the result is uncertain, use **Check current session** or ordinary sign-in with a new code. Do not repeat the consumed recovery proof. Someone with continued control of a linked contact can obtain a new code; recovery does not replace contacts. If every linked contact is lost, assisted recovery is not configured. See [ADR 0023](docs/architecture/adrs/0023-linked-contact-account-recovery.md).

After an uncertain recovery response, **Check recovery outcome** reads whether recovery committed, even if the rotated cookie was lost. The private status grant stays only in this page's memory and expires fifteen minutes after requesting the code. Reloading loses the grant; ordinary sign-in with a new code remains available. A completed outcome does not prove that this browser received a usable session. A not-yet-completed result cannot rule out an in-flight request and never authorizes proof replay. See [ADR 0024](docs/architecture/adrs/0024-protected-recovery-outcome.md).

### Invite a manager by email

At `/workspace/settings/members?workspace=…`, choose **Invitation contact channel → Email**, enter the manager's test email, review the contact/scope/expiry and confirm. The recipient signs in with that email or an account explicitly linked to it, opens `/account/invitations` and accepts. Matching alone grants no membership. Manager capacity is checked at acceptance; email and mobile share the same allowance.

Use **Retry same action** after an uncertain creation response. Owners can revoke invitations or accepted membership through the existing controls. This is a durable in-app invitation; no email/SMS is sent. Existing mobile invitations remain supported. See [ADR 0025](docs/architecture/adrs/0025-email-workspace-invitations.md).

## Full-stack Docker Compose (local sandbox)

```sh
./scripts/compose-local.sh build
./scripts/compose-local.sh up -d --wait
```

Open http://localhost:3100/login. This starts frontend, API, worker, PostgreSQL, one-shot migrations and the same-origin reverse proxy. The existing root Compose database workflow remains unchanged. See [service topology, secrets, verification and recovery](infra/compose/README.md).

### Remove a linked sign-in contact (sandbox)

With both email and mobile linked, open `/account/reauthenticate` and verify the contact you will **keep**. Within five minutes open `/account/identity-links/remove`, review the selected contact/version and confirm. Other browser sessions are revoked; the current account and workspaces remain. Retry the same removal after an uncertain response. Your last contact cannot be removed. `node web/tests/contact-removal.mjs` exercises this journey against the full Compose stack on port 3100.

### Account notification inbox

Open `/account/notifications` (also available under `/tenant/notifications` and `/workspace/notifications`). Filter all/unread/read notices; the URL preserves your selection. Marking read preserves history. Counts distinguish matching notices from account-wide unread notices. This currently contains account-security events, not full tenancy/workspace communications. Run `node web/tests/notification-inbox.mjs` against Compose for desktop/mobile verification.

### Enforced notification preference

At `/account/notification-preferences`, opt into personal in-app workspace setup notices. Create a new workspace after enabling to see the worker-delivered notice in `/account/notifications`. Disabling suppresses subsequent personal copies; shared workspace notices, security notices and prior history remain. This controls only the implemented in-app category. Run `node web/tests/notification-preferences.mjs` against Compose for desktop/mobile verification.

### Optional notification quiet hours

At `/account/notification-preferences`, enable quiet hours and choose distinct start/end times in IST (Asia/Kolkata). Overnight intervals work. Personal workspace setup copies wait; shared setup and security notices stay immediate. `/workspace/operations?workspace=…` explains pending delivery and its next check. Changing quiet hours is observed within 60 seconds. Run `node web/tests/quiet-hours.mjs` for desktop/mobile deferral and worker-restart verification (about two minutes).

## SMS/email sign-in integration

Live sign-in adapters are implemented for **Twilio SMS and Resend email**. The default environment still uses the sandbox inbox until real credentials, approved senders and HTTPS are configured. Follow [live identity setup and verification](infra/compose/LIVE-IDENTITY.md). `python3 scripts/prepare-live-identity.py` prepares private local configuration storage without generating provider credentials or sending messages; `scripts/compose-live.sh` refuses incomplete setup. Provider HTTP fixtures verify integration logic, not real carrier/mailbox delivery.


### Pending account notifications

At `/account/notifications`, enabled workspace-setup notices awaiting delivery are shown separately from delivered history. Quiet-hour deferrals link to preferences, and failed deliveries link to authorized workspace recovery. Enable optional setup notices before creating a workspace to exercise the flow. Refresh the list after policy changes or delivery; no code or job state is fabricated. See [ADR 0032](docs/architecture/adrs/0032-personal-pending-notification-deliveries.md).

Manager invitation acceptance and membership revocation now produce essential **Workspace access** notices at `/account/notifications`, even when optional setup notices are off. The recipient can review retained history after revocation, while workspace access remains denied. This flow is in-app only; external invitation/notification delivery remains separately gated.

The original inviting owner also receives an **Invitation outcome** notice when a manager invitation is accepted or declined, provided owner authority is still active. These in-app notices omit invitee contact details and are deduplicated on retry. Check `/account/notifications`; expiry reminders and external invitation delivery remain incomplete.

Manager invitations are marked expired by the worker after their configured deadline. The original owner receives an in-app expiry notice only while still authorized. Screens explain how to request/send a new invitation; expiry does not allocate or release a manager slot. The worker reconciles up to 25 records per pass every 30 seconds after a 60-second startup delay. Inspect worker logs for persistent failures; database recovery/restart resumes due processing.

Owners can select **Review replacement** on an expired invitation, confirm its masked stored contact and issue a new invitation. The original remains expired; lost responses can retry the same confirmation without duplicates. The recipient must explicitly accept the replacement, with capacity checked then.


### Inspect invitation expiry progress

As an owner, open Members and invitations → **Invitation expiry processing**. Open, overdue-awaiting and recorded-expired counts cover the entire workspace history, not only the loaded page. Refresh after a worker pass. A backlog already prevents acceptance; it does not mean manager capacity was consumed.

If overdue records persist, the deployment operator can inspect `./scripts/compose-local.sh logs --tail=100 worker` and `./scripts/compose-local.sh ps`. Check database availability and restore the worker with `./scripts/compose-local.sh up -d --wait worker` after resolving the cause, then refresh the owner view. Do not delete invitation/history records or reset databases. Container health alone does not prove job completion, and recorded expiry does not prove notification receipt.

## Property setup (local sandbox)

Start/update the existing stack with `./scripts/compose-local.sh up -d --build --wait`. Sign in at http://localhost:3100/login using a test contact and the existing Mailpit/SMS test inbox. Create/select a Free workspace, then open **Properties**. The register creates real drafts; open a property to edit its address/house rules, add a building/floor/whole unit, review readiness, or archive/restore unused records. Unit headings open canonical unit detail pages. State changes require a reason and reviewed confirmation. Proposed INR amounts are configuration, not rent invoices.

Whole-unit drafts consume one rentable slot; archive releases it and restore consumes again. Buildings/floors do not consume slots. The Usage page reconciles recorded allocations/events to the grant. Shared units/beds are gated on the unresolved counting-policy decision. Property activation, delegated managers/co-owner authority, amenities/assets, private unit media and tenancy/finance-linked availability/closure remain incomplete; see `docs/requirements-matrix.md` and ADR 0038. Do not treat setup-ready as vacant, published or occupied.

Verify with `node web/tests/property-management.mjs` against the running stack and the Java `PropertyIntegrationTest` under the established Testcontainers settings. The browser test creates isolated test data, checks retry/persistence/usage and runs mobile reflow/axe checks. It does not send external carrier SMS or email.

Recovery: after an uncertain network result use **Retry same property action**. For stale versions refresh and review again. For `QUOTA_EXCEEDED`, review/archive an actually unused draft or resolve the subscribed allowance through the existing commercial workflow. For `CAPACITY_RECONCILIATION_REQUIRED`, stop capacity-changing actions and inspect the workspace's `rentable_allocation`, `rentable_usage_event`, `workspace_grant` and audit evidence with authorized operator tooling. No automated property counter repair is implemented; never delete history or reset counters to hide a mismatch. Keep backups and use reviewed forward migrations; V41 must not be rewritten after application.

## Workspace subscription dashboard

After `./scripts/compose-local.sh up -d --build --wait`, sign in at http://localhost:3100/login, select an owner workspace and choose **Subscription dashboard**. The canonical route is `/workspace/billing/subscription?workspace={id}`. It reads the persisted Free contract, current entitlement decisions and measured capacity. Billing periods, paid renewals and next invoices are not applicable to Free; unimplemented add-on/payment/messaging sections explicitly say unavailable. Catalogue alternatives are comparison baselines, with no checkout or external charge.

Run `node web/tests/subscription-dashboard.mjs` against the local stack for the real login/dashboard/privacy/recovery/accessibility journey. `SubscriptionIntegrationTest` runs with the established PostgreSQL/Testcontainers settings. On network failure use **Refresh subscription**; stale details are cleared. A denied workspace requires current billing authority, not a different plan. See release status and ADR 0039 for the remaining paid-lifecycle dependencies.

## Local subscription upgrade verification

The upgrade flow is a **payment simulation**, not live checkout. Start/update the local stack with `./scripts/compose-local.sh up -d --build --wait`. Configure an explicit sandbox offer (the following values are test arithmetic only, not commercial prices or tax advice):

```sh
python3 scripts/configure-sandbox-upgrade.py --source FREE --target SILVER --amount 100.00 --tax-rate 18 --months 1 --proration none_free_only
```

As an owner of a workspace subscribed to that source plan version, open **Subscription dashboard → Review subscription upgrade**. Generate/review the quote, confirm the request, then review a clearly labeled simulated success/unknown/failure. Nothing is collected. Only simulated success applies the target and capacity; unknown keeps the old plan and can be reconciled on the same request. For a second test upgrade from Silver to Gold configure the corresponding explicit price and `--proration unused_time_credit`. Only implemented source capabilities are copied; this does not enable unbuilt paid features.

Use `node web/tests/subscription-upgrade.mjs` for automated verification. It creates a labeled test offer and isolated workspace using real UI/API operations. Quoted money and targets come from Java, not browser arithmetic.

Recovery: after a lost response retry the retained exact command. For a stale/expired quote, refresh/review new terms; if an existing sandbox request expired, record a failed outcome before creating another. For payment_unknown, reconcile the same request rather than starting another payment. An audit failure returns retryable 503 and leaves plan/capacity/payment evidence unchanged. Never manually set subscription paid status or edit quotes/payment history. To withdraw an unused offer, an authorized local operator may insert its UUID and reason into `subscription_upgrade_offer_withdrawal`; this prevents new quotes/settlements, so resolve any awaiting sandbox request as failed. Applied history is retained.

Live purchases remain disabled pending provider integration and approved merchant/pricing/tax/credit policies. See ADR 0040 and release status. Existing older source-version workspaces need an explicitly reviewed offer for their contracted version; this helper selects current published source/target versions and does not migrate old contracts.


## Local subscription downgrade verification and recovery

After the local sandbox upgrade journey above, open **Subscription dashboard → Review subscription downgrade**. Review Free's feature/capacity changes and excess usage, then confirm the stored billing-period boundary. Scheduling preserves the current plan until the worker applies the change. Refresh status to see the committed outcome; all retained units/members remain visible and excess blocks further capacity consumption. Paid-to-paid targets explain their pending renewal dependency. No real payment or refund occurs.

Run `node web/tests/subscription-downgrade.mjs` against the healthy local Compose stack for the accelerated isolated test. It creates test records, stops/restarts the worker and shortens only its own workspace billing period; do not run it concurrently with other worker-dependent browser tests. Normal application use does not alter the billing clock.

Recovery: keep the worker running; default scheduling is every 30 seconds after a 60-second startup delay (`lmt.subscription.downgrade-delay-ms` / `lmt.subscription.downgrade-initial-delay-ms`). Claims expire after 60 seconds; transient errors retry with bounded backoff up to five attempts per retry cycle. Exhaustion or changed authority/terms appears as **review_required**. Use the screen's reviewed retry for restored transient failures. If original requester authority, subscription or policy changed, cancel and create a fresh authorized review. Cancellation before application fences claimed work. Never force applied status, delete excess records or reset usage counters to fit the target. Valid retained source evidence can be reconciled above a reduced allowance through existing reviewed repair. Private account notices and domain audit record outcomes; external SMS/email is not promised. See ADR 0041 and release status for scope and test evidence.


## Local paid subscription cancellation

From a locally upgraded workspace, open **Subscription dashboard → Review subscription cancellation**. Review the billing boundary, access/retention consequences and refund/credit applicability, then confirm. The worker ends paid access at that boundary and keeps the workspace on Free; records remain, and excess usage blocks new capacity. **Review withdrawal** stops a pending cancellation; it does not reverse an applied transition. The history distinguishes withdrawn from applied requests.

Run `node web/tests/subscription-cancellation.mjs` against the healthy local Compose stack. Like the downgrade test, it creates isolated test records, shortens only its own workspace billing period and stops/restarts the worker; run it sequentially with worker-dependent tests. Refund and credit amounts are not applicable because local upgrade payments are simulated. Live cancellation and Free workspace closure are not enabled.

Recovery uses the existing boundary worker described above: refresh persisted status, retry only after resolving transient failure, or withdraw and review fresh terms when authority/policy changed. Do not delete records, force request state or fabricate a refund. Original reviews/audit remain immutable. See ADR 0042 and release status for policy gaps and verified behavior.


## Local failed subscription payment recovery

In the local upgrade flow, record a clearly labeled **failed outcome**, then open **Review failed payment recovery** (also **Payment failure and recovery** from the subscription dashboard). The existing plan remains active. Review the failure, grace explanation, retry limit and earliest retry time. After the actual 30-second cooldown, refresh and confirm **Review payment retry**. The same request becomes awaiting payment; only a separately verified simulated successful outcome activates it. Unknown outcomes must reconcile on the same attempt, not start a new collection.

The initial versioned local policy allows three total attempts per confirmed request. Expired/changed quotes, exhausted attempts and legacy requests without known retry policy need fresh reviewed upgrade terms. Repeating a failed outcome cannot restart the clock. Network uncertainty uses **Retry same recovery action** with the existing key. No automatic retry, real charge or renewal grace countdown is configured. Separate permission/security restrictions still apply.

Run `node web/tests/subscription-payment-recovery.mjs` against the healthy Compose stack. It uses real login and persistence, waits the actual local cooldown and verifies lost-response replay, unknown reconciliation and one activation. V45 is additive; old payment history is preserved with unknown historical attempt counts. Policies are immutable versioned commercial configuration; do not alter applied migrations, overwrite quotes or force attempts/status to bypass limits. See ADR 0043 and release status for remaining live/renewal work.


## Local receipt and activation recovery

From a pending local upgrade, open **Payment failure and recovery**. **Record pending callback** leaves payment unknown. **Record received payment for background activation** commits an explicitly simulated receipt; the Java worker then activates independently of the browser. Refresh to see queued/applied/review-required status. Do not pay again when a receipt exists. Legacy immediate simulated-success controls remain test-only and do not model a provider-received payment surviving activation failure.

Run `node web/tests/subscription-settlement-recovery.mjs` sequentially against the healthy local Compose stack. It stops/restarts the worker, delivers a simulated paid callback while the page is closed, repeats the same event and verifies independent activation, reopened status and reversal review. Always let its cleanup restart the worker.

V46 is additive and must precede the coordinated API/worker rollout. The worker runs every five seconds after a ten-second initial delay, leases for 60 seconds and bounds retries to five claims per retry cycle. Resolve transient faults, then use **Review activation retry without payment** if shown. Changed terms, conflicting payments and reversals remain operator-review cases; automated compensation/operator resolution is not implemented. **Review simulated payment reversal** fences pending activation or preserves the current plan after activation, retaining records and holding discretionary subscription changes. Never clear the reversal or rewrite payment evidence directly to bypass review.

The callback endpoint requires normal owner session/CSRF and is only a local simulator; it is not a signed provider webhook. Renewal callbacks/mandates, cancellation during renewal, live refund/credit and reversal resolution remain unavailable. See ADR 0044 and release status.


## Central feature eligibility and local entitlement inputs

Open **Inspect feature eligibility** from the subscription dashboard (or **Inspect a feature decision** from usage). Enter a configured code such as `PROPERTY_MANAGEMENT`, `PROPERTY_CREATE`, `WORKSPACE_MEMBER_ADD` or `AI_CHATBOT`. The response comes from the same engine used by backend business commands. Active members may read the single-capability decision; resource operations still require their own authority and available quota.

Local operator example (replace the workspace and use a future explicit UTC expiry):

```sh
python3 scripts/configure-sandbox-entitlement.py --workspace WORKSPACE_UUID --capability PROPERTY_MANAGEMENT --kind commercial_override --effect deny --expires-at FUTURE_ISO_INSTANT --reason 'Local test of property creation restrictions'
python3 scripts/configure-sandbox-entitlement.py --workspace WORKSPACE_UUID --revoke INPUT_UUID --reason 'Local test complete; restore inherited eligibility'
```

Kinds are `add_on`, `timed_grant`, `commercial_override`; only overrides may deny. `--starts-at` optionally schedules the input; expiry is required. The utility binds the current plan, rejects live workspaces and records immutable evidence/audit. An `add_on` input is explicitly local configuration, not a paid subscription. Required corrections cannot be commercially denied, and unavailable features remain unavailable even with an allow. Do not edit historical policy/input rows; revoke and create a new reviewed input. Inspect evidence before repeating an uncertain CLI command.

Run `node web/tests/entitlement-evaluation.mjs` against the healthy Compose stack for the real configuration → inspector → domain-denial → revocation → successful-property-creation journey. Live add-on billing/activation and production administrative governance remain unimplemented. V47 changes the effective policy; pending subscription quotes may need fresh review and paid activation conflicts retain receipt evidence. See ADR 0045 and release status.

### Feature locks and effective snapshot

Active workspace members can read `/api/v1/workspaces/{w}/entitlements/snapshot`. The response is private and advisory: effective features, configured local upgrade guidance and aggregate quota counters. Existing commands recheck permissions, eligibility and quotas. Property setup, manager invitations and the entitlement inspector use shared lock notices; input drafts survive refresh. UI reads refresh every 15 seconds, on focus/visibility and after workspace mutations across tabs. Only owners receive upgrade actions; unconfigured or unavailable capabilities do not promise an upgrade. See [ADR 0046](docs/architecture/adrs/0046-feature-lock-snapshots.md) and [release status](docs/release-status.md) for verified scope and outstanding production configuration.

### Unit capacity warnings

Property setup shows current unit usage, reservations and remaining allowance from the private backend snapshot. At 80% usage it warns before capacity; at capacity it blocks counted creation/restoration review while preserving drafts and separate correction actions. The backend independently checks every capacity mutation under a transaction. Inconsistent inventory/counter/evidence returns `CAPACITY_RECONCILIATION_REQUIRED`; no automatic evidence repair is performed. Bulk imports remain unavailable pending the FS-32 resumable-import contract. See [ADR 0047](docs/architecture/adrs/0047-unit-capacity-enforcement.md).

### Shared-bed counting

New sandbox workspaces use counting policy 2: whole units and independently rentable beds count once, shared-unit containers count zero. Properties/buildings/floors and occupant capacity are not quota quantities. Draft, ready and maintenance-blocked inventory counts; archive releases and restore reacquires. Create a shared unit with physical bed capacity, then select **Separately rentable bed** and its parent in property setup. The quota panel shows the bound policy version. Existing workspaces retain policy 1 and do not silently adopt this rule; shared creation remains unavailable there. Live shared inventory and weighted pricing are not enabled. See [ADR 0048](docs/architecture/adrs/0048-workspace-bound-unit-counting.md).

### Communication acceptance metering (local only)

`/workspace/billing/usage?workspace={id}` shows private communication allowances and recent operations. By default every channel is unconfigured. To test email or SMS, use `python3 scripts/configure-sandbox-communication.py --help`, supplying an explicit workspace, `--channel email` or `sms`, `--metric provider_accepted`, allowance, timezone-aware start/end instants and a reason. Periods cannot overlap and contracts are immutable. Log in with the corresponding linked test contact, review the one-message confirmation and submit. Mailpit/SMS-inbox acceptance consumes one unit; it does not prove external delivery or a charge. No money is collected. OTP/sign-in traffic does not consume workspace communication allowance. Unknown outcomes retain reservations and are not resent automatically. WhatsApp, other providers and other contractual metrics are unavailable pending integration. See [ADR 0049](docs/architecture/adrs/0049-communication-acceptance-metering.md).

## Amenity catalog and communication recovery

Start the local stack with `./scripts/compose-local.sh up -d --build --wait`. Sign in and select an owned workspace. Open `/workspace/inventory?workspace={id}` (also linked from properties) to search default definitions or create, review, edit and retire a custom amenity. These definitions do not assert installed inventory.

At `/workspace/billing/usage?workspace={id}`, an unknown local communication outcome offers a reviewed evidence check. The durable check queries Mailpit/SMS acceptance without resending. Missing or expired evidence keeps capacity reserved. Date filters also show overlapping historical communication contracts with whole-period totals. Existing explicit local contract configuration is still required; no production allowance or delivery is implied. Run `node web/tests/communication-recovery.mjs` and `node web/tests/amenity-catalog.mjs` against the local stack. See ADRs 0050–0051 and release status for limitations.
