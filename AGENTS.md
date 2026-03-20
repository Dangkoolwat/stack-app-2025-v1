# AGENTS.md

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

---

## Recommended Defaults

- Choose simplest working solution
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

docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/

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
- All contents MUST be written in Korean (concise)

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

## Agent Log Writing Rules

- Agent log documents MUST be written in Korean
- Keep descriptions concise
- Focus on WHY and impact

---

## Code Change Documentation Rules

- Important changes MUST be documented in code comments

Example:

/**
 * Change History:
 *  - 2026-03-20: C-1 Refactor → Removed auth cache
 */

- Explain WHY for logic changes

---

## API Documentation Rules

- MUST follow OpenAPI specification
- Swagger compatible
- Do NOT duplicate full spec in agent-log

---

## Responsibility Separation

- agent-log → WHY
- code comments → WHAT & HOW
- OpenAPI → CONTRACT

---

## Process Documents

All work must also follow:

- docs/process/git-workflow.md
- docs/process/pr-review-checklist.md
- docs/process/ci-automation-rules.md
- docs/process/github-actions.md
---

## Golden Rule

"Make it correct, safe, and understandable first. Then optimize."
