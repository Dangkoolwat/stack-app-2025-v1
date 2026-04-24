---
agent: Antigravity
created_at: 2026-03-25 (수)
language: ko
---

# Final Report: 게시판 리소스 관리 (Orphan Resources) 완전 삭제

## 1. 수행 에이전트
- Agent Model: Antigravity

## 2. 요약
- **목적:** 소프트 삭제(`is_deleted=1`)되거나 글 작성 중 이탈하여 참조를 잃은 고아(Orphan) 상태의 리소스(첨부파일, 태그, 댓글)를 관리자가 조회하고 완전 삭제할 수 있는 기능 추가.
- **주요 내용:**
  - 백엔드: `UploadRepository`, `TagRepository`, `CommentRepository` 에 고아 리소스 조회용 쿼리(안전장치 24시간 시간 제한 포함) 및 하드 삭제 로직 추가.
  - 통합 API: `OrphanResourceAdminResource` 단일 컨트롤러 신설.
  - 프론트엔드: `board-resource-management.vue`, `board-resource-management.component.ts` 컴포넌트 추가 및 `entities-menu.vue`에 통합 메뉴 추가.

## 3. 이유 (도입 배경)
- 프로젝트 원칙상 게시물 데이터는 소프트 삭제가 우선되나, 저장소 용량 확보 및 무의미한 고아 데이터 청소를 위해 완전 삭제(Hard Delete) 기능이 필요함.
- "작성 후 24시간 경과" 조건을 추가하여 정상적으로 게시글 작성 중인 찰나에 리소스가 지워지는 것을 방지함.

## 4. 영향 분석
- **저장소 용량 절감:** `UploadService.hardDelete()` 호출 시 실제 로컬/S3 파일까지 물리적으로 삭제됨.
- **성능:** 고아 타겟을 `@Query` 로 조회하여 쿼리 부담 경감 및 인덱스 활용 구조 수립.
- **보안:** `hasAuthority('ROLE_ADMIN')`을 통해 관리자만 접근 및 조작 가능.

## 5. 결과 (Verification)
- 통합 API(`/api/admin/orphans`)가 Upload, Tag, Comment에 대해 `GET`, `DELETE`를 정상적으로 처리함.
- Vue 페이지에서 조회, 선택, 모달 경고 및 삭제 요청 처리가 동작함.
- `Entities` 하위 라우트로 안전하게 등록됨 (`/board-resource`).
- 컴파일/빌드 오류 없음. TS Lint 수정 완료.
