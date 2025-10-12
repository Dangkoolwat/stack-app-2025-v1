package com.daangcool.stack.web.rest;

import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.service.board.UploadService;
import com.daangcool.stack.web.exception.UploadNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * REST controller for administrative management of uploaded files.
 *
 * <p>
 * Admin-only operations:
 * - Toggle visibility (public/private)
 * - Soft delete / Hard delete
 * - Metadata inspection
 * </p>
 */
@RestController
@RequestMapping("/api/admin/uploads")
@Tag(name = "Upload Admin Resource", description = "Admin-only Upload Management API")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class UploadAdminResource {

    private static final Logger log = LoggerFactory.getLogger(UploadAdminResource.class);
    private final UploadService uploadService;

    public UploadAdminResource(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * {@code PATCH  /admin/uploads/:id/visibility} : Change file visibility (public/private).
     *
     * @param id file ID
     * @param targetIsPublic true for public, false for private
     * @return updated Upload metadata
     */
    @Operation(summary = "Toggle file visibility", description = "Sets the file's public/private visibility flag.")
    @ApiResponse(responseCode = "200", description = "Visibility updated successfully")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Upload> changeVisibility(
        @PathVariable Long id,
        @RequestParam("public") boolean targetIsPublic
    ) throws URISyntaxException {
        log.debug("Admin request to change Upload visibility: id={}, public={}", id, targetIsPublic);
        Upload updated = uploadService.changeVisibility(id, targetIsPublic);
        return ResponseEntity.ok()
            .location(new URI("/api/admin/uploads/" + updated.getId()))
            .body(updated);
    }

    /**
     * {@code DELETE  /admin/uploads/:id} : Soft or hard delete a file.
     *
     * @param id file ID
     * @param mode deletion mode (soft/hard)
     * @param reason optional reason for deletion
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Delete file", description = "Deletes a file either logically (soft) or permanently (hard).")
    @ApiResponse(responseCode = "204", description = "File deleted successfully")
    @ApiResponse(responseCode = "404", description = "File not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
        @PathVariable Long id,
        @RequestParam(name = "mode", defaultValue = "soft") String mode,
        @RequestParam(name = "reason", required = false) String reason
    ) {
        log.debug("Admin request to delete Upload : {} (mode={})", id, mode);

        Optional<Upload> existing = uploadService.findById(id);
        if (existing.isEmpty()) {
            throw new UploadNotFoundException("Upload ID not found: " + id);
        }

        if ("hard".equalsIgnoreCase(mode)) {
            uploadService.hardDelete(id);
        } else {
            uploadService.softDelete(id, reason != null ? reason : "관리자에 의한 삭제");
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET  /admin/uploads/:id} : Get metadata for a specific file.
     *
     * @param id file ID
     * @return the file metadata if found
     */
    @Operation(summary = "Get file metadata", description = "Fetches file information including visibility and status.")
    @ApiResponse(responseCode = "200", description = "File metadata retrieved successfully")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}")
    public ResponseEntity<Upload> getFileMetadata(@PathVariable Long id) {
        log.debug("Admin request to get Upload metadata: {}", id);
        Optional<Upload> upload = uploadService.findById(id);
        return upload.map(ResponseEntity::ok)
            .orElseThrow(() -> new UploadNotFoundException("Upload not found: " + id));
    }
}
