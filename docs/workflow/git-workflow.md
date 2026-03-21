# Git Workflow

## Purpose

Define a safe Git-based workflow so that agent work can be reviewed efficiently and rolled back when necessary.

---

## Rule Interpretation

- MUST = mandatory
- SHOULD = recommended default
- MAY = optional

---

## Core Rules

- Agents MUST NOT work directly on `main` or `master`
- All work SHOULD be performed on a dedicated branch
- Code changes, `agent-log`, required documentation updates, and test results SHOULD move together
- Review SHOULD proceed from summary to risk to detail, not by reading every changed line first

---

## Non-Trivial Change Definition

A change is treated as non-trivial when at least one of the following is true:

- it modifies business logic
- it changes configuration or environment-variable behavior
- it changes API contracts or OpenAPI-relevant behavior
- it impacts database, cache, security, or authentication behavior
- it adds, removes, or upgrades dependencies
- it changes deployment, CI, or runtime operational behavior

If none of the above is true, teams MAY treat the work as trivial according to `AGENTS.md`.

---

## Branch Strategy

### Recommended branch naming

```text
agent/<area>-<task-name>
```

Examples:

```text
agent/backend-user-cache-refactor
agent/frontend-order-list-fix
agent/security-auth-header-hardening
```

### Branch usage rules

- One work branch SHOULD contain one coherent task
- Configuration changes, DB changes, security changes, and dependency upgrades SHOULD be isolated when possible
- Large work SHOULD be split into reviewable branches

---

## Required Deliverables

A completed non-trivial change SHOULD include:

- code changes
- `agent-log`
- source code comments when required
- OpenAPI updates when API contracts change
- test result summary

---

## Agent-log Location and Required Files

Create logs under:

```text
docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/
```

Required files:

- `problem-analysis.md`
- `proposal.md`
- `self-check.md`
- `implementation-plan.md`
- `walkthrough.md`
- `final-report.md`

`agent-log` contents MUST be written in Korean according to `AGENTS.md`.

---

## Commit Rules

### Before commit

Confirm at least the following:

- `agent-log` is complete when required
- test results are recorded
- required code comments are updated
- OpenAPI impact is checked
- rollback feasibility is reviewed

### Recommended commit message format

```text
type: short summary
```

Examples:

```text
feat: improve user query performance and add agent log
fix: remove auth cache and redesign TTL policy
refactor: simplify order query flow and update docs
docs: update workflow and review rules
```

Recommended types:

- `feat`
- `fix`
- `refactor`
- `docs`
- `chore`
- `test`

---

## Review Flow

1. Read `final-report.md` first
2. Check changed files and risk areas
3. Read supporting log documents when risk is high or context is unclear
4. Inspect code details last

High-risk areas include:

- configuration and environment
- security and auth
- cache and Redis
- database
- OpenAPI contracts
- dependencies

---

## Rejection Conditions

A change MAY be rejected when:

- required `agent-log` files are missing
- `final-report.md` is weak or incomplete
- required self-check content is missing
- configuration or dependency changes lack impact analysis
- API changes lack OpenAPI updates
- a high-risk change lacks a rollback plan
