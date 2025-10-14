package com.daangcool.stack.common.exception;

import com.daangcool.stack.common.constant.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * 파일을 찾을 수 없거나 삭제된 경우 발생하는 예외.
 * 404 Not Found 응답으로 매핑됩니다.
 */
public class UploadNotFoundException extends RuntimeException {

    public UploadNotFoundException(String message) {
        super(message);
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(ErrorConstants.FILE_NOT_FOUND_TYPE);
        problem.setTitle("File Not Found");
        problem.setDetail(getMessage());
        problem.setInstance(URI.create(instance));
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        return problem;
    }
}
