package com.daangcool.stack.repository.board;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
class BoardRepositoryIT {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private BoardRepository boardRepository;



    private User user;
    private CommonCodeDetail boardType;
    private Board board;

    /** 테스트용 User 생성 */
    public static User createUser(EntityManager em) {
        User user = new User();
        user.setLogin("test_user");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60)); // Changed to 60-character random password
        user.setActivated(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }

    public static CommonCodeGroup createBoardGroup(EntityManager em) {
        CommonCodeGroup group = new CommonCodeGroup();
        group.setGroupCode("BOARD_TYPE");  // 그룹코드
        group.setGroupName("게시판유형");
        group.setDescription("게시판 분류 그룹");
        return group;
    }

    public static CommonCodeDetail createBoardType(EntityManager em, CommonCodeGroup group) {
        CommonCodeDetail detail = new CommonCodeDetail();
        detail.setCode("FREE");
        detail.setName("자유게시판");
        detail.setDescription("테스트용 게시판 유형");
        detail.setGroup(group);   //  필수! group 연결
        return detail;
    }

    /** 🔹 테스트용 Board 생성 */
    public static Board createBoard(EntityManager em, User user, CommonCodeDetail boardType) {
        Board board = new Board();
        board.setTitle("Soft Delete Test Board");
        board.setContent("테스트 게시글 내용입니다.");
        board.setUser(user);
        board.setBoardType(boardType);
        board.setDeleted(false);
        board.setNotice(false);
        return board;
    }

    @BeforeEach
    void initTest() {
        // 그룹 먼저 저장
        CommonCodeGroup group = commonCodeGroupRepository.saveAndFlush(createBoardGroup(em));

        //  사용자 생성
        user = userRepository.saveAndFlush(createUser(em));

        //  게시판 유형 생성 (그룹 연결 필수)
        boardType = commonCodeDetailRepository.saveAndFlush(createBoardType(em, group));

        //  게시글 생성
        board = boardRepository.saveAndFlush(createBoard(em, user, boardType));
    }

    @Test
    void softDelete_ShouldMarkBoardAsDeleted() {
        Long id = board.getId();

        // when
        board.setDeleted(true);
        boardRepository.saveAndFlush(board);
        em.flush();
        em.clear();

        // then — native query로 Hibernate @Where 필터 우회
        Board deletedBoard = (Board) em.createNativeQuery(
                "SELECT * FROM STACK_BOARD WHERE ID = ?", Board.class)
            .setParameter(1, id)
            .getSingleResult();

        assertThat(deletedBoard).isNotNull();
        assertThat(deletedBoard.isDeleted()).isTrue();
    }
}
