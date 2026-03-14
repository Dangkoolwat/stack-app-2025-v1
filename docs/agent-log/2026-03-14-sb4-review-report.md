# Spring Boot 4.0 Migration — 최적화 · 성능 · 보안 검토 보고서

**대상 프로젝트:** `stack-app-2025-v1` (com.daangcool:stack:2.0.0)  
**스택:** Spring Boot 4.0.3 · Java 21 · Oracle DB · Redis/Redisson 4.3.0 · Liquibase 4.31.0  
**검토일:** 2026-03-14

---

## 요약

| 심각도 | 건수 |
|--------|------|
| 🔴 Critical | 3 |
| 🟠 High | 4 |
| 🟡 Medium | 5 |
| 🟢 Low / 개선 | 5 |

---

## 🔴 Critical — 즉시 조치 필요

### C-1. 운영(prod) JWT Secret이 개발(dev)과 동일

**파일:** `application-dev.yml` L116, `application-prod.yml` L121

두 파일 모두 동일한 `base64-secret` 값을 사용하고 있습니다.  
공격자가 개발 시크릿을 획득하면 운영 JWT 토큰 위·변조가 가능합니다.

```yaml
# application-prod.yml — 반드시 별도의 강력한 시크릿으로 교체
jhipster:
  security:
    authentication:
      jwt:
        base64-secret: ${JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET}
```

> **권장:** 환경변수 또는 Vault(HashiCorp, OCI Vault 등)를 통한 외부 주입. 교체 후 기존 토큰 전체 무효화(Redis TTL 또는 블랙리스트 적용) 필요.

---

### C-2. 운영 DB 자격증명 소스코드에 하드코딩

**파일:** `application-prod.yml` L37–38, `pom.xml` L789–790

```yaml
# application-prod.yml
username: system
password: oracle      # ← 운영 평문 패스워드 직접 노출
```

```xml
<!-- pom.xml dev profile -->
<liquibase-plugin.username>dizzyUncle</liquibase-plugin.username>
<liquibase-plugin.password>Docker#2020</liquibase-plugin.password>
```

> **권장:** Spring Cloud Config, OCI Vault, 또는 OS 환경변수(`SPRING_DATASOURCE_PASSWORD`)를 통한 분리.  
> `application-secret.yml`(`.gitignore` 등록)에만 기입하거나, CI/CD 파이프라인의 시크릿 변수로 주입.

---

### C-3. 운영 프로파일에서 `hibernate.ddl-auto: update` 사용

**파일:** `application-prod.yml` L51–52

```yaml
jpa:
  properties:
    hibernate:
      ddl-auto: update    # ← 운영 환경 절대 금지
  generate-ddl: true
```

`ddl-auto: update`는 운영 DB에 예기치 않은 스키마 변경을 일으킬 수 있으며, 롤백 불가 위험이 있습니다.  
Liquibase가 이미 사용 중이므로 DDL 자동 관리를 완전히 비활성화해야 합니다.

```yaml
jpa:
  properties:
    hibernate:
      ddl-auto: none    # Liquibase가 스키마 관리
  generate-ddl: false
```

---

## 🟠 High — 조속한 개선 필요

### H-1. Redisson 클라이언트 이중 생성 (메모리·연결 낭비)

**파일:** `CacheConfiguration.java`

`jcacheConfiguration()` 빈과 `redissonClient()` 빈이 각각 `Redisson.create(config)`를 호출하여 **두 개의 독립적인 Redis 커넥션 풀**이 생성됩니다. 운영 환경에서 Redis 연결 수가 2배가 됩니다.

```java
// 개선: jcacheConfiguration이 redissonClient 빈을 재사용하도록 변경
@Bean
public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(
        RedissonClient redissonClient, JHipsterProperties props) {
    MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();
    jcacheConfig.setStatisticsEnabled(true);
    jcacheConfig.setExpiryPolicyFactory(
        CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS,
            props.getCache().getRedis().getExpiration())));
    return RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);
}
```

---

### H-2. Virtual Threads 활성화와 ThreadPoolTaskExecutor 혼용

**파일:** `application.yml` L103, `AsyncConfiguration.java`

```yaml
spring:
  threads:
    virtual:
      enabled: true    # 가상 스레드 활성화
```

```java
// AsyncConfiguration.java — 플랫폼 스레드 기반 풀 생성
ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());  // 2
executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());    // 50
```

Spring Boot 4.x의 가상 스레드 모드에서는 별도로 `ThreadPoolTaskExecutor`를 생성하면 가상 스레드의 이점을 살리지 못합니다.

```java
// 개선: 가상 스레드를 그대로 사용하도록 AsyncConfiguration 단순화
@Override
@Bean(name = "taskExecutor")
public Executor getAsyncExecutor() {
    return new ExceptionHandlingAsyncTaskExecutor(
        Executors.newVirtualThreadPerTaskExecutor());
}
```

---

### H-3. 파일 다운로드 시 전체 바이트 배열 메모리 로드

**파일:** `UploadResource.java`

```java
byte[] data = storageService.loadAsResource(upload.getFilePath()); // 전체를 byte[]로 로드
```

대용량 파일 다운로드 시 JVM 힙 메모리 고갈 위험이 있습니다.

```java
// 개선: StreamingResponseBody 반환
@GetMapping("/{id}/download")
public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Long id) {
    Upload upload = ...;
    StreamingResponseBody stream = outputStream ->
        storageService.streamTo(upload.getFilePath(), outputStream);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
        .contentType(MediaType.parseMediaType(upload.getMimeType()))
        .body(stream);
}
```

---

