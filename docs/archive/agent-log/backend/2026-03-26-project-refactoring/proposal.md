---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 해결 방안 제안

1. 캐시 관리의 중앙 집중화 (Primary)
- CacheNames.java 파일을Common 패키지에 생성하여 모든 캐시 이름을 상수로 정의.
- 모든 서비스(Board, Comment, Upload, Tag)에서 CacheNames 상수를 참조하도록 리팩토링.
- 이를 통해 캐시 이름의 정합성을 보장하고 관리 포인트를 단일화함.

2. 리소스 권한 검증 서비스 도입 (Primary)
- ResourceAuthorizationService를 생성하여 소유자 및 관리자 여부를 판단하는 로직을 캡슐화.
- SecurityUtils 및 AuthoritiesConstants를 기반으로 Deny-by-default 보안 정책 수립.
- 서비스 계층(Board, Comment, Upload)에서 수정/삭제 작업 시 해당 서비스를 호출하도록 강제함.

3. 애그리거트 생명주기 기반 계층적 삭제 (Primary)
- BoardRepository 및 BoardTagRepository에 softDeleteAllByBoardId 등의 일괄 삭제 메서드 추가.
- BoardService.delete() 내에서 연관된 BoardTag, Comment, Upload를 순차적으로 논리 삭제 처리.
- 이를 통해 메인 엔티티 삭제 시 데이터 정합성을 유지함.

4. 통합 테스트 환경 복구 및 권한 지원 (Primary)
- BoardServiceIT 등 통합 테스트에 @WithMockUser를 적용하고 테스트용 사용자 로그인을 고정하여 권한 검증 호환성 확보.
- CacheNames 변경 사항을 CacheConfigurationIT에 반영하여 캐시 로직 검증 정상화.
- ResourceAuthorizationService의 ID 기반 검증을 Login 기반 검증으로 병행 지원하여 테스트 유연성 확대.

5. 유닛 테스트 정합성 복구 (Primary)
- BoardServiceT 및 CommonCodeServiceT에 누락된 Mock 객체(CommentRepository, CacheManager)를 주입하고 Mockito 설정을 보완함.
- UploadFileUtilsT에서 테스트용 임시 디렉토리(TempDir) 구조를 유틸리티의 rootPath/webPath 로직에 맞춰 재배치하여 경로 불일치 해결.

대안 1: JPA Cascade 이용 (Rejected)
- JPA의 CascadeType.ALL 및 orphanRemoval 기능을 사용할 수도 있으나, Soft Delete 환경에서는 영속성 컨텍스트가 논리 삭제 상태를 자동으로 전파하지 않거나 예기치 않은 부작용이 있을 수 있어 명시적인 Repository 호출 방식을 선택함.
