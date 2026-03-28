---
agent: GPT-5 Codex
created_at: 2026-03-28 (Saturday)
language: ko
---

# 프론트엔드 UI UX 점검 보고서

## 1. 목적

이 문서는 현재 Vue 3 프론트엔드의 화면 구조와 사용자 경험을 코드 기준으로 점검하고, 사용성 개선을 위한 우선순위 높은 제안을 정리한 분석 보고서다.

이번 점검은 실제 구현 변경이 아니라 다음에 집중했다.

- 현재 레이아웃과 네비게이션이 사용자 목적에 맞게 구성되어 있는가
- 대시보드가 실제 운영 화면으로서 역할을 하는가
- CRUD 화면들이 반복 작업에 적합한가
- 디자인 시스템과 화면 구현 방식이 일관적인가

## 2. 점검 범위

확인한 주요 파일:

- `src/main/webapp/app/app.vue`
- `src/main/webapp/app/core/jhi-navbar/jhi-navbar.vue`
- `src/main/webapp/app/core/home/home.vue`
- `src/main/webapp/app/entities/entities-menu.vue`
- `src/main/webapp/app/entities/board/board.vue`
- `src/main/webapp/app/entities/board/board-detail.vue`
- `src/main/webapp/app/entities/board/board-update.vue`
- `src/main/webapp/app/entities/common-code-detail/common-code-detail.vue`
- `src/main/webapp/app/admin/user-management/user-management.vue`
- `src/main/webapp/app/admin/health/health.vue`
- `src/main/webapp/app/admin/metrics/metrics.vue`
- `src/main/webapp/app/admin/logs/logs.vue`
- `src/main/webapp/app/admin/resource-management/resource-management.vue`
- `src/main/webapp/content/scss/global.scss`
- `docs/frontend/Architecture.md`
- `docs/frontend/Engineering_Guideline.md`

## 3. 핵심 결론

현재 프론트엔드는 기능은 존재하지만, 사용자 경험 관점에서는 "운영 도구를 빠르게 쓰기 위한 정보 구조"가 약하다.

가장 큰 문제는 예쁜가 못생겼다가 아니라 다음 세 가지다.

1. 화면 목적이 불명확하다.
2. 탐색 구조가 사용자의 업무 흐름과 맞지 않는다.
3. 리스트, 상세, 수정 화면이 모두 JHipster 기본 CRUD 패턴에 가까워 반복 작업 효율이 낮다.

특히 로그인 후 홈 화면은 대시보드라기보다 환영 메시지와 계정 정보 카드에 가깝다. 사용자는 로그인 직후 다음 행동을 선택할 근거를 얻기 어렵다.

## 4. 주요 문제

### 4.1 정보 구조와 네비게이션

문제:

- 상단 네비게이션이 `Home / Entities / Admin / Language / Account` 구조인데, 실제 업무 관점의 분류가 아니다.
- `Entities` 아래에 설정, 공통코드, 태그, 게시글, 게시글 리소스 관리가 섞여 있다.
- `board-resource`는 엔티티 메뉴에 있고, `/admin/resource-management`는 별도 관리자 라우트로도 존재해 정보 구조가 이원화되어 있다.
- 사용자는 "콘텐츠 관리", "시스템 관리", "운영 진단" 같은 목적 기반 묶음을 기대하는데, 현재는 기술적 분류가 앞에 나온다.

영향:

- 기능은 많지 않아도 어디로 가야 하는지 바로 판단하기 어렵다.
- 신규 사용자나 운영자가 화면 구조를 기억해야만 사용할 수 있다.

### 4.2 대시보드 부재

문제:

- 로그인 후 홈 화면은 실질적 운영 대시보드가 아니다.
- 현재 구성은 환영 알림, 로그인 ID, Role, Environment 정도만 보여 준다.
- 빠른 진입 링크, 최근 작업, 시스템 상태 요약, 주의가 필요한 항목이 없다.

영향:

- 로그인 후 첫 화면이 사용자의 다음 행동을 유도하지 못한다.
- "대시보드"라는 기대와 실제 화면의 정보 가치가 맞지 않는다.

