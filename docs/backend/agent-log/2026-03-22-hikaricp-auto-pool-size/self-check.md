# 셀프 체크 (Self-Check)

- [x] Architecture compliance: 런타임 환경 감지 로직이 `DatabaseConfiguration` 계층에 캡슐화되어 원칙을 준수함.
- [x] No hidden breaking changes: `maximumPoolSize` 커스텀 설정값이 `0`보다 클 경우 기존 설정값을 사용하도록 방어 코드 추가 (하위 호환 유지).
- [x] Rollback possible: yml 설정만 바꾸면 언제든 기존의 하드코딩된 설정값으로 즉시 복원 가능함.
- [x] Test strategy defined: `DatabaseConfigurationTest.java` 단위 테스트 파일을 추가하여, 환경 변수의 유무 및 값에 따른 다양한 시나리오 (0 설정 vs 숫자 명시) 검증 완료.
- [x] Security impact reviewed: DB 연결 구조 자체를 변경하기 보다는 사이즈 및 연결 튜닝을 진행했으므로 보안에는 유해한 영향 없음.
- [x] Config / dependency impact checked: `ApplicationProperties.java`의 `Database` 클래스를 활용하여 결합도 최소화.
