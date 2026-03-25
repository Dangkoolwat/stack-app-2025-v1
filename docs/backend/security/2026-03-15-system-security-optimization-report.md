# 시스템 최적화 · 보안 종합 점검 리포트

대상 프로젝트: `stack-app-2025-v1` (com.daangcool:stack:2.0.0)  
스택: Spring Boot 4.0.3 · Java 21 · Oracle DB · Redis/Redisson 4.3.0 · Liquibase 4.31.0  
검토일: 2026-03-15  
최종 업데이트: 2026-03-16 (C-1, C-2, C-3, W-8 완료 반영)  
이전 리포트: `docs/agent-log/2026-03-14-sb4-review-report.md`

---

## 개요

Spring Boot 3.5 → 4.0 마이그레이션 완료 이후 전체 시스템을 대상으로 보안, 성능, 의존성, 운영 설정을 재점검한 결과입니다.  
이전 리포트(2026-03-14)에서 식별된 항목의 처리 현황을 포함하며, 신규 발견 항목을 추가합니다.

---

## 점검 결과 요약

| 심각도 | 전체 | 완료 | 잔여 |
|--------|------|------|------|
|  Critical — 즉시 조치 | 5 | 4 | 1 |
|  Warning — 조속한 개선 | 9 | 5 | 4 |
|  Pass — 잘 된 부분 | 6 | 6 | 0 |
|  개선 권고 | 4 | 2 | 2 |

---

##  Critical — 즉시 조치 필요

### ~~C-1. 자격증명 하드코딩~~  완료 (2026-03-16)

처리 내용:
- `application-secret.yml` 파일 삭제 완료
- dev/prod datasource 모두 `${SPRING_DATASOURCE_*}` 환경변수화 완료
- Redis 서버 URL, 메일 서버 설정도 함께 환경변수화

잔존 리스크: `pom.xml` dev 프로파일의 Liquibase 플러그인 자격증명은 하드코딩 상태.  
런타임에는 영향 없으며 `mvn liquibase:diff` 실행 시에만 사용됨.  
Maven `~/.m2/settings.xml` 서버 설정으로 분리 권장.

---

### ~~C-2. JWT Base64 Secret 노출 및 이중 관리~~  완료 (2026-03-16)

처리 내용: dev/prod 모두 `${JWT_SECRET:...}` 환경변수화 완료.

주의사항: 배포 시 반드시 512-bit Base64 값으로 `JWT_SECRET` 환경변수 주입 필요.
```bash
openssl rand -base64 64
```

---

### ~~C-3. 운영 DB 기본 자격증명 사용~~  완료 (2026-03-16)

처리 내용: `${SPRING_DATASOURCE_*}` 환경변수화 완료. `system/oracle` 하드코딩 제거.

---

### C-4. 파일 업로드 MIME 타입 서버측 검증 부재

파일: `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`

`upload.getMimeType()` 을 DB 저장값 그대로 `Content-Type` 헤더에 세팅합니다.  
실제 파일 매직바이트(magic bytes) 검증 없이 서빙되어 Stored XSS, 드라이브-바이 공격 위험이 있습니다.

조치:
```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.2</version>
</dependency>
```

```java
Tika tika = new Tika();
String detectedMime = tika.detect(inputStream);
if (!ALLOWED_MIME_TYPES.contains(detectedMime)) {
    throw new InvalidFileTypeException("허용되지 않는 파일 형식: " + detectedMime);
}
```

---

### ~~C-5. Spring Cloud BOM 버전 호환성 문제~~  완료 (2026-03-15)

적용된 버전: `2025.1.0` (Spring Boot 4.0 호환)

---

##  Warning — 조속한 개선 필요

### W-1. Rate Limiting 미적용

파일: `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`

- `POST /api/authenticate` — 무제한 로그인 시도 가능
- `POST /api/register` — 무제한 계정 생성 가능
- `POST /api/account/reset-password/init` — 이메일 열거 공격 가능

