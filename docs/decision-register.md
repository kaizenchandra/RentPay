# Decision register

| ID | Decision / uncertainty | Disposition and prerequisite |
|---|---|---|
| DEC-001 | No executable application existed at initial inspection | Adopt Java 21, Maven, Next.js/npm and PostgreSQL. Existing specifications and IDE files preserved. Public catalogue is the first read-only integration slice; no phase declared complete. |
| DEC-002 | Full source references original FR child requirements but supplies only inclusive ranges and family crosswalks | Preserve all 256 child aliases plus FR-03 and family dispositions. Child-level priorities/acceptance cannot be reconstructed from ranges; locate original catalogue before claiming child-level coverage. |
| DEC-003 | No approved paid prices, tax, quota counting, communication allowances, add-ons or sales destination | Seed explicitly labelled specification baseline; nullable unknown prices and contract-defined capacities. No purchase, upgrade, contact or benefit claims. Review commercial configuration before live activation. |
| DEC-004 | Provider identity, payments, signing, messaging, police and AI approval unavailable | No provider simulation in this slice. Select providers and legal/data-processing policy in applicable phase; no official success or guarantees. |
| DEC-005 | Architecture document puts tours in phase 4 whereas unified source places them in phase 3 | Source unified roadmap wins. Existing design retained; this reconciliation controls future work. |
| DEC-006 | Heading “Reconciled Scope and Precedence” is absent from the supplied full source | Use the actual “Scope Reconciliation and Priority Rules” section plus explicit integration supersession, source catalogue crosswalk, audited FS cross-cutting rules, and supplemental delivery gates; do not infer missing source text. |
| DEC-007 | Runtime permissions | Separate migration and runtime PostgreSQL roles. Runtime catalogue role has SELECT only, no writes or DDL. Catalogue is public and global; workspace RLS is not applicable to this table. Private modules must add scoped isolation and tests before use. |
| DEC-008 | Production and workload decisions | AWS/GCP, region, residency, availability/RPO/RTO, alert owner and measurable load budgets remain unapproved. Local catalogue verification is not production readiness. |


## Foundation continuation

| ID | Decision / uncertainty | Disposition and prerequisite |
|---|---|---|
| DEC-009 | Mobile provider is unavailable | Use only local sandbox HMAC codes in an owner-only filesystem inbox. Never label this real mobile verification. Default runtime rejects live identity; select an approved provider before live rollout. |
| DEC-010 | Session and CSRF storage | Spring Session JDBC and Spring Security session CSRF; opaque cookies, rotation and persisted revocation. See ADR 0002 for limits, loopback guard and failure recovery. |
| DEC-011 | Workspace policy acknowledgements | `sandbox-use-v1` explicitly acknowledges local test use only. This is not an approved legal/privacy policy or production commercial agreement. |
| DEC-012 | Initial Free provisioning and grants | Atomically bind the current versioned Free plan. Owner is separate from manager usage; no unit-counting behavior exists yet. Full quota measurement and essential-lifecycle grants need the appropriate downstream slices. |
| DEC-013 | Changes to baseline display | V3 publishes Free version 2 to describe sandbox setup; V1 evidence is unchanged. Paid prices/features remain unconfigured, and personal membership remains independent/unimplemented. |
| DEC-014 | Identity/audit retention and production abuse controls | Sandbox inbox deletes expired code files; account/challenge/audit retention, distributed rate/abuse protection, contact-change recovery and live policies remain production blockers. |

DEC-004's absence of simulations describes the initial catalogue delivery only. The current slice adds explicitly simulated local SMS delivery. DEC-007's SELECT-only statement applies to the public catalogue: V2 adds narrowly granted writes to identity/workspace/session tables and RLS on private workspace tables.

## Account-control continuation

- DEC-015: Use separate management IDs and hashed session references; tombstones deny subsequent requests after remote revoke or logout. Recent sign-in defaults to five minutes. See ADR 0003.
- DEC-016: V4 requires pre-upgrade cookies to sign in again; account and workspace records are retained. Production registry/command/notification retention and external security notification channels remain pending decisions.

## Workspace membership continuation

- DEC-017: Sandbox manager counting uses active MANAGER accounts per workspace; OWNER and pending invitations are excluded. Acceptance reserves/consumes capacity atomically. Production counting policy remains unapproved; no live membership is enabled by this choice.
- DEC-018: Invitation lifetime defaults to 168 hours (configurable 1–720). Exact deadline is durable and acceptance checks it without relying on cleanup jobs. Production expiry/abuse/retention policy remains open.
- DEC-019: Workspace membership has no implicit property/signing/spending/payout authority. Current owner controls invitations and revocation; owner succession and property delegation remain separate pending workflows. In-app inbox/status is available; external delivery is not configured. See ADR 0004.

## Membership usage continuation

- DEC-020: Separate current manager stock from selected-date event deltas; no monthly reset. Unimplemented inventory measurement returns null, not zero. Counter/ledger/membership discrepancies require reviewed operator reconciliation and cannot silently produce available capacity.
- DEC-021: Commercial policy version 1 covers supported sandbox membership only and binds explicit plan-version grants. Full add-ons, temporary grants, overrides and paid state handling remain pending. Corrective owner revocation remains available independently of commercial availability. See ADR 0005.

## Durable welcome continuation

- DEC-022: Extend the existing workspace outbox for the real welcome handler instead of creating a second queue. Five attempts per automatic cycle, default 30-second lease and bounded retry backoff are development execution defaults, not commercial allowances or SLA promises.
- DEC-023: Recovery remains current-owner scoped and requires original creator authority for replay. No implicit authority handover or arbitrary job payload is accepted. Cancellation cannot recall a committed notification. Production alert ownership, retention and generalized operator roles remain open. See ADR 0006.
