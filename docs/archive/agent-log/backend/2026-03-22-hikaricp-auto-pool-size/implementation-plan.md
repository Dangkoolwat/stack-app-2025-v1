# 구현 계획 (Implementation Plan)

## 1. DatabaseConfiguration.java 로직 수정
- 스프링의 `Environment`와 커스텀 `ApplicationProperties.Database` 빈을 주입받아 Hikari DataSource를 생성.
- `cores = Runtime.getRuntime().availableProcessors()` 로직을 통해 구동 기기의 코어 수 확보.
- 만약 설정에서 받은 `maxPoolSize`가 `0` 이하일 경우, `(cores * 2) + 1` 로직을 타게 지정.
- `minimumIdle`가 `-1`일 경우 `maxPoolSize`에서 산출된 값을 동일하게 대입하도록 로직 구성.

## 2. application.yml 속성 적용 및 주석 복원
- `application-dev.yml`, `application-prod.yml` 파일에서 `max-pool-size: 0`, `minimum-idle: -1`로 조율해 자동 설정 동작을 활성화.
- 향후 스펙 변경 시 바로 참고할 수 있도록, 이전 설정값과 HikariCP 튜닝 지표(타임아웃, 커넥션 검증 조건 등)에 대한 레거시 상세 설명 주석을 원본 파일에 100% 복구.

## 3. DatabaseConfigurationTest.java 작성 및 검증
- 테스트를 위한 가짜 환경 `MockEnvironment` 및 `ApplicationProperties`를 통해 런타임 수치를 제어.
- 동적 계산된 사이즈와 하드코딩한 사이즈를 각각 주입했을 때 Datasource가 반환하는 세팅값이 정확한지 `assertThat()`을 통해 단위 테스트 실행 (`mvnw clean test`).
