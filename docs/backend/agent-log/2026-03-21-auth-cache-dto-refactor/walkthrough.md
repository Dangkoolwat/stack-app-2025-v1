# walkthrough.md

## 구현 흐름 요약

### 핵심 원칙 확립

"캐시를 쓰면 안 된다" →  잘못된 결론
"JPA 엔티티를 캐시 대상으로 쓰면 안 된다" →  올바른 원칙

JPA 엔티티는 Hibernate 가 `$$HibernateProxy$$xxxx` 프록시 클래스로 감싸며,
Jackson 3 의 `DefaultTyping` 이 이 클래스명을 `@class` 에 저장한다.
역직렬화 시 해당 프록시 클래스가 없으므로 반드시 실패한다.

해결책: 캐시 대상을 `record` 또는 단순 POJO DTO 로 변환하여 저장한다.

---

### 1단계: 인증 2차 캐시 (UserAuthCacheDto)

```
로그인 요청
    ↓
DomainUserDetailsService.loadUserByUsername()
    ↓
[1단계] Redis 조회: auth:user:{login}
    ├── HIT  → UserAuthCacheDto → toUserDetails() 변환 → 반환
    └── MISS → DB 조회 → UserAuthCacheDto.from(user) → Redis 저장 → 반환
```

`UserAuthCacheDto` 는 Java record 로 설계:
- 단순 타입만 포함 (`Long`, `String`, `boolean`, `Set<String>`)
- `@class` 값: `com.daangcool.stack.service.dto.UserAuthCacheDto` (안정적)
- 민감 정보(`password`, `activationKey`) 완전 제외

상태 변경 시 즉시 evict():
- `UserService.changePassword()` → evict()
- `UserService.updateUser()` (관리자/본인) → evict()
- `UserService.activateRegistration()` → evict()
- `UserService.deleteUser()` → evict()
- `UserService.removeNotActivatedUsers()` → evict()

---

### 2단계: CommonCodeService @Cacheable 제거

기존 `@Cacheable(value = COMMON_GROUP_LIST_CACHE)` 로 `CommonCodeGroup` 엔티티 직접 캐시
→ `CommonCodeGroup.details` (`@OneToMany` Lazy) 직렬화 실패

수정 후: 명시적 CacheManager + `CommonCodeCacheDto.GroupDto` (record) 저장
- `from(CommonCodeGroup g)`: 트랜잭션 안에서 단순 타입만 추출
- `toGroupEntity(GroupDto dto)`: 캐시 히트 시 경량 엔티티 복원 (연관관계 null)

---

### 3단계: UploadService 엔티티 직접 캐시 제거

기존 `Cache.put(id, upload)` 로 `Upload` JPA 엔티티 직접 저장
→ `AbstractAuditingEntity` 상속 + `@ManyToOne Board` (Lazy) 직렬화 실패

수정 후: `UploadDTO` (Lombok @Data) 로 저장, `toUploadEntity()` 헬퍼로 복원

---

### 4단계: ApplicationProperties 바인딩 오류 수정

`application.yml` 에 `application.auth-cache.ttl-minutes` 추가 후 기동 실패
→ `ignoreUnknownFields = false` 설정으로 인해 대응 Java 필드 없으면 즉시 실패

수정: `ApplicationProperties` 에 `AuthCache` 내부 클래스 추가
수정: `UserAuthCacheService` 의 `@Value` 제거 → `ApplicationProperties` 생성자 주입으로 전환

---

### 5단계: 문서 개정 (에이전트 오판 방지)

기존 문서의 문제:
- `Architecture.md`: "Do NOT cache auth/session data" → 너무 광범위
- `Engineering_Guideline.md` 5.2: "로그인 관련 / UserDetails / 권한 정보 캐시 금지"
  → OTP, Rate Limiting, UserAuthCacheDto 까지 금지로 오해 가능

개정 방향:
- 금지 대상을 "JPA 엔티티 직접 캐시" 로 명확히 한정
- 허용 대상(캐시 전용 DTO, OTP, Rate Limiting)을 명시적으로 기재
- ️ 에이전트 오판 방지 경고문 삽입
- 올바른/잘못된 패턴 코드 예시 추가

## 주요 코드 변경 포인트

| 파일 | 변경 전 | 변경 후 |
|------|---------|---------|
| `DomainUserDetailsService` | DB 직접 조회 | Redis → DB → Redis 저장 3단계 |
| `CommonCodeService.findAllGroups` | `@Cacheable` 엔티티 반환 | CacheManager + GroupDto 저장 |
| `UploadService.findById` | `Upload` 엔티티 캐시 | `UploadDTO` 캐시 + 복원 헬퍼 |
| `UserService.changePassword` | 변경만 | 변경 + `evict()` 호출 |
| `ApplicationProperties` | AuthCache 없음 | `AuthCache { ttlMinutes }` 추가 |
