# ADR 0025: Email recipients in the existing invitation workflow

Status: Accepted for sandbox workspace onboarding.

FS-02 takes an invited contact and requires explicit recipient acceptance. Email-only accounts existed but could not receive manager invitations. Extend the single WorkspaceMembership workflow; do not create a parallel email membership service or implicitly link identities.

V30 permits exactly one mobile or email per workspace_invitation, adds an email-recipient index and extends participant RLS to the account's linked email. Mobile rows and existing invitation/command evidence remain intact. The API accepts the existing {mobile} request or {email}, never both. Identity's existing normalization contract is reused: ASCII local-part spelling is preserved, domain case is normalized. The old mobile request fingerprint is retained for replay compatibility; email fingerprints include a channel prefix.

Invitation responses retain maskedMobile (null for email) and add channel/maskedDestination for safe rendering. Owners can review the exact entered target before creation. Recipients see only invitations matching a current account contact; this confers no workspace membership until explicit acceptance. Email-only accounts do not receive a fabricated mobile number. Separate mobile/email accounts are never merged by invitation matching.

Workspace authority, sandbox identity mode, entitlements, expiry, strong versions, scoped idempotency, membership locks and manager-capacity transactions are reused. Pending invitations reserve no slot. Email and mobile acceptances compete for the same allowance; an invitation does not confer property/signing/spending/payout authority. Already-active membership cannot be overwritten by a second channel. Revocation preserves history and releases capacity through the existing commercial interface.

No email or SMS is sent. This implements the durable in-app invitation and acceptance journey, not provider delivery or verified real contact ownership. Broader manager delegation/property scope and production identity/provider policy remain pending.

FoundationIntegrationTest covers normalization/replay, recipient API and runtime RLS isolation, explicit acceptance, no owner/admin privilege, mixed-channel last-slot concurrency, invalid/both-channel requests, pending duplicates, expiry/revocation and database contact constraints. email-invitation.spec.ts covers lost creation response, one persisted invitation, email sign-in, explicit acceptance, quota display, revoke/denied access, keyboard and responsive/axe checks; existing mobile flows remain in full regression.
