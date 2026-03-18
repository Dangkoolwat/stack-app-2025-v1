# Frontend Engineering Guideline

## 1. 기술 스택 표준
- Framework: Vue 3 (Composition API)
- Build Tool: Vite
- State: Pinia (JHipster 계정 및 설정 연동)
- UI: PrimeVue (Themes 계층을 통한 간접 참조)

## 2. 코딩 및 네이밍 규칙 (Naming Convention)
- 컴포넌트: PascalCase (예: `UserDetail.vue`)
- 스토어: `useXxxStore.ts` (Composition API 방식)
- 상태 관리: UI 전역 공유가 필요한 데이터 외에는 컴포넌트 내 지역 상태(ref)를 우선합니다.

## 3. AI 에이전트 상호작용 규칙 (Engineering Discussion Rules)
에이전트가 아키텍처적 변경이나 복잡한 기능을 구현할 때는 코드를 작성하기 전 다음 항목을 포함한 제안(Proposal)을 먼저 수행해야 합니다:
1. Problem: 현재 코드의 한계점 또는 요구사항 정의.
2. Proposal: 아키텍처 원칙(P1~P4)에 부합하는 해결 설계안.
3. Trade-offs: 테마 독립성과 구현 속도 간의 영향 분석.
4. Risks: JHipster 표준 기능과의 충돌 가능성 또는 사이드 이펙트.

## 4. 에이전트 전용 금지 가이드 (Explicit Bans)
- BAN 1: Views 계층 내 PrimeVue 라이브러리의 직접적인 참조.
- BAN 2: Core 계층에서의 Vue 컴포넌트 또는 DOM 조작 코드 작성.
- BAN 3: 서로 다른 테마(Admin <-> Landing) 간의 컴포넌트나 스타일 직접 참조.

## 5. 리뷰 및 검증 체크리스트
에이전트는 작업 완료 후 스스로 다음 항목을 검토해야 합니다:
- [ ] Core/Themes/Views의 경계가 무너지지 않았는가?
- [ ] 새로운 UI 요소가 Base 컴포넌트로 적절히 추상화되었는가?
- [ ] JHipster 백엔드 인터페이스(API 경로, 보안 가드)와 호환되는가?
- [ ] 에러 발생 시 중앙 에러 처리 규격을 준수하는가?

## 6. 브랜치 및 워크플로우
1. 에이전트의 모든 작업은 안전을 위해 자율적으로 생성된 독립 브랜치에서 수행되는 것을 원칙으로 합니다.
2. 작업 결과물은 '유닛 테스트 -> Lint 체크 -> 빌드 검증' 절차를 통과해야 합니다.
