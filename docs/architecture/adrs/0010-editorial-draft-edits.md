# ADR 0010: Audited edits to unpublished editorial terms

Status: accepted for a bounded Phase 1 draft-lifecycle subset, 2026-11-08. Extends ADRs 0008/0009; full plan administration remains Partial.

## Scope and authority

PATCH `/admin/plan-versions/{id}` changes only a source-backed draft's display name, disclosure and proposed effective instant. It cannot alter source provenance, plan code/audience, prices, limits, currency, classification or capabilities. Published versions cannot be edited.

V12 introduces `can_edit_draft`, false by default, with grant audit. Creation and publication permissions do not imply editing. Local operators must explicitly select `--allow-draft-editing`; ordinary reissuance without it removes edit authority. Production identity/governance requirements remain open.

## Transactions and recovery

`PlanDraftEdits` takes the existing actor authority lock and audience/code lock, then locks the candidate row. It rechecks current edit authority, draft state, strong ETag, future effective instant and effective-date uniqueness. It shares the publication lock order: if an edit wins first, the existing database revision trigger invalidates prior publication reviews; if publication wins, editing fails without changing the published evidence.

The transaction stores immutable-runtime before/after projections, the reason, exact typed confirmation and original result alongside the domain audit. Actor/key idempotency replays that result before checking the current revision, while current authority is always required. A changed confirmation conflicts. An audit/journal failure rolls back terms and revision. No provider or public-catalogue side effect occurs.

Runtime gains UPDATE only on the three editable columns. It still cannot change financial fields or modify/delete command evidence. The existing published-version trigger remains authoritative. Source creation evidence is never rewritten when the draft changes.

## UI

The existing private review routes show **Edit draft** only with explicit eligibility. The shared RHF/Zod form preloads the persisted terms, exposes a current-terms disclosure for comparison, focuses the heading, confirms the exact proposal/reason and warns that publication reviews become stale. Input stays in memory and unknown-outcome retry preserves the exact body/key. Successful save reports a recorded command and refreshes current state, avoiding a claim that a replay's historical draft state is still current.

Stale editing cannot silently overwrite another editor: close the old proposal, refresh and review the current terms. Financial/feature configuration and draft discard remain unavailable. This is not a full generic plan editor.

## Verification

`DraftEditIntegrationTest` verifies independent authority, CSRF, missing precondition, past-date rejection, injected audit-failure rollback, same-key replay, changed-body conflict, before/after evidence, publication-review invalidation, concurrent editor conflict, preserved inherited money/capacity, public exclusion, published-edit rejection and revoked access. The browser test edits a real sandbox draft, loses a successful response and retries to exactly one new revision on desktop/mobile.

Remaining work includes draft discard, deactivation, full financial/feature/quota configuration, conflict-merge UX, production governance, retention/notifications and restore drills. No phase or full SAAS requirement is declared complete.
