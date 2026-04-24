---
agent: Antigravity (Gemini)
created_at: 2026-03-25 (화)
language: ko
---

# Project-wide Entity Relationship and Service Logic Analysis

## 1. Analysis Scope

프로젝트 전체 엔티티를 대상으로, 연관관계 정합성, 서비스 계층 로직의 문제점, 그리고 개선점을 분석합니다.

### 분석 대상 파일

| Layer | Entity/Module | Files |
|-------|---------------|-------|
| Entity | Board | `Board.java`, `BoardTag.java`, `Tag.java`, `Upload.java`, `Comment.java` |
| Entity | User/Auth | `User.java`, `Authority.java`, `AbstractAuditingEntity.java` |
| Entity | Common | `CommonCodeGroup.java`, `CommonCodeDetail.java` |
| Entity | System | `Settings.java`, `EmailOtpLog.java` |
| Service | Board | `BoardService`, `BoardTagService`, `TagService`, `UploadService`, `CommentService` |
| Service | User/System | `UserService`, `CommonCodeService`, `GlobalSettingsService`, `EmailOtpLogService` |
| Repository | All | `BoardRepository` 외 9개 |

---

## 2. Entity Relationship Map

```
User (1) ----< (N) Board
User (1) ----< (N) Comment
User (N) >----< (N) Authority   [via stack_user_authority]
User (1) ----< (N) EmailOtpLog

Board (1) ----< (N) BoardTag (N) >---- (1) Tag
Board (1) ----< (N) Upload      [nullable FK]
Board (1) ----< (N) Comment
Board (N) >---- (1) CommonCodeDetail  [boardType]

CommonCodeGroup (1) ----< (N) CommonCodeDetail

Settings  [Singleton, ID=1, JSON CLOB]
```

---

## Part A: Board Domain Issues

### ISSUE-01: syncTags() - Soft Delete된 Tag 재생성 시 UNIQUE 위반

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L221)

`findByNameIgnoreCase()`는 `@Filter(softDeleteFilter)` 적용 상태에서 호출됩니다. Soft Delete된 Tag가 조회되지 않아 새 Tag 생성이 시도되면, `name` UNIQUE 제약 위반으로 DB 예외가 발생합니다.

- 심각도: HIGH
- 발생 시나리오: 관리자가 태그를 Soft Delete한 뒤, 사용자가 같은 이름으로 태그를 추가할 때
- 근본 원인: Soft Delete + UNIQUE 제약의 충돌. DB에 물리적으로 남아 있는 `is_deleted=1` 레코드가 UNIQUE 검사 대상에 포함됨.
- 대안 검토:
  - DB Conditional UNIQUE (Function-based Index): Oracle/PostgreSQL에서는 `is_deleted=0`인 행에만 UNIQUE를 적용할 수 있으나, MySQL/H2 등 미지원 DB가 있어 JPA의 DB 이식성(호환성)이 깨짐.
  - 서비스 레벨 해결: DB 종류에 무관하게 동작하며 JPA 표준 범위 내에서 처리 가능.
- 개선안 (서비스 레벨): `findByNameIgnoreCase()`를 삭제 포함 조회(`@IncludeDeleted` 스코프 또는 별도 쿼리)로 교체.

```java
Tag tag = tagRepository.findByNameIgnoreCaseIncludingDeleted(tagName)
    .map(existingTag -> {
        if (existingTag.isDeleted()) {
            existingTag.setDeleted(false);  // Soft Delete 복원
            existingTag.setUsageCount(0);
            return tagRepository.save(existingTag);
        }
        return existingTag;  // 활성 태그 재사용
    })
    .orElseGet(() -> {
        // Hard Delete 또는 미존재 → 새로 생성
        Tag newTag = new Tag();
        newTag.setName(tagName);
        return tagRepository.save(newTag);
    });
```

- 3가지 시나리오 모두 안전:
  - 활성 태그 존재 → 그대로 사용
  - Soft Delete 상태 → 복원 (UPDATE, UNIQUE 위반 없음)
  - Hard Delete 완료 → 레코드 없으므로 새 INSERT (UNIQUE 위반 없음)

---

### ISSUE-02: syncTags() - usageCount 정합성 drift

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L230-L244)

