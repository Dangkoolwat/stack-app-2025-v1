---
agent: GPT-5.4
created_at: 2026-03-26 (Thu)
language: ko
---

# 구현 흐름

1. 작성자 결정 로직을 서비스 계층으로 이동했습니다.
- `BoardService.save`, `CommentService.save`에서 요청 DTO의 `userId`를 신뢰하지 않고 현재 로그인한 사용자를 조회해 다시 설정합니다.
- 이 방식으로 컨트롤러 외 다른 진입점에서도 동일한 보안 경계를 유지합니다.

2. private 업로드 다운로드 전용 조회 경로를 분리했습니다.
- `UploadService.getAuthorizedPrivateUpload`를 추가하여 파일 존재 여부, 삭제 여부, private 여부, owner/admin 권한을 한 번에 확인합니다.
- `UploadResource.downloadPrivateFile`은 이 경로만 사용하도록 바꿔 400/403/404 응답을 분리했습니다.

3. 게시글 애그리거트 restore/hard delete를 완결했습니다.
- restore 시 게시글에 속한 댓글, 업로드, 태그 관계를 함께 복구합니다.
- hard delete 시 댓글/업로드/태그 관계를 제거하고, active 상태였던 태그 연결만 usageCount를 감소시킵니다.
- 관련 캐시는 게시글 단건뿐 아니라 댓글/태그/업로드 축까지 함께 비웁니다.

4. 회귀 테스트를 추가했습니다.
- 작성 시 `userId` 스푸핑을 막는 테스트를 게시글/댓글 각각 추가했습니다.
- private 업로드에 대해 owner 성공, 타 사용자 403, admin 성공 시나리오를 추가했습니다.
- 관리자 restore/hard delete 통합 테스트를 새로 추가해 애그리거트 정합성을 검증했습니다.
