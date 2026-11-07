import Verify from '../../features/foundation/verify';
export const metadata = { title: 'Verify mobile · LMT', robots: {index:false,follow:false} };
export default async function Page({searchParams}:{searchParams:Promise<{challenge?:string}>}) {const q=await searchParams;return <Verify id={q.challenge||''}/>;}
