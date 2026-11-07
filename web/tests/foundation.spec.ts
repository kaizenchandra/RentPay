import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile} from 'node:fs/promises';
import {randomInt} from 'node:crypto';
import Ajv2020 from 'ajv/dist/2020';
import addFormats from 'ajv-formats';
import contract from '../../contracts/openapi/lmt-v1.json';
const ajv=new Ajv2020({strict:false});addFormats(ajv);
function schema(name:string,value:unknown){const check=ajv.compile({$ref:`#/components/schemas/${name}`,components:contract.components});expect(check(value),JSON.stringify(check.errors)).toBeTruthy();}
async function signIn(page:Page){
 await page.goto('/login');
 await page.getByLabel('Full name',{exact:true}).fill('Sandbox Test Owner');
 await page.getByLabel('Mobile number',{exact:true}).fill('+919'+randomInt(100000000,999999999));
 const response=page.waitForResponse(r=>r.url().endsWith('/auth/challenges')&&r.request().method()==='POST');
 await page.getByRole('button',{name:'Request verification code'}).click();
 expect((await response).status()).toBe(202);
 await expect(page).toHaveURL(/verify-otp/);
 const id=new URL(page.url()).searchParams.get('challenge');
 const challenge=await (await page.request.get('/api/v1/auth/challenges/'+id)).json();schema('Challenge',challenge);
 let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+challenge.id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);
 await page.getByLabel('Six-digit verification code').fill(code);
 await page.getByRole('button',{name:'Verify and continue'}).click();
 await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
}
test('sandbox identity → atomic Free workspace → reload, context and logout',async({page,context})=>{
 await signIn(page);
 const cookies=await context.cookies();const cookie=cookies.find(c=>c.name==='lmt_dev_session');expect(cookie?.httpOnly).toBe(true);expect(cookie?.sameSite).toBe('Lax');
 await page.getByRole('link',{name:'Create a Free workspace'}).click();
 await expect(page.getByRole('heading',{name:'Free · ₹0.00'})).toBeVisible();
 await page.getByLabel('Workspace or organization name').fill('Sandbox E2E '+Date.now());
 await page.getByLabel('City',{exact:true}).fill('Pune');await page.getByLabel('State / union territory').fill('Maharashtra');
 await page.getByRole('checkbox').check();
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 const created=page.waitForResponse(r=>r.url().endsWith('/workspaces')&&r.request().method()==='POST');
 await page.getByRole('button',{name:'Confirm and create Free workspace'}).click();
 const response=await created;expect(response.status()).toBe(201);
 await expect(page).toHaveURL(/setup\?workspace=/);
 const csrf=await (await page.request.get('/api/v1/auth/csrf')).json();
 const replay=await page.request.post('/api/v1/workspaces',{data:response.request().postDataJSON(),headers:{Origin:'http://127.0.0.1:3000','X-CSRF-Token':csrf.token,'Idempotency-Key':response.request().headers()['idempotency-key']}});
 expect(replay.status()).toBe(201);const result=await replay.json();schema('CreatedWorkspace',result);
 await expect(page).toHaveURL(new RegExp(result.id));
 await expect(page.getByText('Your Free workspace is provisioned',{exact:true})).toBeVisible();
 const setup=await page.request.get('/api/v1/workspaces/'+result.id+'/setup');expect(setup.status()).toBe(200);schema('Setup',await setup.json());expect(setup.headers()['cache-control']).toContain('no-store');
 await page.reload();await expect(page.getByRole('heading',{name:'Setup progress'})).toBeVisible();
 await expect(page.getByRole('cell',{name:'5',exact:true})).toBeVisible();
 expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
 const fresh=await context.newPage();await fresh.goto('/account');await expect(fresh.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Sign out'}).click();await expect(page).toHaveURL(/login/);await expect(fresh).toHaveURL(/login/);
 expect((await page.request.get('/api/v1/workspaces/'+result.id+'/setup')).status()).toBe(401);
});
test('validation does not send a challenge and private shell is not cached',async({page})=>{
 const response=await page.goto('/login');expect(response?.headers()['cache-control']).toContain('no-store');expect(response?.headers()['x-robots-tag']).toContain('noindex');
 await page.getByRole('button',{name:'Request verification code'}).click();await expect(page.getByText('Enter your full name.',{exact:true})).toBeVisible();
 await expect(page.getByText('Use +91 followed by a ten-digit Indian mobile number.',{exact:true})).toBeVisible();
});
