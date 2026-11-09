import type { NextConfig } from 'next';
const config: NextConfig = {
  poweredByHeader: false,
  output: 'standalone',
  async headers() {
    return ['/admin','/admin/:path*','/login','/register','/verify-otp','/account','/account/:path*','/workspace','/workspace/:path*','/create-workspace','/setup'].map(source => ({source,headers:[{key:'Cache-Control',value:'private, no-store'},{key:'X-Robots-Tag',value:'noindex, nofollow'}]}));
  },
  async rewrites() {
    return [{ source: '/api/v1/:path*', destination: `${process.env.API_INTERNAL_ORIGIN || 'http://127.0.0.1:8080'}/api/v1/:path*` }];
  },
};
export default config;
