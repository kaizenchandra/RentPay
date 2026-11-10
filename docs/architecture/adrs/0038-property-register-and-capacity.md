# ADR 0038: Property setup and rentable capacity

Date: 2026-11-10. Status: accepted for the implemented sandbox setup slice; full FS-07/08/09 remains incomplete.

## Context and source

PROP-001 / OWN-WF-001 / MARKET-002 and FS-07 require real hierarchy, durable edits, scoped access, readiness blockers and atomic rentable capacity. FS-08/09 add ownership/delegation and physical inventory; these cannot be inferred from workspace membership or a furnishing label. ARCH-01/02/03/05/06/07 and INT-01–06/09–12 apply. The full specification takes precedence over illustrative FRS route/payload examples.

## Decision

The existing Java modular monolith owns `property_record`, `property_space`, command deduplication and immutable property history. The commercial module owns rentable allocation/usage and grant updates. Next.js uses generated OpenAPI contracts, one-origin API requests and explicit workspace/property selectors. Existing Java/JDBC, migrations, npm and pinned dependencies are retained; no dependency upgrade or second business engine is introduced.

Property containers and building/floor records do not consume capacity. An explicit development-only baseline counts nonarchived whole units, including drafts. Shared units/beds fail closed until shared counting is selected in a versioned policy; neither Diamond allowances nor production counting are invented. The requested shared-counting decision is still pending. V41 is additive and preserves prior commercial policy versions; the current policy adds property capabilities to Free. Editorial plan publication inherits the complete source capability set.

Mutations serialize per workspace and lock the commercial grant when consuming/releasing capacity. Property/space changes, allocation events, history, domain audit and saved idempotent response commit together. Duplicate commands replay the stored result only after current owner authorization. Changed confirmations conflict; existing-resource mutations require strong If-Match. Parent foreign keys include workspace/property; application validation enforces hierarchy type and shared physical capacity. UUID-keyset pagination is bounded and context-bound. No record deletion or external side effect occurs.

Current authorization is workspace-owner draft setup only. This is not verified legal ownership or delegated manager authority. RLS mirrors this limited scope. Managers, co-owners without the current owner membership and tenants have no access until FS-08/09 subject/property authority is implemented. Readiness reviews retain blockers; activation is unavailable. Setup-ready does not imply vacant, allocated, published, signed or occupied. Prices are decimal-string proposed configuration, not issued charges.

The implementation uses `/properties/{id}/spaces` for one validated hierarchy command path covering buildings, floors, units and beds instead of duplicating the illustrative FS-07 `/units` commands. Canonical frontend routes `/workspace/properties`, `/workspace/properties/{id}` and `/workspace/units/{id}` are provided. Unit detail requires explicit workspace/property context and the authorized single-space API.

## Limits and next work

Archive checks currently cover child hierarchy only. No tenancy, financial or asset obligations can be created by this module; integration must add authoritative obligation checks before connecting those domains. Activation remains unavailable until ownership/authority, operating/signing/payee policies, meters, inventory and occupancy/finance prerequisites exist. No availability flags or historical tenancy records are fabricated.

Mandatory FS-08 authority/policy/approval workflows, FS-09 catalogue/assets/movements/assessment matrix/handover, unit media, effective-dated charge configuration, shared-policy approval, tenancy/allocation history, readiness notifications and property dashboards remain tracked in the requirement matrix. Existing audit retains actors and action summaries; complete before/after version snapshots and export are still pending. This ADR does not declare property management, Phase 2 or production readiness complete.

## Verification and recovery

`PropertyIntegrationTest` exercises actual PostgreSQL migrations, owner scope, cross-property parents, stale/missing versions, duplicate commands, last-slot races, quota release/restore and an injected audit-write failure rolling back all inventory/capacity effects. `web/tests/property-management.mjs` exercises the real local stack through captured SMS login, property/unit forms, unit detail, persisted edits, configuration readiness, lost-response recovery, archive/restore usage, API failure recovery, keyboard confirmation, axe and mobile reflow. Full-suite results are recorded in release status.

After a network/503 failure retry the retained exact command/key; do not create a new command to guess the outcome. For CAPACITY_RECONCILIATION_REQUIRED inspect commercial allocation/event evidence and transaction/audit logs; no automated destructive counter reset is provided. Migration forward recovery and backups follow existing runbooks. No financial data or historical migration is rewritten.
