package com.daangcool.stack.web.rest;

import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.service.board.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin management of {@link com.daangcool.stack.domain.board.Tag}.
 *
 * <p>
 * Admin-only API for:
 * - Soft deleting tags
 * - Restoring deleted tags
 * - Managing tag lifecycle
 * </p>
 */
@RestController
@RequestMapping("/api/admin/tags")
@Tag(name = "Tag Admin Resource", description = "Admin-only Tag Management API")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class TagAdminResource {

    private static final Logger log = LoggerFactory.getLogger(TagAdminResource.class);
    private final TagService tagService;

    public TagAdminResource(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * {@code DELETE  /admin/tags/:id} : Soft delete a tag.
     *
     * @param id the id of the tag to delete
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Soft delete a tag", description = "Marks the tag as deleted without removing it from the database.")
    @ApiResponse(responseCode = "204", description = "Tag deleted successfully")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.debug("Admin request to delete Tag : {}", id);
        try {
            tagService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * {@code PATCH  /admin/tags/:id/undelete} : Restore a deleted tag.
     *
     * @param id the id of the tag to restore
     * @return {@link ResponseEntity} with status {@code 200 (OK)}
     */
    @Operation(summary = "Restore deleted tag", description = "Restores a previously soft-deleted tag.")
    @ApiResponse(responseCode = "200", description = "Tag restored successfully")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    @PatchMapping("/{id}/undelete")
    public ResponseEntity<Void> undeleteTag(@PathVariable Long id) {
        log.debug("Admin request to restore Tag : {}", id);
        try {
            tagService.undelete(id);
            // 캐시 초기화 (전역 반영)
            tagService.clearAllTagCaches();
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