### 4.3 리스트 화면의 생산성 부족

문제:

- 게시글, 사용자, 공통코드 상세 등 주요 리스트가 대부분 넓은 테이블 중심이다.
- 검색, 필터, 저장된 정렬, 상태 요약, 일괄 작업 같은 운영용 장치가 부족하다.
- 액션 버튼이 행 끝에만 몰려 있고, 중요 필드의 위계가 약하다.
- 테이블 헤더 클릭 정렬은 있으나, 정렬 상태와 검색 맥락이 약해 사용자가 계속 재탐색해야 한다.

영향:

- 데이터가 조금만 늘어나도 탐색 비용이 빠르게 증가한다.
- 모바일이나 작은 화면에서 사실상 사용성이 급격히 낮아진다.

### 4.4 상세/수정 화면의 읽기 흐름 부족

문제:

- 게시글 상세는 `<dl>` 기반 메타데이터 나열과 본문, 첨부, 태그가 한 화면에 단순 직렬 배치되어 있다.
- 게시글 수정 화면도 긴 단일 폼 구조라 입력 우선순위가 약하다.
- 제목, 본문, 태그, 첨부, 게시 옵션이 모두 동일한 시각적 무게를 가진다.

영향:

- 읽을 때는 정보가 끊기고, 수정할 때는 중요한 입력과 부가 입력의 경계가 흐리다.
- 작성과 검수 흐름이 자연스럽지 않다.

### 4.5 디자인 시스템 불일치

문제:

- `docs/frontend/Engineering_Guideline.md`는 `PrimeVue through the Themes layer`를 선호한다고 적고 있다.
- 실제 화면은 `bootstrap-vue-next`와 JHipster 기본 구조에 직접 의존하고 있다.
- 재사용 가능한 Base 컴포넌트 레이어보다 화면별 마크업 반복이 많다.

영향:

- 화면마다 밀도, 버튼 위치, 헤더 구조, 테이블 처리 방식이 조금씩 달라질 가능성이 높다.
- 향후 UI 개선을 해도 화면별 부분 최적화에 머무를 위험이 있다.

### 4.6 시각적 위계와 공간 설계 부족

문제:

- 전역 레이아웃은 `container-fluid > card.jh-card`로 대부분의 화면을 감싸고 있어 "큰 흰 카드 안에 또 표와 카드가 들어가는" 이중 컨테이너가 자주 생긴다.
- 상단 헤더, 페이지 타이틀, 보조 설명, 액션 영역이 체계적으로 분리되어 있지 않다.
- Flatly 테마와 기본 Bootstrap 스타일 위에 일부 수동 스타일만 더해져 화면마다 완성도 차이가 난다.

영향:

- 정보 우선순위가 약하게 느껴진다.
- 화면이 답답하고, 업무용 도구치고는 시선 이동량이 많다.

## 5. 가장 먼저 개선해야 할 방향

권장 방향:

"대시보드 재정의 + 목적 기반 네비게이션 + 운영형 리스트 패턴 표준화"를 1차 목표로 잡는 것이 가장 효과적이다.

이 방향이 좋은 이유:

- 사용자 체감 개선 폭이 가장 크다.
- 많은 화면을 전면 재개발하지 않아도 된다.
- 홈, 메뉴, 리스트 헤더 패턴만 잡아도 전체 인상이 크게 좋아진다.

대안 1:

- 색상, 간격, 버튼 스타일만 우선 정리
- 장점: 빠르다
- 단점: 동선 문제와 정보 구조 문제는 거의 남는다

대안 2:

- 게시판 영역만 먼저 새 UI로 전면 재설계
- 장점: 핵심 도메인 집중 가능
- 단점: 전체 앱 사용성은 부분 개선에 그칠 수 있다

## 6. 대시보드 재배치 제안

현재 홈 화면은 유지 가치가 낮으므로, 로그인 후에는 운영 대시보드로 바꾸는 것이 좋다.

권장 구조:

### 6.1 상단 요약 영역

- 오늘의 상태
- 최근 변경 수
- 삭제 대기 리소스 수
- 활성 사용자 수 또는 최근 로그인 수
- 시스템 이상 유무

