package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.board.CommentService;
import com.daangcool.stack.service.dto.CommentDTO;
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
 * REST controller for administrative operations on {@link com.daangcool.stack.domain.board.Comment}.
 *
 * <p>
 * Admin API for:
 * - Viewing deleted comments
 * - Restoring or permanently deleting comments
 * </p>
 */
@RestController
@RequestMapping("/api/admin/comments")
@Tag(name = "Comment Admin Resource", description = "Admin-only Comment Management API")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class CommentAdminResource {

    private final Logger log = LoggerFactory.getLogger(CommentAdminResource.class);
    private final CommentService commentService;

    public CommentAdminResource(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * {@code GET  /admin/comments/deleted} : Get all soft-deleted comments.
     *
     * @return list of deleted comments
     */
    @Operation(summary = "Get deleted comments", description = "Retrieves all comments marked as deleted.")
    @ApiResponse(responseCode = "200", description = "Deleted comments retrieved successfully")
    @GetMapping("/deleted")
    public ResponseEntity<List<CommentDTO>> getDeletedComments() {
        log.debug("Admin request to get all deleted Comments");
        return ResponseEntity.ok(commentService.findAllDeleted());
    }

    /**
     * {@code PUT  /admin/comments/restore/:id} : Restore a soft-deleted comment.
     *
     * @param id the ID of the comment to restore
     * @return {@link ResponseEntity} with status {@code 200 (OK)} if restored
     */
    @Operation(summary = "Restore a deleted comment", description = "Restores a comment previously marked as deleted.")
    @ApiResponse(responseCode = "200", description = "Comment successfully restored")
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreComment(@PathVariable Long id) {
        log.debug("Admin request to restore Comment : {}", id);
        commentService.restore(id);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code DELETE  /admin/comments/hard-delete/:id} : Permanently delete a comment.
     *
     * @param id the ID of the comment to permanently delete
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Permanently delete a comment", description = "Completely removes a comment and its data from the database.")
    @ApiResponse(responseCode = "204", description = "Comment permanently deleted")
    @DeleteMapping("/hard-delete/{id}")
    public ResponseEntity<Void> hardDeleteComment(@PathVariable Long id) {
        log.debug("Admin request to hard delete Comment : {}", id);
        commentService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
