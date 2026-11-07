'use client';
import {useRef,useState} from 'react';
import {useForm} from 'react-hook-form';
import {z} from 'zod';
import {api,type Models} from '../../lib/api/browser';
import {ErrorSummary,SandboxNotice,useUnsaved} from './shared';
const schema=z.object({displayName:z.string().trim().min(1,'Enter your full name.').max(100),destination:z.string().regex(/^\+91[6-9][0-9]{9}$/,'Use +91 followed by a ten-digit Indian mobile number.')});
type Fields=z.infer<typeof schema>;
export default function Login(){
 const {register,handleSubmit,setError,formState:{errors,isSubmitting,isDirty}}=useForm<Fields>();
 const [error,setFailure]=useState<Error|null>(null);const [leaving,setLeaving]=useState(false);const key=useRef<string|null>(null);const last=useRef('');
 const allowLeave=useUnsaved(isDirty&&!leaving);
 const submit=handleSubmit(async data=>{setFailure(null);const parsed=schema.safeParse(data);if(!parsed.success){setFailure(new Error('Check the highlighted fields.'));parsed.error.issues.forEach(i=>setError(i.path[0] as keyof Fields,{message:i.message}));return;}
  const payload={...parsed.data,channel:'mobile' as const,purpose:'sign_in' as const};const serialized=JSON.stringify(payload);
  if(last.current!==serialized){key.current=crypto.randomUUID();last.current=serialized;}
  try{const result=await api<Models['Challenge']>('/auth/challenges',{method:'POST',body:payload,key:key.current!});allowLeave();setLeaving(true);window.location.assign('/verify-otp?challenge='+result.id);}catch(e){setFailure(e as Error);}
 });
 return <main id="main" tabIndex={-1} className="form-page"><p className="eyebrow">Your account</p><h1>Sign in or register</h1><p>Verify your mobile to access your own workspaces. Returning users keep the same account.</p><SandboxNotice/><ErrorSummary error={error}/><form onSubmit={submit} noValidate>
 <label htmlFor="displayName">Full name</label><input id="displayName" autoComplete="name" maxLength={100} aria-invalid={!!errors.displayName} aria-describedby="name-error" {...register('displayName')}/><p id="name-error" className="field-error">{errors.displayName?.message}</p>
 <label htmlFor="destination">Mobile number</label><input id="destination" type="tel" autoComplete="tel" placeholder="+919876543210" aria-invalid={!!errors.destination} aria-describedby="mobile-help mobile-error" {...register('destination')}/><p id="mobile-help" className="help">Include +91. A local test code expires after five minutes.</p><p id="mobile-error" className="field-error">{errors.destination?.message}</p>
 <button disabled={isSubmitting||leaving}>{isSubmitting?'Requesting code…':'Request verification code'}</button></form><p className="help">Live mobile verification and contact-change recovery are not configured.</p></main>;
}
