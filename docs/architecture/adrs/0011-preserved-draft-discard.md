# ADR 0011: Terminal draft discard with retained evidence

Status: accepted for a bounded Phase 1 draft-lifecycle subset, 2026-11-08. Full commercial configuration and plan administration remain Partial.

## State and authority

V13 adds `discarded` to plan-version state and protects both published and discarded versions against updates/deletion through the existing immutability trigger. Discard is a terminal transition from a source-backed unpublished draft. It is not deactivation of a published offering, data deletion or a subscription change. Terms, source provenance, edit evidence and publication reviews remain intact.

A false-by-default `can_discard_draft` grant is independent of review, creation, editing and publication. Grant changes are audited. The local sandbox operator must explicitly choose `--allow-draft-discard`; existing grants do not gain authority automatically. Every command/replay checks current authority and expiry.

## Command, concurrency and recovery

POST `/admin/plan-versions/{id}/discard` requires CSRF/Origin, a current ETag, reason and actor-scoped idempotency key. Actor and audience/code locks, followed by the candidate row lock, match edit/publication lock order. Only a still-current draft can transition. Publication and discard cannot both succeed: whichever commits first makes the other ineligible.

The state/revision change, original terms snapshot, exact confirmation, result/reason and domain audit commit atomically. Audit failure restores the original state/revision with no command result. Same-key retry returns the committed result even though the draft is terminal; changed confirmation conflicts. Runtime has no command-evidence update/delete privilege.

Discarded records retain their version number and effective instant. The existing audience/code/effective-time uniqueness rule continues to include retained evidence; a new proposal must choose a distinct instant. No silent date/version reuse or restoration is introduced. Correct direction by preparing a new draft from the latest published source.

## UI and verification

Authorized reviewers see **Discard draft** only for eligible draft state. Confirmation identifies plan/version/terms/date, explains permanence and evidence retention, and requires a reason. The UI focuses the review, blocks duplicate submission and retains the exact command after an uncertain response. Discarded rows remain visible with textual state and without edit, publish or discard actions. Server enforcement remains authoritative.

`DraftDiscardIntegrationTest` verifies independent authority, CSRF, missing/stale preconditions, injected audit-failure rollback, duplicate concurrent replay, changed confirmation, retained terms/audit, publication-review rejection, public exclusion, immutable discarded rows, revoke denial and a real discard/publication race. Desktop/mobile browser tests create their own labelled sandbox drafts, lose a successful response, retry once and verify retained state with no mutation controls.

Published deactivation, broader financial/feature/quota configuration, subscriber migrations, retention policy, production administrator governance and restore drills remain pending. This slice does not complete FS-03/06 or Phase 1.
