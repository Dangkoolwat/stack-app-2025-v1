# Server Settings Integration Plan

Integrate the backend `Settings` entity (singleton) into the entity menu and provide a simple edit interface for administrators.

## Proposed Changes

### [Frontend]

#### [NEW] [settings.service.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/settings/settings.service.ts)
- Implement `get()` and `update(settings)` methods for `/api/settings`.

#### [NEW] [settings.vue](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/settings/settings.vue)
- Simple form to edit `tokenValiditySeconds`, `tokenValiditySecondsForRememberMe`, `loginMaxFailureAttempts`, and `description`.

#### [NEW] [settings.component.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/settings/settings.component.ts)
- Logic to fetch and save the settings.

#### [MODIFY] [main.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/main.ts)
- Provide `settingsService`.

#### [MODIFY] [entities.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/router/entities.ts)
- Add route for `/settings`.

#### [MODIFY] [entities-menu.vue](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/entities-menu.vue)
- Add "Server Settings" (`cogs` icon) to the very top.

#### [MODIFY] [ko.json](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/i18n/ko/global.json) (or similar)
- Add translation keys for `entities.settings`.

## Verification Plan

### Manual Verification
- Log in as admin.
- Click "Server Settings" in the entity menu.
- Verify that current settings are loaded.
- Change a value and save.
- Verify that the change is persisted (refresh page).
