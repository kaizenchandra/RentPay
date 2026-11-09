# ADR 0008: Unpublished editorial plan drafts

Status: accepted for a bounded Phase 1 authoring subset, 2026-11-08. Extends ADR 0007; SAAS-ADMIN-002 and SAAS-PLAN-003 remain Partial.

## Scope

A reviewer with explicit `can_create_draft` authority may create a new unpublished version from a published source. This slice edits display name, commercial disclosure and proposed effective instant only. Audience, code, prices, currency, capacity and baseline classification are inherited server-side. It creates no new plan family, capabilities, add-ons, eligibility rule, subscriber migration or public offer. Publication and financial configuration remain separate pending commands.

The canonical `POST /admin/plans/{id}/versions` uses the immutable source version UUID as `id` in the current version-backed catalogue. The response identifies both source and draft. OpenAPI 0.8.0 documents that interpretation; a future Plan aggregate must preserve compatibility explicitly.

## Authority and transactions

V10 adds draft authority to the existing expiring commercial grant, false by default. Grant audit includes that flag. The local operator must supply `--allow-draft-creation`; ordinary review-grant issuance removes write authority. Revocation also clears the flag. Existing grants remain read-only.

The command takes the actor's advisory transaction lock, rechecks active write authority, then reads immutable source terms. Grant updates use the same advisory lock so revocation and command execution serialize. A second lock, scoped to audience/code, serializes version-number allocation across different editors. Distinct effective instants are required by the existing database uniqueness constraint. Proposed effective dates must be future instants for new commands; replay is resolved first so elapsed time cannot invalidate a previously committed result.

Exact source ETag, session/Origin/CSRF and a UUID idempotency key are required. Actor/key stores the source, exact typed request, result ID and reason. Same confirmation returns its original result; changed terms/source conflict. The new draft, command evidence and domain audit commit atomically. There is no provider side effect or publication outbox to recover.

Runtime gains INSERT on plan versions, with a database trigger restricting its inserts to draft state. It still cannot update/delete published terms or issue commercial grants. Draft command evidence has no update/delete runtime privileges. Public catalogue queries continue to exclude drafts; subscriptions continue referencing their original versions.

## UI and recovery

The review projection exposes `canCreateDraft` and source ETag. Published rows show an authoring action only for eligible reviewers. The form uses React Hook Form/Zod with linked errors and an explicit-offset ISO timestamp, previews exact proposed text/date/reason, and confirms inherited prices. Inputs are kept in memory; unsaved navigation is guarded.

After an unknown response, the form freezes the exact confirmation/key and offers same-command retry. Subsequent errors do not discard an uncertain command. A successful response announces the persisted draft ID and refreshes the reviewed versions. The draft is not represented as scheduled publication. Grant/session failure remains explicit; there is no claim to recall previously viewed data.

## Evidence and gaps

PostgreSQL tests exercise absent write authority, CSRF, missing/stale ETag, duplicate concurrent submission, changed-body conflict, duplicate effective date, past dates, concurrent editors, monotonic distinct version allocation, inherited money/capacity, public exclusion, audit and revoked access. Runtime attempts to insert a published plan fail. Browser tests create real sandbox drafts and recover a deliberately lost response without duplication. Sandbox test drafts are labelled; they remain unpublished.

Still pending: price/tax/feature/quota editing, existing-draft updates/discard, publication review/activation/deactivation, approval policy, full immutable-contract transition tests, production administrator assurance, retention/notifications and restore drills. This slice does not satisfy the complete FS-03/06 administration contract.
