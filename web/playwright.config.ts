import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir: './tests', fullyParallel: false,
  // Offering tests temporarily change the shared local public catalogue.
  workers: 1,
  use: { baseURL: 'http://127.0.0.1:3000' },
  projects: [{ name: 'desktop', use: { ...devices['Desktop Chrome'] } }, { name: 'mobile', use: { ...devices['Pixel 7'] } }],
});
