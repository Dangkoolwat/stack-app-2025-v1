# 실행 흐름 (Walkthrough)

1. 클라이언트(해커)가 `X-Forwarded-For: 8.8.8.8` 라는 가짜 헤더를 스스로 심어서 무차별 로그인 요청을 보냅니다.
2. 이 통신이 사내 망(Gateway, LB, Nginx Proxy)이 아닌 외부 인터넷 구간(예: 211.x.x.x)에서 곧바로 Tomcat Web Server로 진입합니다.
3. Tomcat의 `RemoteIpValve` 객체가 현재 연결된 실제 소켓의 IP(211.x.x.x)를 확인합니다.
4. 해당 IP가 YAML 환경설정에 명시된 `trusted-proxies` 대역(내부망 및 루프백) 목록 영역에 포함되어 있는지 대조 검사를 진행합니다.
5. 포함되어 있지 않으므로 이 요청은 신뢰할 수 없는 외부 직접 연결입니다. 따라서, 조작된 `8.8.8.8` 라는 헤더를 즉시 폐기하고 실제 연결된 IP 값(211.x.x.x)을 `HttpServletRequest.getRemoteAddr()` 결과로 강제 치환합니다.
6. 결과적으로 `RateLimitingFilter.getClientIdentifier()`는 치환된 진짜 클라이언트의 IP를 식별 데이터로 바라보게 되며, 정상적으로 Rate Limiting 정책이 적용되어 해당 해커의 IP 차단 조치가 안전하게 즉시 이루어집니다.
