# Workflow Templates

## Purpose

This folder contains reusable prompt templates for AI-assisted task design and execution.

These files are support tools for workflow.
They are NOT rule documents.

If any instruction conflicts with project rules, follow:

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

---

## Files

### `thinking-prompt.md`
Use this template with a stronger reasoning model to design a task before handing execution to another model.

### `execution-prompt.md`
Use this template with an execution-oriented model to perform the task according to `AGENTS.md` and project documents.

---

## Recommended Usage

1. Use `thinking-prompt.md` to generate a structured task design
2. Review and refine the proposed direction
3. Pass the result into `execution-prompt.md`
4. Let the execution model follow the approved direction

---

## Note

These templates are backup support materials for consistent agent behavior.
