/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  eslint: {
    ignoreDuringBuilds: true,
  },
  reactStrictMode: false,
  swcMinify: true,
  images: {
    domains: ['avatars.akamai.steamstatic.com', 'localhost', 'api.windota.xyz', 'win-dota.herokuapp.com'],
    minimumCacheTTL: 691200,
  },
}

module.exports = nextConfig
