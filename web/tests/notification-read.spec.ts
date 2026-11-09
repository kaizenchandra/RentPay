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
test('security notice read status survives reload and a lost response',async({page})=>{
 await signIn(page,'+919'+randomInt(100000000,999999999));await page.goto('/account/profile');await page.getByLabel('Display name').fill('Notice Reader');await page.getByRole('button',{name:'Save profile'}).click();await expect(page.getByText('Your profile was saved.',{exact:false})).toBeVisible();
 await page.goto('/account/security');await expect(page.getByText(/1 unread · 1 notices/)).toBeVisible();const list=await(await page.request.get('/api/v1/me/notifications')).json();schema('SecurityNotices',list);expect(list.items[0].readAt).toBeNull();
 let lost=false;await page.route('**/api/v1/me/notifications/*/read',async route=>{if(!lost){lost=true;const response=await route.fetch();expect(response.status()).toBe(200);schema('SecurityNotice',await response.json());await route.abort('failed');}else await route.continue();});
 await page.getByRole('button',{name:'Mark as read',exact:true}).press('Enter');await expect(page.getByRole('button',{name:'Retry same read update'})).toBeVisible();await page.getByRole('button',{name:'Retry same read update'}).click();await expect(page.getByText(/0 unread · 1 notices/)).toBeVisible();await expect(page.getByRole('button',{name:'Mark as read',exact:true})).toHaveCount(0);
 const before=await(await page.request.get('/api/v1/me/notifications')).json();await page.reload();await expect(page.getByText(/0 unread · 1 notices/)).toBeVisible();const after=await(await page.request.get('/api/v1/me/security-notifications')).json();expect(after.items[0].readAt).toBe(before.items[0].readAt);expect(after.items[0].version).toBe(2);
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});
