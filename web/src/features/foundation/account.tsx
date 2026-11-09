'use client';
import {useEffect,useState} from 'react';
import {api,type Models} from '../../lib/api/browser';
import {ErrorSummary,Logout,useSession} from './shared';
export default function Account(){
 const {session,error:sessionError}=useSession();const [contexts,setContexts]=useState<Models['Contexts']|null>(null);const [error,setError]=useState<Error|null>(null);
 useEffect(()=>{setContexts(null);if(!session)return;const c=new AbortController();api<Models['Contexts']>('/me/contexts',{signal:c.signal}).then(setContexts).catch(e=>{if(e.name!=='AbortError')setError(e);});return()=>c.abort();},[session]);
 return <main id="main" tabIndex={-1}><div className="toolbar"><div><p className="eyebrow">Account context</p><h1>Your workspaces</h1></div><Logout/></div><ErrorSummary error={sessionError||error}/>{session?<p>Signed in as <strong>{session.account?.displayName}</strong> · {session.mode} identity</p>:<p role="status">Checking your session…</p>}
 {session&&contexts?<><nav aria-label="Account settings"><a href="/account/profile">Edit profile</a> · <a href="/account/security">Sessions and security</a> · <a href="/account/invitations">Your invitations</a> · <a href="/account/uploads">Private upload staging</a></nav><p>Choose the workspace for this tab. Personal rental-tenant membership is separate.</p>{contexts.items.length?<div className="workspace-list">{contexts.items.map(w=><a className="workspace-link" key={w.id} href={(w.role==='OWNER'?'/setup?workspace=':'/workspace?workspace=')+w.id}><strong>{w.name}</strong><span>{w.role} · {w.mode}</span></a>)}</div>:<aside className="notice"><h2>No workspaces yet</h2><p>Create your first sandbox workspace with the Free baseline.</p></aside>}
 {contexts.page.nextCursor?<button className="secondary" onClick={async()=>{try{const next=await api<Models['Contexts']>('/me/contexts?cursor='+encodeURIComponent(contexts.page.nextCursor!));setContexts({...next,items:[...contexts.items,...next.items]});}catch(e){setError(e as Error);}}}>Load more workspaces</button>:null}<a className="button" href="/create-workspace">Create a Free workspace</a></>:null}</main>;
}
