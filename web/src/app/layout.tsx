import type { Metadata } from 'next';
import './globals.css';
export const metadata: Metadata = { title: 'LMT · Workspace plans', description: 'Landlord, Manager, Tenant workspace plan catalogue.' };
export default function Layout({children}: {children: React.ReactNode}) {
  return <html lang="en-IN"><body><a className="skip" href="#main">Skip to content</a><header><a className="brand" href="/pricing"><span aria-hidden="true">L</span>LMT</a><span>Landlord · Manager · Tenant</span><nav aria-label="Main"><a href="/pricing">Workspace plans</a><a href="/account">Your workspaces</a></nav></header>{children}<footer>LMT · India <span>Personal rental-tenant memberships are separate from workspace plans.</span></footer></body></html>;
}
