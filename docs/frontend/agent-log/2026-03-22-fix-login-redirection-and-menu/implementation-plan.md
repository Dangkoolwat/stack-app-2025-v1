# 구현 계획

## 단계 1: Entities 메뉴 수정
- `src/main/webapp/app/entities/entities-menu.vue`에서 마지막 메뉴 항목 제거.
- `src/main/webapp/app/entities/entities-menu.component.ts`의 `hasAnyAuthority` 로직을 스토어 직접 참조 방식으로 수정.

## 단계 2: 로그인 리다이렉션 최적화
- `src/main/webapp/app/account/login-form/login-form.component.ts`에서 `window.location.href = '/'` 대신 `router.push('/')`를 우선 사용하도록 검토 및 적용.
- (참고: 대규모 상태 초기화가 필요한 경우에 대비해 `window.location.href`가 사용되었을 수 있으므로, 에러 해결 후 동작을 관찰하며 최선의 방식 선택)

## 단계 3: 확인
- 애플리케이션 빌드 및 로그인 동작 확인.
- 권한 있는 사용자로 로그인 시 관리자 대시보드가 정상적으로 표시되는지 확인.
