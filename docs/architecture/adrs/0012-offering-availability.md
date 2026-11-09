# ADR 0012: Whole-offering catalogue availability

Status: Accepted for the sandbox Phase 1 slice, 2026-11-09.

SAAS-ADMIN-002 requires offering activation/deactivation without rewriting active contracts. FS-03 requires public reads of active offerings; SAAS-REG-002 requires selectable Free provisioning.

Availability belongs to an audience/code family, separately from immutable published versions. V14 stores explicit state and revision; an absent row is available at revision zero. Withdrawn families are excluded after latest-effective-version selection, with no older-version fallback. Withdrawal covers future published versions too. Existing subscriptions and capability references remain unchanged. Workspace Free cannot be withdrawn, enforced by the use case and a database check.

GET/POST `/api/v1/admin/plan-versions/{id}/availability` accepts a published version selector. Read requires current commercial review authority; mutation additionally requires independent, false-by-default `can_manage_offering`. CSRF, exact strong ETag, reason and actor-scoped idempotency are mandatory. Actor and audience/code locks serialize commands with publication and grant changes. State, before/result snapshots, command and audit commit together. Replay rechecks authority; uncertain results retry the original body/key. Runtime cannot update/delete command evidence.

V15 binds publication reviews to the availability revision. Preview explicitly discloses whether publication will remain hidden; availability changes invalidate prior reviews even if subsequently restored. A separate migration preserves already-applied V14 history.

Private admin UI confirms the whole-family effect, protects Free, handles conflicts and uncertain responses, and exposes read-only state to reviewers. No provider effect, paid activation, subscriber migration, price, entitlement or money mutation occurs. Public queries remain uncached. Browser tests run serially because they temporarily change shared sandbox catalogue state and restore their own changes.

Recovery: after an unknown response, retry the same confirmed command. On a known stale-version response, reload current availability and review a new confirmation. A separately authorized restore increments revision; it never rewrites history. Failed audit writes roll back the operation. Never directly delete evidence to recover.

Production identity/MFA, approval governance, retention/notification policy and full commercial configuration remain pending. This advances SAAS-ADMIN-002 and FS-03/06 only partially. Tests: OfferingAvailabilityIntegrationTest plus desktop/mobile plan review and availability browser journeys; see release status for actual run evidence.
