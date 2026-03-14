package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: BoardAdminResource (API 계층)
 */
@AutoConfigureMockMvc
@IntegrationTest
class BoardAdminResourceIT {

    @Autowired
    private BoardRepository boardRepository;

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

    private Board activeBoard;
    private Board deletedBoard;
    private CommonCodeDetail noticeBoardType;

    @BeforeEach
    public void initTest() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll();
        commonCodeGroupRepository.deleteAll();
        entityManager.flush();

        User user = new User();
        user.setLogin("admin_user");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(user);

        CommonCodeGroup boardTypeGroup = new CommonCodeGroup();
        boardTypeGroup.setGroupCode("BOARD_TYPE");
        boardTypeGroup.setGroupName("게시판 유형");
        commonCodeGroupRepository.saveAndFlush(boardTypeGroup);

        CommonCodeDetail noticeBoardTypeSaved = new CommonCodeDetail();
        noticeBoardTypeSaved.setCode("NOTICE");
        noticeBoardTypeSaved.setName("공지사항");
        noticeBoardTypeSaved.setGroup(boardTypeGroup);
        commonCodeDetailRepository.saveAndFlush(noticeBoardTypeSaved);

        entityManager.clear(); // 모든 엔티티를 분리

        // 분리된 엔티티들을 다시 조회하여 영속성 컨텍스트에 관리되는 상태로 만듭니다.
        User managedUser = userRepository.findOneByLogin("admin_user").get();
        CommonCodeGroup managedBoardTypeGroup = commonCodeGroupRepository.findById("BOARD_TYPE").get();
        CommonCodeDetail managedNoticeBoardType = commonCodeDetailRepository.findOneByGroupGroupCodeAndCodeAndDeletedIsFalse(managedBoardTypeGroup.getGroupCode(), "NOTICE").get();

        activeBoard = new Board();
        activeBoard.setTitle("활성 게시글");
        activeBoard.setContent("활성 게시글 내용");
        activeBoard.setUser(managedUser);
        activeBoard.setBoardType(managedNoticeBoardType);
        boardRepository.saveAndFlush(activeBoard);

        deletedBoard = new Board();
        deletedBoard.setTitle("삭제된 게시글");
        deletedBoard.setContent("삭제된 게시글 내용");
        deletedBoard.setUser(managedUser);
        deletedBoard.setDeleted(true);
        deletedBoard.setBoardType(managedNoticeBoardType);
        boardRepository.saveAndFlush(deletedBoard);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getDeletedBoards_AsAdmin_ShouldReturnDeletedBoards() throws Exception {
        restMockMvc.perform(get("/api/admin/boards/deleted"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].title").value(hasItem("삭제된 게시글")))
            .andExpect(jsonPath("$.[*].title").value(not(hasItem("활성 게시글"))));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void getDeletedBoards_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(get("/api/admin/boards/deleted"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void restoreBoard_AsAdmin_ShouldRestoreBoard() throws Exception {
        restMockMvc.perform(put("/api/admin/boards/restore/{id}", deletedBoard.getId()))
            .andExpect(status().isOk());

        Board restoredBoard = boardRepository.findById(deletedBoard.getId()).get();
        assertThat(restoredBoard.isDeleted()).isFalse();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void hardDeleteBoard_AsAdmin_ShouldDeletePermanently() throws Exception {
        restMockMvc.perform(delete("/api/admin/boards/hard-delete/{id}", activeBoard.getId()))
            .andExpect(status().isNoContent());

        assertThat(boardRepository.findById(activeBoard.getId())).isEmpty();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void toggleNotice_AsAdmin_ShouldChangeNoticeStatus() throws Exception {
        restMockMvc.perform(patch("/api/admin/boards/{id}/notice?notice=true", activeBoard.getId()))
            .andExpect(status().isOk());

        Board noticeBoard = boardRepository.findById(activeBoard.getId()).get();
        assertThat(noticeBoard.isNotice()).isTrue();

        restMockMvc.perform(patch("/api/admin/boards/{id}/notice?notice=false", activeBoard.getId()))
            .andExpect(status().isOk());

        Board normalBoard = boardRepository.findById(activeBoard.getId()).get();
        assertThat(normalBoard.isNotice()).isFalse();
    }
}
