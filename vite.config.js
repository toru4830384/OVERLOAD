import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// 開発も同一オリジンの/apiを使い、CookieやCSRFの本番との差を減らす。
export default defineConfig({
  plugins: [react()],
  server: { watch: { usePolling: process.env.OVERLOAD_DEV_POLLING === '1' }, proxy: { '/api': process.env.OVERLOAD_DEV_API_TARGET || 'http://127.0.0.1:8080' } },
})
