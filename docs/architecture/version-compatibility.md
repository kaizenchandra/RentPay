# Version compatibility evidence

Verified official sources on 2026-11-07; exact resolved Java and frontend dependencies are in `backend/pom.xml` and `web/package-lock.json`.

- Java 21: observed local runtime 21.0.13.0.3. [Spring Boot 4.0.9 system requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html) list Java 17–27 and Maven 3.6.3+. Boot manages its compatible Spring/Flyway/JDBC/test dependency versions; no independent Spring override.
- Next.js 16.4.5 and React 19.3.1 resolved from npm; [official installation documentation](https://nextjs.org/docs/app/getting-started/installation) identifies 16.4.5. [Official release/security listing](https://nextjs.org/blog) reviewed. Supported runtime is pinned in `.node-version`; local verification initially used Node 26.13.0.
- PostgreSQL 17.11: [official version policy](https://www.postgresql.org/support/versioning/) identifies supported 17.x and its current patch. Local image is pinned by patch; production digest pinning remains pending.
- Maven wrapper pins 3.9.11. Dependency licenses/SBOM and comprehensive security scanning remain release gates; version selection is not a security certification.

On upgrades, rerun migration/role/immutability integration tests, client regeneration/drift, TypeScript/build and real browser catalogue checks. No library/tool compatibility is claimed solely from documentation.

Foundation continuation: Spring Session JDBC and Bean Validation use the Boot 4.0.9 BOM. The official Spring Boot Session documentation confirms `spring-boot-starter-session-jdbc`. React Hook Form and Zod are exact-pinned in the updated npm manifest/lockfile. No resolver package was added because its optional AJV-format peer conflicted with the existing contract-test dependency; forms invoke Zod validation directly and render errors through React Hook Form.
