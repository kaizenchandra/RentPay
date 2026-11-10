import {Suspense} from 'react';
import Notifications from '../../../features/foundation/notifications';
export const metadata={title:'Your account notifications · LMT',robots:{index:false,follow:false}};
export default function Page(){return <Suspense fallback={<main><p role="status">Loading notifications…</p></main>}><Notifications/></Suspense>;}
