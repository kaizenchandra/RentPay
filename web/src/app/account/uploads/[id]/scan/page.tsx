import ScanStatus from '../../../../../features/foundation/scan-status';
export const metadata={title:'Scan preparation · LMT',robots:{index:false,follow:false}};
export default async function Page({params}:{params:Promise<{id:string}>}){const {id}=await params;return <ScanStatus id={id}/>;}
