---
agent: Antigravity
created_at: 2026-03-25 (수)
language: ko
---

# 게시판 리소스 관리 - 완전 삭제 작업 플랜 (Analysis)

## 1. 개요 및 목적
- **목적:** 소프트 삭제(is_deleted=1) 되거나, 에디터 상에서 업로드/태그 입력 후 최종 저장 없이 이탈하여 생성된 식별되지 않은 외톨이 리소스(Orphan contents)를 관리자가 직접 조회하고 영구 삭제(Hard Delete)할 수 있도록 전용 관리자 UI 및 API를 구현.

## 2. 주요 대상 컨텐츠 (Orphan Target)
1. **Upload (업로드/첨부파일/이미지)**
   - 스토리지: 로컬 또는 클라우드 스토리지 상의 물리적 파일.
   - 대상 조건: `is_deleted = true` 이거나 (`board_id`가 null 이면서 `created_date`가 24시간 이전인 경우).
   - 기존 API 검증결과: `/api/admin/uploads/purge` 및 특정 파일 식별 하드 삭제 기능 존재. 확장/통합 적용 필요.
2. **Tag (태그)**
   - 스토리지: DB 테이블 내 레코드.
   - 대상 조건: `is_deleted = true` 이거나, 사용처가 없는 (`usage_count = 0`) 태그 중 24시간이 경과한 것 (에디터 생성 후 취소 시 잔존하는 태그).

## 3. 구현 제안 (작업 절차)

### 3.1. 백엔드 (Backend API)
- **컨트롤러 (`OrphanResourceAdminResource`):** 고아 리소스 목록 조회(GET) 및 일괄 하드 삭제(DELETE) 처리용 API 개발.
  - `GET /api/admin/orphans?type=UPLOAD|TAG`
  - `DELETE /api/admin/orphans?type=UPLOAD|TAG` (Payload: List<Long> ids)
- **리포지토리 변경:**
  - `@SQLRestriction("is_deleted = 0")` 조건 등 기존 소프트 삭제 필터를 무시하고 고아 조건을 찾을 수 있도록 Native Query 혹은 특정 `@Query` 추가.

### 3.2. 프론트엔드 (Frontend UI/UX)
- **메뉴 네비게이션 (`entities-menu.vue`):** 게시판 관리(`Board`) 메뉴 하단 혹은 옆에 `게시판 리소스 관리` (Board Resource Management) 접속 점 생성.
- **라우터/뷰 컴포넌트 (`board-resource-management.vue`):**
  - 상단 필터: 드롭다운으로 `이미지/첨부파일 / 태그` 옵션 선택
  - Data Table: 조회된 고아 항목 리스트 출력. 첫 번째 Column에 일괄 처리를 위한 `<input type="checkbox">` 배치
  - Action Area: 최소 1개 체크 시 활성화되는 `[완전 삭제]` 강렬한 톤의 버튼 노출. 클릭 시 SweetAlert(등) 경고 모달 출력 후 물리 삭제 요청.

## 4. 리뷰 요청
이 작업 절차 및 대상 컨텐츠 분석이 올바른 방향인지 확인을 요청합니다.
"Is this the correct direction?" (이 방향이 맞습니까?)
