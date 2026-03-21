# Documentation Structure Standard

## Purpose
Provide a quick reference for the documentation structure defined in `AGENTS.md`.

This document is a simplified navigation guide. If there is any conflict, `AGENTS.md` always takes precedence.

---

## Core Principle

> Organize documents by purpose so that both humans and agents know where to look first.

---

## Structure Overview

```text
docs/
  backend/
  frontend/
  standards/
  workflow/
  operations/
```

---

## Folder Roles

### `docs/standards/`
Mandatory engineering rules and reusable implementation patterns.

### `docs/workflow/`
Collaboration rules, review process, CI rules, and branching strategy.

### `docs/operations/`
Runbooks, setup guides, deployment guides, and troubleshooting HOW TO documents.

---

## Rule of Interpretation

Priority order:

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

---

## Naming Rules

- English only
- lowercase only
- hyphen-separated file names
- purpose-driven names only
- avoid vague names such as `final`, `misc`, `notes`, or `temp`

---

## Note

This document does not define rules independently. It exists to help navigation and onboarding.
