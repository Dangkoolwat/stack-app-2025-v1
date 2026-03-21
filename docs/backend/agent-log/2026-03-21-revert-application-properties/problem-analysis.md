# 문제 분석

## 문제 현상
`ApplicationProperties.java`가 여러 개의 개별 파일로 분리되어 있어 의존성 관리가 복잡해지고, 설정 파일 간의 관계를 파악하기 어려움. 또한, 분리된 구조에서 필드 타입이나 경로 설정에 불일치가 발생하여 테스트 코드가 실패하는 상황이 발생함.

## 재현 방법
1. 기존 분리된 `*Properties.java` 파일들이 존재하는 상태에서 프로젝트 빌드 및 테스트 수행.
2. `UploadServiceT`, `LocalDefaultFileStorageServiceT` 등에서 파일 경로 관련 Assertion 실패 발생.
3. `RateLimitingFilterTest`에서 기대하는 Rate Limit 정책 수치와 실제 Properties의 기본값이 일치하지 않아 429 에러 대신 200 응답이 반환됨.

## 원인
- **설정의 파편화**: 설정값이 여러 파일에 흩어져 있어 일관성 있는 수정이 어려움.
- **경로 접두사 중복**: `UploadFileUtils`가 이미 `/uploads`를 포함하여 경로를 반환함에도 불구하고, `StorageService` 구현체에서 중복으로 접두사를 붙임.
- **기본값 매칭 실패**: `ApplicationProperties`의 중첩 클래스 구조에서 `Policy` 클래스의 기본값이 모든 엔드포인트에 대해 동일하게 설정되어, 엔드포인트별 특화된 테스트 케이스의 기대를 충족하지 못함.

## 영향
- 시스템 설정의 유지보수성 저하.
- 파일 저장 및 조회 기능의 테스트 실패로 인한 신뢰도 하락.
- 보안 필터(Rate Limit)의 오동작 위험.
