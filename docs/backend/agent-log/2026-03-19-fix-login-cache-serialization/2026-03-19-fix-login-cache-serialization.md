# Agent Work Log: Fix Login Cache & Serialization Error

## Date
2026-03-19 ~ 2026-03-20

## Agent
Antigravity (Google Deepmind)

## Task Title
로그인 직렬화 무한 루프 및 Spring Boot 4 마이그레이션 통합 테스트 복구

## Goal
- 로그인 시 발생하는 `JsonMappingException` (Hibernate PersistentSet 직렬화 무한 루프) 해결.
- Spring Boot 4 / Jackson 3 마이그레이션 후 깨진 주요 통합 테스트(`AuthenticateControllerIT`, `UploadResourceIT`)의 정상화.
- 불필요한 로그 노이즈 제거 및 Swagger UI 404 오류 해결.

## Context
- `User` 엔티티와 `Authority` 간의 관계가 JSON으로 직렬화될 때 Hibernate 프록시 객체 충돌로 인해 무한 루프가 발생함.
- Spring Boot 4 업그레이드 이후 패키지 경로 변경(Naming Strategy), Jackson 옵션 삭제 등으로 인해 애플리케이션 초기화 실패 및 테스트 실패 발생.
- Swagger UI가 `/v3/api-docs/springdocDefault`를 찾지 못해 API 문서가 보이지 않는 현상 발생.

## Work Performed
1. **직렬화 문제 해결**: `UserRepository`의 캐시 어노테이션을 제거하여 Hibernate L2 캐시(이진 방식)만 사용하도록 변경. `CacheConfiguration` 정리.
2. **Spring Boot 4 호환성 수정**:
   - `SpringImplicitNamingStrategy` 패키지 경로 수정.
   - Jackson `write-durations-as-timestamps` 속성 제거.
   - 테스트 코드 내 Jackson 패키지 Import 갱신 (`tools.jackson`).
3. **인증 핸들러 개선**: `ExceptionTranslator`에 `AuthenticationException` 처리기를 추가하여 500 에러 대신 401 Unauthorized Problem Detail 반환.
4. **UploadResourceIT 수정**: 파일 확장자 허용 정책 업데이트 및 MockMvc 인증 주입 방식 변경.
5. **로그 관리**: OpenTelemetry B3 Propagator의 불필요한 `DEBUG` 로그를 `INFO` 레벨로 차단.
6. **Swagger UI 복구**: 요청 경로 불일치 문제를 해결하기 위해 백엔드 OpenAPI 엔드포인트를 `/v3/api-docs`로 변경.
7. **Websocket 핸드셰이크 정규화**: `STATELESS` 정책 하에서 SockJS의 `/info` 요청이 인증 문제로 차단되어 실시간 트래커가 작동하지 않던 문제를 `SecurityConfiguration` 수정을 통해 해결.

## Files Modified
- `src/main/java/com/daangcool/stack/repository/UserRepository.java`
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- `src/main/java/com/daangcool/stack/web/rest/errors/ExceptionTranslator.java`
- `src/main/resources/config/application.yml`
- `src/main/resources/logback-spring.xml`
- `src/test/resources/config/application.yml`
- `src/test/java/com/daangcool/stack/web/rest/AuthenticateControllerIT.java`
- `src/test/java/com/daangcool/stack/web/rest/UploadResourceIT.java`

## Verification
- `mvn clean test`를 통해 `AuthenticateControllerIT`, `UploadResourceIT` 통과 확인.
- Swagger UI `/admin/docs` 접속 시 OpenAPI 정의 로딩 확인.

## Notes for Future Agents
- Spring Boot 4 환경에서 JSON 직렬화와 캐시를 혼용할 때 Hibernate Managed Collection이 포함된 엔티티는 직접 JSON 캐시에 넣지 않도록 주의하십시오.
- Swagger UI Fetch Error 발생 시 `vite.config.ts`의 Proxy 경로와 백엔드의 `springdoc.api-docs.path` 일치 여부를 먼저 확인하십시오.
