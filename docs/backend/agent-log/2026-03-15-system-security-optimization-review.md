# 2026-03-15 시스템 최적화 · 보안 종합 점검

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

Spring Boot 4.0 마이그레이션 후 시스템 최적화 및 보안 종합 점검

---

## Goal

Spring Boot 3.5 → 4.0 마이그레이션이 완료된 `stack-app-2025-v1` 프로젝트를 대상으로  
최신 버전 기준의 보안 취약점, 성능 최적화, 의존성 버전, 운영 설정 적정성을 전면 재점검하여  
실행 가능한 개선 항목과 우선순위를 문서화한다.

---

## Context

- 이전 리포트 `docs/agent-log/2026-03-14-sb4-review-report.md`에서 Spring Boot 4 마이그레이션 직후의  
  Critical 3건 / High 4건 / Medium 5건 / Low 5건을 식별하고 일부 즉시 수정이 이루어짐.
- `docs/agent-log/2026-03-14-h3-h4-fix.md` — H-3 스트리밍, H-4 URI 파라미터 차단 적용 완료
- `docs/agent-log/2026-03-14-m2-cache-review.md` — 캐시 TTL 세분화 적용 완료
- 본 검토는 이전 작업에서 처리되지 않은 잔존 항목과 추가 발견 항목을 포함한 전체 재점검.
- 검토 범위: `pom.xml`, `application*.yml`, 주요 Java 설정 클래스, 보안/필터/도메인 코드

---

## Work Performed

1. `pom.xml` 전체 의존성 버전 및 BOM 호환성 검토
2. `application.yml`, `application-dev.yml`, `application-prod.yml`, `application-secret.yml` 보안 설정 점검
3. `SecurityConfiguration.java` — CSRF, CORS, Rate Limiting, 엔드포인트 접근 제어 검토
4. `SecurityJwtConfiguration.java` — JWT 설정 및 BearerTokenResolver 검토
5. `CacheConfiguration.java` — Redisson deprecated API 및 캐시 설정 검토
6. `LoggingAspect.java` — 민감 파라미터 로깅 위험 검토
7. `User.java` 도메인 — OTP 평문 저장 검토
8. `UploadResource.java` — MIME 타입 검증 및 스트리밍 처리 검토
9. `AsyncConfiguration.java` — Virtual Threads 연동 적절성 검토
10. `SpaWebFilter.java` — 경로 매칭 범위 검토
11. `docs/security/` 폴더 신규 생성 및 상세 리포트 작성
12. 본 agent-log 작성

---

## Files Modified

생성(신규):
- `docs/security/2026-03-15-system-security-optimization-report.md`
- `docs/agent-log/2026-03-15-system-security-optimization-review.md` (본 파일)

읽기 전용 검토(수정 없음):
- `pom.xml`
- `src/main/resources/config/application.yml`
- `src/main/resources/config/application-dev.yml`
- `src/main/resources/config/application-prod.yml`
- `src/main/resources/config/application-secret.yml`
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
- `src/main/java/com/daangcool/stack/config/SecurityJwtConfiguration.java`
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- `src/main/java/com/daangcool/stack/config/AsyncConfiguration.java`
- `src/main/java/com/daangcool/stack/aop/logging/LoggingAspect.java`
- `src/main/java/com/daangcool/stack/domain/User.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`
- `src/main/java/com/daangcool/stack/web/filter/SpaWebFilter.java`
- `src/main/java/com/daangcool/stack/web/rest/AuthenticateController.java`
- `src/main/java/com/daangcool/stack/service/UserService.java`
- `src/main/resources/logback-spring.xml`

---

## Architecture Impact

코드 변경은 수행되지 않았습니다. 문서화 작업만 수행.  
향후 권고 사항 중 아래 항목은 아키텍처 영향이 있습니다.

- **W-7 (OTP Redis 전환)**: `User` 도메인 컬럼 제거 → Liquibase 마이그레이션 필요
- **W-1 (Rate Limiting)**: `SecurityConfiguration`에 새 필터 체인 요소 추가
- **C-4 (MIME 검증)**: 업로드 서비스 레이어에 검증 로직 추가

---

## Security Impact

본 검토에서 발견된 보안 영향이 큰 항목:

