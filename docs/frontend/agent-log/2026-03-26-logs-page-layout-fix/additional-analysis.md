---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: ko
---

# 추가 문제 분석: Logs 페이지 UI/UX 개선

## 사용자 요구사항

### 1. 컬럼 비율 조정
- **현재**: 60% (이름) : 40% (레벨)
- **변경**: 30% (이름) : 70% (레벨)
- 왼쪽 컬럼을 작게, 오른쪽 컬럼을 크게

### 2. 버튼 사이즈 최소화
- TRACE, DEBUG, INFO, WARN, ERROR, OFF 버튼이 무엇인지 모름
- 가장 작은 사이즈로 변경 필요

### 3. 간격 조정
- 필터 항목과 테이블 헤더 사이 간격이 너무 붙어있음
- 여백 추가 필요

### 4. 필터 입력칸 크기 조정
- 현재: 너무 김 (화면 전체 너비)
- 변경: 중간 정도 (form-control-md 또는 custom width)

## 해결 방안

### CSS 수정
```css
/* 컬럼 비율 3:7 */
.logs-table td:nth-child(1) { width: 30%; }
.logs-table td:nth-child(2) { width: 70%; }

/* 필터 입력칸 중간 크기 */
.logs-filter-input {
  width: 300px;
  max-width: 50%;
}

/* 간격 추가 */
.logs-controls {
  margin-bottom: 1rem;
}
```

### 버튼 사이즈
- `size="xs"` → 더 작은 사이즈 없음
- CSS 로 추가 축소: `font-size: 10px; padding: 1px 4px;`
