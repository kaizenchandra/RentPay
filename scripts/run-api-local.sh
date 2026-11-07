#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
umask 077
mkdir -p .local/otp-inbox
if [ ! -f .local/sandbox-secret ]; then openssl rand -hex 32 > .local/sandbox-secret; fi
LMT_SANDBOX_SECRET=$(cat .local/sandbox-secret)
LMT_SANDBOX_INBOX="$(pwd)/.local/otp-inbox"
export LMT_SANDBOX_SECRET LMT_SANDBOX_INBOX
export SPRING_PROFILES_ACTIVE=local
cd backend
# Only for the loopback-bound local Compose database. Production must inject approved credentials.
export DB_URL=jdbc:postgresql://127.0.0.1:54329/lmt
export DB_USER=lmt_runtime
export DB_PASSWORD=lmt_local_runtime
export MIGRATION_DB_USER=lmt_migrate
export MIGRATION_DB_PASSWORD=lmt_local_migration
if [ -x /usr/libexec/java_home ]; then
  JAVA_HOME=$(/usr/libexec/java_home -v 21)
  export JAVA_HOME
fi
exec ./mvnw -B -ntp spring-boot:run -Dspring-boot.run.arguments=--server.address=127.0.0.1
