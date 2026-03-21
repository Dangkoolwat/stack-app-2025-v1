# 실행 흐름 (Walkthrough)

1. **설정 주입 시점**:
   - 애플리케이션 기동 시 스프링 부트가 `DatabaseConfiguration` 빈을 로드합니다.
   - `dataSource()` 메서드 파라미터로 `Environment`(DB url, 계정 등)와 `ApplicationProperties`(수치 정보들) 속성을 획득하여 `HikariDataSource` 객체의 기반 데이터를 설정합니다.
2. **동적 사이즈 계산 (`max-pool-size`)**:
   - `ApplicationProperties`의 `database.max-pool-size` 값을 확인합니다.
   - 해당 값이 `0` 이하라면 커스텀 할당이 필요하다 판단해, 서버의 `Runtime.getRuntime().availableProcessors()`를 통해 런타임 머신의 코어 수를 확인합니다.
   - `(코어수 * 2) + 1` 공식에 맞춰 HikariCP 풀의 `maximumPoolSize`를 동적으로 확대/세팅합니다.
3. **고정 풀 적용 (`minimum-idle`)**:
   - 풀 크기가 결정된 후, `minimum-idle` 속성이 `-1` 값을 가지고 있다면 최저 유휴 연결 옵션을 앞서 설정된 `maximumPoolSize`와 완전히 동일하게 덮어씁니다.
   - 고정 풀 사이즈가 되어 연결/소멸의 반복적인 생성 오버헤드가 제거되며 트래픽 급증 시의 대비 성능이 극대화됩니다.
4. **테스트 검증 파이프라인**:
   - 패키징 전 `mvnw test` 실행 시, `DatabaseConfigurationTest.java`가 `MockEnvironment` 상에서 위 로직이 0, -1과 같은 변수를 받았을 때 정확한 튜닝 수치를 반환하는가 사전에 차단 및 검사합니다.
