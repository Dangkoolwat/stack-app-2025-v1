# 구현 계획 (Implementation Plan) - 동적 파일 업로드 정책 관리

현재 정적으로 설정된 파일 업로드 정책(확장자, MIME 타입)을 데이터베이스(`Settings` 엔티티)에서 관리하고 운영자가 UI를 통해 동적으로 변경할 수 있도록 확장합니다.

## 제안된 변경 사항

### Backend - Domain / Service

#### [MODIFY] [Settings.java](file:///src/main/java/com/daangcool/stack/domain/Settings.java)
- `global_settings` JSON 구조에 파일 업로드 관련 필드 추가.
- 개별 필드 접근을 위한 델리게이트 메서드 추가.

#### [NEW] [FileTypePolicy.java](file:///src/main/java/com/daangcool/stack/service/dto/FileTypePolicy.java)
- 정책 항목을 정의하는 DTO.

#### [MODIFY] [SettingsDTO.java](file:///src/main/java/com/daangcool/stack/service/dto/SettingsDTO.java)
- UI와의 통신을 위해 필드 추가.

#### [MODIFY] [UploadService.java](file:///src/main/java/com/daangcool/stack/service/board/UploadService.java)
- `validateFile()` 메서드 리팩토링 및 동적 검증 로직 구현.

### Backend - Migration / Config

#### [MODIFY] [20251005203000_added_entity_Settings.xml](file:///src/main/resources/config/liquibase/changelog/20251005203000_added_entity_Settings.xml)
- 초기 데이터에 권장 기본 정책 추가.

### Frontend - Vue 3 UI (vuu3)

#### [MODIFY] [settings.vue](file:///src/main/webapp/app/entities/settings/settings.vue) / [settings.component.ts](file:///src/main/webapp/app/entities/settings/settings.component.ts)
- "파일 업로드 정책" 섹션 및 CRUD UI 추가.

## 검증 계획

### 자동 테스트
- `./mvnw test -Dtest=SettingsTest,GlobalSettingsServiceIT,UploadServiceTest`

### 수동 검증
1. 관리자 UI에서 파일 업로드 정책 수정/저장.
2. 실제로 파일 업로드 시 정책 즉시 반영 확인.
3. Swagger UI에서 API 문서 확인.
