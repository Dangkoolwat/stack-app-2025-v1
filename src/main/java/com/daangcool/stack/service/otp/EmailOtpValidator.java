package com.daangcool.stack.service.otp;

import com.daangcool.stack.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validator class responsible for verifying OTP codes from cache.
 *
 * <p>
 * This class compares the OTP code entered by the user with the one
 * stored in Redis (or in-memory cache). It also tracks failed attempts
 * and locks the account after repeated failures.
 * </p>
 *
 * <ul>
 *   <li>Retrieve OTP from cache</li>
 *   <li>Compare with user input</li>
 *   <li>Increment failure count if invalid</li>
 *   <li>Lock account if failures exceed max attempts</li>
 *   <li>Delete OTP and reset count on success</li>
 * </ul>
 *
 * <p>
 * This component is stateless and thread-safe.
 * </p>
 *
 * @author Steve
 * @since 2025-10-15
 * @see EmailOtpCacheService
 */
@Component
public class EmailOtpValidator {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpValidator.class);
    private final EmailOtpCacheService otpCacheService;

    public EmailOtpValidator(EmailOtpCacheService otpCacheService) {
        this.otpCacheService = otpCacheService;
    }

    /**
     * Validates the OTP code entered by the user.
     *
     * <p>
     * Validation workflow:
     * </p>
     * <ol>
     *   <li>Retrieve the cached OTP code for the user's email.</li>
     *   <li>If not found, return false (expired).</li>
     *   <li>If mismatched, increment failure count and check for lock.</li>
     *   <li>If matched, delete the OTP and reset failure count.</li>
     * </ol>
     *
     * @param user  The {@link User} who owns the OTP.
     * @param input The OTP code entered by the user.
     * @return {@code true} if OTP is valid; {@code false} if invalid, expired, or locked.
     *
     * @see EmailOtpCacheService#getOtpCode(String)
     * @see EmailOtpCacheService#incrementFailureCount(String)
     * @see EmailOtpCacheService#lockAccount(String)
     * @see EmailOtpCacheService#resetFailureCount(String)
     */
    public boolean validateOtp(User user, String input) {
        String email = user.getEmail();

        // Retrieve cached OTP
        String cachedCode = otpCacheService.getOtpCode(email);
        if (cachedCode == null) {
            log.warn("[OTP] Expired or not found: {}", email);
            return false;
        }

        // Compare codes
        if (!cachedCode.equals(input)) {
            log.warn("[OTP] Invalid code for {}", email);
            otpCacheService.incrementFailureCount(email);
            int fails = otpCacheService.getFailureCount(email);
            log.debug("[OTP] {} → Fail count: {}", email, fails);

            if (fails >= otpCacheService.getMaxFailureAttempts()) {
                otpCacheService.lockAccount(email);
                log.error("[OTP] Account temporarily locked: {}", email);
            }
            return false;
        }

        // Success: clean up
        log.info("[OTP] Verified successfully: {}", email);
        otpCacheService.deleteOtpCode(email);
        otpCacheService.resetFailureCount(email);
        return true;
    }
}
