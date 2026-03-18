# Frontend Architecture & Structure — stack-app-2025-v1

## 1. 개요
본 문서는 JHipster 기반의 백엔드 인프라와 결합된 stack-app-2025-v1-client의 프론트엔드 계층 구조를 정의합니다. Vue 3, Vite, Pinia, PrimeVue를 기본 스택으로 사용하며, 로직과 표현의 완전한 분리를 핵심 목표로 합니다.

## 2. 3계층 구조 및 역할
- Core (src/core/): JHipster와의 통신을 위한 API(Axios), 전역 상태 관리(Pinia), 다국어(i18n), JWT 기반 인증 로직을 담당합니다. UI 컴포넌트(.vue) 포함이 금지됩니다.
- Themes (src/themes/): 관리자(Avalon) 및 랜딩(Genesis) 테마의 시각적 구현체입니다. Layout, 전용 SCSS, 브랜드 자산, 그리고 PrimeVue를 래핑한 Base 컴포넌트를 포함합니다.
- Views (src/views/): 실제 화면을 구성하는 페이지 컴포넌트입니다. Core의 로직을 호출하고 Themes의 레이아웃과 Base 컴포넌트를 조합하여 기능을 구현합니다.

## 3. 핵심 설계 원칙 (P1 ~ P4)
- P1. 로직–표현의 완벽한 분리: 비즈니스 로직(Core)과 시각적 구현(Themes)은 서로의 내부 구현을 직접 참조하지 않습니다.
- P2. 다중 L&F 격리: Admin 테마와 Landing 테마는 완전한 독립 모듈입니다. 한쪽의 변경이 다른 테마나 비즈니스 로직에 영향을 주지 않아야 합니다.
- P3. Base 컴포넌트 강제 사용: 모든 View는 PrimeVue 컴포넌트를 직접 import 하지 않고, 반드시 해당 테마의 Base 컴포넌트(예: BaseButton)를 사용해야 합니다.
- P4. 권한 및 에러 통합 관리: JHipster 표준 보안 규격(JWT)과 RFC 7807 에러 응답 처리는 Core 계층에서 중앙 집중 관리합니다.

## 4. 상세 디렉터리 구조
src/
├── core/               # API, Store, i18n, Router Guards
├── themes/             # Avalon(Admin) & Genesis(Landing) 구현체
│   ├── admin/          # 전용 Assets, Styles, Layouts, Base Components
│   └── landing/        # 전용 Assets, Styles, Layouts, Base Components
└── views/              # admin/, public/, auth/ 기능 화면

## 5. 라우팅 및 레이아웃 매핑
- Landing (/): Genesis 테마의 MainLayout 적용
- Auth (/login): JHipster 인증 연동을 위한 전용 AuthLayout 적용
- Admin (/admin): Avalon 테마의 MainLayout(Sidebar 포함) 적용
