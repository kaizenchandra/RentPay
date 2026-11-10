import Recovery from '../../../../../features/foundation/subscription-payment-recovery';
export const metadata={title:'Subscription payment recovery | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Recovery key={workspace} workspace={workspace}/>:<main><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
