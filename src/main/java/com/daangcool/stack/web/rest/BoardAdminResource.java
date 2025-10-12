package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.board.BoardService;
import com.daangcool.stack.service.dto.BoardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative operations on {@link com.daangcool.stack.domain.board.Board}.
 * <p>
 * Admin API for:
 * - Viewing deleted posts
 * - Restoring or permanently deleting posts
 * - Managing notice status
 */
@RestController
@RequestMapping("/api/admin/boards")
@Tag(name = "Board Admin Resource", description = "Admin-only Board Management API")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BoardAdminResource {

    private final Logger log = LoggerFactory.getLogger(BoardAdminResource.class);
    private final BoardService boardService;

    public BoardAdminResource(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * {@code GET  /admin/boards/deleted} : Get all soft-deleted boards.
     *
     * @return list of deleted boards
     */
    @Operation(summary = "Get deleted boards", description = "Retrieves all boards marked as deleted.")
    @ApiResponse(responseCode = "200", description = "Deleted boards retrieved successfully")
    @GetMapping("/deleted")
    public ResponseEntity<List<BoardDTO>> getDeletedBoards() {
        log.debug("Admin request to get all deleted Boards");
        return ResponseEntity.ok(boardService.findAllDeleted());
    }

    /**
     * {@code PUT  /admin/boards/restore/:id} : Restore a soft-deleted board.
     *
     * @param id the ID of the board to restore
     * @return {@link ResponseEntity} with status {@code 200 (OK)} if restored
     */
    @Operation(summary = "Restore a deleted board", description = "Restores a board previously marked as deleted.")
    @ApiResponse(responseCode = "200", description = "Board successfully restored")
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreBoard(@PathVariable Long id) {
        log.debug("Admin request to restore Board : {}", id);
        boardService.restore(id);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code DELETE  /admin/boards/hard-delete/:id} : Permanently delete a board.
     *
     * @param id the ID of the board to permanently delete
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Permanently delete a board", description = "Completely removes a board and related data from the database.")
    @ApiResponse(responseCode = "204", description = "Board permanently deleted")
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<Void> hardDeleteBoard(@PathVariable Long id) {
        log.debug("Admin request to hard delete Board : {}", id);
        boardService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code PATCH  /admin/boards/:id/notice} : Toggle the notice status of a board.
     *
     * @param id the ID of the board
     * @param notice true to mark as notice, false to unmark
     * @return {@link ResponseEntity} with status {@code 200 (OK)}
     */
    @Operation(summary = "Toggle board notice status", description = "Marks or unmarks a board as a notice post.")
    @ApiResponse(responseCode = "200", description = "Notice status successfully changed")
    @PatchMapping("/{id}/notice")
    public ResponseEntity<Void> toggleNotice(@PathVariable Long id, @RequestParam boolean notice) {
        log.debug("Admin request to toggle notice status for Board : {}", id);
        boardService.toggleNotice(id, notice);
        return ResponseEntity.ok().build();
    }
}
