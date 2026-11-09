# ADR 0016: Scoped, expiring manager-onboarding overrides

Status: Accepted for the local Phase 1 entitlement slice, 2026-11-09. SAAS-ENT-001–003 / FS-05 and SAAS-ADMIN-002 / FS-06 remain Partial.

## Scope and precedence

Extend the existing commercial evaluator rather than creating another eligibility engine. One supported capability, WORKSPACE_MEMBER_ADD, can receive an immediate temporary allow/deny override or return to inherited plan configuration. No arbitrary capability code, price, subscription status, allowance or permission can be supplied. WORKSPACE_MEMBER_REVOKE is excluded by the database capability check and remains available for corrective access/capacity release.

Evaluation order is subscribed version inclusion → active temporary override → subscription availability → workspace restrictions/security suspension. An allow does not bypass later restrictions, resource permission or quota consumption. Invitation creation and acceptance already call this evaluator inside their authoritative membership transaction. Expiry is exclusive (`expires_at > clock_timestamp()`), evaluated on each request; no cleanup job or client timer is needed to restore plan eligibility.

The snapshot adds source, override revision and expiry. The owner usage page displays source and expiry; membership UI shows the server-provided restriction before invitation confirmation while retaining correction actions. Accepted members are not implicitly revoked when onboarding is denied. Existing prices, plan/subscription references, allowance counters and personal memberships remain unchanged.

## Authority and persistence

V20 adds entitlement_operator_grant with account/workspace scope, expiry/revocation, attribution and trigger audit. No runtime grant mutation privilege exists. Workspace ownership and global catalogue review do not imply this permission. The separate local bootstrap requires an existing sandbox account and sandbox workspace. Runtime administration fails closed outside sandbox mode. One-hour default grants, 1–24 hour bootstrap bounds and an override maximum of 24 hours are local safety controls, not approved production commercial policy.

An operator may view/change only the selected override. This does not grant membership or access to workspace usage, tenant files or other workspaces. The UI identifies the exact workspace UUID; it does not bypass workspace privacy merely to fetch a friendly name. Owners/members can read applicable eligibility through their existing authorized domain projections. Private rows use account/member/operator RLS; recovery journals are actor-scoped and append-only to runtime.

workspace_capability_override stores the current revision; entitlement_override_command retains before/after projections, normalized request hash and reason. Existing evidence is preserved when configuration changes. Revoking operator authority blocks subsequent commands and replay; it does not undo an already-authorized override, which remains until its own expiry or a separately authorized change.

## Concurrency and recovery

Operator grant changes and API authorization share an actor/workspace advisory lock, acquired by the grant audit trigger before commit. This serializes revocation without giving runtime UPDATE privileges on grants. Override mutations also acquire the existing membership advisory lock, serializing them with invitations and acceptance. Current authority is rechecked before mutation.

GET/POST `/admin/workspaces/{id}/member-entitlement` requires explicit scoped authority. POST additionally requires CSRF, strong ETag, reason and actor-scoped idempotency. The ETag includes revision and whether the expiry is currently effective, so expiry can stale a pending confirmation. Exact retries return their original result even after expiry; GET supplies current configuration. Changed confirmation conflicts. Configuration, before/result evidence and audit commit together; failures roll back all database effects.

The private `/admin/workspaces/{id}/member-entitlement` UI confirms effect, workspace, reason and expiry interpreted in Asia/Kolkata. It retains unknown command identity in memory, warns on unsaved changes, clears on context changes and refreshes current state after a recorded result. No provider, payment or irreversible external effect occurs.

## Verification and remaining work

Tests cover owner versus operator authority, scope isolation, current authority on replay, actual invitation and pending-acceptance denial, expiry/inherit fallback, restrictions stronger than allow, quota enforcement, protected revocation, rollback, concurrent updates, stale expiry ETags and database capability constraints. Desktop/mobile journeys cover lost response, schema conformance, source/expiry display, blocked onboarding UI, restoration and accessibility scans. See release status for actual runs.

Other capability grants, add-ons, layered allowances, production commercial policy/approvals, grant notifications/retention, full quota reservations, paid billing and domain-specific feature gates remain pending. The 24-hour sandbox control is unavailable in production until an approved policy is implemented. No dependency versions changed.
