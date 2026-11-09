# ADR 0020: Current-session sandbox reauthentication

Status: Accepted for local sandbox; production provider policy remains pending.

AUTH-001 / MARKET-001 (FS-01/22) require recent proof for sensitive account operations. Signing out was previously the only recovery when the recent-authentication interval elapsed. Add `/account/reauthenticate` and identity-owned challenge/verification use cases without creating another account or changing contacts.

The server selects an existing contact. V26 binds the challenge to purpose, account, current session and profile version. Existing expiry, attempt, delivery and rate limits apply. Request retries retain the same idempotency key and body; verification is single-use. Sign-in and linking cannot consume reauthentication proof. Verification locks the durable session and account, refreshes registry authentication time, rotates the session identifier and clears the CSRF token. The original absolute session deadline remains unchanged; the registry timestamp represents last verification. No new contact or authority is granted.

Challenge consumption, registry update, audit and in-app notice commit together. Servlet session/cookie rotation cannot participate atomically in that database transaction: an audit/database failure can leave a rotated but unregistered session, requiring sign-in. This fails closed. A lost successful response can likewise require sign-in; the UI offers a session check and never replays the proof as if verification were idempotent. Sandbox delivery remains the private inbox, not real contact verification.

FoundationIntegrationTest covers restored recent authority, old-cookie rejection, stale CSRF, actor/purpose/profile isolation, invalid/expired proofs and audit rollback. Browser tests exercise aged-session recovery, schema conformance, keyboard submission, mobile reflow/axe and lost-response guidance. Contact replacement/unlinking, compromised-contact recovery and live notification/provider policies remain separate incomplete work.
