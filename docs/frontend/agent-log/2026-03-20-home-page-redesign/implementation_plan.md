# UI Improvements and Home Page Redesign Plan

Improve button layout and redesign the Home page with conditional rendering (Public/Admin) and cleanup unused assets.

## Proposed Changes

### [Frontend]

#### [MODIFY] [config.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/shared/config/config.ts)
- Add icons for the Home page (e.g., `rocket`, `shield-alt`, `cloud`, `server`, etc.).

#### [MODIFY] [Update Pages]
- Add `me-2` to "Cancel" buttons in:
  - `user-management-edit.vue`
  - `board-update.vue`
  - `tag-update.vue`
  - `common-code-group-update.vue`
  - `common-code-detail-update.vue`
  - `settings.vue`

#### [MODIFY] [home.vue](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/core/home/home.vue)
- Use `v-if` to toggle between:
  - **Public Mode (Logged-out)**: Marketing-style page using `README.md` content (cards, features, tech stack).
  - **Admin Mode (Logged-in)**: Brief admin guide and current user information (login, roles).

#### [MODIFY] [home.json (ko/en)]
- Extract all new text (Hero, Goals, Features) into i18n JSON files.
- Ensure no hardcoded strings in `home.vue`.

#### [DELETE] [unused images]
- Remove JHipster default images (e.g., `logo-jhipster.png`, `hipster.png`, `hipster2.png`) if they are no longer used.

## Verification Plan

### Manual Verification
- Verify button spacing on all update pages.
- Verify Home page appearance when logged out (rich marketing content).
- Verify Home page appearance when logged in (admin/user info).
- Verify i18n support by switching languages.
