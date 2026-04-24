# Walkthrough (구현 흐름)

## 핵심 변경 사항

### 1. ApplicationProperties 통합 및 주석 추가
- 모든 설정을 하나의 파일로 모아 응집도를 높였습니다.
- `authenticate(10/5)`, `otpRequest(5/10)` 등 보안 정책별로 차등화된 기본값을 생성자를 통해 명시적으로 설정했습니다.
- 각 설정 블록마다 상세한 한글 주석을 달아 가독성을 확보했습니다.

### 2. 스토리지 경로 처리 수정
- `UploadFileUtils.fileSave`의 반환값에 이미 `/uploads` 접두사가 포함되어 있음을 확인했습니다.
- 따라서 서비스 계층에서 `webPath = prefix + storagePath`와 같이 중복으로 더하던 로직을 `webPath = storagePath`로 단순화했습니다.

### 3. 테스트 안정성 확보
- `S3Client` 모킹 시 `ResponseInputStream` 타입 캐스팅 오류를 해결하기 위해 Mocking 방식을 정교화했습니다.
- `Mockito.lenient()`를 사용하여 불필요한 스터빙 예외를 방지했습니다.

## 핵심 포인트
- 설정 구조는 단순해졌으나, 내부에 주석을 상세히 달아 복잡한 설정도 쉽게 관리할 수 있도록 했습니다.
- 단위 테스트를 통해 실제 경로가 `/uploads/public/...` 형태로 정확히 생성됨을 검증했습니다.