형태:

- 4개 카드 또는 2x2 카드
- 숫자 + 짧은 설명 + 바로가기

### 6.2 빠른 작업 영역

- 게시글 작성
- 게시글 관리
- 공통코드 관리
- 사용자 관리
- 삭제 리소스 확인

형태:

- 아이콘 카드 또는 버튼형 링크
- 사용 빈도순으로 배치

### 6.3 주의 필요 영역

- 삭제된 리소스 존재
- 비활성 사용자
- 오류 상태 health 항목
- 최근 수정된 설정

형태:

- Alert list 또는 compact table

### 6.4 시스템 진단 영역

- Health / Metrics / Logs / Tracker 바로가기
- 상세 수치 전체를 홈에 다 노출하지 말고 "요약 + 진입"만 제공

형태:

- 1줄 카드 묶음 또는 탭형 요약

## 7. 네비게이션 구조 제안

현재:

- Home
- Entities
- Admin
- Language
- Account

권장:

- Dashboard
- Content
- System
- Account

예시 매핑:

- Dashboard
  - 홈
- Content
  - 게시글
  - 태그
  - 공통코드
  - 첨부/삭제 리소스
- System
  - 사용자 관리
  - Health
  - Metrics
  - Logs
  - Tracker
  - API Docs
  - Configuration

추가 제안:

- 언어 전환은 글로벌 유틸이므로 드롭다운 가장 오른쪽 아이콘형으로 축소
- 버전 표시는 브랜드 옆 상시 노출보다, 계정 메뉴 하단이나 footer 쪽이 덜 산만하다
- JHipster 기본 로고는 프로젝트 고유 로고 또는 텍스트 심볼로 대체하는 편이 좋다

## 8. 화면별 개선 제안

### 8.1 게시글 목록

현재 문제:

- 표 중심 구조
- 중요 정보와 부가 정보가 같은 밀도
- 필터 부재

개선:

- 상단에 검색창, 게시판 유형 필터, 공지 여부 필터, 정렬 기준 추가
- 본문 일부 미리보기나 태그를 목록 카드/행 안에 함께 표시
- 행 액션 버튼 수를 줄이고, 1차 액션은 제목 클릭과 행 메뉴로 정리
- 모바일에서는 카드 리스트 형태로 전환

### 8.2 게시글 상세

현재 문제:

- 메타데이터와 본문이 같은 흐름에 섞여 있음

개선:

- 상단: 제목, 작성자, 생성일, 조회수, 공지 여부
- 본문: 넓은 읽기 영역
- 우측 또는 하단 보조 패널: 태그, 첨부파일, 관리 액션
- 첨부파일은 파일 유형 아이콘과 다운로드 액션을 더 명확히 표시

### 8.3 게시글 수정

현재 문제:

- 긴 단일 폼
- 핵심 입력과 부가 입력의 구분 부족

개선:

- 2단 레이아웃 사용
- 좌측: 제목, 본문
- 우측: 태그, 첨부, 게시 옵션
- 저장/취소 버튼은 sticky footer 또는 상단 우측에도 노출
- 업로드 영역은 현재보다 상태 표시를 강화
  - 업로드 수
  - 용량 초과 메시지
  - 파일 형식 제한 안내

### 8.4 사용자 관리

현재 문제:

- 운영 테이블로는 컬럼이 많고 가독성이 낮다

개선:

- 기본 목록에는 핵심 컬럼만 남기기
  - login
  - email
  - status
  - authorities
  - last modified
- 상세 정보는 드로어/상세 화면으로 분리
- 활성/비활성 토글은 배지형 버튼 대신 상태 스위치 또는 명확한 action button으로 정리

### 8.5 Health / Metrics / Logs

현재 문제:

- 운영자용 진단 도구인데도 대부분 원본 테이블과 수치 나열에 가깝다

개선:

- Health: 정상/주의/오류 개수 요약 카드 + 상세 테이블
- Metrics: 주요 지표만 접은 섹션으로 우선 노출
- Logs: 필터, 즐겨찾기 logger, 최근 변경된 logger 강조

## 9. 디자인 시스템 측면 제안

