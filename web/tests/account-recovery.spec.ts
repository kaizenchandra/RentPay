import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile} from 'node:fs/promises';
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
for(const lost of [false,true])test(lost?'lost recovery response offers session check without proof replay':'linked-contact recovery preserves account and ends previous sessions',async({page,browser})=>{
 const old=await browser.newContext({baseURL:'http://127.0.0.1:3000'});try{const original=await old.newPage();const mobile='+919'+randomInt(100000000,999999999);await signIn(original,mobile);const actor=(await(await original.request.get('/api/v1/auth/session')).json()).account.id;
 await page.goto('/login');await page.getByRole('link',{name:'Recover an existing account and end its sessions'}).click();await page.getByLabel('Linked mobile number').fill(mobile);const requested=page.waitForResponse(r=>r.url().endsWith('/auth/recovery/challenges'));await page.getByRole('button',{name:'Request recovery code'}).click();const r=await requested;expect(r.status()).toBe(202);const proof=await r.json();schema('RecoveryChallenge',proof);let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+proof.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Recovery code',{exact:true}).fill(code);await page.getByRole('checkbox').check();expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 if(lost)await page.route('**/api/v1/auth/recovery/verify',async route=>{const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');});
 await page.getByRole('button',{name:'Confirm recovery and end sessions'}).press('Enter');
 if(lost){await expect(page.getByText(/Do not resubmit this proof/)).toBeVisible();await expect(page.getByRole('button',{name:'Confirm recovery and end sessions'})).toHaveCount(0);await expect(page.getByRole('link',{name:'Check current session'})).toBeVisible();await page.context().clearCookies();const checked=page.waitForResponse(r=>r.url().endsWith('/auth/recovery/outcome'));await page.getByRole('button',{name:'Check recovery outcome'}).click();schema('RecoveryOutcome',await(await checked).json());await expect(page.getByText(/Recovery completed. Existing sessions were ended/)).toBeVisible();expect((await page.request.get('/api/v1/me/profile')).status()).toBe(401);}
 else{await expect(page.getByText(/Account recovered. Existing sessions were ended/)).toBeVisible();const session=await(await page.request.get('/api/v1/auth/session')).json();schema('Session',session);expect(session.account.id).toBe(actor);await page.getByRole('link',{name:'Review account security'}).click();await expect(page.getByText(/Account recovery through a linked contact ended existing browser sessions/)).toBeVisible();}
 expect((await original.request.get('/api/v1/me/profile')).status()).toBe(401);
 }finally{await old.close();}
});
