import type { components } from './schema';
export type Catalog = components['schemas']['Catalog'];
export async function readCatalog(interval: 'monthly' | 'annual'): Promise<Catalog> {
  const origin = process.env.API_INTERNAL_ORIGIN || 'http://127.0.0.1:8080';
  const response = await fetch(`${origin}/api/v1/public/plans?audience=workspace&interval=${interval}`, { cache: 'no-store', signal: AbortSignal.timeout(5000) });
  if (!response.ok) throw new Error('Catalogue unavailable');
  return response.json();
}