구조 제안:

- `PageHeader`
- `PageSection`
- `SummaryCard`
- `DataToolbar`
- `EmptyState`
- `StatusBadge`
- `ActionMenu`

기대 효과:

- 헤더 구조, 액션 위치, 빈 상태, 요약 카드 패턴이 통일된다
- 화면별 품질 편차가 줄어든다
- Bootstrap을 계속 쓰더라도 "프로젝트 고유 UI 계층"을 만들 수 있다

중요:

- 지금 단계에서는 UI 라이브러리를 갈아엎기보다, Bootstrap 위에 공통 패턴 컴포넌트를 얹는 편이 현실적이다
- 다만 문서상 PrimeVue 기준을 유지할지, 실제 사용 중인 Bootstrap 기반으로 기준을 다시 정리할지는 별도 결정이 필요하다

## 10. 우선순위별 실행안

### 10.1 1차 개선

- 로그인 후 홈을 실제 운영 대시보드로 변경
- 상단 네비게이션 목적 기반으로 재구성
- 공통 `PageHeader`, `DataToolbar`, `SummaryCard`, `EmptyState` 컴포넌트 도입
- 게시글 목록과 사용자 목록의 헤더/필터/액션 구조 통일

### 10.2 2차 개선

- 게시글 상세/수정 화면 재배치
- 공통코드/리소스 관리 화면의 테이블 밀도 조정
- 상태 배지와 액션 버튼 체계 정리

### 10.3 3차 개선

- Health, Metrics, Logs를 운영자 대시보드와 연결
- 모바일 레이아웃 최적화
- 디자인 토큰 또는 테마 계층 정리

## 11. 리스크와 주의사항

- 현재 앱은 JHipster CRUD 패턴을 많이 따르므로, 한 화면만 과하게 다르게 만들면 전체 일관성이 깨질 수 있다
- 메뉴 구조 변경 시 기존 사용자 학습 비용이 발생할 수 있으므로, 명칭과 그룹핑은 신중해야 한다
- 라우트 구조와 메뉴 구조가 분리되어 있으므로, 정보 구조 정리 시 실제 URL 정책도 함께 검토하는 편이 좋다
- `board-resource`와 `/admin/resource-management`처럼 역할이 중복되거나 경계가 애매한 메뉴는 먼저 정리해야 한다

## 12. 최종 제안

가장 추천하는 시작점은 다음 3개다.

1. 로그인 후 홈을 운영형 대시보드로 재정의한다.
2. `Entities/Admin` 중심 메뉴를 `Content/System` 중심 구조로 바꾼다.
3. 게시글 목록과 사용자 목록에 공통 툴바 패턴을 적용해 검색, 필터, 액션 흐름을 표준화한다.

이 세 가지만 먼저 해도 사용자는 다음 변화를 바로 체감할 가능성이 높다.

- 어디로 가야 할지 덜 헷갈림
- 로그인 후 해야 할 일이 더 분명해짐
- 반복 작업 속도가 빨라짐
- 앱이 "기본 생성 화면 모음"이 아니라 "운영 도구"처럼 느껴짐

## 13. 공통 컴포넌트 UI 감사

이번 점검에서 특히 반복적으로 보인 문제는 "버튼 위치, 버튼 간격, 하단 네비게이션 간격, 모달 푸터 정렬"이 화면마다 제각각이라는 점이다.

이 문제는 큰 디자인 개편보다 먼저 공통 규칙을 정하면 빠르게 개선할 수 있다.

### 13.1 모달 푸터 버튼 정렬

현재 관찰:

- 삭제 확인 모달의 footer는 대부분 직접 커스텀되어 있음
- 어떤 화면은 `btn-secondary`와 `btn-primary`가 붙어 있고, 어떤 화면은 `me-2` 간격이 있고, 어떤 화면은 없음
- 어떤 삭제 모달은 위험 동작인데 확인 버튼이 `primary` 색상임

대표 위치:

