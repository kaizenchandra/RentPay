#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
for name in runtime.env bootstrap_password migration_password runtime_password session_key twilio_auth_token resend_api_key tls.crt tls.key; do
  if [ ! -s "$ROOT/.local/live/$name" ]; then
    echo "Live sign-in is not configured: missing .local/live/$name. See infra/compose/LIVE-IDENTITY.md." >&2
    exit 1
  fi
done
exec docker compose --env-file "$ROOT/.local/live/runtime.env" -f "$ROOT/infra/compose/compose.yaml" -f "$ROOT/infra/compose/live.yaml" "$@"
