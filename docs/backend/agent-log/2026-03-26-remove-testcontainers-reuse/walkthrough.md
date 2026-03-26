---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 작업 흐름

## 1. 원인 확인

경고 메시지 내용과 현재 테스트 컨테이너 선언 코드를 대조해 보니, 실제 원인은 `withReuse(true)` 설정 자체였다.

## 2. 코드 수정

`IntegrationTestContainers.java`에서 Oracle과 Redis 컨테이너의 `withReuse(true)` 호출을 제거했다.

## 3. 검증

테스트 소스 컴파일을 다시 수행해 설정이 정상적으로 유지되는지 확인했다.

이번 변경은 런타임 동작보다는 테스트 컨테이너 옵션 정리에 가깝기 때문에, 우선 컴파일 검증을 최소 기준으로 사용했다.

