package com.daangcool.stack.config;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.service.board.BoardService;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_BY_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 통합 테스트: CacheConfiguration
 */
@IntegrationTest
class CacheConfigurationIT {

    @Autowired
    private BoardService boardService;

    @SpyBean
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository; // 추가
    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository; // 추가

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Board board;

    @BeforeEach
    @Transactional
    void setUp() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
        commonCodeDetailRepository.deleteAll(); // 추가
        commonCodeGroupRepository.deleteAll(); // 추가
        entityManager.flush();

        User user = new User();
        user.setLogin("cache_user");
        user.setPassword(passwordEncoder.encode(RandomStringUtils.insecure().nextAlphanumeric(60)));
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
        CommonCodeDetail noticeBoardType = new CommonCodeDetail();
        noticeBoardType.setCode("NOTICE");
        noticeBoardType.setName("공지사항");
        noticeBoardType.setGroup(boardTypeGroup);
        commonCodeDetailRepository.saveAndFlush(noticeBoardType);


        // 영속성 컨텍스트를 강제로 비워, 다음 조회가 반드시 DB에서 일어나도록 합니다.
        entityManager.clear();

        // DB에서 방금 저장된 User를 다시 조회하여, 완전히 관리되는 인스턴스를 확보합니다.
        User managedUser = userRepository.findOneByLogin("cache_user").get();
        // DB에서 방금 저장된 CommonCodeDetail을 다시 조회하여, 완전히 관리되는 인스턴스를 확보합니다.
        CommonCodeDetail managedBoardType = commonCodeDetailRepository.findOneByGroupGroupCodeAndCodeAndDeletedIsFalse("BOARD_TYPE", "NOTICE").get();


        board = new Board();
        board.setTitle("Cache Test Board");
        board.setContent("Content");
        board.setUser(managedUser);
        board.setBoardType(managedBoardType); // boardType 설정
        board = boardRepository.saveAndFlush(board);

        Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_BY_ID)).clear();
    }

    /**
     * BoardService의 findOne 메소드에 대한 캐시 동작을 검증합니다.
     */
    @Test
    @Transactional
    void testBoardServiceCaching() {
        boardService.findOne(board.getId());
        verify(boardRepository, times(1)).findById(board.getId());

        boardService.findOne(board.getId());
        verify(boardRepository, times(1)).findById(board.getId());

        Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_BY_ID)).clear();

        boardService.findOne(board.getId());
        verify(boardRepository, times(2)).findById(board.getId());
    }
}
