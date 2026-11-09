# ADR 0024: Protected recovery outcome reads

Status: Accepted for sandbox recovery continuity.

A completed recovery may revoke old sessions while the response carrying a new cookie is lost. The user needs durable outcome evidence without repeating proof or requiring a cookie they did not receive. Extend the existing recovery challenge rather than introduce another recovery engine.

V29 adds recovery_completed_at with a constraint requiring the recovery purpose and consumed proof. The identity recovery transaction writes it with generation/revocation/audit/notice. Existing successful recovery rows are backfilled only from committed identity.account_recovered audit evidence. Consumed unknown-contact or rejected proofs are not completion.

Recovery request responses now include a challenge-specific HMAC outcomeToken and expiry fifteen minutes after the request. Derivation is domain-separated from OTP codes, incorporates the original session binding and uses the server secret. It is a narrow bearer read grant, not identity or a session credential. Request replay returns the same grant. The page retains it only in memory; reload loses it. No grant is added to general challenge-status responses.

POST `/auth/recovery/outcome` takes the grant in a JSON body, keeping it out of URLs. Origin/CSRF protection and no-store still apply, including with a fresh anonymous cookie. It requires the matching unexpired grant on every read and returns only challenge ID, coarse state and completion time. It exposes no identity/contact/workspace data and cannot sign in, revoke sessions or replay proof. Incorrect, cross-challenge, expired and missing grants use privacy-safe errors. The endpoint is unavailable outside sandbox configuration.

States distinguish completed, not_completed, failed, expired and locked. A completed result is durable transaction evidence, not proof of browser-cookie delivery. A not_completed read cannot rule out an in-flight request subsequently committing and does not cancel it. The UI offers manual checks and ordinary sign-in/session-check links; it never repeats recovery based on a status read. The fifteen-minute limit is a local read-grant safety bound, not an approved production retention promise.

PostgreSQL tests cover cross-grant denial, expiry, stable retry, post-cookie-loss read, absence of account authority, failure/expiry states and completion constraints. Existing audit rollback tests now check absent completion evidence. Browser regression clears all cookies after deliberately losing a successful response, then verifies completion through the real outcome API while ordinary account access remains denied. Other contact/privacy/provider requirements remain incomplete.
