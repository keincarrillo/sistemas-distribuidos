import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

// Ruta absoluta de los abstractos SCSS: se inyectan en todos los .scss de entrada
const abstracts = `@use "${fileURLToPath(new URL('./src/styles/abstracts', import.meta.url))}" as *;`;

export default defineConfig({
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      injectRegister: false,
      manifest: {
        name: 'Tienda SD — tienda en línea',
        short_name: 'Tienda SD',
        description:
          'Tienda en línea como servicio web REST (práctica de Sistemas Distribuidos) como PWA',
        lang: 'es',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        background_color: '#f8fafc',
        theme_color: '#1d4ed8',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' },
          {
            src: '/icons/icon-maskable-512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico,woff2}'],
        navigateFallback: 'index.html',
      },
    }),
  ],
  css: {
    preprocessorOptions: {
      scss: { additionalData: abstracts },
    },
  },
  build: {
    outDir: '../back/src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/health': 'http://localhost:8080',
    },
  },
});
