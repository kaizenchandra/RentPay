# ADR 0002: Persistent sessions and atomic sandbox workspace provisioning

Status: accepted for the local Phase 1 slice; live identity and full Phase 1 remain incomplete.

## Decision and scope

Use Spring Session JDBC under the Spring Boot 4.0.9 dependency BOM rather than a custom session store or browser tokens. Framework schema is applied by Flyway V2. Session cookies contain opaque IDs, are HttpOnly, SameSite=Lax and Path=/ with no Domain. The default environment uses Secure `__Host-lmt_session`; the explicit `local` sandbox uses `lmt_dev_session` on loopback HTTP. Sandbox requires the local profile alone, a server loopback binding, a secret of at least 32 characters and a private inbox. Mixed production/local profiles or non-loopback sandbox binding fail startup. No live authentication adapter exists; default identity requests fail unavailable, and sandbox authenticated state is not accepted outside that mode.

Spring Security owns CSRF using session-bound tokens. All mutations require an allowlisted Origin in addition to CSRF, including challenge request/verification. The web client fetches `/auth/csrf` before a command; login rotates the session ID and clears old CSRF state. No password/user-admin bootstrap exists. Unknown DTO fields are rejected, rather than accepted as role/paid-state grants.

Session idle limits: five minutes before authentication and thirty minutes after authentication. Absolute authenticated lifetime: twelve hours, checked on each request. These are initial engineering policies, not commercial promises. Logout invalidates the persisted session. A repeat mobile proof returns the existing account without replacing its name. Account recovery currently means proving the same mobile again; lost-number recovery, contact changes, identity linking, full device management and approved live SMS remain pending.

## Identity and delivery

Local codes are six digits, expire after five minutes and allow five attempts. At most five challenge creations per mobile OR browser session are accepted in fifteen minutes. These explicit sandbox limits are not a production anti-abuse policy. Idempotency binds challenge request keys to the anonymous session and normalized inputs. Challenge status and verification also bind to that session; UUID possession is insufficient.

The challenge and delivery intent commit together. A Java worker locks bounded pending delivery rows, derives the same code using HMAC(challenge ID, server secret), writes only the private inbox and commits delivery status. The outbox never contains a raw code; neither logs nor HTTP responses expose one. Delivery can repeat safely after a crash. File permissions are owner-only, retries are bounded, and expired code files are removed on a one-minute sweep after six minutes. Challenge/audit database retention is a pending production privacy decision. Development data can be removed only through an explicit operator reset, not as migration recovery.

Verification serializes on the challenge row. Wrong attempts commit their counter and audit; expired, consumed and locked proofs cannot authenticate. Unique mobile/account constraints prevent duplicate identities across simultaneous challenges. A consumed proof is never replayed after session persistence failure: request another proof, which safely finds the existing account. No delivered file is represented as a live SMS or independently verified real phone.

## Ownership, authorization and transactions

| Module | Owned state / interface |
|---|---|
| identity | User account, auth challenges and delivery; ChallengeService; Spring Session integration |
| workspace | Workspaces, memberships, consent, settings, setup query, provisioning command records and welcome delivery |
| commercial | Existing catalog remains in `catalog`; FreeProvisioning owns subscription and initial grant writes. No duplicate catalogue engine. |
| orchestration | Coordinates WorkspaceCommands + FreeProvisioning + identity read + audit in one transaction |
| shared | Technical ApiProblem/error response and append-only audit insert helper; no universal business service |

`POST /workspaces` validates a signed-in sandbox account, exact current Free version and sandbox-use-v1 acknowledgement. One transaction commits workspace, active owner membership, settings, consent evidence, Free subscription, initial capacity grants, idempotency result, audit and welcome outbox. Actor-scoped advisory locking plus unique command keys prevent duplicate retry effects. A changed payload conflicts; a stale/non-Free plan rolls back the whole transaction. Idempotency results remain durable; no expiry policy is invented. Revoked owners cannot retrieve an old command result.

Initial grants copy versioned plan limits. They do not enforce unit creation (not implemented), and they do not grant authority by themselves. Owner identity does not consume a manager slot in this development baseline; shared-bed/counting policy remains unresolved. Personal membership is not provisioned or inferred from a workspace subscription.

The runtime role is not table owner or BYPASSRLS. Workspace, membership, settings, consent, subscription, grants, commands and notifications have row policies. Validated actor context is set with transaction-local `set_config` before queries; pooled connections cannot retain it across commits. Application queries additionally require active owner authority for setup. No public write accepts owner IDs or roles. Public plans and identity internals use different access classes rather than attaching every account to a landlord workspace.

A committed workspace event carries event/schema/aggregate/scope/correlation/causation metadata and no profile or OTP payload. The worker rechecks active membership, inserts an in-app welcome message keyed by event ID, and marks delivery in the same transaction. Replay cannot duplicate messages. External welcome delivery, generalized job leases, retry administration and full worker deployment remain pending.

## UI, privacy and recovery

Canonical routes: `/login`, `/register`, `/verify-otp`, `/account`, `/create-workspace`, `/setup?workspace=…`. Context is explicit in the URL per tab; it is never a mutable server-global selected workspace. Private data is loaded through same-origin authenticated, no-store API calls. Private route shells have no-store/noindex headers and contain no private SSR data. Focus/page return rechecks the session, obsolete requests are aborted, and logout broadcasts to other tabs. No OTP, account/workspace data or sensitive draft is stored in localStorage/sessionStorage.

Creation holds the same key and payload while the outcome is unknown; edits are locked until a definitive rejection or success. Navigating away from dirty input warns; successful completion and logout clear the warning. Setup reports only profile/subscription steps complete. Properties, units, tenancy imports, charge configuration and preferences stay visibly unavailable.

API failures carry stable safe problems. A top-level servlet filter also covers persistent-session DB failures outside MVC, returning retryable 503 when the response has not already committed. Test evidence and remaining gates are in `docs/release-status.md`.

## Alternatives and reconsideration

A custom opaque-session implementation would duplicate fixation, revocation and persistence machinery; JWT/localStorage authentication would complicate revocation and expose tokens to browser code. Production SMS/electronic identity needs a selected provider, replay/rate/abuse controls, delivery reconciliation and verified privacy/retention terms before replacing the sandbox. Do not relax the local gate to simulate live verification.

Official references checked: [Spring Session integration](https://docs.spring.io/spring-boot/reference/web/spring-session.html), [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html), [authentication persistence](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/persistence.html).
