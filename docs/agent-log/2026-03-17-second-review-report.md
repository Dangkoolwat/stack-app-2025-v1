# Spring Boot 4.0 — 2차 전체 점검 보고서

**대상 프로젝트:** `stack-app-2025-v1` (com.daangcool:stack:2.0.0)  
**스택:** Spring Boot 4.0.3 · Java 21 · Oracle DB · Redis/Redisson 4.3.0 · Liquibase 4.31.0 · Bucket4j 8.10.1  
**검토일:** 2026-03-17  
**기준:** 1차 보고서(`2026-03-14-sb4-review-report.md`) 이후 변경사항 반영 + 신규 발견 이슈  

---

## 1차 점검 조치 현황 요약

| 항목 | 상태 | 비고 |
|------|------|------|
| C-1. JWT Secret 동일화 | ⚠️ 부분 해결 | 환경변수화는 되었으나 기본값(fallback)이 동일 |
| C-2. DB 자격증명 하드코딩 | ✅ 해결 | `${SPRING_DATASOURCE_*}` 환경변수 전환 |
| C-3. `ddl-auto: update` 운영 사용 | ✅ 해결 | `ddl-auto: none` 적용 |
| H-1. Redisson 이중 생성 | ✅ 해결 | `fromInstance()` 재사용 + `destroyMethod="shutdown"` |
| H-2. Virtual Threads 혼용 | ✅ 해결 | `SimpleAsyncTaskExecutorBuilder` 이용 virtual thread executor |
| H-3. 파일 다운로드 메모리 로드 | ✅ 해결 | `StreamingResponseBody` + `loadAsStream()` 전환 |
| H-4. URI 쿼리 파라미터 JWT 노출 | ✅ 해결 | `setAllowUriQueryParameter(false)` |
| M-1. HikariCP 풀 미설정 | ✅ 해결 | 상세 풀 옵션 명시 완료 |
| M-2. CSP `unsafe-inline` | ✅ 부분 해결 | `script-src`에서 제거, `style-src`는 유지 |
| M-3. Prometheus 운영 비활성화 | ❌ 미해결 | prod에 여전히 `enabled: false` |
| M-4. Hibernate Validator 내부 클래스 | ✅ 해결 | `Constants.LOGIN_REGEX` Pattern 사용 |
| M-5. 캐시 TTL 단일화 | ✅ 해결 | 정적 24h / 동적 1h 세분화 |
| L-3. User-Agent 분기 | ✅ 해결 | RFC 5987 단일 방식 |
| L-5. `setPassword()` deprecated | ✅ 해결 | URL 내 password 포함 방식 전환 |

---

## 2차 신규 발견 이슈

| 심각도 | 건수 |
|--------|------|
| 🔴 Critical | 2 |
| 🟠 High | 4 |
| 🟡 Medium | 6 |
| 🟢 Low | 5 |

---

## 🔴 Critical — 즉시 조치 필요

### NC-1. JWT Secret 기본값(fallback)이 dev·prod 동일 — 완전 미해결

**파일:** `src/main/resources/config/application-dev.yml` L155, `application-prod.yml` L177

```yaml
# application-dev.yml L155
base64-secret: ${JWT_SECRET:N2U2YTUwODQ2MjI5  }   # ← trailing space 포함

# application-prod.yml L177
base64-secret: ${JWT_SECRET:N2U2YTUwODQ2MjI5  }   # ← 완전히 동일
```

환경변수 `JWT_SECRET`이 **설정되지 않으면** 양 환경이 동일한 base64 시크릿으로 동작합니다.  
운영 서버에서 해당 환경변수 누락 시, 개발 시크릿으로 서명된 토큰이 운영에서 검증 통과됩니다.

> **권장 조치:**
> 1. prod 기본값 fallback을 **완전히 제거**하거나 의도적으로 실패하도록 설정:
> ```yaml
> base64-secret: ${JWT_SECRET}   # fallback 없이 — 환경변수 누락 시 시작 실패
> ```
> 2. `dev` 전용 시크릿은 별도의 짧은 개발용 값을 사용하되 prod 값과 절대 동일하지 않게 유지.
> 3. CI/CD 파이프라인에서 `JWT_SECRET` 주입 여부를 배포 전 검증하는 게이트 추가.

---

### NC-2. `EmailOtpService.recordLog()` — `@Transactional(REQUIRES_NEW)` 무효화 (Self-Invocation 문제)

