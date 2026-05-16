# semble_rs Operation Guide

This guide covers `semble_rs` operational order only. Installation and environment-specific fixes live in [semble_rs Troubleshooting](docs/standards/semble-troubleshooting.md).

---

## 1. Operational Protocol

Agents MUST follow the search order defined in `AGENTS.md` (Section 2A).

1. **Unclear target**: Use `semble_rs plan` first. Use `rg --files` / `rg` only if more candidate narrowing is still needed.
2. **Known symbol target**: Use `search --outline` or `search --compact` first, then Serena for exact refs.
3. **Semantic narrowing**: Use `mcp_semble_search` when text search is too broad.
4. **Java/Vue structure map**: Use `tree --symbols` to see top-level files and symbols, and `deps` to confirm direct imports and defined symbols.
5. **Vue fallback**: If `deps` does not cover a Vue file, fall back to `tree --symbols` plus `search --outline` or Serena.
6. **Broad impact**: Add `code-review-graph` only when the blast radius is broad or unclear.
7. **Final Read**: Read the actual file content only after the target location is confirmed.

---

## 2. Supported Discovery

- `tree --symbols` is valid for Java/Vue source in this repo.
- `deps` returns direct imports, defined symbols, and reverse dependency context for a file when the graph covers it.
- `search --outline` is useful when a smaller signature-only result is enough.
- `impact` can be used as a quick reverse-dependency probe, but empty output is inconclusive.
- `digest` is the default for long or noisy build and test logs.

---

## 3. Troubleshooting Boundary

- Installation, MCP setup, and environment-specific fixes live in [semble_rs Troubleshooting](docs/standards/semble-troubleshooting.md).
- If `mcp_semble_search` is unavailable, fall back to `rg` and Serena.
- Keep this guide short. Do not put environment-specific patch steps here.
