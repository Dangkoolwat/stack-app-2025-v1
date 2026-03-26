package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.BoardTag;
import com.daangcool.stack.domain.board.Comment;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.BoardTagRepository;
import com.daangcool.stack.repository.board.CommentRepository;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.service.board.UploadService;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트: Board aggregate 관리자 생명주기
 */
@AutoConfigureMockMvc
@IntegrationTest
class BoardAggregateAdminResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardTagRepository boardTagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private EntityManager entityManager;

    private Board activeBoard;
    private Board deletedBoard;
    private Tag activeTag;
    private Tag deletedTag;
    private Upload activeUpload;
    private Upload deletedUpload;

    @BeforeEach
    void initTest() {
        boardTagRepository.deleteAll();
        commentRepository.deleteAll();
        uploadRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll();
        commonCodeGroupRepository.deleteAll();
        entityManager.flush();

        User owner = new User();
        owner.setLogin("aggregate_owner");
        owner.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        owner.setActivated(true);
        owner.setEnabled(true);
        owner.setAccountNonExpired(true);
        owner.setAccountNonLocked(true);
        owner.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(owner);

        CommonCodeGroup boardTypeGroup = new CommonCodeGroup();
        boardTypeGroup.setGroupCode("BOARD_TYPE");
        boardTypeGroup.setGroupName("게시판 유형");
        commonCodeGroupRepository.saveAndFlush(boardTypeGroup);

        CommonCodeDetail boardType = new CommonCodeDetail();
        boardType.setCode("NOTICE");
        boardType.setName("공지사항");
        boardType.setGroup(boardTypeGroup);
        commonCodeDetailRepository.saveAndFlush(boardType);

        entityManager.clear();

        User managedOwner = userRepository.findOneByLogin("aggregate_owner").orElseThrow();
        CommonCodeDetail managedBoardType = commonCodeDetailRepository
            .findOneByGroupGroupCodeAndCodeAndDeletedIsFalse("BOARD_TYPE", "NOTICE")
            .orElseThrow();

        activeBoard = new Board();
        activeBoard.setTitle("활성 aggregate 게시글");
        activeBoard.setContent("활성 aggregate 내용");
        activeBoard.setUser(managedOwner);
        activeBoard.setBoardType(managedBoardType);
        activeBoard = boardRepository.saveAndFlush(activeBoard);

        deletedBoard = new Board();
        deletedBoard.setTitle("삭제 aggregate 게시글");
        deletedBoard.setContent("삭제 aggregate 내용");
        deletedBoard.setUser(managedOwner);
        deletedBoard.setBoardType(managedBoardType);
        deletedBoard.setDeleted(true);
        deletedBoard = boardRepository.saveAndFlush(deletedBoard);

        activeTag = new Tag();
        activeTag.setName("active-tag");
        activeTag.setUsageCount(1L);
        activeTag = tagRepository.saveAndFlush(activeTag);

        deletedTag = new Tag();
        deletedTag.setName("deleted-tag");
        deletedTag.setUsageCount(0L);
        deletedTag = tagRepository.saveAndFlush(deletedTag);

        BoardTag activeBoardTag = new BoardTag();
        activeBoardTag.setBoard(activeBoard);
        activeBoardTag.setTag(activeTag);
        boardTagRepository.saveAndFlush(activeBoardTag);

        BoardTag deletedBoardTag = new BoardTag();
        deletedBoardTag.setBoard(deletedBoard);
        deletedBoardTag.setTag(deletedTag);
        deletedBoardTag.setDeleted(true);
        deletedBoardTag.setDescription("게시글 삭제 연쇄 처리");
        boardTagRepository.saveAndFlush(deletedBoardTag);

        Comment activeComment = new Comment();
        activeComment.setBoard(activeBoard);
        activeComment.setUser(managedOwner);
        activeComment.setContent("활성 댓글");
        commentRepository.saveAndFlush(activeComment);

        Comment deletedComment = new Comment();
        deletedComment.setBoard(deletedBoard);
        deletedComment.setUser(managedOwner);
        deletedComment.setContent("삭제 댓글");
        deletedComment.setDeleted(true);
        deletedComment.setDescription("게시글 삭제 연쇄 처리");
        commentRepository.saveAndFlush(deletedComment);

        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "aggregate.png",
            "image/png",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 'A', 'G', 'G'}
        );

        activeUpload = uploadService.saveUpload(multipartFile, "AGG_ACTIVE", false);
        activeUpload.setBoard(activeBoard);
        activeUpload = uploadRepository.saveAndFlush(activeUpload);

        deletedUpload = uploadService.saveUpload(multipartFile, "AGG_DELETED", false);
        deletedUpload.setBoard(deletedBoard);
        deletedUpload.setDeleted(true);
        deletedUpload.setDescription("게시글 삭제 연쇄 처리");
        deletedUpload = uploadRepository.saveAndFlush(deletedUpload);
    }

    @Test
    @Transactional
    void restoreBoard_ShouldRestoreAggregateDescendants() throws Exception {
        restMockMvc.perform(put("/api/admin/boards/restore/{id}", deletedBoard.getId())
                .with(user("admin").authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Board restoredBoard = boardRepository.findById(deletedBoard.getId()).orElseThrow();
        assertThat(restoredBoard.isDeleted()).isFalse();

        assertThat(commentRepository.findAllByBoard_IdOrderByIdAsc(deletedBoard.getId()))
            .extracting(Comment::isDeleted)
            .containsOnly(false);

        assertThat(boardTagRepository.findAllByBoard_IdOrderByIdAsc(deletedBoard.getId()))
            .extracting(BoardTag::isDeleted)
            .containsOnly(false);

        assertThat(uploadRepository.findAllByBoard_IdOrderByIdAsc(deletedBoard.getId()))
            .extracting(Upload::isDeleted)
            .containsOnly(false);

        Tag restoredTag = tagRepository.findById(deletedTag.getId()).orElseThrow();
        assertThat(restoredTag.getUsageCount()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void hardDeleteBoard_ShouldDeleteAggregateDescendantsAndAdjustUsageCount() throws Exception {
        restMockMvc.perform(delete("/api/admin/boards/hard-delete/{id}", activeBoard.getId())
                .with(user("admin").authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertThat(boardRepository.findById(activeBoard.getId())).isEmpty();
        assertThat(commentRepository.findAllByBoard_IdOrderByIdAsc(activeBoard.getId())).isEmpty();
        assertThat(boardTagRepository.findAllByBoard_IdOrderByIdAsc(activeBoard.getId())).isEmpty();
        assertThat(uploadRepository.findAllByBoard_IdOrderByIdAsc(activeBoard.getId())).isEmpty();

        Tag updatedTag = tagRepository.findById(activeTag.getId()).orElseThrow();
        assertThat(updatedTag.getUsageCount()).isEqualTo(0L);
    }
}
