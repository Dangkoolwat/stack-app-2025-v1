---
agent: GPT-5 Codex
created_at: 2026-03-28 (Sat)
language: ko
---

# proposal

## 방안 1

- Redis codec 또는 전역 ObjectMapper 설정을 바꿔 raw map 역직렬화를 막는다.
- 장점: 서비스별 보정 코드가 줄어든다.
- 단점: 캐시 외 다른 직렬화 경로까지 함께 흔들 수 있어 리스크가 크다.

## 방안 2

- 각 서비스에서 cache hit payload를 DTO/read-model로 정규화한 뒤 반환한다.
- 장점: DTO cache 정책과 맞고 영향 범위를 서비스 내부로 제한할 수 있다.
- 단점: 리스트/페이지 캐시마다 보정 코드와 회귀 테스트가 필요하다.

## 선택

- 방안 2를 선택했다.
- 이유: 현재 정책은 DTO/read-model cache이며, 전역 직렬화 설정을 흔들지 않고도 서비스 경계에서 안정적으로 복구할 수 있기 때문이다.
