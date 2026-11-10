#!/bin/sh
set -eu
umask 077
export LMT_PROCESS="${1:-api}"
case "$LMT_PROCESS" in
  api|worker)
    export DB_PASSWORD="$(cat "$LMT_DB_PASSWORD_FILE")"
    export LMT_SANDBOX_SECRET="$(cat "$LMT_SESSION_KEY_FILE")"
    export LMT_OTP_SECRET="$LMT_SANDBOX_SECRET"
    if [ "$LMT_PROCESS" = worker ] && [ "${SPRING_PROFILES_ACTIVE:-}" = live ]; then
      export TWILIO_AUTH_TOKEN="$(cat "$TWILIO_AUTH_TOKEN_FILE")"
      export RESEND_API_KEY="$(cat "$RESEND_API_KEY_FILE")"
    fi
    export SPRING_FLYWAY_ENABLED=false
    if [ "$LMT_PROCESS" = worker ]; then
      export LMT_JOBS_ENABLED=true SERVER_ADDRESS=127.0.0.1
    else
      export LMT_JOBS_ENABLED=false SERVER_ADDRESS=0.0.0.0
    fi
    ;;
  migrate) export DB_PASSWORD="$(cat "$LMT_DB_PASSWORD_FILE")" ;;
  *) echo 'Unknown process mode' >&2; exit 2 ;;
esac
exec java -XX:MaxRAMPercentage=70 -jar /app/lmt.jar
