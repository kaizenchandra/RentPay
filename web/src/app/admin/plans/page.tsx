import PlanReview from '../../../features/foundation/plan-review';
export const metadata={title:'LMT · Commercial plan review',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{cursor?:string}>}) {const q=await searchParams;return <PlanReview audience="workspace" cursor={q.cursor}/>;}
