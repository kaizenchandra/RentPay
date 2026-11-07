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
test('profile persists, rejects stale edits and exposes accessible recovery',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999));await page.getByRole('link',{name:'Edit profile',exact:true}).click();
 await expect(page.getByLabel('Display name')).toHaveValue('Account Test');
 await page.getByLabel('Display name').fill('Saved Owner');await page.getByRole('button',{name:'Save profile'}).click();await expect(page.getByText('Your profile was saved.',{exact:true})).toBeVisible();
 await page.reload();await expect(page.getByLabel('Display name')).toHaveValue('Saved Owner');
 const initial=await (await page.request.get('/api/v1/me/profile')).json();schema('Profile',initial);
 const csrf=await (await page.request.get('/api/v1/auth/csrf')).json();
 const response=await page.request.patch('/api/v1/me/profile',{data:{displayName:'Changed elsewhere'},headers:{Origin:'http://127.0.0.1:3000','X-CSRF-Token':csrf.token,'If-Match':initial.etag,'Idempotency-Key':crypto.randomUUID()}});expect(response.status()).toBe(200);
 await page.getByLabel('Display name').fill('Stale edit');await page.getByRole('button',{name:'Save profile'}).click();await expect(page.getByRole('button',{name:'Reload latest profile and discard my edit'})).toBeVisible();
 await expect(page.getByLabel('Display name')).toHaveValue('Stale edit');await page.getByRole('button',{name:'Reload latest profile and discard my edit'}).click();await expect(page.getByLabel('Display name')).toHaveValue('Changed elsewhere');
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});
test('confirmed session revocation invalidates the other browser and records notice',async({page,browser},testInfo)=>{
 const mobile='+919'+randomInt(100000000,999999999);await signIn(page,mobile);
 const other=await browser.newContext({baseURL:'http://127.0.0.1:3000'});try{
  const second=await other.newPage();await signIn(second,mobile);
  await page.getByRole('link',{name:'Sessions and security',exact:true}).click();await expect(page.getByText(/2 active sessions/)).toBeVisible();
  schema('Sessions',await (await page.request.get('/api/v1/me/sessions')).json());
  await page.getByRole('button',{name:'End session',exact:true}).click();await expect(page.getByRole('button',{name:'Confirm end session'})).toBeFocused();await page.getByRole('button',{name:'Cancel',exact:true}).click();expect((await second.request.get('/api/v1/me/profile')).status()).toBe(200);
  await page.getByRole('button',{name:'End session',exact:true}).click();const revoked=page.waitForResponse(r=>r.url().endsWith('/revoke'));await page.getByRole('button',{name:'Confirm end session'}).press('Enter');const response=await revoked;expect(response.status()).toBe(200);schema('RevokedSession',await response.json());
  await expect(page.getByText(/1 active sessions/)).toBeVisible();expect((await second.request.get('/api/v1/me/profile')).status()).toBe(401);expect((await page.request.get('/api/v1/me/profile')).status()).toBe(200);
  schema('SecurityNotices',await (await page.request.get('/api/v1/me/security-notifications')).json());await expect(page.getByText(/Another browser session was signed out from account security/)).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
  await page.screenshot({path:testInfo.outputPath('account-security.png'),fullPage:true});
 }finally{await other.close();}
});

test('lost profile response retries the committed command without a second update',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999));await page.goto('/account/profile');await expect(page.getByLabel('Display name')).toHaveValue('Account Test');
 let loseResponse=true;
 await page.route('**/api/v1/me/profile',async route=>{
  if(route.request().method()==='PATCH'&&loseResponse){loseResponse=false;const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');}else await route.continue();
 });
 await page.getByLabel('Display name').fill('Recovered change');await page.getByRole('button',{name:'Save profile'}).click();await expect(page.getByRole('button',{name:'Retry same change'})).toBeVisible();await expect(page.getByLabel('Display name')).toBeDisabled();
 await page.getByRole('button',{name:'Retry same change'}).click();await expect(page.getByText('Your profile was saved.',{exact:true})).toBeVisible();
 const saved=await (await page.request.get('/api/v1/me/profile')).json();expect(saved.displayName).toBe('Recovered change');expect(saved.version).toBe(2);
 const notices=await (await page.request.get('/api/v1/me/security-notifications')).json();expect(notices.totalMatching).toBe(1);
});
