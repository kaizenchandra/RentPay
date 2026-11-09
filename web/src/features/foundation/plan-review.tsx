'use client';
import {useEffect,useState} from 'react';
import OfferingAvailability from './offering-availability';
import PlanDiscard from './plan-discard';
import PlanPublication from './plan-publication';
import PlanDraft from './plan-draft';
import {api,ApiError,type Models} from '../../lib/api/browser';
import {ErrorSummary,Logout,useSession} from './shared';
const date=(value:string)=>new Intl.DateTimeFormat('en-IN',{dateStyle:'medium',timeStyle:'short',timeZone:'Asia/Kolkata'}).format(new Date(value));
export default function PlanReview({audience,cursor}:{audience:'workspace'|'consumer';cursor?:string}) {
 const {session,error:sessionError}=useSession();const actor=session?.account?.id;
 const [data,setData]=useState<Models['PlanReview']|null>(null);const [error,setError]=useState<Error|null>(null);const [reload,setReload]=useState(0);
 const [offering,setOffering]=useState<string|null>(null);const [availabilitySaved,setAvailabilitySaved]=useState(false);
 const [discard,setDiscard]=useState<Models['PlanReviewVersion']|null>(null);const [discarded,setDiscarded]=useState<string|null>(null);
 const [publication,setPublication]=useState<string|null>(null);const [published,setPublished]=useState<Models['PlanActivationResult']|null>(null);
 const [source,setSource]=useState<Models['PlanReviewVersion']|null>(null);const [saved,setSaved]=useState<string|null>(null);
 useEffect(()=>{if(!actor||(error instanceof ApiError&&[401,403].includes(error.status))){setSource(null);setPublication(null);setDiscard(null);setOffering(null);}},[actor,error]);
 const route=audience==='workspace'?'/admin/plans':'/admin/consumer-plans';
 useEffect(()=>{
  setData(null);setError(null);if(!actor)return;
  const c=new AbortController();let expiry:ReturnType<typeof setTimeout>|undefined;
  const query=new URLSearchParams({audience});if(cursor)query.set('cursor',cursor);
  api<Models['PlanReview']>('/admin/plans?'+query,{signal:c.signal}).then(result=>{
   if(c.signal.aborted)return;setData(result);
   expiry=setTimeout(()=>{setData(null);setReload(n=>n+1);},Math.max(0,Math.min(2147483647,new Date(result.accessExpiresAt).getTime()-Date.now())));
  }).catch(e=>{if(e.name!=='AbortError'){setData(null);setError(e);}});
  return()=>{c.abort();if(expiry)clearTimeout(expiry);};
 },[actor,audience,cursor,reload]);
 useEffect(()=>{const refresh=()=>{setData(null);setReload(n=>n+1);};window.addEventListener('focus',refresh);window.addEventListener('pageshow',refresh);return()=>{window.removeEventListener('focus',refresh);window.removeEventListener('pageshow',refresh);};},[]);
 return <main id="main" tabIndex={-1}>
  <div className="toolbar"><div><p className="eyebrow">Platform commercial review</p><h1>Plan versions</h1></div><Logout/></div>
  <nav aria-label="Review audience" className="interval"><a href="/admin/plans" aria-current={audience==='workspace'?'page':undefined}>Workspace plans</a><a href="/admin/consumer-plans" aria-current={audience==='consumer'?'page':undefined}>Personal plans</a></nav>
  <aside className="notice"><strong>Version review and draft preparation</strong><p>This view includes unpublished, scheduled and historical terms. Published does not mean currently effective. Authorized draft creation inherits existing prices and allowances. Separately authorized publication applies only to reviewed editorial versions; subscriber migration and financial configuration remain unavailable. This access grants no workspace, tenant or police-record authority.</p></aside>
  <ErrorSummary error={sessionError||error}/><button className="secondary" onClick={()=>{setData(null);setReload(n=>n+1);}}>Refresh versions</button>
  {saved?<p role="status">Draft save recorded: {saved}. The list below shows current version status. Draft saves do not change public offers or subscriptions.</p>:null}
  {published?<p role="status">Publication recorded for {published.id}. Public catalogue selection uses its effective instant: {date(published.effectiveAt)} IST. Existing subscriptions are unchanged.</p>:null}
  {discarded?<p role="status">Draft discard recorded: {discarded}. Terms and audit evidence are retained.</p>:null}
  {availabilitySaved?<p role="status">Offering availability change recorded. Open its availability review to see current state.</p>:null}
  {actor&&offering?<OfferingAvailability key={offering} id={offering} onClose={()=>setOffering(null)} onSaved={()=>{setAvailabilitySaved(true);setOffering(null);setReload(n=>n+1);}}/>:null}
  {actor&&discard?<PlanDiscard key={discard.id} draft={discard} onClose={()=>setDiscard(null)} onSaved={result=>{setDiscarded(result.id);setDiscard(null);setReload(n=>n+1);}}/>:null}
  {actor&&publication?<PlanPublication key={publication} id={publication} onClose={()=>setPublication(null)} onSaved={result=>{setPublished(result);setPublication(null);setReload(n=>n+1);}}/>:null}
  {actor&&source?<PlanDraft key={source.id} source={source} editing={source.state==='draft'} onClose={()=>setSource(null)} onSaved={result=>{setSaved(result.id);setSource(null);setReload(n=>n+1);}}/>:null}
  {actor&&data?<><h2>{audience==='workspace'?'Workspace':'Personal'} versions</h2><p>{data.totalMatching} versions · Checked {date(data.asOf)} IST · Access expires {date(data.accessExpiresAt)} IST</p>
   {data.items.length?data.items.map(v=><article className="session-card" key={v.id}><h3>{v.name} · Version {v.version}</h3><p>{v.code} · {v.state} · Effective {date(v.effectiveAt)} IST</p><p>{v.baselineOnly?'Baseline definition; no shipped capability is implied.':'Commercial definition; publication and effective date require separate review.'}</p><dl><div><dt>Monthly amount</dt><dd>{v.monthlyAmount===null?'Not configured':`₹${v.monthlyAmount}`}</dd></div><div><dt>Annual amount</dt><dd>{v.annualAmount===null?'Not configured':`₹${v.annualAmount}`}</dd></div>{audience==='workspace'?<><div><dt>Rentable-item allowance</dt><dd>{v.rentableLimit??'Contract-defined'}</dd></div><div><dt>Manager allowance</dt><dd>{v.managerLimit??'Contract-defined'}</dd></div></>:null}</dl><p>{v.disclosure}</p><details><summary>Version reference</summary><p style={{overflowWrap:'anywhere'}}>{v.id}</p><p>Currency: {v.currency}. Historical contracts retain their own version references.</p></details>{data.canCreateDraft&&v.state==='published'?<button className="secondary" disabled={!!source||!!publication||!!discard||!!offering} onClick={()=>{setSaved(null);setSource(v);}}>Create draft from version {v.version} · {v.name}</button>:null}{data.canEditDraft&&v.state==='draft'?<button className="secondary" disabled={!!source||!!publication||!!discard||!!offering} onClick={()=>{setSaved(null);setSource(v);}}>Edit draft · {v.name}</button>:null}{data.canPublish&&v.state==='draft'?<button className="secondary" disabled={!!source||!!publication||!!discard||!!offering} onClick={()=>{setPublished(null);setPublication(v.id);}}>Review publication · {v.name}</button>:null}{data.canDiscardDraft&&v.state==='draft'?<button className="secondary" disabled={!!source||!!publication||!!discard||!!offering} onClick={()=>{setDiscarded(null);setDiscard(v);}}>Discard draft · {v.name}</button>:null}{v.state==='published'?<button className="secondary" disabled={!!source||!!publication||!!discard||!!offering} onClick={()=>{setAvailabilitySaved(false);setOffering(v.id);}}>Offering availability · {v.name} · {v.version}</button>:null}</article>):<p>No versions on this page.</p>}
   <nav aria-label="Version pages"><a href={route}>First page</a>{data.page.nextCursor?<p><a href={route+'?cursor='+encodeURIComponent(data.page.nextCursor)}>Next page</a></p>:null}</nav>
  </>:!error&&!sessionError?<p role="status">Loading authorized versions…</p>:null}
 </main>;
}
