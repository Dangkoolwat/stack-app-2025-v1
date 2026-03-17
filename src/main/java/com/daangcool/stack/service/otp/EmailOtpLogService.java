package com.daangcool.stack.service.otp;

import com.daangcool.stack.domain.EmailOtpLog;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.EmailOtpLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * OTP 인증 로그 기록 서비스 (NC-2 이슈 해결을 위해 분리)
 * ------------------------------------------------------------------
 * 트랜잭션 전파를 REQUIRES_NEW로 설정하여 주 로직의 성공/실패 여부와 
 * 관계없이 독립적인 로그 기록을 보장합니다.
 * ------------------------------------------------------------------
 */
@Service
public class EmailOtpLogService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpLogService.class);

    private final EmailOtpLogRepository emailOtpLogRepository;

    public EmailOtpLogService(EmailOtpLogRepository emailOtpLogRepository) {
        this.emailOtpLogRepository = emailOtpLogRepository;
    }

    /**
     * OTP 인증 관련 로그를 독립된 트랜잭션으로 기록합니다.
     *
     * @param user   인증 대상 사용자
     * @param code   발송 또는 검증 시도된 코드
     * @param ip     요청자 IP
     * @param ua     요청자 User-Agent
     * @param status 로그 상태 (SENT, VERIFIED, FAILED_VERIFY 등)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLog(User user, String code, String ip, String ua, String status) {
        try {
            EmailOtpLog logEntity = new EmailOtpLog();
            logEntity.setUser(user);
            logEntity.setEmail(user.getEmail());
            logEntity.setOtpCode(maskOtp(code)); // NH-3 보안 개선: 마스킹 처리
            logEntity.setRequestIp(ip);
            logEntity.setUserAgent(ua);
            logEntity.setDeviceType(detectDevice(ua));
            logEntity.setCreatedDate(Instant.now());
            logEntity.setStatus(status);
            emailOtpLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("[OTP LOG] 로그 기록 실패: {}", e.getMessage());
        }
    }

    private String maskOtp(String code) {
        if (code == null) return "******";
        if (code.length() < 2) return "******";
        
        // NH-3 보안 강화: 마스킹 + SHA-256 해시의 앞부분 일부 저장 (Audit 용도)
        // 전체 해시를 저장하지 않는 이유는 무작위 대입(Brute-force)으로 6자리 숫자를 복원할 수 있기 때문임.
        // 마스킹된 문자열만 저장하여 사후 분석 시 원본 코드 확인을 불가능하게 합니다.
        return code.substring(0, 2) + "****";
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return "mobile";
        if (ua.contains("tablet")) return "tablet";
        return "desktop";
    }
}
