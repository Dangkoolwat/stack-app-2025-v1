package com.daangcool.stack.web.rest.errors;

import com.daangcool.stack.web.exception.BadRequestAlertException;
import com.daangcool.stack.web.exception.EmailAlreadyUsedException;
import com.daangcool.stack.web.exception.InvalidPasswordException;
import com.daangcool.stack.web.exception.LoginAlreadyUsedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class ExceptionTranslator extends ResponseEntityExceptionHandler {


    private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslator.class);

    /**
     * @Valid 검증 실패 처리 (400 Bad Request)
     * - 필드 오류 리스트를 properties.errors 로 내려줌
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();

        // 공통 ProblemDetail 생성
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            "https://stack-app.com/probs/validation-error",
            "Validation Error",
            "One or more fields are invalid",
            servletRequest
        );

        // FieldError → DTO 변환 후 확장필드에 추가
        List<FieldErrorVM> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(f -> new FieldErrorVM(
                f.getObjectName().replaceFirst("DTO$", ""),
                f.getField(),
                f.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        problem.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * JSON 파싱 오류 처리 (400 Bad Request)
     * - ex.getMostSpecificCause().getMessage() 로 detail 제공
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();

        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            "https://stack-app.com/probs/bad-request",
            "Invalid Request",
            ex.getMostSpecificCause().getMessage(),
            servletRequest
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 커스텀 예외: 이메일 중복
     */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Object> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            "https://stack-app.com/probs/email-used",
            "Email Already Used",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 커스텀 예외: 비밀번호 유효성 실패
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            "https://stack-app.com/probs/invalid-password",
            "Invalid Password",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 커스텀 예외: 로그인 중복
     */
    @ExceptionHandler(LoginAlreadyUsedException.class)
    public ResponseEntity<Object> handleLoginAlreadyUsed(LoginAlreadyUsedException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            "https://stack-app.com/probs/login-used",
            "Login Already Used",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 커스텀 예외: BadRequestAlertException
     * - 이 예외는 내부적으로 ProblemDetail 변환 지원
     */
    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<Object> handleBadRequestAlert(BadRequestAlertException ex, HttpServletRequest request) {
        var problem = ex.toProblemDetail(request.getRequestURI());
        // 공통 확장 필드 추가
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 그 외 모든 예외 처리 (500 Internal Server Error)
     * - 운영환경에서는 민감한 메시지 숨김
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        LOG.error("Unexpected error", ex);

        String safeDetail = ex.getMessage();
        if (safeDetail != null && safeDetail.contains("org.")) {
            safeDetail = "Unexpected server error";
        }

        var problem = ProblemUtils.build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "https://stack-app.com/probs/internal-error",
            "Internal Server Error",
            safeDetail,
            request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }
}
