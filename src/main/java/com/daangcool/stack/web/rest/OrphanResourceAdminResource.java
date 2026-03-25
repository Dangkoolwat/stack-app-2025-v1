package com.daangcool.stack.web.rest;

import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.service.board.CommentService;
import com.daangcool.stack.service.board.TagService;
import com.daangcool.stack.service.board.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 게시판 리소스 관리 - 고아(Orphan) 컨텐츠 영구 삭제를 위한 통합 컨트롤러입니다.
 * 관련 모델: Upload, Tag, Comment.
 */
@RestController
@RequestMapping("/api/admin/orphans")
@Tag(name = "Orphan Resource Admin", description = "Admin API for managing orphan / soft-deleted resources")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class OrphanResourceAdminResource {

    private final Logger log = LoggerFactory.getLogger(OrphanResourceAdminResource.class);

    private final UploadService uploadService;
    private final TagService tagService;
    private final CommentService commentService;

    public OrphanResourceAdminResource(UploadService uploadService, TagService tagService, CommentService commentService) {
        this.uploadService = uploadService;
        this.tagService = tagService;
        this.commentService = commentService;
    }

    /**
     * 안전장치를 위한 24시간 시간 제한 임계값 반환
     */
    private Instant getThreshold() {
        return Instant.now().minus(24, ChronoUnit.HOURS);
    }

    @GetMapping("/uploads")
    @Operation(summary = "Get orphan uploads", description = "Returns uploads not attached to any board and older than 24h, or soft deleted")
    public ResponseEntity<List<?>> getOrphanUploads() {
        log.debug("REST request to get orphan uploads");
        return ResponseEntity.ok(uploadService.getOrphanUploads(getThreshold()));
    }

    @DeleteMapping("/uploads")
    @Operation(summary = "Hard delete orphan uploads")
    public ResponseEntity<Map<String, Integer>> deleteUploads(@RequestBody List<Long> ids) {
        log.debug("REST request to delete orphan uploads: {}", ids);
        int count = uploadService.hardDelete(ids);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @GetMapping("/tags")
    @Operation(summary = "Get orphan tags", description = "Returns tags with usage=0 and older than 24h, or soft deleted")
    public ResponseEntity<List<?>> getOrphanTags() {
        log.debug("REST request to get orphan tags");
        return ResponseEntity.ok(tagService.getOrphanTags(getThreshold()));
    }

    @DeleteMapping("/tags")
    @Operation(summary = "Hard delete orphan tags")
    public ResponseEntity<Map<String, Integer>> deleteTags(@RequestBody List<Long> ids) {
        log.debug("REST request to delete orphan tags: {}", ids);
        int count = tagService.hardDelete(ids);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @GetMapping("/comments")
    @Operation(summary = "Get orphan comments", description = "Returns soft deleted comments older than 24h")
    public ResponseEntity<List<?>> getOrphanComments() {
        log.debug("REST request to get orphan comments");
        return ResponseEntity.ok(commentService.getOrphanComments(getThreshold()));
    }

    @DeleteMapping("/comments")
    @Operation(summary = "Hard delete orphan comments")
    public ResponseEntity<Map<String, Integer>> deleteComments(@RequestBody List<Long> ids) {
        log.debug("REST request to delete orphan comments: {}", ids);
        int count = commentService.hardDelete(ids);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }
}
