import Catalog from '../../../features/inventory/catalog';
export const metadata={title:'Amenity catalog | LMT',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string;search?:string;status?:string;category?:string}>}){const p=await searchParams;return p.workspace?<Catalog key={p.workspace} workspace={p.workspace} search={p.search||''} status={p.status||'active'} category={p.category||''}/>:<main id="main"><h1>Choose a workspace</h1><a href="/account">Your workspaces</a></main>;}
