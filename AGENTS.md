# AGENTS.md

## Language Policy (MANDATORY)

- All shared documentation under `docs/` MUST be written in English.
- All source code comments SHOULD be written in Korean.
- All `agent-log` files MUST be written in Korean.

---

## Documentation Structure Policy (MANDATORY)

All project documentation under `docs/` MUST be organized by **purpose**, not by temporary topic.

### Recommended top-level structure

```text
docs/
  backend/          # backend architecture, engineering rules, backend agent-log
  frontend/         # frontend architecture, engineering rules, frontend agent-log
  standards/        # mandatory standards and implementation patterns
  workflow/         # collaboration, review, CI, branching, delivery workflow
  operations/       # practical runbooks, setup guides, deployment and environment HOW TO
```

### Folder purpose definitions

#### `docs/standards/`
Use this folder for documents that define REQUIRED implementation standards.

Examples:
- environment-variables-guideline.md
- configuration-externalization-guideline.md
- java-class-comment-guideline.md
- properties-template.java.md
- configuration-template.java.md

Rules:
- These documents define MUST / MUST NOT / SHOULD rules.
- Agents MUST follow these documents directly.
- These documents should be stable, reusable, and project-wide.

#### `docs/workflow/`
Use this folder for collaboration and delivery workflow documents.

Examples:
- git-workflow.md
- pr-review-checklist.md
- ci-automation-rules.md
- github-actions.md

Rules:
- These documents define branch strategy, review flow, CI expectations, and approval rules.
- These documents govern how work is proposed, reviewed, and merged.
- These documents are shared by both humans and agents.

#### `docs/operations/`
Use this folder for practical execution guides and operational HOW TO documents.

Examples:
- environment-variables.md
- local-setup.md
- deployment.md
- troubleshooting.md

Rules:
- These documents explain how to run, configure, deploy, or troubleshoot the system.
- These documents MUST NOT override standards documents.
- If there is any conflict, `docs/standards/` takes precedence.

### Naming rules

- Use lowercase English file names.
- Use hyphen-separated names.
- File names should describe purpose, not author intent.
- Avoid vague names such as `guide-final.md`, `notes.md`, `misc.md`, `temp.md`.

Good examples:
- `environment-variables-guideline.md`
- `configuration-externalization-guideline.md`
- `git-workflow.md`

Bad examples:
- `final-guide.md`
- `process-notes.md`
- `new-doc.md`

### Priority order

When multiple documents are relevant, agents MUST use this order:

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

### Documentation ownership rule

- Architecture and engineering rules remain under `docs/backend/` and `docs/frontend/`.
- Cross-cutting rules MUST live outside backend/frontend if they apply to both sides.
- Root-level `docs/` files SHOULD be avoided unless they are index files such as `README.md`.

---

## Core Execution Flow (Mandatory)

All non-trivial work MUST follow:

1. Problem Analysis
2. Proposal
3. Self-Check Gate
4. Implementation Plan
5. Implementation
6. Verification & Documentation

---

## 1. Problem Analysis (REQUIRED)

- Symptoms
- Reproduction steps
- Suspected root cause
- Impact scope

Do NOT jump to solution before defining the problem.

---

## 2. Proposal

- At least 2 alternatives
- Trade-offs (Maintainability, Performance, Security)
- Risks
- Decision rationale

---

## 3. Self-Check Gate (MANDATORY)

- [ ] Architecture compliance
- [ ] No hidden breaking changes
- [ ] Rollback possible
- [ ] Test strategy defined
- [ ] Security impact reviewed
- [ ] Config / dependency impact checked

---

## 4. Implementation Plan

- Steps
- Files to change
- Test plan
- Documentation updates

---

## Maintainability First

- Prefer small, safe, reversible changes
- Prefer explicit code over abstraction
- Readability over cleverness

---

## Security by Default

- Validate all inputs
- Backend is source of truth for auth
- Never log sensitive data
- Review cache safety

### Cache Safety Rules

All caching must follow:
- docs/standards/cache-safety-guideline.md

---

## Recommended Defaults

- Choose the simplest working solution
- Prefer consistency over optimization
- Follow existing patterns first

---

## Global Impact Review (REQUIRED)

Required for:
- Config changes
- Dependencies
- Serialization
- Cache / Redis
- Security / Auth
- API contracts

Must include:
- Affected systems
- Compatibility
- Data/cache impact
- Performance impact
- Security impact
- Rollback plan
- Test plan

---

## Agent Log File Structure (MANDATORY)

```text
docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/
```

Required files:

- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

Rules:
- All files MUST be created
- File names MUST match exactly
- All contents MUST be written in Korean
- Keep entries concise and impact-focused

---

## Required Content per File

### problem-analysis.md
- 문제 현상
- 재현 방법
- 추정 원인
- 영향 범위

### proposal.md
- 해결 방안 2개 이상
- 선택 이유
- Trade-offs
- 리스크

### self-check.md
- 아키텍처 위반 여부
- Breaking change 여부
- 보안 영향
- 설정/의존성 영향
- 테스트 계획

### implementation-plan.md
- 작업 단계
- 수정 파일 목록
- 테스트 계획

### walkthrough.md
- 구현 흐름 요약
- 주요 코드 변경 포인트

### final-report.md
- 변경 요약
- 변경 이유
- 영향 범위
- 테스트 결과
- 후속 작업

---

## Code Change Documentation Rules

- Important changes MUST be documented in source code comments.
- Source code comments SHOULD be written in Korean.
- Configuration, security, cache, external integration, and shared policy classes MUST follow:
  - `docs/standards/java-class-comment-guideline.md`

---

## API Documentation Rules

- MUST follow OpenAPI specification
- MUST remain Swagger compatible
- Do NOT duplicate full API specs in `agent-log`

---

## Responsibility Separation

- `agent-log` → WHY (Korean)
- source code comments → WHAT / HOW / cautions (Korean recommended)
- OpenAPI → CONTRACT
- standards docs → REQUIRED engineering rules (English)
- workflow docs → review and delivery rules (English)
- operations docs → execution HOW TO (English)

---

## Required Cross-Cutting Documents

All agents MUST follow:

### Standards
- `docs/standards/environment-variables-guideline.md`
- `docs/standards/configuration-externalization-guideline.md`
- `docs/standards/java-class-comment-guideline.md`
- `docs/standards/properties-template.java.md`
- `docs/standards/configuration-template.java.md`

### Workflow
- `docs/workflow/git-workflow.md`
- `docs/workflow/pr-review-checklist.md`
- `docs/workflow/ci-automation-rules.md`
- `docs/workflow/github-actions.md`

### Operations
- `docs/operations/environment-variables.md`

---

## Environment Variable Policy (MANDATORY)

- NEVER hardcode secrets
- ALWAYS use environment variables
- ALWAYS update `.env.sample` when adding variables
- Production MUST use OS environment variables or Secret Manager
- Trigger-based rules MUST follow:
  - `docs/standards/environment-variables-guideline.md`

---

## Configuration Externalization Policy (MANDATORY)

All configuration changes MUST follow:

- `docs/standards/configuration-externalization-guideline.md`
- `docs/standards/properties-template.java.md`
- `docs/standards/configuration-template.java.md`

This policy MUST be applied when:
- adding new configuration
- removing hardcoded values
- handling environment-specific values
- handling security-sensitive values
- handling operationally tunable values such as timeout, pool size, TTL, or endpoint URLs

---

## Golden Rule

"Make it correct, safe, and understandable first. Then optimize."
