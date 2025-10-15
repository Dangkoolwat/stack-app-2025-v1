package com.daangcool.stack.service.otp;


import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    private final EmailOtpGenerator otpGenerator;
    private final EmailOtpCacheService otpCacheService;
    private final EmailOtpValidator otpValidator;
    private final MailService mailService;

    public EmailOtpService(
        UserRepository userRepository,
        EmailOtpGenerator otpGenerator,
        EmailOtpCacheService otpCacheService,
        EmailOtpValidator otpValidator,
        MailService mailService
    ) {
        this.userRepository = userRepository;
        this.otpGenerator = otpGenerator;
        this.otpCacheService = otpCacheService;
        this.otpValidator = otpValidator;
        this.mailService = mailService;
    }

    // ==========================================================
    // 1️⃣ OTP 요청: 이메일 발송
    // ==========================================================
    /**
     * 주어진 이메일 주소로 OTP 인증번호를 생성 및 발송합니다.
     *
     * @param email 대상 이메일 주소
     */
    public void requestOtp(String email) {
        log.debug("[OTP] requestOtp() called for {}", email);

        var userOpt = userRepository.findOneByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.warn("[OTP] 존재하지 않는 이메일: {}", email);
            return;
        }

        var user = userOpt.get();

        // 6자리 OTP 코드 생성
        String otpCode = otpGenerator.generateCode();

        // 캐시에 저장 (TTL 예: 5분)
        otpCacheService.setOtpCode(email, otpCode);

        // 이메일 발송
        mailService.sendEmailOtp(user, otpCode);

        log.info("[OTP] {} 에 OTP 코드 발송 완료", email);
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
            return Optional.empty();
        }

        // 검증 성공
        log.info("[OTP] 인증 성공: {}", email);
        return Optional.of(user);
    }
}
