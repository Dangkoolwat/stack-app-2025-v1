package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.board.UploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: UploadResource (API 계층)
 * - 실제 파일 I/O를 포함하여 다운로드 및 접근 제어 로직을 검증합니다.
 */
@AutoConfigureMockMvc
@IntegrationTest
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

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 가짜 파일을 생성합니다.
        multipartFile = new MockMultipartFile(
            "file",
            "hello.txt",
            "text/plain",
            "Hello, World!".getBytes()
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
        if (publicUpload != null) {
            uploadService.hardDelete(publicUpload.getId());
        }
        if (privateUpload != null) {
            uploadService.hardDelete(privateUpload.getId());
        }
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
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(header().string("Content-Disposition", "attachment; filename*=UTF-8''hello.txt"))
            .andExpect(content().string("Hello, World!"));
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
    @WithMockUser // 이 어노테이션으로 인증된 사용자를 시뮬레이션합니다.
    @Transactional
    void downloadPrivateFile_WithAuth_ShouldSucceed() throws Exception {
        restMockMvc.perform(get("/api/uploads/private/{id}/download", privateUpload.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(content().string("Hello, World!"));
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
            .andExpect(header().string("Content-Type", "text/plain"))
            .andExpect(header().string("Content-Disposition", containsString("inline; filename=")))
            .andExpect(content().string("Hello, World!"));
    }
}
