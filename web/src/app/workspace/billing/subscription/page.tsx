import Subscription from '../../../../features/foundation/subscription';
export const metadata={title:'Workspace subscription | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Subscription key={workspace} workspace={workspace}/>:<main id="main"><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
