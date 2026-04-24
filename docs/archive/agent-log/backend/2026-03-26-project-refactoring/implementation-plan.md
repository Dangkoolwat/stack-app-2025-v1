---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 구현 계획 (Implementation Plan)

1단계: 인프라 및 상수 정의
- CacheNames.java 생성 및 snake_case 형식의 캐시 이름 정의.
- ResourceAuthorizationService 구현 (isOwnerOrAdmin, validateOwnerOrAdmin 등).

2단계: 서비스 계층 리팩토링 (캐시 및 보안)
- BoardService, CommentService, UploadService, TagService 순차 리팩토링.
- 로컬 캐시 상수를 CacheNames로 교정.
- 수정/삭제 로직에 ResourceAuthorizationService 권한 검증 추가.

3단계: 레포지토리 및 계층적 삭제 구현
- BoardTagRepository, CommentRepository, UploadRepository에 softDeleteAllByBoardId 메서드 추가 (@Modifying, @Query).
- BoardService.delete() 로직 수정하여 연관 관계 엔티티 일괄 논리 삭제 처리.

4단계: 테스트 환경 개선 및 검증
- BoardServiceIT: @WithMockUser 추가 및 stable test user 적용.
- CacheConfigurationIT: CacheNames 상수를 사용하도록 리팩토링하고 Mockito 기반 검증 수행.
- ./mvnw test를 통한 최종 통합 테스트 수행.

5단계: 문서화 및 마무리
- AGENTS.md 규정에 따른 agent-log 생성.
- markdown linting 및 bolding 제거 등 문서 정제 작업.
- 최종 결과 보고 및 walkthrough 작성.
