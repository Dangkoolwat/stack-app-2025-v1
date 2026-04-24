# 문제 분석 (Problem Analysis)

## 현상 및 배경
- `RateLimitingFilter`에서 클라이언트(접속자) 식별 시, `getClientIdentifier()` 메서드를 호출하여 HTTP Request의 헤더 및 IP 정보를 기반으로 Rate Limit 식별값을 추정하고 있음.

## 재현 / 문제점
- 클라이언트가 고의로 HTTP `X-Forwarded-For` 헤더에 허위 IP 주소를 주입(Spoofing)하여 서버에 전송할 경우, 기존 설정에서는 이 조작된 IP를 무분별하게 진짜로 신뢰하게 됨.
- 결과적으로 악의적인 공격자가 매번 가짜 헤더를 바꿔가면서 Rate Limiting을 완전히 우회(Bypass)하여 무차별 대입 공격(Brute Force) 등을 실행할 수 있는 심각한 보안 취약점이 잠재되어 있음.
