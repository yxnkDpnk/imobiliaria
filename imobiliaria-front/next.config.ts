import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Proxy local: o navegador chama o Next, que consulta a API Spring.
  async rewrites() {
    return [{ source: "/api/imoveis", destination: "http://127.0.0.1:8080/imoveis" }];
  },
  experimental: {
    agentFeedback: true,
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
