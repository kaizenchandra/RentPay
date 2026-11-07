import {execFileSync} from 'node:child_process';
import {readFile} from 'node:fs/promises';
import {randomInt,randomUUID} from 'node:crypto';
import {chromium} from 'playwright';
import assert from 'node:assert/strict';
const browser=await chromium.launch();const context=await browser.newContext();
const request=context.request;const origin='http://127.0.0.1:3000';
async function get(path){return request.get(origin+'/api/v1'+path);}
async function post(path,data,key){const csrf=await(await get('/auth/csrf')).json();return request.post(origin+'/api/v1'+path,{data,headers:{Origin:origin,'X-CSRF-Token':csrf.token,...(key?{'Idempotency-Key':key}:{})}});}
try{
 const challenge=await(await post('/auth/challenges',{channel:'mobile',destination:'+919'+randomInt(100000000,999999999),purpose:'sign_in',displayName:'Sandbox recovery test'},randomUUID())).json();
 let code;for(let i=0;i<50;i++){try{code=await readFile('../.local/otp-inbox/'+challenge.id+'.txt','utf8');if(code.length===6)break;}catch{}await new Promise(resolve=>setTimeout(resolve,100));}
 assert.equal((await post('/auth/challenges/'+challenge.id+'/verify',{code})).status(),200);
 const free=await(await get('/public/plans/FREE')).json();const key=randomUUID();const body={profile:{name:'Sandbox recovery workspace',customerType:'individual',city:'Pune',state:'Maharashtra'},selectedPlanVersionId:free.versionId,consentIds:['sandbox-use-v1']};
 const created=await(await post('/workspaces',body,key)).json();assert.ok(created.id);
 const inviteKey=randomUUID();const inviteBody={mobile:'+919'+randomInt(100000000,999999999)};
 const invitation=await(await post('/workspaces/'+created.id+'/invitations',inviteBody,inviteKey)).json();assert.ok(invitation.resourceId);
 const managerContext=await browser.newContext();const managerRequest=managerContext.request;
 async function managerPost(path,data,key,etag){const csrf=await(await managerRequest.get(origin+'/api/v1/auth/csrf')).json();return managerRequest.post(origin+'/api/v1'+path,{data,headers:{Origin:origin,'X-CSRF-Token':csrf.token,...(key?{'Idempotency-Key':key}:{}),...(etag?{'If-Match':etag}:{})}});}
 const managerChallenge=await(await managerPost('/auth/challenges',{channel:'mobile',destination:inviteBody.mobile,purpose:'sign_in',displayName:'Recovery Manager'},randomUUID())).json();
 let managerCode;for(let i=0;i<50;i++){try{managerCode=await readFile('../.local/otp-inbox/'+managerChallenge.id+'.txt','utf8');if(managerCode.length===6)break;}catch{}await new Promise(resolve=>setTimeout(resolve,100));}
 assert.equal((await managerPost('/auth/challenges/'+managerChallenge.id+'/verify',{code:managerCode})).status(),200);
 const pending=await(await managerRequest.get(origin+'/api/v1/me/invitations')).json();
 assert.equal((await managerPost('/me/invitations/'+invitation.resourceId+'/accept',{},randomUUID(),pending.items[0].etag)).status(),200);
 const usageBefore=await(await get('/workspaces/'+created.id+'/usage/history')).json();assert.equal(usageBefore.totalMatching,1);
 const operationsBefore=await(await get('/workspaces/'+created.id+'/operations')).json();assert.equal(operationsBefore.totalMatching,1);const operationId=operationsBefore.items[0].id;
 execFileSync('docker',['compose','-f','../compose.yaml','stop','db'],{stdio:'inherit'});
 const failed=await get('/workspaces/'+created.id+'/setup');assert.equal(failed.status(),503);assert.equal((await failed.json()).code,'SERVICE_UNAVAILABLE');
 execFileSync('docker',['compose','-f','../compose.yaml','up','-d','--wait','db'],{stdio:'inherit'});
 const replay=await post('/workspaces',body,key);assert.equal(replay.status(),201);assert.equal((await replay.json()).id,created.id);
 assert.equal((await get('/workspaces/'+created.id+'/setup')).status(),200);
 const invitationReplay=await post('/workspaces/'+created.id+'/invitations',inviteBody,inviteKey);assert.equal(invitationReplay.status(),201);assert.equal((await invitationReplay.json()).resourceId,invitation.resourceId);
 const inbox=await(await get('/workspaces/'+created.id+'/invitations')).json();assert.equal(inbox.totalMatching,1);assert.equal(inbox.items[0].id,invitation.resourceId);
 const usageAfter=await(await get('/workspaces/'+created.id+'/usage/history')).json();assert.equal(usageAfter.totalMatching,1);assert.equal(usageAfter.items[0].id,usageBefore.items[0].id);
 const snapshot=await(await get('/workspaces/'+created.id+'/entitlements')).json();const managerQuota=snapshot.quotas.find(q=>q.resource==='managers');assert.equal(managerQuota.used,1);assert.equal(managerQuota.reconciled,true);
 const operationsAfter=await(await get('/workspaces/'+created.id+'/operations')).json();assert.equal(operationsAfter.totalMatching,1);assert.equal(operationsAfter.items[0].id,operationId);
 let operation;for(let i=0;i<50;i++){operation=await(await get('/operations/'+operationId)).json();if(operation.operation.status==='delivered')break;await new Promise(resolve=>setTimeout(resolve,200));}assert.equal(operation.operation.status,'delivered');
 const deliveredSetup=await(await get('/workspaces/'+created.id+'/setup')).json();assert.equal(deliveredSetup.notifications.length,1);
 const page=await context.newPage();await page.goto(origin+'/setup?workspace='+created.id);await page.getByText('Your Free workspace is provisioned',{exact:true}).waitFor();await page.screenshot({path:'/tmp/lmt-workspace.png',fullPage:true});
 await post('/auth/logout',{});
 console.log('PASS: persisted session and workspace survive DB restart; protected read returns safe 503; provisioning/invitation replay and reconciled manager usage and welcome operation survive without duplicate records.');
}finally{execFileSync('docker',['compose','-f','../compose.yaml','up','-d','--wait','db'],{stdio:'inherit'});await browser.close();}
