---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# final-report

## 수행 에이전트

- GPT-5 Codex

## 요약

공용코드 API 500의 직접 원인은 DTO 캐시 전환이 리스트 cache read 경로까지 완전히 마무리되지 않은 것이었다. 이 패턴을 기준으로 `CommonCodeService`, `BoardService`, `TagService`, `UploadService`를 전수 조사해 raw 리스트/페이지 캐시를 DTO 정규화 방식으로 보강했다. 동시에 관리자 홈 상태 카드가 실제 actuator health payload와 같은 키를 사용하도록 수정했다.

## 이유

- 캐시 hit 시 `List<Map>` 또는 `PageDTO<Map>` 형태가 들어오면 DTO 반환 계약이 깨질 수 있었다.
- 공용코드에서 먼저 드러났지만 동일한 회귀 가능성이 다른 서비스에도 남아 있었다.
- 운영 화면에서 Redis/DB/Disk 카드가 health 상세와 다른 기준을 쓰면 `N/A`와 drift가 반복된다.

## 영향

- 공용코드 그룹/상세 조회는 서버 재시작 후에도 안정적으로 200 응답을 유지한다.
- Board/Tag/Upload의 DTO 캐시 read 경로도 같은 역직렬화 형태에서 더 안전해졌다.
- 관리자 홈은 Redis `used_memory_human`, DB `database`, Disk `free/total` 값을 직접 표시한다.

## 결과

- 변경 파일
  - `src/main/java/com/daangcool/stack/service/common/CommonCodeService.java`
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java`
  - `src/main/java/com/daangcool/stack/service/board/TagService.java`
  - `src/main/java/com/daangcool/stack/service/board/UploadService.java`
  - 관련 서비스 테스트 4종
  - `src/main/webapp/app/core/home/home.component.ts`
  - `src/main/webapp/app/core/home/home.component.spec.ts`
  - `vitest.config.ts`
  - `docs/knowledge/2026-03-28-dto-centric-cache-boundaries.md`
- 검증
  - `./mvnw -Dtest=CommonCodeServiceT,TagServiceT,UploadServiceT,BoardServiceT test`
  - `npx vitest run app/core/home/home.component.spec.ts app/admin/health/health-modal.component.spec.ts`
  - `npm run webapp:build:dev`

## 남은 리스크

- `BoardService`의 page cache는 read 경로를 보강했지만, 장기적으로는 search/page cache를 `PageDTO` 단일 규약으로 더 명시적으로 통일하는 편이 좋다.
- 다른 신규 캐시가 추가될 때도 같은 raw payload 회귀가 생기지 않도록 cache hit regression test를 함께 요구해야 한다.
