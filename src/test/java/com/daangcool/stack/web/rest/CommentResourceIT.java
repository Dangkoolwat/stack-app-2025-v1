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
import com.daangcool.stack.service.dto.CommentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * 통합 테스트: CommentResource (API 계층)
 */
@AutoConfigureMockMvc
@IntegrationTest
class CommentResourceIT {

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
    private ObjectMapper om;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Board board;
    private Comment comment;
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

        User userToSave = new User();
        userToSave.setLogin("comment_user");
        userToSave.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60)); // Set password to 60 characters
        userToSave.setActivated(true);
        userToSave.setEnabled(true);
        userToSave.setAccountNonExpired(true);
        userToSave.setAccountNonLocked(true);
        userToSave.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(userToSave);

        entityManager.clear();
        this.user = userRepository.findOneByLogin("comment_user").get();

        Board boardToSave = new Board();
        boardToSave.setTitle("댓글 테스트 게시글");
        boardToSave.setContent("내용");
        boardToSave.setBoardType(this.boardType); // Set boardType
        boardToSave.setUser(this.user);
        this.board = boardRepository.saveAndFlush(boardToSave);

        comment = new Comment();
        comment.setContent("기본 댓글");
        comment.setBoard(this.board);
        comment.setUser(this.user);
    }

    @Test
    @Transactional
    void createComment() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setBoardId(board.getId());
        commentDTO.setUserId(user.getId());
        commentDTO.setContent("새로운 댓글입니다.");

        long databaseSizeBeforeCreate = commentRepository.count();

        restMockMvc.perform(post("/api/comments")
                .with(user(user.getLogin())) // Authenticate the request
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentDTO)))
            .andExpect(status().isCreated());

        assertThat(commentRepository.count()).isEqualTo(databaseSizeBeforeCreate + 1);
    }

    @Test
    @Transactional
    void getCommentsByBoard() throws Exception {
        commentRepository.saveAndFlush(comment);

        restMockMvc.perform(get("/api/comments/board/{boardId}", board.getId())
                .with(user(user.getLogin()))) // Authenticate the request
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].content").value(hasItem("기본 댓글")));
    }

    @Test
    @Transactional
    void updateComment() throws Exception {
        commentRepository.saveAndFlush(comment);
        CommentDTO updatedDTO = new CommentDTO();
        updatedDTO.setContent("수정된 댓글 내용");

        restMockMvc.perform(put("/api/comments/{id}", comment.getId())
                .with(user(user.getLogin())) // Authenticate the request
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updatedDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("수정된 댓글 내용"));
    }

    @Test
    @Transactional
    void deleteComment() throws Exception {
        commentRepository.saveAndFlush(comment);

        restMockMvc.perform(delete("/api/comments/{id}", comment.getId())
                .with(user(user.getLogin()))) // Authenticate the request
            .andExpect(status().isNoContent());

        // After soft delete, the comment should no longer be found by the default findById method
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @Transactional
    void increaseStar() throws Exception {
        commentRepository.saveAndFlush(comment);
        Long starCountBefore = comment.getStar();

        restMockMvc.perform(patch("/api/comments/{id}/star", comment.getId())
                .with(user(user.getLogin()))) // Authenticate the request
            .andExpect(status().isOk());

        Comment starredComment = commentRepository.findById(comment.getId()).get();
        assertThat(starredComment.getStar()).isEqualTo(starCountBefore + 1);
    }

    @Test
    @Transactional
    void searchComments() throws Exception {
        commentRepository.saveAndFlush(comment);

        restMockMvc.perform(get("/api/comments/search?keyword=기본")
                .with(user(user.getLogin()))) // Authenticate the request
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.content.[0].content", is("기본 댓글")));
    }

    @Test
    @Transactional
    void countByBoard() throws Exception {
        commentRepository.saveAndFlush(comment);

        restMockMvc.perform(get("/api/comments/count/board/{boardId}", board.getId())
                .with(user(user.getLogin()))) // Authenticate the request
            .andExpect(status().isOk())
            .andExpect(content().string("1"));
    }
}
