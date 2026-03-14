package com.daangcool.stack.web.rest;


import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.service.otp.EmailOtpCacheService;
import com.daangcool.stack.service.otp.EmailOtpService;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class EmailOtpResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailOtpService emailOtpService;

    @Autowired
    private EmailOtpCacheService otpCacheService;

    private User testUser;

    @BeforeEach
    void initTest() {
        testUser = new User();
        testUser.setLogin("otp-user");
        testUser.setEmail("otp-user@test.local");
        testUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser.setActivated(true);
        userRepository.saveAndFlush(testUser);
    }

    // ==========================================================
    // OTP 요청 테스트
    // ==========================================================
    @Test
    void shouldSendOtpToEmailSuccessfully() throws Exception {
        mockMvc.perform(
                post("/api/auth/email/request")
                    .param("email", testUser.getEmail())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            )
            .andExpect(status().isOk());

        // Redis 캐시에 OTP 코드가 저장되었는지 확인
        String cachedOtp = otpCacheService.getOtpCode(testUser.getEmail());
        assertThat(cachedOtp).isNotBlank();
    }

    // ==========================================================
    // OTP 검증 + JWT 발급 성공
    // ==========================================================
    @Test
    void shouldVerifyOtpAndReturnJwt() throws Exception {
        // given
        String otpCode = "123456";
        otpCacheService.setOtpCode(testUser.getEmail(), otpCode);

        // when
        var result = mockMvc.perform(
                post("/api/auth/email/verify")
                    .param("email", testUser.getEmail())
                    .param("code", otpCode)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            )
            // then
            .andExpect(status().isOk())
            .andExpect(header().exists("Authorization"))
            .andExpect(jsonPath("$.jwt").exists())
            .andExpect(jsonPath("$.email").value(testUser.getEmail()))
            .andReturn();

        // 응답 JWT 값 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("jwt");
    }

    // ==========================================================
    //  잘못된 OTP 코드 테스트
    // ==========================================================
    @Test
    void shouldFailOnInvalidOtp() throws Exception {
        otpCacheService.setOtpCode(testUser.getEmail(), "654321");

        mockMvc.perform(
                post("/api/auth/email/verify")
                    .param("email", testUser.getEmail())
                    .param("code", "000000")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            )
            .andExpect(status().isUnauthorized());

        // 실패 횟수 증가 확인
        int fails = otpCacheService.getFailureCount(testUser.getEmail());
        assertThat(fails).isEqualTo(1);
    }

    // ==========================================================
    // 5회 실패 후 계정 잠금 테스트
    // ==========================================================
    @Test
    void shouldLockAccountAfterFiveFailures() throws Exception {
        otpCacheService.setOtpCode(testUser.getEmail(), "654321");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post("/api/auth/email/verify")
                        .param("email", testUser.getEmail())
                        .param("code", "000000")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                )
                .andExpect(status().isUnauthorized());
        }

        boolean locked = otpCacheService.isLocked(testUser.getEmail());
        assertThat(locked).isTrue();
    }

    // ==========================================================
    // OTP 만료(캐시 없음) 테스트
    // ==========================================================
    @Test
    void shouldFailWhenOtpExpired() throws Exception {
        // OTP 캐시 없음
        mockMvc.perform(
                post("/api/auth/email/verify")
                    .param("email", testUser.getEmail())
                    .param("code", "123456")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            )
            .andExpect(status().isUnauthorized());
    }
}
