# ADR 0019 — Explicit sandbox email link to a mobile account

Status: Accepted for local sandbox; live identity and account-merging policy remain pending.

## Decision

Support adding one unused email to the currently signed-in sandbox mobile account. Require recent authentication from the durable account-session registry, a separate expiring email proof, the exact profile ETag, and explicit confirmation. The account ID remains unchanged, preserving existing workspace contexts and any account-owned records. Replacement, unlinking, adding a mobile to an email-only account, and merging existing accounts are not implemented.

V24 binds linking challenges to purpose, account, originating session and profile version. Public sign-in verification accepts only sign_in challenges. Link confirmation accepts only the bound link_email proof; a generic email sign-in proof cannot authorize linking. Contact locks shared by sign-in creation and linking serialize ownership claims. Database uniqueness remains the final guard. Existing-contact conflict is disclosed only after proof consumption and never transfers account ownership.

Recent session authority is checked before request, confirmation and idempotent replay; a shared registry-row lock serializes revocation during the command. Account-row locking plus strong ETag prevents silently accepting a changed profile. Code expiry/attempt limits reuse the established sandbox policy. Expected invalid-proof failures commit attempts; infrastructure/audit failures roll back link, proof consumption, command and security notice together. Confirmation fingerprints use HMAC so persisted replay metadata does not expose a brute-forceable plain hash of the six-digit code.

Successful confirmation atomically records the existing identity command, audit and in-app security notification. Same key/body recovers the original result; changed confirmation conflicts. UI keeps uncertain submissions in memory, warns on unsaved work, clears state on account changes, and never writes codes to browser storage. Local filesystem delivery remains explicitly simulated.

## Limits and evidence

AUTH-001 / MARKET-001 / FS-01/22 remain Partial. Production email/mobile providers, external security notices, compromised/lost-contact recovery, two-account merging, internationalized address policy, federation and optional push remain incomplete or conditional. Existing sessions are not automatically ended by adding a contact; account security retains explicit session revocation. Original mobile sign-in remains available.

FoundationIntegrationTest covers preserved account/workspace access through the linked email, proof-purpose and cross-actor denial, stale profile, conflicts, idempotency, recent sign-in, concurrent ownership claims and audit rollback. Additional boundaries cover expiry and attempt lockout. Browser tests exercise both confirmations, lost successful response, single security notice, accessible/mobile UI and return through linked email. See release status for actual command results.

## Follow-up: explicit mobile link (2026-11-09)

V25 extends the same use case to adding an unused Indian mobile to an email-only account. IdentityLinks/IdentityLinkController now serve both directions; existing email-link payloads that omit channel still default to email. Purpose and channel must agree at the database boundary. Mobile sign-in creation and mobile linking share the contact lock, preserving unique ownership. A linked mobile exposes matching invitations for explicit acceptance without granting membership automatically. No replacement, unlinking or existing-account merge is enabled. See DEC-039 and the current release checkpoint for verification.
