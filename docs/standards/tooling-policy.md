# Tooling & Workflow Policy

This document defines how to use MCP, CLI, and advanced workflows in this repository.

## 1. MCP / Serena Rules
- `Semble` is the 1st-priority tool for narrow conceptual discovery.
- `Serena` is MANDATORY for deep semantic analysis and symbolic editing.
- `code-review-graph` is mandatory for Non-trivial+ structural analysis (via CLI).

## 2. Token Economy Utilities (CLI)
Agents SHOULD use these CLI tools to minimize token consumption:
- **Repomix:** Batch file packing for folder-level analysis.
- **LLMLingua:** Compressing long logs/discussions for permanent docs.
- **code-review-graph:** Structural impact analysis via `npx caveman-shrink code-review-graph`. (Summarize to <30 lines).

## 3. Superpower Workflow
For Non-trivial or High-Risk tasks, use the `superpower` skill.
- **Source:** Read `.agents/skills/superpower/SKILL.md`.
- **Process:** Brainstorm -> Plan (in `docs/superpowers/plans/`) -> Implement.
- **Evidence:** Each step requires explicit evidence of success.
