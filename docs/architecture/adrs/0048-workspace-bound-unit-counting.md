# ADR 0048: Workspace-bound independent-item counting

Accepted 2026-11-10. SAAS-QUOTA-002, FS-05/FS-32.

Choose the recommended independently rentable inventory baseline for **new sandbox workspaces**, policy 2. Each whole unit counts one; each separately rentable bed counts one; its shared-unit container counts zero. Properties, buildings and floors count zero. Physical capacity, occupant count and tenancy count are not multipliers. Weighted combinations are not offered. This is explicit sandbox commercial configuration, not approval of production pricing or live shared accommodation.

All nonarchived inventory contributes according to that classification: draft, ready and maintenance-blocked (temporarily inactive) items count. Occupancy must not discount a counted item. Archived items contribute zero and must reacquire a slot when restored. Classification and rental mode cannot be edited to evade counting. Parent shared capacity cannot shrink below active child beds; an active child prevents parent archive. Readiness, property restrictions and absence of rent data do not release capacity.

V49 binds the counting version to each workspace. Existing workspaces keep their pre-existing version 1; new live workspaces keep shared counting unavailable. Binding and definition updates/deletes are rejected. New policy publication does not silently reprice inventory or change a workspace's count. Future transitions require an explicitly reviewed migration with reconciliation; no policy-change UI or automatic adoption is offered.

A single pure database quantity function supplies the authoritative classification used by commands and reconciliation. The existing Java transaction owns resource authorization, entitlement checks, grant locking, allocation consumption/release, ledger and audit. New workspace bindings are committed and audited with provisioning. Consumers see the bound version and definition in the private quota snapshot/usage report. Property UI supports shared containers and beds under eligible sandbox policies and uses the same server-provided policy for advisory controls.

Race/recovery evidence covers last-slot create/restore, repeated archive replay, concurrent restore of one version (one succeeds; one is stale), unavailable-item counting, active-child archive prevention, immutable bindings, newer-policy non-adoption and live shared denial. Real browser tests create a zero-count shared container and one-count bed, archive the bed and verify release.

Limits: tenancy/possession integration is not implemented; do not claim occupied archive protection is verified. Before occupancy is enabled, archive must check active allocations/possession and closure rules in addition to children. FS-32 imports, reservations and partial recovery remain unavailable and must reuse this bound definition. Production shared counting and historical contract transitions need explicit approval and evidence; no weighted policy is synthesized.
