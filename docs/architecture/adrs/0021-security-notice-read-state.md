# ADR 0021: Recipient-owned security notice read state

Status: Accepted for the implemented account-security subset.

FS-01/22 require useful in-app security notifications and recipient-isolated read state. Extend the existing identity-owned account_notification records; do not introduce a competing notification store. GET `/me/notifications` is the canonical account inbox projection; `/me/security-notifications` remains a compatible alias. This subset contains account security events only, not workspace/tenancy delivery or preferences.

V27 adds nullable first-read time, positive version and an account/id paging index. Historical notices remain unread: delivery or sign-in cannot establish review. Runtime UPDATE privilege is limited to read_at/version; RLS and explicit recipient predicates apply. Existing content/time is preserved. Read status is not legal acceptance, consent or proof of external delivery.

POST `/me/notifications/{id}/read` requires CSRF, strong notice If-Match and actor-scoped Idempotency-Key. The existing command lock serializes identical retries; row locking serializes competing updates. A new stale command fails 412; an exact replay returns its original result. A current-version read of an already read notice preserves the timestamp and produces no second read audit. Changed key payload fails 409. First read, version, audit and command commit atomically; no notification is created about reading a notification.

The UI waits for the server, reloads authoritative totals and preserves the same command after an uncertain response. Scope changes/unmount abort outstanding reads and commands. Counts cover all owned records within the list transaction, not just a page. Existing bounded actor-bound UUID pagination is retained; ordering is stable by ID, not presented as chronological.

Verification: FoundationIntegrationTest covers wrong recipients, CSRF, missing/stale preconditions, replay/conflict, concurrent reads, rollback and pagination counts. notification-read.spec.ts covers real persisted UI read/reload and a lost response, keyboard activation, responsive reflow and axe. Preferences, tenant/workspace inbox navigation, delivery providers, external notifications and general notification rules remain incomplete.
