# 동적 파일 업로드 정책 관리 구현 계획 (Dynamic File Upload Policy Management)

현재 정적으로 설정된 파일 업로드 정책(확장자가, MIME 타입)을 데이터베이스(`Settings` 엔티티)에서 관리하고 운영자가 UI를 통해 동적으로 변경할 수 있도록 확장합니다.

## 제안된 변경 사항

### [Component] Backend - Domain / Service

#### [MODIFY] [Settings.java](file:///src/main/java/com/daangcool/stack/domain/Settings.java)
- `global_settings` JSON 구조에 파일 업로드 관련 필드 추가:
    - `fileUploadDefaults`: 전역 기본 최대 용량, 요청 크기, 매칭 실패 시 처리 방식 등.
    - `fileTypePolicies`: 타입별 정책 리스트 (`List<FileTypePolicy>`).
- 개별 필드 접근을 위한 델리게이트 메서드 추가.

#### [NEW] [FileTypePolicy.java](file:///src/main/java/com/daangcool/stack/service/dto/FileTypePolicy.java)
- 정책 항목을 정의하는 DTO:
    - `key`, `label`, `enabled`, `allowedExtensions`, `allowedMimeTypes`, `maxFileSizeBytes`, `displayOrder`, `description`.
    - 향후 확장을 위한 `metadata` (Map) 포함.

#### [MODIFY] [SettingsDTO.java](file:///src/main/java/com/daangcool/stack/service/dto/SettingsDTO.java)
- UI와의 통신을 위해 `fileUploadDefaults` 및 `fileTypePolicies` 필드 추가.

#### [MODIFY] [UploadService.java](file:///src/main/java/com/daangcool/stack/service/board/UploadService.java)
- `validateFile(MultipartFile file)` 메서드 리팩토링:
    - `GlobalSettingsService`를 통해 현재 활성화된 정책 목록 조회.
    - 확장자 및 Tika 감지 MIME 타입을 기반으로 매칭되는 정책 탐색.
    - 정책 유무, 활성화 여부, 용량 제한을 동적으로 검증.
    - 적절한 한국어 예외 메시지 반환.

### [Component] Backend - Migration / Config

#### [MODIFY] [20251005203000_added_entity_Settings.xml](file:///src/main/resources/config/liquibase/changelog/20251005203000_added_entity_Settings.xml)
- 초기 `insert` 데이터에 권장 기본 정책(JPEG, PNG, PDF 등)을 포함한 JSON 구조 반영.

### [Component] Frontend - Vue 3 UI (vuu3)

- 용량 입력 시 MB 단위 지원 및 서버 전송 시 Bytes 변환.

### [Component] Template Management (NEW)
- Backend Model: `Settings` 엔티티 내 `global_settings` JSON에 `fileTypeTemplates` 필드 추가.
- Liquibase: 초기 템플릿 데이터(이미지, PDF, 오피스 등)를 `fileTypeTemplates`에 사전 주입하여 하드코딩 제거.
- Frontend UI:
    - "템플릿 관리" 섹션 또는 탭 추가 (CRUD 기능).
    - 기존 "Quick Add" 및 "마법 지팡이" 메뉴가 DB의 `fileTypeTemplates` 데이터를 기반으로 동작하도록 수정.

## 검증 계획

### 자동 테스트 (Automated Tests)
- 백엔드 단위 테스트: `SettingsTest.java`에 새 필드 직렬화/역직렬화 테스트 추가.
- 백엔드 통합 테스트: `GlobalSettingsServiceIT.java`에서 설정 저장 및 조회 테스트.
- 업로드 검증 테스트: `UploadServiceTest.java` (신규) 또는 기존 테스트 기능을 확장하여 동적 정책 매칭 로직 검증.
    - 허용/차단 확장자/MIME 테스트.
    - 용량 초과 테스트.
    - 정책 비활성화 테스트.

### 수동 검증 (Manual Verification)
1. 관리자 UI 로그인 후 글로벌 설정 메뉴 진입.
2. 파일 업로드 정책 섹션에서 새 정책(예: mp4/video) 추가 및 저장.
3. 실제로 해당 파일을 업로드하여 허용 여부 확인.
4. 정책을 비활성화하거나 용량을 줄인 후 즉시 업로드 차단되는지 확인.
5. Swagger UI (`/v3/api-docs`)에서 `Settings` 관련 필드와 한국어 설명 확인.
6. DB 초기화(`/mvnw liquibase:dropAll liquibase:update`) 후 기본 추천 정책이 잘 들어갔는지 확인.
