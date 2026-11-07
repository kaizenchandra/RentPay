// Explicit local fault injection; always restart the local database in finally.
import { execFileSync } from 'node:child_process';
import { chromium } from 'playwright';
import assert from 'node:assert/strict';
const browser = await chromium.launch();
const page = await browser.newPage();
const origin = 'http://127.0.0.1:3000';
const before = await (await fetch(origin+'/api/v1/public/plans')).json();
try {
  execFileSync('docker', ['compose','-f','../compose.yaml','stop','db'], {stdio:'inherit'});
  const failed = await fetch(origin+'/api/v1/public/plans');
  assert.equal(failed.status,503);
  const problem=await failed.json();
  assert.equal(problem.code,'CATALOG_UNAVAILABLE');
  assert.equal(failed.headers.get('retry-after'),'5');
  assert.ok(problem.correlationId);
  await page.goto(origin+'/pricing?interval=annual');
  await page.getByRole('heading',{name:'We couldn’t load the catalogue.'}).waitFor();
  assert.equal(await page.getByRole('article').count(),0);
  execFileSync('docker',['compose','-f','../compose.yaml','up','-d','--wait','db'],{stdio:'inherit'});
  await page.getByRole('link',{name:'Try again'}).click();
  await page.getByRole('heading',{name:'Free',exact:true}).waitFor();
  assert.ok(page.url().includes('interval=annual'));
  const after = await (await fetch(origin+'/api/v1/public/plans')).json();
  assert.deepEqual(after.items,before.items);
  await page.screenshot({path:'/tmp/lmt-pricing.png',fullPage:true});
  console.log('PASS: real DB outage -> safe 503/no fake plans -> retry -> same persisted versions');
} finally {
  execFileSync('docker',['compose','-f','../compose.yaml','up','-d','--wait','db'],{stdio:'inherit'});
  await browser.close();
}
