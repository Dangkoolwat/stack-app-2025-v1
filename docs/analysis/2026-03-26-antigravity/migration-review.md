---
agent: Antigravity (Gemini)
created_at: 2026-03-26 (수)
language: ko
---

# Spring Boot 4 테스트 환경 마이그레이션 가이드 리뷰

## 가이드 요약

가이드 문서는 JHipster 9 기반 Spring Boot 4 테스트 환경으로의 전환을 안내하며, 핵심 방향은 레거시 `ContextCustomizerFactory` + `spring.factories` 패턴을 제거하고 `@ServiceConnection` + `@Testcontainers`로 대체하는 것입니다.

---

## 현재 프로젝트 상태 (사실 기반)

| 항목 | 현재 상태 |
|------|----------|
| Spring Boot 버전 | 4.0.4 |
| `spring-boot-testcontainers` | 미추가 |
| `testcontainers-jdbc` | pom.xml에 존재 |
| `spring.factories` | 존재 (SQL + Redis ContextCustomizerFactory 등록) |
| `@IntegrationTest` | `@EmbeddedRedis` + `@EmbeddedSQL` 조합으로 구성 |
| `@ActiveProfiles` | `EmbeddedSQL.java`에서 `{"testdev", "test"}` 활성화 |
| 테스트 프로파일 | `application-testdev.yml`, `application-testprod.yml` 사용, `application-test.yml` 없음 |
| Oracle 컨테이너 | `gvenzl/oracle-xe:21-slim-faststart`, 비밀번호 `oracle`, 타임아웃 300초 |
| Redis 컨테이너 | `redis:8.0.0` |
| `JacksonConfiguration` | 존재 (Jackson 3 Hibernate MixIn + 커스텀 introspector) |
| `AsyncSyncConfiguration` | 존재 (`@Configuration`, `SyncTaskExecutor`) |
| `SpringBootTestClassOrderer` | 존재, `junit-platform.properties`에 등록 완료 |
| `LiquibaseTestConfiguration` | 존재 |
| 통합 테스트 클래스 수 | 약 30개 이상 |

---

## 1. 가이드에 동의하는 부분

- `spring-boot-testcontainers` 모듈 추가: Spring Boot 4.0.4에서 공식 지원하며 타당
- `testcontainers-jdbc` 제거: `@ServiceConnection` 사용 시 JDBC URL 자동 주입으로 불필요
- 레거시 `ContextCustomizerFactory` + `spring.factories` 제거: 복잡도/유지보수 부담 해소
- `@Testcontainers` + `@Container` + `@ServiceConnection` 패턴: Spring Boot 공식 방식
- `SpringBootTestClassOrderer` 유지: 이미 `junit-platform.properties`에 등록 완료

---

## 2. 보완이 필요한 항목

### 2-1. Oracle 컨테이너 비밀번호 불일치

- 가이드: `withPassword("testpass")`, `withUsername("testuser")`
- 프로젝트: `withPassword("oracle")`, `withEnv("ORACLE_PASSWORD", "oracle")`, username 미지정
- `application-testdev.yml`의 datasource 설정과 정합성 확인 필수

### 2-2. Redis 이미지 버전 불일치

- 가이드: `redis:8.6.1` / 프로젝트: `redis:8.0.0`
- Redis 8.x minor 버전이므로 큰 문제 없으나 결정 사항을 명시할 것

### 2-3. Redis `@ServiceConnection` 비호환 (위험도: 높음)

이 프로젝트는 Redis 연결에 `spring.data.redis.*`가 아닌 Redisson + JHipster `jhipster.cache.redis.server` 프로퍼티를 사용합니다. `@ServiceConnection`은 이 커스텀 프로퍼티를 자동 주입하지 않습니다.

- Oracle: `@ServiceConnection`으로 완전 자동화 가능
- Redis: `@DynamicPropertySource` 방식 필수

가이드에서 "Oracle은 `@ServiceConnection`, Redis는 `@DynamicPropertySource`"로 이원화 필수.

### 2-4. `@ActiveProfiles` 설정 누락 (위험도: 높음)

가이드의 `@IntegrationTest` 변경 코드에 `@ActiveProfiles`가 없습니다.
현재 `EmbeddedSQL.java`에서 `@ActiveProfiles({"testdev", "test"})`를 활성화하며, 삭제 시 프로파일 미활성화로 전체 테스트 실패 가능성이 높습니다.

### 2-5. Oracle `withStartupTimeoutSeconds(300)` 설정 누락

현재 `TestContainer.java`에 300초 타임아웃 설정이 있으나 가이드 코드에는 없습니다.
CI 환경 안정성을 위해 `.withStartupTimeout(Duration.ofSeconds(300))` 추가 권장.

### 2-6. 삭제 대상 목록에 `TestContainer.java` 누락 (위험도: 높음)

`SqlTestContainersSpringContextCustomizerFactory`가 동적 로딩하는 Oracle 구현체입니다.
삭제 대상에 `TestContainer.java`를 추가해야 합니다.

