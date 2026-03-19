package com.daangcool.stack.web.rest.errors;

import com.daangcool.stack.common.exception.*;
import com.daangcool.stack.common.util.ProblemUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.daangcool.stack.common.constant.ErrorConstants;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ExceptionTranslator
 * -----------------------------------------------------------
 * 애플리케이션 전역 예외를 RFC 7807 기반 ProblemDetail JSON으로 변환합니다.
 * - Spring Boot 3.x 표준 ProblemDetail 통합
 * - ErrorConstants URI 기반 에러 식별자
 * - ProblemUtils를 통한 i18n 메시지 및 시간대 일관성 유지
 * -----------------------------------------------------------
 */
@RestControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    /**
     * @Valid 유효성 검증 실패 (400 Bad Request)
     * - 필드별 오류 리스트를 "errors" 확장 필드로 포함
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();

        // 필드별 오류 리스트 수집
        List<FieldErrorVM> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> new FieldErrorVM(
                f.getObjectName().replaceFirst("DTO$", ""),
                f.getField(),
                f.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.CONSTRAINT_VIOLATION_TYPE.toString(),
            "problem.validationError",
            "problem.validationError.detail",
            servletRequest
        );

        problem.setProperty("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * JSON 요청 본문 파싱 실패 (400 Bad Request)
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
            ErrorConstants.BAD_REQUEST_TYPE.toString(),
            "problem.invalidJson",
            ex.getMostSpecificCause().getMessage(),
            servletRequest
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 이메일 중복 (400 Bad Request)
     */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Object> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.EMAIL_ALREADY_USED_TYPE.toString(),
            "problem.emailUsed",
            ex.getMessage(),
            request
        );
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 로그인 중복 (400 Bad Request)
     */
    @ExceptionHandler(LoginAlreadyUsedException.class)
    public ResponseEntity<Object> handleLoginAlreadyUsed(LoginAlreadyUsedException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.LOGIN_ALREADY_USED_TYPE.toString(),
            "problem.loginUsed",
            ex.getMessage(),
            request
        );
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 잘못된 비밀번호 (400 Bad Request)
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.INVALID_PASSWORD_TYPE.toString(),
            "problem.invalidPassword",
            ex.getMessage(),
            request
        );
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 엔티티를 찾을 수 없음 (404 Not Found)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.NOT_FOUND,
            ErrorConstants.ENTITY_NOT_FOUND_TYPE.toString(),
            "problem.entityNotFound",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 파일 저장/조회 관련 오류
     */
    @ExceptionHandler({ FileStorageException.class, UploadNotFoundException.class })
    public ResponseEntity<Object> handleFileExceptions(RuntimeException ex, HttpServletRequest request) {
        URI type = ex instanceof UploadNotFoundException
            ? ErrorConstants.FILE_NOT_FOUND_TYPE
            : ErrorConstants.FILE_STORAGE_ERROR_TYPE;

        var problem = ProblemUtils.build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            type.toString(),
            "problem.fileError",
            ex.getMessage(),
            request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * BadRequestAlertException (서비스/리소스 단위 유효성 실패)
     */
    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<Object> handleBadRequestAlert(BadRequestAlertException ex, HttpServletRequest request) {
        var problem = ex.toProblemDetail(request.getRequestURI());
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        problem.setProperty("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 요청 횟수 초과 (429 Too Many Requests)
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        var problem = ex.toProblemDetail(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
            .body(problem);
    }

    /**
     * 접근 권한 거부 (403 Forbidden)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(Exception ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.FORBIDDEN,
            ErrorConstants.ACCESS_DENIED_TYPE.toString(),
            "problem.accessDenied",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 인증 실패 (401 Unauthorized)
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(Exception ex, HttpServletRequest request) {
        var problem = ProblemUtils.build(
            HttpStatus.UNAUTHORIZED,
            ErrorConstants.UNAUTHORIZED_TYPE.toString(),
            "problem.unauthorized",
            "Authentication failed",
            request
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 처리되지 않은 예외 (500 Internal Server Error)
     * - 내부 스택트레이스 메시지는 감춤
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);

        String safeDetail = ex.getMessage();
        if (safeDetail != null && safeDetail.contains("org.")) {
            safeDetail = "problem.internalServerError";
        }

        var problem = ProblemUtils.build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorConstants.DEFAULT_TYPE.toString(),
            "problem.internalError",
            safeDetail,
            request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }
}
