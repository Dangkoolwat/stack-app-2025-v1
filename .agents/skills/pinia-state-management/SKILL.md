---
name: pinia-state-management
description: >
  Pinia 3 state management patterns for Vue 3 projects. Use when designing stores,
  managing shared state, implementing actions with API calls, or debugging state
  in JHipster-based Vue 3 applications.
---

# Pinia State Management Expert

Pinia 3 기반 상태 관리 가이드. JHipster + Vue 3 + TypeScript 환경에 최적화.

## 1. Core Standards

- **Composition API Store**: `defineStore` with setup function (JHipster default).
- **TypeScript**: 모든 state, getter, action에 타입 명시.
- **Naming**: `use{Domain}Store` 패턴 (e.g., `useAccountStore`, `useBoardStore`).
- **Scope**: Store는 도메인 단위로 분리. 하나의 Store가 여러 도메인을 관리하지 않는다.

## 2. Store Design Patterns

### 2-1. 기본 Composition Store
```typescript
/**
 * 계정 상태 관리 Store (JHipster 기본 패턴)
 *
 * 관리 상태:
 * - account: 현재 로그인된 사용자 정보
 * - authenticated: 인증 여부
 *
 * 주요 액션:
 * - fetchAccount: 서버에서 계정 정보 로드
 * - logout: 인증 상태 초기화
 */
export const useAccountStore = defineStore('account', () => {
  // 상태 정의 (반응형)
  const account = ref<Account | null>(null);
  const authenticated = ref(false);

  // Getter (계산된 값)
  const isAdmin = computed(() =>
    account.value?.authorities?.includes('ROLE_ADMIN') ?? false
  );

  // 액션 (비동기 API 호출 포함)
  async function fetchAccount(): Promise<void> {
    try {
      const response = await axios.get<Account>('/api/account');
      account.value = response.data;
      authenticated.value = true;
    } catch {
      // 인증 실패 시 상태 초기화 (보안 방어)
      account.value = null;
      authenticated.value = false;
    }
  }

  function logout(): void {
    account.value = null;
    authenticated.value = false;
  }

  return {
    account,
    authenticated,
    isAdmin,
    fetchAccount,
    logout,
  };
});
```

### 2-2. CRUD Store 패턴
```typescript
/**
 * 게시판 CRUD 상태 관리 Store
 *
 * 관리 상태:
 * - items: 게시글 목록
 * - loading: API 호출 진행 상태
 * - error: 마지막 에러 메시지
 *
 * 주의사항:
 * - 페이지네이션은 서버 사이드 (Spring Data Page 응답)
 */
export const useBoardStore = defineStore('board', () => {
  const items = ref<Board[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const totalItems = ref(0);

  async function fetchItems(page = 0, size = 20): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const response = await axios.get<Page<Board>>('/api/boards', {
        params: { page, size },
      });
      items.value = response.data.content;
      totalItems.value = response.data.totalElements;
    } catch (e) {
      // API 에러 시 기존 목록 유지, 에러 메시지만 갱신
      error.value = e instanceof Error ? e.message : '알 수 없는 오류';
    } finally {
      loading.value = false;
    }
  }

  // 낙관적 업데이트: UI 반영 후 서버 동기화
  async function deleteItem(id: number): Promise<void> {
    const backup = [...items.value];
    items.value = items.value.filter(item => item.id !== id);
    try {
      await axios.delete(`/api/boards/${id}`);
    } catch {
      // 실패 시 원복 (낙관적 업데이트 롤백)
      items.value = backup;
    }
  }

  return { items, loading, error, totalItems, fetchItems, deleteItem };
});
```

## 3. Store 간 통신

```typescript
/**
 * 다른 Store 참조 시 액션 내부에서 호출 (순환 의존 방지)
 */
export const useNotificationStore = defineStore('notification', () => {
  async function checkPermission(): Promise<boolean> {
    // 액션 내부에서 다른 Store 참조 (setup 레벨에서 하지 않는다)
    const accountStore = useAccountStore();
    return accountStore.authenticated;
  }

  return { checkPermission };
});
```

## 4. Safety Rules

- **NEVER** Store에서 DOM을 직접 조작하지 않는다.
- **NEVER** 하나의 컴포넌트에서만 사용되는 상태를 Store에 넣지 않는다 → `ref`/`reactive` 사용.
- **MUST** 비동기 액션에서 `try/catch` + loading/error 상태 관리.
- **MUST** 인증 관련 Store(`useAccountStore`)는 앱 초기화 시 `fetchAccount()` 호출.
- **Prefer** `shallowRef`를 대용량 목록에 사용하여 불필요한 deep reactivity 방지.

## 5. Testing (with Vitest)

```typescript
import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach } from 'vitest';
import { useAccountStore } from '@/stores/account';

describe('AccountStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('초기 상태는 미인증이다', () => {
    const store = useAccountStore();
    expect(store.authenticated).toBe(false);
    expect(store.account).toBeNull();
  });
});
```

## 6. JHipster-Specific

- JHipster가 생성하는 기본 Store: `account-store.ts`, `translation-store.ts`
- Store 위치: `src/main/webapp/app/stores/` (JHipster convention)
- Devtools: Vue Devtools의 Pinia 탭에서 상태 디버깅 가능

## 7. Korean Comment Rule

Store의 상태와 액션에는 한글 주석으로 역할과 부수 효과를 설명한다:
```typescript
// 로그아웃 시 모든 인증 상태 초기화 (보안: 토큰 잔류 방지)
function logout(): void { ... }
```
