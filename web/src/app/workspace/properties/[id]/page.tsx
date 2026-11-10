import {Suspense} from 'react';
import Properties from '../../../../features/property/properties';
export const metadata={title:'Property setup · LMT',robots:{index:false,follow:false}};
export default async function Page({params,searchParams}:{params:Promise<{id:string}>;searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;const {id}=await params;return workspace?<Suspense fallback={<main><p role="status">Loading property…</p></main>}><Properties key={workspace+id} workspace={workspace} id={id}/></Suspense>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
