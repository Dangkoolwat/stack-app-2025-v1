# 자기 점검 (Self-Check)

- [x] **아키텍처 준수**: `FileTypePolicy`, `FileUploadDefaults`를 `domain.vo`로 이동하여 계층 구조를 준수함.
- [x] **보안 영향**: Apache Tika를 도입하여 실제 바이너리 기반 MIME 타입 검증을 강화함.
- [x] **데이터 무결성**: 정책 변경 시 즉시 반영 및 캐시 정합성을 확보함.
- [x] **테스트 전략**: ArchUnit 테스트 및 `UploadServiceT` 단위 테스트를 통해 기능과 구조를 검증함.
- [x] **영향도 체크**: 기존 업로드 로직과의 하위 호환성을 유지함.
