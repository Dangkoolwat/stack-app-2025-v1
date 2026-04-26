# AGENTS.md

Minimum rules for agents working in this repo.
Keep changes correct, safe, small, and reversible.

---

## 1. Source Order

When rules conflict, follow this order:

1. User instructions
2. Nearest `AGENTS.md`
3. `docs/standards/`, `docs/workflow/`, `docs/operations/`
4. Other docs
5. Code, tests, config
6. General knowledge

Higher source wins. Ask before acting on risky conflicts.

---

## 2. Startup

For substantial discussion, architecture decisions, migrations, or implementation:

- Read `README.md`
- Read this `AGENTS.md`
- Read relevant backend config, e.g. `pom.xml`
- Read relevant frontend config, e.g. `package.json`, `vite.config.*`
- Read nearby code before editing

Do not invent build, test, lint, or run commands. Discover them from repo files.

---

## 3. Task Levels

Classify before acting.

### Trivial

Small local changes with no shared impact.

Allowed:
- typos/comments
- static text
- one-component styling
- isolated tests
- clearly local fixes

Not trivial if it touches:
- shared state
- API/DTO/validation
- auth/session/security
- DB/cache/persistence
- global styles/theme
- shared components
- build/deploy/config
- file I/O
- destructive ops
- workflow/state flow

Procedure:
- implement directly
- run scoped verification
- report briefly

### Non-trivial

Behavior, flow, integration, or multi-file changes.

Procedure:
- analyze first
- read required local rules
- propose the smallest safe approach
- state assumptions
- ask approval when scope, risk, or impact is unclear

### High-risk

Always high-risk:
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

Procedure:
- analyze first
- read required guides/skills
- check cross-module impact
- propose safest small approach
- wait for explicit approval before editing

---

## 4. Required Reading

Open linked/local rules when their trigger matches. Do not rely on memory.

Must read:
- high-risk: relevant impact/standard docs
- non-trivial/high-risk coding: `.agents/skills/karpathy-guidelines/SKILL.md`
- cross-module changes: `docs/graphify/GRAPH_REPORT.md`
- backend changes: backend config and relevant backend standards
- frontend changes: frontend config and relevant frontend standards

If a required file is missing, say so and continue with the best available repo context.

---

## 5. Approval Rules

Do not ask vague questions like "What should I do?"

Instead:
- state the likely direction
- state assumptions
- state the smallest safe approach
- ask for approval only when required

Trivial low-risk tasks may proceed directly.
High-risk tasks require explicit approval before editing.

---

## 6. Read Before Write

Before editing:

- inspect the target file
- inspect nearby code
- inspect direct callers/consumers when behavior may change
- inspect existing patterns before adding new ones

Never edit from filename or memory alone.

---

## 7. Monorepo Boundaries

Before editing, identify the affected area:

- backend
- frontend
- shared contract
- config
- docs
- cross-cutting workflow

Backend changes use backend verification.
Frontend changes use frontend verification.
Shared API/DTO/validation/auth changes require checking both sides when practical.

---

## 8. Implementation

- make the smallest safe change
- prefer targeted edits over broad rewrites
- stay within approved scope
- preserve existing architecture/naming/conventions
- avoid opportunistic refactors
- avoid unrelated formatting churn
- do not rename/move unrelated code
- do not add dependencies/frameworks without approval
- do not remove compatibility behavior without approval
- state assumptions when unclear

Revert accidental unrelated edits.

---

## 9. Data Safety

- never delete, overwrite, move, or migrate user data without approval
- use temp paths, backups, or separate outputs for risky operations
- do not hardcode secrets
- do not expose sensitive data in logs/docs/examples/commits
- report partial failures and uncertain state

---

## 10. Verification

Use the strongest practical scoped verification. Do not invent commands.

Pre-change tests are optional.
Run them first for high-risk work, unknown repo state, bug reproduction, failing CI/tests, or shared DB/cache/auth/API/refactor changes.

Post-change verification is required when practical.
Do not claim success without it.

Discover commands from:
- `README.md`
- `pom.xml`
- `package.json`
- existing scripts
- `docs/operations/`

Backend:
- Load `.env` when running Maven tests.
- Unit/non-IT: `./mvnw test`
- Full/IT: `./mvnw verify`
- Follow `docs/operations/testing-guideline.md` for Spring Boot 4 IT, JWT auth, DB cleanup, rate-limit, and cache tests.

Frontend:
- Use `package.json` scripts.
- Run scoped tests/lint/typecheck/build according to impact.
- Shared UI/API/auth/routing/build changes need broader checks.

Report:
- commands run
- pass/fail
- key errors only
- skipped checks and why

If verification cannot run, explain why and give the best static check.

---

## 11. Architecture Risk

For risky changes, check:

- callers/consumers
- shared state
- backend/frontend contract compatibility
- persistence impact
- security impact
- cache impact
- rollback path
- data loss risk
- runtime/env assumptions

Keep business logic out of presentation-only layers unless existing architecture requires it.
Prefer existing project patterns over new abstractions.

---

## 12. Graphify

Graphify is for dependency/blast-radius checks only.
It is not a source of truth. Code, tests, and current docs win.

Use Graphify when:
- changing services/managers/stores/workflow state
- changing module/package dependencies
- touching more than 3 modules
- changing backend/frontend contracts
- changing auth, DB, cache, deploy, or other cross-cutting infra
- planning large refactors, renames, or moves

Do not use Graphify for:
- typos/comments/docs-only
- local UI text
- one-component styling
- isolated local fixes

Exception:
Use Graphify for UI/styling only if it affects shared components, global styles, state flow, API contracts, or module boundaries.

Read order:
1. `docs/graphify/GRAPH_REPORT.md`
2. `docs/graphify/graph.json` if dependency details are needed
3. `docs/graphify/graph.html` if visual review helps

Do not treat Graphify output as proof that a change is safe.
Use it to decide what else to inspect.

---

## 13. Skills

- Prefer `.agents/skills/`.
- Local skills override global guidance.
- If a task matches a local skill, read it before editing.
- For non-trivial/high-risk coding, read `.agents/skills/karpathy-guidelines/SKILL.md`.
- Follow surgical changes, simplicity, explicit assumptions, and minimal diffs.
- Use/install tools locally only when needed.

---

## 14. Docs and Logs

Guide docs under `docs/standards/`, `docs/workflow/`, and `docs/operations/` are authoritative.

Do not modify guide docs unless the user asks.
If a guide seems wrong/outdated, report:
- path
- issue
- suggested fix

For non-trivial implementation work, write a concise agent log in the relevant backend/frontend log path.
For failed or paused non-trivial work, record useful findings when appropriate.

Use lightweight logs by default.
Use full logs only for high-risk or requested work.

---

## 15. Git

- do not run `git add`, `git commit`, or `git push` without approval
- use Conventional Commits when preparing commit messages
- keep commits scoped to the approved task
- report changed files and verification before asking about commit

---

## 16. Response

- start with the core point
- be concise but complete
- prefer one recommended path
- include alternatives only when risk/cost/architecture differs
- report verification clearly
- do not dump long logs unless requested

---

## 17. Golden Rule

Make it correct, safe, small, and understandable.

When unsure:
1. read more repo context
2. reduce scope
3. state assumptions
4. ask before changing high-risk areas