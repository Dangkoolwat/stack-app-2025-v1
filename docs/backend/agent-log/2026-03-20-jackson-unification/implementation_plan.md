# Implementation Plan - Jackson Dependency Unification (Jackson 3)

현재 프로젝트에서 혼용되고 있는 Jackson 2(com.fasterxml)를 제거하고, Spring Boot 4의 권장 사항에 따라 Jackson 3(tools.jackson)으로 단일화합니다.

## 1. 분석 단계
- [ ] `pom.xml` 내 Jackson 2 관련 의존성 및 BOM 확인
- [ ] `mvn dependency:tree`를 통한 전이 의존성(transitive dependency) 내 Jackson 2 확인
- [ ] 전체 소스 코드에서 `com.fasterxml.jackson` import 사용 현황 파악
- [ ] `ObjectMapper`를 `new`로 직접 생성하는 위치 파악

## 2. 의존성 정리 (pom.xml)
- [ ] `com.fasterxml.jackson:jackson-bom` 제거
- [ ] 모든 `com.fasterxml.jackson` 관련 의존성 제거 및 필요 시 `exclusion` 처리
- [ ] `tools.jackson:jackson-bom` 및 관련 Jackson 3 의존성 확인 및 최신화
- [ ] `jackson-databind-nullable` 등 Jackson 2 의존성이 있는 라이브러리의 Jackson 3 호환 버전 확인 또는 exclusion 설정

## 3. 코드 수정
- [ ] `com.fasterxml.jackson.*` -> `tools.jackson.*` 패키지명 전수 교체
- [ ] `new ObjectMapper()` 호출부를 Spring Bean 주입 방식으로 변경 (`@Autowired` 또는 생성자 주입)
- [ ] `CacheConfiguration` 등 커스텀 `ObjectMapper` 설정 로직 수정

## 4. Swagger / OpenAPI 연동 확인
- [ ] `springdoc-openapi`가 Jackson 3를 정상적으로 사용하는지 확인
- [ ] `/v3/api-docs` 엔드포인트 및 Swagger UI 동작 확인

## 5. 검증 및 테스트
- [ ] `mvn clean compile` 수행 (패키지명 변경 확인)
- [ ] `mvn clean package` 수행 (테스트 통과 확인)
- [ ] 애플리케이션 실행 및 주요 API(로그인, 캐시 사용 API) 동작 확인
- [ ] JSON 직렬화/역직렬화 오류 여부 최종 점검

> [!IMPORTANT]
> Annotation Package Limitation: Jackson 3.1.0은 내부적으로 Jackson 2 어노테이션(`com.fasterxml.jackson.annotation`)을 여전히 패키지로 공유하고 있습니다. 따라서 도메인 엔티티들의 임포트 경로는 현시점에서 `com.fasterxml`을 유지하는 것이 정상 구동을 위해 필수적입니다.

## 6. 완료 보고
- [ ] 에이전트 로그 및 Walkthrough 생성
- [ ] 최종 결과 요약 보고
