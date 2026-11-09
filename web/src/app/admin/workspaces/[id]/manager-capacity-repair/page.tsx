import CapacityRepair from '../../../../../features/foundation/capacity-repair';
export const metadata={title:'Manager capacity repair · LMT',robots:{index:false,follow:false}};
export default async function Page({params}:{params:Promise<{id:string}>}){const {id}=await params;return <CapacityRepair workspace={id}/>;}
