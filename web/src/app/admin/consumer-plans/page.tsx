import PlanReview from '../../../features/foundation/plan-review';
export const metadata={title:'LMT · Personal plan review',robots:{index:false,follow:false}};
export default async function Page({searchParams}:{searchParams:Promise<{cursor?:string}>}) {const q=await searchParams;return <PlanReview audience="consumer" cursor={q.cursor}/>;}
