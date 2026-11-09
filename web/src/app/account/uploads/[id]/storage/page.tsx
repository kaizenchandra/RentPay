import StorageReview from '../../../../../features/foundation/storage-review';
export const metadata={title:'Private file recovery · LMT',robots:{index:false,follow:false}};
export default async function Page({params}:{params:Promise<{id:string}>}){const {id}=await params;return <StorageReview id={id}/>;}
