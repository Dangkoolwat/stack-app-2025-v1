---
agent: GPT-5.4
created_at: 2026-03-26 (Thu)
language: ko
---

# 프로젝트 리팩토링 후속 리뷰

## 1. 검토 범위

이번 리뷰는 아래 두 축을 대조하는 방식으로 진행했다.

- 작업 기준 문서
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md`
  - `docs/analysis/2026-03-25-gpt54/project-wide-review.md`
- 완료 보고 문서
  - `docs/backend/agent-log/2026-03-26-project-refactoring/`
- 실제 반영 코드
  - 보안, 인가, 게시글 aggregate, 업로드 다운로드, 테스트 설정 관련 주요 파일

리뷰 목적은 "이미 구현된 것의 요약"이 아니라, 계획 대비 미완료 지점과 추가 보완이 필요한 지점을 식별하는 것이다.

## 2. 전체 판단

이번 작업은 다음 항목을 실제로 전진시켰다.

- 캐시 이름 중앙화
- 일부 owner/admin 검증 도입
- Board soft delete 시 자식 엔티티 soft delete 연쇄 처리
- `*T.java` 테스트 실행 포함

하지만 2026-03-25 분석 문서의 완료 기준을 기준으로 보면, 보안과 aggregate 정합성의 핵심 일부는 아직 닫히지 않았다.

특히 아래 4개 항목은 "후속 작업 필요"가 아니라, 현재 완료 보고를 그대로 수용하면 안 되는 수준의 미비점으로 본다.

## 3. 주요 미비점

### 3.1. 게시글 생성이 여전히 요청 `userId` 를 신뢰함

심각도: Critical

근거:

- 계획 문서는 게시글 생성 시 요청 `userId` 를 무시하고 로그인 사용자로 강제하도록 요구했다.
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md:137`
- 그러나 실제 구현은 `userId` 가 비어 있을 때만 현재 로그인 사용자를 채우고, 값이 있으면 그대로 사용한다.
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java:90-101`

영향:

- 인증 사용자 A가 요청 payload 에 B의 `userId` 를 넣으면 B 명의 게시글 생성이 가능하다.
- 객체 수준 인가의 핵심 전제가 무너진다.

권장 조치:

- `BoardService.save()` 에서 요청 `dto.userId` 를 완전히 무시하고 현재 로그인 사용자로 강제한다.
- "요청 `userId` 조작 시 실제 작성자는 로그인 사용자로 저장"되는 통합 테스트를 추가한다.

### 3.2. 댓글 생성도 여전히 요청 `userId` 를 신뢰함

심각도: Critical

근거:

- 계획 문서는 댓글 생성 시에도 요청 `userId` 를 무시하도록 요구했다.
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md:138`
- 실제 구현은 `boardId` 와 `userId` 가 모두 필수이며, 전달받은 `userId` 로 사용자를 그대로 조회한다.
  - `src/main/java/com/daangcool/stack/service/board/CommentService.java:78-93`

영향:

- 인증 사용자 A가 B의 `userId` 를 넣어 B 명의 댓글을 생성할 수 있다.
- 댓글 수정/삭제 권한 검증이 이후에 추가되어 있어도, 생성 시점 위조를 막지 못하면 근본 방어가 되지 않는다.

권장 조치:

- `CommentService.save()` 도 현재 로그인 사용자를 기준으로 작성자를 강제한다.
- 게시글과 동일하게 spoofing 방지 테스트를 추가한다.

### 3.3. private upload 다운로드는 아직 owner/admin 검증이 없음

심각도: Critical

근거:

