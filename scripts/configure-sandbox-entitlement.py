#!/usr/bin/env python3
"""Configure or revoke an audited local entitlement input; never a paid add-on purchase."""
import argparse,datetime,pathlib,re,subprocess,uuid
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--workspace',required=True,type=uuid.UUID)
p.add_argument('--capability');p.add_argument('--kind',choices=['add_on','timed_grant','commercial_override']);p.add_argument('--effect',choices=['allow','deny'],default='allow')
p.add_argument('--starts-at');p.add_argument('--expires-at');p.add_argument('--revoke',type=uuid.UUID);p.add_argument('--reason',required=True)
a=p.parse_args()
if not 10<=len(a.reason.strip())<=300 or any(ord(c)<32 for c in a.reason):p.error('Give a reason of 10–300 characters without controls.')
def q(v):return "'"+str(v).replace("'","''")+"'"
w=q(a.workspace);reason=q(a.reason.strip());wrapper=str(pathlib.Path(__file__).resolve().parent/'compose-local.sh')
lock=f"SELECT pg_advisory_xact_lock(hashtextextended('membership:'||{w},0));"
if a.revoke:
 statement=f"INSERT INTO workspace_entitlement_revocation(input_id,workspace_id,reason,created_by) SELECT {q(a.revoke)},id,{reason},current_user FROM workspace WHERE id={w} AND mode='sandbox' RETURNING input_id;"
else:
 if not a.capability or not re.fullmatch('[A-Z_]{3,80}',a.capability) or not a.kind or not a.expires_at:p.error('Supply capability, kind and expires-at for a new input.')
 try:
  start=datetime.datetime.fromisoformat(a.starts_at.replace('Z','+00:00')) if a.starts_at else datetime.datetime.now(datetime.timezone.utc)
  end=datetime.datetime.fromisoformat(a.expires_at.replace('Z','+00:00'))
  if start.tzinfo is None or end.tzinfo is None or end<=start or end<=datetime.datetime.now(datetime.timezone.utc):raise ValueError()
 except ValueError:p.error('Use explicit timezone instants and a future expiry after the start.')
 identifier=uuid.uuid4()
 statement=f"""INSERT INTO workspace_entitlement_input(id,workspace_id,policy_version,capability,kind,effect,plan_version_id,starts_at,expires_at,mode,reason,created_by)
 SELECT {q(identifier)},w.id,c.policy_version,c.code,{q(a.kind)},{q(a.effect)},s.plan_version_id,{q(start.isoformat())},{q(end.isoformat())},'sandbox',{reason},current_user
 FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id CROSS JOIN commercial_capability c
 WHERE w.id={w} AND w.mode='sandbox' AND c.code={q(a.capability)} AND c.policy_version=(SELECT version FROM commercial_policy WHERE effective_at<=CURRENT_TIMESTAMP ORDER BY effective_at DESC,version DESC LIMIT 1) RETURNING id;"""
result=subprocess.run([wrapper,'exec','-T','db','psql','-U','lmt_bootstrap','-d','lmt','-At','-v','ON_ERROR_STOP=1','-c','BEGIN;'+lock+statement+'COMMIT;'],capture_output=True,text=True)
if result.returncode:p.exit(1,result.stderr)
ids=[line for line in result.stdout.splitlines() if re.fullmatch('[a-f0-9-]{36}',line)]
if len(ids)!=1:p.exit(1,'No matching sandbox workspace/capability. Nothing configured.\n')
print(ids[0])
