#!/usr/bin/env python3
"""Issue a scoped, expiring sandbox capacity-repair-operator grant; never a production bootstrap."""
import argparse
import getpass
from pathlib import Path
import subprocess
import uuid
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('account_id', type=uuid.UUID)
p.add_argument('workspace_id', type=uuid.UUID)
p.add_argument('--reason', required=True)
p.add_argument('--hours', type=int, default=1)
p.add_argument('--revoke', action='store_true')
a = p.parse_args()
if not 1 <= a.hours <= 24 or not 10 <= len(a.reason.strip()) <= 300 or any(ord(c)<32 or ord(c)==127 for c in a.reason):
    p.error('Use 1–24 hours and a 10–300 character reason without control characters.')
def quote(v): return "'" + str(v).replace("'", "''") + "'"
sql = f"""BEGIN;
DO $$ BEGIN IF NOT EXISTS(SELECT 1 FROM user_account WHERE id={quote(a.account_id)} AND identity_mode='sandbox') OR NOT EXISTS(SELECT 1 FROM workspace WHERE id={quote(a.workspace_id)} AND mode='sandbox') THEN RAISE EXCEPTION 'Existing sandbox account and workspace required'; END IF; END $$;
INSERT INTO capacity_repair_operator_grant(account_id,workspace_id,valid_until,revoked,reason,issued_by)
VALUES({quote(a.account_id)},{quote(a.workspace_id)},CURRENT_TIMESTAMP+interval '{a.hours} hours',{str(a.revoke).lower()},{quote(a.reason.strip())},{quote(getpass.getuser())})
ON CONFLICT(account_id,workspace_id) DO UPDATE SET valid_until=EXCLUDED.valid_until,revoked=EXCLUDED.revoked,reason=EXCLUDED.reason,issued_by=EXCLUDED.issued_by,updated_at=CURRENT_TIMESTAMP;
COMMIT;
"""
subprocess.run(['docker','compose','exec','-T','db','psql','-U','lmt_migrate','-d','lmt','-v','ON_ERROR_STOP=1'],input=sql,text=True,cwd=Path(__file__).resolve().parent.parent,check=True)
print(f'/admin/workspaces/{a.workspace_id}/manager-capacity-repair')
