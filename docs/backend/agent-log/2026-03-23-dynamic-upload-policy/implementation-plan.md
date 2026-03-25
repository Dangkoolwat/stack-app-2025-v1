# 구현 계획 (Implementation Plan)

## 단계별 계획
1. 모델 정립: `FileTypePolicy`, `FileUploadDefaults`를 `domain.vo` 패키지로 이동 및 `Settings` 엔티티 연동.
2. 백엔드 서비스: `GlobalSettingsService`에 JSON 파싱 및 캐싱 로직 구현. `UploadService` 검증 로직을 동적 정책 기반으로 전환.
3. 보안 강화: Apache Tika 의존성 추가 및 `validateFile`에서 파일 콘텐츠 분석 적용.
4. 초기 데이터: Liquibase를 통해 표준 이미지 및 PDF 정책 주입.
5. 관리자 UI: Vue 3 컴포넌트(`settings.vue`) 수정 및 템플릿 기반 빠른 설정 기능 추가.
6. 검증: ArchUnit 테스트 및 `UploadServiceT`를 통한 시나리오 검증.

## 변경 파일 목록
- `pom.xml`: Apache Tika 추가
- `Settings.java`: imports 및 VO 연동
- `FileTypePolicy.java`, `FileUploadDefaults.java`: [NEW] domain.vo로 이동
- `GlobalSettingsService.java`: 조회/저장 로직 고도화
- `UploadService.java`: 동적 검증 및 Tika 적용
- `settings.vue`, `settings.component.ts`: UI 및 템플릿 기능 구현
- `config-bootstrap-vue.ts`: `BDropdown` 등록
