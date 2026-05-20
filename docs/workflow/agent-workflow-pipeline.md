# AI Agent Workflow Rules (Conditional Search & Token Guard)

Use the lightest tool that fits the task. Stop as soon as the current step answers the question.

## Stage 1. Discovery & Search
- If the target is unclear, use `semble_rs plan` first. Use `rg --files` / `rg` only if more candidate narrowing is still needed.
- If the target already looks like a symbol, use `search --outline` or `search --compact` first; use `ast-grep` when the pattern is already known and syntax-aware narrowing helps, then Serena for exact refs.
- Use `tree --symbols` for Java/Vue structure, and `deps` when file dependency context is needed.
- Use `semble_rs digest` for noisy build or test logs.
- Avoid `--json` unless another tool needs it.
- Do not use `cat`, `read`, or `grep` as the first pass for broad discovery.
- If external specifications are required, use the dedicated docs or standard browsing. Do not scrape broadly.

## Stage 2. Impact Analysis
- Add `code-review-graph` only when the blast radius is broad or unclear.
- Use `code-review-graph` when the change may cross modules, change architecture, or affect many callers.
- Use `impact` only as a quick reverse-dependency probe; empty output is inconclusive.
- Connection order: MCP tools first, then CLI (`npx caveman-shrink code-review-graph`) if needed.
- Run `code-review-graph update` after major refactoring to keep analysis accurate.

## Stage 3. Code Generation & Final Review
- Modify code with surgical precision. Do not rewrite whole files unless necessary.
- Use `Serena (LSP)` for symbol definitions, references, and precise edits before writing.
- For Java/Vue files, prefer `deps` before deeper reads when the graph covers the target.
- Re-check the final diff if the scope changed or if the impact step found a wide blast radius.

## Efficiency Constraints
- Proceed to the next tool only if the current result is not enough.
- Do not read large files in full when a narrow symbol or line range is enough.
- **Context Economy Limit**: Strictly forbid reading files > 500 lines entirely. Read specific symbols or 200-line chunks.
- **Model-Specific Discovery (Two-Speed Tooling)**:
  - **Gemini 3.5 Flash**: Utilize low-latency advantage to perform rapid, sequential tool probes (e.g., nesting `tree --symbols` and `search --compact`).
  - **Gemini 3.1 Pro**: Prioritize `semble_rs plan` for semantic grouping and `code-review-graph` to map dependencies.
- Skip Stage 1-2 for typos or simple comment edits with no logic change.

## Workflow Principle
> "Start with the smallest useful search, verify the exact location, and read only what is needed. Re-validate any change with wide impact."

## Token Utilities & Fallbacks
| Utility | Role | Execution Method |
| :--- | :--- | :--- |
| **Repomix** | Folder/Scope filtering | `npx repomix --include "path/*"` |
| **Graph** | Impact analysis | `npx caveman-shrink code-review-graph` |

- If CLI tools fail due to environment issues, fall back to the narrowest possible `rg`/`find` search.
- Keep large outputs compressed before review when possible.