**파일:** `src/main/java/com/daangcool/stack/service/otp/EmailOtpService.java` L169–184

```java
@Service
@Transactional        // ← 클래스 레벨 트랜잭션
public class EmailOtpService {

    // ...

    @Transactional(propagation = Propagation.REQUIRES_NEW)  // ← AOP 프록시 미통과
    protected void recordLog(...) {
        // 주 트랜잭션 실패 시에도 독립적으로 커밋되어야 하는 감사 로그
        emailOtpLogRepository.save(logEntity);
    }
}
```

Spring AOP는 **동일 빈 내부 메서드 호출(self-invocation)**을 프록시로 가로채지 못합니다.  
`requestOtp()` → `recordLog()` 호출 시 `REQUIRES_NEW`가 무시되고 **동일 트랜잭션 내에서 실행**됩니다.  
결과적으로 주 트랜잭션이 롤백되면 감사 로그(`EmailOtpLog`)도 함께 롤백되어 **보안 감사 로그의 신뢰성이 저하**됩니다.

> **권장 조치 (A안 — 권장):** `recordLog` 책임을 별도 스프링 빈으로 분리
> ```java
> @Service
> public class OtpLogService {
>     @Transactional(propagation = Propagation.REQUIRES_NEW)
>     public void recordLog(User user, String code, String ip, String ua, String status) {
>         // ...
>     }
> }
> ```
> `EmailOtpService`에 `OtpLogService`를 주입하여 호출하면 AOP 프록시가 정상 작동합니다.

---

## 🟠 High — 조속한 개선 필요

### NH-1. `RateLimitingConfiguration` — Reflection을 통한 내부 API 의존

**파일:** `src/main/java/com/daangcool/stack/config/RateLimitingConfiguration.java` L45–58

```java
Method getCommandExecutor = client.getClass().getMethod("getCommandExecutor");
commandExecutor = (CommandAsyncExecutor) getCommandExecutor.invoke(client);
// ...
commandExecutor = ((Redisson) client).getCommandExecutor();  // 구현체 직접 캐스팅
```

두 가지 심각한 문제가 있습니다:

1. **Reflection으로 `getCommandExecutor()`를 호출**: Redisson 내부 메서드이므로 버전 업그레이드 시 메서드 시그니처 변경으로 `NoSuchMethodException` 런타임 오류 발생 가능.
2. **`((Redisson) client)` 구현체 직접 캐스팅**: 인터페이스(`RedissonClient`) 계약을 위반하며, 데코레이터/프록시 패턴 적용 시 `ClassCastException` 발생.

> **권장 조치:** Bucket4j + Redisson 공식 통합 방식 사용
> ```java
> // Bucket4j 8.x + Redisson 공식 지원 방법
> // pom.xml에서 bucket4j-redis 대신 bucket4j-redisson 사용 검토
> // 또는 RedissonClient에서 RMap을 사용하는 커스텀 구현으로 교체
> RMapCache<String, byte[]> rMapCache = redissonClient.getMapCache("rate-limit-buckets");
> ```
> 만약 Phase 3 구현 방식을 유지할 경우, Redisson 버전을 고정하고 **업그레이드 시 반드시 호환성 검증** 단계를 추가해야 합니다.

---

### NH-2. `RedisMonitoringConfiguration` — 클러스터 모드 미지원

**파일:** `src/main/java/com/daangcool/stack/config/RedisMonitoringConfiguration.java` L60

```java
RedisSingle nodes = redissonClient.getRedisNodes(RedisNodes.SINGLE);  // 싱글 모드 고정
RedisNode node = nodes.getInstance();
```

현재 헬스 인디케이터는 `RedisNodes.SINGLE` 전용입니다.  
`application.redis.cluster=true`로 클러스터 모드 전환 시 **`ClassCastException` 또는 런타임 오류**가 발생합니다.

> **권장 조치:** 클러스터 여부에 따라 분기 처리
> ```java
> @Bean
> public HealthIndicator redisServerHealthIndicator(
>         RedissonClient redissonClient,
>         ApplicationProperties appProps) {
>     return new AbstractHealthIndicator() {
>         @Override
>         protected void doHealthCheck(Health.Builder builder) {
>             if (appProps.getRedis().isCluster()) {
>                 // RedisNodes.CLUSTER 방식으로 헬스 체크
>             } else {
>                 // RedisNodes.SINGLE 방식
>             }
>         }
>     };
> }
> ```

