# ADR 0028: Account inbox across navigation contexts

Accepted 2026-11-09 for the implemented in-app security notices (FS-22).

Expose `/account/notifications`, `/tenant/notifications` and `/workspace/notifications` as navigation surfaces for the same account-owned inbox. They do not grant tenant/workspace authority or aggregate workspace records. The page explicitly states that operational and tenancy notices are not included yet. Account and security navigation link the dedicated inbox; existing embedded security notices remain functional.

Extend existing GET notification contracts with `status=all|unread|read`. Default all preserves current clients. Query, filtered count and account-wide unread count share a repeatable-read transaction and account RLS scope. Order by occurrence time descending and UUID descending for equal timestamps. An opaque v2 cursor binds account, filter, timestamp and ID; old or mismatched cursors fail with reload guidance. Counts are current at `asOf`, not a frozen export snapshot; refresh includes newly arrived notices. V32 adds history and unread history indexes without changing recorded events.

The URL retains the filter. Changing it unmounts the prior request scope, aborts obsolete work and resets pagination. During an uncertain read mutation, filter switching is disabled until the exact command is retried. Existing If-Match/idempotency/audit and durable read state are reused. Marking read is review status, never acceptance or deletion. All three routes have private/no-store and noindex policies.

Channel/category/quiet-hour preferences are not represented by inert switches: the currently exposed account messages are essential security notices and no optional account delivery channel is implemented. General notification rules, delivery preferences, operational/tenant event coverage, providers and metering remain open work. No full FS-22 or product phase completion is claimed.
