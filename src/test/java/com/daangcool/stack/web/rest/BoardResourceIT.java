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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트: BoardResource (API 계층)
 */
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser(username = "user", roles = "USER")
class BoardResourceIT {

    @Autowired
    private MockMvc restMockMvc;

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

        User userToSave = new User();
        userToSave.setLogin("user");
        userToSave.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userToSave.setActivated(true);
        userToSave.setEnabled(true);
        userToSave.setAccountNonExpired(true);
        userToSave.setAccountNonLocked(true);
        userToSave.setCredentialsNonExpired(true);
        userRepository.saveAndFlush(userToSave);

        // CommonCodeGroup 생성 및 저장
        CommonCodeGroup boardTypeGroup = new CommonCodeGroup();
        boardTypeGroup.setGroupCode("BOARD_TYPE");
        boardTypeGroup.setGroupName("게시판 유형");
        commonCodeGroupRepository.saveAndFlush(boardTypeGroup);

        // CommonCodeDetail 생성 및 저장
        CommonCodeDetail noticeBoardTypeSaved = new CommonCodeDetail();
        noticeBoardTypeSaved.setCode("NOTICE");
        noticeBoardTypeSaved.setName("공지사항");
        noticeBoardTypeSaved.setGroup(boardTypeGroup);
        commonCodeDetailRepository.saveAndFlush(noticeBoardTypeSaved);

        entityManager.clear();

        // 분리된 엔티티들을 다시 조회하여 영속성 컨텍스트에 관리되는 상태로 만듭니다.
        this.user = userRepository.findOneByLogin("user").get();
        CommonCodeGroup managedBoardTypeGroup = commonCodeGroupRepository.findById("BOARD_TYPE").get();
        this.noticeBoardType = commonCodeDetailRepository.findOneByGroupGroupCodeAndCodeAndDeletedIsFalse(managedBoardTypeGroup.getGroupCode(), "NOTICE").get();

        board = new Board();
        board.setTitle("Test Board");
        board.setContent("Test Content");
        board.setDeleted(false);
        board.setUser(this.user);
        board.setBoardType(this.noticeBoardType);
        this.board = boardRepository.saveAndFlush(board);
    }

    @Test
    @Transactional
    void deleteBoard_ShouldSoftDeleteSuccessfully() throws Exception {
        // given : 테스트용 게시글이 존재함
        assertThat(boardRepository.findById(board.getId())).isPresent();

        // when : DELETE 요청 수행
        restMockMvc.perform(delete("/api/boards/{id}", board.getId())
                .param("reason", "Test reason"))
            .andExpect(status().isNoContent());

        // Hibernate 캐시 클리어 (DB에서 실제 상태 확인을 위해)
        entityManager.flush();
        entityManager.clear();

        // then : native SQL 로 직접 조회 (Hibernate @Where 필터 우회)
        Board deletedBoard = (Board) entityManager
            .createNativeQuery("SELECT * FROM STACK_BOARD WHERE ID = ?", Board.class)
            .setParameter(1, board.getId())
            .getSingleResult();

        // soft delete 플래그 검증
        assertThat(deletedBoard).isNotNull();
        assertThat(deletedBoard.isDeleted()).isTrue();
    }
}
