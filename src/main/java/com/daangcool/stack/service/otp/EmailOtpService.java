package com.daangcool.stack.service.otp;


import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.service.MailService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Email OTP 인증 서비스.
 *
 * <p>
 * 이 클래스는 OTP 요청 및 검증 로직을 처리하며,
 * JWT 발급은 {@link com.daangcool.stack.web.rest.EmailOtpResource} 에서 수행합니다.
 * </p>
 *
 * <ul>
 *   <li>이메일로 OTP 코드 생성 및 발송</li>
 *   <li>OTP 코드 유효성 검증</li>
 *   <li>유효한 OTP일 경우 {@link User} 반환</li>
 * </ul>
 *
 * @author Steve
 * @since 2025-10-15
 */
@Service
@Transactional
public class EmailOtpService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);

    private final UserRepository userRepository;
    private final EmailOtpLogService emailOtpLogService;
    private final EmailOtpGenerator otpGenerator;
    private final EmailOtpCacheService otpCacheService;
    private final EmailOtpValidator otpValidator;
    private final MailService mailService;
    private final RedissonClient redissonClient;

    public EmailOtpService(
        UserRepository userRepository,
        EmailOtpLogService emailOtpLogService,
        EmailOtpGenerator otpGenerator,
        EmailOtpCacheService otpCacheService,
        EmailOtpValidator otpValidator,
        MailService mailService,
        RedissonClient redissonClient
    ) {
        this.userRepository = userRepository;
        this.emailOtpLogService = emailOtpLogService;
        this.otpGenerator = otpGenerator;
        this.otpCacheService = otpCacheService;
        this.otpValidator = otpValidator;
        this.mailService = mailService;
        this.redissonClient = redissonClient;
    }

    // ==========================================================
    // 1️⃣ OTP 요청: 이메일 발송
    // ==========================================================
    /**
     * 주어진 이메일 주소로 OTP 인증번호를 생성 및 발송합니다.
     * <p>Redisson Lock을 사용해 동일 이메일에 대한 중복 요청을 방지합니다.</p>
     *
     * @param email 대상 이메일 주소
     * @param ip 요청자 IP
     * @param userAgent 요청자 User-Agent
     */
    public void requestOtp(String email, String ip, String userAgent) {
        log.debug("[OTP] requestOtp() called for {}", email);

        RLock lock = redissonClient.getLock("lock:otp:" + email);
        boolean acquired = false;
        try {
            // 5초 대기, 10초 후 자동 해제
            acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[OTP] 중복 요청 감지 (Lock 획득 실패): {}", email);
                return;
            }

            var userOpt = userRepository.findOneByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                log.warn("[OTP] 존재하지 않는 이메일: {}", email);
                return;
            }

            var user = userOpt.get();

            // 6자리 OTP 코드 생성
            String otpCode = otpGenerator.generateCode();

            // 캐시에 저장 (Redis TTL 적용)
            otpCacheService.setOtpCode(email, otpCode);

            // 이메일 발송
            try {
                mailService.sendEmailOtp(user, otpCode);
                emailOtpLogService.recordLog(user, otpCode, ip, userAgent, "SENT");
                log.info("[OTP] {} 에 OTP 코드 발송 완료", email);
            } catch (Exception e) {
                log.error("[OTP] 메일 발송 실패: {}", e.getMessage());
                emailOtpLogService.recordLog(user, otpCode, ip, userAgent, "FAILED_SEND");
                // 발송 실패 시 캐시 삭제 처리 (필요에 따라 유지 가능)
                otpCacheService.deleteOtpCode(email);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[OTP] Lock 획득 중 인터럽트 발생: {}", email);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==========================================================
    // 2️⃣ OTP 검증
    // ==========================================================
    /**
     * OTP 인증코드를 검증하고, 성공 시 {@link User} 를 반환합니다.
     *
     * @param email 이메일 주소
     * @param code 입력된 OTP 코드
     * @return 검증 성공 시 {@link User}, 실패 시 {@link Optional#empty()}
     */
    public Optional<User> verifyOtp(String email, String code) {
        log.debug("[OTP] verifyOtp() called for {}", email);

        var userOpt = userRepository.findOneByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.warn("[OTP] Unknown email: {}", email);
            return Optional.empty();
        }

        var user = userOpt.get();

        // OTP 코드 검증
        boolean verified = otpValidator.validateOtp(user, code);
        if (!verified) {
            log.warn("[OTP] 인증 실패: {}", email);
            emailOtpLogService.recordLog(user, code, null, null, "FAILED_VERIFY");
            return Optional.empty();
        }

        // 검증 성공
        log.info("[OTP] 인증 성공: {}", email);
        emailOtpLogService.recordLog(user, code, null, null, "VERIFIED");
        return Optional.of(user);
    }


}
