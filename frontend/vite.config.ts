import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import ElementPlus from 'unplugin-element-plus/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    ElementPlus({}),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        // 默认代理到本地后端,需要连别的后端实例时用 VITE_PROXY_TARGET 覆盖
        target: process.env.VITE_PROXY_TARGET ?? 'http://localhost:8123',
        changeOrigin: true,
      },
    },
  },
})
