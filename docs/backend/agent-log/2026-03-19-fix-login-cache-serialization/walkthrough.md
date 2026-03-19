# Walkthrough: Fix Login Cache, Serialization, and Test Failures

## 완료된 작업 요약
이 작업에서는 로그인 시도 중 발생한 직렬화(Serialization) 무한 루프 버그를 해결하고, Spring Boot 4 / Jackson 3 마이그레이션 여파로 깨진 통합 테스트들과 인프라 설정 오류들을 모두 복원하였습니다.

### 1. 핵심 이슈: 직렬화 무한 루프 (`JsonMappingException`)
- **원인**: Hibernate의 지연 로딩 컬렉션(`PersistentSet`)이 포함된 `User` 엔티티를 Spring `@Cacheable`과 기본 `ObjectMapper`(JSON)가 처리하는 과정에서 순환 참조 및 매핑 충돌 발생.
- **해결**: `UserRepository`에서 권한을 포함한 조회 메서드의 `@Cacheable` 어노테이션을 제거. 대신 Hibernate L2 캐시(이진 직렬화 방식)에만 의존하도록 하여 JSON 직렬화 충돌을 원천 차단.
- **캐시 설정 정리**: `CacheConfiguration.java`에서 불필요하게 JSON 직렬화를 강제하던 Redis 관련 빈(Bean) 설정을 제거하여 안정성을 확보.

### 2. 통합 테스트 및 인프라 복구 (마이그레이션 후속 조치)
- **JPA Naming Strategy**: Spring Boot 4에서 패키지 위치가 바뀐 `SpringImplicitNamingStrategy` 경로를 `application-testdev.yml` 등에 반영하여 DB 초기화 오류 해결.
- **Jackson 설정 정리**: 더 이상 지원되지 않는 `write-durations-as-timestamps` 속성을 제거하여 애플리케이션 컨텍스트 로딩 실패를 방지.
- **ObjectMapper 주입**: `AuthenticateControllerIT` 등에서 Jackson 2 패키지를 바라보던 Import를 Jackson 3(`tools.jackson...`)로 변경.
- **인증 예외 핸들링**: `BadCredentialsException` 발생 시 서버 500 에러가 아닌 표준 401 Unauthorized Problem Detail을 반환하도록 `ExceptionTranslator` 보강.

### 3. 기능별 추가 수정
- **UploadResourceIT (보안 & 검증)**:
    - 테스트용 `.txt` 확장자가 보안 정책에 걸려 업로드되지 않던 문제를 `application.yml` 설정을 통해 해결.
    - 비공개 다운로드 테스트 시 `@WithMockUser`가 제대로 적용되지 않아 발생하던 403 Forbidden 오류를 MockMvc의 `with(user(...))` 명시적 주입 방식으로 해결.
- **Log Noise 제거**: 콘솔에 도배되던 `B3PropagatorExtractorMultipleHeaders` (Invalid TraceId) 로그를 `logback-spring.xml` 설정을 통해 `INFO` 레벨로 억제.
- **Swagger UI 404 해결**: 백엔드의 OpenAPI 메타데이터 경로(`/api-docs`)가 프론트엔드/Vite Proxy 기대값(`/v3/api-docs`)과 일치하지 않아 발생하던 fetch error를 해결하기 위해 백엔드 경로를 `/v3/api-docs`로 재정렬.
- **Websocket 401 Unauthorized 해결**: `STATELESS` 모드에서 SockJS의 초기 `/info` 호출이 인증 헤더 없이 차단되던 문제를 `SecurityConfiguration`의 `/websocket/**` 경로 `permitAll()` 설정으로 해결. (실제 보안은 STOMP 레벨에서 유지)

## 수정된 파일 목록
- `src/main/java/com/daangcool/stack/repository/UserRepository.java`
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- `src/main/java/com/daangcool/stack/web/rest/errors/ExceptionTranslator.java`
- `src/main/java/com/daangcool/stack/web/filter/SpaWebFilter.java`
- `src/main/resources/config/application.yml`
- `src/main/resources/logback-spring.xml`
- `src/test/resources/config/application.yml`
- `src/test/resources/config/application-testdev.yml`
- `src/test/java/com/daangcool/stack/web/rest/AuthenticateControllerIT.java`
- `src/test/java/com/daangcool/stack/web/rest/UploadResourceIT.java`

## 최종 검증
- `AuthenticateControllerIT`, `UploadResourceIT` 등 주요 통합 테스트 모듈이 100% 통과(Pass)함을 확인.
- 관리자 페이지의 Swagger API 문서 서비스 및 로그인 흐름 정상 작동 확인.
- 로그 가독성 개선 확인.

## 에이전트 로그 기록
- 상세 기록 위치: `docs/backend/agent-log/2026-03-19-fix-login-cache-serialization/`
- 상세 기록 위치: `docs/backend/agent-log/2026-03-19-fix-upload-resource-tests/`
