package com.daangcool.stack.service.otp;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * OTP 코드 생성 및 만료 시간 계산 유틸리티.
 */
@Component
public class EmailOtpGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public Instant calculateExpiry(Duration ttl) {
        return Instant.now().plus(ttl);
    }
}

