# Project Structure — stack-app-2025-v1-client

## 1. 문서 목적
이 문서는 팀원이 프로젝트 구조를 5분 안에 이해하고,  
폴더별 역할과 import 규칙을 빠르게 파악할 수 있도록 작성되었습니다.  
설계 원리는 architecture.md 를 참고하세요.

---

## 2. 전체 디렉터리 구조

```
src/
├── core/
│   ├── api/                # Axios, Interceptor, RFC 7807 에러 처리
│   ├── assets/             # 공통 브랜드 자산 (로고 등)
│   ├── i18n/               # 다국어 설정 및 메시지
│   └── store/              # Pinia 상태 관리 (auth, settings 등)
│
├── themes/
│   ├── admin/              # 관리자 L&F (Avalon) 구현체
│   │   ├── assets/         # Avalon 전용 리소스
│   │   ├── styles/         # Avalon 전용 CSS/SCSS
│   │   └── components/
│   │       ├── BaseButton.vue      # L&F 구현체 (PrimeVue)
│   │       └── layouts/
│   │           ├── MainLayout.vue  # (Sidebar 포함)
│   │           └── AuthLayout.vue  # (로그인 전용)
│   │
│   └── landing/            # 랜딩 L&F (Genesis) 구현체
│       ├── assets/         # Genesis 전용 리소스
│       ├── styles/         # Genesis 전용 CSS/SCSS
│       └── components/
│           ├── BaseButton.vue      # L&F 구현체 (PrimeVue)
│           └── layouts/
│               ├── MainLayout.vue  # (상단 헤더 포함)
│               └── SimpleLayout.vue # (약관 페이지용)
│
└── views/
    ├── admin/              # 관리자 기능 (Avalon MainLayout 사용)
    │   ├── dashboard/
    │   │   └── Index.vue
    │   └── user/
    │       └── Management.vue      # /admin/user/management
    │
    ├── public/             # 랜딩 기능 (Genesis MainLayout 사용)
    │   ├── home/
    │   │   └── Index.vue
    │   └── legal/
    │       └── Privacy.vue
    │
    └── auth/               # 인증 기능 (AuthLayout 사용)
        └── LoginPage.vue   # /login
```

---

## 3. 폴더별 역할 요약

| 경로 | 역할 | 비고 |
|------|------|------|
| core/api | Axios 인스턴스, Interceptor, 에러 처리 | RFC 7807 규격 기반 |
| core/store | 전역 Pinia 상태 관리 (Auth, Settings 등) | UI 비의존 상태만 |
| core/i18n | 다국어 메시지 관리 | locale.json 포함 |
| themes/admin | 관리자 테마 (Avalon L&F) 구현 | Look & Feel 전용 |
| themes/landing | 랜딩 테마 (Genesis L&F) 구현 | Look & Feel 전용 |
| views/admin | 관리자 기능 화면 | Admin MainLayout 사용 |
| views/public | 랜딩 기능 화면 | Landing MainLayout 사용 |
| views/auth | 인증 화면 | AuthLayout 사용 |

---

## 4. import 규칙 (팀 공통 규약)

| 구분 | 허용 import | 금지 import |
|------|--------------|--------------|
| core | axios, pinia, vue-router | .vue 파일 |
| themes | PrimeVue, style assets | core/api, core/store |
| views | core, themes | PrimeVue 직접 import |

---

## 5. 권장 네이밍 규칙

- 컴포넌트: PascalCase (예: BaseButton.vue, MainLayout.vue)
- Pinia store: useXxxStore.ts
- 라우트 파일: xxx.router.ts
- 스타일 파일: _variables.scss, main.scss

---

## 6. 라우팅 구조 개요

| 구분 | Layout | 대표 URL | 설명 |
|------|---------|-----------|------|
| Landing | Landing MainLayout | `/`, `/about`, `/legal/privacy` | Genesis 테마 적용 |
| Auth | Admin AuthLayout | `/login`, `/register` | 관리자 전용 로그인 |
| Admin | Admin MainLayout | `/admin/dashboard`, `/admin/users` | Avalon 테마 적용 |

---

## 7. 핵심 설계 원칙 요약 (architecture.md 참조)

- P1. 로직–표현 완전 분리
- P2. 테마 간 완전 독립
- P3. Base 컴포넌트만 사용
- P4. 인증·에러 중앙 관리

---

## 8. 팀 참고 주석

이 문서는 개발자, 디자이너, PM 모두가 프로젝트 구조를 이해하기 위한 가이드입니다.  
core 는 로직, themes 는 시각, views 는 흐름을 담당합니다.  
세 계층의 경계를 넘지 않는 것이 유지보수의 핵심입니다.
