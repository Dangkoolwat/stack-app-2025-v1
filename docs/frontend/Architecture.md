# Architecture Principles — stack-app-2025-v1-client

## 개요
본 문서는 stack-app-2025-v1-client 의 프론트엔드 구조 원칙을 정의합니다.  
이 프로젝트는 Vue 3 + Vite + Pinia + PrimeVue 기반의 SPA 애플리케이션으로,  
로직과 표현의 완전한 분리, 테마 단위의 독립적 Look & Feel 교체,  
Base 컴포넌트 추상화, 권한 및 에러의 중앙 집중 관리를 핵심 목표로 합니다.

---

## 프로젝트 디렉터리 구조

```
src/
├── core/
│   ├── api/                # Axios, Interceptor
│   ├── assets/             # 공통 브랜드 자산 (로고 등)
│   ├── i18n/               # 다국어 설정/메시지
│   └── store/              # Pinia (auth.js 등)
│
├── themes/
│   ├── admin/              # 관리자 L&F (Avalon) 구현체
│   │   ├── assets/         # Avalon 전용 리소스
│   │   ├── styles/         # Avalon 전용 CSS/SCSS
│   │   └── components/
│   │       ├── BaseButton.vue  # L&F 구현체 (PrimeVue)
│   │       └── layouts/        # 관리자용 레이아웃 분리
│   │           ├── MainLayout.vue  # (Sidebar 포함)
│   │           └── AuthLayout.vue  # (로그인 전용)
│   │
│   └── landing/            # 랜딩 L&F (Genesis) 구현체
│       ├── assets/         # Genesis 전용 리소스
│       ├── styles/         # Genesis 전용 CSS/SCSS
│       └── components/
│           ├── BaseButton.vue  # L&F 구현체 (PrimeVue)
│           └── layouts/        # 랜딩 페이지용 레이아웃 분리
│               ├── MainLayout.vue  # (상단 헤더 포함)
│               └── SimpleLayout.vue # (약관 페이지용)
│
└── views/
    ├── admin/                  # 관리자 기능 (MainLayout 사용)
    │   ├── dashboard/
    │   └── user/
    │       └── Management.vue  # /admin/user/management
    │
    ├── public/                 # 랜딩 기능 (LandingLayout 사용)
    │   ├── home/
    │   └── legal/
    │
    └── auth/                   # 인증 기능 (AuthLayout 사용)
        └── LoginPage.vue       # /login
```

---

## P1. 로직–표현의 완벽한 분리 (Logic–Presentation Isolation)

원칙
- src/core/ 와 src/views/ 는 비즈니스 로직만 담당합니다.
- src/themes/ 는 시각적 구현(Look & Feel)만 담당합니다.
- core 는 themes 를 모르며, themes 는 core 의 내부 로직을 직접 접근하지 않습니다.

Do
- core: API, Store, Router, i18n 정의
- views: 화면 흐름(페이지 로직) 처리
- themes: 스타일, Layout, Base UI 구성

Don’t
- core 에서 .vue 파일 import 금지
- themes 에서 API, Store import 금지
- views 에서 PrimeVue 컴포넌트 직접 import 금지

---

## P2. 다중 L&F 격리 (Theme Autonomy)

원칙
- themes/admin (Avalon) 과 themes/landing (Genesis) 은 완전한 독립 모듈입니다.
- 한쪽 테마 교체 시, 다른 쪽 및 비즈니스 로직에 영향이 없어야 합니다.

구성  
| 테마 | 역할 | L&F | 경로 |
|------|------|-----|------|
| Admin | 관리자 UI | Avalon Theme | /src/themes/admin |
| Landing | 랜딩/퍼블릭 UI | Genesis Theme | /src/themes/landing |

특징
- 각 테마는 전용 Layout, Style, Asset, Base 컴포넌트를 가집니다.
- SCSS 네임스페이스 분리 (.avalon-theme, .genesis-theme)
- 빌드 시 별도 Chunk로 분리 (admin-theme.js, landing-theme.js)

---

## P3. Base 컴포넌트 강제 사용 (Theme Component Abstraction)

원칙
- 모든 src/views/ 의 View 컴포넌트는 PrimeVue 컴포넌트를 직접 import 하지 않습니다.
- 반드시 해당 테마의 Base Wrapper 컴포넌트를 사용해야 합니다.

예시
```vue
<!-- themes/admin/components/BaseButton.vue -->
<template>
  <Button class="p-button-success" v-bind="$attrs"><slot /></Button>
</template>

<script setup>
import Button from 'primevue/button'
</script>
```

```vue
<!-- views/admin/dashboard/Index.vue -->
<template>
  <BaseButton label="Save" @click="save" />
</template>

<script setup>
import BaseButton from '@admin/components/BaseButton.vue'
</script>
```

이점
- 테마 교체 시 View 코드 변경 불필요
- UI 일관성 유지
- PrimeVue 교체(예: Naive UI)도 Base 레벨 수정으로만 가능

---

## P4. 권한 및 에러 통합 (Unified Auth & Error Layer)

원칙
- JWT 인증, ROLE 기반 권한, RFC 7807 에러 응답은 core 레벨에서 통합 관리합니다.
- View 는 core 의 추상화된 API 만 호출하고, 실제 에러 UI 표현은 테마의 컴포넌트에 위임합니다.

구조
```
core/
 ├── api/http.ts           # Axios 인터셉터, RFC7807 처리
 ├── store/useAuthStore.ts # JWT/권한 관리
 └── router/guards.ts      # beforeEach 인증 가드
themes/
 └── components/ErrorToast.vue  # 에러 시각 표현
```

RFC 7807 에러 처리 예시
```ts
// core/api/http.ts
api.interceptors.response.use(
  res => res,
  error => {
    const problem = error.response?.data
    if (problem?.type?.includes('rfc7807')) {
      // 전역 에러 스토어 혹은 이벤트 버스로 전달
      console.error('RFC7807 Error:', problem)
    }
    return Promise.reject(error)
  }
)
```

---

## 보조 규칙 (Supporting Rules)

| 코드 | 원칙 | 목적 |
|------|------|------|
| P5 | 라우터 그룹 격리 | /, /auth, /admin 라우트 트리 구분 |
| P6 | SCSS 네임스페이스 | CSS 충돌 방지 (.avalon-theme, .genesis-theme) |
| P7 | ESLint 규칙 | Cross-theme import 방지 |
| P8 | Build Chunk 분리 | 테마별 캐시, 배포 분리 |
| P9 | Testing Stub | Base 컴포넌트 단위 테스트 기준 통일 |

---

## 프로젝트 계층 요약
```
src/
├── core/      # API, Store, Router, Auth, i18n (로직)
├── themes/    # Avalon & Genesis Look & Feel (표현)
└── views/     # 페이지 로직 (theme Layout을 통해 렌더
