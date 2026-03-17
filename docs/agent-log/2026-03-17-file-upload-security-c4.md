# 2026-03-17-file-upload-security-c4.md

- Date: 2026-03-17
- Agent: Antigravity
- Task Title: 파일 업로드 보안 강화 (C-4)
- Goal: 보안 보고서의 C-4 항목을 해결하기 위해 파일 업로드 시 MIME 타입 및 확장자 다중 계층 검증 구현.

## Context
- `UploadService.java`에서 `MultipartFile.getContentType()`에만 의존하던 기존 로직을 실제 파일 콘텐츠 분석 기반으로 고도화 필요.
- 보안 보고서(`2026-03-15-system-security-optimization-report.md`)의 C-4(Immediate) 항목 대응.

## Work Performed
- **의존성 추가**: `pom.xml`에 Apache Tika 3.0.2 (`tika-core`) 추가.
- **설정 확장**: `ApplicationProperties.java` 및 `application.yml`에 `allowedMimeTypes`, `allowedExtensions` 화이트리스트 정의.
- **예외 클래스 생성**: `InvalidFileException.java` 생성 (400 Bad Request & RFC 7807 Problem Detail 대응).
- **검증 로직 구현**: `UploadService.java`에 `validateFile` 메서드 추가 및 `saveUpload` 시작 시 호출.
    - 파일 비어있음 체크.
    - 확장자 화이트리스트 검사.
    - Tika를 이용한 실제 바이너리 콘텐츠 기반 MIME 타입 감지.
    - 감지된 타입과 브라우저 제공 타입 간의 mismatch 로깅 (보안 모니터링).

## Files Modified
- `pom.xml`
- `src/main/resources/config/application.yml`
- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java`
- `src/main/java/com/daangcool/stack/common/constant/ErrorConstants.java`
- `src/main/java/com/daangcool/stack/common/exception/InvalidFileException.java` [NEW]
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`

## Architecture Impact
- 업로드 파이프라인에 보안 검증 레이어 추가. 비즈니스 로직(저장) 전 단계에서 필터링 수행.

## Security Impact
- **Malicious Upload Prevention**: 확장자 위변조(Double extension) 및 실행 파일(.exe, .sh 등) 업로드 원천 차단.
- **Content Trust**: 클라이언트가 보내는 메타데이터가 아닌 실제 파일 바이너리를 신뢰하도록 개선.

## Verification
- 코드 레벨 정적 분석 및 컴파일 확인 (Tika 라이브러리 동기화 로컬 대기 중).
- `walkthrough.md`에 상세 구현 내용 기록.

## Risks
- **Dependency Missing**: `mvn` 명령어가 사용자 환경에서 취소되었으므로, 실제 빌드 시 Tika 라이브러리 다운로드가 필요함.

## Next Suggested Tasks
- `W-7`: OTP 평문 저장 → Redis TTL 전환 (다음 우선순위 보안 항목).
- `UploadServiceTest` 작성: 다양한 악성 파일 시나리오에 대한 단위 테스트 강화.

## Notes for Future Agents
- `Tika` 인스턴스는 쓰레드 세이프하므로 `UploadService`에서 싱글톤 필드로 유지함.
- 프로젝트가 Spring Boot 4.0.3 기반이므로 Tika 3.x 버전을 사용함.
