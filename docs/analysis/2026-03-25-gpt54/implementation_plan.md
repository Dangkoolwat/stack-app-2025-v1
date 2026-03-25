---
agent: GPT-5.4
created_at: 2026-03-25 (수)
language: ko
---

# 프로젝트 전체 리팩토링 수행 계획

## 1. 문서 목적

이 문서는 다음 두 분석 문서를 통합하여, 실제 구현 작업으로 바로 이어질 수 있는 실행 계획을 제공하기 위한 문서입니다.

- `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md`
- `docs/analysis/2026-03-25-gpt54/project-wide-review.md`

목표는 단순한 문제 요약이 아니라, 다른 에이전트가 이 문서만 보고도 다음을 수행할 수 있도록 만드는 것입니다.

- 무엇을 먼저 해야 하는지 이해
- 어떤 파일을 수정해야 하는지 파악
- 어떤 문제를 같이 묶어야 하는지 구분
- 어떤 테스트를 추가하거나 실행해야 하는지 판단
- 어디까지가 완료인지 명확히 확인

## 2. 전체 전략

이번 리팩토링은 하나의 거대한 작업으로 밀어붙이면 위험합니다. 다음 원칙으로 나누어 진행해야 합니다.

1. 보안과 인가 문제를 가장 먼저 해결한다.
2. 게시글 aggregate 정합성은 하나의 묶음으로 다룬다.
3. 캐시, orphan resource, 관리자 리소스 관리는 aggregate 정합성 이후에 정리한다.
4. 테스트 실행 신뢰도를 초반에 보강하여 이후 작업의 안전성을 높인다.
5. 한 번에 너무 많은 구조 변경을 하지 말고, 각 단계마다 통과 기준을 둔다.

## 3. 작업 스트림 개요

전체 작업은 아래 7개 스트림으로 나눈다.

1. 보안 설정 정리
2. 객체 수준 인가 도입
3. Board aggregate 생명주기 정합성 정리
4. Tag / BoardTag 책임 재배치 및 캐시 일원화
5. Orphan resource 관리 기능 정리
6. 테스트 체계 정비
7. 문서 및 정책 정리

권장 순서는 1 → 2 → 6 일부 → 3 → 4 → 5 → 7 이다.

## 4. 선행 조건

실제 코드 작업 전, 아래를 먼저 점검한다.

### 4.1. 브랜치와 작업 범위

- 기존 사용자 변경사항이 있는지 `git status` 로 확인
- unrelated 변경은 절대 되돌리지 않음
- 가능하면 작업 스트림별로 커밋을 분리

### 4.2. 테스트 실행 기준 확인

- 현재 Maven test 에서 실제로 어떤 테스트가 잡히는지 확인
- `*T.java` 패턴 테스트가 누락되는지 먼저 확인
- 이후 기능 수정 전후로 같은 테스트 명령을 유지

### 4.3. 문서 기준

- 시스템 전체 리뷰 및 구현 계획 문서는 현재 사용자 요청 기준으로 한국어 허용
- 코드 주석은 기존 정책대로 한국어 유지

## 5. 스트림별 상세 계획

## 스트림 1. 보안 설정 정리

### 목표

관리 엔드포인트가 익명 사용자에게 잘못 공개되는 위험을 제거한다.

### 핵심 문제

- `application.security.public-paths.management` 와 `SecurityConfiguration` 의 request matcher 순서가 충돌한다.
- `/management/**` 전체 공개 가능성이 있다.

### 주요 수정 대상

- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java`
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
- 필요 시 관련 설정 문서

### 구현 작업

1. `publicPaths.management` 기본값에서 `/management/**` 제거
2. 공개할 최소 경로만 allowlist 로 재정의
3. `SecurityConfiguration` 에서 management 경로 순서를 명확히 재정리
4. `/management/prometheus` 와 기타 관리 API 는 관리자 전용 유지

### 권장 구현 방식

- 가장 단순한 방식은 `/management/health` 와 `/management/health/**` 만 공개하고 나머지는 전부 관리자 전용으로 두는 것이다.

### 테스트

- 익명 사용자로 `/management/health` 접근 가능 여부
- 익명 사용자로 `/management/prometheus` 접근 차단 여부
- 익명 사용자로 `/management/metrics`, `/management/info` 차단 여부
- 관리자 사용자 접근 허용 여부

### 완료 기준

- `/management/**` 와일드카드 공개 규칙이 사라진다.
- 통합 테스트로 공개/비공개 경계가 고정된다.

### 병렬 작업 가능 여부

- 스트림 2 와 일부 병렬 가능
- 스트림 3 과는 독립적

## 스트림 2. 객체 수준 인가 도입

### 목표

로그인만 되어 있으면 남의 게시글, 댓글, 파일을 수정/삭제/다운로드할 수 있는 구조를 제거한다.

### 핵심 문제

- create/update/delete/download 에서 owner/admin 검사가 빠져 있다.
- 요청의 `userId` 를 신뢰하는 코드가 존재한다.

### 주요 수정 대상

- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/CommentService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`
- 필요 시 `SecurityUtils`, 공통 authorization helper 신설

### 구현 작업

1. 게시글 생성 시 요청 `userId` 무시, 로그인 사용자로 강제
2. 댓글 생성 시 요청 `userId` 무시, 로그인 사용자로 강제
3. 게시글 수정/삭제는 owner 또는 admin 만 가능하게 변경
4. 댓글 수정/삭제는 owner 또는 admin 만 가능하게 변경
5. 파일 soft delete 와 private download 는 owner 또는 admin 만 가능하게 변경
6. 공통 판별 로직을 helper 또는 authorization service 로 통합

### 권장 구현 방식

- `ResourceAuthorizationService` 같은 공통 서비스 신설을 권장
- 최소 메서드 예시:
  - `assertBoardOwnerOrAdmin(Board board)`
  - `assertCommentOwnerOrAdmin(Comment comment)`
  - `assertUploadOwnerOrAdmin(Upload upload)`

### 테스트

- A 사용자가 B 사용자의 게시글 수정 시 403 또는 접근 거부 예외
- A 사용자가 B 사용자의 댓글 삭제 시 차단
- A 사용자가 B 사용자의 private upload 다운로드 시 차단
- 관리자 사용자는 우회 가능
- 생성 시 요청 `userId` 를 조작해도 실제 작성자는 로그인 사용자로 저장

### 완료 기준

- user-facing 쓰기 API 에서 요청 `userId` 를 더 이상 신뢰하지 않음
- owner/admin 규칙이 board/comment/upload 에 모두 적용됨
- 교차 사용자 부정 테스트가 추가됨

### 병렬 작업 가능 여부

- 스트림 1 과 병렬 가능
- 스트림 3 과는 일부 충돌 가능하므로 owner 체크 helper 설계는 먼저 고정하는 편이 좋음

## 스트림 3. Board aggregate 생명주기 정합성 정리

### 목표

`Board`, `BoardTag`, `Upload`, `Comment` 를 하나의 aggregate 로 보고 soft delete, restore, hard delete 흐름을 일관되게 정리한다.

### 핵심 문제

- Board 삭제 시 연관 엔티티 cascade soft delete 누락
- restore 시 연관 복구 누락
- hard delete 시 FK 제약 위반 가능성
- tag usageCount drift

### 주요 수정 대상

- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/BoardTagService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/service/board/CommentService.java`
- 관련 repository 들

### 구현 작업

1. Board soft delete 시:
   - BoardTag soft delete
   - Tag usageCount 감소
   - Upload soft delete
   - Comment soft delete
2. Board restore 시:
   - BoardTag restore
   - Tag usageCount 복원
   - Upload restore
   - Comment restore
3. Board hard delete 시:
   - Comment 물리 삭제
   - BoardTag 물리 삭제
   - Tag usageCount 반영
   - Upload 물리 삭제 및 스토리지 정리
   - Board 물리 삭제
4. 생명주기 처리 순서를 코드와 테스트에 명시

### 주의 사항

- Upload hard delete 는 물리 파일 삭제와 결합되므로 실패 처리 전략이 중요
- 하나의 트랜잭션으로 묶을 수 없는 스토리지 작업은 예외 처리 정책을 먼저 정해야 함
- hard delete 는 관리자 전용 경로에서만 호출되도록 보장해야 함

### 테스트

- Board soft delete 후 연관 Comment/Upload/BoardTag 상태 검증
- Board restore 후 연관 복구 상태 검증
- Board hard delete 후 FK 예외 없이 삭제 완료 검증
- Tag usageCount 증가/감소 재검증

### 완료 기준

- Board 생명주기 관련 주요 경로가 aggregate 단위로 설명 가능해짐
- soft delete / restore / hard delete 테스트가 모두 존재

### 병렬 작업 가능 여부

- 스트림 4 와 강하게 연결됨
- 스트림 5 는 스트림 3 완료 이후 착수 권장

## 스트림 4. Tag / BoardTag 책임 재배치 및 캐시 일원화

### 목표

태그 동기화와 캐시 무효화 책임을 정리하여 stale cache 와 중복 로직을 제거한다.

### 핵심 문제

- `BoardService.syncTags()` 가 과도한 책임을 가지고 있다.
- `BoardService` 와 `BoardTagService` 가 서로 다른 캐시 이름을 사용한다.
- soft delete 된 tag 재사용 처리도 현재 설계상 취약하다.

### 주요 수정 대상

- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/BoardTagService.java`
- `src/main/java/com/daangcool/stack/service/board/TagService.java`
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- 관련 repository / mapper

### 구현 작업

1. `BoardTagService.syncTags(Board board, List<String> tagNames)` 신설 또는 확장
2. `BoardService` 는 태그 동기화 세부사항을 위임만 하도록 단순화
3. 캐시 이름 상수를 공통 클래스로 통합
4. soft delete 된 Tag 를 포함 조회할 수 있는 repository 메서드 추가
5. soft delete tag 재활성화 로직 도입
6. `BoardMapper.toDto()` 에서 boardTags 방어 필터 보강

### 권장 구현 방식

- 캐시 이름은 `CacheNames` 또는 도메인 공통 상수 클래스로 이동
- `BoardService`, `BoardTagService`, `CacheConfiguration` 이 같은 상수를 공유

### 테스트

- soft delete 된 tag 이름 재사용 시 unique 오류 미발생
- 태그 추가/삭제 후 board page cache 무효화 검증
- mapper 에서 deleted boardTag 노출 방지 검증

### 완료 기준

- `BoardService` 에서 태그 도메인 세부 구현이 제거되거나 크게 축소됨
- 캐시 이름 drift 가 사라짐
- tag 재사용 관련 예외 재현 테스트가 통과

### 병렬 작업 가능 여부

- 스트림 3 과 같은 담당자가 함께 처리하는 것이 안전

## 스트림 5. Orphan resource 관리 기능 정리

### 목표

게시판 리소스 관리 기능을 aggregate 정합성과 맞게 재정의하고, 관리자 UI/API 를 안전하게 완성한다.

### 핵심 문제

- orphan 판단 기준이 현재 board aggregate 정합성과 완전히 맞물려 있지 않다.
- aggregate cascade 가 정리되기 전에 orphan 관리 기능부터 확장하면 오판 가능성이 있다.

### 주요 수정 대상

- `src/main/java/com/daangcool/stack/web/rest/OrphanResourceAdminResource.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/service/board/TagService.java`
- orphan 관련 repository query
- 프런트엔드의 `board-resource-management` 관련 화면

### 구현 작업

1. aggregate cascade 정리 이후 orphan 정의 재검증
2. 대상 리소스별 orphan 조건 문서화
3. 관리자 목록 조회 API 정비
4. 일괄 hard delete API 정비
5. 프런트엔드에서 조회, 선택, 완전 삭제 UX 보완

### 권장 구현 순서

1. 백엔드 orphan 조건부터 정리
2. 테스트 추가
3. 프런트엔드 연동

### 테스트

- soft delete 된 upload/tag 조회 가능 여부
- orphan 조건에 맞는 리소스만 조회되는지
- 일괄 hard delete 후 DB 와 스토리지 상태 검증
- 비관리자 접근 차단 검증

### 완료 기준

- orphan 관리 기능이 aggregate 생명주기와 충돌하지 않음
- 관리자 화면에서 실제 정리 가능한 상태가 됨

### 병렬 작업 가능 여부

- 백엔드와 프런트엔드는 병렬 가능
- 단, orphan 조건 정의는 백엔드 기준으로 먼저 고정 필요

## 스트림 6. 테스트 체계 정비

### 목표

이후 리팩토링의 안전성을 위해 테스트가 실제로 실행되고, 중요한 회귀 시나리오를 포착하도록 만든다.

### 핵심 문제

- `*T.java` 테스트 실행 여부가 불명확
- 보안/인가/캐시 관련 부정 테스트가 부족

### 주요 수정 대상

- `pom.xml`
- `src/test/java` 하위 테스트 파일들

### 구현 작업

1. `*T.java` 를 `*Test.java` 로 rename 하거나 Surefire include 추가
2. 보안 부정 테스트 추가
3. aggregate 생명주기 테스트 추가
4. 캐시 invalidation 테스트 추가
5. 관리 엔드포인트 접근 테스트 추가

### 권장 구현 방식

- 가능하면 테스트 파일명은 `*Test.java` 로 통일
- 단기적으로 시간이 부족하면 Surefire include 를 추가하되, 장기적으로는 rename 권장

### 완료 기준

- Maven test 기준 실행 대상이 명확함
- 새로 추가된 핵심 회귀 테스트가 CI 성격으로 동작 가능함

### 병렬 작업 가능 여부

- 모든 스트림과 병렬 가능
- 가장 먼저 일부 착수하는 것이 좋음

## 스트림 7. 문서 및 정책 정리

### 목표

분석 문서와 정책 문서를 현재 운영 방식에 맞게 정리하여 다음 작업자가 혼란 없이 이어받도록 만든다.

### 주요 수정 대상

- `AGENTS.md`
- `docs/analysis/2026-03-25-antigravity/*`
- `docs/analysis/2026-03-25-gpt54/*`
- 필요 시 `docs/standards/*`

### 구현 작업

1. 시스템 리뷰 문서의 한국어 허용 범위를 현재 정책대로 유지
2. 이번 구현 계획 문서를 기준 문서로 연결
3. 이후 실제 코드 작업마다 agent-log 또는 후속 analysis 문서를 남김

### 완료 기준

- 다음 에이전트가 어떤 문서를 읽고 시작해야 하는지 명확함

## 6. 권장 실행 순서와 병렬화 가이드

### 순차 우선 작업

1. 스트림 1 보안 설정 정리
2. 스트림 2 객체 수준 인가 도입
3. 스트림 6 테스트 체계 정비의 최소선 확보
4. 스트림 3 aggregate 생명주기 정합성
5. 스트림 4 Tag / Cache 정리
6. 스트림 5 Orphan resource 관리
7. 스트림 7 문서 정리

### 병렬 가능 조합

- 에이전트 A: 스트림 1 보안 설정
- 에이전트 B: 스트림 6 테스트 파일명/빌드 규칙 정리
- 에이전트 C: 스트림 2 객체 수준 인가 설계

위 3개는 초기 병렬 작업이 가능하다.

이후에는 아래처럼 묶는 것을 권장한다.

- 에이전트 A: 스트림 3 aggregate 생명주기
- 에이전트 B: 스트림 4 Tag / Cache 정리
- 에이전트 C: 스트림 5 프런트엔드 포함 orphan resource 관리

단, 스트림 5 는 스트림 3 의 orphan 기준이 고정된 뒤 본격 착수해야 한다.

## 7. 에이전트 handoff 규칙

다른 에이전트가 이 문서를 보고 작업할 때 반드시 지켜야 할 규칙은 다음과 같다.

1. 스트림 3 과 스트림 4 는 서로 독립 작업처럼 보이지만 실제로는 강하게 연결되어 있으므로, 동일 에이전트 또는 긴밀히 조율되는 두 에이전트가 맡는다.
2. owner/admin 인가 규칙은 컨트롤러와 서비스에 분산 구현하지 말고 공통 정책으로 먼저 고정한다.
3. orphan resource 조건은 aggregate 생명주기 정리 전에는 확정하지 않는다.
4. 테스트 체계 정비 없이 대규모 생명주기 리팩토링부터 시작하지 않는다.
5. 캐시 상수는 새로 늘리지 말고 통합 방향으로만 수정한다.

## 8. 단계별 완료 정의

### 1차 완료

- management 보안 규칙 정리
- board/comment/upload owner 체크 도입
- 관련 부정 테스트 추가

### 2차 완료

- board aggregate soft delete / restore / hard delete 정합성 확보
- tag usageCount drift 방지
- cache 이름 통일

### 3차 완료

- orphan resource 관리 기능 완성
- 테스트 체계 안정화
- 문서 정리 완료

## 9. 최종 검증 체크리스트

아래 항목이 모두 충족되면 이번 리팩토링 계획 범위는 완료로 본다.

- `/management/**` 접근 제어가 기대대로 동작한다.
- 일반 사용자가 남의 게시글/댓글/파일에 쓰기 작업을 하지 못한다.
- 일반 사용자가 남의 private file 을 다운로드하지 못한다.
- board 삭제/복구/완전삭제 시 연관 리소스 정합성이 유지된다.
- tag 재사용 시 unique 예외가 발생하지 않는다.
- tag 변경 후 board page cache stale 문제가 재현되지 않는다.
- orphan resource 조회/삭제 결과가 aggregate 정책과 일치한다.
- 단위 테스트와 통합 테스트 실행 범위가 명확해진다.

## 10. 바로 다음 액션

가장 먼저 시작할 추천 순서는 다음과 같다.

1. `pom.xml` 과 테스트 파일명 규칙을 점검하여 테스트 실행 신뢰도 확보
2. `SecurityConfiguration` 과 `ApplicationProperties` 에서 management 공개 범위 수정
3. `BoardService`, `CommentService`, `UploadService` 에 owner/admin 인가 공통 로직 도입
4. 이후 `BoardService` 와 `BoardTagService` 를 중심으로 aggregate 생명주기 정리 착수

이 문서는 구현의 출발점 문서이며, 실제 코드 변경이 시작되면 각 스트림별 agent-log 또는 후속 analysis 문서로 세부 진행 상황을 남겨야 한다.
