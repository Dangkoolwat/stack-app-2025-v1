# 문제 분석 (Problem Analysis)

## 현상 및 배경
- 팀 지침에 따라 에러 응답 포맷이 최신 Spring Boot 4 표준 규격인 RFC 7807 (`ProblemDetail`)로 완전히 일원화되어야 함.
- 현재 `ExceptionTranslator.java` 에서는 `EntityNotFoundException` 발생 시 `ProblemUtils` 유틸을 사용한 `ResponseEntity<Object>` 형태의 간접 반환 구조를 가지고 있었으며, JPA 엔티티 제약조건 위반인 `ConstraintViolationException`에 대한 개별 핸들러가 누락되어 있었음.

## 재현 / 문제점
- `ConstraintViolationException`이 던져질 때 500 에러 처리기(`handleAll`)를 타버려 클라이언트가 `400 Bad Request`의 정확한 원인(Valid 위반 필드 및 사유)을 알 수 없는 문제.
- 프론트엔드나 클라이언트(Vue 3, Mobile)가 통일된 인터페이스인 `ProblemDetail`을 파싱할 때 구조 일관성이 약간 떨어질 수 있음.