---

### NH-3. OTP 코드 감사 로그에 평문 저장

**파일:** `src/main/java/com/daangcool/stack/service/otp/EmailOtpService.java` L175

```java
logEntity.setOtpCode(code);   // ← OTP 코드 평문 그대로 저장
```

감사 로그(`stack_email_otp_log` 테이블)에 OTP 코드가 **평문**으로 저장됩니다.  
DB 접근 권한이 있는 내부자 또는 DB 탈취 공격자가 사용 전 OTP 코드를 악용할 수 있습니다.

> **권장 조치:** OTP 코드를 마스킹하거나 SHA-256 해쉬로 저장
> ```java
> // 마스킹 방식 (앞 2자리 + ***)
> logEntity.setOtpCode(code.substring(0, 2) + "****");
>
> // 또는 해쉬 방식 (검색은 불가하지만 감사 목적은 충족)
> logEntity.setOtpCode(DigestUtils.sha256Hex(code));
> ```

---

### NH-4. `/management/prometheus` 운영 공개 노출

**파일:** `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java` L129

```java
.requestMatchers(
    "/management/health", "/management/health/**",
    "/management/info", "/management/prometheus"
).permitAll()      // ← 인증 없이 누구나 접근 가능
```

Prometheus 엔드포인트는 JVM 메모리 사용량, 스레드 수, HTTP 요청 경로/카운트, DB 쿼리 지연 등 **시스템 내부 구조를 노출**합니다.  
공격자가 이 정보를 기반으로 타겟 공격을 설계할 수 있습니다.

> **권장 조치 (A안):** ADMIN 권한으로 제한
> ```java
> .requestMatchers("/management/prometheus").hasAuthority(AuthoritiesConstants.ADMIN)
> ```
>
> **권장 조치 (B안):** 내부망/VPN IP 범위로 제한 (인프라 레벨 방화벽 또는 Spring Security IP 필터)
> ```java
> .requestMatchers("/management/prometheus")
>     .access(new IpAddressAuthorizationManager("10.0.0.0/8"))
> ```

---

## 🟡 Medium — 중기 개선 권고

### NM-1. `application.yml` `hibernate.generate_statistics: true` — 전체 프로파일 적용

**파일:** `src/main/resources/config/application.yml` L120

```yaml
spring:
  jpa:
    properties:
      hibernate.generate_statistics: true   # ← prod 포함 전체에 적용
```

`generate_statistics: true`는 Hibernate가 **SQL 실행마다 통계**를 수집하여 성능 오버헤드를 유발합니다.  
이 설정이 `application.yml` 베이스에 있으므로 dev, prod 모두 활성화됩니다.

> **권장 조치:** 개발 환경 전용으로 이동
> ```yaml
> # application.yml에서 제거 후 application-dev.yml로 이동
> spring:
>   jpa:
>     properties:
>       hibernate.generate_statistics: true
> ```

---

### NM-2. Virtual Threads 환경에서 `spring.task.execution.pool` 데드 설정

**파일:** `src/main/resources/config/application.yml` L152–158

```yaml
spring:
  threads:
    virtual:
      enabled: true   # ← Virtual Threads 활성화
  task:
    execution:
      pool:
        core-size: 2
        max-size: 50
        queue-capacity: 10000   # ← Virtual Thread 환경에서 무의미한 설정
```

`spring.threads.virtual.enabled: true`가 활성화되면 Spring Boot는 `SimpleAsyncTaskExecutor`(가상 스레드 기반)를 사용합니다.  
`task.execution.pool.*` 설정은 `ThreadPoolTaskExecutor` 전용이므로 **현재 환경에서는 적용되지 않는 dead configuration**입니다.  
혼란을 유발하고 향후 개발자가 풀 크기를 조정해도 효과가 없는 상황이 반복될 수 있습니다.

> **권장 조치:** Virtual Threads 전용 주석으로 명확히 표기하거나 제거
> ```yaml
> spring:
>   threads:
>     virtual:
>       enabled: true  # Virtual Threads 활성화 시 아래 task.execution.pool 설정은 무시됨
>   # task.execution.pool: (Virtual Threads 모드에서는 미적용 — 주석 처리)
> ```

---

