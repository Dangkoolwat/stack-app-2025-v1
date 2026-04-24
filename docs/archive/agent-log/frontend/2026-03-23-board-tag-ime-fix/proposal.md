# 제안

## 방향 (Primary Direction)
- Vue 컴포넌트에서 제공하는 이벤트 수식어(`@keydown.enter.capture`)를 사용하여, IME 조합 중일 때 발생하는 `Enter` 이벤트를 캡처 단계에서 강제로 멈추게(stopPropagation) 함.
- `<b-form-tags>`를 감싸고 있는 `<div class="form-group mb-4">` 영역의 이벤트 리스너에 조합 판별식을 넣어서, `e.isComposing`인 경우 `b-form-tags` 컴포넌트 내부로 `Enter` 이벤트가 전파되지 않도록 차단함.

## 대안 (Alternative Options)
- `b-form-tags`의 슬롯(slot) 커스터마이징을 활용해 네이티브 `input` DOM 요소를 직접 구성하고, `@keydown` 핸들러를 별도로 개발할 수도 있으나, 현재 깔끔하게 구성된 UI 코드가 지나치게 방대해지는 단점이 있음. 이벤트 캡처링 차단 방식은 기존 템플릿의 외형을 유지하면서 단 1줄의 인라인 함수 추가만으로 버그를 우회할 수 있어 경제적임.

## 리스크
- 사용자가 한글 입력 후 엔터를 눌렀을 때 즉시 태그 모양으로 바뀌지 않고, 완성(밑줄 사라짐)만 된 상태로 한 번 더 엔터를 쳐야 태그가 추가되는 로직으로 변경됨. 하지만 이는 맥(Mac)/웹 등 일반적 IME 환경에서 가장 표준적인 `isComposing` 회피 시나리오이므로 사용성 측면에서 수용 가능함.
