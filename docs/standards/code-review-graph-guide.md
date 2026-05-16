# Code-Review-Graph Unified MCP Proxy Guide (v2.1)

This guide defines the standard procedure for using `code-review-graph` via the Unified MCP Proxy to maximize token efficiency and structural analysis accuracy.

## 1. Architecture: Unified MCP Mode
To optimize token consumption while maintaining high performance, `code-review-graph` is operated as an MCP server wrapped with the `caveman-shrink` proxy.

- **Proxy:** `caveman-shrink` (compresses output for LLM efficiency).
- **Core Strategy:** Whitelisting core tools to stay within the 50-tool execution limit.

## 2. Whitelisted Core Tools (Power Six)
Only the following tools are exposed via MCP. Agents should prioritize these for structural analysis:

1. `query_graph_tool`: Search the graph using predefined patterns (callers, callees, etc.).
2. `semantic_search_nodes_tool`: Find code entities by name or semantic similarity.
3. `detect_changes_tool`: Analyze changes between git refs and map to affected functions.
4. `get_review_context_tool`: Generate focused, token-efficient review context.
5. `get_impact_radius_tool`: Analyze the blast radius of changed files.
6. `get_architecture_overview_tool`: Generate high-level community structure view.

## 3. CLI Fallback Protocol
If the MCP server is unavailable or fails, agents MUST fallback to the CLI using the `caveman-shrink` wrapper.

### Standard CLI Prefix
```bash
npx caveman-shrink code-review-graph [subcommand]
```

### Tool-to-CLI Mapping
| MCP Tool Name | CLI Fallback Subcommand |
|---|---|
| `query_graph_tool` | `query-graph` |
| `semantic_search_nodes_tool` | `semantic-search` |
| `detect_changes_tool` | `detect-changes` |
| `get_review_context_tool` | `get-review-context` |
| `get_impact_radius_tool` | `get-impact-radius` |
| `get_architecture_overview_tool` | `get-architecture-overview` |

## 4. Maintenance Commands
Periodic maintenance ensures graph accuracy. These do not require the `caveman-shrink` wrapper.

- **Status Check:** `code-review-graph status`
- **Incremental Update:** `code-review-graph update` (Run after major refactors)
- **Full Rebuild:** `code-review-graph build` (Run if the graph is corrupted)

## 5. Mandatory Policy
1. **Read First:** Agents MUST read this guide before using any `code-review-graph` tools.
2. **Surgical Analysis:** Use `detect_changes_tool` or `get_impact_radius_tool` before proposing any plan when the change may cross modules, change architecture, or has an unclear blast radius.
3. **Verification:** Re-run impact analysis after implementation to verify 0% regression in unrelated modules.
