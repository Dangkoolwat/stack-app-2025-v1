---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: en
---

# Health Page Eye Icon Fix - Final Report

## Summary

Fixed the issue where the eye icon was not appearing for "Liveness state" and "Application Readiness state" items on the `/admin/health` page.

Also fixed the issue where database and disk space details were displayed as raw JSON instead of formatted tables.

## Root Cause

### Issue 1: No Eye Icon for Liveness/Readiness States

The `livenessState` and `readinessState` health indicators from Spring Boot's availability state only contain a `status` field (e.g., `ACCEPTING_TRAFFIC`, `REFUSING_TRAFFIC`) without any additional `details`.

The original code only displayed the eye icon when `health.details || health.error` existed, so these state indicators never showed the eye icon.

### Issue 2: JSON Display Instead of Table

The backend returns health data in this format:
```json
{
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "Oracle",
        "validationQuery": "SELECT 1"
      }
    }
  }
}
```

The `addHealthObject` method was treating the `details` key as a regular property, causing incorrect structure that led to JSON being displayed in the modal.

## Changes Made

### `src/main/webapp/app/admin/health/health.service.ts`

Modified the `addHealthObject` method to:
1. Unpack the `details` key from backend response and merge its contents
2. Store details in the correct nested structure: `details: { details: {...} }`
3. Include `status` in details when it's the only available information (for livenessState/readinessState)

```typescript
} else if (key === 'details' && typeof value === 'object') {
  // details 키의 값은 그대로 details 에 병합 (백엔드 응답 구조 처리)
  for (const detailKey in value) {
    if (Object.hasOwn(value, detailKey)) {
      details[detailKey] = value[detailKey];
      hasDetails = true;
    }
  }
}

// details 가 있으면 details.details 구조로 저장
if (hasDetails) {
  healthData.details = { details };
}

// status 만 있는 경우 (livenessState, readinessState 등) 눈 아이콘 표시를 위해 details 추가
if (!hasDetails && healthData.status && !healthData.error) {
  healthData.details = { details: { status: healthData.status } };
}
```

Also modified `flattenHealthData` to properly handle systems with existing details.

### `src/main/webapp/app/admin/health/health-modal.vue`

The template correctly iterates over `currentHealth.details.details`:
```vue
<tr v-for="(item, index) in currentHealth.details.details" :key="index">
```

## Test Results

All 18 health-related tests pass:
- `health.service.spec.ts`: 5 tests passed
- `health-modal.component.spec.ts`: 6 tests passed
- `health.component.spec.ts`: 7 tests passed

## Impact

### Before
- Liveness state: No eye icon, status information not viewable
- Application Readiness state: No eye icon, status information not viewable
- Database/Disk space: Details shown as raw JSON string

### After
- Liveness state: Eye icon appears, clicking shows status in table format
- Application Readiness state: Eye icon appears, clicking shows status in table format
- Database: Shows formatted table with database name, validation query, etc.
- Disk space: Shows formatted table with total, free, and threshold information

## Files Modified

1. `src/main/webapp/app/admin/health/health.service.ts`
2. `src/main/webapp/app/admin/health/health-modal.vue` (already correct)
3. `src/main/webapp/app/admin/health/health.service.spec.ts` (already updated)
