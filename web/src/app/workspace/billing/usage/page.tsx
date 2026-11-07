import Usage from '../../../../features/foundation/usage';
export const metadata={title:'Workspace usage | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string;from?:string;to?:string}>}){const params=await searchParams;return params.workspace?<Usage key={[params.workspace,params.from,params.to].join(':')} workspace={params.workspace} from={params.from} to={params.to}/>:<main id="main"><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
