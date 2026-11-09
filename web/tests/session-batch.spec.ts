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
test('reviewed batch sign-out recovers a lost result and preserves current session',async({page,browser})=>{
 const mobile='+919'+randomInt(100000000,999999999);await signIn(page,mobile);const contexts=await Promise.all([browser.newContext({baseURL:'http://127.0.0.1:3000'}),browser.newContext({baseURL:'http://127.0.0.1:3000'})]);
 try{const others=await Promise.all(contexts.map(c=>c.newPage()));for(const p of others)await signIn(p,mobile);
 await page.goto('/account/security');await page.getByRole('link',{name:'End selected sessions',exact:true}).click();await expect(page.getByRole('checkbox')).toHaveCount(2);await page.getByRole('checkbox').nth(0).check();await page.getByRole('checkbox').nth(1).check();
 const reviewed=page.waitForResponse(r=>r.url().endsWith('/me/session-revocation-preview'));await page.getByRole('button',{name:'Review selected sign-out'}).click();schema('SessionRevocationPreview',await(await reviewed).json());await expect(page.getByRole('button',{name:'Confirm selected sign-out'})).toBeFocused();
 expect((await others[0].request.get('/api/v1/me/profile')).status()).toBe(200);expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 let lost=false;await page.route('**/api/v1/me/session-revocations',async route=>{if(!lost){lost=true;const r=await route.fetch();expect(r.status()).toBe(200);schema('BatchRevokedSessions',await r.json());await route.abort('failed');}else await route.continue();});
 await page.getByRole('button',{name:'Confirm selected sign-out'}).press('Enter');await expect(page.getByRole('button',{name:'Retry same batch sign-out'})).toBeVisible();await page.getByRole('button',{name:'Retry same batch sign-out'}).click();await expect(page.getByText(/2 selected sessions were signed out/)).toBeVisible();
 for(const p of others)expect((await p.request.get('/api/v1/me/profile')).status()).toBe(401);expect((await page.request.get('/api/v1/me/profile')).status()).toBe(200);await page.reload();await expect(page.getByText('No other active sessions. Your current session can be ended with Sign out.')).toBeVisible();const notices=await(await page.request.get('/api/v1/me/notifications')).json();expect(notices.totalMatching).toBe(1);
 }finally{for(const c of contexts)await c.close();}
});
