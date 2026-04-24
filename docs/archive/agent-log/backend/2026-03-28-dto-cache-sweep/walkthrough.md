---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# walkthrough

1. 먼저 인증 토큰으로 `/api/common/groups`를 직접 호출해 캐시 hit 상황의 500을 서버 기준으로 재현했다.
2. `CommonCodeService`에서 `List.class` raw payload를 `GroupDto`/`DetailDto`로 정규화하도록 보강했다.
3. 같은 조사 패턴으로 서비스 계층 전체를 검색해 `BoardService`, `TagService`, `UploadService`의 raw 리스트/페이지 캐시를 찾았다.
4. `TagService`와 `UploadService`는 `List<Map>` cache hit를 DTO 리스트로 복원하도록 수정했다.
5. `BoardService`는 페이지 캐시와 공지 리스트 캐시를 `PageDTO<BoardDTO>` 및 `List<BoardDTO>` 정규화 경로로 바꿨다.
6. 관리자 홈은 `management/health`의 실제 payload를 다시 확인해 `redisServer` 기준으로 Redis 메모리 값을 표시하게 맞췄다.
7. `vite.config.ts` 함수형 변경으로 `vitest.config.ts`가 깨진 회귀도 함께 복구했다.

## 핵심 포인트

- DTO cache 정책은 write payload만 DTO인 것으로 끝나지 않고, read 경로도 역직렬화 결과를 DTO로 복원해야 완결된다.
- Redis/Jackson 환경에서는 `List<Map>`와 `PageDTO<Map>` 같은 중간 형태가 충분히 발생할 수 있으므로 cache hit regression test가 꼭 필요하다.
- 관리자 홈 상태 카드와 `admin/health`는 같은 health source를 써야 운영자가 서로 다른 정보를 보지 않는다.
