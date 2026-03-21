# Project Documentation Guide

## Purpose

Help humans and agents decide where to start, which document type to follow, and how to resolve conflicts across the `docs/` tree.

---

## Start Here

1. Read `AGENTS.md` first
2. Then follow `docs/standards/`
3. Then follow `docs/workflow/`
4. Use `docs/operations/` for execution HOW TO

---

## Start Rule by Task Type

- New task or unclear task → read `AGENTS.md` first
- Implementation rule or engineering pattern → read `docs/standards/`
- Review, PR, CI, merge, or delivery process → read `docs/workflow/`
- Local setup, deployment, runtime operation, or troubleshooting → read `docs/operations/`

If more than one document is relevant, use the documented priority order and do not let `workflow` or `operations` override a stricter `standards` rule.

---

## Documentation Structure

```text
docs/
  backend/
  frontend/
  standards/
  workflow/
  operations/
```

---

## Folder Guide

### `standards/`
Mandatory engineering rules and reusable implementation patterns.

### `workflow/`
Collaboration, review, CI, and delivery process.

### `operations/`
Execution guides, runtime HOW TO, and runbooks.

---

## Rule of Priority

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

---

## Rule Interpretation

- MUST = mandatory, no exception unless `AGENTS.md` explicitly allows one
- SHOULD = recommended default, can be overridden with a recorded reason
- MAY = optional

---

## Tip

If unsure, start from `AGENTS.md`, then open the most relevant document in `docs/standards/`.
