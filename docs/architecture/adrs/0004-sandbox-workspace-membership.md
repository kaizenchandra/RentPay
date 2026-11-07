# ADR 0004: Sandbox workspace manager membership

Status: accepted for the sandbox Phase 1 foundation, 2026-11-07. The invitation/member subset of FS-02 and manager-capacity subset of FS-05 are implemented; their parent requirements and Phase 1 remain Partial.

## Scope and authority

An active workspace owner can invite an Indian test mobile contact to become a manager member. The recipient must sign in with that exact contact and explicitly accept the current invitation version. No bearer invitation link, forwarded UUID or supplied role establishes authority. Invitations may precede account creation. The account inbox needs no workspace membership.

This establishes **workspace membership only**. It provides a scoped workspace context, not property access, ownership, signing, spending, payout or member-administration authority. Property delegation, effective dates, handover and daily operations remain Phase 2 work. Owner membership cannot be revoked through the manager command. Managers cannot read the owner member directory, setup or invitation-administration APIs. Context is explicit in each route/request and is reauthorized at every API read.

Creation and acceptance are restricted to active sandbox workspaces. Existing manager revocation remains available when the workspace is restricted. The original inviter must still have active owner authority at acceptance. Live contact proof, provider delivery and production policies remain prerequisites; simulated identity cannot establish live membership.

## Sandbox counting and expiry policy

Count each active MANAGER account once per workspace; exclude OWNER, pending/expired/declined/revoked invitations and inactive memberships. Pending invitations do not reserve a manager slot or promise successful acceptance. This is a documented development policy, not an approved production commercial rule. The allowance comes from the workspace's existing version-bound commercial grant, without plan-name conditions. Free's configured baseline is one manager.

At acceptance the commercial command locks/reserves one available slot, persists its allocation and consumes the reservation in the **same transaction** as membership and audit. A losing last-slot command rolls back its provisional membership. There is no asynchronous reservation to expire: either all changes commit or none do. Revocation marks the commercial allocation released and decrements usage exactly once. Re-invitation reactivates the same membership identity with a new version and allocation; prior evidence remains.

Invitation expiry defaults to 168 hours and can be configured with `lmt.workspace.invitation-hours` (1–720). The exact deadline is persisted and displayed before acceptance; creation confirmation shows the configured duration. Expired invitations immediately fail acceptance even after process downtime. Expiry is a projection of the stored deadline, not a cleanup-job prerequisite. A new invitation is required after expiry/decline/revocation. These defaults need policy approval before live enablement.

## Ownership, transactions and isolation

- `workspace`: invitation/member rows and membership command results; `WorkspaceMembership` is the application coordinator.
- `commercial`: grant counters and `manager_allocation`; only the exported `ManagerCapacity` commands write them, require an existing transaction and are called after workspace authorization. The dependency is workspace → commercial, with no reverse dependency.
- `identity`: existing account/session ownership. Membership projections use only required account display name/contact; no session/credential projections are exposed.
- Shared audit remains append-only for the runtime role.

V5 is additive: membership IDs/versions/joined timestamps, invitations, scoped command results, allocation history and narrowly expanded runtime grants. It does not rewrite V1–V4 or delete existing data. Unexpected legacy manager/counter mismatches block release with `CAPACITY_RECONCILIATION_REQUIRED`; an operator must reconcile evidence rather than reset counters blindly.

One workspace advisory transaction lock serializes invitation acceptance, cancellation and member revocation. Commercial conditional updates plus the existing `used + reserved <= allowance` constraint protect capacity. Idempotency is actor/workspace/operation/key scoped and hashes the exact target/version or invitation contact. ETag/If-Match guards existing-record commands; changed confirmation/key reuse conflicts. Acceptance replay rechecks active membership and cannot restore revoked access.

Invitation RLS permits only current workspace owners or the matching account contact. Member RLS retains self scope and adds an owner predicate. A narrow SQL SECURITY DEFINER boolean predicate avoids recursive membership-policy evaluation; it uses fully qualified tables, a fixed search path with `pg_temp` last, revoked PUBLIC execution and runtime-only execution. It performs no mutation. Java still enforces action-level permissions. Reference: [PostgreSQL row security](https://www.postgresql.org/docs/17/ddl-rowsecurity.html), [safe definer functions](https://www.postgresql.org/docs/17/sql-createfunction.html).

List reads use repeatable-read snapshots, stable UUID ordering, 25-item pages and actor/workspace-bound cursors. Raw contact is submitted over the authenticated API and retained privately for recipient binding; list projections mask it. Private routes and APIs are no-store/noindex. Invitation-time workspace names are snapshots, not a second workspace-name authority.

## Notifications and recovery

The durable invitation inbox and membership state provide in-app notification/status: pending, expired, accepted, declined, revoked or access revoked. These source records are committed with audit; no external delivery is attempted and no SMS/email success is claimed. General notification preferences, external dispatch/outbox delivery and live verification remain pending.

The browser confirms exact contact/scope/workspace or target member/version. Unknown outcomes retain the same command in memory for explicit retry, disable competing actions, and warn on navigation. Sensitive drafts are never stored in browser storage. Definite failures expose refresh/review. Revoked managers lose context access on their next request; already-authorized in-flight work is not claimed to be cancelled.

## Verification and limits

`FoundationIntegrationTest` exercises cross-account/forwarded-ID denial, owner protection, stale versions, expiration, decline/cancel, revoked inviter, two recipients racing the last slot, rollback, duplicate acceptance/revocation and release/reuse. `membership.spec.ts` exercises real owner/recipient UI, context selection, access removal, lost committed-response retry, OpenAPI schemas, keyboard confirmation, mobile overflow and axe. The recovery script verifies the invitation ID/idempotency result survives database restart.

Full effective entitlements/add-ons, generic quota reservations, member role editing, owner succession, property delegation, live identity/delivery, abuse controls, retention, manual screen-reader and production operational evidence remain pending. This is not a completed manager rental workflow or Phase 1 completion.
