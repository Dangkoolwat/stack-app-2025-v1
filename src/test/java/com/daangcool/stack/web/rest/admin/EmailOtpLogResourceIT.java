package com.daangcool.stack.web.rest.admin;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.EmailOtpLog;
import com.daangcool.stack.repository.EmailOtpLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for {@link EmailOtpLogResource}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class EmailOtpLogResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailOtpLogRepository emailOtpLogRepository;

    private EmailOtpLog sampleLog;

    @BeforeEach
    void init() {
        sampleLog = new EmailOtpLog();
        sampleLog.setEmail("admin@test.com");
        sampleLog.setOtpCode("123456");
        sampleLog.setStatus("VERIFIED");
        sampleLog.setCreatedDate(Instant.now());
        emailOtpLogRepository.saveAndFlush(sampleLog);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("관리자 OTP 로그 조회 성공")
    void getOtpLogs_shouldReturnOkAndList() throws Exception {
        mockMvc.perform(get("/api/admin/otp-logs")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].email").value("admin@test.com"))
            .andExpect(jsonPath("$.content[0].status").value("VERIFIED"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @DisplayName("관리자 권한 없는 사용자는 403 반환")
    void getOtpLogs_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/otp-logs"))
            .andExpect(status().isForbidden());
    }
}

