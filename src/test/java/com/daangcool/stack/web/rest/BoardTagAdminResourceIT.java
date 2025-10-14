package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.BoardTag;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.BoardTagRepository;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: BoardTagAdminResource (API 계층)
 */
@AutoConfigureMockMvc
@IntegrationTest
class BoardTagAdminResourceIT {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BoardTagRepository boardTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private EntityManager entityManager;

    private Board board;
    private Tag tag;
    private BoardTag activeBoardTag;
    private BoardTag deletedBoardTag;
    private CommonCodeDetail noticeBoardType;

    @BeforeEach
    public void initTest() {
        boardTagRepository.deleteAll();
        boardRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll();
        commonCodeGroupRepository.deleteAll();
        entityManager.flush();

        User user = new User();
        user.setLogin("admin_user");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60)); // Changed to 60-character random password
        user.setActivated(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(user);

        // CommonCodeGroup 생성 및 저장
        CommonCodeGroup boardTypeGroup = new CommonCodeGroup();
        boardTypeGroup.setGroupCode("BOARD_TYPE");
        boardTypeGroup.setGroupName("게시판 유형");
        commonCodeGroupRepository.saveAndFlush(boardTypeGroup);

        // CommonCodeDetail 생성 및 저장
        noticeBoardType = new CommonCodeDetail();
        noticeBoardType.setCode("NOTICE");
        noticeBoardType.setName("공지사항");
        noticeBoardType.setGroup(boardTypeGroup);
        commonCodeDetailRepository.saveAndFlush(noticeBoardType);

        entityManager.clear();
        User managedUser = userRepository.findOneByLogin("admin_user").get();
        CommonCodeDetail managedNoticeBoardType = commonCodeDetailRepository.findOneByGroupGroupCodeAndCodeAndDeletedIsFalse("BOARD_TYPE", "NOTICE").get();

        board = new Board();
        board.setTitle("Board For Tag Test");
        board.setContent("Test Content for BoardTagAdminResourceIT"); // content 추가
        board.setUser(managedUser);
        board.setBoardType(managedNoticeBoardType); // boardType 추가
        boardRepository.saveAndFlush(board);

        tag = new Tag();
        tag.setName("tag-for-board");
        tagRepository.saveAndFlush(tag);

        activeBoardTag = new BoardTag();
        activeBoardTag.setBoard(board);
        activeBoardTag.setTag(tag);
        boardTagRepository.saveAndFlush(activeBoardTag);

        Tag anotherTag = new Tag();
        anotherTag.setName("another-tag");
        tagRepository.saveAndFlush(anotherTag);

        deletedBoardTag = new BoardTag();
        deletedBoardTag.setBoard(board);
        deletedBoardTag.setTag(anotherTag);
        deletedBoardTag.setDeleted(true);
        boardTagRepository.saveAndFlush(deletedBoardTag);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllRelations_AsAdmin_ShouldReturnAllRelations() throws Exception {
        restMockMvc.perform(get("/api/admin/board-tags"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].boardId").value(hasItem(board.getId().intValue())))
            .andExpect(jsonPath("$.[*].tagId").value(hasItem(tag.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void getAllRelations_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(get("/api/admin/board-tags"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void hardDelete_AsAdmin_ShouldDeleteRelation() throws Exception {
        long countBeforeDelete = boardTagRepository.count();

        restMockMvc.perform(delete("/api/admin/board-tags/{boardId}/{tagId}/hard", board.getId(), tag.getId()))
            .andExpect(status().isNoContent());

        assertThat(boardTagRepository.count()).isEqualTo(countBeforeDelete - 1);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void restore_AsAdmin_ShouldRestoreRelation() throws Exception {
        assertThat(deletedBoardTag.isDeleted()).isTrue();

        restMockMvc.perform(put("/api/admin/board-tags/{id}/restore", deletedBoardTag.getId()))
            .andExpect(status().isNoContent());

        BoardTag restored = boardTagRepository.findById(deletedBoardTag.getId()).get();
        assertThat(restored.isDeleted()).isFalse();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void restore_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(put("/api/admin/board-tags/{id}/restore", deletedBoardTag.getId()))
            .andExpect(status().isForbidden());
    }
}
