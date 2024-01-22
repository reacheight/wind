/** @type {import('next').NextConfig} */
const withRI = require('next-transpile-modules')(["react-icons"]);

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
    domains: ['avatars.akamai.steamstatic.com', 'localhost', 'api.windota.xyz', 'win-dota.herokuapp.com', 'avatars.steamstatic.com', '192.168.0.104'],
    minimumCacheTTL: 691200,
  },
}

module.exports = withRI(nextConfig)
