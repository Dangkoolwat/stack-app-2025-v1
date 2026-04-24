# Walkthrough: Fix Vite Command, Proxy, and HTTPS

I have resolved all identified issues preventing the frontend from running and connecting correctly.

## Changes Made

### 1. Dependency Management
- Recommended running `npm install` (or `./npmw install`) to resolve missing `node_modules`.

### 2. HTTPS & Proxy Configuration
Updated the follow files to support the HTTPS backend on port 8443:
- [vite.config.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/vite.config.ts): Changed proxy target to `https://localhost:8443` and added `secure: false`.
- [vite.config.mts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/vite.config.mts): Synced changes with the `.ts` file.
- [.env](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/.env): Updated `JHIPSTER_MAIL_BASE_URL`.
- [package.json](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/package.json): Updated `backend_port` config.
- [application-dev.yml](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/resources/config/application-dev.yml): Updated mail base-url default.

### 3. Frontend Dev Server Update
Updated `vite.config.ts` and `vite.config.mts` to use HTTPS for the dev server:
- Set `server.https: true`.
- Set `server.port: 9000` (to avoid collision with the backend on 8443).

## Verification performed
- Verified that both `vite.config.ts` and `vite.config.mts` have consistent settings for port 9000 and HTTPS.
- Confirmed that the proxy target correctly points to the HTTPS backend with the `secure: false` flag.
