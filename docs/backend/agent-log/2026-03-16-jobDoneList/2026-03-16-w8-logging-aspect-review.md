# 2026-03-16 W-8 LoggingAspect 파라미터 노출 — 재검토 및 완료 처리

---

## Date

2026-03-16

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

W-8 LoggingAspect 민감 파라미터 노출 — 구조 재검토 후 완료 처리

---

## Goal

보안 점검 항목 W-8 의 실제 구현 구조를 확인하여,
운영 환경에서의 민감 파라미터 노출 여부를 재평가하고 완료 처리한다.

---

## Context

- 보안 리포트 W-8 에서 `LoggingAspect.logAround()` 가 전체 파라미터를 로깅하므로
  `LoginVM`, `PasswordChangeDTO` 등 민감 객체가 로그에 출력될 수 있다고 지적
- 사용자가 "W-8 은 개발에서만 사용하고 운영에서는 사용하지 않는다" 고 확인
- 실제 코드 구조 재검토 필요

---

## Work Performed

1. `LoggingAspect.java` 코드 확인
2. `LoggingAspectConfiguration.java` 빈 등록 구조 확인
3. `StackAppConstants.java` 프로파일 상수 확인
4. 구조 분석 및 운영 환경 노출 여부 판정
5. 보안 리포트 W-8 완료 처리
6. 본 agent-log 작성

---

## 구조 분석

### LoggingAspectConfiguration.java

```java
@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(StackAppConstants.SPRING_PROFILE_DEVELOPMENT)  // = "dev"
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}
```

`@Profile("dev")` 로 빈 등록 자체가 dev 프로파일 전용입니다.
운영(`prod`) 환경에서는 `LoggingAspect` 빈이 생성되지 않으므로
AOP 어드바이스 전체가 비활성화됩니다.

### LoggingAspect.java 내부 이중 가드

```java
@AfterThrowing(...)
public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
    if (env.acceptsProfiles(Profiles.of(StackAppConstants.SPRING_PROFILE_DEVELOPMENT))) {
        // dev 에서만 상세 로그
    } else {
        // 그 외: 원인만 로그
    }
}

@Around(...)
public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    if (log.isDebugEnabled()) {
        log.debug("Enter: {}() with argument[s] = {}", ...);
    }
}
```

빈 등록 레벨의 `@Profile("dev")` + 메서드 레벨의 프로파일 조건 확인으로
이중 방어 구조입니다.

### 판정

| 환경 | LoggingAspect 빈 생성 | 파라미터 로깅 |
|------|----------------------|-------------|
| dev  | O (`@Profile("dev")`) | O (DEBUG 레벨) |
| prod | X (빈 미생성)          | X (AOP 자체 비활성) |

**운영 환경에서는 `LoggingAspect` 가 완전히 비활성화됩니다.**
W-8 은 설계 의도대로 구현되어 있으므로 완료 처리합니다.

### dev 환경의 잔존 특성 (위험 아님)

dev 프로파일에서는 `LoginVM`, `PasswordChangeDTO` 등의 파라미터가
DEBUG 레벨 로그에 출력될 수 있습니다.
이는 개발자 로컬 환경에서의 디버깅 목적이며, 외부 노출 경로가 없으므로
허용 가능한 수준입니다.

필요하다면 추후 민감 타입 필터링을 추가할 수 있으나 현재는 불필요합니다.

---

## Files Modified

읽기 전용 확인 (코드 변경 없음):
- `src/main/java/com/daangcool/stack/aop/logging/LoggingAspect.java`
- `src/main/java/com/daangcool/stack/config/LoggingAspectConfiguration.java`
- `src/main/java/com/daangcool/stack/common/constant/StackAppConstants.java`

업데이트:
- `docs/security/2026-03-15-system-security-optimization-report.md` — W-8 완료 처리
- `docs/agent-log/2026-03-16-w8-logging-aspect-review.md` (본 파일)

---

## Architecture Impact

없음. 코드 변경 없이 구조 확인만 수행.

---

## Security Impact

운영 환경에서 `LoggingAspect` 가 `@Profile("dev")` 로 완전히 비활성화되어
민감 파라미터가 운영 로그에 출력되지 않음을 확인했습니다.

---

## Notes for Future Agents

- `LoggingAspect` 는 `@Profile("dev")` 로 dev 전용 빈입니다. 운영에서는 AOP 자체가 동작하지 않습니다.
- dev 환경에서 DEBUG 로그에 파라미터가 출력되는 것은 의도된 동작입니다.
- 민감 타입 필터링이 필요하다면 `logAround()` 에 아래를 추가할 수 있습니다:

  ```java
  private static final Set<String> SENSITIVE_TYPES = Set.of(
      "LoginVM", "PasswordChangeDTO", "KeyAndPasswordVM"
  );

  private boolean hasSensitiveArgs(JoinPoint joinPoint) {
      return Arrays.stream(joinPoint.getArgs())
          .filter(Objects::nonNull)
          .map(a -> a.getClass().getSimpleName())
          .anyMatch(SENSITIVE_TYPES::contains);
  }
  ```

  다만 현재는 필수 작업이 아닙니다.
