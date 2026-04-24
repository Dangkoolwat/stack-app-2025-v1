# Walkthrough

- 웹에서 TOAST UI 훅이 통신을 던지면 `POST /api/uploads` API가 매핑됩니다.
- 권한 체크(`@PreAuthorize("isAuthenticated()")`)로 사용자가 접근 가능한지 파악합니다.
- UUID의 랜던 스토리지 키를 배정받아 `UploadService.saveUpload` 메서드를 통해 로컬 혹은 클라우드에 물리 파일이 배정됩니다.
- DB 에 메타 정보가 매핑되고 다운로드가 가능한 `/api/uploads/{id}/preview` URL 이 리턴되며, 에디터는 이 URL만을 본문에 텍스트 스트링으로 유지합니다.
- 만약 클라이언트가 `DELETE /api/uploads/{id}` 를 찌르면 DB(`UploadEntity`)의 `is_deleted` 상태를 `true`로 바꿉니다. (물리적으론 아무 일도 일어나지 않음)
- 월말/주말, 관리자가 관리자 어드민 전용 URL `DELETE /api/admin/uploads/purge` 호출 시, Hibernate 에 `@SQLRestriction` 이 걸려있지 않는 전용 네이티브 쿼리를 돌려 찌꺼기 목록들을 취득하고 한 건씩 물리 파기를 시작합니다.
