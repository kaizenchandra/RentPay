'use client';
import {useEffect,useRef,useState} from 'react';
import {useForm} from 'react-hook-form';
import {z} from 'zod';
import {api,ApiError,type Models} from '../../lib/api/browser';
import {ErrorSummary,Logout,SandboxNotice,useSession,useUnsaved} from './shared';
const schema=z.object({name:z.string().trim().min(1,'Enter a workspace name.').max(120),customerType:z.enum(['individual','organization']),city:z.string().trim().min(1,'Enter a city.').max(100),state:z.string().trim().min(1,'Enter a state or union territory.').max(100),acknowledged:z.boolean().refine(v=>v,'Acknowledge the sandbox terms before creating a workspace.')});
type Fields=z.infer<typeof schema>;
export default function CreateWorkspace(){
 const {session,error:sessionError}=useSession();const [plan,setPlan]=useState<Models['Plan']|null>(null);const [error,setFailure]=useState<Error|null>(null);const [locked,setLocked]=useState(false);const [leaving,setLeaving]=useState(false);
 const key=useRef<string|null>(null);const pending=useRef<Models['CreateWorkspace']|null>(null);
 const {register,handleSubmit,setError,formState:{errors,isDirty,isSubmitting}}=useForm<Fields>({defaultValues:{customerType:'individual',acknowledged:false}});
 const allowLeave=useUnsaved(isDirty&&!leaving);
 useEffect(()=>{const c=new AbortController();api<Models['Plan']>('/public/plans/FREE',{signal:c.signal}).then(setPlan).catch(e=>{if(e.name!=='AbortError')setFailure(e);});return()=>c.abort();},[]);
 const submit=handleSubmit(async data=>{setFailure(null);if(!plan)return;
  if(!pending.current){const parsed=schema.safeParse(data);if(!parsed.success){setFailure(new Error('Check the highlighted fields.'));parsed.error.issues.forEach(i=>setError(i.path[0] as keyof Fields,{message:i.message}));return;}
   const {acknowledged,...profile}=parsed.data;void acknowledged;pending.current={profile,selectedPlanVersionId:plan.versionId,consentIds:['sandbox-use-v1']};key.current=crypto.randomUUID();}
  setLocked(true);
  try{const result=await api<Models['CreatedWorkspace']>('/workspaces',{method:'POST',body:pending.current,key:key.current!});allowLeave();setLeaving(true);window.location.assign(result.setupUrl);}catch(e){setFailure(e as Error);if(e instanceof ApiError){e.problem.fieldErrors?.forEach(f=>{const field=f.field.replace(/^profile\./,'').replace('consentIds','acknowledged') as keyof Fields;if(['name','customerType','city','state','acknowledged'].includes(field))setError(field,{message:f.message});});}if(e instanceof ApiError&&e.status>0&&e.status<500){pending.current=null;key.current=null;setLocked(false);}}
 });
 return <main id="main" tabIndex={-1} className="form-page"><div className="toolbar"><h1>Create your workspace</h1><Logout/></div><ErrorSummary error={sessionError||error}/>{!session?<p role="status">Checking your session…</p>:<><SandboxNotice/>{plan?<aside className="plan-confirmation"><h2>{plan.name} · ₹{plan.amount}</h2><p>{plan.rentableLimit} rentable items · {plan.managerLimit} manager allowance</p><p>Version {plan.version} · No payment method required. The workspace is created immediately; property/unit operations remain unavailable.</p></aside>:<p role="status">Loading the Free plan…</p>}
 <form onSubmit={submit} noValidate><fieldset disabled={isSubmitting||locked}><legend>Workspace profile</legend>
 {(['name','city','state'] as const).map(field=><div key={field}><label htmlFor={field}>{field==='name'?'Workspace or organization name':field==='state'?'State / union territory':'City'}</label><input id={field} autoComplete={field==='city'?'address-level2':field==='state'?'address-level1':'organization'} maxLength={field==='name'?120:100} aria-invalid={!!errors[field]} aria-describedby={field+'-error'} {...register(field)}/><p className="field-error" id={field+'-error'}>{errors[field]?.message}</p></div>)}
 <label htmlFor="customerType">Customer type</label><select id="customerType" {...register('customerType')}><option value="individual">Individual landlord</option><option value="organization">Organization</option></select>
 <label className="checkbox" htmlFor="acknowledged"><input id="acknowledged" type="checkbox" aria-invalid={!!errors.acknowledged} aria-describedby="acknowledged-error" {...register('acknowledged')}/>I understand this is a local sandbox, will use test details only, and accept sandbox-use-v1. This is not a live subscription purchase.</label><p className="field-error" id="acknowledged-error">{errors.acknowledged?.message}</p></fieldset>
 {locked&&!isSubmitting?<p role="status">Your result is uncertain. Retry these same details to recover the original workspace safely.</p>:null}<button disabled={!plan||isSubmitting||leaving}>{isSubmitting?'Creating workspace…':locked?'Retry creation':'Confirm and create Free workspace'}</button></form><p><a href="/account">Back to your workspaces</a></p></>}</main>;
}
