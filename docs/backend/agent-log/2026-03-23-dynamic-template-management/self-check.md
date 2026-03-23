# 자가 점검 (Self-Check)

- [x] Architecture compliance: 도메인 모델(`Settings`)에 구조화된 JSON 형태로 통합하여 계층 간 결합도를 최소화함.
- [x] No hidden breaking changes: 기존 `fileTypePolicies` 로직은 유지하며 상위 템플릿 개념만 추가함.
- [x] Rollback possible: Liquibase를 통해 스키마 및 초기 데이터가 관리되므로 롤백 가능.
- [x] Test strategy defined: `SettingsResourceIT` 및 `GlobalSettingsServiceT`를 통해 생성자 변경 및 데이터 연동 검증 완료.
- [x] Security impact reviewed: `ADMIN` 권한자만 접근 가능한 `SettingsResource`를 통해서만 수정 가능하도록 보호함.
- [x] Config / dependency impact checked: 추가적인 외부 의존성 없이 기존 Jackson 3 및 Liquibase 내에서 해결함.
