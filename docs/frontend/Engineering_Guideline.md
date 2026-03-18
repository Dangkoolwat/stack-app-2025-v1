# Frontend Engineering Guideline

## 1. 기술 스택
- Framework: Vue 3 (Composition API)
- Build Tool: Vite
- State Management: Pinia (JHipster 연동 계정 정보 및 설정 관리)
- UI Library: PrimeVue (Theme Layer에서 래핑 후 사용)

## 2. 개발 및 네이밍 규칙
- 컴포넌트: PascalCase 사용 (예: BaseButton.vue, MainLayout.vue)
- Store: useXxxStore.ts 형식의 Pinia 스토어 정의
- 라우트: 기능 단위별 xxx.router.ts 파일 분리
- 상태 관리: UI 전용 상태는 컴포넌트 내 ref/reactive를 사용하고, 전역 공유 데이터만 Pinia를 사용합니다.

## 3. 협업 및 제안 규칙 (Discussion Rules)
UI 아키텍처나 구조적 변경 제안 시 다음을 포함해야 합니다:
- **Problem**: 현재 구조의 한계점 또는 문제점
- **Proposal**: 제안하는 구체적인 접근 방식
- **Trade-offs**: 테마 독립성 유지와 개발 편의성 간의 득실 분석
- **Risks**: 기존 JHipster 기능과의 충돌이나 UI 회귀 가능성

## 4. 리뷰 체크리스트
- Core/Themes/Views 계층 간의 Import 규칙을 준수했는가?
- Views 계층에서 PrimeVue를 직접 참조하는 코드가 없는가?
- 테마 간 결합도가 발생하지 않았는가?
- API 호출 및 에러 처리가 Core의 인터셉터 규격에 부합하는가?

## 5. 검증 절차
1. 컴포넌트/로직 테스트 실행
2. ESLint 및 스타일 규칙 준수 확인
3. 빌드 검증 (Theme별 Chunk 분리 확인)
4. JHipster 보안 가드 및 라우트 권한 체크
