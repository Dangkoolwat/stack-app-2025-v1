# Implementation Plan: Fix Login Cache & Serialization Error

## 목표
`admin@localhost` 로그인 시 발생하는 Redis 캐시 및 직렬화 오류를 해결하여 정상적인 인증 및 권한 검증을 보장합니다.

## 제안된 변경 사항

### [MODIFY] [CacheConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/CacheConfiguration.java)
-   듀얼 클라이언트(Dual-Client) 전략 도입:
    -   `redissonClient` (Binary): 기본 클라이언트로, Hibernate L2 캐시의 내부 데이터(`CollectionCacheEntry` 등) 처리를 위해 이진(Binary) 코덱을 사용하여 `InvalidDefinitionException`을 원천 차단합니다.
    -   `redissonJsonClient` (JSON): Spring `@Cacheable` 전용 클라이언트로, 사용자 정의 `ObjectMapper`를 통해 `Instant` 지원, `@JsonIgnore` 무시(비밀번호 해시 보존), Hibernate 컬렉션 프록시 처리를 수행합니다.

### [DIAGNOSTIC] [UserRepository.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/repository/UserRepository.java)
-   캐시 격리 테스트: `@Cacheable` 어노테이션을 일시적으로 주석 처리하여, 로그인 시 무조건 DB에서 직접 데이터를 읽어오도록 강제합니다. 이를 통해 문제가 캐시 계층(Redis/Jackson)인지 매핑 계층(Hibernate/Entity)인지 판별합니다.

### [DIAGNOSTIC] [User.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/User.java)
-   수동 Getter/Setter 추가: Lombok이 런타임에 올바르게 작동하지 않을 가능성을 대비하여 `password` 필드에 대한 접근자를 수동으로 구현합니다.

### [DIAGNOSTIC] [DomainUserDetailsService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/security/DomainUserDetailsService.java)
-   심층 로깅: 리플렉션을 통해 런타임 엔티티의 실제 필드 값을 확인하는 로그를 추가합니다.

## 검증 계획
### 자동화된 테스트
-   `./mvnw compile`: 프로젝트 컴파일 확인.

### 수동 검증
-   `docker exec edenhtwebapp-redis-1 redis-cli FLUSHALL`: 캐시 초기화.
-   서버 재시작 후 `admin@localhost` 로그인.
-   로그인 후 다른 페이지(예: 관리자 기능)로 이동하여 권한 정보가 캐시에서 정상적으로 복원되는지 확인.
