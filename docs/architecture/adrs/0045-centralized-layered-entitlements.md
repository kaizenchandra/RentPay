# ADR 0045: Centralized layered entitlement evaluation

Date: 2026-11-10. Status: accepted for implemented operations and explicitly local supplements; live add-on activation remains pending.

## Decision and scope

SAAS-ENT-001 / FS-05 extends the existing `Entitlements` engine rather than introducing a frontend or parallel billing engine. The authenticated single-capability endpoint uses the same evaluator as membership/property business commands, subscription previews and dashboard projections. Eligibility never establishes identity, record permission, quota or paid status.

V47 publishes commercial policy 3, preserving existing policy/plan bindings, and adds operational availability plus protected-correction metadata. The source example codes are represented. PROPERTY_MANAGEMENT explicitly describes the implemented property register/inventory subset; other unimplemented product capabilities fail closed as FEATURE_UNAVAILABLE. A grant is not implementation of AI, advertising, notifications, reports, public APIs or the complete rental lifecycle.

## Inputs and precedence

1. Read authoritative subscription status and immutable contracted plan version; public catalogue changes do not switch contracts.
2. Resolve versioned plan capability inclusion.
3. Union applicable add-on and time-limited allows. Inputs must match workspace, local mode and bound plan (or an explicitly unbound scope), start <= evaluation time < expiry, and not be revoked.
4. Apply commercial overrides, with deny winning over allows, including the existing member-onboarding override. A current protected-correction definition cannot be denied even by an input bound to an older policy.
5. Enforce active subscription, workspace billing/security restrictions and operational availability as hard gates. An allow cannot bypass those gates.
6. PROPERTY_MANAGEMENT denial blocks discretionary PROPERTY_CREATE/RENTABLE_CREATE through the engine. It does not block PROPERTY_EDIT/PROPERTY_ARCHIVE or corrective membership removal.

Decision payloads include source, reason, subscribed plan/policy versions, operational status, existing override revision/expiry and active supplemental evidence. Time-of-evaluation is outside decision fingerprints so ordinary reads do not invalidate quotes. Effective grant changes, expiry/revocation or a new policy can invalidate reviewed subscription terms; received payments must enter existing recovery rather than collecting again.

V47 adds immutable `workspace_entitlement_input` and separate revocation evidence, scoped foreign keys, member-read RLS and transactional audit triggers. Runtime credentials have SELECT only; users cannot self-grant through supplied IDs or paid-state flags. Inputs and revocation serialize with the membership lock used by subscription/membership commands. Property mutations now acquire that lock before their existing property lock, preventing grant-revocation races with eligibility checks. No quota counters are modified by capability grants.

## Local configuration and user experience

`scripts/configure-sandbox-entitlement.py` is a privileged local Compose operator utility. It creates explicit add_on/timed_grant/commercial_override inputs bound to the current plan, with reason/start/expiry, or revokes an input. Live workspace writes are rejected. These are labeled local test configurations, not paid add-on purchases or verified provider settlements. The CLI does not provide production authority delegation, pricing, checkout or administrator governance. Existing commercial override administration remains separate and unchanged.

`GET /api/v1/workspaces/{w}/entitlements/{capability}` accepts current active workspace membership; unknown codes return 422 UNKNOWN_CAPABILITY and absent/revoked membership returns privacy-safe 404. Existing aggregate usage remains owner-only. The single-capability read does not grant manager property-write permission. `/workspace/billing/entitlements?workspace={id}` displays the server decision and evidence with accessible evaluation/error/retry states. Dashboard and usage link to it; protected operations re-evaluate on the server.

## Limits and recovery

Live add-on purchase/verified activation, full commercial feature matrix, production grant/override governance and all currently unavailable features remain pending. Existing active-paid state is authoritative; this does not invent a renewal grace engine. Expiry is evaluated on each request, so no worker is required to remove access. Revocation appends evidence, never deletes configuration or audit. Required corrections and historical data remain governed by their existing permissions/restriction rules.

Do not rewrite referenced policy, input or revocation rows. Revoke mistaken local inputs and issue a new reviewed input; inspect audit/evidence before repeating an uncertain operator command. Apply V47 before the coordinated runtime rollout. Historical quote fingerprints may require fresh review after policy 3; paid receipt conflicts use existing activation recovery. See release status for actual verification and remaining gates.
