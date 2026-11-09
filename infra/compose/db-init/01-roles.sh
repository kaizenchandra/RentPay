#!/bin/sh
set -eu
export LMT_MIGRATION_PASSWORD="$(cat /run/secrets/migration_password)"
export LMT_RUNTIME_PASSWORD="$(cat /run/secrets/runtime_password)"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\getenv migration_password LMT_MIGRATION_PASSWORD
\getenv runtime_password LMT_RUNTIME_PASSWORD
SELECT format('CREATE ROLE lmt_migrate LOGIN PASSWORD %L NOSUPERUSER NOBYPASSRLS', :'migration_password') \gexec
SELECT format('CREATE ROLE lmt_runtime LOGIN PASSWORD %L NOSUPERUSER NOBYPASSRLS', :'runtime_password') \gexec
ALTER SCHEMA public OWNER TO lmt_migrate;
GRANT CONNECT, CREATE ON DATABASE lmt TO lmt_migrate;
GRANT CONNECT ON DATABASE lmt TO lmt_runtime;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
SQL
unset LMT_MIGRATION_PASSWORD LMT_RUNTIME_PASSWORD
