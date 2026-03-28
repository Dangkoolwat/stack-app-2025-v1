---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: en
---

# Document Authority Boundaries

## Summary

This repository benefits from having several document types, but agent efficiency drops when reference notes, task history, and policy documents all appear equally authoritative.

## Decision

Use the following rule consistently:

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

Everything outside that chain is supporting context unless a higher-priority document explicitly promotes it.

## Practical Boundaries

- `docs/knowledge/` explains rationale and lessons learned.
- `docs/*/agent-log/` preserves task traceability and evidence.
- `docs/backend/` and `docs/frontend/` help contributors understand local architecture and implementation patterns.

These documents are useful, but they must not silently redefine repository-wide policy.

## Common Failure Pattern

Confusion appears when a reference note contains stronger wording than an authoritative guide. Agents may then follow the closest or most detailed document instead of the highest-priority one.

## Preventive Rule

Reference documents should include a short role statement near the top so agents can quickly classify them as:

- authoritative rule
- operational procedure
- supporting reference
- historical log

## Expected Benefit

This keeps the documentation set scalable without forcing every useful explanation into `AGENTS.md`, while still protecting agents from priority confusion.
