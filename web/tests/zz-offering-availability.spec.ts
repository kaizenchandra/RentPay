import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
import {randomInt,randomUUID} from 'node:crypto';
async function signIn(page:Page){await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('Operations Browser Test');await page.getByLabel('Mobile number',{exact:true}).fill('+919'+randomInt(100000000,999999999));await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();}


// Mutates the shared local public catalogue temporarily: run the full suite with --workers=1.
test('offering withdrawal and restoration retain contracts and recover uncertain responses',async({page},testInfo)=>{
 await signIn(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 const id=testInfo.project.name==='desktop'?'00000000-0000-4000-8000-000000000004':'00000000-0000-4000-8000-000000000005';const code=testInfo.project.name==='desktop'?'PLATINUM':'DIAMOND';const name=testInfo.project.name==='desktop'?'Platinum':'Diamond';const endpoint='/api/v1/admin/plan-versions/'+id+'/availability';
 const grant=(write=false,revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Sandbox offering availability browser test',...(revoke?['--revoke']:write?['--allow-offering-management']:[])],{stdio:'pipe'});
 grant();let changed=false;try{
  await page.goto('/admin/plans');await page.getByRole('button',{name:'Offering availability · '+name+' · 1',exact:true}).click();await expect(page.getByRole('heading',{name:'Offering availability',exact:true})).toBeFocused();await expect(page.getByText(/Review-only access/)).toBeVisible();
  const initial=await(await page.request.get(endpoint)).json();expect(initial.available).toBe(true);grant(true);await page.getByRole('button',{name:'Refresh availability'}).click();await page.getByLabel('Availability reason',{exact:true}).fill('Temporarily withdraw the sandbox catalogue offering');await page.getByRole('button',{name:'Review withdrawal'}).click();await expect(page.getByRole('button',{name:'Confirm offering withdrawal'})).toBeFocused();
  let lost=false;await page.route('**/api/v1/admin/plan-versions/'+id+'/availability',async route=>{if(route.request().method()==='POST'&&!lost){lost=true;const response=await route.fetch();expect(response.status()).toBe(200);changed=true;await route.abort('failed');}else await route.continue();});
  await page.getByRole('button',{name:'Confirm offering withdrawal'}).click();await expect(page.getByRole('button',{name:'Retry same availability change'})).toBeVisible();await page.getByRole('button',{name:'Retry same availability change'}).click();await expect(page.getByText(/Offering availability change recorded/)).toBeVisible();expect((await page.request.get('/api/v1/public/plans/'+code)).status()).toBe(404);
  await page.goto('/pricing');await expect(page.getByRole('heading',{name,exact:true})).toHaveCount(0);
  await page.goto('/admin/plans');await page.getByRole('button',{name:'Offering availability · '+name+' · 1',exact:true}).click();await expect(page.getByText('Withdrawn from the published catalogue',{exact:true})).toBeVisible();await page.getByLabel('Availability reason',{exact:true}).fill('Restore the verified sandbox offering');await page.getByRole('button',{name:'Review restoration'}).click();await page.getByRole('button',{name:'Confirm offering restoration'}).click();await expect(page.getByText(/Offering availability change recorded/)).toBeVisible();changed=false;expect((await page.request.get('/api/v1/public/plans/'+code)).status()).toBe(200);
  await page.getByRole('button',{name:'Offering availability · Free · 2',exact:true}).click();await expect(page.getByText(/This offering cannot be withdrawn here/)).toBeVisible();await expect(page.getByRole('button',{name:'Review withdrawal'})).toHaveCount(0);expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 }finally{
  if(changed){const state=await(await page.request.get(endpoint)).json();if(!state.available){const csrf=await(await page.request.get('/api/v1/auth/csrf')).json();const restored=await page.request.post(endpoint,{data:{available:true,reason:'Restore after sandbox browser verification'},headers:{Origin:'http://127.0.0.1:3000','X-CSRF-Token':csrf.token,'If-Match':state.etag,'Idempotency-Key':randomUUID()}});expect(restored.status()).toBe(200);}}
  grant(false,true);
 }
});
