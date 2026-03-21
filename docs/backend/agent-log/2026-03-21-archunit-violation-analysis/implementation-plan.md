# Implementation Plan - UserAuthCache Service Package Move

## 개요
`UserAuthCacheService`와 `UserAuthCacheDto`를 `service` 패키지에서 `security` 패키지로 이동하여 `TechnicalStructureTest` ArchUnit 위반을 해결합니다.

## 변경 대상 파일

### [MOVE] 클래스 및 테스트
- `src/main/java/com/daangcool/stack/service/UserAuthCacheService.java` -> `src/main/java/com/daangcool/stack/security/UserAuthCacheService.java`
- `src/main/java/com/daangcool/stack/service/dto/UserAuthCacheDto.java` -> `src/main/java/com/daangcool/stack/security/UserAuthCacheDto.java`
- `src/test/java/com/daangcool/stack/service/UserAuthCacheServiceTest.java` -> `src/test/java/com/daangcool/stack/security/UserAuthCacheServiceTest.java`

### [MODIFY] 패키지 선언 및 임포트 갱신
- `DomainUserDetailsService.java`: 임포트 경로 변경
- `UserService.java`: `UserAuthCacheService` 임포트 경로 변경
- `UserAuthCacheService.java`: 패키지 선언 변경 및 내부 DTO 참조 확인
- `UserAuthCacheDto.java`: 패키지 선언 변경
- `UserAuthCacheServiceTest.java`: 패키지 선언 변경 및 임포트 갱신

## 작업 단계
1. **파일 물리적 이동**: `run_command`를 통해 파일을 새로운 위치로 이동.
2. **코드 갱신**: 이동된 파일의 `package` 선언과 의존하는 파일들의 `import` 문을 순차적으로 수정.
3. **검증**: `./mvnw test -Dtest=TechnicalStructureTest,UserAuthCacheServiceTest,DomainUserDetailsServiceIT` 실행하여 확인.

## 가이드 준수
- 주석 업데이트: `DomainUserDetailsService` 클래스 주석에 패키지 이동 사유(ArchUnit 준수) 기록.
- `agent-log`: 각 단계 완료 후 `walkthrough.md` 작성.
