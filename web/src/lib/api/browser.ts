import type { components } from './schema';
export type Models = components['schemas'];
export class ApiError extends Error {
  constructor(public status: number, public problem: Partial<Models['Problem']>) { super(problem.detail || 'The service could not complete this request.'); }
}
export async function api<T>(path: string, options: { method?: 'GET' | 'POST' | 'PATCH'; body?: unknown; file?: File; key?: string; etag?: string; signal?: AbortSignal } = {}): Promise<T> {
  const method = options.method || 'GET';
  const headers: Record<string,string> = { Accept:'application/json' };
  if (method !== 'GET') {
    const csrf = await api<Models['Csrf']>('/auth/csrf', { signal: options.signal });
    headers[csrf.headerName] = csrf.token;
    headers['Content-Type'] = options.file ? 'application/octet-stream' : 'application/json';
    if (options.etag) headers['If-Match'] = options.etag;
    if (options.key) headers['Idempotency-Key'] = options.key;
  }
  let response: Response;
  try { response = await fetch('/api/v1'+path, { method, headers, credentials:'include', cache:'no-store', signal:options.signal ? AbortSignal.any([options.signal,AbortSignal.timeout(15000)]) : AbortSignal.timeout(15000), body:options.file ?? (options.body === undefined ? undefined : JSON.stringify(options.body)) }); }
  catch (e) { if (e instanceof DOMException && e.name === 'AbortError') throw e; throw new ApiError(0,{detail:'Connection interrupted. Your outcome may be unknown. Retry the same submission.'}); }
  if (!response.ok) {
    let problem: Partial<Models['Problem']> = {};
    try { problem = await response.json(); } catch { /* Never expose proxy HTML or raw server errors. */ }
    throw new ApiError(response.status, problem);
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}
export function announceLogout() { window.dispatchEvent(new Event('lmt-clear-drafts')); const channel = new BroadcastChannel('lmt-session'); channel.postMessage('logout'); channel.close(); }
