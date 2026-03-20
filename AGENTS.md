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

### Cache Safety 세부 규칙 (에이전트 오판 방지)

**금지:**
- JPA `@Entity` 클래스를 직접 Redis 캐시 대상으로 사용
  → `User`, `Authority`, `Board` 등을 `Cache.put(key, entity)` 또는
    `@Cacheable` 대상으로 직접 지정하는 행위
  → Hibernate Proxy(@class 불일치) 및 LazyLoading 세션 소멸 문제 로 역직렬화 실패
- `@Cacheable`로 `UserDetails` 또는 `GrantedAuthority` 컨렉션 직접 캐시

**허용 (온바른 방식):**
- 캐시 전용 DTO(`record` 또는 단순 POJO)를 사용
  → `UserAuthCacheDto`, `CommonCodeCacheDto.GroupDto` 등 예시 참조
- OTP, Rate Limiting, 분산 Lock 등 인증 주변 인프라는 Redis 활용 제한 없음
- 상태 변경 시 반드시 명시적 `evict()` 호출, Redis 장애 시 DB fallback 보장

> ⚠️ "인증/로그인 관련은 캐시 금지"라는 표현은
> **JPA 엔티티 직접 캐시 금지**를 의미하는 것으로 해석할 것.
> 인증 주변 데이터의 Redis 활용 자체는 실무 표준입니다.

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
