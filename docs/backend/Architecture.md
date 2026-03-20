# Backend Engineering Guideline

## Core Principles
- Maintainability First
- Security by Default
- Clear Layer Separation

---

## Recommended Backend Patterns

### Controller
- Request/Response DTO mapping only
- No business logic

### Service
- Business logic (use-case oriented)
- Transaction boundary

### Repository
- Data access only
- Clear query intent

---

## Maintainability Guidelines

Prefer:
- Explicit queries over magic
- Small services with clear responsibility
- Short transactions

Avoid:
- Deep abstraction layers
- Hidden side effects
- Over-generalization

---

## Security Defaults

- Validate all inputs
- Use standard error format (RFC7807)
- Never log secrets/tokens
- JPA 엔티티(@Entity)를 Redis에 직접 캐시하지 말 것
  → Hibernate Proxy(@class 불일치), LazyLoading 세션 소멸, @Transient 복원 불가 문제 발생
  → 캐시 대상은 반드시 캐시 전용 DTO(record 또는 단순 POJO)로 변환 후 저장
- `@Cacheable`로 UserDetails를 직접 캐시하지 말 것
  → Spring Security 내부 프록시 객체가 직렬화되어 역직렬화 실패 유발

---

## Change Playbook

### 1. Config / Library Change

Must define:
- Why change is needed
- Scope of impact
- Compatibility (breaking 여부)
- Rollback strategy
- Required tests

Checklist:
- [ ] Dependency tree checked
- [ ] No duplicate libraries (e.g. Jackson)
- [ ] Config consistency 유지
- [ ] Runtime 영향 검증

---

### 2. API Development

Steps:
1. Define contract (DTO)
2. Validate inputs
3. Implement service logic
4. Standardize error handling
5. Document (OpenAPI)

---

### 3. Database Change

- Use Liquibase only
- Never modify schema manually
- Ensure backward compatibility
- Plan rollback

---

## Caching Rules (Redis)

Use ONLY when:
- High read frequency
- Low consistency risk

### 캐시 대상 선정 원칙 (중요)

**허용 — 명시적 저장소 패턴으로 캐시 가능:**
- 캐시 전용 DTO (record 또는 단순 POJO, 민감정보 제외)
  - 예) `UserAuthCacheDto` — id, login, activated, Set<String> authorities
  - 예) `CommonCodeCacheDto.GroupDto` — groupCode, groupName 등 단순 타입
- OTP 코드 / 실패 횟수 / 계정 잠금 상태 (Redisson RMapCache, TTL 필수)
- Rate Limiting 상태 (Redisson RRateLimiter)
- 분산 Lock (Redisson RLock)

**금지 — 다음은 절대 캐시 대상으로 사용 불가:**
- JPA 엔티티 직접 캐시 (`User`, `Authority`, `Board` 등 @Entity 클래스)
  → 이유: Hibernate가 런타임에 생성하는 `$HibernateProxy$xxxx` 클래스가
    Jackson DefaultTyping의 @class 필드에 저장되어 역직렬화 시 복원 불가
- `@Cacheable` 어노테이션으로 UserDetails / 권한 객체 캐시
  → 이유: Spring Security 프록시 타입 + LazyLoading 컬렉션 직렬화 실패
- Hibernate L2 캐시 영역과 Application 캐시 영역 혼용
  → 이유: Binary(Hibernate) vs JSON(Application) 코덱 충돌

### 캐시 설계 규칙

- TTL 필수 (데이터 성격에 따라 세분화)
  - Long TTL(24h): 공통코드, 태그, 시스템 설정
  - Default TTL(1h): 게시글, 댓글, 업로드
  - Short TTL(5min): 인증 정보(UserAuthCacheDto)
- 캐시는 Service 레이어에서만 관리
- 상태 변경(비밀번호·권한·활성화·탈퇴) 시 반드시 명시적 evict() 호출
- Redis 장애 시 DB fallback 동작 보장 (예외를 삼키고 Optional.empty() 반환)
- 캐시 저장 시 반드시 트랜잭션(영속성 컨텍스트) 안에서 DTO 변환 수행
  → LazyLoad 해소 후 단순 타입만 추출

---

## Preferred Defaults

- JSON: Single ObjectMapper
- DB: Oracle + Liquibase
- Cache: Redis centralized
- Error: RFC7807
- Auth: Stateless (JWT)

---

## Self-Check Before Merge

- [ ] Business logic is clear and testable
- [ ] No layer violation
- [ ] Security review done
- [ ] Performance acceptable
- [ ] Rollback possible

---

## Golden Rule

👉 "Write code someone else can safely change in 6 months."