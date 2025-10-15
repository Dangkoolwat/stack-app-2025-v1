package com.daangcool.stack.service.otp;

import com.daangcool.stack.domain.EmailOtpLog;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.EmailOtpLogRepository;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.service.GlobalSettingsService;
import com.daangcool.stack.service.MailService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 고수준 Email OTP 관리 서비스.
 * - 트랜잭션 관리
 * - 동시성 제어
 * - 로그 및 캐시 통합
 */
@Service
public class EmailOtpManager {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpManager.class);

    private final RedissonClient redissonClient;
    private final UserRepository userRepository;
    private final EmailOtpLogRepository emailOtpLogRepository;
    private final MailService mailService;
    private final GlobalSettingsService globalSettingsService;
    private final EmailOtpGenerator otpGenerator;

    public EmailOtpManager(
        RedissonClient redissonClient,
        UserRepository userRepository,
        EmailOtpLogRepository emailOtpLogRepository,
        MailService mailService,
        GlobalSettingsService globalSettingsService,
        EmailOtpGenerator otpGenerator
    ) {
        this.redissonClient = redissonClient;
        this.userRepository = userRepository;
        this.emailOtpLogRepository = emailOtpLogRepository;
        this.mailService = mailService;
        this.globalSettingsService = globalSettingsService;
        this.otpGenerator = otpGenerator;
    }

    /**
     * OTP 요청 트랜잭션.
     * <p>Redisson Lock을 사용해 동일 이메일 중복 요청 방지.</p>
     */
    @Transactional
    public void issueOtp(String email, String ip, String userAgent) {
        RLock lock = redissonClient.getLock("lock:otp:" + email);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("OTP 요청 중복 감지: {}", email);
                return;
            }

            User user = userRepository.findOneByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 이메일 없음"));

            String code = otpGenerator.generateCode();
            Instant expiry = otpGenerator.calculateExpiry(Duration.ofMinutes(5));

            user.setOtpCode(code);
            user.setOtpExpireDate(expiry);
            userRepository.save(user);

            // 메일 발송 (재시도 1회)
            try {
                mailService.sendEmailOtp(user, code);
                recordLog(user, code, ip, userAgent, "SENT");
            } catch (Exception ex) {
                log.error("메일 발송 실패: {}", ex.getMessage());
                retryMail(user, code, ip, userAgent);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("OTP Lock 획득 실패: {}", email);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void retryMail(User user, String code, String ip, String userAgent) {
        try {
            Thread.sleep(2000);
            mailService.sendEmailOtp(user, code);
            recordLog(user, code, ip, userAgent, "SENT_RETRY");
        } catch (Exception ex) {
            log.error("메일 재시도 실패: {}", ex.getMessage());
            recordLog(user, code, ip, userAgent, "FAILED_SEND");
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    protected void recordLog(User user, String code, String ip, String ua, String status) {
        EmailOtpLog logEntity = new EmailOtpLog();
        logEntity.setUser(user);
        logEntity.setEmail(user.getEmail());
        logEntity.setOtpCode(code);
        logEntity.setRequestIp(ip);
        logEntity.setUserAgent(ua);
        logEntity.setDeviceType(detectDevice(ua));
        logEntity.setCreatedDate(Instant.now());
        logEntity.setStatus(status);
        emailOtpLogRepository.save(logEntity);
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return "mobile";
        if (ua.contains("tablet")) return "tablet";
        return "desktop";
    }
}

