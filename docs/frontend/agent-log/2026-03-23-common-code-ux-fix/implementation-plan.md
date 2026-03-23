# 구현 계획

1. `common-code-detail.json` 수정
   - `"selectGroup": "공통 코드를 선택하세요."` 구문 추가
2. `common-code-detail.vue` 화면 수정
   - `!selectedGroupCode` 일 때 `selectGroup` 안내 경고 노출
   - 등록 건수가 0건일 때 `!isFetching && commonCodeDetails && commonCodeDetails.length === 0` 인 경우에 `notFound` 에러 노출
3. `common-code-detail-update.component.ts` 화면 수정
   - `save()` 중단 처리 혹은 `previousState()` 시, `router.push('/common-code-detail')`를 호출하되 
     `groupCode` 파라미터가 유효하게 존재하는지 점검 후 Query param에 넘겨서 원복되도록 라우팅 처리.
