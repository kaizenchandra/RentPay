# ADR 0018 — Independent sandbox email identity

Status: Accepted for local sandbox only. Live email provider and identity-linking policy remain pending.

## Decision

Extend the existing challenge/session flow with an email channel. Request/verification, HMAC code generation, expiry, attempts, delivery queue, audit, session rotation/revocation and CSRF stay in the Java identity module. The existing private local inbox is explicitly simulated delivery: no email or SMS is sent, and a sandbox code does not prove real mailbox ownership. Non-sandbox identity remains unavailable; sandbox verification cannot authenticate an existing live-mode identity.

V23 preserves existing mobile accounts/challenges and permits email-only accounts. Email is unique; verified proof creates or selects exactly that contact's account. Concurrent proofs converge through the database uniqueness constraint. Email and mobile contacts are not matched by display name or other unverified attributes. No automatic linking/merging, mobile-invitation transfer or account recovery by a different contact is introduced.

The current supported email subset is ASCII, max 254 characters with local part max 64, no quoted local parts, whitespace, leading/trailing/consecutive local dots or domain literals. The domain is lowercased, while local-part case, dots and plus suffixes are retained. This intentionally avoids silently merging distinct identities. Internationalized addresses and alternative normalization need a reviewed policy/provider contract. Inputs are revalidated in Java; browser validation grants no authority.

Challenge requests bind to a session and normalized contact, with actor-session/key replay and five requests per destination/session per fifteen minutes. Codes expire after five minutes, permit five failed attempts and are single-use. Request/status responses mask destinations and do not reveal account existence. Verification failures commit attempt evidence; infrastructure failures roll back. Profile projection supports nullable masked mobile/email contacts. Existing mobile manager invitations remain mobile-based and do not attach to email accounts.

## Verification and remaining work

FoundationIntegrationTest covers existing mobile behavior plus email normalization/replay, session binding, returning workspace access, concurrent account creation, malformed/expired/locked challenges, request throttling, no mobile cross-account access and sandbox/live isolation. email-identity.spec.ts exercises sandbox email OTP, Free provisioning, profile, logout and return to the same workspace on desktop/mobile. See release status for actual results.

MARKET-001 and AUTH-001 remain Partial. Live delivery/provider failure handling, verified identity linking/merge recovery, changing contact with reauthentication, external security notifications, federation and optional push remain incomplete or conditional. This does not establish real email, residence or police verification.

Follow-up: ADR 0019 now enables the narrow, explicitly confirmed addition of an unused email to a mobile account. It does not permit automatic matching, replacement or merging.
