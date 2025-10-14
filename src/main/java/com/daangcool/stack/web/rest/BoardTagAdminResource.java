package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.board.BoardTagService;
import com.daangcool.stack.service.dto.BoardTagDTO;
import com.daangcool.stack.common.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative operations on Board-Tag relationships.
 * -----------------------------------------------------------
 * 관리자 전용 API:
 *  - 전체 관계 조회
 *  - 관계 복구
 *  - 완전 삭제 (Hard Delete)
 *  - 관계 캐시 강제 초기화
 *
 * 접근 권한: ROLE_ADMIN 전용
 * -----------------------------------------------------------
 */
@RestController
@RequestMapping("/api/admin/board-tags")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BoardTagAdminResource {

    private static final Logger log = LoggerFactory.getLogger(BoardTagAdminResource.class);
    private final BoardTagService boardTagService;

    public BoardTagAdminResource(BoardTagService boardTagService) {
        this.boardTagService = boardTagService;
    }

    /**
     * 전체 Board-Tag 관계 조회
     *
     * @return BoardTagDTO 목록
     */
    @GetMapping
    public ResponseEntity<List<BoardTagDTO>> getAllRelations() {
        log.debug("Admin request to get all BoardTag relations");
        List<BoardTagDTO> list = boardTagService.findAllRelations();
        return ResponseEntity.ok(list);
    }

    /**
     * 특정 게시글과 태그 간 관계 완전 삭제
     *
     * @param boardId 게시글 ID
     * @param tagId 태그 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{boardId}/{tagId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long boardId, @PathVariable Long tagId) {
        log.debug("Admin request to hard delete BoardTag relation board={} tag={}", boardId, tagId);
        boardTagService.hardDeleteRelation(boardId, tagId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 관계 복구 (Soft Delete → 활성화)
     *
     * @param id BoardTag 관계 ID
     * @return 204 No Content
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        log.debug("Admin request to restore BoardTag relation id={}", id);
        try {
            boardTagService.restoreRelation(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 모든 Board-Tag 관련 캐시 강제 초기화
     *
     * 운영 중 관계 동기화가 필요할 때 수동으로 호출할 수 있습니다.
     *
     * @return 204 No Content
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Void> clearAllCaches() {
        log.debug("Admin request to clear all Board/Tag caches");
        boardTagService.clearAllCaches();
        return ResponseEntity.noContent().build();
    }
}
