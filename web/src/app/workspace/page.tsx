import WorkspaceContext from '../../features/foundation/workspace-context';
export const metadata={title:'Workspace | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}){const {workspace}=await searchParams;return workspace?<WorkspaceContext key={workspace} id={workspace}/>:<main id="main"><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
