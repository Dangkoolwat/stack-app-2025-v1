# Java & Vue 3 Korean Comment Guideline (통합 한글 주석 가이드라인)

## Purpose

Define where comments are required and how they should be written across both **Java (Spring Boot)** and **Vue 3 (TypeScript)** so that developers and agents can quickly understand intent, risk, and maintenance boundaries. All source code comments MUST be written in Korean.

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
- Source code comments MUST be written in Korean according to `AGENTS.md` to ensure deep contextual understanding for the team.

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

---

## Part 2: Vue 3 / TypeScript Korean Comment Guideline (프론트엔드)

### Scope

#### MUST have a component/module-level comment

- `*Store` (Pinia stores)
- `*Composable` (shared composables used by >1 component)
- Router guards and middleware
- API client modules (`*Service.ts`, `*Api.ts`)
- Complex page components with business logic
- Shared utility modules (`utils/`, `shared/`)

#### SHOULD have a component-level comment

- Multi-step form components
- Components with complex reactive state
- Components integrating external libraries

#### SHOULD NOT have a component-level comment

- Simple presentational components
- Layout wrappers
- Icon/asset components

---

### Standard Template: Vue 3 SFC

```vue
<script setup lang="ts">
/**
 * [한글 요약] 컴포넌트의 핵심 역할과 존재 이유를 한 줄로 설명합니다.
 *
 * 역할:
 * - 책임 1 (무엇을 하는가)
 * - 책임 2 (어떤 문제를 해결하는가)
 *
 * 주의사항:
 * - 의존하는 Store 또는 Composable
 * - 성능 / 렌더링 관련 제약 사항
 */
</script>
```

### Standard Template: Pinia Store

```typescript
/**
 * [한글 요약] Store의 핵심 역할과 관리하는 상태를 한 줄로 설명합니다.
 *
 * 관리 상태:
 * - state 1: 설명
 * - state 2: 설명
 *
 * 주요 액션:
 * - action 1: 트리거 조건과 부수 효과
 *
 * 주의사항:
 * - 캐시 / 동기화 관련 제약
 */
export const useExampleStore = defineStore('example', () => {
  // 구현부
});
```

### Standard Template: Composable

```typescript
/**
 * [한글 요약] Composable의 핵심 역할을 한 줄로 설명합니다.
 *
 * @param paramName - 매개변수 설명 (한글)
 * @returns 반환값 설명 (한글)
 *
 * 사용처:
 * - 컴포넌트 A, 컴포넌트 B에서 공유
 */
export function useExample(paramName: string) {
  // 구현부
}
```

---

### Inline Comment Rules (공통)

- **Safety guards:** 안전 장치나 예외 처리에는 반드시 한 줄 한글 주석으로 *이유*를 설명합니다.
  ```typescript
  // 인증 토큰 만료 시 자동 갱신 방지 (무한 루프 차단)
  if (isRefreshing) return;
  ```
- **Business logic:** 비즈니스 로직의 의도를 코드 옆에 간략히 주석합니다.
  ```java
  // 관리자 권한이 아닌 경우, 본인 게시물만 수정 가능
  if (!isAdmin && !isOwner) throw new ForbiddenException();
  ```
- **TODO/FIXME:** 한글로 작성하되, 날짜와 작업 식별자를 포함합니다.
  ```typescript
  // TODO(2026-05-15): 페이지네이션 무한스크롤로 전환 예정
  ```

