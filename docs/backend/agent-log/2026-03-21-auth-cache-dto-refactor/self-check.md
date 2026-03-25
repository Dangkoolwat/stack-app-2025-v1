# self-check.md

## 아키텍처 위반 여부

- [ ] Controller 계층에서 Entity 직접 반환: 없음 (BAN 2 미위반)
- [ ] 캐시 로직이 Service 레이어 외부에 위치: 없음
- [ ] RedissonClient 중복 생성: 없음 (기존 `redissonJsonClient` 빈 재사용)
- [ ] `ApplicationProperties` 외부 설정 누락: 수정 완료 (`AuthCache` 내부 클래스 추가)

## Breaking Change 여부

- API 응답 계약 변경: 없음
- DB 스키마 변경: 없음 (Liquibase 불필요)
- 기존 Redis 캐시 데이터 형식 변경: 있음
  → 기존 JPA 엔티티 형태 데이터와 새 DTO 형태 데이터 공존 시 역직렬화 오류 가능
  → 배포 전 Redis FLUSHALL 또는 Docker Redis 재시작 필수

## 보안 영향

- `UserAuthCacheDto` 에 `password`, `activationKey`, `resetKey` 미포함 → 민감정보 노출 없음
- 캐시 TTL 5분 적용 → Access Token 유효기간보다 짧게 유지
- 상태 변경(비밀번호·권한·활성화·탈퇴) 시 즉시 evict() 호출 → stale 인증 방지
- Redis 장애 시 DB fallback → 서비스 중단 없이 인증 정상 동작

## 설정/의존성 영향

- `application.yml` 에 `application.auth-cache.ttl-minutes: 5` 추가
- `ApplicationProperties` 에 `AuthCache` 내부 클래스 추가
- 신규 클래스: `UserAuthCacheDto`, `UserAuthCacheService`, `CommonCodeCacheDto`
- 기존 클래스 수정: `DomainUserDetailsService`, `UserService`, `UploadDTO`, `UploadService`, `CommonCodeService`
- 신규 의존성: 없음 (기존 Redisson 빈 재사용)

## 테스트 계획

- `UserAuthCacheServiceTest` (Mockito 단위 테스트, 10개 케이스): Redis 불필요
- `DomainUserDetailsServiceIT` (통합 테스트, 기존 6 + 신규 4개): Redis TestContainer 필요
- 전체 테스트: `mvn test`
- 배포 전 Redis 초기화 필수: `redis-cli FLUSHALL` 또는 Docker 재시작
