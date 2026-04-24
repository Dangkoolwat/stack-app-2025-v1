# 구현 계획

## 1단계: ApplicationProperties 구조 통합
- `ApplicationProperties.java`에 `Redis`, `File`, `AuthCache`, `RateLimit`, `Security`, `Cache`, `Liquibase`, `Logging` 정적 클래스 추가.
- `RateLimit.Policy` 클래스에 기본값 설정을 위한 생성자 추가.

## 2단계: 의존성 코드 수정 및 최적화
- `RateLimitingFilter.java`의 Import 및 타입 참조 수정.
- `LocalDefaultFileStorageService.java`, `S3FileStorageService.java`, `ShareFileStorageService.java`의 중복 경로 접두사 로직 제거 (`webPath = storagePath`).

## 3단계: 테스트 코드 수정
- `UploadServiceT`, `LocalDefaultFileStorageServiceT`, `S3FileStorageServiceT`, `ShareFileStorageServiceT`에서 변경된 Properties 참조 및 Assertion 값 업데이트.
- `RateLimitingFilterTest`의 실패 케이스 대응.

## 4단계: 정리 및 문서화
- 불필요해진 개별 Java 파일 삭제.
- 코드 내 한글 상세 주석 추가.
