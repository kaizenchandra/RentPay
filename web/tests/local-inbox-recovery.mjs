import {request,expect} from '@playwright/test';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {randomUUID,randomInt} from 'node:crypto';
const wrapper=fileURLToPath(new URL('../../scripts/compose-local.sh',import.meta.url));
const compose=(...args)=>execFileSync(wrapper,args,{stdio:['ignore','pipe','pipe']});
const base='http://127.0.0.1:8026';const id=randomUUID();
const body={id,destination:'+919'+randomInt(100000000,999999999),text:'Local simulator storage test'};
const post=(data,headers={})=>fetch(base+'/messages',{method:'POST',headers:{'Content-Type':'application/json',...headers},body:JSON.stringify(data)});
expect((await post(body)).status).toBe(201);expect((await post(body)).status).toBe(200);
expect((await post({...body,text:'Changed message'})).status).toBe(409);
expect((await post(body,{Origin:'http://untrusted.test'})).status).toBe(403);
compose('restart','sms-inbox');await expect.poll(async()=>{try{return (await fetch(base+'/healthz')).status;}catch{return 0;}},{timeout:30000}).toBe(200);
expect((await(await fetch(base+'/messages')).json()).messages.filter(m=>m.id===id)).toHaveLength(1);
const client=await request.newContext({baseURL:'http://127.0.0.1:3100',extraHTTPHeaders:{Origin:'http://127.0.0.1:3100'}});
try{
 compose('stop','sms-inbox');
 const csrf=await(await client.get('/api/v1/auth/csrf')).json();
 const response=await client.post('/api/v1/auth/challenges',{headers:{[csrf.headerName]:csrf.token,'Idempotency-Key':randomUUID()},data:{channel:'mobile',destination:'+919'+randomInt(100000000,999999999),purpose:'sign_in',displayName:'Inbox outage test'}});
 expect(response.status()).toBe(202);const challenge=await response.json();
 await expect.poll(async()=> (await(await client.get('/api/v1/auth/challenges/'+challenge.id)).json()).status,{timeout:30000}).toBe('unknown');
 expect((await(await client.get('/api/v1/auth/session')).json()).authenticated).toBe(false);
 console.log('PASS: durable SMS inbox, duplicate suppression, changed-body conflict, cross-origin write denial and unavailable-inbox unknown outcome without authentication.');
}finally{compose('start','sms-inbox');await expect.poll(async()=>{try{return (await fetch(base+'/healthz')).status;}catch{return 0;}},{timeout:30000}).toBe(200);await client.dispose();}
