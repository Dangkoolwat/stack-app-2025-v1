package com.daangcool.stack.service.board;

import com.daangcool.stack.common.constant.CacheNames;


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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * BoardTagService
 * -----------------------------------------------------------
 * 게시글(Board)과 태그(Tag)의 연결 관계를 관리합니다.
 * - 게시글에 태그 추가 / 제거
 * - 특정 게시글의 태그 조회
 * - 특정 태그의 게시글 조회
 *
 * 캐시 전략:
 * - BoardTag 자체 캐시는 사용하지 않음
 * - 관계 변경 시 관련 Board / Tag 캐시 모두 무효화
 * -----------------------------------------------------------
 */
@Service
@Transactional
public class BoardTagService {

    private static final Logger log = LoggerFactory.getLogger(BoardTagService.class);
    private static final String ENTITY_NAME = "boardTag";

    // -----------------------------------------------
    // 캐시 (CacheNames 사용)
    // -----------------------------------------------

    private final BoardTagRepository boardTagRepository;
    private final BoardRepository boardRepository;
    private final TagRepository tagRepository;
    private final BoardTagMapper boardTagMapper;
    private final CacheManager cacheManager;

    public BoardTagService(
        BoardTagRepository boardTagRepository,
        BoardRepository boardRepository,
        TagRepository tagRepository,
        BoardTagMapper boardTagMapper,
        CacheManager cacheManager
    ) {
        this.boardTagRepository = boardTagRepository;
        this.boardRepository = boardRepository;
        this.tagRepository = tagRepository;
        this.boardTagMapper = boardTagMapper;
        this.cacheManager = cacheManager;
    }

    // -----------------------------------------------------
    // 캐시 클리어 유틸리티 (Board, Tag 관련 캐시 무효화)
    // -----------------------------------------------------
    private void clearBoardTagCaches(BoardTag bt) {
        if (bt == null) return;
        try {
            if (bt.getBoard() != null) {
                Long boardId = bt.getBoard().getId();
                Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_BY_ID)).evictIfPresent(boardId);
                Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_PAGE)).clear();
                Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_SEARCH)).clear();
                Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_NOTICES)).clear();
            }
            if (bt.getTag() != null) {
                Long tagId = bt.getTag().getId();
                Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_BY_ID)).evictIfPresent(tagId);
                Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_ALL)).clear();
                Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_POPULAR)).clear();
            }
            log.debug("[BOARD_TAG CACHE] Cleared related caches for boardId={} / tagId={}",
                bt.getBoard() != null ? bt.getBoard().getId() : null,
                bt.getTag() != null ? bt.getTag().getId() : null);
        } catch (Exception e) {
            log.warn("[BOARD_TAG CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------
    // 게시글에 태그 추가
    // -----------------------------------------------------
    public BoardTagDTO addTagToBoard(Long boardId, Long tagId) {
        log.debug("Request to add Tag {} to Board {}", tagId, boardId);

        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID=" + boardId));

        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다. ID=" + tagId));

        if (boardTagRepository.existsByBoard_IdAndTag_Id(boardId, tagId)) {
            throw new BadRequestAlertException("이미 게시글에 연결된 태그입니다.", ENTITY_NAME, "boardTag.exists");
        }

        BoardTag bt = new BoardTag();
        bt.setBoard(board);
        bt.setTag(tag);

        BoardTag saved = boardTagRepository.save(bt);
        clearBoardTagCaches(saved);
        return boardTagMapper.toDto(saved);
    }

    // -----------------------------------------------------
    // 게시글에서 태그 제거 (Soft Delete)
    // -----------------------------------------------------
    public void removeTagFromBoard(Long boardId, Long tagId, String reason) {
        log.debug("Request to remove Tag {} from Board {}", tagId, boardId);
        List<BoardTag> relations = boardTagRepository.findAllByBoard_IdOrderByIdAsc(boardId);

        relations.stream()
            .filter(bt -> bt.getTag() != null && bt.getTag().getId().equals(tagId))
            .findFirst()
            .ifPresentOrElse(bt -> {
                boardTagRepository.softDelete(bt.getId(), reason);
                clearBoardTagCaches(bt);
            }, () -> {
                throw new EntityNotFoundException("연결된 태그 관계를 찾을 수 없습니다.");
            });
    }

    // -----------------------------------------------------
    // 조회 (Board 기준 / Tag 기준)
    // -----------------------------------------------------
    @Transactional(readOnly = true)
    public List<BoardTagDTO> findAllTagsByBoard(Long boardId) {
        log.debug("Request to get Tags for Board {}", boardId);
        return boardTagRepository.findAllByBoard_IdOrderByIdAsc(boardId).stream()
            .map(boardTagMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardTagDTO> findAllBoardsByTag(Long tagId) {
        log.debug("Request to get Boards for Tag {}", tagId);
        return boardTagRepository.findAllByTag_Id(tagId).stream()
            .map(boardTagMapper::toDto)
            .toList();
    }

    // -----------------------------------------------------
    // 관리자 전용 기능 (AdminResource에서 호출)
    // -----------------------------------------------------

    /** 전체 관계 조회 */
    @Transactional(readOnly = true)
    public List<BoardTagDTO> findAllRelations() {
        log.debug("Admin request to get all BoardTag relations");
        return boardTagRepository.findAll().stream()
            .map(boardTagMapper::toDto)
            .toList();
    }

    /** 관계 복구 (Soft Delete → 활성화) */
    public void restoreRelation(Long id) {
        log.debug("Admin request to restore BoardTag relation : {}", id);
        BoardTag bt = boardTagRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("복구할 관계를 찾을 수 없습니다. ID=" + id));

        bt.setDeleted(false);
        bt.setDescription(null);
        boardTagRepository.save(bt);
        clearBoardTagCaches(bt);
    }

    // -----------------------------------------------------
    // 하드 삭제 (정책 위반 시 관리자 전용)
    // -----------------------------------------------------
    public void hardDeleteRelation(Long boardId, Long tagId) {
        log.debug("Admin request to hard delete BoardTag relation board={} tag={}", boardId, tagId);
        boardTagRepository.hardDeleteRelation(boardId, tagId);
    }

    /** 모든 관련 캐시를 강제로 초기화 */
    public void clearAllCaches() {
        try {
            Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_BY_ID)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_PAGE)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_SEARCH)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_NOTICES)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_BY_ID)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_ALL)).clear();
            Objects.requireNonNull(cacheManager.getCache(CacheNames.TAG_POPULAR)).clear();
            log.info("[BOARD_TAG CACHE] 모든 관련 캐시를 초기화했습니다.");
        } catch (Exception e) {
            log.warn("[BOARD_TAG CACHE] 캐시 초기화 중 오류: {}", e.getMessage());
        }
    }
}
