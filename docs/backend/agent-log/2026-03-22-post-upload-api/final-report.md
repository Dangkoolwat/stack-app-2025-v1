# Final Report

## 1. Summary (작업 요약)
- 목표: Vue 3 환경의 게시글 작성/수정(Create/Update Board) 페이지에 TOAST UI Markdown Editor, 다중 첨부파일 드래그 앤 드롭, 해시태그 시스템(Bootstrap-Vue-Next `<b-form-tags>`) 적용 및 백엔드와의 통합.
- 주요 구현:
  1. Vue 3 `board-update.vue` 및 `board-update.component.ts` 수정.
  2. TOAST UI Editor 통합 (`@toast-ui/editor`) 및 드래그 앤 드롭 이미지 업로드 후 Markdown 삽입 처리.
  3. 에디터 영역 내의 이미지를 입력 도중 지우고 최종 저장 시 유실된 업로드 파일(고아 객체)에 대해 `DELETE /api/uploads/{id}` 호출로 즉각적인 Soft Delete 구현 (효율적인 용량 관리).
  4. 별도 영역을 통한 최대 5개, 각 10MB 제한의 첨부 파일존(Dropzone) 구현 및 파일 개별 삭제 기능.
  5. 태그(문자열 배열)와 첨부파일(ID 포함 객체 배열)을 백엔드 요청 DTO(`BoardDTO`)에 담아 전송하는 맵핑 기능 활성화.
  6. `BoardMapper.java`, `BoardService.java`, `BoardDTO.java` 등 백엔드 영속 계층을 연동하여 `syncTags` 및 `syncUploads` 로직 구축 (게시글 생성/수정에 맞춰 관련된 `Tag`, `BoardTag`, `Upload` 엔티티 매핑).
  7. (HOTFIX) `Page<BoardDTO>` Redisson 캐시 시도 중 Jackson 3의 `PagedModel` 직렬화 에러 발생 (`InvalidDefinitionException`). `CacheConfiguration.java`에 `PagedModelMixIn`을 선언하여 Jackson 3에서도 Spring Data `PagedModel` 클래스를 안전하게 역직렬화(Deserialize)할 수 있도록 조치함.
- 결과 검증 (Verification):
  - 프론트엔드 컴파일/빌드 검증 (`npm run build`) 성공.
  - 백엔드 단위/통합 테스트(`mvnw clean test`) 성공 및 Redisson Page 캐싱 작동 확인.

## 2. Impact & Risks (영향 및 리스크 파악)
- 보안 (Security):
  - 게시물 생성 시(DTO에 userId가 Null일 때) 클라이언트를 신뢰하지 않고 서버 측 `SecurityUtils.getCurrentUserLogin`을 통해 인증된 세션(로그인 사용자)으로 `userId`를 강제 할당하여 사용자 식별 위조 취약점(IDOR)을 방어.
  - `<b-form-tags>` 로 입력받는 태그 등은 배열로 송출되므로 클라이언트/서버 분리 처리.
  - 마크다운 저장 시 XSS는 향후 Detail 페이지 렌더링 시 보완 요망.
- 영향도 (Impact):
  - Tag 및 Upload, PagedModel 등 모든 데이터 흐름이 안정적이고 캐시(Redis)까지 완벽하게 스며들었습니다.

## 3. Assumptions & Remaining Works (가정 및 잔여 작업)
- [x] 프론트엔드 및 백엔드 간 상호 통신 파이프라인 정립 (보안 토큰 기반 UserId 주입 완료)
- [x] Redis Cache의 PagedModel 역직렬화 호환성(Jackson 3) 확보 완료
- 완료되었습니다! 실 환경에서 추가적인 사이드 이펙트 모니터링만 해주시면 됩니다.
