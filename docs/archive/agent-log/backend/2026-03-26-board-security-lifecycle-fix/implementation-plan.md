---
agent: GPT-5.4
created_at: 2026-03-26 (Thu)
language: ko
---

# 구현 계획

1. 작성자 위조 차단
- 변경 파일: `BoardService`, `CommentService`, 관련 단위/통합 테스트
- 작업 내용: 요청 `userId`를 무시하고 현재 인증 사용자 기준으로 작성자 설정

2. private 업로드 권한 강화
- 변경 파일: `UploadService`, `UploadResource`, `ResourceAuthorizationService`, 관련 통합 테스트
- 작업 내용: private 다운로드 시 owner/admin 검증을 서비스 계층에서 강제하고 403 응답 정합성 확보

3. 게시글 애그리거트 생명주기 보완
- 변경 파일: `BoardService`, 신규 `BoardAggregateAdminResourceIT`
- 작업 내용: restore/hard delete 시 댓글, 업로드, 태그 관계 및 tag usageCount 정리

4. 테스트 및 문서화
- 변경 파일: `docs/backend/agent-log/...`, `docs/knowledge/...`
- 작업 내용: 가이드 기준 전체 테스트 실행, 결과 기록, 후속 유지보수를 위한 KI 남기기
