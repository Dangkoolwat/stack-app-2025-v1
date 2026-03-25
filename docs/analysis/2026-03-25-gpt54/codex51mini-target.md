---
agent: GPT-5.4
created_at: 2026-03-25 (수)
language: ko
---

# Codex 5.1 mini 대상 실행 문서

## 1. 문서 목적

이 문서는 `Codex 5.1 mini` 가 실제 코드 변경 작업을 수행할 수 있도록, 이번 리팩토링 계획 중 우선 착수 범위를 실행 중심으로 정리한 작업 지시 문서입니다.

이 문서의 역할은 다음과 같습니다.

- 무엇을 수정할지 명확히 지정
- 무엇은 이번 턴에 하지 말아야 하는지 경계 설정
- 어떤 파일을 우선 볼지 안내
- 어떤 테스트를 추가 또는 확인해야 하는지 고정
- 다음 단계로 넘겨야 할 설계 메모를 남기게 하기

## 2. 반드시 먼저 읽을 문서

작업 시작 전에 아래 문서를 반드시 읽습니다.

1. `docs/analysis/2026-03-25-gpt54/implementation_plan.md`
2. `docs/analysis/2026-03-25-gpt54/project-wide-review.md`
3. `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md`

실행 기준은 `implementation_plan.md` 이고, 세부 위험 근거는 나머지 두 문서에서 확인합니다.

## 3. 이번 작업 범위

이번 턴에서 `Codex 5.1 mini` 가 맡아야 할 범위는 아래 3개 스트림의 최소선입니다.

1. 스트림 1 보안 설정 정리
2. 스트림 2 객체 수준 인가 도입
3. 스트림 6 테스트 체계 정비의 최소선

이번 턴에서 하지 말아야 할 범위는 아래와 같습니다.

- 스트림 3 Board aggregate 생명주기 대공사 전체
- 스트림 4 Tag / BoardTag 책임 재배치 전체
- 스트림 5 orphan resource 정책 최종 확정
- 프런트엔드 대규모 UI 변경

## 4. 핵심 목표

이번 작업의 성공 기준은 다음 4개입니다.

1. `/management/**` 가 잘못 공개되지 않도록 보안 규칙을 수정한다.
2. board/comment/upload 에서 owner 또는 admin 인가 규칙의 최소 골격을 도입한다.
3. 요청의 `userId` 를 신뢰하는 생성 로직을 제거한다.
4. 위 변경을 보호할 최소 테스트를 추가하거나, 실행 불가 사유를 명확히 남긴다.

## 5. 우선 수정 대상 파일

가장 먼저 확인할 파일은 아래와 같습니다.

- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java`
- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/CommentService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`
- `pom.xml`
- `src/test/java` 하위 관련 테스트

## 6. 작업 지시

### 6.1. 보안 설정 정리

수행 항목:

1. `/management/**` 전체 공개가 가능한 경로 구성을 제거
2. 공개가 필요한 경우 최소 health 경로만 허용
3. 나머지 management 경로는 관리자 전용으로 고정

구현 메모:

- `ApplicationProperties.Security.PublicPaths.management` 기본값 검토
- `SecurityConfiguration.authorizeHttpRequests` 의 매처 순서 재검토
- broad permitAll 규칙이 admin 규칙보다 먼저 나오지 않게 정리

완료 기준:

- 익명 사용자의 management 접근 경계가 코드상 명확
- management security 관련 테스트 또는 검증 메모 존재

### 6.2. 객체 수준 인가 도입

수행 항목:

1. Board 생성 시 요청 `userId` 무시
2. Comment 생성 시 요청 `userId` 무시
3. Board 수정/삭제는 owner 또는 admin 만 허용
4. Comment 수정/삭제는 owner 또는 admin 만 허용
5. Upload soft delete 와 private download 는 owner 또는 admin 만 허용

권장 구현 방식:

- 가능한 한 공통 authorization helper 또는 service 로 묶을 것
- 중복된 owner/admin 판별 코드를 각 서비스에 복붙하지 않는 방향 선호

예상 helper 예시:

- `assertBoardOwnerOrAdmin(...)`
- `assertCommentOwnerOrAdmin(...)`
- `assertUploadOwnerOrAdmin(...)`

주의:

- 이번 턴의 목표는 aggregate 전체 정리가 아니라 “남의 리소스를 건드릴 수 없는 최소 안전선” 확보입니다.
- 따라서 설계는 재사용 가능하게 하되, 구현 범위를 무리하게 넓히지 않습니다.

### 6.3. 테스트 체계 최소선 정비

수행 항목:

1. `*T.java` 테스트가 Maven test 에 잡히는지 확인
2. 안 잡히면 아래 둘 중 하나 선택
   - `*Test.java` 로 rename
   - Surefire include 추가
3. 아래 부정 테스트를 최소한 일부 추가
   - 교차 사용자 board update 차단
   - 교차 사용자 comment delete 차단
   - 교차 사용자 private upload download 차단
   - management unauthorized access 차단

선호 순서:

1. 실행 여부 확인
2. 최소한의 naming 또는 build fix
3. 핵심 부정 테스트 추가

## 7. 이번 턴에서 하지 말아야 할 것

다음 작업은 이번 턴 범위를 넘기므로 착수하지 않습니다.

- Board delete/restore/hard delete 전체 cascade 구현
- Tag soft delete 재활성화 로직 전체 정리
- BoardTagService 로 syncTags 전면 이관
- orphan resource 정의 확정
- 캐시 상수 전면 통합

이 항목들은 후속 스트림으로 넘깁니다.

## 8. 테스트 및 검증 기준

가능하면 아래를 확인합니다.

- `/management/health` 공개 여부
- `/management/prometheus` 비공개 여부
- 교차 사용자 쓰기 요청 차단 여부
- private upload 다운로드 권한 차단 여부
- 생성 시 userId 위조 방지 여부

테스트를 직접 못 돌리면 아래를 반드시 남깁니다.

- 어떤 명령을 시도했는지
- 왜 막혔는지
- 어떤 테스트를 추가했는지
- 무엇이 아직 검증되지 않았는지

## 9. 최종 보고 형식

작업 후 보고는 아래 형식으로 정리합니다.

1. 실제 변경한 파일
2. 해결한 문제
3. 실행 또는 추가한 테스트
4. 이번 턴에서 남긴 후속 작업 메모
5. 스트림 3, 4 담당자에게 넘길 주의사항

## 10. 후속 담당자에게 넘길 메모

이번 작업이 끝나면 반드시 아래를 다음 담당자에게 넘깁니다.

- owner/admin authorization helper 설계 상태
- management security 규칙 최종 형태
- 테스트 naming 또는 Surefire 처리 방식
- 스트림 3 진입 전 필요한 제약 사항
