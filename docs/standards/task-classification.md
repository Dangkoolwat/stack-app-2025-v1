# Task Levels & Classification

Classify the task before acting. The classification determines the approval rules and verification depth.

## 1. Trivial

Small local changes with no shared impact.

**Allowed:**
- typos/comments
- static text
- one-component styling
- isolated tests
- clearly local fixes

**Not trivial if it touches:**
- shared state
- API/DTO/validation
- auth/session/security
- DB/cache/persistence
- global styles/theme
- shared components (defined as files in `shared/`, `common/`, or imported by >1 consumer)
- build/deploy/config
- file I/O
- destructive ops
- workflow/state flow (e.g. Pinia/Vuex actions)

**Procedure:**
- implement directly
- run scoped verification
- report briefly

## 2. Non-trivial

Behavior, flow, integration, or multi-file changes.

**Procedure:**
- analyze first
- read required local rules (`superpower` skill BPI workflow)
- propose the smallest safe approach
- state assumptions
- ask approval when scope, risk, or impact is unclear

## 3. High-risk

**Always high-risk:**
- security/auth/session
- DB schema/migration/persistence
- API contracts shared by backend/frontend
- DTO/request/response/validation contracts
- cache behavior/invalidation/Redis payloads
- build/deploy/env/secrets/CI
- dependency/framework upgrades
- shared state/global stores/workflow transitions
- file overwrite/delete/move/migration
- large refactors/renames/package moves

**Procedure:**
- analyze first
- read required guides/skills
- check cross-module impact (Serena, Code Review Graph)
- propose safest small approach
- wait for explicit approval before editing
