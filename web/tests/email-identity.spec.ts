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
async function emailSignIn(page:Page,email:string){
 await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('Email Browser Owner');await page.getByLabel('Verification channel').selectOption('email');await page.getByLabel('Email address',{exact:true}).fill(email);await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await expect(page.getByText(/No SMS or email is sent/)).toBeVisible();await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}
async function provision(page:Page){
 await page.getByRole('link',{name:'Create a Free workspace'}).click();await page.getByLabel('Workspace or organization name').fill('Membership E2E');await page.getByLabel('City',{exact:true}).fill('Pune');await page.getByLabel('State / union territory').fill('Maharashtra');await page.getByRole('checkbox').check();await page.getByRole('button',{name:'Confirm and create Free workspace'}).click();await expect(page.getByRole('heading',{name:'Setup progress'})).toBeVisible();const w=new URL(page.url()).searchParams.get('workspace')!;await page.getByRole('link',{name:'Members and invitations'}).click();await expect(page.getByRole('heading',{name:'Members and invitations',exact:true})).toBeVisible();return w;
}
test('email proof provisions and returns to the same workspace without mobile identity',async({page})=>{
 const email='Browser'+randomInt(100000000,999999999)+'@EXAMPLE.test';await emailSignIn(page,email);const account=(await (await page.request.get('/api/v1/auth/session')).json()).account.id;const w=await provision(page);
 await page.goto('/account/profile');await expect(page.getByText(/Email: B•••@example.test/)).toBeVisible();const profile=await(await page.request.get('/api/v1/me/profile')).json();schema('Profile',profile);expect(profile.maskedMobile).toBeNull();
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 await page.getByRole('button',{name:'Sign out',exact:true}).click();await expect(page).toHaveURL(/login/);await emailSignIn(page,email.replace('EXAMPLE','example'));expect((await(await page.request.get('/api/v1/auth/session')).json()).account.id).toBe(account);
 await page.goto('/workspace/billing/usage?workspace='+w);await expect(page.getByRole('heading',{name:'Membership E2E',exact:true})).toBeVisible();await expect(page.getByText('Reconciled with membership, allocations and usage events.',{exact:true})).toBeVisible();
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});

async function signIn(page:Page,mobile:string,name:string){
 await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill(name);await page.getByLabel('Mobile number',{exact:true}).fill(mobile);await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}

test('mobile owner explicitly links email and recovers one confirmation',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999),'Link Owner');const w=await provision(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;const email='LinkBrowser'+randomInt(100000000,999999999)+'@example.test';
 await page.goto('/account/profile');await page.getByRole('link',{name:'Link an unused email for sign-in'}).click();await page.getByLabel('Email address to link').fill(email);await page.getByRole('button',{name:'Review linking code request'}).click();await expect(page.getByRole('heading',{name:'Confirm linking code request'})).toBeFocused();
 const accepted=page.waitForResponse(r=>r.url().endsWith('/me/identity-links/challenges')&&r.request().method()==='POST');await page.getByRole('button',{name:'Request linking code',exact:true}).click();const response=await accepted;expect(response.status()).toBe(202);const challenge=await response.json();schema('Challenge',challenge);let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+challenge.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Linking verification code').fill(code);await page.getByRole('button',{name:'Review email link'}).click();await expect(page.getByRole('heading',{name:'Confirm email link'})).toBeFocused();let lose=true;await page.route('**/api/v1/me/identity-links',async route=>{if(lose&&route.request().method()==='POST'){lose=false;const result=await route.fetch();expect(result.status()).toBe(200);await route.abort('failed');}else await route.continue();});
 await page.getByRole('button',{name:'Confirm email link',exact:true}).click();await expect(page.getByRole('button',{name:'Retry same linking step'})).toBeVisible();await page.getByRole('button',{name:'Retry same linking step'}).click();await expect(page.getByText('Email linked to this account. Your existing workspaces are preserved.',{exact:true})).toBeVisible();
 const notices=await(await page.request.get('/api/v1/me/security-notifications')).json();expect(notices.items.filter((n:{message:string})=>n.message.startsWith('An email sign-in contact'))).toHaveLength(1);
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 await page.getByRole('button',{name:'Sign out',exact:true}).click();await expect(page).toHaveURL(/login/);await emailSignIn(page,email);expect((await(await page.request.get('/api/v1/auth/session')).json()).account.id).toBe(actor);await page.goto('/workspace/billing/usage?workspace='+w);await expect(page.getByRole('heading',{name:'Membership E2E',exact:true})).toBeVisible();
});