`increaseUsage` / `decreaseUsage` 쿼리 자체는 `SET usageCount = usageCount + 1` 형태로 DB 레벨에서 atomic하므로 동시성 race condition은 아닙니다. 실제 문제는 **usageCount가 실제 참조 수와 장기적으로 어긋나는 정합성 drift**입니다.

- 심각도: MEDIUM
- 근본 원인: ISSUE-06(Board soft delete 시 연관 BoardTag 미처리)으로 인해 삭제된 게시글의 태그 usageCount가 감소되지 않음. 시간이 지남에 따라 실제 참조 수와 표시 값의 차이가 누적됨.
- 개선안:
  1. ISSUE-06 수정 시 함께 해결: Board soft delete 시 연관 BoardTag를 함께 soft delete하면서 각 Tag의 usageCount도 감소 처리.
  2. 방어적 보완: 관리자용 usageCount 재계산 유틸리티 추가 (운영 중 drift 발생 시 수동 보정용).

```java
// 관리자용 재계산 메서드 (TagService에 추가)
public void recalculateUsageCount(Long tagId) {
    long actualCount = boardTagRepository.countByTag_IdAndDeletedIsFalse(tagId);
    tagRepository.updateUsageCount(tagId, actualCount);
}
```

---

### ~~ISSUE-03: syncUploads() - 기존 첨부파일 해제 로직 부재~~ (NOT AN ISSUE)

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L248-L260)

프론트엔드(`board-update.component.ts` L238-L248)에서 첨부파일 삭제 시 즉시 `DELETE /api/uploads/{id}`를 호출하여 서버에서 soft delete 처리합니다. 에디터 내 이미지도 저장 직전 `processSoftDeletes()`(L145-L160)에서 별도 처리됩니다. syncUploads는 "새 파일 연결"만 담당하도록 의도된 설계이며, 역할 분리가 정상적으로 되어 있습니다.

- 심각도: NONE (설계 의도 확인됨)

---

### ISSUE-04: save() - 생성 시 deleted=true 전달 가능

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L103) → [BoardMapper.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/mapper/BoardMapper.java#L60)

`BoardMapper.toEntity()`가 DTO의 `deleted` 필드를 그대로 매핑합니다. UI에서는 발생하지 않으나 API 직접 호출 시 `deleted=true`를 넘기면 생성 즉시 삭제 상태가 됩니다. `save()`는 사용자 전용 함수이므로 서비스 레벨에서 방어하는 것이 적합합니다.

- 심각도: LOW
- 개선안: `BoardService.save()` 진입 시 `dto.setDeleted(false)` 강제 적용. 삭제는 전용 `delete()` API를 통해서만 처리.

---

### ISSUE-10: BoardService vs BoardTagService - 도메인 전문가 분리 (Separation of Domain Experts)

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L197-L245)

현재 `BoardService.syncTags()`는 게시글 업무와 태그 관리 업무가 혼재되어 있습니다. "게시글은 게시글 업무에만 집중하고, 태그 관계의 성격(UNIQUE 제약, usageCount, 캐시 전략)은 해당 도메인 전문가인 `BoardTagService`가 전담"하도록 정리하는 것이 아키텍처적으로 가장 깔끔합니다.

- 심각도: MEDIUM
- 결정사항: **전문화된 서비스 분리 (Expert Delegation)**.
    - `BoardTagService.syncTags(Board board, List<String> tagNames)`를 신설하여 동기화 로직을 완전히 캡슐화함.
    - `BoardService`는 이 전문가(BoardTagService)를 호출만 하도록 격리하여 로직 중복을 제거함.
    - 이 과정에서 `ISSUE-01`(UNIQUE 위반), `ISSUE-11`(캐시 이름 불일치), `ISSUE-31`(FK 위반) 문제를 `BoardTagService` 내부에서 모두 통합 해결함.

---

### ISSUE-05: update() - boardType 변경 시 불필요한 전체 mapper 호출

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L177-L179)

boardType 코드만 필요한데 `boardMapper.toEntity(dto)` 전체를 호출하여 Board 인스턴스 생성 + CommonCodeDetail DB 조회가 중복 발생합니다.

- 심각도: LOW
- 개선안: `CommonCodeDetailRepository`로 boardType 코드를 직접 조회하여 설정.

