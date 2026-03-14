package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.Comment;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.CommentRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: CommentAdminResource (API 계층)
 */
@AutoConfigureMockMvc
@IntegrationTest
class CommentAdminResourceIT {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Comment activeComment;
    private Comment deletedComment;
    private CommonCodeDetail boardType;
    private CommonCodeGroup boardTypeGroup;

    @BeforeEach
    public void initTest() {
        commentRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll();
        commonCodeGroupRepository.deleteAll();
        entityManager.flush();

        // CommonCodeGroup 생성 및 저장 (관리되는 인스턴스 할당)
        CommonCodeGroup tempBoardTypeGroup = new CommonCodeGroup();
        tempBoardTypeGroup.setGroupCode("BOARD_TYPE");
        tempBoardTypeGroup.setGroupName("게시판 유형");
        this.boardTypeGroup = commonCodeGroupRepository.saveAndFlush(tempBoardTypeGroup);

        // CommonCodeDetail 생성 및 저장 (관리되는 인스턴스 할당)
        CommonCodeDetail tempBoardType = new CommonCodeDetail();
        tempBoardType.setCode("FREE_BOARD");
        tempBoardType.setName("자유게시판");
        tempBoardType.setGroup(this.boardTypeGroup); // 관리되는 group 사용
        this.boardType = commonCodeDetailRepository.saveAndFlush(tempBoardType);

        User user = new User();
        user.setLogin("admin_user");
        // Use a pre-encoded password that is 60 characters long to satisfy @Size constraint
        user.setPassword("$2a$10$M.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1.2.1."); // 60 characters
        user.setActivated(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(user);

        Board board = new Board();
        board.setTitle("Comment Admin Test Board");
        board.setContent("테스트 게시글 내용");
        board.setBoardType(this.boardType); // 관리되는 boardType 사용
        board.setUser(user);
        boardRepository.saveAndFlush(board);

        entityManager.clear(); // 모든 초기 엔티티 저장 후 clear 호출

        User managedUser = userRepository.findOneByLogin("admin_user").get();

        activeComment = new Comment();
        activeComment.setContent("활성 댓글");
        activeComment.setBoard(board);
        activeComment.setUser(managedUser);
        commentRepository.saveAndFlush(activeComment);

        deletedComment = new Comment();
        deletedComment.setContent("삭제된 댓글");
        deletedComment.setBoard(board);
        deletedComment.setUser(managedUser);
        deletedComment.setDeleted(true);
        commentRepository.saveAndFlush(deletedComment);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getDeletedComments_AsAdmin_ShouldReturnDeletedComments() throws Exception {
        restMockMvc.perform(get("/api/admin/comments/deleted"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].content").value(hasItem("삭제된 댓글")))
            .andExpect(jsonPath("$.[*].content").value(not(hasItem("활성 댓글"))));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void getDeletedComments_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(get("/api/admin/comments/deleted"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void restoreComment_AsAdmin_ShouldRestoreComment() throws Exception {
        restMockMvc.perform(put("/api/admin/comments/restore/{id}", deletedComment.getId()))
            .andExpect(status().isOk());

        Comment restoredComment = commentRepository.findById(deletedComment.getId()).get();
        assertThat(restoredComment.isDeleted()).isFalse();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_ADMIN")
    void hardDeleteComment_AsAdmin_ShouldDeletePermanently() throws Exception {
        long countBeforeDelete = commentRepository.count();

        restMockMvc.perform(delete("/api/admin/comments/hard-delete/{id}", activeComment.getId()))
            .andExpect(status().isNoContent());

        assertThat(commentRepository.count()).isEqualTo(countBeforeDelete - 1);
        assertThat(commentRepository.findById(activeComment.getId())).isEmpty();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = "ROLE_USER")
    void hardDeleteComment_AsUser_ShouldBeForbidden() throws Exception {
        restMockMvc.perform(delete("/api/admin/comments/hard-delete/{id}", activeComment.getId()))
            .andExpect(status().isForbidden());
    }
}