조치:
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.14.0</version>
</dependency>
```

---

### W-2. CSRF 전면 비활성화 재검토

파일: `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`

`spring-boot-starter-thymeleaf` 의존성 포함. Thymeleaf 폼 사용 여부 확인 후 결정.  
미사용 확인 시 현 상태 유지.

---

### W-3. Actuator 민감 엔드포인트 과다 노출

운영 프로파일에서 `configprops`, `env`, `loggers`, `liquibase` 노출 축소 필요.

```yaml
# application-prod.yml 권장
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
```

---

### ~~W-4. Redisson `setPassword()` Deprecated~~  완료 (2026-03-15)

URL 방식으로 전환 완료. `@SuppressWarnings("deprecation")` 제거.

---

### ~~W-5. AWS SDK 구버전 사용~~  완료 (2026-03-15)

적용된 버전: `2.42.13`

---

### ~~W-6. commons-io 구버전 사용~~  완료

적용된 버전: `2.18.0`

---

### W-7. OTP Code 평문 DB 저장

파일: `src/main/java/com/daangcool/stack/domain/User.java`

6자리 OTP가 DB에 평문 저장. Redis TTL 기반 임시 저장으로 전환 권장.

```java
redisTemplate.opsForValue().set("otp:" + userId, rawOtp, 5, TimeUnit.MINUTES);
```

---

### ~~W-8. LoggingAspect 민감 파라미터 노출~~  완료 (2026-03-16 재검토)

재검토 결과: 운영 환경에서 `LoggingAspect` 는 이미 비활성화 되어 있습니다.

`LoggingAspectConfiguration` 에서 `@Profile("dev")` 로 빈 등록 자체가 dev 전용입니다.
운영(`prod`) 에서는 `LoggingAspect` 빈이 생성되지 않아 AOP 전체가 동작하지 않습니다.

```java
// LoggingAspectConfiguration.java
@Bean
@Profile("dev")   // dev 프로파일에서만 빈 생성
public LoggingAspect loggingAspect(Environment env) {
    return new LoggingAspect(env);
}
```

| 환경 | LoggingAspect 활성 | 파라미터 로깅 |
|------|-------------------|-------------|
| dev  | O | O (디버깅 목적, 의도된 동작) |
| prod | X (빈 미생성) | X (AOP 비활성) |

관련 작업 로그: `docs/agent-log/2026-03-16-w8-logging-aspect-review.md`

---

### W-9. 운영 환경 TLS 미설정

파일: `src/main/resources/config/application-prod.yml`

Nginx Reverse Proxy TLS 종단 또는 Spring Boot 내장 SSL 설정 필요.

```yaml
server:
  port: 443
  ssl:
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    enabled-protocols: TLSv1.2,TLSv1.3
```

---

##  Pass — 잘 구현된 부분

| 항목 | 파일 | 내용 |
|------|------|------|
| JWT URI 파라미터 차단 | `SecurityJwtConfiguration.java` | `setAllowUriQueryParameter(false)` |
| Virtual Threads 전면 도입 | `application.yml`, `AsyncConfiguration.java` | Java 21 VT + `SimpleAsyncTaskExecutorBuilder` |
| 파일 스트리밍 전송 | `UploadResource.java` | `InputStream.transferTo(outputStream)` — OOM 위험 방지 |
| 캐시 TTL 세분화 | `CacheConfiguration.java` | 정적 24h / 동적 1h TTL 분리 |
| Permissions-Policy 헤더 | `SecurityConfiguration.java` | camera, geolocation 등 브라우저 API 비활성화 |
| Jackson 자동 설정 활용 | `JacksonConfiguration.java` | Boot 4 Jackson 3 환경 수동 등록 제거 |

---

##  개선 권고

### ~~I-1. HikariCP maxPoolSize 누락 (dev 환경)~~  완료

dev/prod 모두 실무 권장값 적용 완료.  
관련 작업 로그: `docs/agent-log/2026-03-15-hikaricp-oracle-config.md`

### I-2. `Optional` 사용 패턴 현대화

파일: `src/main/java/com/daangcool/stack/web/rest/AccountResource.java`

`isPresent()` / `get()` 패턴을 `orElseThrow()` 로 정리.

### I-3. `SpaWebFilter` 경로 매칭 범위 과다

파일: `src/main/java/com/daangcool/stack/web/filter/SpaWebFilter.java`

`/uploads/`, `/share/` 경로 제외 조건 추가 필요.

### ~~I-4. Liquibase hibernate6 확장과 Hibernate 7 불일치~~  완료

`liquibase-hibernate6 5.0.1` 업그레이드 완료.

---

## 액션 플랜 요약 (최신 상태)

| 우선순위 | 항목 | 담당 파일 | 상태 |
|----------|------|-----------|------|
| ~~ 즉시~~ | ~~C-1: 자격증명 환경변수화~~ | `application-*.yml` |  완료 |
| ~~ 즉시~~ | ~~C-2: JWT 시크릿 환경변수화~~ | `application-*.yml` |  완료 |
| ~~ 즉시~~ | ~~C-3: prod DB 계정 최소 권한 전환~~ | `application-prod.yml` |  완료 |
|  즉시 | C-4: 파일 업로드 MIME 검증 추가 | `UploadService.java` | 미완료 |
| ~~ 즉시~~ | ~~C-5: Spring Cloud BOM 호환 버전~~ | `pom.xml` |  2025.1.0 |
| ~~ 단기~~ | ~~W-1: Rate Limiting 적용~~ | `SecurityConfiguration.java` |  완료 |
| ~~ 단기~~ | ~~W-4: Redisson setPassword() 제거~~ | `CacheConfiguration.java` |  완료 |
|  단기 | W-7: OTP 평문 저장 → Redis TTL 전환 | `User.java`, OTP Service | 미완료 |
| ~~ 단기~~ | ~~W-8: LoggingAspect 파라미터 필터링~~ | `LoggingAspect.java` |  @Profile("dev") 로 운영 비활성 확인 |
|  단기 | W-9: 운영 TLS 활성화 | `application-prod.yml` | 미완료 |
|  중기 | W-3: Actuator 노출 범위 축소 | `application-prod.yml` | 미완료 |
| ~~ 중기~~ | ~~W-5: AWS SDK 업데이트~~ | `pom.xml` |  2.42.13 |
| ~~ 중기~~ | ~~W-6: commons-io 업데이트~~ | `pom.xml` |  2.18.0 |
| ~~ 권고~~ | ~~I-1: HikariCP dev 설정 보완~~ | `application-dev.yml` |  완료 |
|  권고 | I-3: SpaWebFilter 경로 보완 | `SpaWebFilter.java` | 미완료 |
| ~~ 권고~~ | ~~I-4: Liquibase hibernate6 업그레이드~~ | `pom.xml` |  5.0.1 |

---

## 검증 방법

```bash
./mvnw clean package -DskipTests
./mvnw test
```

---

## 관련 문서

- `docs/agent-log/2026-03-14-sb4-review-report.md` — Spring Boot 4 마이그레이션 초기 검토
- `docs/agent-log/2026-03-15-liquibase-hibernate6-upgrade.md` — Liquibase hibernate6 업그레이드
- `docs/agent-log/2026-03-15-mockito-jvm-agent-setup.md` — Mockito JVM Agent 설정
- `docs/agent-log/2026-03-15-dependency-version-updates.md` — C-5, W-5 버전 업데이트
- `docs/agent-log/2026-03-15-w4-redisson-setpassword-removal.md` — W-4 Redisson 처리
- `docs/agent-log/2026-03-15-hikaricp-oracle-config.md` — HikariCP Oracle 최적화
- `docs/agent-log/2026-03-16-c1-c2-c3-credentials-env-migration.md` — C-1, C-2, C-3 완료
- `docs/agent-log/2026-03-16-w8-logging-aspect-review.md` — W-8 재검토 및 완료
- `docs/agent-log/2026-03-16-rate-limiting-phase3-redis.md` — W-1 Phase 3 Redis 연동 및 고도화
- `docs/agent-log/2026-03-17-rate-limiting-test-stabilization.md` — W-1 테스트 안정화 및 Mocking 개선
- `AGENTS.md` — 프로젝트 에이전트 가이드
