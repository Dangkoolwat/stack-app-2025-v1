---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# Problem Analysis

사용자는 캐시 전략, 특히 DTO cache와 Hibernate 2nd cache의 차이와 선택 기준을 실무적으로 이해하고자 했다.

현재 프로젝트는 Spring Boot 4 + Jackson 3 마이그레이션 과정에서 엔티티 중심 캐시의 취약점이 드러난 상태이며, 단순 장애 회고가 아니라 향후 캐시 정책 수립에 도움이 되는 정리 문서가 필요했다.

따라서 이번 작업은 코드 변경보다는 분석 보고서를 작성해 재사용 가능한 판단 기준과 적용 예제를 정리하는 데 목적이 있었다.
