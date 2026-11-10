import Inspector from '../../../../features/foundation/entitlement-inspector';
export const metadata={title:'Feature eligibility | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Inspector key={workspace} workspace={workspace}/>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
