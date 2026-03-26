---
agent: Qwen Code
created_at: 2026-03-26 (목요일)
language: ko
---

# 문제 분석: Configuration 페이지 레이아웃 개선

## 문제 현상

Logs 페이지와 동일한 문제:
- 첫 번째 컬럼 (Prefix/Property) 이 너무 김
- 두 번째 컬럼 (Properties/Value) 이 화면 밖으로 밀려남
- 필터 입력칸이 너무 김 (전체 너비)
- 필터와 테이블 사이 간격 없음

## 현재 코드

**파일**: `src/main/webapp/app/admin/configuration/configuration.vue`

```vue
<input type="text" v-model="filtered" class="form-control" />

<table class="table table-striped table-bordered table-responsive d-table">
  <thead>
    <tr>
      <th class="w-40">Prefix</th>
      <th class="w-60">Properties</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><span>{{ entry.prefix }}</span></td>
      <td>
        <div class="row">
          <div class="col-md-4">{{ key }}</div>
          <div class="col-md-8">
            <span class="float-end bg-secondary break">{{ entry.properties[key] }}</span>
          </div>
        </div>
      </td>
    </tr>
  </tbody>
</table>
```

## 문제 원인

1. `w-40`, `w-60` 클래스가 있지만 컨텐츠가 길면 무시됨
2. `table-responsive` 만으로는 부족
3. 필터 입력칸이 `form-control` 전체 너비 사용
4. 간격 요소 없음

## 해결 방안

Logs 페이지와 동일한 패턴 적용:
- `table-layout: fixed`
- 컬럼 비율 30:70
- 필터 입력칸 250px
- 간격 추가
