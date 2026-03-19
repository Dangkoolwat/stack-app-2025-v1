# Cache Refactoring Implementation Plan (Spring Boot 4 + Redis)

## 1. 개요 및 분석 (Step 0)

현재 시스템은 JHipster 기반의 Redisson/JCache 구조를 사용하고 있으며, 이전 작업(Jackson 3 단일화)을 통해 직렬화 체계는 정리된 상태입니다. 이번 작업의 핵심은 **보안(인증 캐시 제거), 단순화(Redis 연결 최적화), 책임 분리(서비스 중심 관리)**입니다.

### 현 상태 분석 결과 (Current Cache List)

| cache name | 사용 위치 | 데이터 종류 | TTL | codec | 문제 여부 |
|------------|----------|------------|-----|-------|----------|
| `com.daangcool.stack.domain.User` | Hibernate L2 | Entity | 1h | Binary | **보안 정책 위반 (제거 대상)** |
| `com.daangcool.stack.domain.Authority` | Hibernate L2 | Entity | 1h | Binary | **보안 정책 위반 (제거 대상)** |
| `com.daangcool.stack.domain.User.authorities` | Hibernate L2 | Collection | 1h | Binary | **보안 정책 위반 (제거 대상)** |
| `usersByLogin` | `UserService` | DTO/Entity | - | - | **명시적 제거 필요 (Service 코드 잔재)** |
| `usersByEmail` | `UserService` | DTO/Entity | - | - | **명시적 제거 필요 (Service 코드 잔재)** |
| `SETTING_CACHE` | `GlobalSettingsService` | JSON DTO | 24h | JSON | 정상 |
| `COMMON_GROUP_CACHE` 등 | `CommonCodeService` | JSON DTO | 24h | JSON | 정상 |
| `TAG`, `BOARD`, `COMMENT` | Service/L2 | DTO/Entity | 1h/24h | JSON/Binary | **중복 캐시 감사 필요** |

---

## 2. 세부 작업 단계

### Phase 1: 인증 캐시 완전 제거 (Step 1)
- **대상**: `User`, `Authority` 관련 모든 캐시
- **작업**:
    - `CacheConfiguration.java`: `User.class.getName()` 관련 `createCache` 호출 제거
    - `User.java`, `Authority.java`: `@Cache` 어노테이션 제거
    - `UserService.java`: `clearUserCaches` 메서드 및 관련 호출부 제거
    - `UserRepository.java`: 캐시 이름 상수(`USERS_BY_LOGIN_CACHE` 등) 제거

### Phase 2: Redis 연결 구조 최적화 (Step 2)
- **목표**: 불필요한 `RedissonClient` 생성을 억제하고 설정을 중앙 집중화
- **작업**: 
    - 현재 Binary(Hibernate용)와 JSON(Spring용) 2개의 클라이언트를 유지하되, `getRedissonConfig`를 공유하여 관리 포인트를 일원화
    - (옵션) 가능하다면 하나의 클라이언트에서 코덱 오버라이드를 시도하나, JCache 제약 사항 고려 시 현재의 2개 Bean 체계가 가장 안정적임 (Step 2 "명확한 목적" 부합)

### Phase 3: 캐시 분류 및 TTL 재설계 (Step 3, 4)
- **분류**: 
    - **A (유지)**: 설정값, 공통코드, 게시글/댓글/태그 조회
    - **C (제거)**: 모든 인증 관련 캐시
- **TTL**:
    - Long TTL(24h): `binaryLongConfig`, `springLongTtlConfig` (설정, 공통코드)
    - Default TTL(1h): `binaryConfig`, `springConfig` (조회 데이터)

### Phase 4: Hibernate L2 충돌 및 책임 정리 (Step 6, 7)
- **작업**: 
    - 서비스 레이어에서 DTO를 캐싱하는 경우, Hibernate L2(Entity 캐시)는 필요 최소한으로 유지
    - `Board`, `Comment`, `Tag` 등의 L2 캐시는 유지하되(DB 부하 방지), `UserService`처럼 인증과 직결된 엔티티는 L2에서도 제외

### Phase 5: Swagger UI 및 기타 편의성 (Step 8)
- **작업**:
    - `application.yml`: `springdoc.swagger-ui.persistAuthorization=true` 설정 추가
    - 인증 실패 시 클라이언트 토큰 정리 가이드 확인

---

## 3. 검증 전략 (Step 9)

1. **인증 검증**: 캐시 제거 후에도 로그인/로그아웃 및 권한 체크가 정상적으로 DB를 참조하여 동작하는지 확인
2. **비즈니스 캐시 검증**: `Board`, `Comment` 등의 조회가 Redis에 JSON 형태로 잘 저장되고 조회되는지(Hit/Miss 로그) 확인
3. **직렬화 검증**: DTO 필드 변경 시 Jackson 3 기반의 직렬화 오류가 없는지 확인
4. **빌드 검증**: `./mvnw clean package -DskipTests`

---

## 4. 일정 및 체크리스트

1. [x] 현 상태 분석 및 Table 작성 (완료)
2. [ ] Phase 1: 인증 캐시 제거
3. [ ] Phase 2: Redis 연결 단일화/최적화
4. [ ] Phase 3: TTL 및 직렬화 최종 점검
5. [ ] Phase 4: Swagger 및 문서화
6. [ ] 최종 테스트 및 로그 생성
