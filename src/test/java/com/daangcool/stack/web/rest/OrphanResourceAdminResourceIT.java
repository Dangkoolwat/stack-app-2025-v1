package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.repository.board.UploadRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/**
 * 통합 테스트: OrphanResourceAdminResource (API 계층)
 * - 관리자 전용 고아(Orphan) 리소스 조회 및 물리 삭제 API 검증
 * - 24시간 유예 기간 로직 (last_modified_date 기준) 테스트 포함
 */
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser(username = "admin", authorities = {"ROLE_ADMIN", "ROLE_USER"})
class OrphanResourceAdminResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private EntityManager em;

    private Upload testUpload;

    @BeforeEach
    void setUp() {
        // 테스트 전 기존 업로드 데이터 정리
        uploadRepository.deleteAll();

        // Upload 더미 데이터 세팅 (24시간 유예시간 검증 용도)
        Upload upload = new Upload();
        upload.setSourceFilename("orphan-test.txt");
        upload.setStorageFilename("orphan-test-stored.txt");
        upload.setFileSize(1024L);
        upload.setMimeType("text/plain");
        upload.setFilePath("/tmp/orphan-test-stored.txt");
        upload.setStorageKey("BOARD_ATTACHMENT");
        upload.setPublic(true);
        upload.setDeleted(true); // 소프트 삭제 상태 (고아 분류 조건)
        
        testUpload = uploadRepository.saveAndFlush(upload);
    }

    /**
     * 최근에 삭제(생성)된 파일은 24시간 유예 조건에 걸려 노출되지 않아야 함
     */
    @Test
    @Transactional
    void getOrphanUploads_GracePeriod_ShouldReturnEmpty_WhenRecentlyUpdated() throws Exception {
        restMockMvc.perform(get("/api/admin/orphans/uploads")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * 24시간 이상 지난 파일은 고아 목록에 정상 노출되어야 함
     */
    @Test
    @Transactional
    void getOrphanUploads_GracePeriod_ShouldReturnItem_WhenOlderThan24h() throws Exception {
        // Native Query를 사용하여 JPA Auditing을 우회하고 last_modified_date를 과거(25시간 전)로 강제 변경
        Instant pastTime = Instant.now().minus(25, ChronoUnit.HOURS);
        em.createNativeQuery("UPDATE stack_upload_file SET last_modified_date = :date WHERE id = :id")
            .setParameter("date", pastTime)
            .setParameter("id", testUpload.getId())
            .executeUpdate();
        em.flush();
        em.clear();

        restMockMvc.perform(get("/api/admin/orphans/uploads")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(testUpload.getId().intValue()));
    }

    /**
     * 고아 리소스 하드 삭제(완전 삭제) 기능 검증
     */
    @Test
    @Transactional
    void deleteOrphanUploads_ShouldHardDeleteItems() throws Exception {
        // 관리자로 직접 hard delete 수행 요청 (배열 형태)
        restMockMvc.perform(delete("/api/admin/orphans/uploads")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + testUpload.getId() + "]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deletedCount").value(1)); // 1개 삭제 성공 반환

        // 데이터베이스에서 완전 삭제되었는지 확인
        assertThat(uploadRepository.findById(testUpload.getId())).isEmpty();
    }

    /**
     * 일반 사용자(관리자가 아닌)는 접근 불가능해야 함
     */
    @Test
    @Transactional
    void apiAccess_ShouldBeForbidden_ForNormalUser() throws Exception {
        restMockMvc.perform(get("/api/admin/orphans/uploads")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
    }
}
