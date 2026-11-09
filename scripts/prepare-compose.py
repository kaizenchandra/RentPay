#!/usr/bin/env python3
"""Create local-only secrets once; never rotate credentials of an existing database."""
from pathlib import Path
import os
import secrets
root = Path(__file__).resolve().parents[1] / '.local' / 'compose'
root.mkdir(parents=True, exist_ok=True, mode=0o700)
root.chmod(0o700)
for name in ('bootstrap_password', 'migration_password', 'runtime_password', 'session_key'):
    path = root / name
    try:
        fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o400)
    except FileExistsError:
        continue
    with os.fdopen(fd, 'w') as stream:
        stream.write(secrets.token_urlsafe(48))
    # Compose file-backed secrets must be readable by container non-root UIDs.
    # The enclosing directory remains owner-only on the host.
    path.chmod(0o444)
