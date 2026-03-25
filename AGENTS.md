# AGENTS.md

## Language Policy (MANDATORY)

- All shared documentation under `docs/` MUST be written in English.
- All source code comments SHOULD be written in Korean.
- All `agent-log` files MUST be written in Korean.

---

## Documentation Structure Policy (MANDATORY)

All project documentation under `docs/` MUST be organized by purpose.

### Structure

docs/
  backend/
  frontend/
  analysis/
  knowledge/
  standards/
  workflow/
  operations/

### Folder Roles

- standards: mandatory engineering rules (MUST / MUST NOT)
- workflow: collaboration, review, CI, delivery rules
- operations: execution HOW TO (run, deploy, troubleshoot)

### Priority Order

1. AGENTS.md
2. docs/standards/
3. docs/workflow/
4. docs/operations/

---

## Core Execution Flow

All non-trivial work MUST follow:

1. Problem Analysis
2. Proposal
3. Self-Check
4. Plan
5. Implementation
6. Verification

### Exception

For trivial or clearly defined tasks:

Problem → Plan → Implementation → Verification

---

## Interaction Rule (MANDATORY)

When receiving a task:

1. Agents MUST start with a proposed solution direction (hypothesis)
2. Agents SHOULD provide:
   - one recommended solution (primary direction)
   - 1–2 brief alternative options (if relevant)
3. Agents MUST NOT ask "what should I do?" or present only open-ended options
4. Agents MUST ask for confirmation after proposing direction:
   - "Is this the correct direction?"
5. Only after confirmation:
   - proceed with detailed plan and implementation

### Exception (Interaction)

- If the task is trivial or explicitly defined, agents MAY proceed directly to implementation

---

## Git Workflow & Commit Policy (MANDATORY)

All agents MUST follow the **Conventional Commits** standard (v1.0.0).

- **Format**: `<type>(<scope>): <short description>`
- **Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`
- **Body**: MUST include the Agent Name and a link to the corresponding `agent-log` path.
- **Example**: `fix(security): patch transitive vulnerabilities in parent POM (see docs/backend/agent-log/2026-03-25-cve-patch/)`

---

## Knowledge Management (KI) (MANDATORY)

When a task involves complex troubleshooting, non-obvious logic, or critical architectural decisions:

- **Action**: Agents MUST create a **Knowledge Item (KI)**.
- **Location**: `docs/knowledge/YYYY-MM-DD-topic-name.md`
- **Goal**: To prevent recurring issues and shared context across different agents/time.
- **Metadata**: MUST include the same Metadata header as `agent-log` files.

---

## Self-Check (MANDATORY)

- [ ] Architecture compliance
- [ ] No hidden breaking changes
- [ ] Rollback possible
- [ ] Test strategy defined
- [ ] Security impact reviewed
- [ ] Config / dependency impact checked
- [ ] Cache safety checked (if used)
- [ ] OpenAPI impact checked (if API changed)

---

## Agent Log (MANDATORY)

### Location

docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/

### File Metadata Requirement

All files under `agent-log/` MUST include a metadata header (YAML format) at the top:

```markdown
---
agent: [Agent Model Name]
created_at: YYYY-MM-DD (요일)
---
```

Files:

- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

### Content Guide

problem-analysis.md:

- 문제 현상 / 재현 / 원인 / 영향

proposal.md:

- 최소 2개 방안 / 선택 이유 / 리스크

self-check.md:

- 아키텍처 / 보안 / 영향 / 테스트

implementation-plan.md:

- 단계 / 변경 파일 / 테스트

walkthrough.md:

- 구현 흐름 / 핵심 포인트

final-report.md:

- 수행 에이전트 (Agent Model)
- 요약 / 이유 / 영향 / 결과

---

## Cross-Cutting Rules

### Configuration & Env

- MUST follow environment-variables-guideline
- MUST NOT hardcode secrets

### Cache

- MUST follow cache-safety-guideline

### Code Comments

- MUST follow java-class-comment-guideline

---

## Required Documents

### Standards

- docs/standards/environment-variables-guideline.md
- docs/standards/configuration-externalization-guideline.md
- docs/standards/java-class-comment-guideline.md
- docs/standards/cache-safety-guideline.md

### Workflow

- docs/workflow/git-workflow.md
- docs/workflow/pr-review-checklist.md
- docs/workflow/ci-automation-rules.md

### Knowledge

- docs/knowledge/ (Repository for lessons learned and architectural insights)

### Operations

- docs/operations/environment-variables.md

---

## Analysis Repository (MANDATORY)

When an agent is asked to perform a deep analysis (not tied to a specific code-changing task):

- **Location**: `docs/analysis/YYYY-MM-DD-agentName/`
- **Metadata**: MUST follow the same Metadata header rule as `agent-log` files.
- **Goal**: To build a persistent knowledge base of system insights and research findings.

---

## Global Impact Review

Required when changing:

- config
- cache
- security
- dependencies
- API contracts

Must check:

- affected systems
- rollback
- performance
- security

---

## Golden Rule

"Make it correct, safe, and understandable first."
