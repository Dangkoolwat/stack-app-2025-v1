# Walkthrough - Entity CRUD Generation

본 문서는 `Board` 및 `CommonCodeGroup` 엔티티의 frontend CRUD 화면을 자동 구성한 과정을 설명합니다.

## 1. 개요
Swagger(OpenAPI) 스펙과 Spring Boot 백엔드 API를 기반으로, Vue 3 프론트엔드에서 해당 엔티티들을 관리할 수 있는 메뉴, 라우터, 서비스, 그리고 CRUD(List, Detail, Update) 화면을 생성하였습니다.

## 2. 수행 작업 목록

### 2.1 i18n 다국어 리소스 생성
- Board: `src/main/webapp/i18n/ko/board.json`, `en/board.json`
- Tag: `src/main/webapp/i18n/ko/tag.json`, `en/tag.json`
- CommonCodeGroup: `src/main/webapp/i18n/ko/common-code-group.json`, `en/common-code-group.json`
- CommonCodeDetail: `src/main/webapp/i18n/ko/common-code-detail.json`, `en/common-code-detail.json`
- UI에 표시되는 모든 텍스트는 하드코딩하지 않고 번역 키를 사용하도록 구현하였습니다.

### 2.2 서비스 레이어 구현
- BoardService: `src/main/webapp/app/entities/board/board.service.ts`
- TagService: `src/main/webapp/app/entities/tag/tag.service.ts` (Admin Soft Delete 포함)
- CommonCodeService: `src/main/webapp/app/entities/common-code/common-code.service.ts` (그룹 및 상세 코드 통합 관리)
- 생성된 서비스는 `src/main/webapp/app/main.ts`에 등록되어 의존성 주입(provide/inject)이 가능하도록 설정하였습니다.

### 2.3 라우터 및 메뉴 등록
- Router: `src/main/webapp/app/router/entities.ts`에 각 엔티티별 기반 경로를 등록하였습니다.
- Menu: `src/main/webapp/app/entities/entities-menu.vue`에 태그(`hashtag`), 공통코드(`tags`), 상세코드(`list-ol`) 메뉴를 연동하였습니다.

### 2.4 CRUD 컴포넌트 생성
각 엔티티별로 `.vue` 파일과 `.component.ts` 파일을 분리하여 생성하였습니다.
- Board: List, Detail, Update (BOARD_TYPE 연동)
- Tag: List (Soft Delete 연동)
- CommonCodeGroup: List, Detail, Update
- CommonCodeDetail: List (그룹별 필터링), Update (그룹 선택 포함)

## 3. 검증 내용
- i18n 적용: `$t` 또는 `v-text="t$('...')" `를 통한 다국어 지원 확인.
- 의존성 주입: `inject`를 통한 서비스 및 알림 서비스 호출 구조 확인.
- 관계 연동: `CommonCodeDetail` 생성 시 `CommonCodeGroup` 목록을 조회하여 할당하는 등 엔티티 간 관계를 고려한 UI를 구성하였습니다.


## 4. 향후 작업
- `CommonCodeDetail` 및 `Tag` 엔티티에 대해서도 동일한 패턴으로 CRUD를 확장할 수 있습니다.
- 백엔드의 `api.yml` 스펙 변경 시 프론트엔드 DTO(model) 및 서비스의 동기화가 필요합니다.