### H-4. `BearerTokenResolver.setAllowUriQueryParameter(true)` 보안 위협

**파일:** `SecurityJwtConfiguration.java`

```java
bearerTokenResolver.setAllowUriQueryParameter(true);
```

URL 쿼리스트링으로 JWT 전달을 허용하면 토큰이 브라우저 히스토리, 서버 액세스 로그, 리퍼러 헤더에 노출될 수 있습니다.

```java
bearerTokenResolver.setAllowUriQueryParameter(false); // 보안 개선
```

---

## 🟡 Medium

### M-1. 운영 HikariCP 풀 미설정 (연결 제한 없음)

**파일:** `application-prod.yml`

```yaml
hikari:
  poolName: Hikari
  auto-commit: false
  # maximumPoolSize, minimumIdle, connectionTimeout 등 미설정
```

```yaml
# 권장 설정
hikari:
  auto-commit: false
  maximum-pool-size: 20
  minimum-idle: 5
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
  keepalive-time: 30000
```

---

### M-2. Content Security Policy에 `unsafe-inline` 포함

**파일:** `application.yml`

```yaml
content-security-policy: "... script-src 'self' 'unsafe-inline' https://storage.googleapis.com ..."
```

`'unsafe-inline'`은 XSS 방어 효과를 크게 약화시킵니다.

> **권장:** `script-src`에서 `'unsafe-inline'` 제거. `style-src`는 Vue SFC 런타임 주입 필요 시 유지, nonce/hash 기반 CSP 전환 권장.

---

### M-3. Prometheus 메트릭 운영 비활성화

**파일:** `application-prod.yml`

```yaml
management:
  prometheus:
    metrics:
      export:
        enabled: false   # 운영에서 Prometheus 꺼짐
```

운영 모니터링(Grafana 연동 등)이 필요하다면 `true`로 설정하되, `/management/prometheus` 엔드포인트 접근을 내부망으로 제한해야 합니다.

---

### M-4. `DomainUserDetailsService`의 이메일 유효성 검사에 Hibernate Validator 내부 클래스 사용

**파일:** `DomainUserDetailsService.java`

```java
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
if (new EmailValidator().isValid(login, null)) {
```

`internal` 패키지는 공개 API가 아니므로 버전 업그레이드 시 깨질 수 있습니다.

```java
// 개선
private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
if (EMAIL_PATTERN.matcher(login).matches()) { ...
```

---

### M-5. 캐시 항목 기준 TTL이 단일 값으로 통일 (세분화 불가)

**파일:** `CacheConfiguration.java`

`buildTTLConfig()` 메서드가 준비되어 있으나, 모든 캐시가 전역 TTL(3600초)를 사용합니다.

> **권장:** 변경 빈도가 낮은 캐시(공통코드, 태그 등)는 더 긴 TTL(예: 24시간), 동적 데이터는 짧은 TTL 적용.

---

## 🟢 Low / 개선 사항

### L-1. 로깅 레벨 과다 (개발 환경 TRACE)

**파일:** `application-dev.yml`

Hibernate 6.x 환경에서 `BasicBinder` 로거는 불필요. TRACE 레벨 제거 또는 DEBUG로 조정.

---

### L-2. `spring.main.allow-bean-definition-overriding: true` 설정

**파일:** `application.yml`

빈 정의 오버라이딩은 예기치 못한 빈 교체를 숨길 수 있습니다. Spring Boot 4.x 기본값 `false`. 어떤 빈이 오버라이딩되는지 파악 후 `@Primary`로 전환 권장.

---

### L-3. `encodeFilename` User-Agent 기반 분기 — 현대 브라우저에서 단순화 가능

**파일:** `UploadResource.java`

MSIE/Trident 분기 제거 후 RFC 5987 단일 방식 적용.

```java
headers.set(HttpHeaders.CONTENT_DISPOSITION,
    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
```

---

### L-4. `UploadResource.downloadPrivateFile`의 오류 응답 타입 불일치

**파일:** `UploadResource.java`

RFC7807 `ProblemDetail`을 통한 일관된 에러 응답 형식 권장.

---

### L-5. `@SuppressWarnings("deprecation")` 해소 필요

**파일:** `CacheConfiguration.java`

Redisson `setPassword()` deprecated. Redisson 4.x API에서 `withPassword()` 체이닝으로 전환 검토.

---

## 액션 플랜 요약

| 우선순위 | 항목 | 파일 |
|----------|------|------|
| 즉시 | JWT Secret 환경변수화 | `application-prod.yml` |
| 즉시 | DB 자격증명 외부화 | `application-prod.yml`, `pom.xml` |
| 즉시 | `ddl-auto: none` 적용 | `application-prod.yml` |
| 단기 | Redisson 이중 생성 제거 | `CacheConfiguration.java` |
| 단기 | `AsyncConfiguration` 가상 스레드 대응 | `AsyncConfiguration.java` |
| 단기 | 파일 다운로드 스트리밍 전환 | `UploadResource.java` |
| 단기 | `allowUriQueryParameter false` | `SecurityJwtConfiguration.java` |
| 중기 | HikariCP 운영 풀 사이즈 명시 | `application-prod.yml` |
| 중기 | CSP `unsafe-inline` 제거 | `application.yml` |
| 중기 | 캐시 TTL 세분화 | `CacheConfiguration.java` |

---

## 검증 방법

```bash
# 빌드 전체 검증
./mvnw clean package -DskipTests

# 단위/통합 테스트
./mvnw test

# 보안 의존성 취약점 체크 (OWASP Dependency Check 플러그인 추가 권장)
./mvnw verify -Powasp-check
```
