---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# implementation-plan

1. 공용코드 API 500을 인증 토큰으로 직접 재현한다.
2. `CommonCodeService`의 리스트 캐시 read 경로를 DTO 정규화 방식으로 보강한다.
3. 같은 패턴이 남아 있는 `BoardService`, `TagService`, `UploadService`를 전수 조사한다.
4. 리스트/페이지 캐시 hit 시 `Map` payload를 DTO로 복원하는 보정 코드를 추가한다.
5. 관리자 홈 상태 카드가 `management/health`의 실제 detail 값을 사용하도록 수정한다.
6. 서비스 테스트, 프런트 단위 테스트, 프런트 빌드로 검증한다.

## 변경 파일

- `src/main/java/com/daangcool/stack/service/common/CommonCodeService.java`
- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/TagService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/test/java/com/daangcool/stack/service/common/CommonCodeServiceT.java`
- `src/test/java/com/daangcool/stack/service/board/BoardServiceT.java`
- `src/test/java/com/daangcool/stack/service/board/TagServiceT.java`
- `src/test/java/com/daangcool/stack/service/board/UploadServiceT.java`
- `src/main/webapp/app/core/home/home.component.ts`
- `src/main/webapp/app/core/home/home.component.spec.ts`
- `vitest.config.ts`
- `docs/knowledge/2026-03-28-dto-centric-cache-boundaries.md`
