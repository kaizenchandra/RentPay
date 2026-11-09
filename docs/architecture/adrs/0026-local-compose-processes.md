# ADR 0026: Local Compose process separation

Accepted 2026-11-09 for the currently implemented local sandbox only (ARCH-01/03/04, INT-09–12 partial).

Keep the root database-only Compose workflow. Add a distinct six-service stack under `infra/compose`, with loopback edge ingress, standalone Next.js, one Java image used for API/worker/migrations, and PostgreSQL. Persist database and private quarantine/inbox separately. Dedicated migration credentials cannot reach API/worker; runtime processes validate migration compatibility without applying DDL. Domain scheduling runs only in the worker; its private health checks database and scheduler liveness. Existing native development keeps scheduling enabled by default.

Local identity normally requires loopback binding. Containers need private-interface API binding, so an explicit container-sandbox flag permits it only with the sole local profile and loopback HTTP origins. This is not a production exception. Worker HTTP remains container-loopback and denies business routes. The edge is the only published port; an ingress network is necessary for host publication while the service network remains internal.

No additional datastore or unimplemented live provider is introduced. Production deployment, external secret management, TLS, restore drills and operational SLOs remain separate incomplete gates. See the Compose runbook for observed verification and limitations.
