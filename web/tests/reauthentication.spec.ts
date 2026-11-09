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
async function signIn(page:Page,mobile:string){
 await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('Account Test');await page.getByLabel('Mobile number',{exact:true}).fill(mobile);
 await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');let code='';
 await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}

test('aged session reauthenticates and resumes a protected contact request',async({page,context})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999));const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;expect(actor).toMatch(/^[0-9a-f-]{36}$/);const before=(await context.cookies()).find(c=>c.name==='lmt_dev_session')!.value;
 execFileSync('docker',['compose','exec','-T','db','psql','-U','lmt_migrate','-d','lmt','-v','ON_ERROR_STOP=1','-c',"UPDATE account_session SET authenticated_at=CURRENT_TIMESTAMP-interval '10 minutes' WHERE account_id='"+actor+"'"],{cwd:'..',stdio:'pipe'});
 await page.goto('/account/identity-links');await page.getByLabel('Email address to link').fill('StepUp'+randomInt(100000000,999999999)+'@example.test');await page.getByRole('button',{name:'Review linking code request'}).click();await page.getByRole('button',{name:'Request linking code',exact:true}).click();await page.getByRole('link',{name:'Verify your current session',exact:true}).click();await expect(page.getByRole('heading',{name:'Verify your current session'})).toBeVisible();
 const accepted=page.waitForResponse(r=>r.url().endsWith('/me/reauthentication/challenges'));await page.getByRole('button',{name:'Request verification code'}).click();const response=await accepted;expect(response.status()).toBe(202);const proof=await response.json();schema('Challenge',proof);let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+proof.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);await page.getByLabel('Reauthentication code').fill(code);
 const verified=page.waitForResponse(r=>r.url().endsWith('/me/reauthentication/verify'));await page.getByRole('button',{name:'Verify and rotate session'}).press('Enter');schema('ReauthenticationResult',await(await verified).json());await expect(page.getByText(/Session verified again at/)).toBeVisible();expect((await context.cookies()).find(c=>c.name==='lmt_dev_session')!.value).not.toBe(before);expect((await(await page.request.get('/api/v1/auth/session')).json()).account.id).toBe(actor);
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 await page.goto('/account/identity-links');await page.getByLabel('Email address to link').fill('Renewed'+randomInt(100000000,999999999)+'@example.test');await page.getByRole('button',{name:'Review linking code request'}).click();const resumed=page.waitForResponse(r=>r.url().endsWith('/me/identity-links/challenges'));await page.getByRole('button',{name:'Request linking code',exact:true}).click();expect((await resumed).status()).toBe(202);
});
test('unknown reauthentication result offers session recovery without proof replay',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999));await page.goto('/account/reauthenticate');const accepted=page.waitForResponse(r=>r.url().endsWith('/me/reauthentication/challenges'));await page.getByRole('button',{name:'Request verification code'}).click();const proof=await(await accepted).json();let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+proof.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.route('**/api/v1/me/reauthentication/verify',async route=>{const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');});await page.getByLabel('Reauthentication code').fill(code);await page.getByRole('button',{name:'Verify and rotate session'}).click();await expect(page.getByText(/Do not resubmit the same proof/)).toBeVisible();await expect(page.getByRole('button',{name:'Verify and rotate session'})).toHaveCount(0);await expect(page.getByRole('button',{name:'Check current session'})).toBeVisible();
});
