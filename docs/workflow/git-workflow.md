# Git Workflow

## Purpose
Define a safe Git-based workflow so that agent work can be reviewed efficiently and rolled back when necessary.

---

## Core Rules

- Agents MUST NOT work directly on `main` or `master`
- All work SHOULD be performed on a dedicated branch
- Code changes, `agent-log`, required documentation updates, and test results SHOULD move together
- Review SHOULD proceed from summary to risk to detail, not by reading every changed line first

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

A completed change SHOULD include:

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

- `agent-log` is complete
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

## Change Levels

### Level 1: Minor change

Examples:

- wording updates
- style-only fixes
- obvious typo fixes
- simple UI adjustments

### Level 2: General feature change

Examples:

- service logic updates
- internal API handling improvements
- new screen behavior
- query optimization

### Level 3: High-risk change

Examples:

- configuration changes
- dependency upgrades
- auth or security changes
- cache policy changes
- DB schema changes
- API contract changes

High-risk changes SHOULD include Global Impact Review, rollback planning, required comments, and synchronized documentation.

---

## Rejection Conditions

A change MAY be rejected when:

- `agent-log` is missing
- required files are missing
- `final-report.md` is weak or incomplete
- required self-check content is missing
- configuration or dependency changes lack impact analysis
- API changes lack OpenAPI updates
- a high-risk change lacks a rollback plan
