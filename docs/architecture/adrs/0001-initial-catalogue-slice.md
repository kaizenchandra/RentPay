# ADR 0001: One transactional core and a public catalogue projection

Status: accepted for the initial catalogue slice; later phase gates remain open.

The repository initially contained specifications only. Preserve the RentPay name and those assets. Select Java 21 / Spring Boot 4.0.9, Maven, Next.js App Router, npm and PostgreSQL. A Go core or separate catalogue service adds a second implementation without evidence of need. JDBC is appropriate for this bounded read projection; use JPA where future aggregates benefit from it.

The `catalog` package owns plan versions and its migration audit. Only effective published versions appear in the public API; latest effective version per audience/code wins. Published rows cannot be updated/deleted, even by the migration owner through ordinary SQL. New terms require a new version. Future subscription references must point to immutable version IDs. No activation/admin endpoint or subscriber migration exists yet.

Next.js server rendering uses one configured internal API origin; it does not access PostgreSQL. Browser API URLs use `/api/v1`. Only catalogue GET and shallow health are public; other backend routes fail closed. No identity provider/session authority is implemented. CSRF remains enabled. Future cookie sessions need INT-04 before any protected mutation is exposed.

Schema changes and initial publication audit are in the same Flyway migration. Runtime uses a distinct SELECT-only database role. This avoids pretending that a public read needs workspace membership while keeping private APIs closed. There is no external side effect, so no outbox/job is invented for GET.

Local Compose currently supplies PostgreSQL only. API/web run as processes until the full INT-09 image/migration topology is delivered. This is explicitly not a full deployment gate. Reconsider the read projection, migration grants and initial publication model when adding controlled administration, commercial terms or plan eligibility. Add Tailwind/shared Radix components and form libraries with the first interactive application slice; the current read-only page uses semantic HTML and scoped global design tokens.
