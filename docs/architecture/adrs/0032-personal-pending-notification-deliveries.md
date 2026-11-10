# 0032 — Personal pending notification delivery visibility

Date: 2026-11-10. Status: Accepted for the current optional workspace-setup notification scope.

## Context and decision

FS-22 requires delivery status and supported recovery, while the existing account inbox only showed delivered records. Expose `GET /api/v1/me/notification-deliveries` as a read-only projection of the workspace module's durable outbox. Keep the outbox as the sole owner of operation state; obtain current consent through the identity module's existing preference interface. Do not introduce a second queue or direct cross-module writes.

Show only the authenticated event recipient, current active owner membership, applicable current opt-in and events since that opt-in. Include pending/running/failed/blocked operations, excluding delivered/cancelled records. Stable ascending event-ID pages have at most 25 items, an actor/purpose-bound cursor, an authorized total and snapshot time. The cursor selects a page and never grants permission. Counts and records use a repeatable-read transaction. A V36 partial index bounds the recipient query. Queue changes between requests are expected; explicit refresh starts a new snapshot.

The account notification page and its existing tenant/workspace aliases show pending notices separately from delivered history. Quiet-hour deferral explains that the shared notice already exists; failed/blocked entries link to the existing authorized workspace recovery screen. Users can review preferences through the existing ETag/idempotency-protected confirmation flow. No read action changes delivery, consumes attempts or bypasses consent. Worker execution, audited recovery and current authorization remain unchanged.

## Limits

This is only optional workspace-setup delivery, not all tenant or workspace messaging. Opt-out hides pending personal candidates and the worker suppresses their personal copy at its next check. Revoked authority removes sensitive resource visibility; completed account history retains its existing policy. Email/SMS security notices, additional event categories, provider callbacks, billing/metering and broader notification rules remain incomplete. No production-readiness or complete AUTH/NOTIFY coverage is claimed.

## Verification

OperationsIntegrationTest covers real PostgreSQL recipient isolation, anonymous denial, malformed/cross-account cursors, stable multi-page results, pre-consent exclusion, opt-out, completed/failed status, revoked ownership and quiet-hour state. The extended real Compose quiet-hours browser journey checks desktop/mobile pending visibility, links, network failure/recovery, accessibility scans and disappearance after successful delivery.
