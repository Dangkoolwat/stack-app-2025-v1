---
name: jhipster-vue-standards
description: Project-specific frontend engineering standards for JHipster-based Vue 3 projects. Includes boundaries between Core, Themes, and Views, and JHipster compatibility rules.
---

# JHipster Vue 3 Standards

This skill defines the architectural boundaries and coding standards for the frontend, ensuring compatibility with JHipster conventions and maintainability of the project.

## 1. Architectural Boundaries

- **Views Layer**: UI composition only. Avoid complex business logic. Use abstracted Base components or BootstrapVue Next components.
- **Core Layer**: Business logic, auth, routing, guards, and API services. No DOM manipulation or Vue component code here.
- **Themes Layer**: Reusable UI components (Base components). This layer handles styling and theme-specific overrides.

## 2. Technical Stack
- **Framework**: Vue 3 (Composition API with `<script setup lang="ts">`).
- **Build Tool**: Vite.
- **State Management**: Pinia (primarily for account/settings). Prefer local component state (`ref`, `reactive`) for view-specific data.
- **UI Library**: BootstrapVue Next (with Bootstrap 5).

## 3. Coding Standards
- **Component Naming**: PascalCase (e.g., `UserDetail.vue`).
- **Store Naming**: `useXxxStore.ts` (Composition API style).
- **API Integration**: Use centralized API layer. Avoid direct `fetch` or `axios` calls in components.
- **Error Handling**: Use centralized error handling conventions.
- **JHipster Compatibility**: Maintain compatibility with JHipster backend interfaces (API paths, DTO structure, security guards).

## 4. Prohibited Patterns (BANs)
- **BAN 1**: No direct style manipulation or raw CSS in the **Views** layer if a utility class exists.
- **BAN 2**: No Vue component code or DOM manipulation inside the **Core** layer.
- **BAN 3**: No direct component or style references across different themes (e.g., Admin vs Landing).

## 5. Verification Checklist
- [ ] Are Core, Themes, and Views boundaries intact?
- [ ] Were new UI elements abstracted into Base components (Themes) where appropriate?
- [ ] Is it compatible with JHipster backend API and security?
- [ ] Does it follow the centralized error handling convention?
- [ ] Does it pass unit tests (Vitest) and lint checks?
