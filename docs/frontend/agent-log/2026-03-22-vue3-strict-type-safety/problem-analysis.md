# 문제 분석 (Problem Analysis)

## 현상 및 배경
- 팀 내 Vue 3 기반 프론트엔드 프로젝트에서 `tsconfig.app.json` 컴파일러 옵션이 기본값으로 유지되어 있어, 가장 강력한 타입 타이핑 방어책인 Strict Mode가 활성화되지 않은 상태였음.
- 전역 상태 관리자인 Pinia의 구심점 역할을 하는 `account-store.ts` 파일에서 `userIdentity` 필드의 타입이 `any` 로 선언되어 있어, 핵심 인증 정보임에도 컴파일 타임에 필드 오타 등 개발자의 실수를 잡아낼 수 없음.

## 재현 / 문제점
- Any 타입 남용: 인증 및 권한 정보가 담긴 Account 객체의 구조(ID, login, langKey, authorities)를 에디터가 추론하지 못해 자동완성(IntelliSense)이 작동하지 않고, 개발 시 잠재적 런타임 오류 가능성을 키움.
- 유연한 타입 검사: Nullish(`null`, `undefined`) 연산이나 묵시적 Any 파라미터가 묵인되어 예상치 못한 동작을 유발함.
