# ADR 0027: Remove a contact only after retained-contact proof

Accepted for local sandbox, 2026-11-09. AUTH-001 / MARKET-001, FS-01/22 remain Partial.

A generic recent sign-in cannot authorize removal of another identity contact. V31 stores the channel and profile version of successful current-session reauthentication. Removal requires both contacts, fresh proof through the contact being kept, and the unchanged profile ETag. A sign-in or proof through the removed contact does not qualify. Existing single-contact accounts remain usable and cannot remove their last contact.

`POST /me/identity-links/unlink` is account-owned and uses the existing identity command log. Confirmation binds channel, profile version and actor; same key/body recovers the original response and a changed body conflicts. The actor is derived from the authenticated session. The operation atomically clears the selected contact, increments profile/authentication generation, advances the existing authentication invalidation timestamp, invalidates outstanding proofs for either contact, revokes other registered sessions, and records audit/security notice. Current retained-contact session remains usable. No workspace membership is deleted or transferred. PostgreSQL retains its at-least-one-contact constraint.

Account-generation advisory locking serializes removal with linking, reauthentication and session registration. Required current-session and profile rows are locked. Opposing removals cannot remove both contacts. Reauthentication records proof only after consuming a valid session/purpose/profile-bound challenge. Historical proof metadata does not authorize a later profile version.

The removed address/number becomes available for a separate future account through normal sign-in; no implicit merge, transfer or reassignment is performed. This behavior is disclosed before confirmation. Existing historical audit/command evidence is preserved, not erased. Account merge, assisted all-contact-loss recovery, live proof/notification providers and production retention policies remain unresolved. The UI makes no claim of real SMS/email delivery.

Verification: FoundationIntegrationTest checks retained-channel requirement, last-contact protection, preconditions, replay/conflict, invalidated outstanding proof, audit cardinality and opposing-removal race. `web/tests/contact-removal.mjs` exercises real Compose UI on desktop/mobile, linking and reauthentication, uncertain-success recovery, session revocation, persistence and axe/keyboard/reflow.
