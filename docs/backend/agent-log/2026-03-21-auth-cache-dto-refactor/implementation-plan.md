# implementation-plan.md

## 작업 단계

### Phase 1: 인증 2차 캐시 도입

1. `UserAuthCacheDto` (record) 생성
   - 단순 타입 필드만 포함, 민감정보 제외
   - `from(User user)` 정적 변환 메서드 (트랜잭션 안 호출 필수)

2. `UserAuthCacheService` 생성
   - `get()` / `put()` / `evict()` 메서드
   - `redissonJsonClient` 빈 재사용
   - Redis 장애 시 예외 삼키고 `Optional.empty()` 반환 (fallback 보장)
   - TTL: `ApplicationProperties.AuthCache.ttlMinutes` 주입

3. `ApplicationProperties` 수정
   - `AuthCache` 내부 클래스 추가 (`ttlMinutes` 필드)
   - `application.yml` 에 `application.auth-cache.ttl-minutes: 5` 추가

4. `DomainUserDetailsService` 수정
   - 1단계: Redis 캐시 조회 → 2단계: DB 조회 (MISS) → 3단계: DTO 저장
   - `toUserDetails(UserAuthCacheDto)` 헬퍼 추가

5. `UserService` 수정
   - 상태 변경 5개 메서드에 `userAuthCacheService.evict()` 연동
   - `changePassword`, `updateUser`(×2), `activateRegistration`, `deleteUser`, `removeNotActivatedUsers`

### Phase 2: 기존 엔티티 직접 캐시 수정

6. `UploadDTO` 보강
   - `storageKey`, `storageFilename`, `isPublic`, `downloadCount`, `deleted` 필드 추가

7. `UploadService` 수정
   - `findById()`: `Upload` 엔티티 → `UploadDTO` 캐시 저장, `toUploadEntity()` 헬퍼 추가
   - `findAllByBoard()`: `List<Upload>` → `List<UploadDTO>` 캐시 저장

8. `CommonCodeCacheDto` 생성
   - `GroupDto` (record): groupCode, groupName, displayOrder 등 단순 타입
   - `DetailDto` (record): id, code, name, groupCode(String) 등 단순 타입

9. `CommonCodeService` 수정
   - `@Cacheable` 4개 제거
   - 명시적 CacheManager + DTO 패턴으로 전환
   - `toGroupEntity()` / `toDetailEntity()` 복원 헬퍼 추가

### Phase 3: 문서 수정 (에이전트 오판 방지)

10. `docs/backend/Architecture.md` 수정
    - `Security Defaults`: JPA 엔티티 직접 캐시 금지 이유 구체화
    - `Caching Rules`: 허용/금지 명확 분리, TTL 세분화, fallback 규칙 추가

11. `docs/backend/Engineering_Guideline.md` 수정
    - `5.2 Cache 변경 작업 Step 2`: 금지 판별 표 + ⚠️ 에이전트 오판 방지 경고문
    - `5.2 Step 3`: 캐시 전용 DTO 설계 조건 + 코드 예시
    - `5.4 리뷰 체크리스트`: 7개 구체적 점검 항목으로 세분화

12. `AGENTS.md` 수정
    - `Security by Default`: Cache Safety 세부 규칙 추가

### Phase 4: 테스트

13. `UserAuthCacheServiceTest` 작성 (Mockito 단위 테스트, 10개 케이스)
14. `DomainUserDetailsServiceIT` 수정 (캐시 검증 통합 테스트 4개 추가)

## 수정 파일 목록

| 파일 | 작업 |
|------|------|
| `service/dto/UserAuthCacheDto.java` | 신규 생성 |
| `service/UserAuthCacheService.java` | 신규 생성 |
| `config/ApplicationProperties.java` | AuthCache 내부 클래스 추가 |
| `security/DomainUserDetailsService.java` | 캐시 레이어 추가 |
| `service/UserService.java` | evict() 연동 |
| `service/dto/UploadDTO.java` | 필드 보강 |
| `service/board/UploadService.java` | DTO 캐시 전환 |
| `service/dto/CommonCodeCacheDto.java` | 신규 생성 |
| `service/common/CommonCodeService.java` | @Cacheable 제거 + DTO 캐시 |
| `resources/config/application.yml` | ttl-minutes 설정 추가 |
| `docs/backend/Architecture.md` | 캐시 규칙 개정 |
| `docs/backend/Engineering_Guideline.md` | 5.2 / 5.4 개정 |
| `AGENTS.md` | Cache Safety 규칙 추가 |
| `test/.../UserAuthCacheServiceTest.java` | 신규 생성 |
| `test/.../DomainUserDetailsServiceIT.java` | 캐시 검증 테스트 추가 |
