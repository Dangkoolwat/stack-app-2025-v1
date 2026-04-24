---
agent: Antigravity (Gemini 2.0 Flash)
created_at: 2026-03-25 (수요일)
language: ko
---

# 구현 계획

## 1단계: 코드 수정
- `BoardService.java` 내 `update` 메서드 수정: `findById` -> `findByIdWithDetails`.
- `syncTags`, `syncUploads` 내의 수동 컬렉션 조작 로직 제거.

## 2단계: 검증용 테스트 코드 작성
- `src/test/java/com/daangcool/stack/service/board/BoardServiceIT.java` 생성.
- 태그 추가, 삭제, 교체 시나리오를 포함하여 `LazyInitializationException` 발생 여부 확인.

## 3단계: 통합 테스트 실행
- `./mvnw test -Dtest=BoardServiceIT` 명령으로 검증 수행.
- Oracle DB 및 하이버네이트 필터 환경에서의 동작 확인.
