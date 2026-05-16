# Tooling & Workflow Policy

This document defines how to use MCP, CLI, and advanced workflows in this repository.

## 1. MCP / Serena Rules
- `semble_rs plan` is the first pass when the target is unclear.
- `rg --files` / `rg` is the next pass when more candidate narrowing is still needed.
- `search --outline` or `search --compact` is the direct choice when the target already looks like a symbol.
- `Serena` is the exact-reference tool after the symbol is known.
- `tree --symbols` and `deps` are the Java/Vue structure tools.
- `semble_rs digest` is the default for long or noisy logs.
- `code-review-graph` is mandatory only when the blast radius is broad or unclear.

## 2. Token Economy Utilities (CLI)
Agents SHOULD use these CLI tools to minimize token consumption:
- **Repomix:** Batch file packing for folder-level analysis.
- **semble_rs digest:** Compress build/test logs before review.
- **code-review-graph:** Structural impact analysis via `npx caveman-shrink code-review-graph` when impact needs confirmation. (Summarize to <30 lines).

## 3. Superpower Workflow
For Non-trivial or High-Risk tasks, use the `superpower` skill.
- **Source:** Read `.agents/skills/superpower/SKILL.md`.
- **Process:** Brainstorm -> Plan (in `docs/superpowers/plans/`) -> Implement.
- **Evidence:** Each step requires explicit evidence of success.
