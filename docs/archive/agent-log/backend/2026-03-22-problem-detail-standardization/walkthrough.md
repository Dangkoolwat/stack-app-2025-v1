# 실행 흐름 (Walkthrough)

1. 클라이언트(프론트엔드/모바일)가 REST API 통신 중, 리소스를 찾지 못하거나(`EntityNotFoundException`) 파라미터 값의 유효성이 깨진 상태(`ConstraintViolationException`)로 백엔드를 찌름.
2. 예외가 쓰레드를 타고 올라와 `@RestControllerAdvice` 가 지정된 `ExceptionTranslator` 객체에 낚임.
3. Spring 4 / Boot의 네이티브 클래스인 `ProblemDetail`를 통해 RFC 7807 호환 오류 JSON 객체가 빚어짐.
   - `404 Not Found` 의 경우 `Resource Not Found`와 시간값이 세팅됨.
   - `400 Bad Request` 의 경우 `violations` 란 이름의 확장 배열 프로퍼티 아래, `field: "password", message: "length must be..."` 처럼 상세 내역이 세팅됨.
4. 프론트엔드 Axios Interceptor 등에서 응답을 수신하고 `error.response.data.violations` 를 통해 각 인풋 박스(UI) 하단에 붉은색 경고 텍스트를 파싱하여 정확하게 뿌려줄 수 있게 됨.