- `src/main/webapp/app/entities/board/board.vue`
- `src/main/webapp/app/entities/tag/tag.vue`
- `src/main/webapp/app/entities/common-code-group/common-code-group.vue`
- `src/main/webapp/app/entities/common-code-detail/common-code-detail.vue`
- `src/main/webapp/app/admin/user-management/user-management.vue`
- `src/main/webapp/app/entities/board-resource-management/board-resource-management.vue`
- `src/main/webapp/app/admin/resource-management/resource-management.vue`

권장 규칙:

- 모달 footer는 항상 오른쪽 정렬
- 버튼 순서는 항상 `취소 → 확인`
- 기본 간격은 `8px`
- destructive action은 확인 버튼을 반드시 `danger`
- 일반 저장/확정은 `primary`
- footer 높이와 패딩은 공통화

권장 스타일:

- footer padding: `16px 20px`
- 버튼 min-width: `96px`
- 버튼 간 gap: `8px`

### 13.2 폼 하단 액션 버튼

현재 관찰:

- 수정/생성 화면의 하단 액션은 대부분 왼쪽 정렬 혹은 단순 inline 버튼 두 개로 구성
- 어떤 화면은 `mt-3`, 어떤 화면은 `hr + justify-content-end`, 어떤 화면은 별도 액션 바가 없음

대표 위치:

- `src/main/webapp/app/entities/board/board-update.vue`
- `src/main/webapp/app/entities/common-code-group/common-code-group-update.vue`
- `src/main/webapp/app/admin/user-management/user-management-edit.vue`

문제:

- 저장 버튼이 폼 끝에만 보여 긴 화면에서 발견성이 떨어짐
- 취소/저장 패턴이 화면마다 달라 학습 비용이 생김

권장 규칙:

- 폼 하단 액션은 항상 오른쪽 정렬
- `취소`는 secondary outline 또는 muted
- `저장`은 primary
- 긴 화면은 sticky footer 또는 상단 보조 저장 버튼 고려
- 액션 영역 상단에는 `margin-top: 24px`와 얇은 구분선 사용

권장 스타일:

- action bar padding-top: `20px`
- action bar margin-top: `24px`
- button gap: `8px`

### 13.3 테이블과 페이지네이션 간격

현재 관찰:

- 리스트 화면의 하단에서 `table → item count → pagination`은 존재하지만 간격 규칙이 약함
- 일부 화면은 `row justify-content-center`, 일부는 `d-flex justify-content-center`
- 테이블과 페이지네이션 사이에 시각적 구분이 부족함

대표 위치:

- `src/main/webapp/app/entities/board/board.vue`
- `src/main/webapp/app/admin/user-management/user-management.vue`

문제:

- 목록이 끝나고 하단 조작부가 붙어 보여 숨이 막히는 느낌을 줌
- 테이블이 큰 경우 페이지네이션이 부속 요소처럼 보이지 않고 애매하게 떠 있음

권장 규칙:

- 테이블 하단에 pagination zone을 별도 블록으로 분리
- `item count`와 `pagination`은 한 묶음으로 보이게 구성
- 테이블과 pagination zone 사이 간격은 최소 `16px`, 권장 `20px`
- pagination zone 내부는 세로 간격 `8px`

권장 구조:

- 데스크탑: 좌측 item count, 우측 pagination
- 모바일: 위 item count, 아래 pagination 중앙 정렬

### 13.4 페이지 헤더와 액션 버튼 간격

현재 관찰:

- 대부분의 리스트 화면이 `h2` 안에 제목과 액션 버튼을 함께 넣고 있음
- 그 뒤에 `<br />`로 간격을 만드는 패턴이 반복됨

대표 위치:

- `src/main/webapp/app/entities/board/board.vue`
- `src/main/webapp/app/entities/common-code-group/common-code-group.vue`
- `src/main/webapp/app/entities/common-code-detail/common-code-detail.vue`
- `src/main/webapp/app/entities/tag/tag.vue`
- `src/main/webapp/app/admin/user-management/user-management.vue`

문제:

- 제목과 액션이 구조적으로 분리되지 않아 헤더가 답답함
- `<br />` 중심 간격은 일관된 시스템을 만들기 어려움

권장 규칙:

