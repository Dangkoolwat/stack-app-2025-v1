# 자가 점검 (Self-Check)

- [x] **아키텍처 준수**: `ApplicationProperties` 통합 구조가 프로젝트 표준에 부합하는가?
- [x] **속성 파일 삭제**: 사용하지 않게 된 `*Properties.java` 파일들이 모두 삭제되었는가?
- [x] **테스트 통과**: `StorageService`, `RateLimitingFilter`, `ApplicationProperties` 관련 테스트가 PASS되는가?
- [x] **경로 일관성**: `/uploads` 접두사 중복 문제가 해결되어 실제 파일 접근에 문제가 없는가?
- [x] **보안 영향**: Rate Limit 정책이 각 엔드포인트별로(OTP 5회 등) 올바르게 적용되었는가?
- [x] **하위 호환성**: `application.yml`의 기존 설정 키가 그대로 유지되는가?
- [x] **가독성**: 코드 내 한글 주석이 충분히 추가되어 유지보수가 용이한가?
