import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  base: '/blog-manager/',
  plugins: [
    vue(),
    {
      name: 'redirect-blog-manager',
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          const url = req.url || ''
          if (url === '/blog-manager' || url.startsWith('/blog-manager?')) {
            const query = url.includes('?') ? url.slice(url.indexOf('?')) : ''
            res.statusCode = 302
            res.setHeader('Location', `/blog-manager/${query}`)
            res.end()
            return
          }
          next()
        })
      },
    },
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: 'localhost',
    port: 80,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080/agent-blog/server',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8080/agent-blog/server',
        changeOrigin: true,
      },
    },
  },
})
