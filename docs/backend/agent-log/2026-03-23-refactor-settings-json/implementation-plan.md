# 구현 계획 (Implementation Plan) - v5 (필드 분리 및 @Lob 제거)

## 요구 사항 반영
1. **`description` 필드 분리**: `global_settings` JSON 내부가 아닌 별도 컬럼으로 관리.
2. **`@Lob` 제거**: `global_settings` 데이터가 크지 않으므로 `@Lob` 대신 `VARCHAR` 계열 사용.

## 제안된 변경 사항

### Database (Liquibase)

#### [MODIFY] [20251005203000_added_entity_Settings.xml](file:///src/main/resources/config/liquibase/changelog/20251005203000_added_entity_Settings.xml)
- `global_settings` 컬럼 타입을 `clob` 에서 `varchar(2000)` 으로 변경.
- `description` 컬럼 추가 (`varchar(255)`).
- 초기 데이터 `insert` 문에 `description` 컬럼 값 추가 및 `global_settings` JSON에서 `description` 항목 제거.

### Backend (Java)

#### [MODIFY] [Settings.java](file:///src/main/java/com/daangcool/stack/domain/Settings.java)
- `globalSettings` 필드에서 `@Lob` 어노테이션 제거. `@Column(length = 2000)` 추가.
- `description` 필드를 유지하고, `getDescription()` 및 `setDescription()` 메서드가 더 이상 JSON Map을 통하지 않고 직접 필드에 접근하도록 수정.

### Testing & Verification

#### [MODIFY] [SettingsTest.java](file:///src/test/java/com/daangcool/stack/domain/SettingsTest.java)
- `description`이 별도 필드로 동작하는지 검증하도록 테스트 코드 수정.

#### [MODIFY] [GlobalSettingsServiceIT.java](file:///src/test/java/com/daangcool/stack/service/GlobalSettingsServiceIT.java)
- 통합 테스트 시 `description` 값이 DB 컬럼에 올바르게 저장되는지 확인.

## 검증 계획

### 자동 테스트
- `./mvnw test -Dtest=SettingsTest,GlobalSettingsServiceIT`

### 수동 검증
- 메이븐 커맨드로 DB 초기화: `./mvnw liquibase:dropAll liquibase:update`
- H2 Console 또는 DB 툴로 `stack_settings` 테이블 스키마 및 데이터 구조 확인.
