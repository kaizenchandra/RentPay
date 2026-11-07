import Memberships from '../../../../features/foundation/memberships';
export const metadata={title:'Members and invitations | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<Memberships key={workspace} workspace={workspace}/>:<main id="main"><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
