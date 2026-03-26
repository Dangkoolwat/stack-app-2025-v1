package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.board.UploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * 통합 테스트: UploadResource (API 계층)
 * - 실제 파일 I/O를 포함하여 다운로드 및 접근 제어 로직을 검증합니다.
 */
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser(username = "user", roles = "USER")
class UploadResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UploadRepository uploadRepository;

    private Upload publicUpload;
    private Upload privateUpload;
    private MockMultipartFile multipartFile;
    private byte[] imageBytes;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 가짜 파일을 생성합니다.
        imageBytes = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 'H', 'e', 'l', 'l', 'o'};
        multipartFile = new MockMultipartFile(
            "file",
            "hello.png",
            "image/png",
            imageBytes
        );

        // 테스트 실행 전, 데이터베이스를 초기화합니다.
        uploadRepository.deleteAll();

        // 테스트용 공개 파일과 비공개 파일을 미리 저장해둡니다.
        publicUpload = uploadService.saveUpload(multipartFile, "PUBLIC_TEST", true);
        privateUpload = uploadService.saveUpload(multipartFile, "PRIVATE_TEST", false);
    }

    @AfterEach
    void tearDown() {
        // 테스트 실행 후, 생성된 파일을 물리적으로 삭제하여 정리합니다.
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        if (publicUpload != null) {
            uploadService.hardDelete(publicUpload.getId());
        }
        if (privateUpload != null) {
            uploadService.hardDelete(privateUpload.getId());
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 공개 파일 다운로드 테스트 (GET /api/uploads/{id}/download)
     * - 공개된 파일을 누구나 성공적으로 다운로드할 수 있는지 확인합니다.
     */
    @Test
    @Transactional
    void downloadPublicFile_Success() throws Exception {
        restMockMvc.perform(get("/api/uploads/{id}/download", publicUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(header().string("Content-Disposition", "attachment; filename*=UTF-8''hello.png"))
            .andExpect(content().bytes(imageBytes));
    }

    /**
     * 비공개 파일 다운로드 테스트 (인증 없이)
     * - 공개 다운로드 API로 비공개 파일을 받으려 할 때 403 Forbidden 오류가 발생하는지 확인합니다.
     */
    @Test
    @Transactional
    void downloadPrivateFile_WithoutAuth_ShouldFail() throws Exception {
        restMockMvc.perform(get("/api/uploads/{id}/download", privateUpload.getId()))
            .andExpect(status().isForbidden());
    }

    /**
     * 비공개 파일 다운로드 테스트 (인증 완료)
     * - 인증된 사용자가 비공개 다운로드 API를 통해 파일을 성공적으로 받는지 확인합니다.
     */
    @Test
    @Transactional
    void downloadPrivateFile_WithAuth_ShouldSucceed() throws Exception {
        restMockMvc.perform(get("/api/uploads/private/{id}/download", privateUpload.getId())
                .with(user("user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    @Transactional
    void downloadPrivateFile_WithOtherUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(get("/api/uploads/private/{id}/download", privateUpload.getId())
                .with(user("other-user").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void downloadPrivateFile_WithAdmin_ShouldSucceed() throws Exception {
        restMockMvc.perform(get("/api/uploads/private/{id}/download", privateUpload.getId())
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"));
    }

    /**
     * 존재하지 않는 파일 다운로드 테스트
     * - 유효하지 않은 ID로 다운로드를 시도할 때 404 Not Found 오류가 발생하는지 확인합니다.
     */
    @Test
    @Transactional
    void downloadNonExistentFile_ShouldReturnNotFound() throws Exception {
        restMockMvc.perform(get("/api/uploads/{id}/download", 9999L))
            .andExpect(status().isNotFound());
    }

    /**
     * 파일 미리보기 테스트 (GET /api/uploads/{id}/preview)
     * - Content-Disposition 헤더가 'inline'으로 설정되어 브라우저에서 바로 열리는지 확인합니다.
     */
    @Test
    @Transactional
    void previewPublicFile_ShouldReturnInline() throws Exception {
        restMockMvc.perform(get("/api/uploads/{id}/preview", publicUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(header().string("Content-Disposition", containsString("inline; filename*=UTF-8''")))
            .andExpect(content().bytes(imageBytes));
    }
    /**
     * 파일 업로드 테스트 (POST /api/uploads)
     * - 인증된 사용자가 파일을 정상적으로 업로드하고 URL Location을 리턴받는지 확인합니다.
     */
    @Test
    @Transactional
    void uploadFile_WithAuth_ShouldSucceed() throws Exception {
        restMockMvc.perform(multipart("/api/uploads")
                .file(multipartFile)
                .param("public", "true")
                .with(user("user").roles("USER")))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.sourceFilename").value("hello.png"));
    }

    /**
     * 파일 단건 소프트 삭제 테스트 (DELETE /api/uploads/{id})
     * - 권한이 있는 사용자가 파일 아이디로 소프트 삭제를 수행할 수 있는지 확인합니다.
     */
    @Test
    @Transactional
    void deleteUpload_WithAuth_ShouldSoftDelete() throws Exception {
        restMockMvc.perform(delete("/api/uploads/{id}", publicUpload.getId())
                .with(user("user").roles("USER")))
            .andExpect(status().isNoContent());

        Upload deletedUpload = uploadRepository.findById(publicUpload.getId()).get();
        org.junit.jupiter.api.Assertions.assertTrue(deletedUpload.isDeleted());
    }
}
