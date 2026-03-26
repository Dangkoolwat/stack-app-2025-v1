---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: ko
---

# 구현 계획: Logs 페이지 레이아웃 개선

## 수정 파일

**파일**: `src/main/webapp/app/admin/logs/logs.vue`

## 변경 사항

### 1. 테이블에 CSS 클래스 추가

```vue
<table class="table table-sm table-striped table-bordered logs-table">
```

### 2. 스타일 블록 추가 (Scoped CSS)

```vue
<style scoped>
.logs-table {
  table-layout: fixed;
  width: 100%;
}

.logs-table th:nth-child(1),
.logs-table td:nth-child(1) {
  width: 60%;
  max-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logs-table th:nth-child(2),
.logs-table td:nth-child(2) {
  width: 40%;
  min-width: 280px;
}

.logs-table td:nth-child(1) {
  position: relative;
}

.logs-table td:nth-child(1):hover small {
  overflow: visible;
  white-space: normal;
  word-break: break-all;
}

.logs-table td:nth-child(1) small {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
```

### 3. 버튼 사이즈를 `xs` 로 변경

```vue
<BButtonGroup role="group" aria-label="Log level" size="sm" class="flex-nowrap">
  <BButton @click="updateLevel(logger.name, 'TRACE')" :variant="logger.level === 'TRACE' ? 'primary' : 'light'" size="xs">
    TRACE
  </BButton>
  <BButton @click="updateLevel(logger.name, 'DEBUG')" :variant="logger.level === 'DEBUG' ? 'success' : 'light'" size="xs">
    DEBUG
  </BButton>
  <BButton @click="updateLevel(logger.name, 'INFO')" :variant="logger.level === 'INFO' ? 'info' : 'light'" size="xs">
    INFO
  </BButton>
  <BButton @click="updateLevel(logger.name, 'WARN')" :variant="logger.level === 'WARN' ? 'warning' : 'light'" size="xs">
    WARN
  </BButton>
  <BButton @click="updateLevel(logger.name, 'ERROR')" :variant="logger.level === 'ERROR' ? 'danger' : 'light'" size="xs">
    ERROR
  </BButton>
  <BButton @click="updateLevel(logger.name, 'OFF')" :variant="logger.level === 'OFF' ? 'secondary' : 'light'" size="xs">
    OFF
  </BButton>
</BButtonGroup>
```

## 구현 세부사항

### 테이블 레이아웃
- `table-layout: fixed`: 컬럼 너비를 고정하여 컨텐츠가 넘쳐도 테이블이 깨지지 않음
- 첫 번째 컬럼: 60% 너비, 말줄임표 처리
- 두 번째 컬럼: 40% 너비, 최소 280px 보장 (버튼 6 개 표시)

### 호버 효과
- 평소: 로거 이름을 한 줄로 표시 (말줄임표)
- 호버: 전체 이름 표시 (줄바꿈)

### 버튼 사이즈
- `size="xs"`: BootstrapVue 의 가장 작은 버튼 사이즈
- 기존 `size="sm"`보다 약 30% 작아짐

## 테스트 항목

- [ ] 첫 번째 컬럼이 길어도 두 번째 컬럼이 화면에 표시됨
- [ ] 버튼이 기존보다 작아져서 6 개 모두 한 줄에 표시됨
- [ ] 로거 이름에 마우스 호버 시 전체 이름이 표시됨
- [ ] 정렬 기능이 정상 작동함
- [ ] 필터 기능이 정상 작동함
