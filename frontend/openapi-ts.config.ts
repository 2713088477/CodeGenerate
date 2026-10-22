import { defineConfig } from '@hey-api/openapi-ts'

export default defineConfig({
  input: 'http://localhost:8123/api/v3/api-docs',
  output: 'src/api',
  plugins: [
    '@hey-api/typescript',
    '@hey-api/sdk',
    {
      name: '@hey-api/client-axios',
      // 生成客户端时写入 baseURL，开发环境由 Vite 把 /api 代理到 8123
      baseUrl: '/api',
    },
  ],
})
