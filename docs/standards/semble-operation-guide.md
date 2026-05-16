# Semble Operation Guide

This guide covers Semble operational order only. Installation and environment-specific fixes live in [Semble Troubleshooting](docs/standards/semble-troubleshooting.md).

---

## 1. Operational Protocol

Agents MUST follow the search order defined in `AGENTS.md` (Section 2A).

1. **Unclear target**: Use `rg --files` / `rg` first to narrow candidates.
2. **Known symbol target**: Use Serena or the closest symbol tool directly.
3. **Semantic narrowing**: Use `mcp_semble_search` when text search is too broad.
4. **Broad impact**: Add `code-review-graph` only when the blast radius is broad or unclear.
5. **Final Read**: Read the actual file content only after the target location is confirmed.
