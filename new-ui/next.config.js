/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  reactStrictMode: false,
  swcMinify: true,
  images: {
    domains: ['avatars.akamai.steamstatic.com', 'localhost', 'api.windota.xyz', 'win-dota.herokuapp.com'],
  },
}

module.exports = nextConfig
