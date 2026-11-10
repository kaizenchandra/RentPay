import {Suspense} from 'react';
import Properties from '../../../features/property/properties';
export const metadata={title:'Properties · LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Suspense fallback={<main><p role="status">Loading property context…</p></main>}><Properties key={workspace} workspace={workspace}/></Suspense>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
