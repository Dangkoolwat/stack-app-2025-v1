package com.daangcool.stack.web.rest.errors;

import com.daangcool.stack.common.exception.*;
import com.daangcool.stack.common.util.ProblemUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.daangcool.stack.common.constant.ErrorConstants;
import tech.jhipster.web.util.HeaderUtil;
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

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

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
    public ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        return ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.EMAIL_ALREADY_USED_TYPE.toString(),
            "problem.emailUsed",
            ex.getMessage(),
            request
        );
    }

    /**
     * 로그인 중복 (400 Bad Request)
     */
    @ExceptionHandler(LoginAlreadyUsedException.class)
    public ProblemDetail handleLoginAlreadyUsed(LoginAlreadyUsedException ex, HttpServletRequest request) {
        return ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.LOGIN_ALREADY_USED_TYPE.toString(),
            "problem.loginUsed",
            ex.getMessage(),
            request
        );
    }

    /**
     * 잘못된 비밀번호 (400 Bad Request)
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        return ProblemUtils.build(
            HttpStatus.BAD_REQUEST,
            ErrorConstants.INVALID_PASSWORD_TYPE.toString(),
            "problem.invalidPassword",
            ex.getMessage(),
            request
        );
    }

    /**
     * 엔티티를 찾을 수 없음 (404 Not Found)
     * Spring Boot 4 표준 ProblemDetail 반환
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage() != null ? ex.getMessage() : "Entity Not Found"
        );
        pd.setTitle("Resource Not Found");
        pd.setProperty("timestamp", java.time.Instant.now());
        return pd;
    }

    /**
     * JPA/Hibernate Validator 제약조건 위반 (400 Bad Request)
     * Spring Boot 4 표준 ProblemDetail 반환
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ProblemDetail handleValidation(jakarta.validation.ConstraintViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed"
        );
        pd.setProperty("violations", ex.getConstraintViolations()
            .stream()
            .map(v -> java.util.Map.of(
                "field", v.getPropertyPath().toString(),
                "message", String.valueOf(v.getMessage())
            ))
            .toList()
        );
        return pd;
    }

    /**
     * 파일 저장/조회 관련 오류
     */
    @ExceptionHandler({ FileStorageException.class, UploadNotFoundException.class })
    public ProblemDetail handleFileExceptions(RuntimeException ex, HttpServletRequest request) {
        URI type = ex instanceof UploadNotFoundException
            ? ErrorConstants.FILE_NOT_FOUND_TYPE
            : ErrorConstants.FILE_STORAGE_ERROR_TYPE;

        return ProblemUtils.build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            type.toString(),
            "problem.fileError",
            ex.getMessage(),
            request
        );
    }

    /**
     * BadRequestAlertException (서비스/리소스 단위 유효성 실패)
     * JHipster HeaderUtil 을 사용하여 프론트엔드가 오류 메시지를 표시할 수 있도록 헤더를 추가합니다.
     */
    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestAlert(BadRequestAlertException ex, HttpServletRequest request) {
        var problem = ex.toProblemDetail(request.getRequestURI());
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        problem.setProperty("path", request.getRequestURI());

        // JHipster HeaderUtil 을 사용하여 오류 헤더 생성
        // X-stackapp-error: errorKey, X-stackapp-params: entityName
        HttpHeaders headers = HeaderUtil.createFailureAlert(
            applicationName,
            true,  // enableTranslation = true (i18n 키 사용)
            ex.getEntityName(),
            ex.getErrorKey(),
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .headers(headers)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 요청 횟수 초과 (429 Too Many Requests)
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ProblemDetail> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
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
    public ProblemDetail handleAccessDenied(Exception ex, HttpServletRequest request) {
        return ProblemUtils.build(
            HttpStatus.FORBIDDEN,
            ErrorConstants.ACCESS_DENIED_TYPE.toString(),
            "problem.accessDenied",
            ex.getMessage(),
            request
        );
    }

    /**
     * 인증 실패 (401 Unauthorized)
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ProblemDetail handleAuthentication(Exception ex, HttpServletRequest request) {
        return ProblemUtils.build(
            HttpStatus.UNAUTHORIZED,
            ErrorConstants.UNAUTHORIZED_TYPE.toString(),
            "problem.unauthorized",
            "Authentication failed",
            request
        );
    }

    /**
     * 처리되지 않은 예외 (500 Internal Server Error)
     * - 내부 스택트레이스 메시지는 감춤
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);

        String safeDetail = ex.getMessage();
        if (safeDetail != null && safeDetail.contains("org.")) {
            safeDetail = "problem.internalServerError";
        }

        return ProblemUtils.build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorConstants.DEFAULT_TYPE.toString(),
            "problem.internalError",
            safeDetail,
            request
        );
    }
}