- 계획 문서는 private upload 다운로드를 owner/admin 으로 제한하도록 요구했다.
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md:141`
- 실제 다운로드 엔드포인트는 인증 여부만 확인하고, 리소스 소유자 검증은 수행하지 않는다.
  - `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:203-220`
- 현재 통합 테스트도 "인증된 사용자는 누구나 성공"하는 시나리오만 검증한다.
  - `src/test/java/com/daangcool/stack/web/rest/UploadResourceIT.java:103-110`

영향:

- 로그인만 되어 있으면 타인의 private 파일을 다운로드할 수 있다.
- 분석 문서가 가장 먼저 막으려던 교차 사용자 접근이 그대로 남는다.

권장 조치:

- `UploadResource.downloadPrivateFile()` 또는 `UploadService` 계층에서 `ResourceAuthorizationService` 를 통해 owner/admin 검증을 수행한다.
- "A 사용자가 B 사용자의 private upload 다운로드 시 403" 테스트를 추가한다.
- 관리자 우회 허용 테스트도 함께 추가한다.

### 3.4. Board aggregate 생명주기 정리는 soft delete 일부만 구현됨

심각도: High

근거:

- 계획 문서는 soft delete 뿐 아니라 restore, hard delete, tag usageCount 반영까지 한 묶음으로 정리하도록 요구했다.
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md:194-210`
- 실제 구현에서 soft delete 시 자식 엔티티 soft delete 는 들어갔다.
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java:267-273`
- 그러나 restore 는 Board 본체만 복구하고 자식 엔티티를 복구하지 않는다.
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java:398-406`
- hard delete 역시 자식 댓글, 태그 관계, 업로드 파일 정리 없이 Board 만 물리 삭제한다.
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java:411-416`
- 또한 soft delete 경로에서 tag usageCount 감소 처리도 빠져 있다.
  - `src/main/java/com/daangcool/stack/service/board/BoardService.java:267-273`
- 그런데 완료 보고서는 "계층적 생명주기 로직을 구현하여 데이터 정합성을 확보"했다고 단정한다.
  - `docs/backend/agent-log/2026-03-26-project-refactoring/final-report.md:13-15`

영향:

- 복구 시 게시글만 살아나고 댓글/첨부/태그 관계는 계속 삭제 상태일 수 있다.
- hard delete 시 FK 제약 또는 orphan resource 정리가 불완전할 수 있다.
- tag usageCount drift 문제도 그대로 남을 가능성이 높다.

권장 조치:

- Board aggregate 에 대해 soft delete, restore, hard delete 를 하나의 오케스트레이션으로 다시 정리한다.
- restore 시 Comment/Upload/BoardTag 복구와 usageCount 복원을 구현한다.
- hard delete 시 Comment 물리 삭제, BoardTag 정리, Upload 물리 삭제, Tag usageCount 반영 순서를 명시한다.
- 이 흐름을 통합 테스트로 고정한다.

## 4. 추가 보완 필요 항목

### 4.1. management 공개 범위 축소 작업은 아직 완료로 보기 어려움

근거:

- 계획 문서는 `/management/health` 계열만 공개하고 `/management/info`, `/management/metrics`, `/management/prometheus` 는 차단하는 방향을 권장했다.
  - `docs/analysis/2026-03-25-gpt54/implementation_plan.md:97-109`
- 하지만 현재 설정에는 여전히 `/management/info` 가 공개 경로에 포함되어 있다.
  - `src/main/resources/config/application.yml:302-305`
- 이 경계를 검증하는 전용 통합 테스트도 저장소에서 확인되지 않았다.

메모:

- 이것은 즉시 취약점이라고 단정하기보다, 2026-03-25 계획 문서의 완료 기준이 아직 충족되지 않았다는 뜻에 가깝다.

### 4.2. 공개 다운로드/미리보기의 스트리밍 전환은 미처리 상태

근거:

- 공개 다운로드와 preview 는 여전히 `readAllBytes()` 를 사용한다.
  - `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:116-120`
  - `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:166-170`
- 클래스 주석은 OOM 방지를 위해 스트리밍을 유지하라고 설명하고 있어 코드와 문서가 어긋난다.

메모:

- 이번 라운드의 핵심 보안 이슈보다는 우선순위가 낮지만, 파일 트래픽이 커질수록 운영 리스크가 된다.

## 5. 권장 후속 순서

1. 생성 경로의 `userId` 신뢰 제거
2. private upload 다운로드 owner/admin 강제
3. 교차 사용자 부정 테스트 추가
4. Board aggregate restore/hard delete 완성
5. management 공개 경계 테스트 추가 및 `/management/info` 공개 여부 재결정
6. 공개 다운로드/preview 스트리밍 전환

## 6. 검증 메모

이번 리뷰에서는 문서와 코드의 정합성 검토를 수행했다.

- 수행한 검증
  - 분석 문서와 agent-log 대조
  - 관련 서비스/리소스/설정/테스트 코드 직접 확인
- 수행하지 않은 검증
  - 전체 테스트 재실행
  - 실제 런타임에서 보안 시나리오 재현

따라서 테스트 통과 여부 자체를 부정하는 것은 아니지만, "분석 계획 대비 완료 여부" 관점에서는 위 미비점을 남은 작업으로 명확히 표시해야 한다.