| 항목 | 영향 | 심각도 |
|------|------|--------|
| C-1, C-2: 자격증명·JWT 시크릿 Git 노출 | DB 및 JWT 위·변조 가능 | Critical |
| C-3: prod Oracle system 계정 사용 | DB 전체 관리자 권한 노출 | Critical |
| C-4: MIME 타입 미검증 | Stored XSS, 악성 파일 서빙 | Critical |
| W-1: Rate Limiting 부재 | 브루트포스, 계정 열거 공격 | High |
| W-7: OTP 평문 저장 | DB 접근 시 OTP 노출 | High |
| W-8: LoggingAspect 패스워드 로깅 | 로그 파일을 통한 자격증명 노출 | High |

---

## Verification

본 작업은 코드 수정이 없는 검토/문서화 작업입니다.  
소스코드 정적 분석을 통해 각 항목을 확인하였으며, 빌드 및 테스트는 수행하지 않았습니다.

코드 수정 작업이 진행될 경우 아래 순서로 검증을 수행해야 합니다:

```bash
./mvnw clean package -DskipTests   # 빌드 검증
./mvnw test                        # 단위·통합 테스트
./mvnw verify                      # 전체 검증 라이프사이클
```

---

## Risks

1. **C-1, C-2 Git 히스토리 노출**: `git filter-repo` 실행 후 팀 전원이 force-pull 해야 함. 원격 브랜치 히스토리 rewrite 필요.
2. **C-3 prod DB 계정 전환**: 운영 Oracle 계정 교체 시 Liquibase 실행 계정 권한도 함께 조정 필요.
3. **W-7 OTP Redis 전환**: 기존 `otpCode`, `otpExpireDate` 컬럼 제거 시 Liquibase rollback 전략 수립 필요.
4. **C-5 Spring Cloud 호환**: Boot 4.0 호환 Spring Cloud BOM이 아직 GA 미발표 상태. 해당 기능 사용 범위 파악 후 결정.

---

## Next Suggested Tasks

우선순위 순:

1. **즉시** — `application-secret.yml` `.gitignore` 추가 및 Git 히스토리 정리, DB/JWT 시크릿 rotate
2. **즉시** — `application-prod.yml` DB 자격증명 환경변수화, Oracle 전용 계정 생성
3. **단기** — `UploadService`에 Apache Tika MIME 타입 검증 로직 추가 (C-4)
4. **단기** — `SecurityConfiguration`에 Bucket4j Rate Limiting 필터 추가 (W-1)
5. **단기** — `LoggingAspect` 민감 파라미터 필터링 추가 (W-8)
6. **단기** — OTP Redis TTL 방식으로 전환, `User` 엔티티 컬럼 제거 + Liquibase 마이그레이션 (W-7)
7. **중기** — `application-prod.yml` Actuator 노출 범위 축소 (W-3)
8. **중기** — AWS SDK, commons-io 버전 업데이트 (W-5, W-6)
9. **중기** — 운영 TLS 활성화 또는 Nginx Reverse Proxy TLS 종단 적용 (W-9)
10. **중기** — Spring Cloud BOM Boot 4.0 호환 버전 업데이트 (C-5)

---

## Notes for Future Agents

- `application-secret.yml`은 `.gitignore`에 추가 전까지 Git에 평문 시크릿이 노출된 상태입니다.  
  이 파일을 수정하거나 읽을 때 민감 정보 취급에 주의하세요.
- 이전 리포트(2026-03-14)에서 H-3(스트리밍), H-4(URI 파라미터), M-2(캐시 TTL), M-5(캐시 세분화)는  
  이미 수정 적용된 상태입니다. 중복 작업 방지를 위해 해당 로그를 먼저 확인하세요.
- `User.java`의 `otpCode` 컬럼 제거는 반드시 Liquibase `dropColumn` 변경셋과 함께 진행해야 합니다.  
  컬럼 삭제 전 애플리케이션 코드에서 해당 필드 참조를 모두 제거한 후 단계적으로 마이그레이션하세요.
- `pom.xml`의 `spring-cloud-dependencies 2024.0.1`은 현재 애플리케이션이 실제로 Spring Cloud 기능(Feign, Config, Gateway 등)을 사용하는지 확인이 필요합니다. 미사용 시 BOM 전체 제거가 가장 안전합니다.
- `logback-spring.xml`의 `application.logging.*` 프로퍼티는 `ApplicationProperties.Logging` 클래스와 연동됩니다.  
  prod 로그 경로는 `application-prod.yml`의 `application.logging.file-path`로 제어됩니다.
- 상세 점검 리포트 전문은 `docs/security/2026-03-15-system-security-optimization-report.md`를 참고하세요.