```java
// 개선: boardType 코드로 직접 조회
CommonCodeDetail newBoardType = commonCodeDetailRepository
    .findByGroupGroupCodeAndCode("BOARD_TYPE", dto.getBoardType())
    .orElseThrow(() -> new BadRequestAlertException("Invalid board type", "board", "boardType.invalid"));
existingBoard.setBoardType(newBoardType);
```

---

### ISSUE-06: delete() - 연관 엔티티(BoardTag, Upload, Comment) 연쇄 Soft Delete 미처리

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L264-L273)

게시글 Soft Delete 시 연관 엔티티에 대한 연쇄 처리가 없습니다. 현재 관리자 리소스 관리 페이지(`OrphanResourceAdminResource`)에는 댓글, 첨부파일, 태그만 있고 게시판 자체의 관리 메뉴는 없습니다. 향후 게시판 관리 메뉴를 추가하고 hard delete를 제공하려면 아래 연쇄 로직이 반드시 선행되어야 합니다.

영향:
- Tag.usageCount가 실제보다 높게 유지 (ISSUE-02의 근본 원인)
- 첨부파일이 고아 파일로 식별되지 않음
- Comment 직접 조회 API에서 삭제된 게시글의 댓글이 노출 가능

- 심각도: HIGH
- 개선안: Board soft delete 시 연쇄 처리 추가:
  1. `boardTagRepository.softDeleteAllByBoardId(boardId)` + 각 Tag의 usageCount 감소
  2. 해당 Board의 Upload 전체 soft delete
  3. 해당 Board의 Comment 전체 soft delete

---

### ISSUE-07: restore() - 연관 엔티티 복구 미처리

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L401-L410)

ISSUE-06에서 연쇄 soft delete를 구현하면, 복구(restore) 시에도 연쇄 복구가 필요합니다. BoardTag 복원 + Tag usageCount 증가 + Upload/Comment 복원을 포함해야 합니다.

- 심각도: MEDIUM (ISSUE-06 구현 시 함께 처리)

---

### ISSUE-08: hardDelete() - 연관 엔티티 미삭제로 FK 제약 위반

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L418-L425)

Board에 `CascadeType`이 설정되어 있지 않으므로, 연관 BoardTag/Upload/Comment가 존재하면 FK 위반으로 예외가 발생합니다. 현재 관리자 리소스 관리에 게시판 메뉴가 없어 호출 경로가 제한적이나, 향후 추가 시 반드시 해결 필요합니다.

- 심각도: CRITICAL
- 개선안: Board hard delete 전 연관 엔티티를 순서대로 물리 삭제:
  1. Comment 물리 삭제
  2. BoardTag 물리 삭제 + Tag usageCount 감소
  3. Upload 물리 삭제 (로컬/S3 파일 포함)
  4. Board 물리 삭제

---

### ISSUE-09: BoardMapper.toDto() - boardTags Soft Delete 필터 방어 코드 누락

