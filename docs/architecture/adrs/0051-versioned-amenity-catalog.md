# 0051 — Workspace amenity definitions and retained identities

Status: Accepted, 2026-11-10.

The inventory module owns workspace amenity definitions independently of installed inventory. V52 seeds the source-defined 25 editable definitions during provisioning and backfills existing workspaces. These are configuration, not claims that assets exist. Physical serialized/quantity items are distinct from availability-only features and services.

Stable codes cannot change. Names and aliases reserve normalized spellings within a workspace; V53 preserves these reservations after rename or retirement so historical references cannot be reassigned to another item. Current definitions have immutable version snapshots and audited idempotent commands. Update preconditions, workspace serialization and database constraints protect concurrent aliases and stale reviews. Audit failure rolls the entire change back.

Current administration requires workspace owner membership and the central property-edit entitlement. This does not grant signing authority or implement property delegation. The UI exposes the latest 50 revisions; older snapshots remain durable. Installed assessments, assets, quantities, shared ownership, movements and handover snapshots remain separate pending requirements. No deletion API is provided.

Evidence: AmenityCatalogIntegrationTest covers scope, concurrent aliases, immutable identity/history, audit rollback and replay. amenity-catalog.mjs covers real UI/API persistence, lost responses, retirement, historical spelling protection and keyboard/mobile/desktop accessibility scans.
