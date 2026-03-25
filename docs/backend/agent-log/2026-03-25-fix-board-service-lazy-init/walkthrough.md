---
agent: Antigravity (GPT-4o)
created_at: 2026-03-25 (수요일)
language: ko
---

# 워크쓰루 (Walkthrough)

## 로직 상세 설명
1. **상세 로드**: `update` 메서드 시작 시 `findByIdWithDetails`를 사용하여 해당 엔티티에 엮인 `user`, `boardType`, `attachments`, `boardTags`를 한 번의 쿼리(또는 `@EntityGraph` 기반 패치)로 가져옵니다.
2. **태그 동기화**: `syncTags`는 이제 `board.getBoardTags()`라는 프록시 컬렉션을 직접 수정하지 않습니다. 대신 `boardTagRepository`를 통해 DB 상에서 `is_deleted = 1` 처리를 하거나 새로운 관계를 생성합니다.
3. **최종 갱신**: 모든 DB 변경(태그, 파일 등)이 끝난 후, `boardRepository.findByIdWithDetails(id)`를 한 번 더 호출합니다. 이 시점에는 이전에 처리한 DB 변경 사항이 하이버네이트 필터(`softDeleteFilter`)에 의해 자동 필터링되어, 활성 상태의 태그들만 포함된 엔티티가 로드됩니다.
4. **결과 반환**: 정제된 엔티티를 `boardMapper`를 통해 DTO로 변환하여 사용자에게 반환합니다.

## 핵심 포인트
- 지연 로딩을 유발하는 코드를 원천 제거하여 세션 문제를 회피했습니다.
- 실시간 DB 수정 내용을 다시 읽어오는(Refetch) 전략을 통해 데이터 무결성을 보장했습니다.
