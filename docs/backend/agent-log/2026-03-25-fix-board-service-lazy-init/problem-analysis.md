---
agent: Antigravity (GPT-4o)
created_at: 2026-03-25 (수요일)
language: ko
---

# 문제 분석

## 문제 현상
게시판 글 수정 시 태그를 새로 입력하고 기존 태그를 삭제한 뒤 저장할 때 `LazyInitializationException`이 발생함.
에러 로그에 따르면 `com.daangcool.stack.domain.board.Board.boardTags` 컬렉션을 초기화할 수 없다고 나옴 (no session).

## 재현 경로
1. 게시판 글 작성 (태그 포함)
2. 글 수정 화면 진입
3. 기존 태그 삭제 및 새 태그 입력
4. 저장 버튼 클릭 -> 서버 에러 발생

## 원인 분석
- `BoardService.update` 메서드에서 게시글을 조회할 때 `findById`를 사용하여 지연 로딩(Lazy Loading) 설정된 `boardTags` 컬렉션이 초기화되지 않은 프록시 상태로 로드됨.
- `syncTags` 메서드 내에서 `board.getBoardTags().removeIf(...)` 및 `add(...)`를 호출하여 컬렉션에 접근함.
- 이때 트랜잭션 범위 내임에도 불구하고 어떤 이유로(엔티티 분리 또는 세션 상태 불안정) 하이버네이트 세션이 없다는(`no session`) 오류와 함께 초기화에 실패함.
- 특히 수동으로 메모리 상의 컬렉션을 동기화하려는 시도가 지연 로딩을 트리거하면서 문제를 일으킴.

## 영향 범위
- 게시판 글 수정 기능 전반 (태그 또는 첨부파일 변경 시 포함)
- 데이터 일관성 및 사용자 경험 저하
