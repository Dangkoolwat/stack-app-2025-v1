# 실행 흐름 (Walkthrough)

1. Vue 컴파일러(Vite 또는 vue-tsc)가 소스 코드를 해석할 때, `tsconfig.app.json` 의 설정을 로딩함 (`strict: true`).
2. 모든 `.ts`, `.vue` 파일 내 함수 리턴과 전역 상태에 대해 타입 가이드라인을 강제 평가함.
3. 인증 후 사용자 객체가 Pinia의 `setAuthentication(identity)` 액션으로 인입됨.
4. 이때 파라미터 `identity`가 `Account | null` 로 명시되어 있으므로, 만약 잘못된 형태(문자열 등)가 들어오면 무시하거나 컴파일 에러를 뿜어냄.
5. 유효한 스키마일 경우 `state.userIdentity` 에 값을 할당하며, 이후 프로그램 전역에서 해당 `state.userIdentity.authorities` 에 접근할 때 절대 `undefined` 타입 오류 연산이 일어나지 않음을 TS 엔진이 보증함.
