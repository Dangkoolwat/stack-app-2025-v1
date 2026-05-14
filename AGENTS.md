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

## 2A. AI Agent Workflow Rules (3-Stage Pipeline & Token Guard)

You have three specialized tools: `semble_rs` (Search/Deps), `code-review-graph` (Architecture), and `serena` (Review). Follow this strict pipeline to minimize token usage. **If the results of any step sufficiently meet the objective, stop the search immediately and do not proceed to the next step.**

### Stage 1. Discovery & Search (Strictly use `semble_rs`)
- NEVER use `cat`, `read`, or `grep` to find or read code blocks.
- For structural overview, use: `semble_rs search "query" . --outline`
- For targeted line-by-line context, use: `semble_rs search "query" . --compact`
- DO NOT use `--json` unless integrating with other tools, as it consumes 50x more tokens.
- **Open API Docs Skills:** If external specifications (Next.js, Spring Boot, etc.) are required, use dedicated skills or standard browsing. Do not perform broad web scraping.

### Stage 2. Impact Analysis
- Before modifying any file, check its dependencies and ripple effects:
  - `semble_rs deps <file> . --json`
  - `semble_rs impact <file> . --json`
- Use `code-review-graph` only if you need a macro-level visual breakdown of the change scope.
  - **Connection order:** MCP tools first → CLI (`npx caveman-shrink code-review-graph`) fallback.
  - **Maintenance:** Run `code-review-graph update` after major refactoring to maintain analysis accuracy.

### Stage 3. Code Generation & Final Review
- Modify the code based on the accurate snippets found. Use surgical precision (e.g. diff/patch formats); do not rewrite entire files unless necessary.
- Once modifications are done, pass the final diff or code logic to `serena` for prompt refinement and comprehensive code-review validation before committing.
- Use `Serena (LSP)` (e.g., `get_symbols_overview`) for precision navigation to specific symbol definitions and references before editing.

### 🛡️ Efficiency Constraints
- **Gating Principle**: Proceed to the next priority tool only if current results are insufficient. Unnecessary tool calls are forbidden.
- **Selective Reading**: Do not read files over 500 lines in their entirety. Use `semble_rs` outline or Serena's Skeleton analysis first, then read specific function ranges.
- **Trivial Exception**: Stage 1-2 can be skipped for typos or simple comment edits with no logic changes.

### 💡 Workflow Principle
> **"Formulate a hypothesis first (semble_rs for narrow search and impact), verify the location (Skeleton/LSP), and read only when certain (Read). Critical modifications must be re-validated."**

### 🛠️ Advanced Token Utilities & Fallbacks (Token Shield)
| Utility | Role | Execution Method |
| :--- | :--- | :--- |
| **Repomix** | Folder/Scope filtering | `npx repomix --include "path/*"` |
| **Graph** | Impact analysis | `npx caveman-shrink code-review-graph` |

- **CLI Failure Fallback:** If CLI tools fail due to environment issues, fallback to traditional `grep` and `find`. **CRITICAL:** Limit the search range extremely narrowly to minimize token waste.

### 🛡️ MCP Optimization & Token Utilities (Caveman Protocol)
- **Schema Aggression**: Omit verbose descriptions and redundant types during tool schema loading; map only core parameters to save input tokens.
- **Shrink-First**: Large responses (e.g., graph data, file content) MUST undergo semantic compression via `caveman-shrink` proxy before agent interpretation.
- **Token Shield**: Prioritize `caveman-shrink` wrapped tools for all structural and semantic analysis.

---

## 2B. Core Policy Document Defense (Surgical Edit Rules)

Editing core policy documents (like `AGENTS.md`) is considered a **Highest Difficulty and Highest Risk** operation. You MUST adhere to these 5 procedures:
1. **Mandatory History Audit & Sequential Thinking**: Before modifying, you must review `docs/history.md` (or git history) and previous logs. You must use the `[Reasoning]` block to formulate your logic before executing changes.
2. **Zero Context Contamination**: Arbitrary deletion or "clean-up" is strictly prohibited. 100% of existing context must be preserved.
3. **Lazy-Loading Architecture**: Keep `AGENTS.md` lightweight. Move detailed guidelines to the `docs/standards/` directory.
4. **Token-Efficient & Unambiguous**: Use short, decisive English to prevent misinterpretation by other agents.
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