### NM-3. Prometheus 운영 비활성화 — 1차 미해결 지속

**파일:** `src/main/resources/config/application-prod.yml` L11–15

```yaml
management:
  prometheus:
    metrics:
      export:
        enabled: false     # ← 운영에서 메트릭 수집 비활성
```

운영 모니터링 체계(Grafana, AlertManager 등)와 연동이 필요하다면 활성화가 필요합니다.  
현재 비활성 상태로는 `RedisMonitoringConfiguration`에서 등록한 커스텀 Redis 메트릭(`redis.server.used_memory`)도 수집되지 않습니다.

> **권장 조치:**
> - 모니터링 필요 시: `enabled: true`로 변경, `/management/prometheus`는 내부망 제한(NH-4 참고)
> - 모니터링 불필요 시: `RedisMonitoringConfiguration`의 `MeterBinder` 빈도 같이 비활성화

---

### NM-4. `LoggingAspect` — 메서드 인수 전체 로깅 (민감 정보 노출 위험)

**파일:** `src/main/java/com/daangcool/stack/aop/logging/LoggingAspect.java` L101

```java
log.debug("Enter: {}() with argument[s] = {}",
    joinPoint.getSignature().getName(),
    Arrays.toString(joinPoint.getArgs()));   // ← 모든 인수를 문자열로 변환
```

Service 레이어를 포괄적으로 가로채므로, `changePassword(currentPassword, newPassword)`, `registerUser(AdminUserDTO)` 등 **비밀번호·개인정보가 포함된 인수가 DEBUG 로그에 그대로 출력**될 수 있습니다.  
`application-dev.yml`에서 `com.daangcool.stack` 레벨이 `DEBUG`이므로 개발 환경에서 즉시 발생합니다.

> **권장 조치:** 민감 파라미터 마스킹 또는 감사 대상 메서드 제외 포인트컷 추가
> ```java
> @Pointcut("execution(* com.daangcool.stack.service.UserService.changePassword(..))")
> public void sensitiveOperationPointcut() {}
>
> @Around("applicationPackagePointcut() && springBeanPointcut() && !sensitiveOperationPointcut()")
> public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
>     // 인수 로깅
> }
> ```

---

### NM-5. `Redisson SerializationCodec` — Java 직렬화 보안 및 성능 이슈

**파일:** `src/main/java/com/daangcool/stack/config/CacheConfiguration.java` L255

```java
config.setCodec(new org.redisson.codec.SerializationCodec());
```

Java 직렬화(`ObjectOutputStream`)는 다음 문제를 내포합니다:

- **보안:** 역직렬화 취약점(CVE)에 노출될 가능성이 있음 (Redis에서 신뢰할 수 없는 데이터 역직렬화 시)
- **성능:** JSON/Kryo 대비 직렬화 크기가 크고 속도가 느림
- **버전 호환성:** 도메인 객체 변경 시 `InvalidClassException` 위험

> **권장 조치:** Jackson 기반 코덱으로 전환
> ```java
> // Jackson2JsonRedisCodec 또는 MarshallingCodec (Redisson 4.x)  
> config.setCodec(new org.redisson.codec.JsonJacksonCodec());
> ```
> ⚠️ 전환 시 기존 Redis 캐시 데이터와 포맷이 달라 캐시가 무효화됩니다. 배포 전 Redis `FLUSHDB` 또는 캐시 버전 키 정책이 필요합니다.

---

### NM-6. `WebConfigurer.corsConfigurationSource()` — `api-docs/**` 경로 CORS 누락

**파일:** `src/main/java/com/daangcool/stack/config/WebConfigurer.java` L90–94

```java
source.registerCorsConfiguration("/api/**", config);
source.registerCorsConfiguration("/management/**", config);
source.registerCorsConfiguration("/v3/api-docs", config);
source.registerCorsConfiguration("/swagger-ui/**", config);
// ← "/api-docs/**" 경로 누락 (SecurityConfiguration에서 허용됨)
```

`SecurityConfiguration`에서 `/api-docs/**` 경로를 permitAll()로 허용하지만 CORS 설정에는 빠져 있습니다.  
크로스 오리진 환경에서 OpenAPI 문서를 직접 접근하는 클라이언트의 preflight가 차단될 수 있습니다.

> **권장 조치:**
> ```java
> source.registerCorsConfiguration("/api-docs/**", config);
> ```

---

