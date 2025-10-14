package com.daangcool.stack.common.exception;

import com.daangcool.stack.common.constant.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * 파일 저장, 이동, 삭제 중 발생하는 서버 내부 오류.
 * 500 Internal Server Error로 매핑됩니다.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(ErrorConstants.FILE_STORAGE_ERROR_TYPE);
        problem.setTitle("File Storage Error");
        problem.setDetail(getMessage());
        problem.setInstance(URI.create(instance));
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        return problem;
    }
}
