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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: UploadAdminResource (API 계층)
 * - 관리자 전용 파일 관리 API의 권한 및 동작을 검증합니다.
 */
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser(username = "admin", authorities = {"ROLE_ADMIN", "ROLE_USER"})
class UploadAdminResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UploadRepository uploadRepository;

    private Upload testUpload;

    @BeforeEach
    void setUp() {
        // 테스트 실행 전, 데이터베이스를 초기화합니다.
        uploadRepository.deleteAll();

        // 테스트에 사용할 가짜 파일을 생성하고 저장합니다.
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "admin-test.txt",
            "text/plain",
            "Admin Test Content".getBytes()
        );
        testUpload = uploadService.saveUpload(multipartFile, "ADMIN_TEST", true);
    }

    @AfterEach
    void tearDown() {
        // 테스트 실행 후, 생성된 파일을 물리적으로 삭제하여 정리합니다.
        // hardDelete는 ID로 찾아서 삭제하므로, soft-delete 테스트 후에도 ID로 찾을 수 있어야 합니다.
        Optional<Upload> uploadOptional = uploadRepository.findById(testUpload.getId());
        uploadOptional.ifPresent(upload -> uploadService.hardDelete(upload.getId()));
    }

    /**
     * 파일 논리 삭제 테스트 (DELETE /api/admin/uploads/{id}?mode=soft)
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteFile_AsAdmin_SoftDelete_ShouldSucceed() throws Exception {
        restMockMvc.perform(delete("/api/admin/uploads/{id}?mode=soft", testUpload.getId()))
            .andExpect(status().isNoContent());

        Upload deletedUpload = uploadRepository.findById(testUpload.getId()).get();
        assertThat(deletedUpload.isDeleted()).isTrue();
    }

    /**
     * 파일 물리 삭제 테스트 (DELETE /api/admin/uploads/{id}?mode=hard)
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteFile_AsAdmin_HardDelete_ShouldSucceed() throws Exception {
        restMockMvc.perform(delete("/api/admin/uploads/{id}?mode=hard", testUpload.getId()))
            .andExpect(status().isNoContent());

        assertThat(uploadRepository.findById(testUpload.getId())).isEmpty();
    }

    /**
     * 파일 삭제 권한 테스트
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void deleteFile_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(delete("/api/admin/uploads/{id}", testUpload.getId()))
            .andExpect(status().isForbidden());
    }

    /**
     * 파일 공개 상태 변경 테스트 (PATCH /api/admin/uploads/{id}/visibility)
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeVisibility_AsAdmin_ShouldChangeFlag() throws Exception {
        // 공개 -> 비공개로 변경
        restMockMvc.perform(patch("/api/admin/uploads/{id}/visibility?public=false", testUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.public").value(false));

        Upload privateUpload = uploadRepository.findById(testUpload.getId()).get();
        assertThat(privateUpload.isPublic()).isFalse();

        // 비공개 -> 공개로 다시 변경
        restMockMvc.perform(patch("/api/admin/uploads/{id}/visibility?public=true", testUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.public").value(true));

        Upload publicUpload = uploadRepository.findById(testUpload.getId()).get();
        assertThat(publicUpload.isPublic()).isTrue();
    }

    /**
     * 파일 메타데이터 조회 테스트 (GET /api/admin/uploads/{id})
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getFileMetadata_AsAdmin_ShouldReturnMetadata() throws Exception {
        restMockMvc.perform(get("/api/admin/uploads/{id}", testUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(testUpload.getId().intValue()))
            .andExpect(jsonPath("$.sourceFilename").value("admin-test.txt"));
    }
    /**
     * 소프트 삭제 일괄 물리 삭제(Purge) 기능 테스트 
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void purgeDeletedFiles_AsAdmin_ShouldClearDeletedUploads() throws Exception {
        // 우선 기존 파일을 소프트 삭제 처리합니다.
        uploadService.softDelete(testUpload.getId(), "For Purge Test");

        restMockMvc.perform(delete("/api/admin/uploads/purge"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Purged 1 soft-deleted files.")));

        // Purge 후에는 DB에서도 완전히 사라져야 함
        assertThat(uploadRepository.findById(testUpload.getId())).isEmpty();
    }
}
