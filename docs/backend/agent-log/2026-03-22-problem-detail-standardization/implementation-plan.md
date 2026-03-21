# 구현 계획 (Implementation Plan)

## 1. ExceptionTranslator.java 리팩토링
- 대상 경로: `src/main/java/com/daangcool/stack/web/rest/errors/ExceptionTranslator.java`
- `EntityNotFoundException` 핸들러의 반환형을 `ResponseEntity<Object>`에서 `ProblemDetail` 로 변경.
- `ConstraintViolationException` 핸들러 신설 (`@ExceptionHandler(jakarta.validation.ConstraintViolationException.class)`).

## 2. Spring 4 / Boot 3 API 활용
- `ProblemDetail.forStatusAndDetail(...)` 정적 팩토리를 활용.
- `ConstraintViolationException`에서 발생한 제약 조건 미달 리스트(`.getConstraintViolations()`)를 스트림으로 돌면서, 에러 발생 필드명(`field`)과 그 사유문자열(`message`)을 갖춘 커스텀 프로퍼티(`violations`) 맵핑 객체로 확장.

## 3. 코드 컴파일 점검
- Maven `compile` 명령을 돌렸을 시 구문/의존성 에러 없이 통과.
