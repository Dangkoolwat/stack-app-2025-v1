---
agent: Antigravity (Gemini 2.0 Flash)
created_at: 2026-03-25 (수요일)
language: ko
---

# 해결 방안 제안

## 방안 1: 초기 로딩 시 상세 정보 포함 (Eager Fetching)
- `update` 시작 시 `findById` 대신 `findByIdWithDetails`를 사용하여 모든 연관 관계를 미리 로드함.
- 장점: 컬렉션 접근 시나 DTO 변환 시 지연 로딩 오류를 원천 차단함.
- 단점: 성능상 약간의 오버헤드가 있을 수 있으나, 수정 로직에서는 상세 정보가 어차피 필요하므로 미미함.

## 방안 2: 수동 메모리 동기화 제거 및 최종 리프레시 활용 (선택된 방안)
- `syncTags` 등에서 문제가 되는 `board.getBoardTags().removeIf(...)` 코드를 제거함.
- 대신 리포지토리를 통해 DB 상의 매핑 테이블(STACK_BOARD_TAG)을 직접 수정함.
- `update` 메서드 마지막에 `findByIdWithDetails`로 엔티티를 다시 조회하여 최신 DB 상태를 DTO로 반환함.
- **선택 이유**: 세션 요류 위험을 최소화하면서 실시간 DB 상태를 가장 정확하게 반영할 수 있음.

## 리스크
- 최종 리프레시(`findByIdWithDetails`) 직전까지 메모리 상의 엔티티 객체는 DB와 불일치할 수 있으나, 해당 메서드 내에서만 사용되므로 안전함.
