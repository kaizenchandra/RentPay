import Override from '../../../../../features/foundation/entitlement-override';
export const metadata={title:'Workspace capability override · LMT',robots:{index:false,follow:false}};
export default async function Page({params}:{params:Promise<{id:string}>}){const {id}=await params;return <Override workspace={id}/>;}
