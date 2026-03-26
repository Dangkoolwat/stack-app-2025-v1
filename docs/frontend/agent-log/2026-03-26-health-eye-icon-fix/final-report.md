---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: en
---

# Health Page Eye Icon Fix - Final Report

## Summary

Fixed the issue where the eye icon was not appearing for "Liveness state" and "Application Readiness state" items on the `/admin/health` page, preventing users from viewing their status information.

## Root Cause

The `livenessState` and `readinessState` health indicators from Spring Boot's availability state only contain a `status` field (e.g., `ACCEPTING_TRAFFIC`, `REFUSING_TRAFFIC`) without any additional `details` or `error` fields.

The original code only displayed the eye icon when `health.details || health.error` existed:

```vue
<a class="hand" @click="showHealth(health)" v-if="health.details || health.error">
```

Since these state indicators had no `details`, the condition was always false, and the eye icon never appeared.

## Changes Made

### 1. `src/main/webapp/app/admin/health/health.service.ts`

Modified the `addHealthObject` method to include `status` in `details` when it's the only available information:

```typescript
// status 만 있는 경우 (livenessState, readinessState 등) details 에 추가하여 눈 아이콘 표시
// error 가 있을 때는 status 를 details 에 추가하지 않음
if (!hasDetails && healthData.status && !healthData.error) {
  details['status'] = healthData.status;
  hasDetails = true;
}
```

Also fixed the `flattenHealthData` method to properly handle systems with both errors and subsystems:

```typescript
if (value.details) {
  this.addHealthObject(result, true, value, this.getModuleName(path, key));
} else if (this.hasSubSystem(value)) {
  // 서브시스템이 있으면 중첩 처리
  // error 가 있더라도 서브시스템이 있으면 함께 처리
  this.addHealthObject(result, false, value, this.getModuleName(path, key));
  this.flattenHealthData(result, this.getModuleName(path, key), value);
}
```

### 2. `src/main/webapp/app/admin/health/health-modal.vue`

Fixed the template to correctly iterate over `currentHealth.details` instead of `currentHealth.details.details`:

```vue
<tr v-for="(item, index) in currentHealth.details" :key="index">
```

### 3. `src/main/webapp/app/admin/health/health.service.spec.ts`

Updated test expectations to match the new behavior where:
- Items with only `status` (no error, no other details) now have `details: { status: '...' }`
- Items with `error` do not have `status` added to details (the error triggers the eye icon)

## Test Results

All 18 health-related tests pass:
- `health.service.spec.ts`: 5 tests passed
- `health-modal.component.spec.ts`: 6 tests passed
- `health.component.spec.ts`: 7 tests passed

## Impact

### Before
- Liveness state: No eye icon, status information not viewable
- Application Readiness state: No eye icon, status information not viewable

### After
- Liveness state: Eye icon appears, clicking shows status (e.g., `ACCEPTING_TRAFFIC`)
- Application Readiness state: Eye icon appears, clicking shows status (e.g., `ACCEPTING_TRAFFIC`)

## Files Modified

1. `src/main/webapp/app/admin/health/health.service.ts`
2. `src/main/webapp/app/admin/health/health-modal.vue`
3. `src/main/webapp/app/admin/health/health.service.spec.ts`
