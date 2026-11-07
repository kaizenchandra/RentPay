import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
test('catalogue shows real API data and interval selection', async ({ page, request }) => {
  const response = await request.get('/api/v1/public/plans');
  expect(response.ok()).toBeTruthy();
  const catalog = await response.json();
  await page.goto('/pricing');
  await expect(page.getByRole('heading', { name: 'Space to start. Room to grow.' })).toBeVisible();
  await expect(page.getByRole('article')).toHaveCount(catalog.totalMatching);
  for (const plan of catalog.items) await expect(page.getByRole('heading', { name: plan.name, exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Annual', exact: true }).click();
  await expect(page).toHaveURL(/interval=annual/);
  await expect(page.getByRole('link', { name: 'Annual', exact: true })).toHaveAttribute('aria-current', 'page');
  await expect(page.getByText('Development catalogue', { exact: true })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBeTruthy();
  expect((await new AxeBuilder({ page }).withTags(['wcag2a','wcag2aa','wcag21aa','wcag22aa']).analyze()).violations).toEqual([]);
});
test('keyboard skip link and version disclosure work', async ({page}) => {
  await page.goto('/pricing');
  await page.keyboard.press('Tab');
  await expect(page.getByRole('link', {name:'Skip to content'})).toBeFocused();
  await page.keyboard.press('Enter');
  const summary=page.locator('summary').first();
  await expect(summary).toBeVisible();
  await summary.focus();
  await expect(summary).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.getByText(/Version \d+\. Effective/).first()).toBeVisible();
});
