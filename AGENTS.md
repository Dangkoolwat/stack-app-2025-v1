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

## 2A. AI Agent Workflow Rules (Conditional Search Order & Token Guard)

Detailed search and review workflow now lives in:
- **Read:** [agent-workflow-pipeline.md](docs/workflow/agent-workflow-pipeline.md)

Short order:
- Target unclear: `rg --files` / `rg` first.
- Target already a symbol: Serena directly.
- For Java/Vue source, use `tree --symbols`, `deps`, or `search --outline` when the structure is still unclear.
- Broad or unclear impact: add `code-review-graph`.

---

## 2B. Core Policy Document Defense (Surgical Edit Rules)

Editing core policy documents (like `AGENTS.md`) is considered a **Highest Difficulty and Highest Risk** operation. You MUST adhere to these 5 procedures:
1. **Mandatory History Audit & Sequential Thinking**: Before modifying, you must review `docs/history.md` (or git history) and previous logs. You must use the `[Reasoning]` block to formulate your logic before executing changes.
2. **Zero Context Contamination**: Arbitrary deletion or "clean-up" is strictly prohibited. 100% of existing context must be preserved.
3. **Lazy-Loading Architecture**: Keep `AGENTS.md` lightweight. Move detailed guidelines to the `docs/standards/` directory.
4. **Token-Efficient & Unambiguous**: Use short, decisive English for policy text inside `AGENTS.md` and other agent-facing guidance to prevent misinterpretation by other agents.
5. **Detailed Accountability Report**: Explicitly report all additions, modifications, and deletions immediately after the task.

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

Search order:
- Target unclear: `rg --files` / `rg` first.
- Target already a symbol: Serena directly.
- Broad or unclear impact: add `code-review-graph`.

Must read (search `docs/` for keywords matching the target file path/domain):
- high-risk: relevant impact/standard docs
- non-trivial/high-risk coding: `.agents/skills/karpathy-guidelines/SKILL.md`
- cross-module changes: Serena impact analysis (`find_referencing_symbols`) and `code-review-graph` when the blast radius is broad or unclear. Read [code-review-graph-guide.md](docs/standards/code-review-graph-guide.md).
- semantic navigation & editing: [serena-guide.md](docs/standards/serena-guide.md)
- code search & discovery: [semble-operation-guide.md](docs/standards/semble-operation-guide.md), [semble-troubleshooting.md](docs/standards/semble-troubleshooting.md)
- execution engine: read `.agents/skills/superpower/SKILL.md` (BPI workflow) for all non-trivial tasks
- backend changes: backend config and standards (e.g., JPA, REST, Spring Boot patterns, `spring-security-oauth2`, `liquibase-migration`)
- frontend changes: frontend config and standards (e.g., Vue standards, styling guidelines, `pinia-state-management`)

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

Before editing or making architectural judgments:

- **Zero-Trust File Verification**: Never assume a file or directory is missing. You MUST verify its existence using explicit filesystem checks such as terminal `test`/`ls` before reporting it as broken or missing.
- inspect the target file
- inspect nearby code
- inspect direct callers/consumers when behavior may change
- inspect existing patterns before adding new ones

Never edit from filename or memory alone.

---

## 7. Mandatory Lazy-Loaded Policy Triggers

Agents MUST read the required policy file when the following triggers are present in the task or tool intent:

