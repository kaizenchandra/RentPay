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
async function emailSignIn(page:Page,email:string){
 await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('Email Browser Owner');await page.getByLabel('Verification channel').selectOption('email');await page.getByLabel('Email address',{exact:true}).fill(email);await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await expect(page.getByText(/No SMS or email is sent/)).toBeVisible();await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}
test('email manager invitation recovers lost creation and requires explicit acceptance',async({page,browser})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999),'Email Inviting Owner');const w=await provision(page),email='Manager'+randomInt(100000000,999999999)+'@EXAMPLE.test';let lost=false;
 await page.route('**/api/v1/workspaces/'+w+'/invitations',async route=>{if(route.request().method()==='POST'&&!lost){lost=true;const r=await route.fetch();expect(r.status()).toBe(201);await route.abort('failed');}else await route.continue();});
 await page.getByLabel('Invitation contact channel').selectOption('email');await page.getByLabel('Manager email address').fill(email);await page.getByRole('button',{name:'Review invitation'}).click();await expect(page.getByText('Invite '+email,{exact:false})).toBeVisible();await expect(page.getByRole('button',{name:'Confirm action'})).toBeFocused();await page.getByRole('button',{name:'Confirm action'}).press('Enter');await expect(page.getByRole('button',{name:'Retry same action'})).toBeVisible();await page.getByRole('button',{name:'Retry same action'}).click();await expect(page.getByText(/Invitation saved in the recipient/)).toBeVisible();
 const sent=await(await page.request.get('/api/v1/workspaces/'+w+'/invitations')).json();schema('WorkspaceInvitations',sent);expect(sent.totalMatching).toBe(1);expect(sent.items[0].channel).toBe('email');expect(sent.items[0].maskedMobile).toBeNull();
 const context=await browser.newContext({baseURL:'http://127.0.0.1:3000',viewport:page.viewportSize()});try{const recipient=await context.newPage();await emailSignIn(recipient,email.replace('EXAMPLE','example'));expect((await recipient.request.get('/api/v1/workspaces/'+w+'/membership')).status()).toBe(404);await recipient.goto('/account/invitations');await expect(recipient.getByText(/@example.test · Manager · pending/)).toBeVisible();await recipient.getByRole('button',{name:'Review acceptance'}).click();await recipient.getByRole('button',{name:'Confirm action'}).press('Enter');await expect(recipient.getByText(/Invitation accepted/)).toBeVisible();expect((await recipient.request.get('/api/v1/workspaces/'+w+'/membership')).status()).toBe(200);expect((await recipient.request.get('/api/v1/workspaces/'+w+'/members')).status()).toBe(404);
 expect((await new AxeBuilder({page:recipient}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await recipient.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();await page.getByRole('button',{name:'Refresh lists'}).click();await expect(page.getByText(/1 of 1 manager slots used/)).toBeVisible();await page.getByRole('button',{name:'Revoke membership'}).click();await page.getByRole('button',{name:'Confirm action'}).click();await expect(page.getByText(/0 of 1 manager slots used/)).toBeVisible();expect((await recipient.request.get('/api/v1/workspaces/'+w+'/membership')).status()).toBe(404);
 }finally{await context.close();}
});
