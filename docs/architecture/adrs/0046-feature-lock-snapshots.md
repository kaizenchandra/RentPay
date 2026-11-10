# ADR 0046: Advisory feature locks and request-local snapshots

Status: accepted, 2026-11-10. Requirements SAAS-ENT-002/003, FS-05.

The central evaluator remains the single business authority. A member-readable snapshot projects capability decisions and aggregate quota counters, without subscription payment history or grant audit records. Existing owner-only usage/history APIs remain unchanged. Stable denial codes continue through problem details from actual commands.

Lock guidance derives from configured upgrade offers and target-plan evaluation. Only current owners have billing authority in the implemented role model. Restrictions, unsupported features and absent offers do not generate misleading purchase calls to action. Local pricing is explicitly simulated. Confirmation and quote validation remain in the existing subscription flow.

Use no-store reads, per-component context and abort controllers, 15-second polling, focus/visibility refresh, and workspace-ID-only mutation notifications across tabs. Notifications are hints, not authority. Subscription/provider changes converge on the next read; no claim of instantaneous revocation at the UI. Server commands re-evaluate under their established transaction/locking rules. Do not persist snapshots or sensitive drafts in browser storage. Preserve draft input and separate essential correction capabilities from discretionary expansion.

No schema migration, dependency or provider is introduced. The full catalogue and unbuilt tenancy journeys remain separately tracked. Quota counters are an advisory projection; authoritative allocation and reconciliation checks remain in their owning modules.
