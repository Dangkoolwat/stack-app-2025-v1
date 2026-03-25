# 시스템 리포트 (Spring Boot 4 + Vite 통합)

## 개요
- Spring Boot 4.0.3과 Java 21 기반 백엔드가 현재 BOM/의존성을 고정한 상태에서 최신 보안 패치를 적용하고 있으며, 프론트엔드는 Vite/Vue 3·Maven `webapp` 빌드를 병행하여 운영 중입니다.
- 본 문서는 `docs/*/agent-log/2026-03-22-system-report`에서 수집한 검토를 기반으로 백엔드/프론트엔드 관점에서 최적화, 보안, 유지보수 계획을 종합적으로 정리합니다.

## 백엔드 평가
### Spring Boot 4 최적화
- `pom.xml`은 Spring Cloud 2025.1, OpenAPI 3.0.2, Jackson 2.21.1/3.1.0 보안 BOM을 포함하고 있어 의존성 충돌을 예방하고 패치를 강제합니다.
- `SecurityConfiguration`이 Stateless JWT, CSRF 비활성화, SPA 리디렉션, CSP nonce 필터, rate limiting 필터, 정적/관리/Swagger 경로 제어를 일괄 설정해 프레임워크 기본을 벗어나지 않고 실무 정책을 반영합니다.
- Actuator, Micrometer, OpenTelemetry, Logback 로깅, Liquibase, Redis 캐시/Rate Limit 정책이 `application.yml`과 `ApplicationProperties`에 명세되어 있어 운영 시 위임과 조정이 가능하며, `SecurityJwtConfiguration`은 JWT 오류를 메트릭화하면서 URI 토큰을 금지합니다.

### 향후 보안 대비
- 비공개 파일 경로 접근을 Filter에서 완전 차단하고, 공개 파일만 허용하며 `UploadResource`가 권한/예외를 정밀 처리합니다.
- Swagger/OpenAPI와 `/management` 노출은 profile 조합과 `SecurityConfiguration`에서 제어되므로 `api-docs` 프로파일을 통해 비활성화하거나 IP 필터링을 병행해야 합니다.
- CSP nonce 필터(`CspNonceFilter`)로 inline script/style을 제어하지만 아직 `<style>`의 `unsafe-inline`이 남아 있어 nonce/해시를 강화하거나 CSP 헤더를 더 세부적으로 정의할 수 있습니다.

### 유지보수 관점 제안
1. HSTS/Expect-CT/Feature-Policy 등 추가 HTTP 헤더를 `SecurityConfiguration.headers` 블록에서 정의하여 TLS 강제 및 브라우저 보호를 강화합니다.
2. 새로운 dependency 또는 정책 변화 시 `./mvnw dependency:tree`와 `mvn dependency:check`를 CI에 추가하고, `pom.xml`과 `package.json`의 버전이 맞물려 운영되므로 Dependabot/Gradle Versions를 도입하는 것을 고려하세요.
3. 현재 `application.yml`에 캐시 TTL, Redis 서버 등이 문서화되어 있으므로 운영자는 `.env`나 Kubernetes Secret으로 값을 주입하고 `ApplicationProperties`의 구조를 활용해 변경을 중앙화합니다.

## 프론트엔드 평가
### Spring Boot 4와의 통합
- `package.json` 스크립트(`webapp:build`, `start`, `webapp:prod`)가 Maven `webapp` 프로파일과 연계되어 Vite 결과물을 `target/classes/static`으로 복사하고, Swagger UI와 axios minified 배포 파일을 자동 복사합니다.
- `vite.config.ts`는 Swagger UI 복사, HTTPS 인증서 파일, `/api`, `/management`, `/v3/api-docs`, `/websocket`에 대한 proxy 정의로 로컬 개발과 스프링 서버 통신을 일관되게 지원합니다.
- `main.ts`에서 Pinia, axios 인터셉터를 초기화하고 권한/언어 로직을 `router.beforeResolve`로 통제해 프론트에서 인증/권한도메인을 완성합니다.

### 보안 · 유지보수 포커스
- axios 인터셉터(`axios-interceptor.ts`)가 로컬/세션 스토리지 토큰을 Authorization 헤더로 자동 전송하면서 401/403/400/500 상태를 예외 처리하므로 백엔드 API 계약에 따라 응답 흐름을 일관되게 유지할 수 있습니다.
- `SERVER_API_URL`/`SERVER_WS_URL`을 환경에 따라 주입하지 않으면 기본 `/` 주소가 사용되며, 실서비스에서는 `VITE_API_URL`/`VITE_WS_URL` 값을 CI/CD 파이프라인에서 명시해야 합니다.
- `package.json`이 `eslint`, `prettier`, `vitest` 등을 포함하므로 `npm run lint`·`npm run vitest-run` 등을 정기적으로 실행해 디펜던시 취약점이나 스크립트 충돌을 선제 관리합니다.

## 종합 권고 및 실행
1. 보안 헤더 강화: `SecurityConfiguration.headers`에서 HSTS, Expect-CT, Feature Policy를 명시하고, Swagger/management 노출을 프로파일별 정책이나 WAF 룰로 FMU(Flow Management Unit)합니다.
2. 환경 일관성 확보: 프론트에서 사용하는 `SERVER_API_URL`/`SERVER_WS_URL`을 `.env`, CI/CD `VITE_*` 변수로 설정하고, 백엔드 `application.yml`/`ApplicationProperties`와 문서화하여 개발자 onboarding을 단축합니다.
3. 자동화 및 테스트: `./mvnw -ntp verify`와 `npm run vitest-run`, `npm run lint`, `npm run prettier:check`를 CI pipeline에 연결하여 코드/설정 변경 시 즉시 검증하도록 구성합니다.
4. 의존성/캐시 관리: `pom.xml`과 `package.json` 모두 Dependabot 또는 `mvn dependency:check`, `npm audit`을 정기 실행해 취약점 대응을 자동화하고 `ApplicationProperties.cache.ttl`을 활용해 캐시 변경 이력을 문서화합니다.

## 후속 관리 일정
| 항목 | 담당 | 주기 | 비고 |
| --- | --- | --- | --- |
| 의존성 취약점 점검 | DevSecOps | 매주 | `mvn dependency:check` + `npm audit` 결과 공유 |
| 보안 헤더 정책 검토 | SRE | 배포 전 | HSTS/Expect-CT/Feature-Policy 포함 여부 확인 |
| 프론트 환경 변수 점검 | FE팀 | 배포 전 | `VITE_API_URL`, `VITE_WS_URL`이 실제 환경과 일치하는지 검증 |
| 린트/테스트 파이프라인 | 엔지니어링 | PR당 | `npm run lint`, `npm run vitest-run`, `./mvnw verify` 실행 |

## 참고
- 내부 분석 로그: `docs/backend/agent-log/2026-03-22-system-report/`, `docs/frontend/agent-log/2026-03-22-system-report/`
- Spring Boot 설정 참조: `src/main/resources/config/application.yml`, `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
