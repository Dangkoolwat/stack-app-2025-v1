# final-report.md

## 변경 요약

JPA 엔티티를 Redis 에 직접 캐시하면서 발생하던 Jackson 3 직렬화/역직렬화 반복 오류를
캐시 전용 DTO 패턴으로 근본 해결하고, 인증 2차 캐시를 재도입함.
더불어 이전 에이전트들이 오판하게 만든 문서 3개를 수정하여 재발을 방지함.

## 변경 이유

이전 에이전트(C-1, 2026-03-20)가 오류의 근본 원인(JPA 엔티티 직렬화 문제)이 아닌
증상(캐시 오류)만 보고 캐시 자체를 제거하는 잘못된 결론을 내림.
"캐시 자체의 문제"와 "캐시 대상 객체의 설계 문제"를 구분하지 못한 에이전트 오판이 원인.

## 영향 범위

### 신규 생성 파일

| 파일 | 설명 |
|------|------|
| `service/dto/UserAuthCacheDto.java` | 인증 2차 캐시 전용 record DTO |
| `service/UserAuthCacheService.java` | Redis get/put/evict, DB fallback 보장 |
| `service/dto/CommonCodeCacheDto.java` | 공통코드 캐시 전용 record DTO (GroupDto, DetailDto) |
| `test/.../UserAuthCacheServiceTest.java` | Mockito 단위 테스트 10개 |

### 수정 파일

| 파일 | 변경 내용 |
|------|----------|
| `config/ApplicationProperties.java` | `AuthCache` 내부 클래스 추가 |
| `security/DomainUserDetailsService.java` | Redis → DB 2단계 캐시 레이어 |
| `service/UserService.java` | 상태 변경 6개 지점에 evict() 연동 |
| `service/dto/UploadDTO.java` | 필드 보강 (storageKey, isPublic 등) |
| `service/board/UploadService.java` | DTO 캐시 전환 + toUploadEntity() 헬퍼 |
| `service/common/CommonCodeService.java` | @Cacheable 4개 제거 + DTO 캐시 |
| `resources/config/application.yml` | auth-cache.ttl-minutes: 5 추가 |
| `docs/backend/Architecture.md` | 캐시 규칙 전면 개정 |
| `docs/backend/Engineering_Guideline.md` | 5.2 / 5.4 절 개정 |
| `AGENTS.md` | Cache Safety 세부 규칙 추가 |
| `test/.../DomainUserDetailsServiceIT.java` | 캐시 검증 테스트 4개 추가 |

## 테스트 결과

- `UserAuthCacheServiceTest`: 단위 테스트 10개 작성 완료 (Redis 불필요, Mockito 기반)
- `DomainUserDetailsServiceIT`: 기존 6개 유지 + 캐시 검증 4개 추가
- 애플리케이션 기동 오류 수정: `ApplicationProperties.AuthCache` 추가로 바인딩 오류 해소

## 후속 작업

1. 배포 전 필수: Redis 기존 캐시 초기화
   ```bash
   redis-cli FLUSHALL
   # 또는
   docker compose -f src/docker/redis.yml down && docker compose -f src/docker/redis.yml up -d
   ```

2. 통합 테스트 실행:
   ```bash
   mvn test -Dtest=UserAuthCacheServiceTest       # 단위 테스트
   mvn test -Dtest=DomainUserDetailsServiceIT     # 통합 테스트
   mvn test                                        # 전체
   ```

3. 모니터링: 배포 후 Redis Cache HIT 율 확인 (Prometheus/Grafana)
   - `auth:user:*` 키 패턴으로 캐시 저장 여부 확인 가능

## 핵심 설계 원칙 (미래 에이전트를 위한 메모)

> "캐시를 쓰면 안 된다" ≠ "인증 관련 데이터는 캐시 금지"
>
> 올바른 원칙: "JPA 엔티티(@Entity)를 직접 Redis 캐시 대상으로 사용하지 말 것"
>
> - 금지: `@Cacheable` 로 `User`, `Authority`, `Board` 등 JPA 엔티티 직접 캐시
> - 허용: `UserAuthCacheDto` (record), OTP, Rate Limiting 등 캐시 전용 DTO/단순 타입
>
> 캐시 전용 DTO 조건:
> - `record` 또는 `@Data @NoArgsConstructor` Lombok 클래스
> - 단순 타입 필드만 (`Long`, `String`, `boolean`, `Set<String>`)
> - JPA 연관 관계 필드 없음
> - `from(Entity)` 변환은 반드시 트랜잭션(영속성 컨텍스트) 안에서 호출
