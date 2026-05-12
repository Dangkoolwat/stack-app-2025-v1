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
- shared components (defined as files in `shared/`, `common/`, or imported by >1 consumer)
- build/deploy/config
- file I/O
- destructive ops
- workflow/state flow (e.g. Pinia/Vuex actions)

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

Must read (search `docs/` for keywords matching the target file path/domain):
- high-risk: relevant impact/standard docs
- non-trivial/high-risk coding: `.agents/skills/karpathy-guidelines/SKILL.md`
- cross-module changes: Serena impact analysis (`find_referencing_symbols`) and `code-review-graph` impact tools.
- semantic navigation & editing: `docs/standards/serena-guide.md`
- code search & discovery: `docs/standards/semble-guide.md`
- backend changes: backend config and standards (e.g., JPA, REST, Spring Boot patterns)
- frontend changes: frontend config and standards (e.g., Vue standards, styling guidelines)

If a required file is missing, say so and continue with the best available repo context.

---

## 5. Approval Rules

Do not ask vague questions like "What should I do?"

Instead:
- state the likely direction
- state assumptions
- state the smallest safe approach
- ask for approval only when required (i.e., changing public signatures, behavior, or multi-file contracts)

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
- prefer targeted edits; avoid rewriting whole files/functions unless necessary
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

- never delete, overwrite, move, or migrate user data (including local DB seed/test data and `.env`) without approval
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

## 12. Code Review Graph (Structural Analysis)

`code-review-graph` is for dependency/blast-radius checks only.
It is not a source of truth. Code, tests, and current docs win.

Use `code-review-graph` tools when:
- changing services/managers/stores/workflow state
- changing module/package dependencies
- touching more than 3 modules
- changing backend/frontend contracts
- changing auth, DB, cache, deploy, or other cross-cutting infra
- planning large refactors, renames, or moves

Tool usage:
1. Run `detect_changes` or `get_impact_radius` for blast-radius analysis.
2. Use `get_architecture_overview` or `list_communities` for high-level structure.
3. Do NOT rely on static reports; use the tools to query the current graph state.

Do not treat tool output as proof that a change is safe.
Use it to decide what else to inspect with Serena.

---

## 13. Serena (LSP Semantic Agent)

Serena is the primary tool for semantic code navigation, impact analysis, and precise editing. It leverages the Language Server Protocol (LSP) for 100% accurate symbol resolution.

Refer to the full guide: `docs/standards/serena-guide.md`

Operating Principles:
1. **Precision First**: 오타 수정이나 단순 텍스트 변경 외의 모든 **Non-trivial** 작업은 Serena의 심볼 분석(`find_symbol`, `find_referencing_symbols`)을 우선 수행한다.
2. **Memory-Driven**: 새로운 아키텍처 결정이나 복잡한 비즈니스 로직 수정 시 반드시 `write_memory`를 통해 기록을 남긴다.
3. **Zero Assumption**: 코드를 읽기 전 Serena의 `get_symbols_overview`를 통해 파일 구조를 먼저 파악한다.
4. **Surgical Edits**: 대규모 파일 치환 대신 `replace_symbol_body` 또는 `insert_after_symbol`을 사용하여 정밀하게 수정한다.

Do not rely on outdated Graphify reports for implementation details. Serena provides real-time, IDE-level understanding of the code.

---

## 19. Semble (Code Search & Discovery)

Semble is the primary tool for fast, token-efficient code search. It should be used at the beginning of any task to narrow down relevant files and code blocks.

Refer to the full guide: `docs/standards/semble-guide.md`

Operating Principles:
1. **Search First**: Before reading files or analyzing structure, use `semble_search` with natural language or code queries to find candidates.
2. **Explore Related**: Use `semble_find_related` to discover similar patterns or implementations across the codebase.
3. **Token Economy**: Use Semble to avoid reading large files or traversing deep directory structures when a targeted search can identify the correct location.

---

## 14. Skills

- Prefer `.agents/skills/`.
- Local skills override global guidance.
- If a task matches a local skill, read it before editing.
- For non-trivial/high-risk coding, read `.agents/skills/karpathy-guidelines/SKILL.md`.
- Follow surgical changes, simplicity, explicit assumptions, and minimal diffs.
- Use/install tools locally only when needed.

---

## 15. Docs and Logs

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

## 16. Handoff

For paused or handed-off work, report:
- changed files
- decisions made
- assumptions
- verification results
- remaining risks or next steps

Keep handoffs short and factual.

---

## 17. Git

- do not run `git add`, `git commit`, or `git push` without approval
- use Conventional Commits when preparing commit messages
- keep commits scoped to the approved task
- report changed files and verification before asking about commit

---

## 18. Response

- start with the core point
- be concise but complete
- prefer one recommended path
- include alternatives only when risk/cost/architecture differs
- report verification clearly
- do not dump long logs unless requested

---

## 20. Token Efficiency Hierarchy (Token-Saving Protocol)

에이전트 친구들이 작업할 때 특히 토큰 절약할때 아래 순서대로 하세요.

0.  **0순위: `Semble` (Code Search & Discovery)**
    - Use for hybrid (semantic + keyword) search across the local codebase.
    - Highly effective for finding candidates without consuming large amounts of tokens.
    - Tools: `mcp_semble_search`, `mcp_semble_find_related`.
1.  **1순위: `code-review-graph` (Structural Analysis)**
    - Use for high-level codebase understanding, blast-radius, and dependency maps.
    - Tools: `detect_changes`, `get_impact_radius`, `list_communities`.
2.  **2순위: `Serena` (LSP Precision)**
    - Use for real-time symbol resolution, finding callers, and surgical edits.
    - Tools: `find_symbol`, `find_referencing_symbols`, `get_symbols_overview`.
3.  **3순위: `Grep / Read` (Text & Non-code)**
    - Use for searching strings in non-code files (YAML, JSON, Markdown) or simple text matches.
    - Tools: `grep_search`, `view_file`.
4.  **4순위: `git` (History)**
    - Use for understanding *why* a change was made or tracking evolution.
    - Tools: `git log`, `git show`.

---

## 21. Golden Rule

Make it correct, safe, small, and understandable.

When unsure:
1. read more repo context
2. reduce scope
3. state assumptions
4. ask before changing high-risk areas