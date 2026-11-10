# 0034 — Owner invitation outcome notices

Date: 2026-11-10. Status: Accepted for in-app manager invitation responses.

## Decision

FS-02 and FS-22 require invitation events, private recipient delivery and safe recovery. On acceptance or decline, notify the original inviting owner only if their owner authority remains active. Use the existing membership transaction and workspace lock, server-derived inviter, and current owner predicate. Declining does not require the inviter to retain authority: complete the decline but audit suppressed delivery when authority is revoked. An exact replay never backfills a previously suppressed notice after authority is restored.

Reuse the identity-owned `WorkspaceAccessNotices` transaction-mandatory interface with a typed invitation-outcome method; internal persistence restores caller account scope. A deterministic invitation outcome event and existing account/event uniqueness prevent duplicate notices. Response state, quota/membership effects, recipient access notice, owner outcome notice, command result and audit commit together. Database failure returns the existing retryable 503 and rolls all effects back. There is no network operation or second queue in this transaction.

V38 adds `workspace_invitation`; OpenAPI 0.36.0 regenerates the client enum. Existing account/tenant/workspace notification pages label it Invitation outcome and preserve read/filter controls. Notices contain a workspace snapshot and invitation reference, without the invitee's contact or profile. Text describes the outcome historically; it does not promise continuing access. The Your workspaces link resolves current authorized contexts. Retained account history does not grant workspace access.

These direct responses to invitations are essential in-app operational notices, independent of optional setup preferences and quiet hours. Live invitations and external messaging remain separately gated. Pending/expired invitations, proactive expiry reminders and other lifecycle notifications are not implemented by this change.

## Evidence

FoundationIntegrationTest exercises outsider denial, acceptance/decline replay, recipient privacy, revoked inviter suppression, no backfill on replay after restoration, and injected notice-write failure rolling back both membership/capacity and recipient notice before safe retry. The existing full foundation suite covers quota races and membership isolation. Extended `workspace-access-notices.mjs` verifies desktop/mobile decline with a lost response, owner decline/accept outcomes, read-status persistence, privacy, recipient grant/revoke notices and keyboard/axe/reflow. See release status for executed results.
