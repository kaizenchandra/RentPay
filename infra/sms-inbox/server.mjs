// Local-only SMS simulator. No carrier, relay, or external network calls.
import http from 'node:http';
import {DatabaseSync} from 'node:sqlite';
const db=new DatabaseSync('/data/messages.sqlite');
db.exec('PRAGMA journal_mode=WAL; CREATE TABLE IF NOT EXISTS messages(id TEXT PRIMARY KEY,destination TEXT NOT NULL,text TEXT NOT NULL,created INTEGER NOT NULL)');
const prune=()=>db.prepare('DELETE FROM messages WHERE created < ?').run(Date.now()-360000);
prune();setInterval(prune,30000).unref();
const escape=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
http.createServer(async(req,res)=>{
 const reply=(status,body,type='application/json')=>{res.writeHead(status,{'Content-Type':type,'Cache-Control':'no-store','X-Content-Type-Options':'nosniff','Content-Security-Policy':"default-src 'none'; style-src 'unsafe-inline'; frame-ancestors 'none'"});res.end(type==='application/json'?JSON.stringify(body):body);};
 if(!['localhost:8026','127.0.0.1:8026','sms-inbox:8080','127.0.0.1:8080'].includes(req.headers.host))return reply(403,{});
 try{
  if(req.method==='GET'&&req.url==='/healthz')return reply(200,{status:'ok'});
  prune();
  if(req.method==='POST'&&req.url==='/messages'){
   if(req.headers.origin||req.headers['content-type']!=='application/json')return reply(403,{});
   let body='';for await(const chunk of req){body+=chunk;if(Buffer.byteLength(body)>4096)return reply(413,{});}
   let m;try{m=JSON.parse(body);}catch{return reply(400,{});}
   if(!/^[0-9a-f-]{36}$/.test(m.id)||!/^\+91[6-9][0-9]{9}$/.test(m.destination)||typeof m.text!=='string'||m.text.length>500)return reply(422,{});
   const previous=db.prepare('SELECT * FROM messages WHERE id=?').get(m.id);
   if(previous)return reply(previous.destination===m.destination&&previous.text===m.text?200:409,{id:m.id});
   if(db.prepare('SELECT count(*) AS count FROM messages').get().count>=1000)return reply(429,{});
   db.prepare('INSERT INTO messages VALUES(?,?,?,?)').run(m.id,m.destination,m.text,Date.now());return reply(201,{id:m.id});
  }
  if(req.method==='GET'&&/^\/messages\/[0-9a-f-]{36}$/.test(req.url)){const row=db.prepare('SELECT * FROM messages WHERE id=?').get(req.url.split('/').at(-1));return row?reply(200,row):reply(404,{});}
  const rows=db.prepare('SELECT * FROM messages ORDER BY created DESC LIMIT 100').all();
  if(req.method==='GET'&&req.url==='/messages')return reply(200,{messages:rows});
  if(req.method==='GET'&&req.url==='/')return reply(200,`<!doctype html><html lang="en"><meta name="viewport" content="width=device-width"><title>LMT SMS test inbox</title><style>body{font:18px system-ui;max-width:48rem;margin:2rem auto;padding:1rem}article{border:1px solid #888;padding:1rem;margin:1rem 0;overflow-wrap:anywhere}a{display:inline-block;padding:12px}code{font-size:1.2em}</style><main><h1>SMS test inbox</h1><p>Local simulator. No carrier SMS is sent. Messages expire after six minutes. Use test details only.</p><a href="/">Refresh inbox</a>${rows.length?rows.map(m=>`<article><h2>${escape(m.destination)}</h2><p>${escape(m.text)}</p><small>Reference: ${escape(m.id)}</small></article>`).join(''):'<p>No messages yet. Request a mobile verification code in LMT.</p>'}</main></html>`,'text/html; charset=utf-8');
  reply(404,{});
 }catch{reply(500,{error:'Inbox unavailable'});}
}).listen(8080,'0.0.0.0');
