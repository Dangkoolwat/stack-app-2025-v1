# 실행 흐름 (Walkthrough)

1. 백엔드 App 서버(Tomcat)가 Spring Context 구동과 의존성 주입을 모두 마친다.
2. Spring Boot 런타임이 초기화되면서 `ApplicationRunner`를 상속한 `CacheWarmupRunner` 콘솔 어플리케이션이 자동으로 `run()` 메서드를 발화한다.
3. 해당 메서드 안에서 2가지 주요 `CommonCodeService` 메서드를 단발성으로 호출하게 된다.
   - `findAllGroups()` -> 데이터베이스를 찔러 최초 쿼리 정보를 Redis에 올린다.
   - `findAllDetailsByGroup("COMMON")` -> 또 한 번 빈번한 공통 디테일을 Redis에 올린다.
4. 이 작업이 성공적으로 수행되면 로거가 `Redis cache warm-up completed successfully.` 를 찍는다.
5. 이후 유저 접속 시, 해당 데이터는 별도의 DB Read(Cold Start) 지연 1ms 없이 즉시 Redis 메모리로부터 렌더링되게 된다.
