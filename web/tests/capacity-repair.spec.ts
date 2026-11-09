import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
import {randomInt} from 'node:crypto';
import Ajv2020 from 'ajv/dist/2020';
import addFormats from 'ajv-formats';
import contract from '../../contracts/openapi/lmt-v1.json';
const ajv=new Ajv2020({strict:false});addFormats(ajv);
function schema(name:string,value:unknown){const check=ajv.compile({$ref:`#/components/schemas/${name}`,components:contract.components});expect(check(value),JSON.stringify(check.errors)).toBeTruthy();}
async function signIn(page:Page,mobile:string,name:string){
 await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill(name);await page.getByLabel('Mobile number',{exact:true}).fill(mobile);await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}
async function provision(page:Page){
 await page.getByRole('link',{name:'Create a Free workspace'}).click();await page.getByLabel('Workspace or organization name').fill('Membership E2E');await page.getByLabel('City',{exact:true}).fill('Pune');await page.getByLabel('State / union territory').fill('Maharashtra');await page.getByRole('checkbox').check();await page.getByRole('button',{name:'Confirm and create Free workspace'}).click();await expect(page.getByRole('heading',{name:'Setup progress'})).toBeVisible();const w=new URL(page.url()).searchParams.get('workspace')!;await page.getByRole('link',{name:'Members and invitations'}).click();await expect(page.getByRole('heading',{name:'Members and invitations',exact:true})).toBeVisible();return w;
}
test('scoped owner reviews and recovers one durable capacity repair',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999),'Capacity Repair Owner');const w=await provision(page);
 const actor=(await (await page.request.get('/api/v1/auth/session')).json()).account.id;
 expect(w).toMatch(/^[0-9a-f-]{36}$/);
 const reserved=(n:number)=>execFileSync('docker',['compose','exec','-T','db','psql','-U','lmt_migrate','-d','lmt','-v','ON_ERROR_STOP=1','-c',"UPDATE workspace_grant SET reserved="+n+" WHERE workspace_id='"+w+"' AND resource='managers'"],{cwd:'..',stdio:'pipe'});
 const grant=(revoke=false)=>execFileSync('python3',['scripts/grant-local-capacity-repair-operator.py',actor,w,'--reason','Isolated browser capacity repair test',...(revoke?['--revoke']:[])],{cwd:'..',stdio:'pipe'});
 try{
  reserved(1);await page.goto('/admin/workspaces/'+w+'/manager-capacity-repair');await expect(page.getByText(/A separate active capacity-repair grant/)).toBeVisible();grant();await page.reload();
  await expect(page.getByText('Recorded reserved',{exact:true}).locator('..')).toContainText('1');await page.getByLabel('Repair reason').fill('Investigated isolated stranded reservation');await page.getByRole('button',{name:'Review counter repair'}).click();await expect(page.getByRole('heading',{name:'Confirm counter repair'})).toBeFocused();
  let lost=true;await page.route('**/api/v1/admin/workspaces/'+w+'/manager-capacity-repair',async route=>{if(lost&&route.request().method()==='POST'){lost=false;const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');}else await route.continue();});
  await page.getByRole('button',{name:'Confirm repair',exact:true}).click();await expect(page.getByRole('button',{name:'Retry same repair'})).toBeVisible();await page.getByRole('button',{name:'Retry same repair'}).click();await expect(page.getByText(/Counter repair recorded/)).toBeVisible();
  const r=await page.request.get('/api/v1/admin/workspaces/'+w+'/manager-capacity-repair');const v=await r.json();schema('CapacityRepair',v);expect(v.revision).toBe(1);expect(v.reserved).toBe(0);expect(r.headers()['cache-control']).toContain('no-store');
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
  await page.getByRole('link',{name:'Owner usage view',exact:true}).click();await expect(page.getByText('Reconciled with membership, allocations and usage events.',{exact:true})).toBeVisible();
  await expect(page.getByRole('heading',{name:'Counter repair revision 1',exact:true})).toBeVisible();await expect(page.getByText('Reason: Investigated isolated stranded reservation',{exact:true})).toBeVisible();
  const historyResponse=await page.request.get('/api/v1/workspaces/'+w+'/usage/repairs');const history=await historyResponse.json();schema('CapacityRepairHistory',history);expect(history.totalMatching).toBe(1);expect(history.items[0].previousReserved).toBe(1);expect(history.items[0].correctedReserved).toBe(0);
  grant(true);await page.getByRole('button',{name:'Refresh usage'}).click();await expect(page.getByRole('heading',{name:'Counter repair revision 1',exact:true})).toBeVisible();
  await page.getByLabel('From date').fill('2020-01-01');await page.getByLabel('To date').fill('2020-01-02');await page.getByRole('button',{name:'Apply dates'}).click();await expect(page.getByText('No manager counter repairs in this date range.',{exact:true})).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
  expect((await page.request.get('/api/v1/admin/workspaces/'+w+'/manager-capacity-repair')).status()).toBe(403);
 }finally{reserved(0);grant(true);}
});
