import {containerCode} from './container-code.mjs';
// Actual browser/API/worker regression in the local sandbox. Not real provider receipt evidence.
import {chromium,expect} from '@playwright/test';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {randomInt} from 'node:crypto';
const wrapper=fileURLToPath(new URL('../../scripts/compose-local.sh',import.meta.url));
const browser=await chromium.launch();
try{for(const viewport of [{width:1280,height:900},{width:390,height:844}]){for(const channel of ['mobile','email']){
 const context=await browser.newContext({baseURL:'http://127.0.0.1:3100',viewport});const page=await context.newPage();const contact=channel==='mobile'?'+919'+randomInt(100000000,999999999):'Signin'+Date.now()+randomInt(1000,9999)+'@example.test';let account;
 for(let attempt=0;attempt<2;attempt++){
  await page.goto('/login');await expect(page.getByText('Test environment',{exact:true})).toBeVisible();await page.getByLabel('Verification channel').selectOption(channel);await page.getByLabel('Full name',{exact:true}).fill('Sandbox Channel Test');await page.getByLabel(channel==='email'?'Email address':'Mobile number',{exact:true}).fill(contact);await page.getByRole('button',{name:'Request verification code'}).click();await expect(page).toHaveURL(/verify-otp/);const id=new URL(page.url()).searchParams.get('challenge');let code='';
  await expect.poll(async()=>{try{code=await containerCode(id);return code.length;}catch{return 0;}},{timeout:30000}).toBe(6);
  await page.getByLabel('Six-digit verification code').fill(code);await page.getByRole('button',{name:'Verify and continue'}).press('Enter');await expect(page.getByRole('heading',{name:'Your workspaces',exact:true})).toBeVisible();const result=await(await page.request.get('/api/v1/auth/session')).json();expect(result.mode).toBe('sandbox');if(account)expect(result.account.id).toBe(account);account=result.account.id;expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();await page.getByRole('button',{name:'Sign out',exact:true}).click();await expect(page).toHaveURL(/login/);
 }
 await context.close();
}}console.log('PASS: desktop/mobile SMS-channel and email-channel sandbox registration, returning-account login, runtime banner, keyboard submission and sign-out.');}finally{await browser.close();}
