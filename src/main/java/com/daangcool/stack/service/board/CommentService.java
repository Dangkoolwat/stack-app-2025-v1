package com.daangcool.stack.service.board;

import com.daangcool.stack.service.softdelete.IncludeDeleted;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.board.Comment;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.repository.board.BoardRepository;
import com.daangcool.stack.repository.board.CommentRepository;
import com.daangcool.stack.service.dto.CommentDTO;
import com.daangcool.stack.service.mapper.CommentMapper;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.common.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing {@link com.daangcool.stack.domain.board.Comment}.
 *
 * 댓글 CRUD, 검색, 통계, 관리자 복구/삭제 기능을 담당합니다.
 * CacheManager를 이용하여 명시적으로 캐시를 제어합니다.
 * 캐시 무효화는 clearCommentCaches(Comment comment)에서 일괄 처리합니다.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final String ENTITY_NAME = "comment";

    // -----------------------------------------------------
    // 캐시 이름 상수
    // -----------------------------------------------------
    public static final String CACHE_COMMENT_BY_ID = "COMMENT_BY_ID";
    public static final String CACHE_COMMENT_BY_BOARD = "COMMENT_BY_BOARD";
    public static final String CACHE_COMMENT_SEARCH = "COMMENT_SEARCH";
    public static final String CACHE_COMMENT_COUNT_BY_BOARD = "COMMENT_COUNT_BY_BOARD";
    public static final String CACHE_COMMENT_COUNT_BY_USER = "COMMENT_COUNT_BY_USER";
    public static final String CACHE_COMMENT_STATS = "CACHE_COMMENT_STATS";

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final CacheManager cacheManager;

    public CommentService(
        CommentRepository commentRepository,
        BoardRepository boardRepository,
        UserRepository userRepository,
        CommentMapper commentMapper,
        CacheManager cacheManager
    ) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.cacheManager = cacheManager;
    }

    // -----------------------------------------------------
    // 댓글 등록
    // -----------------------------------------------------
    public CommentDTO save(CommentDTO dto) {
        log.debug("Request to save Comment : {}", dto);

        if (dto.getBoardId() == null || dto.getUserId() == null) {
            throw new BadRequestAlertException("게시글 ID와 작성자 ID는 필수입니다.", ENTITY_NAME, "comment.invalidParams");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BadRequestAlertException("댓글 내용은 비어 있을 수 없습니다.", ENTITY_NAME, "comment.emptyContent");
        }

        Board board = boardRepository.findById(dto.getBoardId())
            .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다. ID=" + dto.getBoardId()));
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID=" + dto.getUserId()));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setUser(user);
        comment.setContent(dto.getContent());

        Comment saved = commentRepository.save(comment);
        clearCommentCaches(saved);

        Cache byId = cacheManager.getCache(CACHE_COMMENT_BY_ID);
        if (byId != null) byId.put(saved.getId(), commentMapper.toDto(saved));

        return commentMapper.toDto(saved);
    }

    // -----------------------------------------------------
    // 게시글 기준 댓글 목록 조회 (캐시 사용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CommentDTO> findAllByBoard(Long boardId) {
        log.debug("Request to get all Comments for Board : {}", boardId);
        String key = "board:" + boardId;

        Cache byBoard = cacheManager.getCache(CACHE_COMMENT_BY_BOARD);
        if (byBoard != null) {
            List<CommentDTO> cached = (List<CommentDTO>) byBoard.get(key, List.class);
            if (cached != null) {
                log.debug("[COMMENT CACHE] Cache hit for {}", key);
                return cached;
            }
        }

        List<CommentDTO> list = commentRepository.findAllByBoard_IdOrderByIdAsc(boardId).stream()
            .filter(c -> !c.isDeleted())
            .map(commentMapper::toDto)
            .collect(Collectors.toList());

        if (byBoard != null && !list.isEmpty()) byBoard.put(key, list);
        return list;
    }

    // -----------------------------------------------------
    // 단건 조회 (캐시 사용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public Optional<CommentDTO> findOne(Long id) {
        log.debug("Request to get Comment : {}", id);
        Cache byId = cacheManager.getCache(CACHE_COMMENT_BY_ID);
        if (byId != null) {
            CommentDTO cached = byId.get(id, CommentDTO.class);
            if (cached != null) {
                log.debug("[COMMENT CACHE] Cache hit for id={}", id);
                return Optional.of(cached);
            }
        }

        Optional<CommentDTO> dto = commentRepository.findById(id)
            .filter(c -> !c.isDeleted())
            .map(commentMapper::toDto);

        dto.ifPresent(val -> {
            if (byId != null) byId.put(id, val);
        });

        return dto;
    }

    // -----------------------------------------------------
    // 댓글 수정
    // -----------------------------------------------------
    public CommentDTO update(Long id, CommentDTO dto) {
        log.debug("Request to update Comment : {}", id);

        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("수정할 댓글을 찾을 수 없습니다. ID=" + id));

        if (comment.isDeleted()) {
            throw new BadRequestAlertException("삭제된 댓글은 수정할 수 없습니다.", ENTITY_NAME, "comment.deleted");
        }

        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BadRequestAlertException("댓글 내용은 비어 있을 수 없습니다.", ENTITY_NAME, "comment.emptyContent");
        }

        comment.setContent(dto.getContent());
        Comment saved = commentRepository.save(comment);

        clearCommentCaches(saved);
        return commentMapper.toDto(saved);
    }

    // -----------------------------------------------------
    // 댓글 삭제 (Soft Delete)
    // -----------------------------------------------------
    public void delete(Long id, String reason) {
        log.debug("Request to soft delete Comment : {}", id);
        Comment before = commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다. ID=" + id));

        int updated = commentRepository.softDelete(id, reason);
        if (updated == 0) {
            throw new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다. ID=" + id);
        }

        clearCommentCaches(before);
    }

    // -----------------------------------------------------
    // 좋아요(별점) 증가
    // -----------------------------------------------------
    public void increaseStar(Long id) {
        log.debug("Request to increase star count for Comment : {}", id);
        Comment before = commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("대상 댓글을 찾을 수 없습니다. ID=" + id));

        commentRepository.increaseStar(id);
        clearCommentCaches(before);
    }

    // -----------------------------------------------------
    // 키워드 검색 (캐시 사용)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<CommentDTO> search(String keyword, int page, int size) {
        log.debug("Request to search Comments by keyword: {}", keyword);
        String key = "q:" + (keyword == null ? "" : keyword.toLowerCase()) + ":p:" + page + ":s:" + size;

        Cache search = cacheManager.getCache(CACHE_COMMENT_SEARCH);
        if (search != null) {
            Page<CommentDTO> cached = (Page<CommentDTO>) search.get(key, Page.class);
            if (cached != null) {
                log.debug("[COMMENT CACHE] Cache hit for search {}", key);
                return cached;
            }
        }

        Page<CommentDTO> result = commentRepository
            .searchByKeyword(keyword, PageRequest.of(page, size))
            .map(commentMapper::toDto);

        if (search != null && !result.isEmpty()) search.put(key, result);
        return result;
    }

    // -----------------------------------------------------
    // 댓글 수 조회 (게시글 기준)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public long countByBoard(Long boardId) {
        String key = "board:" + boardId;
        Cache cnt = cacheManager.getCache(CACHE_COMMENT_COUNT_BY_BOARD);
        if (cnt != null) {
            Long cached = cnt.get(key, Long.class);
            if (cached != null) return cached;
        }
        long val = commentRepository.countByBoardId(boardId);
        if (cnt != null) cnt.put(key, val);
        return val;
    }

    // -----------------------------------------------------
    // 댓글 수 조회 (사용자 기준)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        String key = "user:" + userId;
        Cache cnt = cacheManager.getCache(CACHE_COMMENT_COUNT_BY_USER);
        if (cnt != null) {
            Long cached = cnt.get(key, Long.class);
            if (cached != null) return cached;
        }
        long val = commentRepository.countByUserId(userId);
        if (cnt != null) cnt.put(key, val);
        return val;
    }

    // -----------------------------------------------------
    // 관리자 기능 (복구 / 완전 삭제)
    // -----------------------------------------------------
    @IncludeDeleted
    public void restore(Long id) {
        log.debug("Admin request to restore Comment : {}", id);
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("복구할 댓글을 찾을 수 없습니다. ID=" + id));

        comment.setDeleted(false);
        comment.setDescription(null);
        Comment saved = commentRepository.save(comment);
        clearCommentCaches(saved);
    }

    @IncludeDeleted
    public void hardDelete(Long id) {
        log.debug("Admin request to hard delete Comment : {}", id);
        Comment before = commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("완전 삭제할 댓글을 찾을 수 없습니다. ID=" + id));

        commentRepository.delete(before);
        clearCommentCaches(before);
    }

    // -----------------------------------------------------
    // 삭제된 댓글 조회 (관리자)
    // -----------------------------------------------------
    @IncludeDeleted
    @Transactional(readOnly = true)
    public List<CommentDTO> findAllDeleted() {
        log.debug("Admin request to get all deleted comments");
        return commentRepository.findAllWithDeleted().stream()
            .filter(Comment::isDeleted)
            .map(commentMapper::toDto)
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------
    // 관리자 고아/완전 삭제 API 용 (시간 조건 필터링)
    // -----------------------------------------------------
    @IncludeDeleted
    @Transactional(readOnly = true)
    public List<CommentDTO> getOrphanComments(Instant threshold) {
        log.debug("Admin request to get orphan comments older than threshold");
        return commentRepository.findAllOrphanComments(threshold).stream()
            .map(commentMapper::toDto)
            .collect(Collectors.toList());
    }

    @IncludeDeleted
    public int hardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        commentRepository.deleteByIds(ids);
        clearAllCommentCaches();
        return ids.size();
    }

    // -----------------------------------------------------
    // 캐시 전체 초기화 (관리자)
    // -----------------------------------------------------
    public void clearAllCommentCaches() {
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_BY_ID)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_BY_BOARD)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_STATS)).clear();
            log.info("[COMMENT CACHE] 모든 댓글 관련 캐시를 초기화했습니다.");
        } catch (Exception e) {
            log.warn("[COMMENT CACHE] 캐시 초기화 중 오류: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------
    // 캐시 클리어 유틸리티
    // -----------------------------------------------------
    private void clearCommentCaches(Comment comment) {
        if (comment == null) return;
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_BY_ID)).evictIfPresent(comment.getId());
            if (comment.getBoard() != null) {
                Long boardId = comment.getBoard().getId();
                Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_BY_BOARD)).evictIfPresent("board:" + boardId);
                Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_COUNT_BY_BOARD)).evictIfPresent("board:" + boardId);
            }
            if (comment.getUser() != null) {
                Long userId = comment.getUser().getId();
                Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_COUNT_BY_USER)).evictIfPresent("user:" + userId);
            }
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_SEARCH)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_COMMENT_STATS)).clear();
            log.debug("[COMMENT CACHE] Cleared caches for commentId={} / boardId={} / userId={}",
                comment.getId(),
                comment.getBoard() != null ? comment.getBoard().getId() : null,
                comment.getUser() != null ? comment.getUser().getId() : null);
        } catch (Exception e) {
            log.warn("[COMMENT CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }
}
