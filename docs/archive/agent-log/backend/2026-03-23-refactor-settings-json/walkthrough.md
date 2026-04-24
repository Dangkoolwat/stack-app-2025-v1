# 구현 과정 (Walkthrough)

## 변경 사항 상세

### 1. Database (Liquibase)
- `20251005203000_added_entity_Settings.xml` 파일을 수정하여 `stack_settings` 테이블의 구조를 간소화했습니다.
- 개별 컬럼 대신 `global_settings` (CLOB) 컬럼 하나만 사용하며, 초기 데이터를 JSON 문자열로 주입하도록 설정했습니다.

### 2. Domain (Settings.java)
- 기존 개별 설정 필드들을 제거하고 `globalSettings` (String) 필드를 추가했습니다.
- `description` 필드는 주요 메타데이터로서 별도 컬럼으로 분리 유지하며, `@Lob` 어노테이션을 제거하여 DB 리소스를 최적화했습니다.
- Hibernate 7 및 Jackson 3를 활용하여 JSON 파싱 로직을 엔티티 내부에 델리게이트 메서드 형태로 구현했습니다.
- `getTokenValiditySeconds()`, `setTokenValiditySeconds()` 등의 기존 메서드 시그니처를 유지하여 외부 서비스 코드와의 호환성을 확보했습니다.

### 3. Service (GlobalSettingsService.java)
- 엔티티의 변경된 구조(JSON 기반)를 자연스럽게 활용합니다.
- 기존 비즈니스 로직 및 캐시 처리 방식이 그대로 유지됩니다.

### 4. 설정 워밍업 개선 (CacheWarmupRunner.java)
- `spring.liquibase.drop-first: true` 설정 시 워밍업을 명시적으로 건너뛰도록 수정했습니다.
- 개별 설정 워밍업 과정에서 예외 발생 시 치명적 오류로 간주하지 않고 로그를 남긴 후 다음 단계를 진행하도록 예외 처리를 강화했습니다.

### 5. 테스트 코드 추가
- SettingsTest.java: `Settings` 엔티티의 JSON 델리게이트 로직을 검증하는 단위 테스트를 추가했습니다.
- GlobalSettingsServiceIT.java: 서비스 계층에서 DB와 연동하여 JSON 데이터가 올바르게 저장 및 조회되는지 확인하는 통합 테스트를 추가했습니다.

## 핵심 포인트
- 호환성: DB 구조는 바뀌었지만 Java 레벨에서의 Getter/Setter 인터페이스를 유지하여 전체적인 코드 수정을 최소화했습니다.
- 성능: 시작 시 캐시 워밍업을 통해 첫 번째 요청부터 빠른 응답 속도를 보장합니다.
- 유연성: 향후 새로운 설정 항목이 추가될 때 DB 스키마 변경 없이 DTO와 엔티티의 JSON 항목만 추가하면 됩니다.
