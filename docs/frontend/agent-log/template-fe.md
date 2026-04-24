# Frontend Agent Work Log Template

## Purpose
이 템플릿은 프론트엔드(Vue 3) 에이전트가 수행한 작업 내용을 기록하기 위한 표준 형식입니다.
모든 비자명한 작업은 아래 경로에 새로운 폴더를 생성하고 이 템플릿을 작성해야 합니다.

경로: docs/frontend/agent-log/YYYY-MM-DD-task-name/

표준 작업은 짧게 기록해도 됩니다. 나중에 검토할 핵심 정보만 남기면 됩니다.

---

## Date
YYYY-MM-DD

---

## Agent
에이전트 이름 또는 식별자 (예: Gemini, Claude 등)

---

## Task Title
작업의 핵심 내용을 담은 짧은 제목

---

## Goal
- 해결하려는 프론트엔드 문제나 구현하려는 기능의 목적.
- 기대되는 최종 결과물(UI 변경, 기능 추가 등).

---

## Context
작업 전 시스템의 상태를 설명합니다.
- 관련된 UI/UX 요구사항
- 의존하는 백엔드 API 및 엔드포인트
- 사용 중인 테마 (Avalon / Genesis)
- 관련된 이전 작업 로그 또는 아키텍처 제약 사항

---

## Work Performed
작업한 구체적인 단계들을 기술합니다.
1. 아키텍처 계층 설계 (core/themes/views 분리 준수 여부)
2. Pinia Store 또는 API Service(core/api) 구현
3. Vue Component 및 Composable 개발
4. 스타일링 및 테마 독립성(P2) 확인
5. i18n(다국어) 처리 여부

---

## Files Modified
수정되거나 생성된 모든 파일 목록을 작성합니다. (경로 포함)
예:
- src/main/webapp/app/core/api/user-service.ts
- src/main/webapp/app/views/user/user-list.vue
- src/main/webapp/app/themes/avalon/components/user-card.scss

---

## Architecture Impact
프론트엔드 아키텍처(P1~P4 원칙)에 미치는 영향.
- [ ] core -> themes -> views 계층 구조를 준수했는가?
- [ ] 테마 간 스타일 간섭(Leak)이 없는가?
- [ ] 공통 로직이 적절하게 core 또는 composable로 분리되었는가?

---

## Security & API Impact
- [ ] 백엔드 API 계약(RFC 7807 에러 구조)이 정상적으로 처리되었는가?
- [ ] JWT 토큰 또는 민감 정보가 로그나 클라이언트 스토리지에 노출되지 않는가?
- [ ] 인가(Role)에 따른 UI 분기 처리가 되었는가?

---

## Verification
작업을 어떻게 검증했는지 설명합니다.
- [ ] `npm run lint`: 코드 컨벤션 확인
- [ ] `vitest`: 유닛/컴포넌트 테스트 수행 결과
- [ ] `npm run build`: 빌드 에러 여부 확인
- [ ] 수동 UI 확인: 브라우저에서의 동작 및 반응형 레이아웃 체크

---

## Risks
확인된 리스크나 미해결 사항.
- 특정 브라우저에서의 렌더링 이슈
- 백엔드 API 미구현으로 인한 Mock 데이터 사용 여부 등

---

## Next Suggested Tasks
이 작업 이후에 이어져야 할 후속 태스크.
예:
- 추가적인 UI 스타일링 고도화
- 복잡한 상태 관리를 위한 Pinia 스토어 리팩토링

---

## Notes for Future Agents
다음 작업자가 알아야 할 중요한 결정 사항이나 가이드.
- 선택한 컴포넌트 설계 방식의 이유
- 특정 외부 라이브러리(PrimeVue 등) 사용 시 주의점

---

## Minimal Task Log

표준 작업용 최소 기록 형식입니다.

```md
## Token Check
- 작업 유형:
- 읽은 파일:
- 대략 범위:
- 추가 컨텍스트:
- 결과:
```

예시:

```md
## Token Check
- 작업 유형: docs
- 읽은 파일: AGENTS.md, docs/analysis/2026-04-25-token-usage-analysis/measurement-plan.md
- 대략 범위: 2 files / 220 lines
- 추가 컨텍스트: graphify 미사용
- 결과: 마이그레이션 및 cross-module 작업만 graphify 확인
```
