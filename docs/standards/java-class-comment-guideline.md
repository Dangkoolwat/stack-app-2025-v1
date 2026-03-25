# Java Class Comment Guideline

## Purpose

Define where class-level comments are required and how they should be written so that developers and agents can quickly understand intent, risk, and maintenance boundaries.

---

## Scope

### MUST have a class-level comment

- `*Configuration`
- `*Properties`
- Security-related classes
- Cache-related classes
- External client or adapter classes
- Core business services with non-trivial logic
- Shared policy classes

### SHOULD have a class-level comment

- Complex services
- Scheduler or batch classes
- Event listeners
- Strategy or factory classes

### SHOULD NOT have a class-level comment

- DTOs
- Entities
- Enums
- Repositories
- Simple CRUD services with obvious behavior

---

## Required Writing Principles

- Focus on intent, not line-by-line code explanation
- State when future changes are expected
- Mention only meaningful risks or cautions
- Keep change history short and current
- **Source code comments MUST be written in Korean** according to `AGENTS.md` to ensure deep contextual understanding for the team.

---

## Standard Template

```java
/**
 * [한글 요약] 클래스의 핵심 역할과 존재 이유를 한 줄로 설명합니다.
 *
 * 역할:
 * - 책임 1 (무엇을 하는가)
 * - 책임 2 (어떤 문제를 해결하는가)
 *
 * 에이전트 작업 가이드:
 * - 언제 수정해야 하는가 (트리거)
 * - 같이 확인할 설정/문서 (의존성)
 *
 * 주의사항:
 * - 영향 범위 (Side Effects)
 * - 보안 / 성능 / 캐시 관련 제약 사항
 *
 * 변경 이력:
 * - YYYY-MM-DD: [Task] 주요 변경 내용 요약
 */
```

---

## Forbidden Patterns

- Repeating the same generic comment on every class
- Restating what the code already says without adding intent
- Leaving outdated comments after behavior changes

---

## One-Line Principle

Leave context on important classes, and stay quiet on simple classes.
