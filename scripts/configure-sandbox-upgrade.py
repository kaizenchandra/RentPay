#!/usr/bin/env python3
"""Configure explicit local test pricing; never modifies published prices or live providers."""
import argparse, decimal, pathlib, re, subprocess, uuid
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--source',required=True);p.add_argument('--target',required=True)
p.add_argument('--amount',required=True);p.add_argument('--tax-rate',required=True)
p.add_argument('--months',type=int,choices=[1,12],required=True)
p.add_argument('--proration',choices=['none_free_only','unused_time_credit'],required=True)
a=p.parse_args()
if not all(re.fullmatch(r'[A-Z_]{2,32}',v) for v in [a.source,a.target]) or a.source==a.target:p.error('Choose distinct catalogue codes.')
try:
 amount=decimal.Decimal(a.amount);rate=decimal.Decimal(a.tax_rate)
 if not amount.is_finite() or not rate.is_finite() or amount<=0 or amount>=decimal.Decimal('1000000000000') or not 0<=rate<=100 or amount.as_tuple().exponent < -2 or rate.as_tuple().exponent < -4:raise ValueError()
except (decimal.InvalidOperation,ValueError):p.error('Use a positive two-decimal INR test amount and a 0–100 tax rate with at most four decimals.')
root=pathlib.Path(__file__).resolve().parent.parent
wrapper=str(root/'scripts/compose-local.sh')
def sql(statement):
 return subprocess.run([wrapper,'exec','-T','db','psql','-U','lmt_bootstrap','-d','lmt','-At','-v','ON_ERROR_STOP=1','-c',statement],check=True,capture_output=True,text=True).stdout.strip()
def plan(code):
 result=sql("SELECT version_id FROM plan_version WHERE audience='workspace' AND code='"+code+"' AND publication_state='published' AND effective_at<=CURRENT_TIMESTAMP ORDER BY effective_at DESC LIMIT 1")
 return str(uuid.UUID(result))
source,target=plan(a.source),plan(a.target);offer=str(uuid.uuid4())
sql(f"""BEGIN;
INSERT INTO subscription_upgrade_offer(id,source_plan_id,target_plan_id,mode,base_amount,tax_rate,interval_months,proration,recipient,disclosure)
VALUES('{offer}','{source}','{target}','sandbox',{amount},{rate},{a.months},'{a.proration}','Local sandbox merchant — no money collected','Explicit test pricing and tax configuration. Not a production price or legal tax determination. Existing implemented capabilities only.');
INSERT INTO commercial_plan_capability SELECT policy_version,capability,'{target}' FROM commercial_plan_capability WHERE plan_version_id='{source}' ON CONFLICT DO NOTHING;
INSERT INTO domain_audit(id,action,resource_id) VALUES(gen_random_uuid(),'sandbox.upgrade_offer_configured','{offer}');
COMMIT;""")
print(offer)
