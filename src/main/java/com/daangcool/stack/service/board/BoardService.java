package com.daangcool.stack.service.board;

import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.common.exception.EntityNotFoundException;
import com.daangcool.stack.service.softdelete.IncludeDeleted;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.service.mapper.BoardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 *  - 읽기 전용 트랜잭션(@Transactional(readOnly = true))과 쓰기 트랜잭션을 명확히 구분합니다.
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
    public static final String CACHE_BOARD_PAGE = "BOARD_PAGE_V2";
    public static final String CACHE_BOARD_SEARCH = "BOARD_SEARCH";
    public static final String CACHE_BOARD_NOTICE_LIST = "BOARD_NOTICE_LIST";
    public static final String CACHE_BOARD_COUNT_TOTAL = "BOARD_COUNT_TOTAL";
    public static final String CACHE_BOARD_COUNT_BY_USER = "BOARD_COUNT_BY_USER";

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;
    private final CacheManager cacheManager;
    private final com.daangcool.stack.repository.board.UploadRepository uploadRepository;
    private final com.daangcool.stack.repository.board.TagRepository tagRepository;
    private final com.daangcool.stack.repository.board.BoardTagRepository boardTagRepository;

    public BoardService(BoardRepository boardRepository,
                        UserRepository userRepository,
                        BoardMapper boardMapper,
                        CacheManager cacheManager,
                        com.daangcool.stack.repository.board.UploadRepository uploadRepository,
                        com.daangcool.stack.repository.board.TagRepository tagRepository,
                        com.daangcool.stack.repository.board.BoardTagRepository boardTagRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.boardMapper = boardMapper;
        this.cacheManager = cacheManager;
        this.uploadRepository = uploadRepository;
        this.tagRepository = tagRepository;
        this.boardTagRepository = boardTagRepository;
    }

    // -----------------------------------------------------
    /** 게시글 등록 */
    public BoardDTO save(BoardDTO dto) {
        log.debug("Request to save Board : {}", dto);

        if (dto.getUserId() == null) {
            String login = com.daangcool.stack.security.SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new com.daangcool.stack.common.exception.BadRequestAlertException("인증된 사용자를 찾을 수 없습니다.", ENTITY_NAME, "board.unauthorized"));
            User user = userRepository.findOneByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + login));
            dto.setUserId(user.getId());
        }

        validateBoard(dto);

        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("작성자를 찾을 수 없습니다. ID=" + dto.getUserId()));

        Board board = boardMapper.toEntity(dto);
        board.setUser(user);
        Board saved = boardRepository.save(board);
        
        // 태그 및 첨부파일 매핑
        syncTags(saved, dto.getTags());
        syncUploads(saved, dto.getUploads());

        clearBoardCaches(saved);
        BoardDTO result = boardMapper.toDto(saved);
        cacheIfPresent(CACHE_BOARD_BY_ID, saved.getId(), result);

        return result;
    }

    // -----------------------------------------------------
    /** 게시글 목록 조회 (페이징, 캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<BoardDTO> findAll(int page, int size) {
        String key = "page:" + page + ":size:" + size;
        Cache cache = cacheManager.getCache(CACHE_BOARD_PAGE);
        if (cache != null && cache.get(key) != null) {
            com.daangcool.stack.service.dto.PageDTO<BoardDTO> cached = (com.daangcool.stack.service.dto.PageDTO<BoardDTO>) cache.get(key).get();
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit page {}", key);
                return new org.springframework.data.domain.PageImpl<>(cached.getContent(), PageRequest.of(cached.getNumber(), cached.getSize()), cached.getTotalElements());
            }
        }

        Page<BoardDTO> result = boardRepository.findAllActive(PageRequest.of(page, size))
            .map(boardMapper::toDto);

        if (cache != null && !result.isEmpty()) {
            cache.put(key, new com.daangcool.stack.service.dto.PageDTO<>(result.getContent(), result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize()));
        }
        return result;
    }

    // -----------------------------------------------------
    /** 단건 조회 (캐시 활용) */
    @Transactional(readOnly = true)
    public BoardDTO findOne(Long id) {
        Cache cache = cacheManager.getCache(CACHE_BOARD_BY_ID);
        if (cache != null) {
            BoardDTO cached = cache.get(id, BoardDTO.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit board id={}", id);
                return cached;
            }
        }

        Board board = boardRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID=" + id));
        BoardDTO dto = boardMapper.toDto(board);

        cacheIfPresent(CACHE_BOARD_BY_ID, id, dto);
        return dto;
    }

    // -----------------------------------------------------
    /** 게시글 수정 */
    public BoardDTO update(Long id, BoardDTO dto) {
        log.debug("Request to update Board : {}", id);

        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("수정할 게시글을 찾을 수 없습니다. ID=" + id));

        if (board.isDeleted())
            throw new BadRequestAlertException("삭제된 게시글은 수정할 수 없습니다.", ENTITY_NAME, "board.deleted");

        Optional.ofNullable(dto.getTitle()).ifPresent(board::setTitle);
        Optional.ofNullable(dto.getContent()).ifPresent(board::setContent);
        Optional.ofNullable(dto.getNotice()).ifPresent(board::setNotice);
        if (dto.getBoardTypeCode() != null) {
            board.setBoardType(boardMapper.toEntity(dto).getBoardType());
        }

        Board saved = boardRepository.save(board);
        
        // 태그 및 첨부파일 매핑 갱신
        syncTags(saved, dto.getTags());
        syncUploads(saved, dto.getUploads());

        clearBoardCaches(saved);

        // [FIX] attachments, boardTags를 Eager로 다시 로드하여 LazyInitializationException 방지
        Board refreshed = boardRepository.findByIdWithDetails(saved.getId())
            .orElse(saved);
        return boardMapper.toDto(refreshed);
    }

    // -----------------------------------------------------
    /** 태그 동기화 처리 */
    private void syncTags(Board board, List<String> newTagNames) {
        if (newTagNames == null) return;
        
        List<com.daangcool.stack.domain.board.BoardTag> existingBoardTags = boardTagRepository.findAllByBoard_IdOrderByIdAsc(board.getId());
        List<String> existingTagNames = existingBoardTags.stream().map(bt -> bt.getTag().getName()).collect(Collectors.toList());

        // 삭제할 태그 식별 및 처리
        List<com.daangcool.stack.domain.board.BoardTag> toRemove = existingBoardTags.stream()
            .filter(bt -> !newTagNames.contains(bt.getTag().getName()))
            .collect(Collectors.toList());

        for (com.daangcool.stack.domain.board.BoardTag bt : toRemove) {
            boardTagRepository.softDelete(bt.getId(), "게시글 수정 중 태그 제거");
            tagRepository.decreaseUsage(bt.getTag().getId());
        }

        // 추가할 태그 식별 및 처리
        List<String> toAdd = newTagNames.stream()
            .filter(name -> !existingTagNames.contains(name))
            .collect(Collectors.toList());

        for (String tagName : toAdd) {
            com.daangcool.stack.domain.board.Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> {
                    com.daangcool.stack.domain.board.Tag newTag = new com.daangcool.stack.domain.board.Tag();
                    newTag.setName(tagName);
                    newTag.setUsageCount(0L);
                    return tagRepository.save(newTag);
                });
            
            com.daangcool.stack.domain.board.BoardTag bt = new com.daangcool.stack.domain.board.BoardTag();
            bt.setBoard(board);
            bt.setTag(tag);
            boardTagRepository.save(bt);
            tagRepository.increaseUsage(tag.getId());
        }
    }

    /** 업로드(첨부파일) 동기화 처리 */
    private void syncUploads(Board board, List<com.daangcool.stack.service.dto.UploadDTO> uploads) {
        if (uploads == null) return;
        
        for (com.daangcool.stack.service.dto.UploadDTO uploadDto : uploads) {
            if (uploadDto.getId() != null) {
                com.daangcool.stack.domain.board.Upload upload = uploadRepository.findById(uploadDto.getId()).orElse(null);
                if (upload != null && upload.getBoard() == null) { // Board_id가 아직 안 묶여있다면 묶어줌
                    upload.setBoard(board);
                    uploadRepository.save(upload);
                }
            }
        }
    }

    // -----------------------------------------------------
    /** 게시글 삭제 (Soft Delete) */
    public void delete(Long id, String reason) {
        log.debug("Request to soft delete Board : {}", id);
        Board before = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id));

        if (boardRepository.softDelete(id, reason) == 0)
            throw new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id);

        clearBoardCaches(before);
    }

    // -----------------------------------------------------
    /** 게시글 조회수 증가 */
    public void increaseViewCount(Long id) {
        log.debug("Request to increase view count for Board : {}", id);
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("조회수 증가 대상 게시글을 찾을 수 없습니다. ID=" + id));

        boardRepository.increaseViewCount(id);
        clearBoardCaches(board);
    }

    // -----------------------------------------------------
    /** 게시글 검색 (캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<BoardDTO> search(String keyword, int page, int size) {
        String key = "q:" + Optional.ofNullable(keyword).orElse("").toLowerCase() + ":p:" + page + ":s:" + size;
        Cache cache = cacheManager.getCache(CACHE_BOARD_SEARCH);
        if (cache != null) {
            Page<BoardDTO> cached = (Page<BoardDTO>) cache.get(key, Page.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit search {}", key);
                return cached;
            }
        }

        Page<BoardDTO> result = boardRepository.searchByKeyword(keyword, PageRequest.of(page, size))
            .map(boardMapper::toDto);

        if (cache != null && !result.isEmpty()) cache.put(key, result);
        return result;
    }

    // -----------------------------------------------------
    /** 공지글 목록 (캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<BoardDTO> findAllNotices() {
        String key = "notice:all";
        Cache cache = cacheManager.getCache(CACHE_BOARD_NOTICE_LIST);
        if (cache != null) {
            List<BoardDTO> cached = (List<BoardDTO>) cache.get(key, List.class);
            if (cached != null) {
                log.debug("[BOARD CACHE] Hit notice list");
                return cached;
            }
        }

        List<BoardDTO> list = boardRepository.findAllNotices().stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());

        if (cache != null && !list.isEmpty()) cache.put(key, list);
        return list;
    }

    // -----------------------------------------------------
    /** 사용자별 게시글 목록 */
    @Transactional(readOnly = true)
    public List<BoardDTO> findByUser(Long userId) {
        log.debug("Request to get Boards by User : {}", userId);
        return boardRepository.findByUser_Id(userId).stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    /** 게시판 유형별 게시글 목록 */
    @Transactional(readOnly = true)
    public List<BoardDTO> findByBoardType(String code) {
        log.debug("Request to get Boards by BoardType : {}", code);
        return boardRepository.findByBoardType_Code(code).stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    /** 게시글 통계 */
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        String key = "user:" + userId;
        Cache cache = cacheManager.getCache(CACHE_BOARD_COUNT_BY_USER);
        if (cache != null) {
            Long cached = cache.get(key, Long.class);
            if (cached != null) return cached;
        }

        long val = boardRepository.countByUserId(userId);
        if (cache != null) cache.put(key, val);
        return val;
    }

    @Transactional(readOnly = true)
    public long countAll() {
        String key = "count:total";
        Cache cache = cacheManager.getCache(CACHE_BOARD_COUNT_TOTAL);
        if (cache != null) {
            Long cached = cache.get(key, Long.class);
            if (cached != null) return cached;
        }

        long val = boardRepository.countActiveBoards();
        if (cache != null) cache.put(key, val);
        return val;
    }

    // -----------------------------------------------------
    /**
     * 소프트 삭제된 모든 게시글 조회 (관리자용).
     * 
     * @return 삭제된 게시글의 DTO 목록
     */
    @IncludeDeleted
    @Transactional(readOnly = true)
    public List<BoardDTO> findAllDeleted() {
        return boardRepository.findAllDeletedBoards().stream()
            .map(boardMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * 삭제된 게시글 복구.
     * 
     * @param id 복구할 게시글 ID
     */
    @IncludeDeleted
    public void restore(Long id) {
        Board board = boardRepository.findByIdIncludingDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("복구할 게시글을 찾을 수 없습니다. ID=" + id));

        board.setDeleted(false);
        board.setDescription(null);
        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);
    }

    /**
     * 게시글 영구 삭제 (Hard Delete).
     * 
     * 주의: 이 메서드는 데이터베이스에서 해당 레코드를 물리적으로 제거하며 복구가 불가능합니다.
     * @param id 영구 삭제할 게시글 ID
     */
    @IncludeDeleted
    public void hardDelete(Long id) {
        Board board = boardRepository.findByIdIncludingDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID=" + id));

        boardRepository.delete(board);
        clearBoardCaches(board);
    }

    // -----------------------------------------------------
    /** 공지글 상태 전환 */
    public void toggleNotice(Long id, boolean notice) {
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("공지 전환 대상 게시글을 찾을 수 없습니다. ID=" + id));

        board.setNotice(notice);
        Board saved = boardRepository.save(board);

        clearBoardCaches(saved);
    }

    // -----------------------------------------------------
    /** 캐시 클리어 유틸리티 */
    private void clearBoardCaches(Board board) {
        if (board == null) return;
        try {
            evictCache(CACHE_BOARD_BY_ID, board.getId());
            clearCache(CACHE_BOARD_PAGE, CACHE_BOARD_SEARCH, CACHE_BOARD_NOTICE_LIST);
            evictCache(CACHE_BOARD_COUNT_TOTAL, "count:total");

            Optional.ofNullable(board.getUser())
                .map(User::getId)
                .ifPresent(userId -> evictCache(CACHE_BOARD_COUNT_BY_USER, "user:" + userId));

            log.debug("[BOARD CACHE] Cleared caches for board id={}", board.getId());
        } catch (Exception e) {
            log.warn("[BOARD CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------
    /** 내부 유틸 메서드 */
    private void validateBoard(BoardDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new BadRequestAlertException("게시글 제목은 비어 있을 수 없습니다.", ENTITY_NAME, "board.emptyTitle");

        if (dto.getContent() == null || dto.getContent().isBlank())
            throw new BadRequestAlertException("게시글 내용은 비어 있을 수 없습니다.", ENTITY_NAME, "board.emptyContent");

        if (dto.getUserId() == null)
            throw new BadRequestAlertException("작성자 정보가 필요합니다.", ENTITY_NAME, "board.noAuthor");
    }

    private void cacheIfPresent(String cacheName, Object key, Object value) {
        Optional.ofNullable(cacheManager.getCache(cacheName))
            .ifPresent(cache -> cache.put(key, value));
    }

    private void clearCache(String... names) {
        Arrays.stream(names)
            .map(cacheManager::getCache)
            .filter(Objects::nonNull)
            .forEach(Cache::clear);
    }

    private void evictCache(String cacheName, Object key) {
        Optional.ofNullable(cacheManager.getCache(cacheName))
            .ifPresent(cache -> cache.evictIfPresent(key));
    }
}
