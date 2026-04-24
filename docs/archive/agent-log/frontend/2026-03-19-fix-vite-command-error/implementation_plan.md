# Implementation Plan: Custom SSL Certificate for Vite

The user has a `cert` folder with SSL certificates and wants to use them for the Vite dev server at `https://localhost:9000`.

## User Review Required
> [!IMPORTANT]
> I found a `certs` folder in the project root containing `localhost.cer.pem` and `localhost.key.pem`. I will assume these are the correct certificates to use. If not, please provide the correct path.

## Proposed Changes

### [Component Name] SSL Certificate Relocation
I will move the existing `certs` folder contents (or the one provided by the user) to a more "client-standard" location if necessary, or just use them from the root. For Vite, putting them in the root or a `certs` folder is common. I will move them to `src/main/webapp/cert` to group them with client assets.

#### [NEW] [cert folder](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/cert)
- Copy `localhost.cer.pem` and `localhost.key.pem` to this location.

### [Component Name] Vite Configuration update
I will update `vite.config.ts` and `vite.config.mts` to use the specific certificate files.

#### [MODIFY] [vite.config.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/vite.config.ts)
- Set `server.https: { key: fs.readFileSync('./src/main/webapp/cert/localhost.key.pem'), cert: fs.readFileSync('./src/main/webapp/cert/localhost.cer.pem') }`.
- Set `server.port: 9000`.

#### [MODIFY] [vite.config.mts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/vite.config.mts)
- Sync with the same changes.

## Verification Plan

### Automated Tests
- Verify that Vite starts on `https://localhost:9000`.
- Verify that the browser can connect using the provided certificate.

### Manual Verification
- Confirm that the Vite log shows `https://localhost:9000`.
