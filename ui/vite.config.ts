import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import vitePluginRequire from 'vite-plugin-require';

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:7979',
    },
  },
  plugins: [vitePluginRequire(), react()],
});
