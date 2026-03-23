# 제안 (Proposal) - v2 (업데이트됨)

## 제안 배경
- 설정 정보를 개별 컬럼에서 `global_settings` (JSON) 필드 하나로 통합하여 저장소의 유연성을 확보합니다.
- Spring Boot 4 환경에 최적화된 Hibernate 7 설계를 채택합니다.
- 애플리케이션 시작 시 설정 정보를 미리 캐싱하여 성능을 최적화합니다.

## 제안 사항

### 1. Hibernate 7 기반 JSON 매핑
- `Settings` 엔티티에 `globalSettings` 필드를 도입하고, Hibernate 7의 `@JdbcTypeCode(SqlTypes.JSON)`를 사용하여 매핑합니다.
- Oracle DB 환경을 고려하여 `CLOB` 저장소와 JSON 데이터 타입을 활용합니다.
- 기존 필드 접근용 Getter/Setter를 유지(델리게이트 방식)하여 기존 코드(Service, DTO)와의 호환성을 유지합니다.

### 2. Liquibase 초기 설정 방식 변경
- `stack_settings` 테이블을 처음부터 `id`와 `global_settings` 필드만 가지도록 구성합니다.
- 초기 데이터(ID=1) 삽입 시 JSON 형식으로 기본값을 주입합니다.

### 3. 초기 캐시 워밍업 (Cache Warm-up)
- `CacheWarmupRunner`에 `GlobalSettingsService`를 주입하여, 애플리케이션 기동 시점에 설정 정보를 DB에서 읽어 Redis 캐시에 저장합니다.

## 리스크 및 고려사항
- **데이터 구조 유연성**: JSON 내부 구조가 변경되어도 DB 스키마 변경이 필요 없으나, 애플리케이션 레벨에서 데이터 정합성(Validation)을 관리해야 합니다.
- **캐시 일관성**: 설정 변경 시 캐시를 즉시 무효화(`evict`)하는 기존 로직을 유지하여 일관성을 보장합니다.

## Is this the correct direction?
업데이트된 방향으로 진행해도 될까요?
