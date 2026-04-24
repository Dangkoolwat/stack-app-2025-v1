---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 구현 흐름 (Walkthrough)

1. 캐시 이름 중앙 관리 (CacheNames.java)
- 기존에 각 서비스 클래스 내부에 정의되어 있던 캐시 이름들을 com.daangcool.stack.common.constant.CacheNames 클래스 한 곳으로 모았습니다. 
- 이를 통해 일관된 명명 규칙을 적용하고 관리를 용이하게 했습니다.

2. 리소스 권한 검증 고도화
- ResourceAuthorizationService를 도입하여 게시글이나 댓글의 작성자 본인 혹은 관리자만이 수정 및 삭제를 수행할 수 있도록 보안을 강화했습니다. 
- SecurityUtils를 통해 현재 사용자의 ID 또는 로그인 정보를 추출하여 객체 소유권과 비교하는 공통 로직을 수립했습니다.

3. 계층적 논리 삭제 구현
- 게시글(Board)을 삭제할 때 이에 딸린 태그(BoardTag), 댓글(Comment), 첨부파일(Upload)이 함께 논리적으로 삭제되도록 BoardService.delete() 메서드를 개선했습니다. 
- 각 Repository에 대량 논리 삭제를 위한 커스텀 쿼리를 추가하여 성능과 데이터 무결성을 동시에 확보했습니다.

4. 테스트 실패 해결 및 안정화
- 캐시 이름 변경 및 서비스 생성자 파라미터 추가로 인해 깨진 통합 테스트들을 전수 조사하여 수정했습니다. 
- 특히 BoardServiceIT에서 권한 검증 로직이 추가됨에 따라 @WithMockUser를 사용하여 인증된 사용자 환경을 모의(Mock)하도록 보완했습니다.
- BoardServiceT, CommonCodeServiceT, UploadFileUtilsT 등 유닛 테스트에서 발생한 회귀 오류를 Mock 주입 및 경로 조정을 통해 해결했습니다.
- 모든 테스트(119개)가 CI/CD 환경과 동일한 조건에서 통과함을 확인했습니다.
