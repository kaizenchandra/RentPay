# ADR 0009: Reviewed editorial publication and effective dates

Status: accepted for the Phase 1 editorial publication subset, 2026-11-08. Extends ADRs 0007/0008; full SAAS-ADMIN-002 and SAAS-PLAN-003 remain Partial.

## Authority and exact review

V11 adds `can_publish`, false by default, to commercial grants and grant audit. Draft authority does not imply publication. The sandbox operator script requires `--allow-publication` explicitly; omission removes that authority when reissuing a grant. No production administrator assurance or second-approver policy is invented.

GET `/admin/plan-versions/{id}/activation-preview` creates a durable actor-bound five-minute review and audit record. It is a review read, with no public or financial transition. The response compares the published source and exact draft, including names, disclosures, dates and inherited terms. The review carries its source, draft revision and expiry. POST `/admin/plan-versions/{id}/activate` requires publication authority, CSRF/Origin, exact If-Match, review ID, reason and an actor-scoped idempotency key.

V11 adds a database-maintained row revision. Strong ETags include it, so direct draft changes also stale a review. Existing source confirmations made before this migration must refresh their ETags. Published terms remain immutable; revision changes cannot bypass their guard.

## Safe publication subset

Only drafts with durable source provenance are eligible. Audience, code, currency, prices, allowances and baseline classification must equal the source. The source must be the latest published version, including future scheduled versions, and the proposed effective instant must be in the future and after that source. Thus publication extends an ordered timeline; competing drafts based on the same source cannot both be published. Unsupported financial/feature changes fail closed with `APPROVAL_STALE` rather than silently altering commercial terms.

Actor advisory locks serialize grant changes and command replay; the shared audience/code lock serializes draft numbering and publication. The candidate row is locked while validating and committing. Authority and review expiry are rechecked after waits. Exact ETag/review revision protects reviewed content. A replay is resolved before expiration checks, preserving a committed result even after the review expires, but current authority is still required.

Publication, result/reason journal, audit and inherited capability mappings commit together. `EditorialPlanCapabilities` is the commercial module's explicit transaction-mandatory interface for copying mappings from an unchanged source. Catalogue code does not write capability rows directly. Existing subscription references are untouched. New Free provisioning uses the effective published version and its inherited membership capabilities.

Runtime may update only the plan's publication-state column; published update/delete protection remains. Draft INSERT cannot directly establish publication. There is no provider side effect or asynchronous success claim. The version becomes selectable by the existing public query at its effective instant; no job is required. Public/admin fetches are already no-store, so there is no shared cached catalogue to invalidate.

## Frontend and recovery

The private version review shows publication actions only for eligible actors. The review screen displays source versus proposal, effective time in Asia/Kolkata, inherited prices/allowances, scope, no-payment disclosure and the reason before confirmation. Publication success reports the effective instant; it does not claim immediate activation of a paid subscription. Unknown-response recovery retains the exact command/key. Expired or conflicting reviews must be closed and refreshed; a changed timeline requires a new draft based on the latest published source.

Published records cannot be undone or rewritten by this UI. Correct editorial terms with a new source-backed version. Editing/discarding existing drafts, deactivation, financial configuration, feature/quota changes and subscriber migration remain pending. Review/command retention and production approval governance remain open decisions.

## Verification

The isolated PostgreSQL publication test covers separate authority, actor-bound review, expiry, CSRF, missing/stale ETags, changed draft revision, competing editors, one publication audit, duplicate/changed command behavior, revoked access, immutable published terms and inherited capabilities. It crosses the actual effective-date boundary: public selection changes, a new workspace receives capabilities and an existing subscription retains its old version.

A trigger-injected audit failure verifies atomic rollback of publication, copied grants and command evidence, followed by successful retry. A committed command can replay after its review expires. Desktop/mobile browser tests create labelled future-effective sandbox editorial versions and deliberately lose the committed response before retrying. These are local database publications, not live paid offers or provider integrations. Full restore drills and production readiness remain unverified.