- `PageHeader` 패턴으로 통일
- 제목/설명은 왼쪽, 주요 액션은 오른쪽
- 헤더 하단 여백은 `20px ~ 24px`
- `<br />` 제거

### 13.5 테이블 툴바와 필터 입력

현재 관찰:

- `logs.vue`, `configuration.vue`는 별도 툴바가 있으나 단순 inline 필터 수준
- 다른 리스트 화면은 아예 툴바가 없음

문제:

- 화면마다 필터와 액션의 위치가 다르다
- 입력 너비, 여백, 높이가 통일되지 않는다

권장 규칙:

- 테이블 상단에는 항상 `DataToolbar` 사용
- 구성:
  - 좌측: 검색/필터
  - 우측: 새로고침/생성/일괄 작업
- 모바일에서는 세로 적층

### 13.6 공통 권장 수치

- 페이지 헤더 하단 여백: `24px`
- 카드 내부 패딩: `20px ~ 24px`
- 섹션 간 여백: `24px`
- 버튼 간 gap: `8px`
- 폼 필드 간 세로 여백: `16px`
- 테이블 상단 툴바와 테이블 사이: `16px`
- 테이블과 페이지네이션 사이: `20px`
- 모달 푸터 padding: `16px 20px`

### 13.7 바로 도입할 공통 클래스 제안

- `.page-header`
- `.page-header__actions`
- `.data-toolbar`
- `.data-toolbar__filters`
- `.data-toolbar__actions`
- `.table-section`
- `.table-pagination`
- `.form-actions`
- `.modal-actions`

### 13.8 우선 정리 대상

가장 먼저 손보면 체감이 큰 화면:

1. `board.vue`
2. `user-management.vue`
3. `board-update.vue`
4. `common-code-group.vue`
5. `common-code-detail.vue`
6. `board-resource-management.vue`

이 화면들만 공통 규칙에 맞춰도 전체 앱의 완성도가 꽤 올라갈 가능성이 높다.

## 14. 외부 보고서 반영 사항

첨부 문서:

- `/Users/sanghyoukjin/Downloads/JHipster_Vue3_UIUX_종합개선방안_v2_1.docx`

이 문서는 본 보고서와 문제의식이 상당 부분 일치한다. 특히 "디자인 시스템 부재", "CRUD 화면 반복 패턴", "대시보드 정보 구조 부족", "버튼/폼/테이블/모달 표준화 필요"는 그대로 수용 가치가 높다.

다만 몇몇 항목은 현재 코드와 다르거나, 우선순위를 조정해서 받아들이는 편이 좋다.

### 14.1 그대로 반영할 가치가 높은 제안

1. 디자인 토큰 먼저 정의

- 색상, 간격, 타이포그래피, radius, shadow를 CSS 변수로 먼저 통일하자는 제안은 매우 타당하다
- 현재 프로젝트는 화면별 직접 스타일 수정이 많아, 토큰이 없으면 품질 편차가 계속 생긴다

2. 버튼 체계화

- Primary / Secondary / Danger / Ghost / Icon-only 구분
- 버튼 간 gap 8px 고정
- 로딩 상태와 disabled 상태의 시각 규칙 통일

3. 폼 UX 표준화

- label 위치 통일
- validation 메시지 위치와 스타일 통일
- 필드 간 세로 간격 규칙화
- 파일 업로드 컴포넌트의 상태 표현 강화

4. 테이블을 단순 CRUD 표가 아니라 운영용 DataGrid로 재설계

- 정렬, 필터, 선택, 모바일 대응 전략을 함께 설계해야 한다는 방향은 적절하다
- 다만 전체 라이브러리 교체보다 현재는 "헤더 + 툴바 + pagination zone" 표준화가 우선이다

5. 모달 계층 구조 분리

- 단순 삭제 확인은 경량 confirm
- 폼 입력이나 상세 확인은 modal
- 복잡 설정은 drawer 또는 full sheet

이 방향은 현재 모든 액션에 `b-modal`을 남용하는 문제를 줄이는 데 도움이 된다

6. 대시보드의 F-pattern 정보 배치

- KPI 행
- 메인 차트/주요 분석
- 보조 위젯
- 하단 상세 목록

