import { readCatalog } from '../../lib/api/catalog';
export const dynamic = 'force-dynamic';
export default async function Pricing({searchParams}: {searchParams: Promise<{interval?: string}>}) {
  const query = await searchParams;
  const interval = query.interval === 'annual' ? 'annual' : 'monthly';
  let catalog;
  try { catalog = await readCatalog(interval); }
  catch { return <main id="main" tabIndex={-1}><p className="eyebrow">Workspace plans</p><h1>We couldn’t load the catalogue.</h1><p>No prices or allowances are shown while the service is unavailable.</p><a className="button" href={`/pricing?interval=${interval}`}>Try again</a></main>; }
  return <main id="main" tabIndex={-1}>
    <section className="intro"><p className="eyebrow">A workspace for your rental operations</p><h1>Space to start.<br/>Room to grow.</h1><p className="lede">Compare workspace capacity for individual landlords and growing property teams.</p></section>
    <aside className="notice" aria-label="Availability"><strong>Development catalogue</strong><p>These are the specification’s initial allowances, not purchasable offers. Local sandbox registration is available. Paid plans and rental workflows are not available yet.</p></aside>
    <p><a className="button" href="/login">Try Free workspace setup</a></p><div className="toolbar"><h2>Workspace plans</h2><nav className="interval" aria-label="Billing interval"><a href="?interval=monthly" aria-current={interval === 'monthly' ? 'page' : undefined}>Monthly</a><a href="?interval=annual" aria-current={interval === 'annual' ? 'page' : undefined}>Annual</a></nav></div>
    {catalog.items.length === 0 ? <section className="notice"><h2>No published plans</h2><p>There are no current workspace offerings. Please check again later.</p></section> : <div className="plans">{catalog.items.map(plan => <article className="plan" key={plan.versionId}>
      <p className="eyebrow">{plan.baselineOnly ? 'Baseline definition' : 'Published definition'}</p><h3>{plan.name}</h3>
      <p className="price">{plan.amount === null ? 'Not configured' : `₹${plan.amount}`}<span>{plan.amount !== null ? ` / ${interval === 'annual' ? 'year' : 'month'}` : 'Pricing'}</span></p>
      <dl><div><dt>Rentable items</dt><dd>{plan.rentableLimit === null ? 'Contract-defined' : plan.rentableLimit.toLocaleString('en-IN')}</dd></div><div><dt>Managers</dt><dd>{plan.managerLimit === null ? 'Contract-defined' : plan.managerLimit}</dd></div></dl>
      <p className="disclosure">{plan.disclosure}</p><details><summary>Version &amp; terms</summary><p>Version {plan.version}. Effective {new Intl.DateTimeFormat('en-IN',{dateStyle:'medium',timeZone:'Asia/Kolkata'}).format(new Date(plan.effectiveAt))}.</p><p>Currency: {plan.currency}. No tax estimate, communication quota or add-on price is implied where it is not configured.</p></details>
    </article>)}</div>}
    <section className="footnote"><h2>Essential tenancy access is part of the baseline</h2><p>The product specification includes the essential rental lifecycle on Free. The catalogue does not indicate that those workflows have shipped. Shared-bed counting and commercial terms still require configuration.</p><p className="freshness">{catalog.totalMatching} published definitions · As of {new Intl.DateTimeFormat('en-IN', {dateStyle:'medium',timeStyle:'short',timeZone:'Asia/Kolkata'}).format(new Date(catalog.asOf))} IST</p></section>
  </main>;
}
