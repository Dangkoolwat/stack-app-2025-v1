# Implementation Plan

1. 백엔드 전용 엔드포인트 개통: API 스펙(OpenAPI `@Operation`)에 따라 `UploadResource.java` 에 `POST /api/uploads` / `DELETE /api/uploads/{id}` (Soft Delete 용도) 설계.
2. JHipster `UploadRepository` 가 `@SQLRestriction("is_deleted = 0")`을 지니고 있으므로 삭제 대상을 일괄 취득(Purge)할 수 없으므로 `nativeQuery = true` 인 전용 파기 리스트 조회 메소드를 개설.
3. `UploadService.java` 에 찾아낸 찌꺼기(`is_deleted=1`)를 Hard Delete 반복 호출하는 `purgeSoftDeleted()` 신규 구현.
4. 관리자 단용 `UploadAdminResource.java` 에 `DELETE /api/admin/uploads/purge`를 노출하여 GC 엔드포인트 확보.
5. 관련 통합 테스트들을 `*IT.java`에 구성하여 TDD 통과 준수.
6. 자바 클래스 주석 규범(`/docs/standards/java-class-comment-guideline.md`)에 따라 컨트롤러, 서비스 내 목적 주석을 최신화.
