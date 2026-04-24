---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# self-check

- [x] 요구사항 반영
- [x] 관리자 홈 상태 카드가 `redisServer`, `db`, `diskSpace` detail 기준을 사용하도록 수정
- [x] 공용코드 캐시 read 경로 보강
- [x] 같은 raw 리스트/페이지 캐시 패턴을 `BoardService`, `TagService`, `UploadService`까지 전수 조사
- [x] 발견된 패턴을 모두 DTO 정규화 경로로 수정
- [x] 캐시 hit 회귀 테스트 추가
- [x] `./mvnw -Dtest=CommonCodeServiceT,TagServiceT,UploadServiceT,BoardServiceT test` 성공
- [x] `npx vitest run app/core/home/home.component.spec.ts app/admin/health/health-modal.component.spec.ts` 성공
- [x] `npm run webapp:build:dev` 성공

## Consistency Sweep

- 검색 명령:
  - `rg -n "cache\\.get\\([^\\n]*List\\.class|cache\\.get\\([^\\n]*Page\\.class|cache\\.get\\([^\\n]*\\)\\.get\\(\\)" src/main/java/com/daangcool/stack/service --glob '*.java'`
- 조사 결과:
  - `CommonCodeService`
  - `BoardService`
  - `TagService`
  - `UploadService`
- 조치 후 남은 패턴:
  - raw `List.class` 또는 wrapper `cache.get(key).get()`은 남아 있지만 모두 DTO 정규화 함수로 즉시 감싸는 형태로만 남겨 두었다.
  - `Page.class` raw read는 제거했다.
