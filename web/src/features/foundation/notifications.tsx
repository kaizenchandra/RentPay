'use client';
import {useRouter,useSearchParams} from 'next/navigation';
import PendingNotifications from './pending-notifications';
import NotificationInbox from './notification-inbox';
import {ErrorSummary,Logout,useSession} from './shared';
export default function Notifications(){
 const {session,error}=useSession();const params=useSearchParams(),router=useRouter();const selected=params.get('status')||'all';
 const valid=selected==='all'||selected==='read'||selected==='unread';
 return <main id="main" tabIndex={-1}><div className="toolbar"><h1>Your account notifications</h1><Logout/></div><nav aria-label="Account settings"><a href="/account">Your workspaces</a> · <a href="/account/security">Sessions and security</a> · <a href="/account/notification-preferences">Notification preferences</a></nav><p>These notices belong to your signed-in account across contexts. Manager access changes and responses to invitations you sent appear as essential notices. Optional workspace setup notices appear when enabled; other workspace operational messages and tenancy notices are not included yet. Security notices remain available without a paid plan.</p><ErrorSummary error={error}/>{session?.account?<PendingNotifications key={session.account.id}/>:null}{!valid?<p role="alert">Unsupported read-status filter. <a href="?status=all">Show all notifications</a>.</p>:session?.account?<NotificationInbox key={session.account.id+':'+selected} actor={session.account.id} filter={selected} onFilterChange={status=>{const next=new URLSearchParams(params.toString());next.set('status',status);next.delete('cursor');router.replace('?'+next.toString(),{scroll:false});}}/>:!error?<p role="status">Checking your account…</p>:null}</main>;
}
