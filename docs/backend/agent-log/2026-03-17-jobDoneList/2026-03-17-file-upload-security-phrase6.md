# 2026-03-17-file-upload-security-phrase6.md

- Date: 2026-03-17
- Agent: Antigravity
- Task Title: 파일 업로드 보안 강화 및 테스트 검증 (Phrase 6 / C-4)
- Goal: MIME 타입 및 확장자 검증 로직을 완성하고, 보안 테스트 코드를 통해 이를 검증.

## Context
- 보안 보고서(`2026-03-15-system-security-optimization-report.md`)의 C-4 항목 대응.
- `UploadService.java`의 기존 검증 로직을 고도화하고, `UploadServiceT.java`에 보안 시나리오 테스트 추가 필요.

## Work Performed
- UploadService.java 고도화:
    - `validateFile` 메서드에 try-with-resources 적용하여 `InputStream` 자원 누수 방지.
    - `validateFile`이 감지된 MIME 타입을 반환하도록 수정.
    - `saveUpload` 시 클라이언트가 제공한 `contentType` 대신 Tika가 실제 분석한 `detectedMimeType`을 DB에 저장하도록 변경 (보안성 강화).
- 보안 테스트 구현 및 검증 (`UploadServiceT.java`):
    - `saveUpload_ValidFile_ShouldStoreAndSaveMetadata`: 정삭적인 JPEG 파일(매직 바이트 포함) 업로드 검증.
    - `saveUpload_InvalidExtension_ShouldThrowInvalidFileException`: 허용되지 않는 확장자(.exe) 차단 검증.
    - `saveUpload_InvalidMimeType_ShouldThrowInvalidFileException`: 확장자는 위장했으나 실제 내용이 위험한(.sh) 코드 차단 검증.
    - `saveUpload_EmptyFile_ShouldThrowInvalidFileException`: 빈 파일 업로드 차단 검증.
- 빌드 및 테스트 확인:
    - `./mvnw test -Dtest=UploadServiceT` 실행 결과 8개 전체 테스트 통과 (기존 5개 + 신규/강화 3개).

## Files Modified
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/test/java/com/daangcool/stack/service/board/UploadServiceT.java`

## Architecture Impact
- 업로드 시점에 콘텐츠 분석을 강제함으로써 변조된 데이터가 시스템에 유입되는 것을 원천 차단.

## Security Impact
- MIME Spoofing Prevention: 확장자와 무관하게 실제 바이너리 코드를 분석하여 허용된 타입만 저장.
- Improved Served Content Trust: 서빙(다운로드/미리보기) 시 DB에 저장된 신뢰할 수 있는 MIME 타입을 사용함으로써 XSS 및 Drive-by download 위험 제거.

## Verification
- `UploadServiceT.java`의 단위 테스트를 통해 다양한 악성 파일 시나리오 검증 완료.

## Risks
- 대용량 파일의 경우 Tika 분석 시 약간의 성능 오버헤드가 발생할 수 있으나, 현재 설정된 8KB 분석 범위 내에서는 미미함.

## Next Suggested Tasks
- `UploadResource.java` 뿐만 아니라 다른 리소스에서도 `Upload` 엔티티의 `mimeType`을 신뢰하여 사용하고 있는지 전수 조사.
- `ApplicationProperties`에 정의된 허용 목록(`allowedExtensions`, `allowedMimeTypes`)을 운영 환경에 맞게 추가 조정.
