# ADR 0017 — Reviewed sandbox manager counter repair

Status: Accepted for the current sandbox foundation; production governance remains pending.

## Decision

Provide a counter-only recovery command where existing membership, allocation and usage-event evidence agree. It derives `used` from committed allocations and clears unexplained transaction-local reservations. It cannot accept a client count, change the allowance, create memberships, or fabricate/repair historical source events. Missing event pairs, unmatched events, membership/allocation identity mismatches and source usage above the allowance block repair.

Require a separate workspace-scoped, expiring capacity-repair operator grant AND active ownership of the sandbox workspace. Neither entitlement administration nor ordinary ownership suffices. This deliberately narrower workflow does not provision support access to otherwise inaccessible workspaces. Production support/second-approval policy is still unresolved. The local bootstrap checks existing sandbox subjects and is not exposed through the runtime API.

V21 adds audited grant issuance, append-only-to-runtime command before/result snapshots and a counter repair revision. Grant changes and commands share an authority advisory lock; repairs share the existing membership lock and grant row lock. Review returns a strong ETag bound to counters, allowance, source evidence and repair revision. Confirmation checks it under the locks. Current authority is checked before idempotent replay. Same actor/key/body returns the original result; changed confirmation conflicts. Audit, counters and command result commit together.

The existing owner usage page links to the explicitly scoped repair screen. A reason and exact before/after review are required. Unknown outcomes preserve the same command key/version/body for retry. Refresh retrieves current evidence. No sensitive draft is stored in the browser.

## Limits and evidence

This resolves sandbox counter drift, including stranded reservations under DEC-017. It is not a general reservation-expiry worker, source-evidence repair, financial adjustment or production capacity override. There is no new capacity entitlement or change to counting policy. Non-owner platform operations, general reservations, approved production policy and operational monitoring remain incomplete.

CapacityRepairIntegrationTest covers grant separation/owner scope, replay/revocation, concurrent stale confirmation, source mismatch and audit rollback. FoundationIntegrationTest now resumes an actual blocked invitation through the authorized repair API. EntitlementPolicyModeTest verifies non-sandbox fail-closed behavior. capacity-repair.spec.ts uses real local API/PostgreSQL on desktop/mobile, including lost successful response recovery, one revision, generated contract validation, keyboard confirmation, axe and revoked access. See release status for executed commands and counts.
