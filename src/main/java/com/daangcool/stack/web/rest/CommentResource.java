package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.board.CommentService;
import com.daangcool.stack.service.dto.CommentDTO;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.daangcool.stack.domain.board.Comment}.
 *
 * <p>
 * User-facing API for:
 * - Creating, reading, updating, and deleting comments
 * - Searching and counting comments
 * - Managing likes (stars)
 * </p>
 */
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment Resource", description = "Public Comment API for general users")
public class CommentResource {

    private final Logger log = LoggerFactory.getLogger(CommentResource.class);
    private static final String ENTITY_NAME = "comment";

    private final CommentService commentService;

    public CommentResource(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * {@code POST  /comments} : Create a new comment.
     *
     * @param dto the CommentDTO to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new comment
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException if required fields are missing
     */
    @Operation(summary = "Create a new comment", description = "Registers a new comment under a specific board.")
    @ApiResponse(responseCode = "201", description = "Successfully created")
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO dto) throws URISyntaxException {
        log.debug("REST request to save Comment : {}", dto);
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new comment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CommentDTO result = commentService.save(dto);
        return ResponseEntity.created(new URI("/api/comments/" + result.getId())).body(result);
    }

    /**
     * {@code GET  /comments/board/:boardId} : Get all comments for a specific board.
     *
     * @param boardId the ID of the board
     * @return list of comments belonging to the specified board
     */
    @Operation(summary = "Get comments by board", description = "Retrieves all comments for a given board ID.")
    @ApiResponse(responseCode = "200", description = "Comments successfully retrieved")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByBoard(@PathVariable Long boardId) {
        log.debug("REST request to get all Comments for Board : {}", boardId);
        return ResponseEntity.ok(commentService.findAllByBoard(boardId));
    }

    /**
     * {@code GET  /comments/:id} : Get a comment by ID.
     *
     * @param id the ID of the comment to retrieve
     * @return the comment, or {@code 404 (Not Found)} if not found
     */
    @Operation(summary = "Get comment by ID", description = "Retrieves a comment by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Comment found")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @GetMapping("/{id}")
    public ResponseEntity<Optional<CommentDTO>> getComment(@PathVariable Long id) {
        log.debug("REST request to get Comment : {}", id);
        return ResponseEntity.ok(commentService.findOne(id));
    }

    /**
     * {@code PUT  /comments/:id} : Update a comment’s content.
     *
     * @param id the ID of the comment to update
     * @param dto the updated comment content
     * @return updated comment
     */
    @Operation(summary = "Update comment", description = "Updates the content of an existing comment.")
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(
        @PathVariable Long id,
        @Valid @RequestBody CommentDTO dto
    ) {
        log.debug("REST request to update Comment : {}", id);
        return ResponseEntity.ok(commentService.update(id, dto));
    }

    /**
     * {@code DELETE  /comments/:id} : Soft delete a comment.
     *
     * @param id the ID of the comment to delete
     * @param reason optional reason for deletion
     * @return {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Delete a comment (Soft Delete)", description = "Marks a comment as deleted without removing it from the database.")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long id,
        @RequestParam(required = false) String reason
    ) {
        log.debug("REST request to delete Comment : {}", id);
        commentService.delete(id, reason);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code PATCH  /comments/:id/star} : Increase star count (like).
     *
     * @param id comment ID
     * @return {@link ResponseEntity} with status {@code 200 (OK)}
     */
    @Operation(summary = "Increase star count", description = "Adds one like (star) to a comment.")
    @ApiResponse(responseCode = "200", description = "Star count increased")
    @PatchMapping("/{id}/star")
    public ResponseEntity<Void> increaseStar(@PathVariable Long id) {
        log.debug("REST request to increase star count for Comment : {}", id);
        commentService.increaseStar(id);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /comments/search} : Search comments by keyword.
     *
     * @param keyword keyword text
     * @param page page number
     * @param size page size
     * @return paginated comments matching the keyword
     */
    @Operation(summary = "Search comments", description = "Finds comments that contain a specific keyword in their content.")
    @ApiResponse(responseCode = "200", description = "Search successful")
    @GetMapping("/search")
    public ResponseEntity<Page<CommentDTO>> searchComments(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("REST request to search Comments : {}", keyword);
        return ResponseEntity.ok(commentService.search(keyword, page, size));
    }

    /**
     * {@code GET  /comments/count/board/:boardId} : Count comments by board.
     *
     * @param boardId board ID
     * @return comment count
     */
    @Operation(summary = "Count comments by board", description = "Returns the number of comments for a specific board.")
    @ApiResponse(responseCode = "200", description = "Count successful")
    @GetMapping("/count/board/{boardId}")
    public ResponseEntity<Long> countByBoard(@PathVariable Long boardId) {
        log.debug("REST request to count Comments for Board : {}", boardId);
        return ResponseEntity.ok(commentService.countByBoard(boardId));
    }

    /**
     * {@code GET  /comments/count/user/:userId} : Count comments by user.
     *
     * @param userId user ID
     * @return comment count
     */
    @Operation(summary = "Count comments by user", description = "Returns the number of comments created by a specific user.")
    @ApiResponse(responseCode = "200", description = "Count successful")
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Long> countByUser(@PathVariable Long userId) {
        log.debug("REST request to count Comments by User : {}", userId);
        return ResponseEntity.ok(commentService.countByUser(userId));
    }
}
