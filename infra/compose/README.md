# Local full-stack Compose

Run from the repository root (Docker with Compose v2+, Python 3):

```sh
./scripts/compose-local.sh build
./scripts/compose-local.sh up -d --wait
./scripts/compose-local.sh ps --all
```

Open **http://localhost:3100/login**. This is an explicit local sandbox, not a production deployment. Only the edge publishes a host port, bound to 127.0.0.1. Existing root `compose.yaml` remains the independent database-only development environment on port 54329. Never share its volume with this stack.

| Service | Responsibility | Readiness / completion |
|---|---|---|
| edge | Nginx same-origin routing; `/api/v1/*` to API, other pages to web | Internal `/healthz`, healthy web and worker prerequisites |
| web | Non-root Next.js standalone frontend | Real `/login` HTTP 200 |
| api | Java core, runtime database role, domain jobs disabled | Actuator health including database; matching migration history |
| worker | Same Java artifact, durable delivery/scan jobs enabled, business requests denied | Internal loopback health plus recent scheduler heartbeat |
| migrate | One-shot Flyway process with separate DDL credentials | Successful exit required before API/worker |
| db | PostgreSQL 17.11, persistent named volume | `pg_isready`; migration success provides subsequent schema gate |

All base images are pinned by version and digest. Java and web runtime containers are non-root with read-only roots, dropped capabilities and temporary writable directories. Only the edge joins the ingress network; internal services cannot reach external providers. No Redis, queue broker, cloud storage, payment, signing or live messaging service is fabricated. Existing PostgreSQL jobs and local quarantine storage are used. The worker serves health on its container loopback only.

The wrapper creates random secrets once in ignored `.local/compose/`. The host directory is mode 0700; individual files are readable by container UIDs for Compose file-backed secrets. Runtime containers receive only runtime DB credentials and the sandbox signing secret; migration credentials are not mounted into them. The database bootstrap role is distinct from migration/runtime roles. Keep this directory private and back it up securely with the database. Deleting or regenerating these files does not rotate credentials in an existing database; coordinate SQL password rotation and container recreation instead. Never check secrets into Git.

Sandbox verification codes are private files, not real SMS/email. After requesting a code, use the challenge UUID from the verification URL:

```sh
./scripts/compose-local.sh exec -T worker cat /data/otp-inbox/CHALLENGE_UUID.txt
```

Codes expire and are cleaned up. Never publish these files or logs containing codes. Both API and worker share `private_files` for inbox/quarantine; PostgreSQL sessions and application records live in `postgres_data`. An explicit container-local sandbox exception permits API binding on its private container interface only with the local profile and loopback HTTP public origins. Production profiles still reject sandbox identity.

## Verification and recovery

```sh
npm --prefix web ci
cd web && npx playwright install chromium && cd ..
node web/tests/compose-smoke.mjs
./scripts/compose-local.sh logs --tail=100 migrate api worker web edge
./scripts/compose-local.sh stop
./scripts/compose-local.sh up -d --wait
```

The smoke test creates clearly labeled sandbox records, exercises the UI/API/worker, checks unauthorized access and private health, then recreates API, worker and web to verify persistent sessions/workspaces. It intentionally retains the records. Backend PostgreSQL integration tests remain `./backend/mvnw -f backend/pom.xml verify` with the repository's Docker environment. Image builds compile/typecheck but skip backend test execution; they do not replace that suite.

On migration failure inspect `migrate` logs and repair the cause; do not bypass its dependency or edit applied migrations. Rebuild changed images, then `up -d --wait`; Flyway reruns idempotently. Runtime processes refuse pending, mismatched or future migration histories. A failed runtime healthcheck marks the service unhealthy; Compose does not automatically restart a still-running unhealthy process. Investigate queue errors and restart only after understanding uncertain outcomes. Existing durable jobs retry according to their own domain policy.

`stop` retains containers and data. `down` removes containers/networks but retains named volumes. **Do not use `down -v` unless intentionally discarding the entire sandbox database and private files.** To back up consistently, stop API/worker, export PostgreSQL using `pg_dump` from the db container, and securely copy the private-files volume and local secrets together; restore into a separate project and validate before switching. Backup/restore drills, production encrypted object storage, malware scanning providers, TLS, production identity, secret manager, production alerts and capacity/load gates remain unverified or unavailable. This stack makes no production-readiness claim.

Implementation follows [Compose dependency conditions](https://docs.docker.com/compose/how-tos/startup-order/), [Compose secrets](https://docs.docker.com/compose/how-tos/use-secrets/) and [Next.js standalone output](https://nextjs.org/docs/pages/api-reference/config/next-config-js/output). The frontend image includes standalone output and static assets; API origin is fixed at image build and runtime.
