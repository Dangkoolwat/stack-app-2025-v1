# 구현 계획

- `src/main/webapp/app/entities/board/board-update.vue` 파일 오픈.
- `<div class="form-group mb-4" @keydown.enter.prevent>` 로 선언된 "Tags" 레이블 감싸는 DOM 요소 확인.
- `@keydown.enter.capture="(e) => { if (e.isComposing) e.stopPropagation(); }"` 속성을 이벤트 핸들러로 인라인 형태로 추가하여 한글 입력 중의 컴포넌트 이중 호출을 방어.
- `eslint` 및 `prettier` 검사에 위반되지 않도록 컨벤션 뷰포맷 조정 (`npm run lint -- --fix` 수행).
