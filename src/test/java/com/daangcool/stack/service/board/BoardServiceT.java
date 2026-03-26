package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.service.mapper.BoardMapper;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: BoardService
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceT {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardMapper boardMapper;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private com.daangcool.stack.repository.board.UploadRepository uploadRepository;
    @Mock
    private com.daangcool.stack.repository.board.TagRepository tagRepository;
    @Mock
    private com.daangcool.stack.repository.board.BoardTagRepository boardTagRepository;
    @Mock
    private com.daangcool.stack.repository.board.CommentRepository commentRepository;
    @Mock
    private UploadService uploadService;
    @Mock
    private com.daangcool.stack.security.ResourceAuthorizationService resourceAuthorizationService;

    @InjectMocks
    private BoardService boardService;

    private User user;
    private Board board;
    private BoardDTO boardDTO;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 공통 객체들을 초기화합니다.
        user = new User();
        user.setId(1L);
        user.setLogin("testuser");

        board = new Board();
        board.setId(1L);
        board.setTitle("테스트 제목");
        board.setContent("테스트 내용");
        board.setUser(user);

        boardDTO = new BoardDTO();
        boardDTO.setId(1L);
        boardDTO.setTitle("테스트 제목");
        boardDTO.setContent("테스트 내용");
        boardDTO.setUserId(user.getId());

        // 캐시 관련 Mock 설정 (NullPointerException 방지)
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "password"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 게시글 저장 테스트 (성공 케이스)
     */
    @Test
    void save_ValidBoard_ShouldSaveAndReturnDTO() {
        // given
        when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));
        when(boardMapper.toEntity(any(BoardDTO.class))).thenReturn(board);
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardMapper.toDto(any(Board.class))).thenReturn(boardDTO);

        // when
        BoardDTO result = boardService.save(boardDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(boardDTO.getTitle());
        verify(boardRepository, times(1)).save(any(Board.class));
        verify(cacheManager, atLeastOnce()).getCache(anyString());
    }

    /**
     * 게시글 저장 테스트 (실패 케이스 - 작성자 없음)
     */
    @Test
    void save_UserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardService.save(boardDTO))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    /**
     * 게시글 저장 테스트 (실패 케이스 - 제목 없음)
     */
    @Test
    void save_NoTitle_ShouldThrowException() {
        // given
        boardDTO.setTitle(""); // 제목을 비웁니다.
        when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> boardService.save(boardDTO))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("게시글 제목은 비어 있을 수 없습니다");
    }

    /**
     * 게시글 목록 조회 테스트 (페이징)
     */
    @Test
    void findAll_ShouldReturnPagedDTOs() {
        // given
        Page<Board> page = new PageImpl<>(List.of(board));
        when(boardRepository.findAllActive(any(PageRequest.class))).thenReturn(page);
        when(boardMapper.toDto(any(Board.class))).thenReturn(boardDTO);

        // when
        Page<BoardDTO> result = boardService.findAll(0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(board.getTitle());
    }

    /**
     * 단일 게시글 조회 테스트
     */
    @Test
    void findOne_ShouldReturnDTO() {
        // given
        when(boardRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.of(board));
        when(boardMapper.toDto(any(Board.class))).thenReturn(boardDTO);

        // when
        BoardDTO result = boardService.findOne(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(board.getId());
    }

    /**
     * 게시글 수정 테스트
     */
    @Test
    void update_ShouldUpdateAndReturnDTO() {
        // given
        BoardDTO updateRequest = new BoardDTO();
        updateRequest.setTitle("수정된 제목");
        updateRequest.setTags(List.of("tag1"));

        when(boardRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.of(board));
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(new com.daangcool.stack.domain.board.Tag()));
        when(boardRepository.save(any(Board.class))).thenAnswer(i -> i.getArgument(0)); // 저장 메소드가 받은 인자를 그대로 반환
        when(boardMapper.toDto(any(Board.class))).thenAnswer(i -> {
            Board b = i.getArgument(0);
            BoardDTO dto = new BoardDTO();
            dto.setId(b.getId());
            dto.setTitle(b.getTitle());
            return dto;
        });

        // when
        BoardDTO result = boardService.update(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    /**
     * 게시글 삭제 테스트 (Soft Delete)
     */
    @Test
    void delete_ShouldCallSoftDelete() {
        // given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));
        when(boardRepository.softDelete(anyLong(), anyString())).thenReturn(1);
        // when
        boardService.delete(1L, "테스트 삭제");

        // then
        verify(boardRepository, times(1)).softDelete(1L, "테스트 삭제");
        verify(boardTagRepository, times(1)).softDeleteAllByBoardId(1L, "테스트 삭제");
        verify(commentRepository, times(1)).softDeleteAllByBoardId(1L, "테스트 삭제");
        verify(uploadRepository, times(1)).softDeleteAllByBoardId(1L, "테스트 삭제");
    }
}
