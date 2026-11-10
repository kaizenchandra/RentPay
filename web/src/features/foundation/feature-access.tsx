'use client';
import {useEffect,useState} from 'react';
import {api,type Models} from '../../lib/api/browser';
export function useFeatureAccess(workspace:string,actor?:string){
 const [snapshot,setSnapshot]=useState<Models['EffectiveEntitlementSnapshot']|null>(null);
 const [error,setError]=useState<Error|null>(null);
 useEffect(()=>{
  setSnapshot(null);setError(null);if(!actor)return;
  let stopped=false,timer:ReturnType<typeof setTimeout>,request:AbortController|undefined;
  const refresh=()=>{if(stopped)return;request?.abort();clearTimeout(timer);const c=new AbortController();request=c;
   api<Models['EffectiveEntitlementSnapshot']>('/workspaces/'+encodeURIComponent(workspace)+'/entitlements/snapshot',{signal:c.signal}).then(value=>{if(!stopped&&!c.signal.aborted){setSnapshot(value);setError(null);}}).catch(e=>{if(!stopped&&!c.signal.aborted){setSnapshot(null);setError(e);}}).finally(()=>{if(!stopped&&!c.signal.aborted)timer=setTimeout(refresh,15000);});
  };
  const changed=(e:Event)=>{if((e as CustomEvent).detail===workspace)refresh();};
  const channel=new BroadcastChannel('lmt-entitlements');channel.onmessage=e=>{if(e.data===workspace)refresh();};
  const clear=()=>{stopped=true;request?.abort();clearTimeout(timer);setSnapshot(null);};
  const sessionChannel=new BroadcastChannel('lmt-session');sessionChannel.onmessage=e=>{if(e.data==='logout')clear();};
  document.addEventListener('visibilitychange',refresh);window.addEventListener('focus',refresh);window.addEventListener('lmt-entitlements',changed);window.addEventListener('lmt-clear-drafts',clear);refresh();
  return()=>{clear();channel.close();sessionChannel.close();document.removeEventListener('visibilitychange',refresh);window.removeEventListener('focus',refresh);window.removeEventListener('lmt-entitlements',changed);window.removeEventListener('lmt-clear-drafts',clear);};
 },[workspace,actor]);
 return {snapshot,error};
}
export function FeatureLock({feature,workspace}:{feature?:Models['FeatureAccess'];workspace:string}){
 if(!feature||feature.eligible)return null;
 return <aside className="notice" aria-label={feature.label+' limitation'} role="status"><strong>{feature.label}</strong><p>{feature.limitation}</p>{feature.requiredPlans.length>0?<p>Required plan: {feature.requiredPlans.join(' or ')}.</p>:null}{feature.upgradeAvailable?<a href={'/workspace/billing/subscription/upgrade?workspace='+encodeURIComponent(workspace)}>Review upgrade options</a>:feature.requiredPlans.length>0?<p>Ask your workspace billing administrator to review an upgrade.</p>:null}<p>Existing drafts are retained. Essential corrections use their separate access rules.</p><small>Reason: {feature.reason}</small></aside>;
}
