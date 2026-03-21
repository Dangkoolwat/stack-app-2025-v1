# 해결 방안 제안 (Proposal)

## 제안 방향
- **Spring Boot 4 순정(Native) ProblemDetail 반환 도입**: Spring Web/WebFlux 통합 컨텍스트에서 전폭적으로 지원하는 `ProblemDetail`을 컨트롤러나 어드바이스 반환 타입으로 선언하여 리플렉션 오버헤드를 줄이고 호환성 극대화.
- **제약 위반 핸들러(`ConstraintViolationException`) 신규 할당**: `getField()` 및 `getMessage()` 를 매핑해 JSON 응답 내 `violations` 프로퍼티 배열에 삽입하도록 구현하여 에러 명확화.

## 선택 이유 및 기대 효과
- **클린 코드와 일관성**: Spring 생태계에서 가장 권장하는 방식을 엄격하게 따르므로 불필요하게 객체 래핑(Wrap)이나 헤더를 임의 할당하는 코드가 극적으로 압축됨.
- **Front-end DX 개선**: 프론트엔드 개발 시 HTTP Status와 오류 원인을 파싱할 때 일관된 JSON 객체 파싱 로직(RFC 7807)만 수행하면 됨.
