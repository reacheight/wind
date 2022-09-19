/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  swcMinify: true,
  images: {
    domains: ['avatars.akamai.steamstatic.com', 'localhost'],
  },
}

module.exports = nextConfig
