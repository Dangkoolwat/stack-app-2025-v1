# Backend Engineering Guideline

## 1. 구조적 작업 계획 (Plan-First Approach)
모든 백엔드 기능 개발 및 DB 변경 작업은 다음 4단계를 엄격히 준수합니다. 에이전트는 단계별 문서화 및 승인 없이 다음 단계로 진행할 수 없습니다.

### Step 1: 논의 및 분석 (Initial Discussion)
- 요구사항과 현재 시스템(Java 코드, Liquibase 스케줄)의 한계점을 분석합니다.
- 작업 브랜치를 생성하기 전, 변경 범위를 명확히 논의합니다.

### Step 2: 설계 제안 및 토론 (Proposal & Debate)
- 제안서 작성: `docs/proposals/` 내에 작업 계획서를 작성합니다. (API 명세, DB 변경안 포함)
- 토론: 제안된 설계의 트레이드오프(성능, 보안, 유지보수성)를 검토합니다.
- 확정: 설계안이 승인되면 이를 최종 가이드라인으로 삼아 개발을 시작합니다.

### Step 3: 구현 및 자체 검증 (Implementation & Test)
- 설계안에 따라 구현을 진행합니다.
- 오라클 주의: 쿼리 작성 시 인덱스 활용 여부를 검토하고 N+1 문제가 발생하지 않도록 `Join Fetch` 등을 적절히 사용합니다.
- 테스트: 단위 테스트 및 JHipster 보안 가드가 포함된 통합 테스트를 수행합니다.

### Step 4: 완료 보고 및 문서화 (Completion & Finalize)
- 작업 완료 후 최종 API 명세와 변경된 DB 구조를 기록한 '완료 보고서'를 작성합니다.
- 기술 부채나 향후 개선이 필요한 사항을 명시하여 지식을 전파합니다.

## 2. 에이전트 전용 금지 가이드 (Explicit Bans for AI)
- BAN 1: Liquibase를 통하지 않은 직접적인 DB 스키마 수정 제안.
- BAN 2: Resource(Controller) 계층에서 Entity 클래스를 직접 반환하거나 파라미터로 받는 행위.
- BAN 3: 오라클 예약어(USER, ORDER, GROUP 등)를 물리 테이블/컬럼명으로 직접 사용하는 행위.
- BAN 4: 한 트랜잭션 내에서 과도하게 긴 작업 수행 (DB 커넥션 점유 방지).

## 3. 일반 DB 사용 시 공통 주의사항
- NULL 처리: 오라클은 빈 문자열(`''`)을 `NULL`로 처리하므로, 타 DB와 호환성을 고려한 방어적 코딩을 수행합니다.
- Paging: 대량 데이터 조회 시 반드시 `Offset-Fetch` 기반의 페이징 처리를 적용합니다.
- Timezone: 서버, 어플리케이션, 데이터베이스 간의 시간대 설정을 항상 일관되게 유지합니다. (UTC 권장이나 Asia/Seoul 기준)

## 4. 리뷰 체크리스트
- [ ] 설계 제안(Proposal)이 먼저 작성되고 승인되었는가?
- [ ] Liquibase 변경 로그가 오라클 문법 및 식별자 제약을 준수하는가?
- [ ] 모든 API 응답이 DTO를 통해 정규화된 에러 형식을 따르는가?
- [ ] 작업 완료 후 최종 문서 업데이트가 완료되었는가?

## 5. JSON / Cache 변경 작업 규칙

### 5.1 Jackson 변경 작업

Jackson 관련 변경 작업은 반드시 아래 절차를 따른다.

#### Step 1: dependency 분석
- mvn dependency:tree 수행
- Jackson 2 존재 여부 확인

#### Step 2: 혼용 제거
- com.fasterxml.jackson 전부 제거
- tools.jackson으로 통일

#### Step 3: 코드 정리
- import 전수 교체
- ObjectMapper 직접 생성 제거

#### Step 4: 검증
- Swagger 정상 동작 확인
- /v3/api-docs 확인
- JSON 직렬화 오류 확인

---

### 5.2 Cache 변경 작업

Cache 구조 변경 시 반드시 다음을 따른다.

#### Step 1: 캐시 대상 분류
- JPA 엔티티인지 여부 확인 (엔티티이면 무조건 DTO 변환 필요)
- 조회 데이터인지 / 인증 주변 데이터인지 여부 확인

#### Step 2: 캐시 금지 대상 판별