test('without JavaScript contact forms do not put submitted details in the URL',async({browser})=>{
 const context=await browser.newContext({javaScriptEnabled:false,baseURL:'http://127.0.0.1:3000'});try{const page=await context.newPage();await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('No script test');await page.getByLabel('Mobile number',{exact:true}).fill('+919123456789');const request=page.waitForRequest(r=>r.isNavigationRequest()&&r.method()==='POST');await page.getByRole('button',{name:'Request verification code'}).click();const sent=await request;expect(new URL(sent.url()).search).toBe('');expect(page.url()).not.toContain('9123456789');}finally{await context.close();}
});

test('email owner links mobile once and returns to the same workspace by mobile',async({page})=>{
 const email='Reverse'+randomInt(100000000,999999999)+'@example.test',mobile='+919'+randomInt(100000000,999999999);await emailSignIn(page,email);const w=await provision(page),actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 await page.goto('/account/profile');await page.getByRole('link',{name:'Link an unused mobile for sign-in'}).click();await page.getByLabel('Mobile number to link').fill(mobile);await page.getByRole('button',{name:'Review linking code request'}).click();await expect(page.getByRole('heading',{name:'Confirm linking code request'})).toBeFocused();
 const accepted=page.waitForResponse(r=>r.url().endsWith('/me/identity-links/challenges')&&r.request().method()==='POST');await page.getByRole('button',{name:'Request linking code',exact:true}).click();const response=await accepted;expect(response.status()).toBe(202);const challenge=await response.json();schema('Challenge',challenge);let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+challenge.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Linking verification code').fill(code);await page.getByRole('button',{name:'Review mobile link'}).click();await expect(page.getByRole('heading',{name:'Confirm mobile link'})).toBeFocused();let lose=true;await page.route('**/api/v1/me/identity-links',async route=>{if(lose&&route.request().method()==='POST'){lose=false;const result=await route.fetch();expect(result.status()).toBe(200);await route.abort('failed');}else await route.continue();});
 await page.getByRole('button',{name:'Confirm mobile link',exact:true}).click();await expect(page.getByRole('button',{name:'Retry same linking step'})).toBeVisible();await page.getByRole('button',{name:'Retry same linking step'}).click();await expect(page.getByText('Mobile linked to this account. Your existing workspaces are preserved.',{exact:true})).toBeVisible();
 const profile=await(await page.request.get('/api/v1/me/profile')).json();schema('Profile',profile);expect(profile.id).toBe(actor);expect(profile.maskedEmail).not.toBeNull();expect(profile.maskedMobile).toContain(mobile.slice(-4));const notices=await(await page.request.get('/api/v1/me/security-notifications')).json();expect(notices.items.filter((n:{message:string})=>n.message.startsWith('A mobile sign-in contact'))).toHaveLength(1);
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 await page.getByRole('button',{name:'Sign out',exact:true}).click();await expect(page).toHaveURL(/login/);await signIn(page,mobile,'Returning linked owner');expect((await(await page.request.get('/api/v1/auth/session')).json()).account.id).toBe(actor);await page.goto('/workspace/billing/usage?workspace='+w);await expect(page.getByRole('heading',{name:'Membership E2E',exact:true})).toBeVisible();
});
