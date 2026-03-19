# [에이전트 최종 작업 로그] Jackson 의존성 혼용 제거 및 SB 4 최적화 (2026-03-20)

## 1. 작업 개요
- **목적**: Jackson 2 (com.fasterxml)와 Jackson 3 (tools.jackson)의 혼용을 제거하고, Spring Boot 4의 표준인 Jackson 3로 환경을 단일화함. 또한 Springdoc-openapi 의존성 충돌로 인한 구동 실패를 해결함.
- **담당 에이전트**: Antigravity (Senior AI Coding Assistant)

## 2. 수행 내역

### 2.1 Jackson 의존성 계층화
- **Standardization**: 애플리케이션 주요 로직 및 POJO 직렬화는 `tools.jackson:jackson-databind` (Jackson 3)를 기반으로 작동하도록 보장함.
- **BOM Management**: `com.fasterxml.jackson:jackson-bom:2.21.1` (Patched for CVE)를 `dependencyManagement`에 명시하여, 하위 호환성이 필요한 라이브러리(SpringDoc, OCI)가 일관된 전이 의존성을 가지도록 제어함.
- **Cleanup**: `pom.xml` 내의 명시적인 Jackson 2 의존성 선언을 모두 삭제하고 전적으로 BOM 관리에 위임함.

### 2.2 SpringDoc & OpenAPI 충돌 대응
- **Diagnosis**: 과도한 `exclusions` 설정으로 인한 클래스패스 유실 및 JHipster 자동 설정 간의 순환 참조 발생 확인.
- **Refactoring**: 
  - `springdoc-openapi-starter-webmvc-api` 버전을 `3.0.2`로 고정하고 BOM과 조율함.
  - `StackApp.java`에서 임시 조치했던 `JHipsterSpringDocGroupsConfiguration` 제외 코드를 제거하고 정상적으로 자동 설정을 활성화함.

### 2.3 인프라 및 구동 환경 복구
- **H2 Driver**: 유실된 `com.h2database:h2` 의존성을 `runtime` 스코프로 추가함.
- **Hibernate 7 Compatibility**: Hibernate 7의 H2 자동 감지 결함을 해결하기 위해 `application-dev.yml`에 `org.hibernate.dialect.H2Dialect` 설정을 수동으로 추가함.

## 3. 결과 요약
| 항목 | 결과 | 비고 |
| --- | --- | --- |
| Jackson 3 통합 | 성공 | `tools.jackson`으로 표준화됨 |
| SpringDoc 구동 | 성공 | `v3/api-docs` 스캐닝 정상 동작 |
| 앱 시작 (Port 8443) | 성공 | `dev`, `api-docs` 프로파일로 테스트 완료 |
| 유닛 테스트 (RateLimit) | 성공 | `RateLimitingFilterTest` 9개 통과 |

## 4. 변경 파일 목록
- `pom.xml`: Jackson BOM 추가, SpringDoc 버전 조정, H2 의존성 추가
- `src/main/resources/config/application-dev.yml`: Hibernate Dialect 명시적 설정
- `src/main/java/com/daangcool/stack/StackApp.java`: 자동 설정 기반 복구
- `src/test/java/com/daangcool/stack/web/filter/RateLimitingFilterTest.java`: Jackson 3 Mapper 사용

## 5. 잔여 리스크 및 리포트
- **Transitive Dependency**: 여전히 클래스패스 상에 Jackson 2 계열 라이브러리가 BOM에 의해 관리되며 존재함. 이는 라이브러리 제조사가 지원할 때까지 유지해야 함.
- **Annotation Mix Limitation**: Jackson 3.1.0 생태계에서도 핵심 어노테이션 패키지는 `com.fasterxml.jackson.annotation`으로 배포되고 있음을 확인(`tools.jackson.annotation`은 미배포). 도메인 객체의 어노테이션 변경 시도를 했으나, 물리적 라이브러리 부재로 인해 현재 상태(엔진 3 + 어노테이션 2)를 유지하는 것이 최종 결론임.


---
**Walkthrough 생성 확인**: `walkthrough.md` 참조
**AGENTS.md 준수 확인**: 완료