Must read (search `docs/` for keywords matching the target file path/domain):
- high-risk: relevant impact/standard docs
- non-trivial/high-risk coding: `.agents/skills/karpathy-guidelines/SKILL.md`
- cross-module changes: Serena impact analysis (`find_referencing_symbols`) and `code-review-graph` impact tools. Read [code-review-graph-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/code-review-graph-guide.md).
- semantic navigation & editing: [serena-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/serena-guide.md)
- code search & discovery: [semble-operation-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-operation-guide.md), [semble-troubleshooting.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-troubleshooting.md)
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

## 7. Mandatory Lazy-Loaded Policy Triggers

Agents MUST read the required policy file when the following triggers are present in the task or tool intent:

| Trigger | Required policy file |
|---|---|
| `code-review-graph`, knowledge graph, structural analysis, impact radius, blast radius | [code-review-graph-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/code-review-graph-guide.md) |
| `semble_rs`, `semble`, code search, semantic search, vector index | [semble-operation-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-operation-guide.md) and [semble-troubleshooting.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-troubleshooting.md) |
| `serena`, LSP, semantic navigation, symbol analysis | [serena-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/serena-guide.md) |

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

Revert accidental unrelated edits.

---

## 10. Data Safety

- never delete, overwrite, move, or migrate user data (including local DB seed/test data and `.env`) without approval
- use temp paths, backups, or separate outputs for risky operations
- do not hardcode secrets
- do not expose sensitive data in logs/docs/examples/commits
- report partial failures and uncertain state

---

## 11. Verification

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

## 11B. High-Risk & Integrity Guardrails

To prevent model runaways, the following 3 hard guardrails apply universally to all agents:

- **[Sequential Thinking / Stop-and-Think]**: Before modifying any code (e.g., via `replace_file_content`), you MUST output a `[Reasoning]` block in text, explicitly declaring: *"Why am I changing this line, and how is the existing logic preserved?"*
- **[Compile-Gated Verification]**: Before declaring a task complete, you MUST execute the project build command (e.g., `./mvnw verify` or `npm run build`) and attach the successful log containing 'Exit code 0' to your report. Claiming success via text without log proof is a critical violation. For critical architecture or security modifications, a human reviewer MUST cross-check the CI pipeline (e.g., GitHub Actions) to prevent agent log hallucinations.
- **[Atomic Rollback Protocol]**: If your code modification breaks the build, you are granted exactly ONE additional attempt to fix it. If the second attempt fails, you MUST immediately execute `git checkout -- <file>` to rollback to the original state before reporting to the user. Leaving the codebase in a broken state is a critical violation.

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

## 13. Code Review Graph (Structural Analysis)

`code-review-graph` is for dependency/blast-radius checks only. It is not a source of truth. Code, tests, and current docs win.

Refer to the full guide: [code-review-graph-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/code-review-graph-guide.md)

### Tool Usage Standards
1. **Unified MCP Mode**: All agents MUST use the MCP server wrapped with `caveman-shrink` for structural analysis.
2. **Whitelisted Tools**: Only the following core tools are exposed to stay within the 50-tool execution limit:
   - `query_graph_tool`
   - `semantic_search_nodes_tool`
   - `detect_changes_tool`
   - `get_review_context_tool`
   - `get_impact_radius_tool`
   - `get_architecture_overview_tool`
3. **CLI Fallback**: If the MCP server fails, use `npx caveman-shrink code-review-graph <subcommand>` as a fallback (e.g., `detect-changes`).

### Trigger Scenarios
Use `code-review-graph` tools when:
- changing services/managers/stores/workflow state
- changing module/package dependencies
- touching more than 3 modules
- changing backend/frontend contracts
- changing auth, DB, cache, deploy, or other cross-cutting infra
- planning large refactors, renames, or moves

