import Upgrade from '../../../../../features/foundation/subscription-upgrade';
export const metadata={title:'Upgrade subscription | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Upgrade key={workspace} workspace={workspace}/>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
