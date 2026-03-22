# 문제 분석

## 현상
1. 로그인 후 관리자 페이지 홈으로 이동하지 않음.
2. `entities-menu.component.ts` 추가 작업 중 전체적인 오류(Global Error) 발생 추정.

## 원인 분석
1. **코드 결함**: `src/main/webapp/app/entities/entities-menu.component.ts`에서 `accountService.hasAnyAuthority(authorities)`를 호출하고 있으나, `AccountService` 클래스에는 해당 메서드가 존재하지 않음 (`hasAnyAuthorityAndCheckAuth` 또는 직접 스토어 접근 필요).
2. **런타임 에러**: 이로 인해 네비게이션 바(Navbar) 렌더링 시 `TypeError`가 발생하며, Vue 애플리케이션이 크래시되어 로그인 후 레이아웃이 정상적으로 표시되지 않음.
3. **메뉴 항목**: `entities-menu.vue`에 추가된 `게시글 리소스 관리` 항목이 `hasAnyAuthority`를 사용하고 있어 위 에러를 유발함.

## 영향 범위
- 로그인 후 메인 화면 진입 불가 (네비게이션 바 렌더링 실패로 인한 화면 먹통)
- 관리자 권한 체크 로직 오작동
