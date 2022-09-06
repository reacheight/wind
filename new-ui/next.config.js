/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  swcMinify: true,
  images: {
    domains: ['avatars.akamai.steamstatic.com'],
  },
}

module.exports = nextConfig
