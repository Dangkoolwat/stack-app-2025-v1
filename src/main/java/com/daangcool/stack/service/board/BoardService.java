package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardAdminRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.service.mapper.BoardMapper;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import com.daangcool.stack.web.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BoardService
 * -----------------------------------------------------------
 * 게시판(Board) 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 *
 * 주요 기능:
 *  - 게시글 등록, 수정, 조회, 삭제 (Soft Delete)
 *  - 검색 및 페이징 처리
 *  - 공지글 관리 및 통계 기능
 *
 * 설계 원칙:
 *  - 트랜잭션 단위는 서비스 계층에서 정의합니다.
 *  - 읽기 전용 트랜잭션(@Transactional(readOnly=true))과 쓰기 트랜잭션을 명확히 구분합니다.
 *  - 모든 데이터 접근은 Repository 계층을 통해 수행됩니다.
 * -----------------------------------------------------------
 */
@Service
@Transactional
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private static final String ENTITY_NAME = "board";

    // -----------------------------------------------------
    // 캐시 이름 상수 (CacheConfiguration의 이름과 일치해야 함)
    // -----------------------------------------------------
    public static final String CACHE_BOARD_BY_ID = "BOARD_BY_ID";
    public static final String CACHE_BOARD_PAGE = "BOARD_PAGE";
    public static final String CACHE_BOARD_SEARCH = "BOARD_SEARCH";
    public static final String CACHE_BOARD_NOTICE_LIST = "BOARD_NOTICE_LIST";
    public static final String CACHE_BOARD_COUNT_TOTAL = "BOARD_COUNT_TOTAL";
    public static final String CACHE_BOARD_COUNT_BY_USER = "BOARD_COUNT_BY_USER";

    private final BoardRepository boardRepository;
    private final BoardAdminRepository boardAdminRepository; // BoardAdminRepository 추가
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;
    private final CacheManager cacheManager;

    public BoardService(BoardRepository boardRepository, BoardAdminRepository boardAdminRepository, UserRepository userRepository, BoardMapper boardMapper, CacheManager cacheManager) {
        this.boardRepository = boardRepository;
        this.boardAdminRepository = boardAdminRepository; // 주입
        this.userRepository = userRepository;
        this.boardMapper = boardMapper;
        this.cacheManager = cacheManager;
    }



    // -----------------------------------------------------
    // 게시글 등록
    // -----------------------------------------------------
    public BoardDTO save(BoardDTO dto) {
        log.debug("Request to save Board : {}", dto);

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestAlertException("게시글 제목은 비어 있을 수 없습니다.", ENTITY_NAME, "board.emptyTitle");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BadRequestAlertException("게시글 내용은 비어 있을 수 없습니다.", ENTITY_NAME, "board.emptyContent");
        }
        if (dto.getUserId() == null) {
            throw new BadRequestAlertException("작성자 정보가 필요합니다.", ENTITY_NAME, "board.noAuthor");
        }

        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("작성자를 찾을 수 없습니다. ID=" + dto.getUserId()));

        Board board = boardMapper.toEntity(dto);
        board.setUser(user);

        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);

        Cache byId = cacheManager.getCache(CACHE_BOARD_BY_ID);
        if (byId != null) byId.put(saved.getId(), boardMapper.toDto(saved));

        return boardMapper.toDto(saved);
    }

    // -----------------------------------------------------
    // 게시글 목록 조회 (페이징, 캐시 활용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public Page<BoardDTO> findAll(int page, int size) {
        String key = "page:" + page + ":size:" + size;
        Cache pageCache = cacheManager.getCache(CACHE_BOARD_PAGE);
        if (pageCache != null) {
            Page<BoardDTO> cached = pageCache.get(key, Page.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit page {}", key);
                return cached;
            }
        }

        Page<BoardDTO> result = boardRepository.findAllActive(PageRequest.of(page, size))
            .map(boardMapper::toDto);

        if (pageCache != null && !result.isEmpty()) pageCache.put(key, result);
        return result;
    }

    // -----------------------------------------------------
    // 단건 조회 (캐시 활용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public BoardDTO findOne(Long id) {
        Cache byId = cacheManager.getCache(CACHE_BOARD_BY_ID);
        if (byId != null) {
            BoardDTO cached = byId.get(id, BoardDTO.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit board id={}", id);
                return cached;
            }
        }

        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID=" + id));
        BoardDTO dto = boardMapper.toDto(board);

        if (byId != null) byId.put(id, dto);
        return dto;
    }

    // -----------------------------------------------------
    // 게시글 수정
    // -----------------------------------------------------
    public BoardDTO update(Long id, BoardDTO dto) {
        log.debug("Request to update Board : {}", id);

        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("수정할 게시글을 찾을 수 없습니다. ID=" + id));

        if (board.isDeleted()) {
            throw new BadRequestAlertException("삭제된 게시글은 수정할 수 없습니다.", ENTITY_NAME, "board.deleted");
        }

        if (dto.getTitle() != null) board.setTitle(dto.getTitle());
        if (dto.getContent() != null) board.setContent(dto.getContent());
        if (dto.getNotice() != null) board.setNotice(dto.getNotice());

        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);
        return boardMapper.toDto(saved);
    }

    // -----------------------------------------------------
    // 게시글 삭제 (Soft Delete)
    // -----------------------------------------------------
    public void delete(Long id, String reason) {
        log.debug("Request to soft delete Board : {}", id);
        Board before = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id));

        int updated = boardRepository.softDelete(id, reason);
        if (updated == 0) {
            throw new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id);
        }

        clearBoardCaches(before);
    }

    // -----------------------------------------------------
    // 게시글 조회수 증가
    // -----------------------------------------------------
    public void increaseViewCount(Long id) {
        log.debug("Request to increase view count for Board : {}", id);
        Board before = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("조회수 증가 대상 게시글을 찾을 수 없습니다. ID=" + id));

        boardRepository.increaseViewCount(id);
        clearBoardCaches(before);
    }

    // -----------------------------------------------------
    // 게시글 검색 (캐시 활용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public Page<BoardDTO> search(String keyword, int page, int size) {
        String key = "q:" + (keyword == null ? "" : keyword.toLowerCase()) + ":p:" + page + ":s:" + size;
        Cache searchCache = cacheManager.getCache(CACHE_BOARD_SEARCH);
        if (searchCache != null) {
            Page<BoardDTO> cached = searchCache.get(key, Page.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit search {}", key);
                return cached;
            }
        }

        Page<BoardDTO> result = boardRepository.searchByKeyword(keyword, PageRequest.of(page, size))
            .map(boardMapper::toDto);

        if (searchCache != null && !result.isEmpty()) searchCache.put(key, result);
        return result;
    }

    // -----------------------------------------------------
    // 공지글 목록 (캐시 활용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public List<BoardDTO> findAllNotices() {
        String key = "notice:all";
        Cache noticeCache = cacheManager.getCache(CACHE_BOARD_NOTICE_LIST);
        if (noticeCache != null) {
            List<BoardDTO> cached = noticeCache.get(key, List.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit notice list");
                return cached;
            }
        }

        List<BoardDTO> list = boardRepository.findAllNotices().stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());

        if (noticeCache != null && !list.isEmpty()) noticeCache.put(key, list);
        return list;
    }

    // -----------------------------------------------------
    // 사용자별 게시글 목록
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public List<BoardDTO> findByUser(Long userId) {
        log.debug("Request to get Boards by User : {}", userId);
        return boardRepository.findByUser_Id(userId).stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    // 게시판 유형별 게시글 목록
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public List<BoardDTO> findByBoardType(String code) {
        log.debug("Request to get Boards by BoardType : {}", code);
        return boardRepository.findByBoardType_Code(code).stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    // 게시글 통계
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        String key = "user:" + userId;
        Cache cnt = cacheManager.getCache(CACHE_BOARD_COUNT_BY_USER);
        if (cnt != null) {
            Long cached = cnt.get(key, Long.class);
            if (cached != null) return cached;
        }

        long val = boardRepository.countByUserId(userId);
        if (cnt != null) cnt.put(key, val);
        return val;
    }

    @Transactional(readOnly = true)
    public long countAll() {
        String key = "count:total";
        Cache cnt = cacheManager.getCache(CACHE_BOARD_COUNT_TOTAL);
        if (cnt != null) {
            Long cached = cnt.get(key, Long.class);
            if (cached != null) return cached;
        }

        long val = boardRepository.countActiveBoards();
        if (cnt != null) cnt.put(key, val);
        return val;
    }

    // -----------------------------------------------------
    // 삭제된 게시글 관리 (관리자)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public List<BoardDTO> findAllDeleted() {
        return boardAdminRepository.findAllDeleted().stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    public void restore(Long id) {
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("복구할 게시글을 찾을 수 없습니다. ID=" + id));

        board.setDeleted(false);
        board.setDescription(null);
        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);
    }

    public void hardDelete(Long id) {
        Board before = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id));

        boardRepository.delete(before);
        clearBoardCaches(before);
    }

    // -----------------------------------------------------
    // 공지글 상태 전환
    // -----------------------------------------------------
    public void toggleNotice(Long id, boolean notice) {
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("공지 전환 대상 게시글을 찾을 수 없습니다. ID=" + id));

        board.setNotice(notice);
        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);
    }

    // -----------------------------------------------------
    // 캐시 클리어 유틸리티
    // -----------------------------------------------------
    /**
     * 게시글 관련 캐시를 무효화합니다.
     *
     * - 단건 캐시: 게시글 ID 기준 제거
     * - 목록 캐시: 전체 초기화 (page, search, notice 등)
     * - 통계 캐시: 전체/사용자 기준 캐시 제거
     */
    private void clearBoardCaches(Board board) {
        if (board == null) return;
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_BY_ID)).evictIfPresent(board.getId());
            Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_PAGE)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_SEARCH)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_NOTICE_LIST)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_COUNT_TOTAL)).evictIfPresent("count:total");

            if (board.getUser() != null) {
                Objects.requireNonNull(cacheManager.getCache(CACHE_BOARD_COUNT_BY_USER))
                    .evictIfPresent("user:" + board.getUser().getId());
            }

            log.debug("[BOARD CACHE] Cleared caches for board id={}", board.getId());
        } catch (Exception e) {
            log.warn("[BOARD CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }
}
