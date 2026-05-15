# AI Agent Workflow Rules (3-Stage Pipeline & Token Guard)

You have three specialized tools: `semble_rs` (Search/Deps), `code-review-graph` (Architecture), and `serena` (Review). Follow this strict pipeline to minimize token usage. **If the results of any step sufficiently meet the objective, stop the search immediately and do not proceed to the next step.**

## Stage 1. Discovery & Search (Strictly use `semble_rs`)
- NEVER use `cat`, `read`, or `grep` to find or read code blocks.
- For structural overview, use: `semble_rs search "query" . --outline`
- For targeted line-by-line context, use: `semble_rs search "query" . --compact`
- DO NOT use `--json` unless integrating with other tools, as it consumes 50x more tokens.
- **Open API Docs Skills:** If external specifications (Next.js, Spring Boot, etc.) are required, use dedicated skills or standard browsing. Do not perform broad web scraping.

## Stage 2. Impact Analysis
- Before modifying any file, check its dependencies and ripple effects:
  - `semble_rs deps <file> . --json`
  - `semble_rs impact <file> . --json`
- Use `code-review-graph` only if you need a macro-level visual breakdown of the change scope.
  - **Connection order:** MCP tools first → CLI (`npx caveman-shrink code-review-graph`) fallback.
  - **Maintenance:** Run `code-review-graph update` after major refactoring to maintain analysis accuracy.

## Stage 3. Code Generation & Final Review
- Modify the code based on the accurate snippets found. Use surgical precision (e.g. diff/patch formats); do not rewrite entire files unless necessary.
- Once modifications are done, pass the final diff or code logic to `serena` for prompt refinement and comprehensive code-review validation before committing.
- Use `Serena (LSP)` (e.g., `get_symbols_overview`) for precision navigation to specific symbol definitions and references before editing.

## 🛡️ Efficiency Constraints
- **Gating Principle**: Proceed to the next priority tool only if current results are insufficient. Unnecessary tool calls are forbidden.
- **Selective Reading**: Do not read files over 500 lines in their entirety. Use `semble_rs` outline or Serena's Skeleton analysis first, then read specific function ranges.
- **Trivial Exception**: Stage 1-2 can be skipped for typos or simple comment edits with no logic changes.

## 💡 Workflow Principle
> **"Formulate a hypothesis first (semble_rs for narrow search and impact), verify the location (Skeleton/LSP), and read only when certain (Read). Critical modifications must be re-validated."**

## 🛠️ Advanced Token Utilities & Fallbacks (Token Shield)
| Utility | Role | Execution Method |
| :--- | :--- | :--- |
| **Repomix** | Folder/Scope filtering | `npx repomix --include "path/*"` |
| **Graph** | Impact analysis | `npx caveman-shrink code-review-graph` |

- **CLI Failure Fallback:** If CLI tools fail due to environment issues, fallback to traditional `grep` and `find`. **CRITICAL:** Limit the search range extremely narrowly to minimize token waste.

## 🛡️ MCP Optimization & Token Utilities (Caveman Protocol)
- **Schema Aggression**: Omit verbose descriptions and redundant types during tool schema loading; map only core parameters to save input tokens.
- **Shrink-First**: Large responses (e.g., graph data, file content) MUST undergo semantic compression via `caveman-shrink` proxy before agent interpretation.
- **Token Shield**: Prioritize `caveman-shrink` wrapped tools for all structural and semantic analysis.
