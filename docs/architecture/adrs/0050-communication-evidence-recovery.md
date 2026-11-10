# 0050 — Read-only communication acceptance recovery

Status: Accepted for local sandbox, 2026-11-10.

A provider may accept a message while recording its receipt fails. Resending or releasing capacity without evidence would duplicate delivery or undercount usage. V51 therefore stores owner-authorized, idempotent reconciliation jobs separately from sending operations. Exact message and keyed recipient evidence from Mailpit or the SMS inbox can settle the original acceptance once. Missing, expired, ambiguous or legacy evidence retains the original hold. Recovery never sends a message.

Jobs have bounded retries, lease fencing, execution-time authorization and atomic audit/receipt completion under the existing workspace communication lock. Recipient fingerprints avoid storing raw contact details in operations. Existing operations without fingerprints cannot be safely matched; key changes and provider retention can also prevent resolution. Production adapters and operator resolution policy remain pending.

History reports immutable contract periods using inclusive India date selection and overlapping half-open contract windows. Totals always cover the whole contract, not an invented prorated slice. Late acceptance stays attributed to its original contract. Owner-only, no-store pagination binds the cursor to actor, workspace and filters.

Evidence: CommunicationAllowancesIntegrationTest and communication-recovery.mjs exercise leases, revocation, duplicate completion, held unknowns, pagination and actual local inbox acceptance followed by injected receipt-write failure.
