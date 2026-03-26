package com.daangcool.stack.service.board;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.service.dto.BoardDTO;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
@WithMockUser(username = "test_user")
class BoardServiceIT {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Board board;
    private CommonCodeDetail noticeBoardType;

    @BeforeEach
    void initTest() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll();
        commonCodeGroupRepository.deleteAll();
        entityManager.flush();

        user = new User();
        user.setLogin("test_user");
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
        boardTypeGroup = commonCodeGroupRepository.saveAndFlush(boardTypeGroup);

        noticeBoardType = new CommonCodeDetail();
        noticeBoardType.setCode("NOTICE");
        noticeBoardType.setName("공지사항");
        noticeBoardType.setGroup(boardTypeGroup);
        noticeBoardType = commonCodeDetailRepository.saveAndFlush(noticeBoardType);

        board = new Board();
        board.setTitle("Initial Title");
        board.setContent("Initial Content");
        board.setUser(user);
        board.setBoardType(noticeBoardType);
        board = boardRepository.saveAndFlush(board);
    }

    @Test
    void updateBoard_WithTags_ShouldSyncCorrectly() {
        // Given: Create board with one tag first
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setTitle("Updated Title");
        dto.setContent("Updated Content");
        dto.setTags(Arrays.asList("Tag1", "Tag2"));
        
        // When: First update adding tags
        BoardDTO result1 = boardService.update(board.getId(), dto);
        assertThat(result1.getTags()).containsExactlyInAnyOrder("Tag1", "Tag2");

        // When: Second update removing one tag and adding a new one
        dto.setTags(Arrays.asList("Tag2", "Tag3"));
        BoardDTO result2 = boardService.update(board.getId(), dto);

        // Then: Result should be updated
        assertThat(result2.getTags()).containsExactlyInAnyOrder("Tag2", "Tag3");
        assertThat(result2.getTags()).doesNotContain("Tag1");
    }

    @Test
    void updateBoard_ShouldNotThrowLazyInitializationException() {
        // This test simulates the concurrent flow and checks for LazyInitializationException
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setTags(Arrays.asList("NewTag"));

        // When/Then: Should not throw any exception
        BoardDTO updatedDto = boardService.update(board.getId(), dto);
        assertThat(updatedDto.getTags()).contains("NewTag");
    }
}
