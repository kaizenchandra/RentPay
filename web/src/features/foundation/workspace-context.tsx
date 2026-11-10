'use client';
import {useEffect,useState} from 'react';
import {api,type Models} from '../../lib/api/browser';
import {ErrorSummary,Logout,useSession} from './shared';
export default function WorkspaceContext({id}:{id:string}){
 const {session,error:sessionError}=useSession();const [record,setRecord]=useState<Models['MembershipContext']|null>(null);const [error,setError]=useState<Error|null>(null);const [reload,setReload]=useState(0);
 useEffect(()=>{setRecord(null);setError(null);if(!session)return;const c=new AbortController();api<Models['MembershipContext']>('/workspaces/'+encodeURIComponent(id)+'/membership',{signal:c.signal}).then(setRecord).catch(e=>{if(e.name!=='AbortError')setError(e);});return()=>c.abort();},[id,session,reload]);
 return <main id="main" tabIndex={-1}><div className="toolbar"><h1>{record?.name||'Workspace membership'}</h1><Logout/></div><ErrorSummary error={sessionError||error}/>{record?<><p>{record.role} · {record.mode}</p><p>Your membership is active in this workspace. Owners can configure property drafts and hierarchy. Property delegation, tenancy, financial approvals and signing authority remain separately gated.</p>{record.role==='OWNER'?<p><a href={'/workspace/properties?workspace='+id}>Properties</a> · <a href={'/workspace/billing/subscription?workspace='+id}>Subscription dashboard</a> · <a href={'/workspace/settings/members?workspace='+id}>Members and invitations</a> · <a href={'/setup?workspace='+id}>Workspace setup</a></p>:null}</>:!error?<p role="status">Checking workspace access…</p>:null}<button className="secondary" onClick={()=>setReload(n=>n+1)}>Refresh access</button><p><a href="/account">Switch workspace</a> · <a href="/account/invitations">Your invitations</a></p></main>;
}
