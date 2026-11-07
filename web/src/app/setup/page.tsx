import Setup from '../../features/foundation/setup';
export const metadata = { title: 'Workspace setup · LMT', robots: {index:false,follow:false} };
export default async function Page({searchParams}:{searchParams:Promise<{workspace?:string}>}) {const q=await searchParams;return <Setup key={q.workspace} id={q.workspace||''}/>;}
