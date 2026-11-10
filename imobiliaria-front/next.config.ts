import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Proxy local: o navegador chama o Next, que consulta a API Spring.
  async rewrites() {
    return [{ source: "/api/:path*", destination: "http://127.0.0.1:8080/:path*" }];
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
