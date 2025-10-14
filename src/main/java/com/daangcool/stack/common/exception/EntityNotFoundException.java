package com.daangcool.stack.common.exception;

import com.daangcool.stack.common.constant.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * 404 Not Found 예외 처리 클래스.
 * 요청한 엔티티가 존재하지 않거나 접근할 수 없는 경우 발생합니다.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(ErrorConstants.ENTITY_NOT_FOUND_TYPE);
        problem.setTitle("Entity Not Found");
        problem.setDetail(getMessage());
        problem.setInstance(URI.create(instance));
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        return problem;
    }
}
