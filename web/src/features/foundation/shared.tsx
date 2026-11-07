'use client';
import { useEffect, useRef, useState } from 'react';
import { api, ApiError, announceLogout, type Models } from '../../lib/api/browser';
export function useUnsaved(dirty: boolean) {
 const enabled=useRef(dirty);enabled.current=dirty;
 useEffect(()=>{const warn=(e:BeforeUnloadEvent)=>{if(enabled.current){e.preventDefault();e.returnValue='';}};const clear=()=>{enabled.current=false;};window.addEventListener('beforeunload',warn);window.addEventListener('lmt-clear-drafts',clear);return()=>{window.removeEventListener('beforeunload',warn);window.removeEventListener('lmt-clear-drafts',clear);};},[]);
 return ()=>{enabled.current=false;};
}
export function ErrorSummary({error}:{error:Error|null}) {
 const ref=useRef<HTMLDivElement>(null);
 useEffect(()=>{if(error)ref.current?.focus();},[error]);
 if(!error)return null;
 return <div className="form-error" role="alert" tabIndex={-1} ref={ref}><strong>We couldn’t complete that step.</strong><p>{error.message}</p>{error instanceof ApiError && (error.status===401||error.problem.code==='REAUTHENTICATION_REQUIRED')?<a href="/login">Sign in again</a>:null}{error instanceof ApiError && error.problem.fieldErrors?.map(f=><p key={f.field}><a href={`#${f.field.replace(/^profile\./,'').replace('consentIds','acknowledged')}`}>{f.message}</a></p>)}</div>;
}
export function SandboxNotice() {return <aside className="notice"><strong>Local sandbox</strong><p>No SMS is sent. Codes go to the private test inbox on this computer. Use test details only; no payment is collected.</p></aside>;}
export function Logout() {
 const [error,setError]=useState<Error|null>(null);const [busy,setBusy]=useState(false);
 return <><button className="secondary" disabled={busy} onClick={async()=>{setBusy(true);try{await api('/auth/logout',{method:'POST'});announceLogout();window.location.replace('/login');}catch(e){if(e instanceof ApiError&&e.status===401){announceLogout();window.location.replace('/login');}else{setError(e as Error);setBusy(false);}}}}>Sign out</button><ErrorSummary error={error}/></>;
}
export function useSession() {
 const [session,setSession]=useState<Models['Session']|null>(null);const [error,setError]=useState<Error|null>(null);
 useEffect(()=>{
  let controller:AbortController|undefined;
  const load=()=>{controller?.abort();controller=new AbortController();api<Models['Session']>('/auth/session',{signal:controller.signal}).then(s=>{if(!s.authenticated){window.dispatchEvent(new Event('lmt-clear-drafts'));setSession(null);window.location.replace('/login');return;}setSession(previous=>previous?.account?.id===s.account?.id&&previous?.account?.displayName===s.account?.displayName?previous:s);setError(null);}).catch(e=>{if(e.name!=='AbortError'){setError(e);}});};
  const clear=()=>{window.dispatchEvent(new Event('lmt-clear-drafts'));controller?.abort();setSession(null);window.location.replace('/login');};
  load();const channel=new BroadcastChannel('lmt-session');channel.onmessage=clear;
  const timer=setInterval(load,60000);
  window.addEventListener('focus',load);window.addEventListener('pageshow',load);
  return()=>{clearInterval(timer);controller?.abort();channel.close();window.removeEventListener('focus',load);window.removeEventListener('pageshow',load);};
 },[]);
 return {session,error};
}
