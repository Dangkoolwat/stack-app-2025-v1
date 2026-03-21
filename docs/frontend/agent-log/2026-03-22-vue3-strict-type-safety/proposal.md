# 해결 방안 제안 (Proposal)

## 제안 방향
- **TypeScript Strict Mode 전면 활성화**: `tsconfig.app.json`의 `compilerOptions` 뎁스에 `strict: true`, `noImplicitAny: true`, `strictNullChecks: true`, `strictFunctionTypes: true` 속성을 명시적으로 적용.
- **Pinia Store(`account-store.ts`) 객체 모델화**: 인증 토큰 기반으로 파싱되는 계정 상태의 완전한 타입 안정성을 위해 `Account` 인터페이스 블록(Interface Block)을 선언하고, Store의 State 및 Action 제네릭과 파라미터에 엄격히 바인딩.

## 선택 이유 및 기대 효과
- **Zero Runtime Error 목표**: 타입스크립트의 가장 강력한 방어기제를 작동시켜 코드 수정 중 발생할 수 있는 휴먼 에러를 컴파일 단계에서 차단. 
- **코드 직관성 상승**: `Account` 스키마가 소스 레벨에 명시되어 스토어 이용 시 별도의 문서 확인 없이도 자동완성을 통해 즉시 코딩이 가능해져 프론트엔드 DX(Developer Experience) 개선.
