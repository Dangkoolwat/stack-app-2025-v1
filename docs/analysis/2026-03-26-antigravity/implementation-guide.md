---
agent: Antigravity (Gemini)
created_at: 2026-03-26 (수)
language: ko
---

# Spring Boot 4 테스트 환경 마이그레이션 - 구현 가이드

이 문서는 [migration-review.md](./migration-review.md)의 분석 결과를 기반으로 작성된 구현 가이드입니다.

---

## 전제 조건

- [ ] Docker Desktop 실행 중
- [ ] `~/.testcontainers.properties`에 `testcontainers.reuse.enable=true` 설정 확인
- [ ] Git feature branch 생성 (`feature/sb4-test-migration`)

---

## Phase 1: 의존성 변경

### 1-1. `spring-boot-testcontainers` 추가

`pom.xml`의 Test 섹션에 추가:

```xml
<!-- Spring Boot Testcontainers 통합 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

### 1-2. `testcontainers-jdbc` 제거

`pom.xml`에서 아래 블록 삭제:

```xml
<!-- Testcontainers JDBC -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-jdbc</artifactId>
    <scope>test</scope>
</dependency>
```

### 1-3. 검증

```bash
./mvnw dependency:resolve -DincludeScope=test
```

---

## Phase 2: AsyncSyncConfiguration 변경

### 변경 대상

`src/test/java/com/daangcool/stack/config/AsyncSyncConfiguration.java`

### Before

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncSyncConfiguration {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return new SyncTaskExecutor();
    }
}
```

### After (JHipster 9 방식)

```java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import java.util.concurrent.Executor;

@TestConfiguration(proxyBeanMethods = false)
public class AsyncSyncConfiguration {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return new SyncTaskExecutor();
    }
}
```

### 변경 이유

- `@TestConfiguration`은 테스트 전용 설정임을 명확히 표현
- `proxyBeanMethods = false`는 경량 모드로 테스트 컨텍스트 로딩 성능 개선
- JHipster 9 공식 템플릿과 동일한 패턴

---

## Phase 3: `@IntegrationTest` 재작성

### 변경 대상

`src/test/java/com/daangcool/stack/IntegrationTest.java`

### Before

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {StackApp.class, LiquibaseTestConfiguration.class})
@EmbeddedRedis
@EmbeddedSQL
public @interface IntegrationTest {
}
```

### After

```java
package com.daangcool.stack;

import com.daangcool.stack.config.AsyncSyncConfiguration;
import com.daangcool.stack.config.JacksonConfiguration;
import com.daangcool.stack.config.LiquibaseTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

