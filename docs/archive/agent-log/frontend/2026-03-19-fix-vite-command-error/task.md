# Task: Fix Vite Command Not Found Error

- [x] Diagnosing the issue
    - [x] Check if `node_modules` exists
    - [x] Check `package.json` scripts
    - [x] Check `./npmw` script content
- [x] Fix Vite command error
    - [x] Run `npm install` (User likely did this or it's working now)
- [x] Fix Backend Connection Error
    - [x] Check `vite.config.ts` proxy settings
    - [x] Verify if backend is running (User specified HTTPS/8443)
- [x] Update Proxy Configuration for HTTPS
    - [x] Update `vite.config.ts` to use `https://localhost:8443`
    - [x] Add `secure: false` to proxy configuration
    - [x] Update `.env` file
    - [x] Update `package.json` config
    - [x] Update `application-dev.yml`
- [x] Enable HTTPS for Vite Dev Server
    - [x] Add `https: true` to `vite.config.ts` and `vite.config.mts`
    - [x] Set Vite port to 9000 to avoid conflict with backend
