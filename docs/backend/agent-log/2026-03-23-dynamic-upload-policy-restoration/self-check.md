# 셀프 체크 (Self-Check)

- [x] **아키텍처 준수**: `Settings` 엔티티의 JSON 구조와 `GlobalSettingsService` 캐시 로직의 정합성을 유지함.
- [x] **파괴적 변경 없음**: 기존 데이터의 손실 없이 컬럼 타입을 확장함.
- [x] **롤백 가능성**: Liquibase 변경 이력을 통해 스키마 추적이 가능함.
- [x] **테스트 전략**: `SettingsResourceIT`를 통해 Oracle 환경에서의 실제 동작을 검증함.
- [x] **보안 영향**: 파일 업로드 정책 검증 로직(`UploadService`)이 정상적으로 동작하도록 코드를 수정함.
- [x] **설정/의존성**: Jackson 3의 직렬화 정책에 부합하도록 VO 타입을 최적화함.
