'use client';
import {useRef,useState} from 'react';
import {useForm} from 'react-hook-form';
import {z} from 'zod';
import {api,type Models} from '../../lib/api/browser';
import {ErrorSummary,SandboxNotice,useUnsaved,useIdentityConfiguration} from './shared';
const schema=z.object({channel:z.enum(['mobile','email']),displayName:z.string().trim().min(1,'Enter your full name.').max(100),destination:z.string().min(1).max(254)}).superRefine((v,ctx)=>{if(v.channel==='mobile'&&!/^\+91[6-9][0-9]{9}$/.test(v.destination))ctx.addIssue({code:'custom',path:['destination'],message:'Use +91 followed by a ten-digit Indian mobile number.'});if(v.channel==='email'&&!z.email().safeParse(v.destination).success)ctx.addIssue({code:'custom',path:['destination'],message:'Enter your email address.'});});
type Fields=z.infer<typeof schema>;
export default function Login(){
 const {configuration,error:configurationError}=useIdentityConfiguration();
 const {register,watch,handleSubmit,setError,formState:{errors,isSubmitting,isDirty}}=useForm<Fields>({defaultValues:{channel:'mobile'}});const channel=watch('channel');
 const [error,setFailure]=useState<Error|null>(null);const [leaving,setLeaving]=useState(false);const key=useRef<string|null>(null);const last=useRef('');
 const allowLeave=useUnsaved(isDirty&&!leaving);
 const submit=handleSubmit(async data=>{setFailure(null);const parsed=schema.safeParse(data);if(!parsed.success){setFailure(new Error('Check the highlighted fields.'));parsed.error.issues.forEach(i=>setError(i.path[0] as keyof Fields,{message:i.message}));return;}
  const payload={...parsed.data,purpose:'sign_in' as const};const serialized=JSON.stringify(payload);
  if(last.current!==serialized){key.current=crypto.randomUUID();last.current=serialized;}
  try{const result=await api<Models['Challenge']>('/auth/challenges',{method:'POST',body:payload,key:key.current!});allowLeave();setLeaving(true);window.location.assign('/verify-otp?challenge='+result.id);}catch(e){setFailure(e as Error);}
 });
 return <main id="main" tabIndex={-1} className="form-page"><noscript><p>JavaScript is required for secure contact verification. Enable it and reload before submitting.</p></noscript><p className="eyebrow">Your account</p><h1>Sign in or register</h1><p>Verify your selected contact to access your workspaces. Returning users keep the same account for that contact. Email and mobile accounts are not automatically linked.</p><SandboxNotice/>{configuration?.mode==='live'?<p>Receive a sign-in code by SMS or email. Delivery may take a moment; never share your code.</p>:configuration?.mode==='unavailable'?<p role="status">SMS and email verification are not configured.</p>:null}<ErrorSummary error={configurationError||error}/><form method="post" onSubmit={submit} noValidate>
 <label htmlFor="displayName">Full name</label><input id="displayName" autoComplete="name" maxLength={100} aria-invalid={!!errors.displayName} aria-describedby="name-error" {...register('displayName')}/><p id="name-error" className="field-error">{errors.displayName?.message}</p>
 <label htmlFor="channel">Verification channel</label><select id="channel" {...register('channel')}><option value="mobile">Mobile</option><option value="email">Email</option></select><label htmlFor="destination">{channel==='email'?'Email address':'Mobile number'}</label><input id="destination" type={channel==='email'?'email':'tel'} autoComplete={channel==='email'?'email':'tel'} placeholder={channel==='email'?'you@example.test':'+919876543210'} aria-invalid={!!errors.destination} aria-describedby="mobile-help mobile-error" {...register('destination')}/><p id="mobile-help" className="help">Use your selected contact. Verification codes expire after five minutes. Email local-part spelling is case-sensitive; domain case is ignored.</p><p id="mobile-error" className="field-error">{errors.destination?.message}</p>
 <button disabled={isSubmitting||leaving||!configuration||configuration.mode==='unavailable'}>{isSubmitting?'Requesting code…':'Request verification code'}</button></form>{configuration?.mode==='sandbox'?<p><a href="/account/recovery">Recover an existing account and end its sessions</a></p>:null}<p className="help">Use the same verified contact to return to your account. Email and mobile identities are separate unless explicitly linked. Live account linking and assisted recovery are not available in this sign-in flow.</p></main>;
}
