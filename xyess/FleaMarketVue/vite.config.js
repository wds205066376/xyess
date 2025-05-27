import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'url'
import autoprefixer from 'autoprefixer'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 9323,
    proxy: {
      '/api': {
        target: 'http://localhost:9321',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        ws: true,
        secure: false,
        credentials: 'include'
      },
      '/file': {
        target: 'http://localhost:9321',
        changeOrigin: true,
        ws: true,
        secure: false,
        credentials: 'include'
      }
    }
  },
  css: {
    postcss: {
      plugins: [
        autoprefixer({
          overrideBrowserslist: ['> 1%', 'last 2 versions']
        })
      ]
    }
  },
  build: {
    target: 'es2015',
    cssTarget: 'chrome61',
    commonjsOptions: {
      include: [/node_modules/],
      transformMixedEsModules: true
    }
  }
}) 