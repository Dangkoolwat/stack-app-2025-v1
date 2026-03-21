# 최종 보고서 (Final Report)

## 1. 개요
Security 레이어가 Service 레이어에 의존하던 ArchUnit 위반 사항을 해결하고, 인증 캐시 시스템의 안정성과 호환성을 개선함.

## 2. 수행 내용
- **패키지 이동**: `UserAuthCacheService`, `UserAuthCacheDto`를 `security` 패키지로 이동하여 계층 구조 준수.
- **DTO 개선**: `UserAuthCacheDto`를 `record`에서 일반 `class`로 변경하여 Jackson 3의 Redis 직렬화/역직렬화 오류 해결.
- **로직 강화**: `DomainUserDetailsService`에 이메일 우선 -> 로그인 ID 차선(Fallback) 조회 정책 적용 및 정규식 검증 버그 수정.
- **API 현대화**: Redisson의 TTL 설정을 `Duration` API로 전환하여 Deprecated 경고 제거.
- **테스트 보완**: `DomainUserDetailsServiceIT` 테스트 데이터에 권한(`ROLE_USER`) 정보 추가하여 테스트 성공 보장.

## 3. 변경 파일
- `com.daangcool.stack.security.UserAuthCacheDto` [MODIFY]
- `com.daangcool.stack.security.UserAuthCacheService` [MODIFY]
- `com.daangcool.stack.security.DomainUserDetailsService` [MODIFY]
- `com.daangcool.stack.security.DomainUserDetailsServiceIT` [MODIFY]
- `com.daangcool.stack.security.UserAuthCacheServiceTest` [MODIFY]

## 4. 검증 결과
- `./mvnw test -Dtest=TechnicalStructureTest,UserAuthCacheServiceTest,DomainUserDetailsServiceIT` 실행 결과 모두 **SUCCESS**.

## 5. 결론 및 향후 과제
- 아키텍처 위반 사항이 완전히 해소되었으며, 인증 캐시가 안정적으로 작동함. 향후 다른 캐시 DTO들도 동일한 패턴(Class 기반 + Duration 사용)으로 통일하는 것을 권장함.
