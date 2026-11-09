'use client';
import {useEffect,useState} from 'react';
import {useForm} from 'react-hook-form';
import {api,type Models} from '../../lib/api/browser';
import {ErrorSummary,SandboxNotice} from './shared';
export default function Verify({id}:{id:string}){
 const [challenge,setChallenge]=useState<Models['Challenge']|null>(null);const [error,setError]=useState<Error|null>(null);
 const {register,handleSubmit,formState:{errors,isSubmitting}}=useForm<{code:string}>();
 useEffect(()=>{const controller=new AbortController();let timer:ReturnType<typeof setTimeout>;let attempts=0;
  async function load(){try{const c=await api<Models['Challenge']>('/auth/challenges/'+encodeURIComponent(id),{signal:controller.signal});setChallenge(c);if(c.status==='pending'&&attempts++<20)timer=setTimeout(load,2000);}catch(e){if((e as Error).name!=='AbortError')setError(e as Error);}}
  void load();return()=>{controller.abort();clearTimeout(timer);};
 },[id]);
 const submit=handleSubmit(async data=>{setError(null);try{await api<Models['Session']>('/auth/challenges/'+encodeURIComponent(id)+'/verify',{method:'POST',body:data});window.location.replace('/account');}catch(e){setError(e as Error);}});
 return <main id="main" tabIndex={-1} className="form-page"><noscript><p>JavaScript is required for secure contact verification. Enable it and reload before submitting.</p></noscript><p className="eyebrow">Contact verification</p><h1>Enter your code</h1><SandboxNotice/><ErrorSummary error={error}/>{challenge?<><p>For {challenge.maskedDestination} · Status: <strong>{challenge.status}</strong></p><p className="help">Challenge reference: <code>{challenge.id}</code>. The developer running this instance can retrieve its code from the private local inbox.</p></>:<p role="status">Loading challenge…</p>}
 <form method="post" onSubmit={submit} noValidate><label htmlFor="code">Six-digit verification code</label><input id="code" inputMode="numeric" autoComplete="one-time-code" maxLength={6} aria-invalid={!!errors.code} aria-describedby="code-error" {...register('code',{required:'Enter your code.',pattern:{value:/^[0-9]{6}$/,message:'Enter all six digits.'}})}/><p id="code-error" className="field-error">{errors.code?.message}</p><button disabled={isSubmitting||!challenge||['expired','locked','consumed','failed'].includes(challenge.status)}>{isSubmitting?'Verifying…':'Verify and continue'}</button></form><p><a href="/login">Request a new code</a></p></main>;
}
