# Server Settings Integration Walkthrough

The "Server Settings" entity has been integrated into the frontend to allowed administrators to manage global server configurations.

## Changes Made

### 1. UI Integration
- Added **서버 설정** (Server Settings) to the very top of the **Entities** menu with a `cogs` icon.
- Created a dedicated edit view for global settings.

### 2. Implementation Details
- **SettingsService**: Handles `GET` and `PUT` requests to `/api/settings`.
- **Settings Component**: A simple, reactive form to manage:
  - Token Validity Seconds
  - Remember-me Token Validity
  - Login Max Failure Attempts
- **Router**: Integrated as a child route under the entities path.
- **i18n**: Added Korean translations for all fields and labels.

## Verification Checklist
- [x] Login as Administrator.
- [x] Navigate to **Entities** -> **서버 설정**.
- [x] Verify that current settings are loaded correctly.
- [x] Update a value and save to verify persistence.
- [x] Verify that the "Server Settings" menu item is at the top.
