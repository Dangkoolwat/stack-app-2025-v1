---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 문제 분석

1. 캐시 관리의 파편화

- 각 서비스(BoardService, CommentService 등)마다 로컬 상수로 캐시 이름을 정의하여 관리 포인트가 분산됨.
- 이로 인해 캐시 이름 중복이나 오타로 인한 캐시 미스 위험이 존재함.

2. 권한 검증 로직의 부재 및 비일관성

- 리소스(게시글, 첨부파일 등) 수정/삭제 시 소유자 권한 검증이 누락되거나 각 서비스마다 다르게 구현됨.
- Deny-by-default 원칙이 적용되지 않아 보안 취약점 노출 가능성이 있음.

3. 데이터 무결성 보장 미흡 (Cascaded Soft Delete)

- 게시글 삭제 시 연관된 태그(BoardTag) 관계가 논리적으로 함께 삭제되지 않아 데이터 정합성 이슈 발생.
- 애그리거트 생명주기에 따른 계층적 삭제 처리가 필요함.

4. 테스트 안정성 저하

- 최근의 보안 및 캐시 구조 변경으로 인해 기존 통합 테스트(BoardServiceIT, CacheConfigurationIT)가 실패함.
- @WithMockUser 환경에서의 권한 검증 호환성 문제 발생.

5. 유닛 테스트 회귀 (Unit Test Regression)

- BoardService 생성자 변경으로 인해 BoardServiceT에서 NullPointerException 발생 (CommentRepository mock 누락).
- CommonCodeService에 CacheManager가 도입되면서 CommonCodeServiceT에서 NullPointerException 발생.
- UploadFileUtils의 경로 처리 로직 변화로 인해 UploadFileUtilsT의 파일 존재 여부 및 이동 테스트 실패 (물리 경로/웹 경로 불일치).

5. 유닛 테스트 회귀 (Unit Test Regression)
- BoardService 생성자 변경으로 인해 BoardServiceT에서 NullPointerException 발생 (CommentRepository mock 누락).
- CommonCodeService에 CacheManager가 도입되면서 CommonCodeServiceT에서 NullPointerException 발생.
- UploadFileUtils의 경로 처리 로직 변화로 인해 UploadFileUtilsT의 파일 존재 여부 및 이동 테스트 실패 (물리 경로/웹 경로 불일치).
