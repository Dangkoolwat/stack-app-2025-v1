# proposal.md

## 해결 방안

### 방안 A: 캐시 완전 제거 유지 (이전 에이전트 방식)

- 인증 관련 캐시를 모두 제거하고 매 요청마다 DB 조회
- 장점: 단순, stale 데이터 없음
- 단점: DB 부하 증가, 고트래픽 환경에서 병목 발생, 근본 원인 미해결

### 방안 B: 캐시 전용 DTO 도입 (선택) ✅

- JPA 엔티티 대신 캐시 전용 DTO(record 또는 단순 POJO)를 설계하여 Redis 에 저장
- `UserAuthCacheDto` (record): id, login, activated, Set<String> authorities
- `CommonCodeCacheDto.GroupDto` / `DetailDto` (record): 단순 타입만 포함
- `UploadDTO`: @Data Lombok, 단순 타입만 포함
- 장점: 근본 원인 해결, DB 부하 감소, 강제 로그아웃/권한 변경 즉시 반영 가능
- 단점: 추가 DTO 클래스 필요, 상태 변경 시 evict() 연동 필요

## 선택 이유

방안 B 선택.

"캐시를 쓰면 안 된다"가 아니라 "JPA 엔티티를 캐시 대상으로 쓰면 안 된다"가 올바른 원칙.
캐시 전용 DTO 패턴은 대규모 서비스(Netflix, Kakao 등)의 실무 표준이며,
이미 프로젝트에 `redissonJsonClient`(JsonJackson3Codec)가 구성되어 있어
추가 인프라 없이 즉시 적용 가능.

## Trade-offs

| 항목 | 방안 A | 방안 B |
|------|--------|--------|
| DB 부하 | 높음 | 낮음 (Cache HIT 시 DB 쿼리 생략) |
| 구현 복잡도 | 낮음 | 중간 (DTO + evict 연동) |
| 데이터 일관성 | 완벽 | 높음 (Short TTL 5분 + 명시적 evict) |
| Redis 장애 내성 | 완전 무관 | DB fallback 보장 |
| 강제 로그아웃 | 불가 | evict() 로 가능 |

## 리스크

- evict() 호출 누락 시 stale 데이터 노출 → 상태 변경 메서드 전수 점검으로 완화
- Redis 장애 시 fallback 미구현 → try-catch + Optional.empty() 반환으로 완화
- 기존 캐시 데이터(JPA 엔티티 형태) 잔존 → Redis FLUSHALL 또는 서버 재시작으로 초기화