### 2-7. `logback.xml` 레거시 참조 정리

`logback.xml` 53번째 줄에 `com.daangcool.stack.config.RedisTestContainer` 로거 설정이 있습니다.
`RedisTestContainer.java` 삭제 후 함께 제거해야 합니다.

### 2-8. `withReuse(true)` 전제조건

`~/.testcontainers.properties`에 `testcontainers.reuse.enable=true` 설정 필수.
개발자/CI 환경 모두 체크리스트에 추가할 것.

### 2-9. `spring.factories` 파일 확인

현재 SQL/Redis ContextCustomizerFactory만 등록된 상태이므로 파일 전체 삭제 가능.

### 2-10. `withDatabaseName("ORCLTEST")` 정합성

프로젝트와 가이드 모두 `ORCLTEST` 사용으로 일치.
`@ServiceConnection` 사용 시 `application-testdev.yml`의 datasource URL과 충돌 여부 확인 필요.

---

## 3. JHipster 9에서 차용 가능한 개선 사항

### 3-1. `@SpringBootTest(classes=...)` 구성 변경

JHipster 9 공식 템플릿 확인 결과:

```java
@SpringBootTest(classes = {StackApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class})
```

프로젝트의 `JacksonConfiguration`은 Jackson 3 Hibernate MixIn과 커스텀 AnnotationIntrospector를 포함하므로 테스트 컨텍스트에 명시적 포함이 타당합니다. `LiquibaseTestConfiguration`은 JHipster 9 기본에는 없으나 이 프로젝트에서 필요하므로 유지:

```java
@SpringBootTest(classes = {
    StackApp.class,
    JacksonConfiguration.class,
    AsyncSyncConfiguration.class,
    LiquibaseTestConfiguration.class
})
```

### 3-2. `AsyncSyncConfiguration` 어노테이션 변경

- JHipster 9: `@TestConfiguration(proxyBeanMethods = false)`
- 현재 프로젝트: `@Configuration`

`@TestConfiguration`은 테스트 전용 설정 클래스에 적합하며, `proxyBeanMethods = false`(lite mode)는 성능을 개선합니다. 마이그레이션 시 함께 변경 권장.

### 3-3. 레거시 파일 완전 제거 목록

JHipster 9에서 확인된 삭제 대상 전체 목록:
- `SqlTestContainersSpringContextCustomizerFactory.java`
- `RedisTestContainersSpringContextCustomizerFactory.java`
- `SqlTestContainer.java` (interface)
- `TestContainer.java` (Oracle 구현체)
- `EmbeddedSQL.java`
- `EmbeddedRedis.java`
- `RedisTestContainer.java`
- `META-INF/spring.factories`

### 3-4. 인터페이스 기반 컨테이너 공유 패턴

JHipster 9는 인터페이스 기반 Testcontainers 공유 패턴을 사용합니다.
현 프로젝트에서는 모든 IT가 Oracle+Redis를 함께 사용하므로 애노테이션 내장 방식이 더 간결합니다.
향후 확장 필요 시 인터페이스 패턴 전환을 고려할 수 있습니다.

---

## 4. 비교 요약

| 항목 | JHipster 9 | 가이드 (PDF) | 권장 |
|------|-----------|-------------|------|
| `@IntegrationTest` classes | `MainApp`, `JacksonConfig`, `AsyncSyncConfig` | `StackApp`, `LiquibaseTestConfig`, `JacksonConfig`, `AsyncSyncConfig` | 가이드 + `LiquibaseTestConfig` 유지 |
| `AsyncSyncConfiguration` | `@TestConfiguration(proxyBeanMethods = false)` | 언급 없음 | JHipster 9 방식으로 변경 |
| Oracle 연결 | `@DynamicPropertySource` | `@ServiceConnection` | `@ServiceConnection` |
| Redis 연결 | `@DynamicPropertySource` | `@ServiceConnection` (섹션3) | `@DynamicPropertySource` (필수) |
| 컨테이너 타임아웃 | 없음 | 없음 | `Duration.ofSeconds(300)` 추가 |
| 프로파일 관리 | 별도 | `@ActiveProfiles({"test"})` | `@ActiveProfiles({"testdev", "test"})` 필수 |

---

## 5. 결론

가이드의 전체 방향은 올바르며 JHipster 9의 설계와 일치합니다.

가장 중요한 보완 항목 (우선순위순):
1. Redis `@ServiceConnection` 비호환 -> `@DynamicPropertySource` 필수 (2-3)
2. `@ActiveProfiles({"testdev", "test"})` 누락 (2-4)
3. `TestContainer.java` 삭제 대상 누락 (2-6)
4. Oracle 타임아웃 설정 누락 (2-5)
5. `AsyncSyncConfiguration` -> `@TestConfiguration` 변경 (3-2)
6. `JacksonConfiguration` 포함 근거 확인 완료 (3-1)
