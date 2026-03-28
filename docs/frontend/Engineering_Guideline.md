# Frontend Engineering Guideline

Document role: reference-only implementation notes for frontend contributors.
Use this file for orientation, not as a higher-priority rule source than `AGENTS.md`, `docs/standards/`, `docs/workflow/`, or `docs/operations/`.

## 1. Preferred Stack
- Framework: Vue 3 (Composition API)
- Build Tool: Vite
- State: Pinia for JHipster account and settings integration
- UI: PrimeVue through the Themes layer

## 2. Coding and Naming Rules
- Components: PascalCase, for example `UserDetail.vue`
- Stores: `useXxxStore.ts` in Composition API style
- State management: prefer local component state unless the data must be shared globally

## 3. Engineering Discussion Rules
For architectural changes or complex features, propose the direction before coding:
1. Problem: define the current limitation or requirement.
2. Proposal: describe the design that fits the architecture principles.
3. Trade-offs: explain the impact on theme independence, complexity, and delivery speed.
4. Risks: identify possible conflicts with JHipster conventions or other side effects.

## 4. Explicit Bans
- BAN 1: No direct PrimeVue usage inside the Views layer.
- BAN 2: No Vue component code or DOM manipulation inside the Core layer.
- BAN 3: No direct component or style references across themes such as Admin and Landing.

## 5. Review and Verification Checklist
Review these points after implementation:
- [ ] Are Core, Themes, and Views boundaries still intact?
- [ ] Were new UI elements abstracted into Base components where appropriate?
- [ ] Does the frontend remain compatible with JHipster backend interfaces such as API paths and security guards?
- [ ] Does the implementation follow the centralized error handling convention?

## 6. Branch and Workflow Notes
1. Agent work should normally happen on an isolated branch for safety.
2. Deliverables should pass unit tests, lint checks, and build verification.
