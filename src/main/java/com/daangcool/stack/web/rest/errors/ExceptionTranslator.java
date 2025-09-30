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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslator.class);

    /**
     * Validation 실패 처리 (@Valid)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://stack-app.com/probs/validation-error"));
        problem.setTitle("Validation Error");
        problem.setDetail("One or more fields are invalid");
        problem.setProperty("instance", request.getDescription(false).replace("uri=", ""));

        // FieldErrorVM 리스트 변환
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
     * JSON 파싱 오류 등
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://stack-app.com/probs/bad-request"));
        problem.setTitle("Invalid Request");
        problem.setDetail(ex.getMostSpecificCause().getMessage());
        problem.setProperty("instance", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 이메일 중복 예외 처리
     */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Object> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://stack-app.com/probs/email-used"));
        problem.setTitle("Email Already Used");
        problem.setDetail(ex.getMessage());
        problem.setProperty("instance", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 비밀번호 유효성 실패 예외 처리
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Object> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://stack-app.com/probs/invalid-password"));
        problem.setTitle("Invalid Password");
        problem.setDetail(ex.getMessage());
        problem.setProperty("instance", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * 로그인 중복 예외 처리
     */
    @ExceptionHandler(LoginAlreadyUsedException.class)
    public ResponseEntity<Object> handleLoginAlreadyUsed(LoginAlreadyUsedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://stack-app.com/probs/login-used"));
        problem.setTitle("Login Already Used");
        problem.setDetail(ex.getMessage());
        problem.setProperty("instance", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    /**
     * BadRequestAlertException 처리
     */
    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<Object> handleBadRequestAlert(
        BadRequestAlertException ex,
        HttpServletRequest request
    ) {
        ProblemDetail problem = ex.toProblemDetail(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }



    /**
     * 그 외 모든 예외 처리 (운영환경에서는 민감한 메시지 숨김)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        LOG.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://stack-app.com/probs/internal-error"));
        problem.setTitle("Internal Server Error");

        // 운영환경이라면 상세 메시지 대신 일반화
        String safeDetail = ex.getMessage();
        if (safeDetail != null && safeDetail.contains("org.")) {
            safeDetail = "Unexpected server error";
        }
        problem.setDetail(safeDetail);

        problem.setProperty("instance", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }
}
