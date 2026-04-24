# Self-Check

- [x] Architecture compliance: 기존 `GlobalSettingsService` 구조 유지 및 엔티티 내부 JSON 필드 도입(Hibernate 6 기능 활용)
- [x] No hidden breaking changes: DTO 구조를 유지하여 프론트엔드 및 API 영향 최소화
- [x] Rollback possible: Liquibase를 통해 이전 상태로 복구 가능하도록 스크립트 작성
- [x] Test strategy defined: 엔티티 직렬화/역직렬화 테스트 및 서비스 로직 검증
- [x] Security impact reviewed: `SettingsResource`의 `@PreAuthorize` 권한 설정 유지 확인
- [x] Config / dependency impact checked: Hibernate 6 JSON 매핑 지원 여부 확인(Spring Boot 4 기준 지원됨)
- [x] Cache safety checked: `SETTING_CACHE` (Redis) 초기화 로직 유지 확인
- [x] OpenAPI impact checked: `/api/settings` 스펙 유지 확인
