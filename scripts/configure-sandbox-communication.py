#!/usr/bin/env python3
"""Configure an immutable local communication allowance; not a live plan benefit."""
import argparse,datetime,pathlib,subprocess,uuid
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--workspace',required=True,type=uuid.UUID)
p.add_argument('--channel',required=True,choices=['email','sms'])
p.add_argument('--metric',required=True,choices=['provider_accepted'])
p.add_argument('--allowance',required=True,type=int)
p.add_argument('--starts-at',required=True);p.add_argument('--ends-at',required=True)
p.add_argument('--reason',required=True)
a=p.parse_args()
if not 0<=a.allowance<=100000:p.error('Allowance must be 0–100000 local test operations.')
if not 10<=len(a.reason.strip())<=300 or any(ord(c)<32 for c in a.reason):p.error('Give a reason of 10–300 characters without controls.')
try:
 start=datetime.datetime.fromisoformat(a.starts_at.replace('Z','+00:00'));end=datetime.datetime.fromisoformat(a.ends_at.replace('Z','+00:00'))
 if start.tzinfo is None or end.tzinfo is None or end<=start:raise ValueError()
except ValueError:p.error('Use timezone instants with end later than start.')
def q(v):return "'"+str(v).replace("'","''")+"'"
identifier=uuid.uuid4()
sql=f"INSERT INTO communication_contract(id,workspace_id,plan_version_id,channel,metric,allowance,starts_at,ends_at,mode,reason) SELECT {q(identifier)},w.id,s.plan_version_id,{q(a.channel)},{q(a.metric)},{a.allowance},{q(start.isoformat())},{q(end.isoformat())},'sandbox',{q(a.reason.strip())} FROM workspace w JOIN workspace_subscription s ON s.workspace_id=w.id WHERE w.id={q(a.workspace)} AND w.mode='sandbox' RETURNING id"
wrapper=str(pathlib.Path(__file__).resolve().parent/'compose-local.sh')
r=subprocess.run([wrapper,'exec','-T','db','psql','-U','lmt_bootstrap','-d','lmt','-At','-v','ON_ERROR_STOP=1','-c',sql],capture_output=True,text=True)
if r.returncode:p.exit(1,r.stderr)
if str(identifier) not in r.stdout:p.exit(1,'No matching sandbox workspace. Nothing configured.\n')
print(identifier)