### Operating Principles
1. All commands (CLI fallback) MUST be prefixed with `npx caveman-shrink` for optimized output.
2. Run `detect_changes_tool` or `get_impact_radius_tool` for blast-radius analysis before proposing a plan.
3. Do NOT rely on static reports; query the current graph state.
4. Run `code-review-graph update` (CLI) after significant structural changes to maintain accuracy.

Do not treat tool output as proof that a change is safe. Use it to decide what else to inspect with Serena.

---

## 14. Serena (LSP Semantic Agent)

Serena is the primary tool for semantic code navigation, impact analysis, and precise editing. It leverages the Language Server Protocol (LSP) for 100% accurate symbol resolution.

Refer to the full guide: [serena-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/serena-guide.md)

Operating Principles:
1. **Precision First**: For all **Non-trivial** tasks (excluding typos or text changes), prioritize Serena's symbol analysis (`find_symbol`, `find_referencing_symbols`).
2. **Memory-Driven**: Record architectural decisions or complex business logic changes via `write_memory`.
3. **Zero Assumption**: Use `get_symbols_overview` to understand file structure before reading code.
4. **Surgical Edits**: Use `replace_symbol_body` or `insert_after_symbol` for precise modifications instead of full file overwrites.

Do not rely on outdated reports. Serena provides real-time, IDE-level understanding of the codebase.

---

## 15. semble_rs (Code Search & Discovery)

`semble_rs` is the primary tool for fast, token-efficient code search and impact analysis. It should be used at the beginning of any task to narrow down relevant files, check dependencies, and analyze ripple effects.

Refer to the full guide: [semble-operation-guide.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-operation-guide.md), [semble-troubleshooting.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/semble-troubleshooting.md)

Operating Principles:
1. **Search First**: Before reading files or analyzing structure, use `semble_rs search` with `--outline` or `--compact` to find candidates without consuming excessive tokens. NEVER use `cat`, `read`, or `grep`.
2. **Impact Analysis**: Use `semble_rs deps` and `semble_rs impact` with `--json` to check dependencies before making any modifications.
3. **Token Economy**: ALWAYS prioritize `semble_rs`. Do not use `--json` for search unless integrating with other tools, as it consumes 50x more tokens.

---

## 16. Skills

- Prefer `.agents/skills/`.
- Local skills override global guidance.
- If a task matches a local skill, read it before editing.
- For non-trivial/high-risk coding, read `.agents/skills/karpathy-guidelines/SKILL.md`.
- Follow surgical changes, simplicity, explicit assumptions, and minimal diffs.
- Use/install tools locally only when needed.

---

## 17. Docs and Logs

Guide docs under `docs/standards/`, `docs/workflow/`, and `docs/operations/` are authoritative.

Do not modify guide docs unless the user asks.
If a guide seems wrong/outdated, report:
- path
- issue
- suggested fix

For non-trivial implementation work, write a concise agent log in the relevant backend/frontend log path.
For failed or paused non-trivial work, record useful findings when appropriate.

Use lightweight logs by default.
Use full logs only for high-risk or requested work (e.g., changes to core policy documents like `AGENTS.md` require detailed accountability reports as per Rule 2B).

---

## 18. Handoff

For paused or handed-off work, report:
- changed files
- decisions made
- assumptions
- verification results
- remaining risks or next steps

Keep handoffs short and factual.

---

## 19. Git

- do not run `git add`, `git commit`, or `git push` without approval
- use Conventional Commits when preparing commit messages
- keep commits scoped to the approved task
- report changed files and verification before asking about commit

---

## 20. Response

- start with the core point
- be concise but complete
- prefer one recommended path
- include alternatives only when risk/cost/architecture differs
- report verification clearly
- do not dump long logs unless requested

---


## 21. Golden Rule

Make it correct, safe, small, and understandable.

When unsure:
1. read more repo context
2. reduce scope
3. state assumptions
4. ask before changing high-risk areas