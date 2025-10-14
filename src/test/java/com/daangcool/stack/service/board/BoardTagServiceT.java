package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.BoardTag;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.BoardTagRepository;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.service.dto.BoardTagDTO;
import com.daangcool.stack.service.mapper.BoardTagMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: BoardTagService
 */
@ExtendWith(MockitoExtension.class)
class BoardTagServiceT {

    @Mock
    private BoardTagRepository boardTagRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private BoardTagMapper boardTagMapper;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private BoardTagService boardTagService;

    private Board board;
    private Tag tag;
    private BoardTag boardTag;
    private BoardTagDTO boardTagDTO;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 공통 객체들을 초기화합니다.
        board = new Board();
        board.setId(1L);

        tag = new Tag();
        tag.setId(1L);

        boardTag = new BoardTag();
        boardTag.setId(1L);
        boardTag.setBoard(board);
        boardTag.setTag(tag);

        boardTagDTO = new BoardTagDTO();
        boardTagDTO.setId(1L);
        boardTagDTO.setBoardId(board.getId());
        boardTagDTO.setTagId(tag.getId());

        // 캐시 관련 Mock 설정
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    /**
     * 게시글에 태그 추가 테스트 (성공 케이스)
     */
    @Test
    void addTagToBoard_Valid_ShouldCreateRelation() {
        // given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        when(boardTagRepository.existsByBoard_IdAndTag_Id(anyLong(), anyLong())).thenReturn(false);
        when(boardTagRepository.save(any(BoardTag.class))).thenReturn(boardTag);
        when(boardTagMapper.toDto(any(BoardTag.class))).thenReturn(boardTagDTO);

        // when
        BoardTagDTO result = boardTagService.addTagToBoard(1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBoardId()).isEqualTo(board.getId());
        assertThat(result.getTagId()).isEqualTo(tag.getId());
        verify(boardTagRepository, times(1)).save(any(BoardTag.class));
        verify(cacheManager, atLeastOnce()).getCache(anyString());
    }

    /**
     * 게시글에 태그 추가 테스트 (실패 케이스 - 이미 관계 존재)
     */
    @Test
    void addTagToBoard_AlreadyExists_ShouldThrowException() {
        // given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        when(boardTagRepository.existsByBoard_IdAndTag_Id(anyLong(), anyLong())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> boardTagService.addTagToBoard(1L, 1L))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("이미 게시글에 연결된 태그입니다.");
    }

    /**
     * 게시글에 태그 추가 테스트 (실패 케이스 - 게시글 없음)
     */
    @Test
    void addTagToBoard_BoardNotFound_ShouldThrowException() {
        // given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardTagService.addTagToBoard(99L, 1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    /**
     * 게시글에서 태그 제거 테스트 (Soft Delete)
     */
    @Test
    void removeTagFromBoard_Valid_ShouldCallSoftDelete() {
        // given
        when(boardTagRepository.findAllByBoard_IdOrderByIdAsc(anyLong())).thenReturn(List.of(boardTag));

        // when
        boardTagService.removeTagFromBoard(1L, 1L, "test reason");

        // then
        verify(boardTagRepository, times(1)).softDelete(boardTag.getId(), "test reason");
        verify(cache, atLeastOnce()).clear(); // 관련 캐시가 초기화되는지 검증
    }

    /**
     * 게시글 기준 태그 목록 조회 테스트
     */
    @Test
    void findAllTagsByBoard_ShouldReturnDtoList() {
        // given
        when(boardTagRepository.findAllByBoard_IdOrderByIdAsc(anyLong())).thenReturn(List.of(boardTag));
        when(boardTagMapper.toDto(any(BoardTag.class))).thenReturn(boardTagDTO);

        // when
        List<BoardTagDTO> results = boardTagService.findAllTagsByBoard(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTagId()).isEqualTo(tag.getId());
    }

    /**
     * 태그 기준 게시글 목록 조회 테스트
     */
    @Test
    void findAllBoardsByTag_ShouldReturnDtoList() {
        // given
        when(boardTagRepository.findAllByTag_Id(anyLong())).thenReturn(List.of(boardTag));
        when(boardTagMapper.toDto(any(BoardTag.class))).thenReturn(boardTagDTO);

        // when
        List<BoardTagDTO> results = boardTagService.findAllBoardsByTag(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBoardId()).isEqualTo(board.getId());
    }
}
