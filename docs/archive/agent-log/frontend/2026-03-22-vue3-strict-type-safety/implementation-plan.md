# 구현 계획 (Implementation Plan)

## 1. 전역 타입 엄격 모드 적용
- 대상: `tsconfig.app.json` 의 `compilerOptions` 
- 내용: `"strict": true`, `"noImplicitAny": true`, `"strictNullChecks": true`, `"strictFunctionTypes": true` 명시.

## 2. Pinia 사용자 상태(Store) 모델화 코딩
- 위치: `src/main/webapp/app/shared/config/store/account-store.ts`
- 구체안:
  1. `Account` 인터페이스 작성 (id, login, langKey, authorities).
  2. 추측에 의존했던 `AccountStateStorable` 내 `userIdentity: any` 를 `userIdentity: Account | null` 로 변환.
  3. `actions` 매개변수에 `promise: boolean | null`, `identity: Account | null`, `profile: string` 등 확정형(Primitive/Object) 타입 고정.
- Prettier 점검 및 코드 스타일 린트 정리 수행.
