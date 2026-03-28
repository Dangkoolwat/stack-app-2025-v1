---
agent: GPT-5 Codex
created_at: 2026-03-28 (Saturday)
language: ko
---

# Frontend UI Foundation Pattern

## 목적

JHipster 기반 Vue 3 프런트엔드에서 전면 라이브러리 교체 없이도 UX 품질을 빠르게 끌어올리기 위한 실전 패턴을 정리한다.

## 핵심 원칙

1. 라이브러리 교체보다 공통 UI 규칙을 먼저 만든다.
2. 전역 토큰과 공통 레이아웃 클래스를 먼저 도입한다.
3. 홈, 메뉴, 리스트 화면처럼 체감도가 높은 화면부터 적용한다.

## 우선 도입해야 할 공통 패턴

- `page-shell`, `page-surface`
- `dc-page-header`, `dc-page-actions`
- `dc-panel`, `dc-panel__body`
- `dc-toolbar`
- `dc-table-shell`, `dc-table-footer`
- `dc-form-actions`, `dc-modal-actions`
- `dc-status-badge`, `dc-chip`
- `dc-btn-compact`

## 적용 순서

1. 글로벌 SCSS에 토큰과 공통 클래스를 추가
2. 상단 내비게이션 구조 재정리
3. 로그인 후 홈을 운영형 대시보드로 개편
4. 대표 리스트 화면에 공통 헤더/테이블/페이지네이션 적용
5. 이후 상세/수정/모달 계층을 확장
6. health/detail 같이 운영 데이터가 노출되는 팝업은 객체를 표 형태로 렌더링

## 주의점

- Bootstrap 기반 레포에서 UI 라이브러리를 즉시 교체하면 비용이 커진다.
- 공통 패턴을 먼저 정하면 이후 PrimeVue, Naive UI, Tailwind 전환 여부와 무관하게 구조를 유지할 수 있다.
- 전체 lint 실패 여부와 별개로, 변경 파일 단위 검증을 병행하는 것이 현실적이다.
- 운영용 테이블의 액션 버튼은 기본 `btn-sm`보다 더 작은 compact 규칙을 두는 편이 밀도와 가독성에 유리하다.