## 🟢 Low / 유지보수 개선 사항

### NL-1. `ApplicationProperties.RateLimit.redisServer` — 사용되지 않는 필드

**파일:** `src/main/java/com/daangcool/stack/config/ApplicationProperties.java` L84

```java
private String[] redisServer;  // null인 경우 기본 캐시용 RedissonClient 재사용 가능
private boolean cluster = false;
```

`RateLimitingConfiguration.java`식에서 이 필드를 더 이상 참조하지 않습니다 (SSOT 개선 후 제거됨).  
하지만 `ApplicationProperties.RateLimit`에는 여전히 `redisServer`, `cluster` 필드가 잔존합니다.  
`ignoreUnknownFields = false` 정책 하에서는 YAML에 해당 키가 있으면 바인딩되지만 실제로 사용되지 않아 혼란을 줍니다.

> **권장 조치:** 미사용 필드(`redisServer`, `cluster`) 제거 및 관련 주석 정리

---

### NL-2. `UserService` `@Transactional` 클래스와 메서드 이중 선언

**파일:** `src/main/java/com/daangcool/stack/service/UserService.java` L32, L258, L274, L279, L284, L289

```java
@Service
@Transactional     // 클래스 레벨 readWrite 트랜잭션
public class UserService {
    @Transactional(readOnly = true)   // 개별 메서드 재정의
    public Page<AdminUserDTO> getAllManagedUsers(...) { ... }
    @Transactional
    public void changePassword(...) { ... }   // 클래스 레벨과 중복
}
```

`@Transactional`이 클래스 레벨에 선언되어 있으므로, 메서드 레벨의 `@Transactional`(readOnly=false) 재선언은 **클래스 레벨과 동일하여 의미 없는 중복**입니다. `readOnly=true` 재선언은 올바른 패턴입니다.

> **권장 조치:**
> - 클래스 레벨 `@Transactional` 유지
> - 쓰기 메서드의 `@Transactional` 중복 제거 (readOnly=true 는 유지)

---

### NL-3. `spring.liquibase.enabled: false` 운영 환경 — 문서화 필요

**파일:** `src/main/resources/config/application-prod.yml` L139–141

```yaml
spring:
  liquibase:
    enabled: false   # 운영에서 자동 마이그레이션 비활성화
```

운영에서 Liquibase가 비활성화되어 있어, 스키마 변경은 수동 또는 별도 파이프라인으로 실행해야 합니다.  
이 설계가 의도된 것이라면 `docs/architecture/` 또는 `docs/decisions/`에 명확히 문서화되어야 합니다.  
문서 없는 상태에서 신규 에이전트나 개발자가 오해할 수 있습니다.

> **권장 조치:** `docs/decisions/` 하위에 Liquibase 운영 정책 ADR(Architecture Decision Record) 작성

---

### NL-4. `application-dev.yml` `org.hibernate.orm.jdbc.bind: TRACE` — 민감 쿼리 파라미터 노출

**파일:** `src/main/resources/config/application-dev.yml` L12

```yaml
logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE   # ← SQL 바인딩 파라미터 전체 출력
```

개발 환경에서 Hibernate SQL 바인딩 파라미터가 TRACE 레벨로 출력됩니다.  
비밀번호, OTP 코드, 인증 토큰 등이 포함된 쿼리 실행 시 **콘솔/로그 파일에 평문 노출** 가능성이 있습니다.

> **권장 조치:** `DEBUG` 레벨 이하로 조정하거나 민감 서비스 관련 쿼리 로깅은 조건부 활성화

---

### NL-5. `EmailOtpLog` JCache 2차 캐시 등록 — 감사 로그 특성과 불일치

**파일:** `src/main/java/com/daangcool/stack/config/CacheConfiguration.java` L180–181

```java
createCache(cm, com.daangcool.stack.domain.EmailOtpLog.class.getName(), jcacheConfiguration);
createCache(cm, com.daangcool.stack.domain.EmailOtpLog.class.getName() + ".user", jcacheConfiguration);
```

`EmailOtpLog`는 OTP 인증 이벤트를 기록하는 **불변 감사 로그(Append-only)**입니다.  
Hibernate 2차 캐시를 적용하면 새로 삽입된 로그가 캐시를 통해 조회될 때 오래된 뷰를 반환하거나, 캐시 무효화 빈도가 높아 실질적 이득이 없습니다.  
감사 로그 엔티티는 2차 캐시 대상에서 제외하는 것이 표준 패턴입니다.

