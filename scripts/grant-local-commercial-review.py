#!/usr/bin/env python3
"""Operator-only sandbox grant, fixed local Compose database. Never a production bootstrap."""
import argparse
import getpass
from pathlib import Path
import subprocess
import uuid
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('account_id', type=uuid.UUID)
p.add_argument('--reason', required=True)
p.add_argument('--hours', type=int, default=1)
p.add_argument('--allow-draft-creation', action='store_true', help='Explicitly enable draft creation; never publication')
p.add_argument('--allow-publication', action='store_true', help='Explicitly enable reviewed editorial publication')
p.add_argument('--allow-draft-editing', action='store_true', help='Explicitly enable unpublished editorial edits')
p.add_argument('--allow-draft-discard', action='store_true', help='Explicitly enable evidence-preserving draft discard')
p.add_argument('--allow-offering-management', action='store_true', help='Explicitly enable withdrawal/restoration of catalogue offerings')
p.add_argument('--revoke', action='store_true')
a = p.parse_args()
if not 1 <= a.hours <= 24 or not 10 <= len(a.reason.strip()) <= 300 or any(ord(c)<32 for c in a.reason):
    p.error('Use 1–24 hours and a 10–300 character reason without control characters.')
def quote(value): return "'" + str(value).replace("'", "''") + "'"
# Require a persisted sandbox account; the runtime role cannot issue this grant.
sql = f"""BEGIN;
DO $$ BEGIN IF NOT EXISTS(SELECT 1 FROM user_account WHERE id={quote(a.account_id)} AND identity_mode='sandbox')
THEN RAISE EXCEPTION 'Existing sandbox account required'; END IF; END $$;
INSERT INTO commercial_review_grant(account_id,valid_until,revoked,reason,issued_by,can_create_draft,can_publish,can_edit_draft,can_discard_draft,can_manage_offering)
SELECT id,CURRENT_TIMESTAMP+interval '{a.hours} hours',{str(a.revoke).lower()},{quote(a.reason.strip())},{quote(getpass.getuser())},{str(a.allow_draft_creation and not a.revoke).lower()},{str(a.allow_publication and not a.revoke).lower()},{str(a.allow_draft_editing and not a.revoke).lower()},{str(a.allow_draft_discard and not a.revoke).lower()},{str(a.allow_offering_management and not a.revoke).lower()}
FROM user_account WHERE id={quote(a.account_id)} AND identity_mode='sandbox'
ON CONFLICT(account_id) DO UPDATE SET valid_until=EXCLUDED.valid_until,revoked=EXCLUDED.revoked,
reason=EXCLUDED.reason,issued_by=EXCLUDED.issued_by,can_create_draft=EXCLUDED.can_create_draft,can_publish=EXCLUDED.can_publish,can_edit_draft=EXCLUDED.can_edit_draft,can_discard_draft=EXCLUDED.can_discard_draft,can_manage_offering=EXCLUDED.can_manage_offering,updated_at=CURRENT_TIMESTAMP;
COMMIT;
"""
subprocess.run(['docker','compose','exec','-T','db','psql','-U','lmt_migrate','-d','lmt','-v','ON_ERROR_STOP=1'],input=sql,text=True,cwd=Path(__file__).resolve().parent.parent,check=True)
