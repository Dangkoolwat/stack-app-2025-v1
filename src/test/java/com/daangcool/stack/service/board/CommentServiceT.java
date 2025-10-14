package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.Comment;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.CommentRepository;
import com.daangcool.stack.service.dto.CommentDTO;
import com.daangcool.stack.service.mapper.CommentMapper;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: CommentService
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceT {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Board board;
    private Comment comment;
    private CommentDTO commentDTO;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 공통 객체들을 초기화합니다.
        user = new User();
        user.setId(1L);

        board = new Board();
        board.setId(1L);

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("테스트 댓글");
        comment.setBoard(board);
        comment.setUser(user);

        commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setBoardId(board.getId());
        commentDTO.setUserId(user.getId());
        commentDTO.setContent("테스트 댓글");

        // 캐시 관련 Mock 설정
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    /**
     * 댓글 저장 테스트 (성공 케이스)
     */
    @Test
    void save_ValidComment_ShouldSaveAndReturnDTO() {
        // given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDTO);

        // when
        CommentDTO result = commentService.save(commentDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(commentDTO.getContent());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(cacheManager, atLeastOnce()).getCache(anyString());
    }

    /**
     * 댓글 저장 테스트 (실패 케이스 - 내용 없음)
     */
    @Test
    void save_EmptyContent_ShouldThrowException() {
        // given
        commentDTO.setContent(""); // 댓글 내용을 비웁니다.

        // when & then
        assertThatThrownBy(() -> commentService.save(commentDTO))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("댓글 내용은 비어 있을 수 없습니다");
    }

    /**
     * 게시글별 댓글 목록 조회 테스트
     */
    @Test
    void findAllByBoard_ShouldReturnDtoList() {
        // given
        when(commentRepository.findAllByBoard_IdOrderByIdAsc(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDTO);

        // when
        List<CommentDTO> results = commentService.findAllByBoard(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo(comment.getContent());
    }

    /**
     * 단일 댓글 조회 테스트
     */
    @Test
    void findOne_ShouldReturnDto() {
        // given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentDTO);

        // when
        Optional<CommentDTO> result = commentService.findOne(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(comment.getId());
    }

    /**
     * 댓글 수정 테스트
     */
    @Test
    void update_ShouldUpdateAndReturnDTO() {
        // given
        CommentDTO updateRequest = new CommentDTO();
        updateRequest.setContent("수정된 댓글");

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));
        when(commentMapper.toDto(any(Comment.class))).thenAnswer(i -> {
            Comment c = i.getArgument(0);
            CommentDTO dto = new CommentDTO();
            dto.setId(c.getId());
            dto.setContent(c.getContent());
            return dto;
        });

        // when
        CommentDTO result = commentService.update(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("수정된 댓글");
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    /**
     * 댓글 삭제 테스트 (Soft Delete)
     */
    @Test
    void delete_ShouldCallSoftDelete() {
        // given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.softDelete(anyLong(), any())).thenReturn(1);

        // when
        commentService.delete(1L, "테스트 삭제");

        // then
        verify(commentRepository, times(1)).softDelete(1L, "테스트 삭제");
    }
}
