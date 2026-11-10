# 0036 — Explicit expired invitation replacement

Date: 2026-11-10. Status: Accepted.

## Decision

Add owner-reviewed `POST /api/v1/workspaces/{w}/invitations/{id}/replace-expired` to recover from expiry without retyping or exposing the saved contact. This is a new invitation, not an extension, reactivation or acceptance of the original. Existing sandbox/live and centralized member-add eligibility gates apply. Pending invitations reserve no capacity; acceptance independently validates current quota and authority.

The backend derives the contact and channel from the authorized original. It requires current owner authority, strong If-Match and scoped idempotency. Exact replays return the committed replacement after checking current authority; they do not require creating a second invitation. The workspace advisory lock serializes replacement with membership/expiry commands. Only expired deadlines in pending/expired state qualify; declined/revoked/accepted/unexpired records cannot be reused. Already-active membership or another pending contact invitation follows existing conflict rules.

A private common creation routine applies the same contact, pending-invitation and member checks without sharing the public create endpoint's idempotency namespace. V40 persists `replaces_invitation_id`, unique per original, with same-workspace foreign-key lineage and a self-reference prohibition. These constraints backstop concurrent different-key requests. Existing command result and audit commit with the new record and lineage. The original is retained unchanged and remains unusable for acceptance.

OpenAPI 0.38.0 includes the new operation and nullable lineage in invitation responses. The UI shows Review replacement only on expired owner records; confirmation identifies the masked stored contact, workspace, duration, scope and lack of external messaging/capacity reservation. Cancellation has no effect. Network uncertainty retains the exact command for retry. Replacement records carry a history explanation; another replacement attempt on the same original returns an actionable conflict.

## Evidence and remaining work

FoundationIntegrationTest covers unexpired rejection, owner/recipient/outsider scope, missing/stale preconditions, competing keys, one replacement, no allocation before acceptance, mobile/email preservation, exact replay and changed-confirmation conflict. The Compose expiry journey checks confirmation cancellation, lost-response retry, one durable lineage record, mobile layout/accessibility and recipient acceptance.

General expiry-worker diagnostics, pre-expiry reminders and further invitation/provider features remain incomplete. This action does not provide live SMS/email invitation delivery or change any paid-plan configuration.
