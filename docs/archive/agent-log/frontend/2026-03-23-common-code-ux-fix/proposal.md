# 제안

## 방향 (Primary Direction)
### 문제 1: 조회 전 안내 메시지 개선
- `i18n/ko/common-code-detail.json` 번역 파일에 `selectGroup` ("공통 코드를 선택하세요.") 키를 추가함.
- `common-code-detail.vue` 템플릿에 `v-if="!selectedGroupCode"` 조건의 안내 경고(Alert)를 신설. 기존 조회 결과가 0건일 때 보여주던 `notFound` Alert은, `selectedGroupCode`가 선택된 이후에만 노출되도록 `v-else-if` 로 우선순위를 조정.

### 문제 2: 목록으로 돌아갈 때의 명시적 라우팅
- `common-code-detail-update.component.ts` 내부의 취소 및 성공 저장 시 호출되는 브라우저 히스토리 `router.go(-1)`를 `router.push('/common-code-detail?groupCode=...')`로 교체.
- 파라미터는 업데이트 화면의 Data 객체 내 그룹 코드 값(`commonCodeDetail.value.group?.groupCode`)을 활용하여 현재 편집 중인/추가 중인 아이템의 그룹 코드를 되살리도록 함.

## 대안 (Alternative Options)
상태 관리 라이브러리(Vuex / Pinia) 등을 사용하여 `selectedGroupCode`을 전역 스토어에서 기억하게 하는 방식. 그러나 이 화면 단일 문제만을 위해 글로벌 상태를 변형하기에는 비용이 크며, URL Query 파라미터 방식을 보수하여 URL 기반 라우팅 규칙들을 일관되게 따르는 것이 바람직함.

## 리스크
해당 기능들에 대해서 테스트 코드가 있을 경우, 렌더링 검사 로직이 깨질 위험이 있으나, 본 수정 사항은 Vue 구성 요소의 UX 강화 및 명시성 확보에 목적이 있으므로 리스크는 낮음.
