import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import {readFile} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
import {randomInt} from 'node:crypto';
import Ajv2020 from 'ajv/dist/2020';
import addFormats from 'ajv-formats';
import contract from '../../contracts/openapi/lmt-v1.json';
async function signIn(page:Page){await page.goto('/login');await page.getByLabel('Full name',{exact:true}).fill('Operations Browser Test');await page.getByLabel('Mobile number',{exact:true}).fill('+919'+randomInt(100000000,999999999));await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);const id=new URL(page.url()).searchParams.get('challenge');let code='';await expect.poll(async()=>{try{code=await readFile('../.local/otp-inbox/'+id+'.txt','utf8');return code.length;}catch{return 0;}}).toBe(6);await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).click();await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();}

test('commercial review requires explicit grant and loses access on revocation',async({page},testInfo)=>{
 await signIn(page);
 const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 expect(actor).toMatch(/^[0-9a-f-]{36}$/);
 await page.goto('/admin/plans');
 await expect(page.getByRole('main').getByRole('alert')).toContainText('An active commercial-review grant is required');
 const grant=(revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Browser test commercial review',...(revoke?['--revoke']:[])],{stdio:'pipe'});
 grant();
 try{
  await page.getByRole('button',{name:'Refresh versions'}).click();
  await expect(page.getByRole('heading',{name:'Workspace versions',exact:true})).toBeVisible();
  await expect(page.getByRole('article')).toHaveCount(Math.min(25,(await(await page.request.get('/api/v1/admin/plans')).json()).totalMatching));
  const response=await page.request.get('/api/v1/admin/plans');expect(response.status()).toBe(200);expect(response.headers()['cache-control']).toContain('no-store');
  const ajv=new Ajv2020({strict:false});addFormats(ajv);const check=ajv.compile({$ref:'#/components/schemas/PlanReview',components:contract.components});expect(check(await response.json()),JSON.stringify(check.errors)).toBeTruthy();
  const summary=page.locator('summary').first();await summary.focus();await page.keyboard.press('Enter');await expect(page.getByText('Historical contracts retain their own version references.').first()).toBeVisible();
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
  await page.screenshot({path:testInfo.outputPath('plan-review.png'),fullPage:true});
  const html=await page.request.get('/admin/plans');expect(html.headers()['cache-control']).toContain('no-store');expect(html.headers()['x-robots-tag']).toContain('noindex');
  await page.getByRole('link',{name:'Personal plans',exact:true}).click();await expect(page.getByText('No versions on this page.')).toBeVisible();
  grant(true);await page.getByRole('button',{name:'Refresh versions'}).click();await expect(page.getByRole('main').getByRole('alert')).toContainText('An active commercial-review grant is required');await expect(page.getByText('No versions on this page.')).toHaveCount(0);
 }finally{grant(true);}
});

test('draft confirmation recovers a lost response without publication',async({page})=>{
 await signIn(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 const grant=(revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Browser draft creation test',...(revoke?['--revoke']:['--allow-draft-creation'])],{stdio:'pipe'});
 grant();try{
  await page.goto('/admin/plans');await page.getByRole('button',{name:'Create draft from version 2 · Free',exact:true}).click();
  await page.getByRole('button',{name:'Review draft',exact:true}).click();await expect(page.getByRole('main').getByRole('alert')).toContainText('Check the highlighted');
  const name='Browser draft '+randomInt(10000000,99999999);await page.getByLabel('Draft name',{exact:true}).fill(name);await page.getByLabel('Draft reason',{exact:true}).fill('Evaluate wording in isolated browser test');await page.getByLabel('Proposed effective instant',{exact:true}).fill(new Date(Date.now()+86400000+randomInt(1,10000000)).toISOString());
  await page.getByRole('button',{name:'Review draft',exact:true}).click();await expect(page.getByRole('button',{name:'Confirm draft creation'})).toBeFocused();
  let intercepted=false;await page.route('**/api/v1/admin/plans/*/versions',async route=>{if(!intercepted){intercepted=true;const res=await route.fetch();expect(res.status()).toBe(201);await route.abort('failed');}else await route.continue();});
  await page.getByRole('button',{name:'Confirm draft creation'}).click();await expect(page.getByRole('button',{name:'Retry same draft'})).toBeVisible();await expect(page.getByLabel('Draft name',{exact:true})).toBeDisabled();await page.getByRole('button',{name:'Retry same draft'}).click();await expect(page.getByText(/Draft save recorded:/)).toBeVisible();
  let next:string|null=null;const rows:any[]=[];do{const result=await(await page.request.get('/api/v1/admin/plans'+(next?'?cursor='+next:''))).json();rows.push(...result.items.filter((v:{name:string})=>v.name===name));next=result.page.nextCursor;}while(next);expect(rows).toHaveLength(1);expect(rows[0].state).toBe('draft');expect(rows[0].monthlyAmount).toBe('0.00');
  expect(await(await page.request.get('/api/v1/public/plans')).text()).not.toContain(name);
 }finally{grant(true);}
});

test('publication review recovers one scheduled editorial publication',async({page},testInfo)=>{
 await signIn(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 const grant=(revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Sandbox publication browser test',...(revoke?['--revoke']:['--allow-draft-creation','--allow-publication'])],{stdio:'pipe'});
 grant();try{
  const code=testInfo.project.name==='desktop'?'SILVER':'GOLD';
  let cursor:string|null=null;let source:any=null;let sourceCursor:string|null=null;
  do{const result=await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json();for(const v of result.items){if(v.code===code&&v.state==='published'&&(!source||v.effectiveAt>source.effectiveAt)){source=v;sourceCursor=cursor;}}cursor=result.page.nextCursor;}while(cursor);
  expect(source).toBeTruthy();const publicBefore=await(await page.request.get('/api/v1/public/plans/'+code)).json();
  await page.goto('/admin/plans'+(sourceCursor?'?cursor='+sourceCursor:''));await page.getByRole('button',{name:`Create draft from version ${source.version} · ${source.name}`,exact:true}).click();
  const name='Sandbox publication '+testInfo.project.name+' '+randomInt(10000000,99999999);await page.getByLabel('Draft name',{exact:true}).fill(name);await page.getByLabel('Draft reason',{exact:true}).fill('Verify reviewed editorial publication');await page.getByLabel('Proposed effective instant',{exact:true}).fill(new Date(Math.max(Date.now()+365*86400000,new Date(source.effectiveAt).getTime()+86400000)).toISOString());await page.getByRole('button',{name:'Review draft',exact:true}).click();await page.getByRole('button',{name:'Confirm draft creation'}).click();await expect(page.getByText(/Draft save recorded:/)).toBeVisible();
  const saved=await page.getByText(/Draft save recorded:/).textContent();const id=saved!.match(/[0-9a-f]{8}-[0-9a-f-]{27}/)![0];
  cursor=null;let draftCursor:string|null=null;do{const result=await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json();if(result.items.some((v:{id:string})=>v.id===id)){draftCursor=cursor;break;}cursor=result.page.nextCursor;}while(cursor);
  await page.goto('/admin/plans'+(draftCursor?'?cursor='+draftCursor:''));await page.getByRole('button',{name:'Review publication · '+name,exact:true}).click();await expect(page.getByRole('heading',{name:'Review publication',exact:true})).toBeFocused();await expect(page.getByRole('heading',{name:/^Proposed version · /})).toBeVisible();
  await page.getByLabel('Publication reason',{exact:true}).fill('Approve sandbox editorial catalogue text');await page.getByRole('button',{name:'Review publication confirmation'}).click();await expect(page.getByRole('button',{name:'Confirm publication',exact:true})).toBeFocused();await page.getByRole('region',{name:'Publication review',exact:true}).screenshot({path:testInfo.outputPath('publication-review.png')});
  let lost=false;await page.route('**/api/v1/admin/plan-versions/*/activate',async route=>{if(!lost){lost=true;const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');}else await route.continue();});
  await page.getByRole('button',{name:'Confirm publication',exact:true}).click();await expect(page.getByRole('button',{name:'Retry same publication'})).toBeVisible();await page.getByRole('button',{name:'Retry same publication'}).click();await expect(page.getByText(/Publication recorded for/)).toBeVisible();
  expect((await(await page.request.get('/api/v1/public/plans/'+code)).json()).versionId).toBe(publicBefore.versionId);
  expect((await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
 }finally{grant(true);}
});

test('editorial draft edit retries one revision and preserves publication state',async({page})=>{
 await signIn(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 const grant=(revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Sandbox draft edit browser test',...(revoke?['--revoke']:['--allow-draft-creation','--allow-draft-editing'])],{stdio:'pipe'});
 grant();try{
  await page.goto('/admin/plans');await page.getByRole('button',{name:'Create draft from version 2 · Free',exact:true}).click();const name='Editable sandbox '+randomInt(10000000,99999999);await page.getByLabel('Draft name',{exact:true}).fill(name);await page.getByLabel('Draft reason',{exact:true}).fill('Prepare draft for correction test');await page.getByLabel('Proposed effective instant',{exact:true}).fill(new Date(Date.now()+86400000+randomInt(1,10000000)).toISOString());await page.getByRole('button',{name:'Review draft',exact:true}).click();await page.getByRole('button',{name:'Confirm draft creation'}).click();await expect(page.getByText(/Draft save recorded:/)).toBeVisible();const id=(await page.getByText(/Draft save recorded:/).textContent())!.match(/[0-9a-f]{8}-[0-9a-f-]{27}/)![0];
  let cursor:string|null=null;let before:any;do{const list=await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json();before=list.items.find((v:{id:string})=>v.id===id);if(before)break;cursor=list.page.nextCursor;}while(cursor);
  expect(before).toBeTruthy();await page.goto('/admin/plans'+(cursor?'?cursor='+cursor:''));await page.getByRole('button',{name:'Edit draft · '+name,exact:true}).click();await expect(page.getByRole('heading',{name:/^Edit draft /})).toBeFocused();await expect(page.getByLabel('Draft name',{exact:true})).toHaveValue(name);await page.getByLabel('Draft name',{exact:true}).fill(name+' corrected');await page.getByLabel('Draft reason',{exact:true}).fill('Correct the unpublished display text');await page.getByRole('button',{name:'Review draft',exact:true}).click();
  let lost=false;await page.route('**/api/v1/admin/plan-versions/'+id,async route=>{if(!lost){lost=true;const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');}else await route.continue();});await page.getByRole('button',{name:'Confirm draft changes'}).click();await expect(page.getByRole('button',{name:'Retry same draft'})).toBeVisible();await page.getByRole('button',{name:'Retry same draft'}).click();await expect(page.getByText(/Draft save recorded:/)).toBeVisible();
  const after=(await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json()).items.find((v:{id:string})=>v.id===id);expect(after.name).toBe(name+' corrected');expect(after.state).toBe('draft');expect(after.etag).toBe('"plan-'+id+'-2"');expect(after.monthlyAmount).toBe(before.monthlyAmount);expect(await(await page.request.get('/api/v1/public/plans')).text()).not.toContain(name);
 }finally{grant(true);}
});

test('discard keeps evidence and recovers a lost response once',async({page})=>{
 await signIn(page);const actor=(await(await page.request.get('/api/v1/auth/session')).json()).account.id;
 const grant=(revoke=false)=>execFileSync('python3',['../scripts/grant-local-commercial-review.py',actor,'--reason','Sandbox draft discard browser test',...(revoke?['--revoke']:['--allow-draft-creation','--allow-draft-editing','--allow-publication','--allow-draft-discard'])],{stdio:'pipe'});
 grant();try{
  await page.goto('/admin/plans');await page.getByRole('button',{name:'Create draft from version 2 · Free',exact:true}).click();const name='Discard sandbox '+randomInt(10000000,99999999);await page.getByLabel('Draft name',{exact:true}).fill(name);await page.getByLabel('Draft reason',{exact:true}).fill('Prepare an unused editorial proposal');await page.getByLabel('Proposed effective instant',{exact:true}).fill(new Date(Date.now()+86400000+randomInt(1,10000000)).toISOString());await page.getByRole('button',{name:'Review draft',exact:true}).click();await page.getByRole('button',{name:'Confirm draft creation'}).click();await expect(page.getByText(/Draft save recorded:/)).toBeVisible();const id=(await page.getByText(/Draft save recorded:/).textContent())!.match(/[0-9a-f]{8}-[0-9a-f-]{27}/)![0];
  let cursor:string|null=null;let before:any;do{const list=await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json();before=list.items.find((v:{id:string})=>v.id===id);if(before)break;cursor=list.page.nextCursor;}while(cursor);
  await page.goto('/admin/plans'+(cursor?'?cursor='+cursor:''));await page.getByRole('button',{name:'Discard draft · '+name,exact:true}).click();await expect(page.getByRole('heading',{name:'Discard unpublished draft',exact:true})).toBeFocused();await page.getByRole('button',{name:'Review discard confirmation'}).click();await expect(page.getByRole('main').getByRole('alert')).toContainText('10–300 characters');await page.getByLabel('Discard reason',{exact:true}).fill('Retire the unused sandbox proposal');await page.getByRole('button',{name:'Review discard confirmation'}).click();await expect(page.getByRole('button',{name:'Confirm draft discard'})).toBeFocused();
  let lost=false;await page.route('**/api/v1/admin/plan-versions/'+id+'/discard',async route=>{if(!lost){lost=true;const response=await route.fetch();expect(response.status()).toBe(200);await route.abort('failed');}else await route.continue();});await page.getByRole('button',{name:'Confirm draft discard'}).click();await expect(page.getByRole('button',{name:'Retry same discard'})).toBeVisible();await page.getByRole('button',{name:'Retry same discard'}).click();await expect(page.getByText(/Draft discard recorded:/)).toBeVisible();
  const after=(await(await page.request.get('/api/v1/admin/plans'+(cursor?'?cursor='+cursor:''))).json()).items.find((v:{id:string})=>v.id===id);expect(after.state).toBe('discarded');expect(after.name).toBe(before.name);expect(after.disclosure).toBe(before.disclosure);expect(after.etag).toBe('"plan-'+id+'-2"');await expect(page.getByRole('button',{name:'Edit draft · '+name,exact:true})).toHaveCount(0);await expect(page.getByRole('button',{name:'Review publication · '+name,exact:true})).toHaveCount(0);await expect(page.getByRole('button',{name:'Discard draft · '+name,exact:true})).toHaveCount(0);expect(await(await page.request.get('/api/v1/public/plans')).text()).not.toContain(name);
 }finally{grant(true);}
});
