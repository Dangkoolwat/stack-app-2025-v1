package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트: TagAdminResource (API 계층)
 * - 관리자 전용 태그 관리 API의 권한 및 동작을 검증합니다.
 */
@AutoConfigureMockMvc
@IntegrationTest
class TagAdminResourceIT {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MockMvc restMockMvc;

    private Tag activeTag;
    private Tag deletedTag;

    @BeforeEach
    public void initTest() {
        // 테스트 실행 전, 데이터베이스를 초기화합니다.
        tagRepository.deleteAll();

        // 테스트용 활성 태그와 삭제된 태그를 생성합니다.
        activeTag = new Tag();
        activeTag.setName("active-tag");
        tagRepository.saveAndFlush(activeTag);

        deletedTag = new Tag();
        deletedTag.setName("deleted-tag");
        deletedTag.setDeleted(true); // 논리적 삭제 상태로 설정
        tagRepository.saveAndFlush(deletedTag);
    }

    /**
     * 태그 논리 삭제 테스트 (DELETE /api/admin/tags/{id})
     * - 관리자 권한으로 API 호출 시, 태그가 성공적으로 논리 삭제되는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteTag_AsAdmin_ShouldSoftDeleteTag() throws Exception {
        // when & then
        restMockMvc.perform(delete("/api/admin/tags/{id}", activeTag.getId()))
            .andExpect(status().isNoContent());

        // 삭제된 태그의 isDeleted 플래그가 true인지 확인합니다.
        Tag resultTag = tagRepository.findByIdEvenIfDeleted(activeTag.getId()).get(); // Changed to findByIdEvenIfDeleted
        assertThat(resultTag.isDeleted()).isTrue();
    }

    /**
     * 태그 논리 삭제 권한 테스트
     * - 일반 사용자 권한으로 API 호출 시, 403 Forbidden 오류가 발생하는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void deleteTag_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(delete("/api/admin/tags/{id}", activeTag.getId()))
            .andExpect(status().isForbidden());
    }

    /**
     * 태그 복구 테스트 (PATCH /api/admin/tags/{id}/undelete)
     * - 관리자 권한으로 API 호출 시, 삭제된 태그가 성공적으로 복구되는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void undeleteTag_AsAdmin_ShouldRestoreTag() throws Exception {
        // when & then
        restMockMvc.perform(patch("/api/admin/tags/{id}/undelete", deletedTag.getId()))
            .andExpect(status().isOk());

        // 복구된 태그의 isDeleted 플래그가 false인지 확인합니다.
        Tag resultTag = tagRepository.findById(deletedTag.getId()).get();
        assertThat(resultTag.isDeleted()).isFalse();
    }

    /**
     * 태그 복구 권한 테스트
     * - 일반 사용자 권한으로 API 호출 시, 403 Forbidden 오류가 발생하는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void undeleteTag_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(patch("/api/admin/tags/{id}/undelete", deletedTag.getId()))
            .andExpect(status().isForbidden());
    }
}
