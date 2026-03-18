# Frontend Architecture & Structure — stack-app-2025-v1

## 1. 개요 및 지식 정렬 (Knowledge Alignment)
본 문서는 JHipster 기반의 백엔드 인프라와 결합된 프론트엔드 계층 구조를 정의합니다.
모든 AI 코딩 에이전트(AI Agent)는 작업을 시작하기 전 반드시 본 문서와 `Engineering_Guideline.md`를 최우선 컨텍스트로 로드하고, 모든 제안과 코드 생성에 이를 반영해야 합니다.

## 2. 3계층 구조 및 역할 (Layered Architecture)
- Core (src/core/): API 통신(Axios), 전역 상태(Pinia), i18n, 인증 로직을 담당합니다.
- *에이전트 가드레일*: 이 디렉토리 내에 `.vue` 파일이나 UI 관련 라이브러리 참조를 포함해서는 안 됩니다.
- Themes (src/themes/): 레이아웃, 공통 스타일(SCSS), PrimeVue를 추상화한 Base 컴포넌트의 집합입니다.
- *에이전트 가드레일*: 비즈니스 로직이나 특정 페이지의 데이터 스토어에 직접 접근해서는 안 됩니다.
- Views (src/views/): 사용자가 보는 실제 화면입니다. Core의 기능을 호출하고 Themes의 컴포넌트를 조합합니다.

## 3. 핵심 설계 원칙 (Principles & Guardrails)
- P1. 로직–표현의 엄격한 분리: 모든 비즈니스 로직은 Core에, 시각적 요소는 Themes에 격리합니다.
- P2. 테마 독립성 (Theme Autonomy): Admin(Avalon)과 Landing(Genesis) 테마는 상호 의존성 없이 독립적으로 교체 가능해야 합니다.
- P3. Base 컴포넌트 강제 사용 (Critical Guardrail):
  - 에이전트는 Views 계층에서 PrimeVue 컴포넌트를 직접 `import` 할 수 없습니다. - 반드시 `src/themes/`에서 제공하는 래핑된 Base 컴포넌트(예: `BaseButton`, `BaseInput`)를 사용해야 합니다.
- P4. JHipster 보안 및 에러 표준: JWT 인증과 RFC 7807 기반 에러 응답 처리는 Core 계층에서 통합 수행합니다.

## 4. 상세 디렉터리 구조

```
src/
├── core/               # API, Store, i18n, Router Guards
├── themes/             # Avalon(Admin) & Genesis(Landing) 구현체
│   ├── admin/          # 전용 Assets, Styles, Layouts, Base Components
│   └── landing/        # 전용 Assets, Styles, Layouts, Base Components
└── views/              # admin/, public/, auth/ 기능 화면
```

## 5. 라우팅 및 레이아웃 매핑
- Landing (/): Landing 전용 레이아웃 (Genesis)
- Auth (/login): JHipster 인증 연동용 레이아웃
- Admin (/admin): 관리자용 레이아웃 (Avalon)