| Trigger | Required policy file |
|---|---|
| `code-review-graph`, knowledge graph, structural analysis, impact radius, blast radius | [code-review-graph-guide.md](docs/standards/code-review-graph-guide.md) |
| `semble_rs`, code search, semantic search, vector index | [semble-operation-guide.md](docs/standards/semble-operation-guide.md) and [semble-troubleshooting.md](docs/standards/semble-troubleshooting.md) |
| `serena`, LSP, semantic navigation, symbol analysis | [serena-guide.md](docs/standards/serena-guide.md) |
| handshake, approval, refactor proposal, blast radius proposal | [handshake-protocol.md](docs/standards/handshake-protocol.md) |
| surgical edit, file modification, refactoring integrity, write_to_file | [surgical-edit-rules.md](docs/standards/surgical-edit-rules.md) |
| validation, build verification, recovery, handoff, task completion | [validation-standard.md](docs/standards/validation-standard.md) |
| agent capability, protocol design, cross-model, token economy | [protocol-design-intent.md](docs/standards/protocol-design-intent.md) |
| non-trivial, high-risk, complex implementation, task planning | `.agents/skills/superpower/SKILL.md` |
| code style, LLM mistakes, behavioral guidelines | `.agents/skills/karpathy-guidelines/SKILL.md` |
| incident report, false positive, hallucination, policy violation | [incident-reports/_template.md](docs/operations/incident-reports/_template.md) |

---

## 8. Monorepo Boundaries

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

## 9. Implementation

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
- **Korean Comment Rule**: All business logic, safety guards, and security comments MUST be written in Korean.

Revert accidental unrelated edits.

---

## 10. Data Safety

- never delete, overwrite, move, or migrate user data (including local DB seed/test data and `.env`) without approval
- use temp paths, backups, or separate outputs for risky operations
- do not hardcode secrets
- do not expose sensitive data in logs/docs/examples/commits
- report partial failures and uncertain state

---

## 11. Verification & Integrity Guardrails
Use evidence-based success criteria (e.g., Exit code 0) and strict atomic rollback protocols.
- **Read required validation rules:** [validation-standard.md](docs/standards/validation-standard.md)
- **Quick reference:** Backend `./mvnw verify` or `./mvnw test`; Frontend `npm run build` or `npm run test`.

---

## 12. Architecture Risk

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

## 13. Skills

- Prefer `.agents/skills/`.
- Local skills override global guidance.
- **Execution Engine**: Use `.agents/skills/superpower/SKILL.md` (Brainstorm -> Plan -> Implement) for all Non-trivial/High-risk tasks.
- **Backend Core**: Refer to `spring-security-oauth2`, `liquibase-migration`, `jpa-expert`, `redis-expert`, etc.
- **Frontend Core**: Refer to `pinia-state-management`, `jhipster-vue-standards`, `bootstrap-vue3`, `vitest`, etc.
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

For non-trivial implementation work, write a concise agent log in the matching domain path:
- Backend: `docs/backend/agent-log/YYYY-MM-DD-<task-name>/`
- Frontend: `docs/frontend/agent-log/YYYY-MM-DD-<task-name>/`
- Docs/config/cross-cutting work: use the nearest owning domain log path and note every affected domain in the log.
- Required files: `proposal.md`, `problem-analysis.md`, `implementation-plan.md`, `walkthrough.md`, `self-check.md`, `final-report.md`
- Use the matching template file in that domain folder as the source of truth.
For failed or paused non-trivial work, record useful findings when appropriate.

Use lightweight logs by default.
Use full logs only for high-risk or requested work (e.g., changes to core policy documents like `AGENTS.md` require detailed accountability reports as per Rule 2B).

---

## 15. Handoff

For paused or handed-off work, report:
- changed files
- decisions made
- assumptions
- verification results
- remaining risks or next steps

Keep handoffs short and factual.

---

## 16. Git

- do not run `git add`, `git commit`, or `git push` without approval
- use Conventional Commits when preparing commit messages
- keep commits scoped to the approved task
- report changed files and verification before asking about commit

---

## 17. Response

- start with the core point
- be concise but complete
- prefer one recommended path
- include alternatives only when risk/cost/architecture differs
- report verification clearly
- do not dump long logs unless requested
- **Report Style**: User-facing Korean reports and responses should use concise nominal/final business style (e.g. `~ 완료`, `~ 확인`). Keep policy text in concise English.

---


## 18. Golden Rule

Make it correct, safe, small, and understandable.

When unsure:
1. read more repo context
2. reduce scope
3. state assumptions
4. ask before changing high-risk areas
