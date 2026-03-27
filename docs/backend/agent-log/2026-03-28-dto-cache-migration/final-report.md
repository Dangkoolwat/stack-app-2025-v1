---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# 수행 에이전트

GPT-5.4

# 요약

엔티티 중심 Redis/Hibernate 캐시 구성을 DTO 중심 애플리케이션 캐시로 재정렬했다.
공통코드/업로드/설정 캐시의 read path 를 DTO/fallback 기준으로 바꾸고, 전역 Jackson 우회 설정과 Hibernate L2 Redis 의존을 제거했다.

# 이유

- 엔티티/프록시 직렬화는 Spring Boot 4 + Jackson 3 에서 유지비용과 장애 가능성이 높다.
- 캐시 장애가 핵심 기능을 멈추게 하면 안 되므로 서비스 단위 fallback 이 필요했다.
- startup 시 캐시 clear 는 운영상 부작용이 커서 제거가 필요했다.

# 영향

- 공통코드 읽기 응답은 DTO 기반으로 정리되었고, 업로드 메타데이터 조회도 DTO 기반으로 변경되었다.
- Hibernate `@Cache` 의존과 JPA second-level/query cache 는 비활성화되었다.
- 단위 테스트는 새 캐시 계약에 맞게 갱신되었다.

# 결과

- 컴파일 통과
- 핵심 단위 테스트 통과
- 통합 테스트는 기존 Testcontainers/Hikari 데이터소스 초기화 문제로 미검증 상태

# 추가 메모

Guide Document Feedback
- 없음