/**
 * 통합 테스트 기반 애노테이션 (Spring Boot 4 + Testcontainers)
 *
 * - Oracle: @ServiceConnection 으로 자동 프로퍼티 주입
 * - Redis: @DynamicPropertySource 로 jhipster.cache.redis.server 수동 주입
 *   (Redisson 기반이므로 @ServiceConnection 비호환)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {
    StackApp.class,
    JacksonConfiguration.class,
    AsyncSyncConfiguration.class,
    LiquibaseTestConfiguration.class
})
@ActiveProfiles({"testdev", "test"})
@Testcontainers
public @interface IntegrationTest {

    // Oracle: @ServiceConnection 으로 datasource 프로퍼티 자동 주입
    @Container
    @ServiceConnection
    OracleContainer oracle = new OracleContainer(
        DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
        .withDatabaseName("ORCLTEST")
        .withPassword("oracle")
        .withEnv("ORACLE_PASSWORD", "oracle")
        .withStartupTimeout(Duration.ofSeconds(300))
        .withReuse(true);

    // Redis: Redisson(jhipster.cache.redis.server) 사용으로 @ServiceConnection 불가
    @Container
    GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:8.0.0"))
        .withExposedPorts(6379)
        .withReuse(true);

    // Redis 프로퍼티 수동 등록
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("jhipster.cache.redis.server",
            () -> String.format("redis://%s:%d",
                redis.getHost(), redis.getMappedPort(6379)));
    }
}
```

### 핵심 설계 결정

| 항목 | 결정 | 근거 |
|------|------|------|
| Oracle 연결 | `@ServiceConnection` | Spring Boot 공식 JDBC 자동 설정 지원 |
| Redis 연결 | `@DynamicPropertySource` | Redisson + `jhipster.cache.redis.server` 사용으로 `@ServiceConnection` 비호환 |
| 프로파일 | `{"testdev", "test"}` | 기존 `EmbeddedSQL.java`와 동일 유지, `application-testdev.yml` 활성화 필수 |
| Oracle 비밀번호 | `oracle` | 기존 `TestContainer.java`와 동일 유지 |
| Redis 버전 | `8.0.0` | 기존 프로젝트와 동일 유지 (안정성 우선) |
| Oracle 타임아웃 | 300초 | CI 환경 안정성 확보 |

> [주의] `@DynamicPropertySource`는 인터페이스의 `static` 메서드로 선언해야 합니다.
> 애노테이션(annotation)이 아닌 인터페이스 내부에서 사용하므로, 실제 컴파일 시 이 패턴이 정상 동작하는지 Phase 4에서 반드시 검증해야 합니다.
> 만약 애노테이션 내부에서 `@DynamicPropertySource`가 동작하지 않는 경우, 아래 대안을 적용합니다.

### 대안: 별도 Configuration 클래스 분리

애노테이션 내 `@DynamicPropertySource`가 동작하지 않을 경우:

```java
// src/test/java/com/daangcool/stack/config/TestcontainersConfiguration.java

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

    @Container
    @ServiceConnection
    static OracleContainer oracle = new OracleContainer(
        DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
        .withDatabaseName("ORCLTEST")
        .withPassword("oracle")
        .withEnv("ORACLE_PASSWORD", "oracle")
        .withStartupTimeout(Duration.ofSeconds(300))
        .withReuse(true);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:8.0.0"))
        .withExposedPorts(6379)
        .withReuse(true);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("jhipster.cache.redis.server",
            () -> String.format("redis://%s:%d",
                redis.getHost(), redis.getMappedPort(6379)));
    }
}
```

이 경우 `@IntegrationTest`는 다음과 같이 단순화됩니다:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {
    StackApp.class,
    JacksonConfiguration.class,
    AsyncSyncConfiguration.class,
    LiquibaseTestConfiguration.class,
    TestcontainersConfiguration.class
})
@ActiveProfiles({"testdev", "test"})
public @interface IntegrationTest {
}
```

---

## Phase 4: 단계적 검증

### 4-1. 단일 테스트 검증

레거시 파일을 삭제하기 전에 먼저 1개의 IT를 새 방식으로 실행합니다:

```bash
export $(xargs < .env) && ./mvnw test \
  -Dtest=AccountResourceIT \
  -pl . \
  -Dspring.profiles.active=testdev,test
```

확인 사항:
- [ ] Oracle 컨테이너 정상 시작
- [ ] Redis 컨테이너 정상 시작
- [ ] Liquibase 마이그레이션 정상 실행
- [ ] 테스트 통과

### 4-2. 전체 통합 테스트 실행

```bash
export $(xargs < .env) && ./mvnw clean test
```

확인 사항:
- [ ] 모든 unit test 통과
- [ ] 모든 integration test 통과
- [ ] 컨테이너 재사용 동작 확인 (두 번째 실행 시 시작 시간 단축)

---

## Phase 5: 레거시 파일 삭제

### 삭제 대상 (8개 파일)

```
src/test/java/com/daangcool/stack/config/
  SqlTestContainersSpringContextCustomizerFactory.java
  RedisTestContainersSpringContextCustomizerFactory.java
  SqlTestContainer.java
  TestContainer.java
  EmbeddedSQL.java
  EmbeddedRedis.java
  RedisTestContainer.java

src/test/resources/META-INF/
  spring.factories
```

### logback.xml 정리

`src/test/resources/logback.xml`에서 아래 줄 삭제:

```xml
<logger name="com.daangcool.stack.config.RedisTestContainer" level="WARN"/>
```

---

## Phase 6: 최종 검증

### 6-1. 클린 빌드

```bash
export $(xargs < .env) && ./mvnw clean test
```

### 6-2. 삭제된 클래스 참조 확인

```bash
grep -r "EmbeddedSQL\|EmbeddedRedis\|SqlTestContainer\|RedisTestContainer\|spring\.factories" \
  src/test/ --include="*.java" --include="*.xml" --include="*.properties"
```

참조가 남아있으면 안 됩니다.

### 6-3. 확인 체크리스트

- [ ] 모든 테스트 통과 (unit + integration)
- [ ] 삭제된 클래스에 대한 참조 없음
- [ ] `ClassNotFoundException` 없음
- [ ] Oracle 컨테이너 `withReuse(true)` 정상 동작
- [ ] Redis 프로퍼티 (`jhipster.cache.redis.server`) 정상 주입
- [ ] `application-testdev.yml` 프로파일 정상 활성화

---

## Phase 7: 커밋

```
refactor(test): migrate test infrastructure to Spring Boot 4 Testcontainers

- spring-boot-testcontainers 모듈 추가, testcontainers-jdbc 제거
- @IntegrationTest를 @ServiceConnection(Oracle) + @DynamicPropertySource(Redis) 기반으로 재작성
- AsyncSyncConfiguration을 @TestConfiguration(proxyBeanMethods=false)로 변경
- 레거시 ContextCustomizerFactory, spring.factories 등 8개 파일 삭제
- logback.xml 레거시 참조 정리

Agent: Antigravity (Gemini)
see docs/analysis/2026-03-26-antigravity/
```

---

## 롤백 계획

문제 발생 시:
1. `git stash` 또는 `git checkout` 으로 feature branch 변경 이전 상태로 복원
2. 레거시 파일이 복원되면 기존 `spring.factories` 기반 체계가 즉시 동작
3. `spring-boot-testcontainers` 의존성 제거만으로도 신규 체계 비활성화 가능

---

## 파일 변경 요약

| 작업 | 파일 | 변경 타입 |
|------|------|----------|
| 의존성 추가 | `pom.xml` | MODIFY |
| 의존성 제거 | `pom.xml` | MODIFY |
| 어노테이션 변경 | `AsyncSyncConfiguration.java` | MODIFY |
| 전면 재작성 | `IntegrationTest.java` | MODIFY |
| 레거시 참조 제거 | `logback.xml` | MODIFY |
| 신규 (대안 적용 시) | `TestcontainersConfiguration.java` | NEW |
| 삭제 | `SqlTestContainersSpringContextCustomizerFactory.java` | DELETE |
| 삭제 | `RedisTestContainersSpringContextCustomizerFactory.java` | DELETE |
| 삭제 | `SqlTestContainer.java` | DELETE |
| 삭제 | `TestContainer.java` | DELETE |
| 삭제 | `EmbeddedSQL.java` | DELETE |
| 삭제 | `EmbeddedRedis.java` | DELETE |
| 삭제 | `RedisTestContainer.java` | DELETE |
| 삭제 | `spring.factories` | DELETE |