아래 조건 중 하나라도 해당하면 그 객체 자체를 캐시하는 것은 금지:

| 금지 대상 | 이유 |
|-----------|------|
| `@Entity` JPA 클래스 직접 저장 | Hibernate `$HibernateProxy$xxxx` @class 불일치로 역직렬화 실패 |
| `@Cacheable`로 UserDetails 캐시 | Spring Security 프록시 + LazyLoad 컬렉션 직렬화 실패 |
| `@Cacheable`로 Authority 컬렉션 캐시 | 동일 이유 (Hibernate Proxy 감쌈) |
| Hibernate L2 영역과 Application 캐시 혼용 | Binary vs JSON 코덱 충돌 |

> ️ 에이전트 오판 방지 주의사항
> "인증 관련 데이터는 캐시 금지"라는 표현은 JPA 엔티티를 직접 캐시하지 말라는 의미입니다.
> OTP, Rate Limiting, UserAuthCacheDto(record DTO) 같은 인증 주변 인프라는
> 명시적 저장소 패턴(Redisson RBucket/RMapCache)으로 Redis 활용이 허용되고 권장됩니다.

#### Step 3: 캐시 전용 DTO 설계 (금지 대상 우회 방법)

캐시에 저장할 객체는 반드시 아래 조건을 모두 만족해야 한다:

- `record` 또는 `@Data @NoArgsConstructor` Lombok 클래스
- 필드는 단순 타입만 허용: `Long`, `String`, `boolean`, `Set<String>`, `List<String>` 등
- JPA 연관 관계 필드(`@ManyToOne`, `@OneToMany` 등) 포함 금지
- 민감 정보(`password`, `activationKey`, `resetKey`) 포함 금지
- `from(Entity entity)` 정적 변환 메서드를 트랜잭션 안에서 호출하여 LazyLoad 해소

```java
//  올바른 패턴 예시
public record UserAuthCacheDto(
    Long id, String login, boolean activated, Set<String> authorities
) {
    public static UserAuthCacheDto from(User user) {
        // 트랜잭션 안에서 호출 → authorities LazyLoad 해소
        Set<String> authNames = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.toUnmodifiableSet());
        return new UserAuthCacheDto(user.getId(), user.getLogin(), user.isActivated(), authNames);
    }
}

//  잘못된 패턴 (절대 금지)
@Cacheable("users")
public User loadUser(String login) { ... }  // JPA 엔티티 직접 캐시
```

#### Step 4: TTL 설계
- Long TTL(24h): 공통코드, 태그, 시스템 설정
- Default TTL(1h): 게시글, 댓글, 업로드
- Short TTL(5min): 인증 정보(UserAuthCacheDto)

#### Step 5: 영향 분석
- Hibernate L2와 중복 여부
- JSON/Binary 충돌 여부
- 상태 변경 시 evict() 호출 위치 명시
- Redis 장애 시 fallback 전략 정의 (서비스 중단 없어야 함)

---

### 5.3 Redis 변경 작업

#### 필수 확인
- Redis 연결 수 증가 여부
- RedissonClient 중복 생성 여부

#### 금지
- 서비스별 RedisClient 생성
- 기능별 Redis 연결 확장

---

### 5.4 리뷰 체크리스트 (추가)

- [ ] Jackson 혼용이 없는가?
- [ ] ObjectMapper가 단일 체계인가?
- [ ] JPA 엔티티(@Entity)를 직접 Redis 에 저장하고 있지 않은가?
  - `@Cacheable` 대상이 엔티티라면 반드시 DTO 변환 후 캐시할 것
  - `Cache.put(key, entity)` 형태가 코드에 없는지 확인
- [ ] 캐시 전용 DTO가 단순 타입만 포함하는가? (JPA 연관관계 필드 없는가?)
- [ ] DTO 변환이 트랜잭션(영속성 컨텍스트) 안에서 이루어지는가? (LazyLoad 해소)
- [ ] 상태 변경 시 evict() 호출이 모든 변경 지점에 연동되어 있는가?
- [ ] Redis 장애 시 DB fallback 이 보장되는가? (예외를 삼키고 Optional.empty() 반환)
- [ ] Redis 연결이 중앙 집중형인가? (서비스별 RedissonClient 생성 없음)
- [ ] 캐시 TTL이 데이터 성격에 맞게 설정되어 있는가?
  - Long(24h): 공통코드·태그·설정 / Default(1h): 게시글·댓글 / Short(5min): 인증정보
