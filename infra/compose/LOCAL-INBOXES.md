# Local verification inboxes

Run `./scripts/compose-local.sh up -d --build --wait` from the repository root.

- Login: http://localhost:3100/login
- Email: http://localhost:18025 (Mailpit)
- Mobile: http://localhost:8026 (LMT SMS simulator; refresh to see messages)

Choose the channel, enter test contact details and request a code. Open the corresponding inbox, match the destination and challenge reference, then enter the six-digit code in LMT. Repeat with the same contact to sign in to the existing account. Codes remain session-bound, single-use and expire five minutes after request. No external email or carrier SMS is sent. All inbox readers on this computer can see test codes; never use these inboxes for real contact verification.

Mailpit captures email using its documented HTTP send API (SMTP is not published). The dedicated Node SMS container stores messages in SQLite with a unique challenge reference and rejects changed-body replay. Both inboxes have named persistent volumes; SMS retention is six minutes and Mailpit retention is one hour; expired codes cannot authenticate even if pruning has not run yet. Inbox ports bind only to loopback. Native non-Compose development retains the private file inbox. The live overlay disables container delivery and excludes inbox services unless explicitly selected.

The Java worker commits and audits a dispatch before calling an inbox, then records its outcome in a separate transaction. HTTP calls have bounded timeouts and response sizes. Lost submissions become `unknown` after 30 seconds and are not blindly resent; inspect the inbox using the reference, or request a new code. Explicit rate-limit rejection has bounded retries. Container delivery never writes a second plaintext code into the private file inbox. Restart with `up -d --wait`; do not remove database volumes to recover a delivery failure.

Verification: `node web/tests/signin-channels.mjs` exercises registration and returning-account login for both channels at desktop and mobile widths using codes read from these actual containers. Existing Compose lifecycle tests share `web/tests/container-code.mjs`.

Mailpit API and image sources: https://mailpit.axllent.org/docs/api-v1/ and https://mailpit.axllent.org/docs/install/docker/. Mailpit v1.32.0 is digest-pinned; SMS uses the repository's pinned Node image without new packages.
