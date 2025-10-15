package com.daangcool.stack.web.rest.admin;

import com.daangcool.stack.domain.EmailOtpLog;
import com.daangcool.stack.repository.EmailOtpLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * REST controller for managing OTP logs (admin only).
 *
 * <p>관리자 전용 OTP 로그 조회 API</p>
 * <ul>
 *   <li>이메일, 상태, 날짜 범위 필터링</li>
 *   <li>페이징 지원</li>
 * </ul>
 *
 * @author Steve
 * @since 2025-10-15
 */
@RestController
@RequestMapping("/api/admin/otp-logs")
@Tag(name = "Admin OTP Log Management", description = "관리자 전용 OTP 로그 조회 API")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class EmailOtpLogResource {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpLogResource.class);
    private final EmailOtpLogRepository emailOtpLogRepository;

    public EmailOtpLogResource(EmailOtpLogRepository emailOtpLogRepository) {
        this.emailOtpLogRepository = emailOtpLogRepository;
    }

    /**
     * 관리자 OTP 로그 조회
     *
     * @param email 이메일 (선택)
     * @param status 상태 (선택)
     * @param from 시작일 (선택)
     * @param to 종료일 (선택)
     * @param pageable 페이징 정보
     * @return OTP 로그 목록
     */
    @Operation(summary = "OTP 로그 조회", description = "이메일, 상태, 날짜 범위로 필터링된 OTP 로그를 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<EmailOtpLog>> getOtpLogs(
        @Parameter(description = "검색할 이메일 주소", example = "user@example.com")
        @RequestParam(required = false) String email,
        @Parameter(description = "상태 (예: SENT, VERIFIED, FAILED, LOCKED)")
        @RequestParam(required = false) String status,
        @Parameter(description = "조회 시작일 (ISO 형식)", example = "2025-10-01T00:00:00Z")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @Parameter(description = "조회 종료일 (ISO 형식)", example = "2025-10-31T23:59:59Z")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        Pageable pageable
    ) {
        log.debug("[ADMIN] OTP 로그 조회: email={}, status={}, from={}, to={}", email, status, from, to);

        // 기본 쿼리 (단순 필터링)
        List<EmailOtpLog> logs = emailOtpLogRepository.findAll();
        Stream<EmailOtpLog> stream = logs.stream();

        if (email != null && !email.isBlank()) {
            stream = stream.filter(l -> email.equalsIgnoreCase(l.getEmail()));
        }
        if (status != null && !status.isBlank()) {
            stream = stream.filter(l -> status.equalsIgnoreCase(l.getStatus()));
        }
        if (from != null) {
            stream = stream.filter(l -> !l.getCreatedDate().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(l -> !l.getCreatedDate().isAfter(to));
        }

        List<EmailOtpLog> filtered = stream.toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        Page<EmailOtpLog> page = new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());

        return ResponseEntity.ok(page);
    }
}