File: [BoardMapper.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/mapper/BoardMapper.java#L43-L47)

`attachments`에는 명시적 `filter(u -> !u.isDeleted())`가 있으나 `boardTags`에는 없습니다. `@Filter`가 특정 시점에 적용되지 않으면 삭제된 태그가 노출될 수 있습니다.

- 심각도: LOW
- 개선안: boardTags에도 동일한 방어 코드 추가

---

---

### ~~ISSUE-12: syncUploads의 빈 리스트 처리 비대칭~~ (NOT AN ISSUE)

syncTags에 빈 리스트 전달 → 모든 기존 태그 삭제
syncUploads에 빈 리스트 전달 → 아무 작업도 하지 않음 (기존 유지)

ISSUE-03과 동일한 이유로 문제가 아닙니다. 프론트엔드에서 첨부파일 삭제 시 `removeUpload()`로 즉시 `DELETE /api/uploads/{id}`를 호출하여 서버에서 처리 완료됩니다. 저장 시점에 uploads가 빈 리스트여도 이미 개별 삭제가 완료된 상태이므로 syncUploads에서 별도 처리가 불필요합니다.

- 심각도: NONE (설계 의도 확인됨, ISSUE-03 참조)

---

## Part B: User / Authority Domain Issues

### ISSUE-13: User 삭제 시 연관 Board/Comment 미처리

File: [UserService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/UserService.java#L213-L221)

`User` 엔티티는 `Board`나 `Comment`에 대해 양방향 연관관계가 없으며, `UserService.deleteUser()`는 물리 삭제를 수행합니다. 해당 사용자가 작성한 게시글이나 댓글이 남아있는 경우 DB FK 제약 위반(integrity constraint violated)으로 삭제가 실패합니다.

- 심각도: CRITICAL
- 개선안: 탈퇴 시 물리 삭제 대신 `enabled = false` 처리(Soft Delete)하고, 게시글/댓글은 '알 수 없는 사용자'로 치환하거나 연쇄 삭제 처리 필요.

---

### ISSUE-14: User 엔티티에 Soft Delete 체계 부재

`User.java` 확인 결과, 다른 도메인 엔티티들과 달리 `is_deleted` 컬럼이나 Hibernate `@Filter`가 적용되어 있지 않습니다. 프로젝트 전반의 Soft Delete 정책에서 유일하게 예외로 남아 있어 데이터 일관성 관리가 어렵습니다.

- 심각도: MEDIUM
- 개선안: `User` 엔티티에도 `deleted` 필드 추가 및 `@Filter` 적용.

---

### ISSUE-15: User-Authority ManyToMany에 cascade 미설정

File: [User.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/User.java#L127-L135)

`@ManyToMany`에 cascade가 없으며, 이는 `Authority`가 시스템 전체 공유 데이터임을 고려할 때 적절합니다. 다만, `User` 물리 삭제 시 `stack_user_authority` 중간 테이블 정리가 누락될 경우 데이터 고립(Orphan)이 발생할 수 있으나, JPA 기본 동작에 의해 중간 테이블은 자동 정리됩니다.

- 심각도: LOW
- 확인 결과: 설계상 의도된 사항이나, 물리 삭제 정책 유지 시 주의 필요.

---

## Part C: CommonCode Domain Issues

### ISSUE-16: CommonCodeGroup Soft Delete 시 하위 Detail 미처리

File: [CommonCodeService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/common/CommonCodeService.java#L148-L161)

`softDeleteGroup()` 수행 시 `group.setDeleted(true)`만 처리되고 하위 `CommonCodeDetail`들은 여전히 `deleted = false` 상태로 남습니다. `findAllDetailsByGroup()` 등에서 그룹의 삭제 여부를 체크하지 않으므로 논리적 모순이 발생합니다.

- 심각도: HIGH
- 개선안: 그룹 삭제 시 소속 `CommonCodeDetail`들을 일괄 `deleted = true` 처리하는 로직 추가.

---

### ISSUE-17: CommonCodeGroup에 @FilterDef 누락

`CommonCodeGroup.java` 확인 결과, Soft Delete 기능을 사용하면서도 Hibernate `@Filter`가 정의되어 있지 않습니다. 이로 인해 Repository에서 모든 쿼리에 `deletedIsFalse` 조건을 수동으로 추가해야 하며, 실수로 누락 시 삭제된 코드가 노출될 위험이 큽니다.

- 심각도: LOW
- 개선안: `CommonCodeGroup` 및 `CommonCodeDetail`에도 `@Filter` 체계 적용.

---

### ISSUE-18: CommonCodeDetail 수정 시 code 변경 허용 - 참조 무결성 위험

File: [CommonCodeService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/common/CommonCodeService.java#L202)

`updateDetail()`에서 `existingDetail.setCode(updatedDetail.getCode())`를 허용합니다. `Board.boardType` 등 타 엔티티가 이 `code` 스트링을 직접 참조하고 있으므로, 코드 변경 시 기존 데이터와의 연결이 끊어지는 심각한 결함이 발생합니다.

- 심각도: HIGH
- 개선안: 코드(Unique Key) 변경을 금지하거나, 변경 시 참조 중인 모든 데이터에 대한 Update 연쇄 처리(Denormalization 대응) 필요.

---

### ISSUE-19: CommonCodeService 캐시 클리어에서 NPE 가능성

File: [CommonCodeService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/common/CommonCodeService.java#L57)

`Objects.requireNonNull(cacheManager.getCache(...))`를 사용하여 캐시 존재 여부를 확인합니다. 특정 환경에서 캐시 설정이 누락되거나 동적으로 제거될 경우 서비스 전체가 NPE로 중단될 수 있습니다.

- 심각도: MEDIUM
- 개선안: `Optional.ofNullable`을 사용하여 안전하게 무시하거나 로그만 남기도록 수정.

---

## Part D: Settings / GlobalSettings Issues

### ISSUE-20: Settings 엔티티에서 System.err 사용

File: [Settings.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/Settings.java#L52-L53)

JSON 파싱 실패 시 `System.err.println` 및 `e.printStackTrace()`를 사용합니다. 이는 운영 환경에서 로그 수집 시스템(ELK 등)에 기록되지 않거나 정적 분석 도구에 의해 결함으로 식별될 수 있습니다.

- 심각도: MEDIUM
- 개선안: 서비스 레이어에서 로깅 라이브러리(SLF4J)를 사용하도록 리팩토링.

---

### ISSUE-21: Settings.getSettingsMap() 에러 시 빈 Map 반환 - 데이터 유실 위험

File: [Settings.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/Settings.java#L54-L55)

파싱 실패 시 `new HashMap<>()`을 반환합니다. 만약 이후 특정 필드만 수정(`put`)하고 `updateGlobalSettings()`를 호출하면, 기존의 정상적이었던 나머지 모든 설정 데이터가 증발하고 빈 맵에 추가된 1건만 저장되는 대형 사고가 발생할 수 있습니다.

- 심각도: HIGH (Critical Data Loss Risk)
- 개선안: 파싱 실패 시 예외를 던져 후속 업데이트가 발생하지 않도록 차단.

---

### ISSUE-22: Settings.updateGlobalSettings() 에러 무시 (Silent Failure)

File: [Settings.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/Settings.java#L64-L67)

직렬화 시 발생하는 예외를 `catch` 블록 내에서 주석으로만 처리하고 실제 아무런 조치를 취하지 않습니다. 설정 저장이 실패했음에도 사용자는 성공한 것으로 오인하게 됩니다.

- 심각도: HIGH
- 개선안: RuntimeException으로 래핑하여 상위 트랜잭션 롤백 유도.

---

## Part E: Comment Domain Issues

### ISSUE-23: CommentService.findAllByBoard() - 이중 Soft Delete 필터링

File: [CommentService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/CommentService.java#L123-L126)

Hibernate `@Filter`가 적용되어 있어 Repository 조회 결과에 이미 삭제된 댓글이 없는데도, `stream().filter(c -> !c.isDeleted())`를 중복 호출하고 있습니다. 기능상 문제는 없으나 불필요한 연산입니다.

- 심각도: LOW
- 확인 결과: 성능 영향은 미미하나 코드 간결성을 위해 제거 권장.

---

### ISSUE-24: Comment.toString() - content null 시 NPE 위험

File: [Comment.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/domain/board/Comment.java#L86)

`toString()`에서 `content`의 `substring`을 바로 호출합니다. 필드에 `@NotNull`이 있어도 Hibernate 프록시 초기화 전이나 단위 테스트 등 특수한 경우 NPE가 발생하여 디버깅 로그 확인 자체를 방해할 수 있습니다.

- 심각도: LOW
- 개선안: null 체크 가드 절 추가.

---

## Part F: EmailOtpLog Domain Issues

### ISSUE-25: EmailOtpLog - equals/hashCode/toString 미구현

`EmailOtpLog.java` 확인 결과, JPA 엔티티임에도 불구하고 기본 메서드들이 구현되어 있지 않습니다. 이로 인해 `Set` 컬렉션 사용 시 중복 필터링이 작동하지 않거나 객체 비교 시 의도치 않은 결과가 발생할 수 있습니다.

- 심각도: LOW
- 개선안: 타 엔티티와 동일한 JPA ID 기반 `equals`/`hashCode` 구현.

---

### ISSUE-26: EmailOtpLog - AbstractAuditingEntity 미상속

다른 모든 도메인 엔티티와 달리 `AbstractAuditingEntity` 대신 `Serializable`만 직접 구현하고 있습니다. 이로 인해 생성자/수정자 정보 추적이 자동화되지 않으며 개별 필드로 관리해야 하는 번거로움이 있습니다.

- 심각도: LOW
- 분석: 단순 로그성 엔티티이나, 프로젝트 아키텍처 일관성 준수를 위해 상속 모델로 전환 권장.

---

## Part G: Cross-cutting Issues

### ISSUE-27: 캐시 null-safety 패턴 불일치 (프로젝트 전반)

`CommonCodeService`, `BoardTagService` 등은 `Objects.requireNonNull`을 사용하여 캐시 존재를 강제하는 반면, `BoardService` 등은 `Optional.ofNullable`을 사용하여 완곡하게 처리합니다. 인프라 설정에 따른 런타임 안정성 편차가 존재합니다.

- 심각도: MEDIUM
- 개선안: 프로젝트 표준 가이드라인 수립 및 일원화.

---

### ISSUE-28: Soft Delete 패턴 불일치 (Filter vs Manual Query)

도메인별로 Hibernate `@Filter` 기반 자동 필터링과 Repository의 `deletedIsFalse` 수동 필터링이 혼재되어 있습니다. 개발자가 새로운 기능을 추가할 때 어떤 패턴을 따라야 할지 혼란을 줄 수 있으며, 필터 적용 누락 시 보안 사고로 이어질 수 있습니다.

- 심각도: LOW
- 개선안: Hibernate `@Filter` 방식을 전역 표준으로 확립.

---

## Part H: Today's Agent-Log Work Related Issues (2026-03-25)

---

### ISSUE-32: Orphan Upload 조회 쿼리의 시간 조건 잠재적 누락

File: [UploadRepository.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/repository/board/UploadRepository.java#L134)

```sql
SELECT u FROM Upload u WHERE (u.board IS NULL OR u.deleted = true) AND u.lastModifiedDate <= :threshold
```

이 쿼리는 `@IncludeDeleted` 스코프에서 호출되어야 합니다. 그러나 `uploadService.getOrphanUploads()`에는 `@IncludeDeleted` 어노테이션이 정상 적용되어 있습니다.

잠재적 문제: `u.deleted = true` 조건은 Hibernate Filter가 비활성화된 상태에서만 의미가 있습니다. `@IncludeDeleted`가 누락되면 `softDeleteFilter`에 의해 `deleted = true`인 레코드가 자동 제외되어, Soft Delete된 파일이 조회 결과에서 빠지게 됩니다. 현재는 정상이나, `@IncludeDeleted` 어노테이션의 의존성이 암묵적입니다.

- 심각도: LOW (현재 정상 동작)
- 개선안: Repository 메서드 Javadoc에 `@IncludeDeleted` 필수 사용 주석 보강 (이미 일부 적용됨)

---

### ISSUE-33: OrphanResourceAdminResource - 24시간 임계값 하드코딩

File: [OrphanResourceAdminResource.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/web/rest/OrphanResourceAdminResource.java#L45-L47)

```java
private Instant getThreshold() {
    return Instant.now().minus(24, ChronoUnit.HOURS);
}
```

24시간이라는 임계값은 운영 요구에 따라 변경 가능해야 합니다. 현재 하드코딩되어 있어 변경 시 재배포가 필요합니다.

- 심각도: LOW
- 개선안: `ApplicationProperties` 또는 `GlobalSettings`에서 외부 설정으로 관리. 또는 API 파라미터로 관리자가 지정할 수 있도록 `@RequestParam(defaultValue = "24") int hours` 추가.

---

### ISSUE-34: OrphanResourceAdminResource - DELETE 요청 시 입력 검증 부재

File: [OrphanResourceAdminResource.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/web/rest/OrphanResourceAdminResource.java#L56-L62)

```java
@DeleteMapping("/uploads")
public ResponseEntity<Map<String, Integer>> deleteUploads(@RequestBody List<Long> ids) {
    int count = uploadService.hardDelete(ids);
    return ResponseEntity.ok(Map.of("deletedCount", count));
}
```

전달된 `ids`가 실제 고아 리소스인지 검증하지 않습니다. 관리자가 임의의 ID를 전달하면 활성 상태의 Upload/Tag/Comment가 삭제될 수 있습니다. `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` 보호가 있으나, 관리자의 실수나 API 직접 호출에 의한 오삭제 위험이 있습니다.

- 심각도: MEDIUM
- 개선안: 서비스 계층에서 삭제 대상이 실제 고아 상태인지 재검증하는 방어 로직 추가. 또는 GET으로 조회된 ID만 삭제 가능하도록 세션/토큰 기반 제어.

---

### ISSUE-35: Orphan Comment 삭제 시 대댓글 관계 미고려

File: [CommentService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/CommentService.java#L318-L324)

`commentService.hardDelete(List<Long> ids)`는 `commentRepository.deleteByIds(ids)` (JPQL: `DELETE FROM Comment c WHERE c.id IN :ids`)를 실행합니다. Comment 엔티티에 `replyCount` 필드가 있으므로 대댓글 구조가 계획 또는 구현 중인 것으로 보입니다.

현재 Comment 엔티티에 자기 참조(parent/child 관계)가 설정되어 있지 않아 FK 위반은 발생하지 않지만, 향후 대댓글 기능 구현 시 물리 삭제가 연쇄 문제를 일으킬 수 있습니다.

- 심각도: LOW (현재 자기참조 FK 없음)
- 기록 목적: 대댓글 기능 구현 시 반드시 재검토 필요

---

### ISSUE-36: BoardService.save() 내 syncTags/syncUploads 실패 시 부분 저장 문제

File: [BoardService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/board/BoardService.java#L87-L116)

```java
Board saved = boardRepository.save(board);       // Board 저장 성공
syncTags(saved, dto.getTags());                   // 태그 동기화 (여기서 실패하면?)
syncUploads(saved, dto.getUploads());              // Upload 동기화
```

`syncTags`에서 예외가 발생하면(예: UNIQUE 위반) 전체 트랜잭션이 롤백되므로 Board 저장도 취소됩니다. 이는 `@Transactional` 기본 동작으로 올바르지만, `syncTags` 내부에서 `tagRepository.findByNameIgnoreCase()`가 반환하지 못하고 새 Tag 생성 시 DB 에러가 발생하면, 사용자에게는 비직관적인 에러 메시지("Unique constraint violation")가 노출될 수 있습니다.

- 심각도: LOW (트랜잭션 자체는 안전)
- 개선안: syncTags 내에서 UNIQUE 위반 가능성을 사전에 체크하여 사용자 친화적 에러 메시지 제공 (ISSUE-01 수정 시 함께 해결 가능)

---

## 3. Summary Table

| ID | Domain | 위치 | 심각도 | 설명 |
|----|--------|------|--------|------|
| 01 | Board | syncTags | HIGH | Soft Delete된 Tag UNIQUE 제약 위반 |
| 02 | Board | syncTags | MEDIUM | usageCount 동시성 문제 |
| 03 | Board | syncUploads | HIGH | 첨부파일 해제 로직 부재 |
| 04 | Board | save | LOW | 생성 시 deleted=true 가능 |
| 05 | Board | update | LOW | boardType 변경 시 비효율적 mapper 호출 |
| 06 | Board | delete | HIGH | 연관 엔티티 연쇄 Soft Delete 미처리 |
| 07 | Board | restore | MEDIUM | 연관 엔티티 연쇄 복구 미처리 |
| 08 | Board | hardDelete | CRITICAL | FK 제약 위반 가능 |
| 09 | Board | BoardMapper | LOW | boardTags Soft Delete 방어 코드 누락 |
| 10 | Board | 전체 | MEDIUM | 태그 관리 로직 중복 |
| 11 | Board | 캐시 | HIGH | 캐시 이름 상수 불일치 |
| 12 | Board | syncUploads | MEDIUM | 빈 리스트 처리 비대칭 |
| 13 | User | deleteUser | CRITICAL | Board/Comment FK 미처리로 삭제 실패 |
| 14 | User | 전체 | MEDIUM | Soft Delete 체계 부재 |
| 15 | User | Authority | LOW | cascade 미설정 (의도적) |
| 16 | Common | softDeleteGroup | HIGH | 하위 Detail 연쇄 삭제 미처리 |
| 17 | Common | 엔티티 | LOW | @FilterDef 패턴 미적용 (의도적) |
| 18 | Common | updateDetail | HIGH | code 변경으로 참조 무결성 파괴 가능 |
| 19 | Common | 캐시 | MEDIUM | NPE 가능성 (requireNonNull) |
| 20 | Settings | 엔티티 | MEDIUM | System.err 사용 |
| 21 | Settings | getSettingsMap | HIGH | 파싱 실패 시 데이터 유실 위험 |
| 22 | Settings | updateSettings | HIGH | 직렬화 실패 사일런트 무시 |
| 23 | Comment | findAllByBoard | LOW | 이중 Soft Delete 필터링 |
| 24 | Comment | toString | LOW | content null 시 NPE |
| 25 | OTP | EmailOtpLog | LOW | equals/hashCode/toString 미구현 |
| 26 | OTP | EmailOtpLog | LOW | AbstractAuditingEntity 미상속 |
| 27 | 전체 | 캐시 | MEDIUM | null-safety 패턴 불일치 |
| 28 | 전체 | 엔티티 | LOW | Soft Delete 패턴 불일치 |
| 29 | Board | update | LOW | findByIdWithDetails 이중 호출 성능 비용 |
| 30 | Board | syncTags | MEDIUM | @Modifying 후 1차 캐시 정합성 불안정 |
| 31 | Orphan | Tag hardDelete | MEDIUM | SoftDeleted BoardTag로 인한 FK 위반 가능 |
| 32 | Orphan | Upload 조회 | LOW | @IncludeDeleted 의존성 암묵적 |
| 33 | Orphan | 임계값 | LOW | 24시간 하드코딩 |
| 34 | Orphan | DELETE API | MEDIUM | 삭제 대상 고아 상태 미검증 |
| 35 | Orphan | Comment 삭제 | LOW | 대댓글 구현 시 재검토 필요 |
| 36 | Board | save/syncTags | LOW | 에러 메시지 비직관적 (ISSUE-01 연관) |
| 37 | Storage | purge | MEDIUM | 일괄 삭제 성능 리스크 (Batching 부재) |
| 38 | Storage | Cloud | HIGH | 클라우드 환경 공개 전환 미지원 |
| 39 | Storage | 트랜잭션 | HIGH | 파일-DB 트랜잭션 불일치 (Dangling) |
| 40 | Storage | validation | MEDIUM | Settings 장애 시 업로드 차단 위험 |
| 41 | User | delete | CRITICAL | EmailOtpLog FK 제약 위반 |
| 42 | User | delete | MEDIUM | 사용자 삭제 시 물리 파일 정리 누락 |
| 43 | User | login 변경 | MEDIUM | 아이디 변경 시 기존 캐시 누설 |
| 44 | User | register | LOW | 비활성 계정 자동 삭제 로직의 공격 노출 |
| 45 | Common | 캐시명 | LOW | 캐시 네이밍 규칙 불일치 |
| 46 | 전체 | 캐시 정합성 | HIGH | 트랜잭션 레이스(AfterCommit 부재) |
| 47 | 전체 | Audit | MEDIUM | 보안 이벤트 DB 로깅 체계 부재 |
| 48 | 전체 | 검색 | HIGH | @Lob 필드 LIKE 검색 성능 저하 |
| 49 | Comment | N+1 | MEDIUM | 대댓글 계층 구조 조회 시 N+1 가능성 |
| 50 | Comment | 설계 | MEDIUM | replyCount 필드 방치 (기능 미구현) |
| 51 | 전체 | 비정규화 | LOW | 도메인별 집계 필드 관리 불일치 |

---

## 4. Recommended Priority Order

1. ISSUE-08, ISSUE-13, ISSUE-41 (CRITICAL): FK 제약 위반 런타임 예외
2. ISSUE-21, ISSUE-22 (HIGH): Settings 데이터 유실 직결
3. ISSUE-38, ISSUE-39 (HIGH): 스토리지 정합성 및 클라우드 호환성
4. ISSUE-11, ISSUE-46 (HIGH): 캐시 오염 및 이름 불일치
5. ISSUE-48 (HIGH): 검색 성능 병목 (Full Table Scan)
6. ISSUE-01, ISSUE-16, ISSUE-18 (HIGH): 비즈니스 무결성
7. ISSUE-03, ISSUE-06, ISSUE-10 (HIGH/MEDIUM): 게시글 수정/삭제 로직 파편화
8. ISSUE-19, ISSUE-27, ISSUE-40 (MEDIUM): 시스템 안정성 (NPE/장애 전파)
9. ISSUE-43, ISSUE-47, ISSUE-50 (MEDIUM): 보안 및 설계 부채
10. 나머지 LOW 이슈: 일관성 및 가독성 개선
