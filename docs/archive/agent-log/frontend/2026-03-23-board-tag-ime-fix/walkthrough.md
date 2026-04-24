# Walkthrough

- 사용자로부터, 게시글 태그에 한글 ("첫태그") 입력 후 엔터를 누르면 "첫태그", "그" 두 개의 태그가 생기는 버그가 제보됨.
- 코드 라인 `src/main/webapp/app/entities/board/board-update.vue` 35행 확인 및 이벤트 수정 진행.
- 대상 DOM: `b-form-tags`를 포함한 Wrapper `div`.
- `@keydown.enter.capture="e => { if (e.isComposing) e.stopPropagation(); }"` 코드를 통해 키보드(엔터) 이벤트가 자식 요소인 `<b-form-tags>` 로 전파되기 전에 원천 차단하는 로직 주입 완료.
- 이를 통해 한글 조합 과정(Composing)과 조합 완료 처리가 태그 시스템에 혼선을 주기 전 사전에 분리됨.
