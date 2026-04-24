# Self-Check

- [x] Architecture compliance: JHipster의 `@PreAuthorize` 및 OpenAPI(`@Operation`) 표준 문서 작성을 모두 지키고 있는가? (지킴)
- [x] No hidden breaking changes: 신규 API 추가로 인해 기존 다운로드 로직이 부서지는가? (영향 없음)
- [x] Rollback possible: 추가된 쿼리와 서비스 로직은 언제든 분리하거나 제거할 수 있는가? (분리되어 있음, 가능)
- [x] Test strategy defined: `UploadResourceIT`와 `UploadAdminResourceIT` 클래스에 관련 REST 통합 테스트 추가.
- [x] Security impact reviewed: `Multipart` 업로드 어뷰징 방지를 위해 `ApplicationProperties` 내 최대 업로드 제약 사항을 연동할 준비, CSRF 통제를 Axios로 이관.
- [x] Config / dependency impact checked: Tika MIME Validation은 기존 유지됨.
- [x] OpenAPI impact checked: `/api/uploads` 신규 노출 확인.
