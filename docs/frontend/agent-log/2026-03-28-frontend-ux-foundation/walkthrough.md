---
agent: GPT-5 Codex
created_at: 2026-03-28 (Saturday)
language: ko
---

# walkthrough

## 구현 흐름

1. 전역 스타일 계층부터 정리했다.
   - 배경, 패널, 툴바, 상태 배지, 폼 액션, 모달 액션, 테이블 푸터를 공통 클래스로 정의했다.

2. 앱 루트 레이아웃을 정리했다.
   - `jh-card` 중심 구조를 완화하고 `page-shell`, `page-surface` 중심으로 바꿨다.

3. 내비게이션 구조를 재정비했다.
   - 사용자 피드백을 반영해 메뉴 구조와 세부 순서를 기존 체계로 되돌렸다.
   - 동시에 접힘 없는 상단 navbar 구조로 바꿔 항상 메뉴가 보이도록 조정했다.

4. 홈 화면을 공개 랜딩과 로그인 워크스페이스로 분리했다.
   - 비로그인 사용자는 프레임워크 특징과 스펙을 보는 랜딩 페이지를 유지한다.
   - 로그인 사용자는 운영 진입 화면을 보고, 관리자는 상태 카드(DB/Redis/Disk)와 Build Version 카드를 추가로 확인한다.
   - 관리자 홈의 Artifact, Git Branch, Git Commit 카드는 제거했다.

5. 주요 리스트 화면 두 곳에 새 패턴을 적용했다.
   - 게시글 목록: 페이지 헤더, 툴바, 테이블 쉘, 페이지네이션 푸터
   - 사용자 관리: 요약 배지, 권한 chip, 통일된 삭제 모달 액션
   - 게시글 목록 액션 버튼은 더 작은 compact 스타일로 줄여 테이블 밀도에 맞췄다.
   - 사용자 관리의 활성/비활성 및 보기/수정/삭제 버튼도 같은 compact 규칙으로 맞췄다.

6. 관리자 health 상세 렌더링을 보완했다.
   - Redis 항목처럼 객체 세부 정보가 있는 경우 JSON 문자열 대신 중첩 테이블로 렌더링한다.
   - 운영자가 key/value 구조를 바로 읽을 수 있도록 health 팝업의 가독성을 높였다.
   - Redis payload 안에 `detail.used_memory_human`처럼 한 단계 더 감싸진 값은 상위 `detail` 이름을 숨기고 실제 키가 바로 보이도록 펼쳤다.

7. 관리자 홈 상태 카드와 테스트 구성을 다시 맞췄다.
   - `management/health`에서 실제로는 `redisServer` 키를 사용하고 있었기 때문에 홈 카드도 같은 키와 detail 값을 읽도록 수정했다.
   - Database는 `database`, Disk는 `free/total`, Redis는 `used_memory_human/max_memory_human` 기준으로 표시한다.
   - 함수형 `vite.config.ts` 도입 후 `vitest.config.ts`가 깨진 회귀를 함께 수정해 프런트 단위 테스트를 다시 실행 가능하게 복구했다.

## 핵심 포인트

- 라이브러리 교체 없이 Bootstrap 기반 구조 위에 공통 UX 계층을 덧씌웠다.
- 전역 클래스만으로도 카드/모달/하단 액션/페이지네이션 품질을 빠르게 끌어올릴 수 있게 설계했다.
- `management/health`, `management/info`를 활용해 관리자 홈 카드에 실제 운영 메타데이터를 연결했다.
- 관리자 홈은 health 상세 팝업과 같은 원천 데이터를 써야 `N/A` 같은 drift가 생기지 않는다.
- health 세부 팝업은 단순 문자열화보다 구조를 유지하는 렌더링이 운영 화면에서 훨씬 유리하다.
