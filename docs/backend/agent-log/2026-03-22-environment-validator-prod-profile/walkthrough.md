# 실행 흐름 (Walkthrough)

1. `java -jar stack-2.0.0.jar --spring.profiles.active=prod` 명령어나 Docker 컨테이너에서 상용 시작이 지시됨.
2. Spring Boot 초기 Dependency Injection 중, 컴포넌트 스캔 레이어가 `@Profile("prod")` 에 의해 `EnvironmentValidator` 클래스를 Bean으로 생성 허용.
3. `@Value`가 파싱되면서 `.env` 내부 변수나 OS 환경 변수를 주입. 값이 없으면 빈 문자열(`""`)로 갈음.
4. `ApplicationRunner.run()` 인터페이스가 모든 Bean 초기화 후 즉각 동작 개시.
5. 등록된 `jwtSecret`, `datasourceUrl`, `datasourceUsername`, `datasourcePassword` 에 대한 `Assert.hasText` 검사를 진행함.
6. 이 중 하나라도 값이 비어있을 경우 `IllegalArgumentException`이 던져지고, 로그에 직관적인 "❌ (환경 변수) must be set in production" 에러를 남긴 후 즉각(Fail-fast) 어플리케이션은 종료 코드와 함께 사망함. 
7. 검사가 모두 정상 통과되면 `✅ Production environment validation passed safely.` 문구가 남으며 서비스 개통이 성공함.
