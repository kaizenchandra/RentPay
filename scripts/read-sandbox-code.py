#!/usr/bin/env python3
"""Explicit developer-only access to a local test code; never exposed as an HTTP endpoint."""
from pathlib import Path
import sys
import uuid

if len(sys.argv) != 2:
    raise SystemExit('Usage: python3 scripts/read-sandbox-code.py CHALLENGE_UUID')
try:
    challenge = str(uuid.UUID(sys.argv[1]))
except ValueError:
    raise SystemExit('Invalid challenge reference')
path = Path(__file__).resolve().parent.parent / '.local' / 'otp-inbox' / (challenge + '.txt')
try:
    code = path.read_text()
except FileNotFoundError:
    raise SystemExit('No local code is available yet, or it expired. Check the challenge status in the browser.')
if len(code) != 6 or not code.isdigit():
    raise SystemExit('Code delivery is incomplete; try again shortly.')
print(code)
