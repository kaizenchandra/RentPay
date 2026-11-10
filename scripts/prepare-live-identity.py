#!/usr/bin/env python3
"""Prepare local secret storage, never provider credentials or approval claims."""
from pathlib import Path
import os
import secrets
repo = Path(__file__).resolve().parents[1]
root = repo / '.local' / 'live'
root.mkdir(parents=True, exist_ok=True, mode=0o700)
root.chmod(0o700)
for name in ('bootstrap_password', 'migration_password', 'runtime_password', 'session_key'):
    try:
        fd = os.open(root / name, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o400)
    except FileExistsError:
        continue
    with os.fdopen(fd, 'w') as stream:
        stream.write(secrets.token_urlsafe(48))
    (root / name).chmod(0o444)
config = root / 'runtime.env'
if not config.exists():
    config.write_text((repo / 'infra/compose/live.env.example').read_text())
    config.chmod(0o600)
print('Prepared .local/live. Configure runtime.env and supply provider token files and TLS certificate/key before starting. No live messages were sent.')
