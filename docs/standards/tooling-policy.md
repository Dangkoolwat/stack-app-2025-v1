# Tooling & Workflow Policy

This document defines how to use MCP, CLI, and advanced workflows in this repository.

## 1. MCP / Serena Rules
- `rg --files` / `rg` is the first pass when the target is unclear.
- `Serena` is the direct choice when the target already looks like a symbol.
- `semble_rs` is the semantic discovery option when text search is too broad.
- `code-review-graph` is mandatory only when the blast radius is broad or unclear.

## 2. Token Economy Utilities (CLI)
Agents SHOULD use these CLI tools to minimize token consumption:
- **Repomix:** Batch file packing for folder-level analysis.
- **code-review-graph:** Structural impact analysis via `npx caveman-shrink code-review-graph` when impact needs confirmation. (Summarize to <30 lines).

## 3. Superpower Workflow
For Non-trivial or High-Risk tasks, use the `superpower` skill.
- **Source:** Read `.agents/skills/superpower/SKILL.md`.
- **Process:** Brainstorm -> Plan (in `docs/superpowers/plans/`) -> Implement.
- **Evidence:** Each step requires explicit evidence of success.
