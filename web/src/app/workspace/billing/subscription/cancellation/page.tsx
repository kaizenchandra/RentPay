import Downgrade from '../../../../../features/foundation/subscription-downgrade';
export const metadata={title:'Cancel paid subscription | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Downgrade key={workspace} workspace={workspace} cancellation/>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