이 구조는 운영 화면에 적합하고, 로그인 후 홈을 재정의할 때 바로 참고할 수 있다

7. 빈 상태, 로딩 상태, 알림 표준화

- 지금도 일부 spinner와 alert는 있지만 화면마다 통일성이 부족하다
- Empty State, Skeleton, Notification Store를 공통 패턴으로 묶는 제안은 유효하다

### 14.2 현재 코드 기준으로 보정이 필요한 제안

1. "라우트 Lazy Loading 미적용"은 현재 코드와 다르다

- 현재 `router/index.ts`, `router/admin.ts`, `router/entities.ts` 등에서 다수 화면이 이미 동적 import 기반으로 lazy loading 되고 있다
- 따라서 이 항목은 "미적용"이 아니라 "적용은 되어 있으나, UX 체감 개선을 위한 Skeleton/Prefetch 전략은 부족"으로 수정하는 편이 맞다

2. "Pinia / Vuex 혼용"은 현재 점검 범위에서는 확정적으로 보이지 않는다

- 본 점검에서 확인한 범위는 Pinia 및 JHipster store 중심이며, 현행 코드 기준 핵심 문제는 상태관리 라이브러리 혼용보다 UI 계층 표준 부재에 가깝다

3. PrimeVue 전면 도입은 장기 옵션이지 단기 권장안은 아니다

- 외부 문서는 PrimeVue 도입을 강하게 추천하지만, 현재 프로젝트는 Bootstrap-Vue-Next 기반 화면이 이미 넓게 퍼져 있다
- 지금 바로 라이브러리 교체에 들어가면 비용과 리스크가 크다
- 단기적으로는 Bootstrap 위에 공통 UI 패턴을 씌우는 것이 현실적이다

4. Virtual Scroll, Inline Edit, Auto-save는 핵심 1차 범위는 아니다

- 좋은 방향이지만 지금 프로젝트에서 가장 먼저 체감되는 문제는 구조, 간격, 위계, 대시보드, 공통 액션 패턴이다
- 따라서 1차 개선 범위에 넣기보다 2차 이후 후보로 관리하는 편이 맞다

5. 사이드바 중심 구조 제안은 현재 앱 구조와 바로 맞지 않는다

- 현재 앱은 상단 navbar 중심이다
- 외부 문서의 sidebar collapse 제안은 참고 가능하지만, 즉시 적용안은 "상단 메뉴 목적 기반 재구성"이 더 적합하다

### 14.3 외부 보고서를 반영한 우선순위 보정

반영 후 1차 우선순위:

1. 디자인 토큰 정의
2. 공통 버튼/모달/폼 액션 규칙 정의
3. PageHeader, DataToolbar, EmptyState, SummaryCard 공통 패턴 도입
4. 로그인 후 홈을 운영형 대시보드로 개편
5. 게시글/사용자/공통코드 목록 화면의 리스트 UX 표준화

반영 후 2차 우선순위:

1. Notification/Toast/오류 메시지 체계 통일
2. Modal vs Popconfirm vs Drawer 역할 분리
3. 상세/편집 화면 재배치
4. Health/Metrics/Logs 운영 UX 개선

반영 후 장기 우선순위:

1. 컴포넌트 라이브러리 재평가
2. Storybook 또는 컴포넌트 문서화 체계
3. Virtual Scroll, Saved Views, Global Search, Accessibility 고도화

### 14.4 최종 통합 판단

외부 문서의 장점은 "세부 컴포넌트 규칙"과 "단계별 실행 로드맵"을 잘 정리했다는 데 있다.

본 보고서와 통합했을 때 가장 실무적인 결론은 다음과 같다.

- 지금 당장 필요한 것은 라이브러리 교체보다 공통 UI 규칙 수립이다
- 대시보드와 리스트 화면 개편이 체감 개선 효과가 가장 크다
- 버튼, 모달, 폼 액션, 페이지네이션 간격 같은 미세 규칙을 먼저 통일해야 전체 품질이 올라간다
- 외부 문서의 장기 제안은 보관하되, 1차 실행은 현행 Bootstrap 기반 구조 안에서 진행하는 것이 안전하다
