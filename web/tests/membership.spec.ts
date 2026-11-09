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
test('owner invites, recipient accepts and switches context, owner revokes access',async({page,browser},testInfo)=>{
 const owner='+919'+randomInt(100000000,999999999),mobile='+919'+randomInt(100000000,999999999);await signIn(page,owner,'Workspace Owner');const w=await provision(page);
 await expect(page.getByText(/0 of 1 manager slots used/)).toBeVisible();await page.getByLabel('Manager mobile number').fill(mobile);await page.getByRole('button',{name:'Review invitation'}).click();await expect(page.getByRole('button',{name:'Confirm action'})).toBeFocused();await page.getByRole('button',{name:'Cancel',exact:true}).click();expect((await (await page.request.get('/api/v1/workspaces/'+w+'/invitations')).json()).totalMatching).toBe(0);
 await page.getByRole('button',{name:'Review invitation'}).click();const saved=page.waitForResponse(r=>r.url().endsWith('/invitations')&&r.request().method()==='POST');await page.getByRole('button',{name:'Confirm action'}).press('Enter');const response=await saved;expect(response.status()).toBe(201);schema('MembershipOutcome',await response.json());await expect(page.getByText(/Invitation saved in the recipient/)).toBeVisible();
 schema('WorkspaceMembers',await (await page.request.get('/api/v1/workspaces/'+w+'/members')).json());schema('WorkspaceInvitations',await (await page.request.get('/api/v1/workspaces/'+w+'/invitations')).json());
 const context=await browser.newContext({baseURL:'http://127.0.0.1:3000'});try{
  const recipient=await context.newPage();await signIn(recipient,mobile,'Invited Manager');await recipient.getByRole('link',{name:'Your invitations',exact:true}).click();await recipient.getByRole('button',{name:'Review acceptance'}).click();await recipient.getByRole('button',{name:'Confirm action'}).click();await expect(recipient.getByText('Invitation accepted. Your workspace membership is active.',{exact:true})).toBeVisible();await recipient.getByRole('link',{name:'Open workspace'}).click();await expect(recipient.getByRole('heading',{name:'Membership E2E',exact:true})).toBeVisible();
  const current=await recipient.request.get('/api/v1/workspaces/'+w+'/membership');expect(current.status()).toBe(200);schema('MembershipContext',await current.json());expect((await recipient.request.get('/api/v1/workspaces/'+w+'/members')).status()).toBe(404);
  await page.getByRole('button',{name:'Refresh lists'}).click();await expect(page.getByText(/1 of 1 manager slots used/)).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();await page.screenshot({path:testInfo.outputPath('members.png'),fullPage:true});
  await page.getByRole('link',{name:'Usage and eligibility',exact:true}).click();await expect(page.getByRole('heading',{name:'Usage and eligibility',exact:true})).toBeVisible();
  await expect(page.getByText('Reconciled with membership, allocations and usage events.',{exact:true})).toBeVisible();await page.getByText('Reconciliation evidence',{exact:true}).click();await expect(page.getByText('Active manager accounts',{exact:true})).toBeVisible();await expect(page.getByText('Unexplained reservations',{exact:true})).toBeVisible();await expect(page.getByText(/No manager slots remain/)).toBeVisible();
  schema('UsageSnapshot',await (await page.request.get('/api/v1/workspaces/'+w+'/entitlements')).json());schema('UsageHistory',await (await page.request.get('/api/v1/workspaces/'+w+'/usage/history')).json());
  await expect(page.getByText(/Usage, reservations and remaining capacity are unavailable/)).toBeVisible();await expect(page.getByRole('heading',{name:'Manager slot consumed',exact:true})).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();await page.screenshot({path:testInfo.outputPath('usage.png'),fullPage:true});
  await page.getByLabel('From date').fill('2020-01-01');await page.getByLabel('To date').fill('2020-01-02');await page.getByRole('button',{name:'Apply dates'}).click();await expect(page).toHaveURL(/from=2020-01-01/);await expect(page.getByText('No manager capacity events in this date range.',{exact:true})).toBeVisible();await expect(page.getByText(/No manager slots remain/)).toBeVisible();
  await page.getByRole('link',{name:'Members and invitations',exact:true}).click();
  await page.getByRole('button',{name:'Revoke membership'}).click();await page.getByRole('button',{name:'Confirm action'}).click();await expect(page.getByText(/0 of 1 manager slots used/)).toBeVisible();expect((await recipient.request.get('/api/v1/workspaces/'+w+'/membership')).status()).toBe(404);
  await recipient.getByRole('button',{name:'Refresh access'}).click();await expect(recipient.getByText('Workspace is unavailable.',{exact:true})).toBeVisible();await recipient.getByRole('link',{name:'Your invitations',exact:true}).click();await expect(recipient.getByText(/access revoked/)).toBeVisible();
  expect((await new AxeBuilder({page:recipient}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 }finally{await context.close();}
});
test('lost invitation response recovers one durable invitation',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999),'Recovery Owner');const w=await provision(page);let lose=true;
 await page.route('**/api/v1/workspaces/'+w+'/invitations',async route=>{if(route.request().method()==='POST'&&lose){lose=false;const response=await route.fetch();expect(response.status()).toBe(201);await route.abort('failed');}else await route.continue();});
 await page.getByLabel('Manager mobile number').fill('+919'+randomInt(100000000,999999999));await page.getByRole('button',{name:'Review invitation'}).click();await page.getByRole('button',{name:'Confirm action'}).click();await expect(page.getByRole('button',{name:'Retry same action'})).toBeVisible();await page.getByRole('button',{name:'Retry same action'}).click();await expect(page.getByText(/Invitation saved in the recipient/)).toBeVisible();
 expect((await (await page.request.get('/api/v1/workspaces/'+w+'/invitations')).json()).totalMatching).toBe(1);
});

test('owner can inspect stranded reservation evidence and refresh after external recovery',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999),'Reconciliation Owner');const w=await provision(page);
 expect(w).toMatch(/^[0-9a-f-]{36}$/);
 // Isolated test workspace only: deliberately inject drift with the local migration role.
 const reserved=(n:number)=>execFileSync('docker',['compose','exec','-T','db','psql','-U','lmt_migrate','-d','lmt','-v','ON_ERROR_STOP=1','-c',"UPDATE workspace_grant SET reserved="+n+" WHERE workspace_id='"+w+"' AND resource='managers'"],{cwd:'..',stdio:'pipe'});
 try{
  reserved(1);await page.goto('/workspace/billing/usage?workspace='+w);
  await expect(page.getByText('Unavailable — reconciliation required',{exact:true})).toBeVisible();
  const response=await page.request.get('/api/v1/workspaces/'+w+'/usage');schema('UsageSnapshot',await response.json());expect(response.headers()['cache-control']).toContain('no-store');
  await page.getByText('Reconciliation evidence',{exact:true}).click();
  await expect(page.getByText('Unexplained reservations',{exact:true}).locator('..')).toContainText('1');
  await expect(page.getByText(/Counter repair requires separate scoped authority/)).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
  reserved(0);await page.getByRole('button',{name:'Refresh usage'}).click();await expect(page.getByText('Reconciled with membership, allocations and usage events.',{exact:true})).toBeVisible();
 }finally{reserved(0);}
});
