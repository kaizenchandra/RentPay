# RentPay / LMT

LMT (Landlord, Manager, Tenant) is being built in the existing **RentPay** repository. The current delivery includes a PostgreSQL-backed catalogue and a **local sandbox identity → session → Free workspace provisioning → persisted setup** journey, plus profile-name editing, active browser-session revocation and sandbox manager invitations. It is not a completed rental SaaS or a live SMS integration.

Start at `/login` (or `/register`). Verify a test mobile using the private local inbox, choose/create a Free workspace, and revisit its setup through `/account`. Workspace membership, subscription binding, allowances, audit, retry records and in-app welcome delivery are durable. Context is explicit per tab, and unauthorized workspaces return privacy-safe 404. `/pricing` retains public versioned plan comparison; unknown paid prices remain unknown.

Live SMS, rental properties/units, tenancy operations, payments and personal memberships remain unavailable. AUTH-001 and SAAS-REG-001–003 are **Partial**, because their complete source requirements extend beyond this slice.

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

E2E tests require the running API, database and built web application. They exercise real persisted data through the same-origin API and page. Only SMS delivery is simulated through the private filesystem inbox; test workspaces are labelled sandbox. The recovery scripts temporarily stop/restart only the local Compose database and leave its volume intact. Browser failure-recovery checks are documented in the release report. Automated accessibility scans do not replace manual screen-reader verification.

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

For bounded local browser verification: `npm run test:e2e -- --workers=2` in `web`. The extended `node tests/foundation-recovery.mjs` checks persisted invitation replay after a real database restart.

### Usage and eligibility

Workspace owners can open `/workspace/billing/usage?workspace=…` from setup or member administration. It shows current manager capacity, reconciliation, versioned membership eligibility and manager commit/release history. Date filters apply only to history and use inclusive Asia/Kolkata calendar dates; capacity does not reset monthly. Rentable-item usage is explicitly unavailable until inventory measurement exists.

V6/V7 preserve previous migrations and backfill manager events from actual allocation timestamps. The restart test now verifies accepted manager usage. Full add-ons/overrides/time-limited grants, paid billing and inventory quotas remain pending; see [ADR 0005](docs/architecture/adrs/0005-membership-entitlements-and-usage.md).

### Background operations and recovery

Open workspace setup → **Background operations** (`/workspace/operations?workspace=…`) to inspect welcome delivery and its recorded attempts. The current handler saves an in-app workspace message only. Pending/running jobs refresh for a bounded period; failed or blocked jobs expose reviewed retry/cancellation with an explanation and exact version.

Automatic retries stop after five attempts per cycle. Leases default to 30 seconds (`lmt.workspace.lease-seconds`, 1–300); expired claims can be recovered after process/DB interruption. Retry requires the original creator's current owner authority. Completed messages cannot be recalled. V8 preserves previous events/messages; older completed jobs may have no recorded attempt history. See [ADR 0006 and recovery runbook](docs/architecture/adrs/0006-durable-welcome-operations.md).

The operations browser test deliberately injects a failed-state fixture into its own local sandbox workspace. `OperationsIntegrationTest` separately verifies real database delivery failures and lease recovery. Neither is a simulated live provider success.
