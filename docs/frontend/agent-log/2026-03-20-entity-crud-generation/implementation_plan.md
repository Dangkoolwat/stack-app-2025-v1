# Implementation Plan - Entity CRUD Generation

## Overview
Automate the creation of CRUD UI in the Vue 3 frontend for identified backend entities. The implementation will follow the existing project patterns (hybrid Composition/Options API) and ensure full i18n support.

## Targeted Entities
1.  **Board** (`/api/boards`)
2.  **Tag** (`/api/tags`)
3.  **CommonCodeGroup** (`/api/common/groups`)
4.  **CommonCodeDetail** (`/api/common/details`)

## 1. i18n Resource Creation
Create or update JSON files in `src/main/webapp/i18n/{lang}/{entity}.json`.
-   Translation keys for title, menu, fields, actions, and messages.
-   Languages: `ko`, `en`.

## 2. Menu & Router Registration
-   **Menu**: Append drop-down items to `src/main/webapp/app/entities/entities-menu.vue`.
-   **Router**: Register entity routes in `src/main/webapp/app/router/entities.ts`.

## 3. Entity Services (`.service.ts`)
Create a service class for each entity to handle API communication using `axios`.
-   Location: `src/main/webapp/app/entities/{entity}/{entity}.service.ts`

## 4. Vue CRUD Component Generation
For each entity, generate:
-   `{entity}.vue` / `{entity}.component.ts`: List view with table and pagination.
-   `{entity}-detail.vue` / `{entity}-detail.component.ts`: Detailed view of a single record.
-   `{entity}-update.vue` / `{entity}-update.component.ts`: Create/Edit form.

## 5. Implementation Steps for each Entity
1.  Verify Backend: Check Resource/Controller for CRUD endpoints.
2.  i18n JSON: Generate `ko` and `en` files.
3.  Service: Implement axios-based service.
4.  Menu & Router: Append the new entity.
5.  Components: Generate UI and Logic files.
6.  Verification: Build and test manually.

## Standards
-   **Styling**: BootstrapVue.
-   **Language**: TypeScript.
-   **i18n**: No hardcoded strings; use `t$`.
-   **Naming**: Kebab-case for paths, PascalCase for components/names.
