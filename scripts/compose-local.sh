#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
python3 "$ROOT/scripts/prepare-compose.py"
exec docker compose -f "$ROOT/infra/compose/compose.yaml" "$@"
