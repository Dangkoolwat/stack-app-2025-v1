import { URL, fileURLToPath } from 'node:url';
import fs from 'node:fs';

import vue from '@vitejs/plugin-vue';
import { defineConfig, normalizePath, loadEnv } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';

const { getAbsoluteFSPath } = await import('swagger-ui-dist');
const swaggerUiPath = getAbsoluteFSPath();

// .env 및 OS 환경 변수에서 서버 포트/URL을 동적으로 불러옵니다.
const env = loadEnv(process.env.NODE_ENV || 'development', process.cwd(), '');
const backendPort = env.BACKEND_PORT || process.env.BACKEND_PORT || '8443';
const backendUrl = env.BACKEND_URL || process.env.BACKEND_URL || `https://localhost:${backendPort}`;

// eslint-disable-next-line prefer-const
let config = defineConfig({
  plugins: [
    vue(),
    viteStaticCopy({
      targets: [
        {
          src: [
            `${normalizePath(swaggerUiPath)}/*.{js,css,html,png}`,
            `!${normalizePath(swaggerUiPath)}/**/index.html`,
            normalizePath(fileURLToPath(new URL('./dist/axios.min.js', import.meta.resolve('axios/package.json')))),
            normalizePath(fileURLToPath(new URL('./src/main/webapp/swagger-ui/index.html', import.meta.url))),
          ],
          dest: 'swagger-ui',
        },
      ],
    }),
  ],
  root: fileURLToPath(new URL('./src/main/webapp/', import.meta.url)),
  publicDir: fileURLToPath(new URL('./target/classes/static/public', import.meta.url)),
  cacheDir: fileURLToPath(new URL('./target/.vite-cache', import.meta.url)),
  build: {
    emptyOutDir: true,
    cssCodeSplit: false,
    outDir: fileURLToPath(new URL('./target/classes/static/', import.meta.url)),
    rollupOptions: {
      input: {
        app: fileURLToPath(new URL('./src/main/webapp/index.html', import.meta.url)),
      },
    },
  },
  resolve: {
    alias: {
      vue: 'vue/dist/vue.esm-bundler.js',
      '@': fileURLToPath(new URL('./src/main/webapp/app/', import.meta.url)),
      '@content': fileURLToPath(new URL('./src/main/webapp/content/', import.meta.url)),
    },
  },
  define: {
    I18N_HASH: '"generated_hash"',
    SERVER_API_URL: JSON.stringify(env.VITE_API_URL || process.env.VITE_API_URL || '/'),
    APP_VERSION: JSON.stringify(env.VITE_APP_VERSION || process.env.VITE_APP_VERSION || 'DEV'),
  },
  server: {
    host: true,
    port: 9000,
    https: {
      key: fs.readFileSync(fileURLToPath(new URL('./src/main/webapp/cert/localhost.key.pem', import.meta.url))),
      cert: fs.readFileSync(fileURLToPath(new URL('./src/main/webapp/cert/localhost.cer.pem', import.meta.url))),
    },
    proxy: Object.fromEntries(
      ['/api', '/management', '/v3/api-docs', '/websocket'].map(res => [
        res,
        {
          target: backendUrl,
          changeOrigin: true,
          secure: false,
          ws: res === '/websocket',
        },
      ]),
    ),
  },
});

// jhipster-needle-add-vite-config - JHipster will add custom config

export default config;
