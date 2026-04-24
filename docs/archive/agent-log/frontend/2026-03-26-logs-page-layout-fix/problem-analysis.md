---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: ko
---

# 문제 분석: Logs 페이지 레이아웃 개선

## 문제 현상

### 1. 첫 번째 컬럼 (이름) 너무 김
- 로거 이름 (예: `org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration`) 이 너무 길어서 두 번째 컬럼 (레벨) 이 화면 밖으로 밀려남
- 사용자가 레벨 버튼을 볼 수 없음

### 2. 레벨 버튼이 너무 큼
- TRACE, DEBUG, INFO, WARN, ERROR, OFF 버튼이 너무 크게 표시됨
- 버튼 그룹이 테이블 셀을 과도하게 차지함

## 현재 코드

**파일**: `src/main/webapp/app/admin/logs/logs.vue`

```vue
<table class="table table-sm table-striped table-bordered">
  <thead>
    <tr>
      <th @click="changeOrder('name')" scope="col">
        <span>{{ t$('logs.table.name') }}</span>
      </th>
      <th @click="changeOrder('level')" scope="col">
        <span>{{ t$('logs.table.level') }}</span>
      </th>
    </tr>
  </thead>
  <tr v-for="logger in filteredLoggers" :key="logger.name">
    <td>
      <small>{{ logger.name }}</small>
    </td>
    <td>
      <BButtonGroup role="group" aria-label="Log level" size="sm" class="flex-nowrap">
        <BButton @click="updateLevel(...)" size="sm">TRACE</BButton>
        <BButton @click="updateLevel(...)" size="sm">DEBUG</BButton>
        <!-- 6 개 버튼 -->
      </BButtonGroup>
    </td>
  </tr>
</table>
```

## 문제 원인

1. **테이블 컬럼 너비 제한 없음**: 첫 번째 컬럼이 컨텐츠 길이만큼 무제한 확장
2. **버튼 사이즈**: `size="sm"` 사용하지만 여전히 6 개 버튼이 너무 큼
3. **레이아웃 유연성 부족**: 자동 정렬 기능 없음

## 해결 방안

### 1. 테이블 컬럼 레이아웃 개선
- CSS 로 첫 번째 컬럼 최대 너비 제한
- 두 번째 컬럼 최소 너비 보장
- 텍스트 오버플로우 처리 (말줄임표)

### 2. 버튼 사이즈 최소화
- `size="xs"` (초소형) 사용
- 또는 버튼 대신 드롭다운 사용 (고려사항)

## 예상 영향

- ✅ 사용자가 모든 로거의 레벨 버튼 확인 가능
- ✅ 테이블 레이아웃이 깔끔해짐
- ✅ 반응형 디자인 개선
