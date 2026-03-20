# 2026-03-14 Spring Boot 4.0 마이그레이션 검토

## 기본 정보

- **Date:** 2026-03-14
- **Agent:** Antigravity (Google DeepMind)
- **Task Title:** Spring Boot 3.5 → 4.0 마이그레이션 후 최적화·성능·보안 검토
- **Goal:** 마이그레이션 완료 상태에서 보안 취약점, 성능 병목, 최적화 기회를 전반적으로 식별하고 우선순위화된 액션 플랜 제시

## Context

- Spring Boot 4.0.3, Java 21, Oracle DB (ojdbc11), Redisson 4.3.0, Liquibase 4.31.0
- 가상 스레드(`spring.threads.virtual.enabled: true`) 적용
- JHipster 기반 아키텍처: Controller → Service → Domain → Repository
- 인증: JWT(OAuth2 Resource Server), BCryptPasswordEncoder
- 캐시: Redis/Redisson + JCache + Hibernate L2 Cache
- 파일 스토리지: LOCAL / SHARE / S3 / OCI 멀티 전략

## 검토 파일 목록

- `pom.xml`
- `src/main/resources/config/application.yml`
- `src/main/resources/config/application-dev.yml`
- `src/main/resources/config/application-prod.yml`
- `src/main/java/.../config/SecurityConfiguration.java`
- `src/main/java/.../config/SecurityJwtConfiguration.java`
- `src/main/java/.../config/CacheConfiguration.java`
- `src/main/java/.../config/AsyncConfiguration.java`
- `src/main/java/.../config/WebConfigurer.java`
- `src/main/java/.../config/FileResourceConfiguration.java`
- `src/main/java/.../config/ApplicationProperties.java`
- `src/main/java/.../security/DomainUserDetailsService.java`
- `src/main/java/.../web/rest/UploadResource.java`

## 수행 작업

- 전체 코드베이스 정적 분석 (검토 목적, 코드 변경 없음)
- 17개 항목 식별 (Critical 3 / High 4 / Medium 5 / Low 5)
- 우선순위화된 액션 플랜 문서 작성

## 주요 발견 사항

### Critical
1. **JWT Secret 동일** — dev/prod `application-*.yml` 모두 동일한 `base64-secret` 사용
2. **DB 자격증명 하드코딩** — `application-prod.yml`, `pom.xml`에 평문 노출
3. **`hibernate.ddl-auto: update` in prod** — Liquibase 사용 중임에도 DDL 자동화 활성

### High
4. **Redisson 이중 생성** — `jcacheConfiguration` Bean과 `redissonClient` Bean 각각 `Redisson.create()` 호출로 두 배 Redis 연결 생성
5. **Virtual Thread + ThreadPoolTaskExecutor 혼용** — 가상 스레드 모드에서 `AsyncConfiguration`이 플랫폼 스레드 풀 생성
6. **파일 전체 byte[] 로드** — `UploadResource`에서 대용량 파일도 heap에 전체 로드
7. **BearerTokenResolver URI 파라미터 허용** — JWT가 URL에 노출될 수 있음

### Medium
8. 운영 HikariCP 풀 미설정
9. CSP `unsafe-inline` / `unsafe-eval` 포함
10. Prometheus 운영 비활성화
11. Hibernate 내부 EmailValidator 클래스 직접 사용
12. 캐시 TTL 단일값 (세분화 미적용)

## 수정된 파일

없음 (분석/검토 전용 태스크)

## Architecture Impact

검토 결과 코드 변경 없음. 향후 권고사항 적용 시:
- `AsyncConfiguration` 수정 → 가상 스레드 정책 변경 (영향 범위: @Async 메서드 전체)
- `UploadResource` 스트리밍 전환 → 파일 API 계약 변경 없음, 구현만 변경
- `CacheConfiguration` Redisson 통합 → Redis 연결 수 감소

## Security Impact

- C-1, C-2, C-3은 운영 보안에 직접적 영향. 즉시 조치 필요.
- H-4 (URI 파라미터 토큰)는 JWT 토큰 로그 노출 위험.

## Verification

코드 검토 전용. 빌드/테스트 실행 없음.  
권고사항 적용 후 `./mvnw test` 및 `./mvnw clean package` 실행 필요.

## Risks

- JWT Secret 교체 시 기존 발급된 토큰 전체 무효화 필요 (사용자 강제 재로그인)
- `ddl-auto: none` 전환 시 Liquibase 체인지셋 완전성 사전 검증 필요

## Next Suggested Tasks

1. **Critical 3건 즉시 처리** — 환경변수/Vault 연동, prod yml 정리
2. **Redisson 통합 리팩터** — `CacheConfiguration.java`
3. **UploadResource 스트리밍 전환** — `StreamingResponseBody` 적용
4. **AsyncConfiguration 가상 스레드 대응**
5. **CSP nonce 기반 전환** — 프론트엔드 팀 협의 필요

## Notes for Future Agents

- `application-secret.yml`이 이미 존재하나 현재 Liquibase/JWT 시크릿이 포함되어 있지 않음. 시크릿 이동 대상 파일로 우선 활용 가능.
- `buildTTLConfig()` 메서드(`CacheConfiguration.java` L246)는 준비된 상태이나 실제로 캐시 생성에 사용되지 않고 있음. 활성화만 하면 캐시 TTL 세분화 가능.
- Redisson 4.3.0의 deprecated `setPassword()` — 현재 `@SuppressWarnings`로 억제 중. 4.x 마이너 업데이트 시 확인 필요.
