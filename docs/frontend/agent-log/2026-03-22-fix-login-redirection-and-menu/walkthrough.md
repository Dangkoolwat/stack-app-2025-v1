# Walkthrough - Fix Login Redirection and Entity Menu Error

## Changes Made

### 1. Entities Menu Error Fix
Removed the problematic `/admin/resource-management` menu item from `entities-menu.vue` and fixed the `TypeError` in `entities-menu.component.ts`.

#### [entities-menu.component.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/entities-menu.component.ts)
```diff
-    const accountService = inject<any>('accountService');
+    const store = inject<any>('store');
 
     return {
       t$: i18n.t,
-      hasAnyAuthority: (authorities: any) => accountService.hasAnyAuthority(authorities),
+      hasAnyAuthority: (authorities: any) => {
+        const userAuthorities = store?.account?.authorities;
+        if (authorities && userAuthorities) {
+          if (typeof authorities === 'string') {
+            authorities = [authorities];
+          }
+          return authorities.some((auth: string) => userAuthorities.includes(auth));
+        }
+        return false;
+      },
```

### 2. Login Redirection Optimization
Updated `login-form.component.ts` to use `router.push('/')` for a smoother transition after login.

#### [login-form.component.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/account/login-form/login-form.component.ts)
```diff
-        // 가장 확실한 방법으로 홈으로 이동 및 상태 초기화
-        window.location.href = '/';
+        // 브라우저 새로고침 없이 최적의 경로로 이동 (SPA 방식)
+        router.push('/');
```

## Verification Results
- **Logic Verification**: The new `hasAnyAuthority` implementation is now reactive and uses the store directly, matching the successful pattern used in `home.component.ts`.
- **Redirection**: Using `router.push('/')` ensures the user stays in the single-page application context while showing the appropriate view (Public or Admin) based on the updated authentication state.
