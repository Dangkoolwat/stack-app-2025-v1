package com.daangcool.stack.config;

import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.service.board.BoardService;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.service.mapper.BoardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_BY_ID;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_PAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트 (방법 3 적용): CacheConfiguration 캐시 로직 검증
 * --------------------------------------------------------------
 * - 실제 DB 접근 없이 BoardService의 캐시 동작만 검증
 * - SpyBean 제거 (Spring Context 미사용)
 * - SimpleCacheManager로 캐시 구성
 * - Mockito 기반 가벼운 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CacheConfigurationIT {

    @Mock private BoardRepository boardRepository;
    @Mock private BoardMapper boardMapper;
    @Mock private UserRepository userRepository;

    private SimpleCacheManager cacheManager;

    @InjectMocks
    private BoardService boardService;

    private Board board;

    @BeforeEach
    void setUp() {
        cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
            new ConcurrentMapCache(CACHE_BOARD_BY_ID),
            new ConcurrentMapCache(CACHE_BOARD_PAGE)
        ));
        cacheManager.initializeCaches();

        boardService = new BoardService(
            boardRepository,
            userRepository,
            boardMapper,
            cacheManager,
            mock(com.daangcool.stack.repository.board.UploadRepository.class),
            mock(com.daangcool.stack.repository.board.TagRepository.class),
            mock(com.daangcool.stack.repository.board.BoardTagRepository.class)
        );

        board = new Board();
        board.setId(1L);
        board.setTitle("Cache Test Board");

        lenient().when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        lenient().when(boardMapper.toDto(any(Board.class))).thenAnswer(inv -> {
            var b = (Board) inv.getArgument(0);
            var dto = new BoardDTO();
            dto.setId(b.getId());
            dto.setTitle(b.getTitle());
            return dto;
        });
        lenient().when(boardRepository.softDelete(anyLong(), anyString())).thenReturn(1);
    }

    @Test
    void findOne_ShouldUseCacheOnSecondCall() {
        // 첫 번째 호출 → Repository 접근
        boardService.findOne(1L);
        verify(boardRepository, times(1)).findById(1L);

        // 두 번째 호출 → 캐시 사용
        boardService.findOne(1L);
        verify(boardRepository, times(1)).findById(1L); // DB 재호출 없음

        // 캐시에 값이 저장되었는지 검증
        Cache cache = cacheManager.getCache(CACHE_BOARD_BY_ID);
        assertThat(cache).isNotNull();
        assertThat(cache.get(1L)).isNotNull();
    }

    @Test
    void findAll_ShouldCachePageResults() {
        // given
        BoardDTO dto = new BoardDTO();
        dto.setId(1L);
        dto.setTitle("Page Test Board");

        when(boardRepository.findAllActive(any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(board)));
        when(boardMapper.toDto(any(Board.class))).thenReturn(dto);

        // 첫 호출 → DB 접근
        boardService.findAll(0, 10);
        verify(boardRepository, times(1)).findAllActive(any(PageRequest.class));

        // 두 번째 호출 → 캐시 사용
        boardService.findAll(0, 10);
        verify(boardRepository, times(1)).findAllActive(any(PageRequest.class));

        Cache cache = cacheManager.getCache(CACHE_BOARD_PAGE);
        assertThat(cache).isNotNull();
        assertThat(cache.get("page:0:size:10")).isNotNull();
    }

    @Test
    void clearBoardCaches_ShouldEvictEntries() {
        // 캐시 채우기
        boardService.findOne(1L);
        Cache cache = cacheManager.getCache(CACHE_BOARD_BY_ID);
        assertThat(cache.get(1L)).isNotNull();

        // 삭제 시 캐시 클리어 로직 호출됨
        boardService.delete(board.getId(), "clear cache test");
        assertThat(cache.get(1L)).isNull();
    }
}
