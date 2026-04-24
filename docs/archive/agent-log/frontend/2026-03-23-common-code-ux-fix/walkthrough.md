# Walkthrough

- `src/main/webapp/i18n/ko/common-code-detail.json` 번역을 최신화하여 "공통 코드를 선택하세요." 메시지를 `selectGroup` 키에 맵핑함.
- `src/main/webapp/app/entities/common-code-detail/common-code-detail.vue` 파일에서, 데이터를 조회해왔는데 데이터 사이즈가 0건인지를 검사하는 구문 이전에 아무 그룹 코드도 선택되어있지 않은 경우 먼저 경고가 뜨도록 `v-if="!selectedGroupCode"` 분기를 최상단에 추가함.
- `src/main/webapp/app/entities/common-code-detail/common-code-detail-update.component.ts` 파일에서 등록, 수정, 그리고 명시적인 취소 상황 발생 시, 과거 브라우저 히스토리 스택에 의존하던 뒤로 가기 동작 `router.go(-1)`를 완전하게 버림. 명확히 내가 작업한 (혹은 작업하려 했던) 세부 항목의 그룹 코드로 링크를 재설정하여 `router.push` 함으로써 사이드 이펙트를 해소함.
