---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# problem-analysis

## 문제 현상

- 관리자 로그인 후 `/api/common/groups` 호출이 `500 Failed to write request`로 실패했다.
- 서버 재시작 이후 공용코드 캐시가 채워진 상태에서만 재현되었고, 공용코드 그룹/상세 화면이 비어 있었다.
- 관리자 홈 상태 카드에서는 Redis가 `N/A`로 표시되었다.

## 재현

1. `admin@localhost / admin`으로 인증 토큰 발급
2. `GET https://localhost:8443/api/common/groups` 인증 호출
3. 서버 재기동 직후 캐시가 채워진 상태에서 500 재현

## 원인

- `CommonCodeService`의 리스트 캐시 읽기가 `List.class` raw payload를 그대로 신뢰하고 있었다.
- Redis/Jackson 역직렬화 결과가 `List<Map>`으로 들어오면 DTO가 아닌 raw map이 응답 직렬화 경로로 흘러가면서 실패할 수 있었다.
- 같은 패턴이 `BoardService`, `TagService`, `UploadService`에도 남아 있었다.
- 프런트 관리자 홈은 `components.redis`를 읽고 있었지만 실제 actuator health payload는 `components.redisServer`를 사용하고 있었다.

## 영향

- 공용코드 조회 API가 캐시 hit 시 불안정했다.
- 다른 DTO 캐시 서비스도 동일한 역직렬화 형태에서 같은 회귀가 날 가능성이 있었다.
- 관리자 홈 상태 카드가 health 상세와 다른 기준을 사용해 운영 정보가 어긋났다.
