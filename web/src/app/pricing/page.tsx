import { readCatalog } from '../../lib/api/catalog';
export const dynamic = 'force-dynamic';
export const metadata = { title: 'LMT · Plans and memberships', description: 'Published workspace plans and independent personal membership offerings.' };
export default async function Pricing({searchParams}: {searchParams: Promise<{interval?: string; audience?: string}>}) {
  const query = await searchParams;
  const interval = query.interval === 'annual' ? 'annual' : 'monthly';
  const audience = query.audience === 'consumer' ? 'consumer' : 'workspace';
  const personal = audience === 'consumer';
  const title = personal ? 'Personal memberships' : 'Workspace plans';
  const url = (nextAudience = audience, nextInterval = interval) => `/pricing?audience=${nextAudience}&interval=${nextInterval}`;
  const navigation = <nav className="interval" aria-label="Plan audience"><a href={url('workspace')} aria-current={!personal ? 'page' : undefined}>Workspace plans</a><a href={url('consumer')} aria-current={personal ? 'page' : undefined}>Personal memberships</a></nav>;
  let catalog;
  try { catalog = await readCatalog(interval, audience); }
  catch { return <main id="main" tabIndex={-1}>{navigation}<p className="eyebrow">{title}</p><h1>We couldn’t load the catalogue.</h1><p>No prices or allowances are shown while the service is unavailable.</p><a className="button" href={url()}>Try again</a></main>; }
  return <main id="main" tabIndex={-1}>
    {navigation}
    <section className="intro"><p className="eyebrow">{personal ? 'Membership belongs to your account' : 'A workspace for your rental operations'}</p><h1>{personal ? 'Your personal membership.' : <>Space to start.<br/>Room to grow.</>}</h1><p className="lede">{personal ? 'Personal membership is independent of a landlord’s workspace subscription and does not automatically cover co-tenants.' : 'Compare workspace capacity for individual landlords and growing property teams.'}</p></section>
    <aside className="notice" aria-label="Availability"><strong>Development catalogue</strong><p>{personal ? 'Personal plans are not available for purchase. Only published, effective offerings appear here; an empty catalogue does not change tenancy access.' : 'These are the specification’s initial allowances, not purchasable offers. Local sandbox registration is available. Paid plans and rental workflows are not available yet.'}</p></aside>
    {!personal && <p><a className="button" href="/login">Try Free workspace setup</a></p>}<div className="toolbar"><h2>{title}</h2><nav className="interval" aria-label="Billing interval"><a href={url(audience, 'monthly')} aria-current={interval === 'monthly' ? 'page' : undefined}>Monthly</a><a href={url(audience, 'annual')} aria-current={interval === 'annual' ? 'page' : undefined}>Annual</a></nav></div>
    {catalog.items.length === 0 ? <section className="notice"><h2>No published plans</h2><p>{personal ? 'There are no published personal memberships. No payment is collected. You can still compare workspace plans using the audience links above.' : 'There are no current workspace offerings. Please check again later.'}</p></section> : <div className="plans">{catalog.items.map(plan => <article className="plan" key={plan.versionId}>
      <p className="eyebrow">{plan.baselineOnly ? 'Baseline definition' : 'Published definition'}</p><h3>{plan.name}</h3>
      <p className="price">{plan.amount === null ? 'Not configured' : `₹${plan.amount}`}<span>{plan.amount !== null ? ` / ${interval === 'annual' ? 'year' : 'month'}` : 'Pricing'}</span></p>
      {!personal && <dl><div><dt>Rentable items</dt><dd>{plan.rentableLimit === null ? 'Contract-defined' : plan.rentableLimit.toLocaleString('en-IN')}</dd></div><div><dt>Managers</dt><dd>{plan.managerLimit === null ? 'Contract-defined' : plan.managerLimit}</dd></div></dl>}
      <p className="disclosure">{plan.disclosure}</p><details><summary>Version &amp; terms</summary><p>Version {plan.version}. Effective {new Intl.DateTimeFormat('en-IN',{dateStyle:'medium',timeZone:'Asia/Kolkata'}).format(new Date(plan.effectiveAt))}.</p><p>Currency: {plan.currency}. No tax estimate, communication quota or add-on price is implied where it is not configured.</p></details>
    </article>)}</div>}
    <section className="footnote">{!personal && <><h2>Essential tenancy access is part of the baseline</h2><p>The product specification includes the essential rental lifecycle on Free. The catalogue does not indicate that those workflows have shipped. Shared-bed counting and commercial terms still require configuration.</p></>}<p className="freshness">{catalog.totalMatching} published definitions · As of {new Intl.DateTimeFormat('en-IN', {dateStyle:'medium',timeStyle:'short',timeZone:'Asia/Kolkata'}).format(new Date(catalog.asOf))} IST</p></section>
  </main>;
}