> **권장 조치:** `EmailOtpLog` 관련 캐시 등록 제거, 엔티티에 `@Cache` 어노테이션 미적용 확인

---

## 2차 액션 플랜 요약

### 🔴 즉시 (Critical)

| # | 항목 | 파일 | 예상 공수 |
|---|------|------|---------|
| NC-1 | JWT Secret fallback 제거 및 prod 환경변수화 강제 | `application-prod.yml`, `application-dev.yml` | 30분 |
| NC-2 | `OtpLogService` 별도 빈 분리로 `REQUIRES_NEW` 정상화 | `EmailOtpService.java`, `OtpLogService.java` (신규) | 2시간 |

### 🟠 단기 (High)

| # | 항목 | 파일 | 예상 공수 |
|---|------|------|---------|
| NH-1 | Reflection 기반 `CommandAsyncExecutor` 추출 방식 개선 | `RateLimitingConfiguration.java` | 3시간 |
| NH-2 | Redis 클러스터 모드 헬스 인디케이터 분기 추가 | `RedisMonitoringConfiguration.java` | 2시간 |
| NH-3 | OTP 로그 코드 마스킹/해싱 | `EmailOtpService.java` | 30분 |
| NH-4 | `/management/prometheus` 접근 권한 제한 | `SecurityConfiguration.java` | 30분 |

### 🟡 중기 (Medium)

| # | 항목 | 파일 | 예상 공수 |
|---|------|------|---------|
| NM-1 | `hibernate.generate_statistics` dev 전용 이동 | `application.yml`, `application-dev.yml` | 15분 |
| NM-2 | Virtual Threads 환경 dead configuration 주석/정리 | `application.yml` | 15분 |
| NM-3 | Prometheus 운영 활성화 결정 | `application-prod.yml` | 30분 |
| NM-4 | LoggingAspect 민감 메서드 포인트컷 제외 | `LoggingAspect.java` | 1시간 |
| NM-5 | Redisson JsonJacksonCodec 전환 (캐시 재시작 동반) | `CacheConfiguration.java` | 2시간 |
| NM-6 | CORS `/api-docs/**` 경로 추가 | `WebConfigurer.java` | 15분 |

### 🟢 저우선순위 (Low)

| # | 항목 | 파일 | 예상 공수 |
|---|------|------|---------|
| NL-1 | `RateLimit` 미사용 필드 제거 | `ApplicationProperties.java` | 15분 |
| NL-2 | `UserService` 중복 `@Transactional` 정리 | `UserService.java` | 15분 |
| NL-3 | Liquibase 운영 정책 ADR 문서화 | `docs/decisions/` | 1시간 |
| NL-4 | dev 로깅 `TRACE` → `DEBUG` 조정 | `application-dev.yml` | 15분 |
| NL-5 | `EmailOtpLog` JCache 등록 제거 | `CacheConfiguration.java` | 15분 |

---

## 검증 방법

```bash
# 전체 빌드 검증
./mvnw clean package -DskipTests

# 단위/통합 테스트
./mvnw test

# OTP 관련 변경 후 반드시 실행
./mvnw test -pl . -Dtest="*OtpService*,*EmailOtp*"

# 보안 의존성 취약점 체크 (OWASP Dependency Check)
./mvnw verify -Powasp-check
```

---

## 2차 점검 총평

1차 점검 이후 **14개 항목 중 12개**가 조치 완료되어 전반적인 보안·성능 수준이 크게 향상되었습니다.

2차 점검에서 발견된 가장 중요한 신규 이슈는:
- **NC-2 (OtpLogService self-invocation)**: 조용히 감사 로그 신뢰성을 깨뜨리는 잠복 버그로, 즉시 수정 필요
- **NC-1 (JWT Secret fallback)**: 1차에서 환경변수화는 완료되었으나 동일 기본값이 잔존해 실질적 격리가 미흡
- **NH-1 (Reflection 내부 API)**: 차기 Redisson 업그레이드 시 런타임 오류로 이어질 수 있는 시한폭탄

코드 품질·유지보수 측면에서는 전체적으로 안정적인 방향으로 진행되고 있으며, 위 이슈들을 순차적으로 해결하면 운영 수준의 신뢰성을 확보할 수 있습니다.
