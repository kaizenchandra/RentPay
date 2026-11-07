'use client';
import {useEffect,useRef,useState} from 'react';
import {useForm} from 'react-hook-form';
import {z} from 'zod';
import {api,ApiError,type Models} from '../../lib/api/browser';
import {ErrorSummary,Logout,useSession,useUnsaved} from './shared';
const nameSchema=z.string().trim().min(1,'Enter your display name.').max(100,'Use no more than 100 characters.').regex(/^[^\x00-\x1f\x7f]*$/,'Do not include control characters.');
export default function Profile(){
 const {session,error:sessionError}=useSession();const actor=session?.account?.id;
 const [profile,setProfile]=useState<Models['Profile']|null>(null);const [error,setError]=useState<Error|null>(null);const [notice,setNotice]=useState('');const [reload,setReload]=useState(0);const [locked,setLocked]=useState(false);
 const pending=useRef<{key:string;etag:string;body:Models['ChangeProfile']}|null>(null);
 const {register,handleSubmit,reset,setError:setFieldError,formState:{isDirty,isSubmitting,errors}}=useForm<{displayName:string}>();
 useUnsaved(isDirty);
 useEffect(()=>{setProfile(null);pending.current=null;setLocked(false);if(!actor)return;const c=new AbortController();api<Models['Profile']>('/me/profile',{signal:c.signal}).then(p=>{setProfile(p);reset({displayName:p.displayName});setError(null);}).catch(e=>{if(e.name!=='AbortError')setError(e);});return()=>c.abort();},[actor,reload,reset]);
 const submit=handleSubmit(async values=>{
  if(!profile)return;setError(null);setNotice('');
  if(!pending.current){const parsed=nameSchema.safeParse(values.displayName);if(!parsed.success){setFieldError('displayName',{message:parsed.error.issues[0].message});setError(new Error('Check the highlighted name.'));return;}pending.current={key:crypto.randomUUID(),etag:profile.etag,body:{displayName:parsed.data}};}
  setLocked(true);
  try{const command=pending.current;const updated=await api<Models['Profile']>('/me/profile',{method:'PATCH',...command});setProfile(updated);reset({displayName:updated.displayName});pending.current=null;setLocked(false);setNotice('Your profile was saved.');}
  catch(e){setError(e as Error);if(e instanceof ApiError&&e.status>0&&e.status<500){pending.current=null;setLocked(false);e.problem.fieldErrors?.forEach(f=>{if(f.field==='displayName')setFieldError('displayName',{message:f.message});});}}
 });
 return <main id="main" tabIndex={-1} className="form-page"><div className="toolbar"><h1>Your profile</h1><Logout/></div><p><a href="/account">Your workspaces</a> · <a href="/account/security">Sessions and security</a></p><ErrorSummary error={sessionError||error}/>{notice?<p role="status" className="notice">{notice}</p>:null}
 {session&&profile?<><p>Mobile: {profile.maskedMobile} · {profile.mode} identity</p><p className="help">Changing your display name does not change your mobile, account identity or workspace permissions. Contact changes require a separate verified flow, which is not available yet.</p>
 <form onSubmit={submit} noValidate><label htmlFor="displayName">Display name</label><input id="displayName" autoComplete="name" maxLength={100} disabled={locked||isSubmitting} aria-invalid={!!errors.displayName} aria-describedby="displayName-error" {...register('displayName')}/><p id="displayName-error" className="field-error">{errors.displayName?.message}</p><button disabled={isSubmitting||(!isDirty&&!locked)}>{isSubmitting?'Saving…':locked?'Retry same change':'Save profile'}</button></form>
 {locked?<p>Your result is uncertain. Retry the same change to recover its result.</p>:null}{error instanceof ApiError&&error.status===412?<button className="secondary" onClick={()=>{setNotice('Latest saved profile loaded; your unsaved edit will be replaced.');setReload(n=>n+1);}}>Reload latest profile and discard my edit</button>:null}</>:!error&&!sessionError?<p role="status">Loading your profile…</p>:<button className="secondary" onClick={()=>setReload(n=>n+1)}>Reload profile</button>}</main>;
}
