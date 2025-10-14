package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.board.BoardService;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.common.exception.BadRequestAlertException;
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

/**
 * REST controller for managing {@link com.daangcool.stack.domain.board.Board}.
 * <p>
 * User-facing API for:
 * - Creating, reading, updating, deleting posts (Soft Delete)
 * - Searching and filtering posts
 * - Retrieving notice posts
 */
@RestController
@RequestMapping("/api/boards")
@Tag(name = "Board Resource", description = "Public Board API for general users")
public class BoardResource {

    private final Logger log = LoggerFactory.getLogger(BoardResource.class);
    private static final String ENTITY_NAME = "board";

    private final BoardService boardService;

    public BoardResource(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * {@code POST  /boards} : Create a new board.
     *
     * @param dto the BoardDTO to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new board
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException if the board already has an ID
     */
    @Operation(summary = "Create a new board", description = "Registers a new board post.")
    @ApiResponse(responseCode = "201", description = "Successfully created")
    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@Valid @RequestBody BoardDTO dto) throws URISyntaxException {
        log.debug("REST request to save Board : {}", dto);
        if (dto.getId() != null) {
            throw new BadRequestAlertException("A new board cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BoardDTO result = boardService.save(dto);
        return ResponseEntity.created(new URI("/api/boards/" + result.getId())).body(result);
    }

    /**
     * {@code GET  /boards} : Get all boards (paginated).
     *
     * @param page page number (zero-based)
     * @param size page size
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of boards in body
     */
    @Operation(summary = "Get all boards", description = "Retrieves a paginated list of all boards.")
    @ApiResponse(responseCode = "200", description = "List successfully retrieved")
    @GetMapping
    public ResponseEntity<Page<BoardDTO>> getAllBoards(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("REST request to get all Boards");
        return ResponseEntity.ok(boardService.findAll(page, size));
    }

    /**
     * {@code GET  /boards/:id} : Get the board by ID.
     *
     * @param id the ID of the board to retrieve
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the board, or with status {@code 404 (Not Found)}
     */
    @Operation(summary = "Get board by ID", description = "Retrieves a board by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Board found")
    @ApiResponse(responseCode = "404", description = "Board not found")
    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoard(@PathVariable Long id) {
        log.debug("REST request to get Board : {}", id);
        return ResponseEntity.ok(boardService.findOne(id));
    }

    /**
     * {@code PUT  /boards/:id} : Update an existing board.
     *
     * @param id  the ID of the board to update
     * @param dto the boardDTO with updated data
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated board
     */
    @Operation(summary = "Update a board", description = "Updates the title, content, or notice status of a board.")
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @PutMapping("/{id}")
    public ResponseEntity<BoardDTO> updateBoard(@PathVariable Long id, @Valid @RequestBody BoardDTO dto) {
        log.debug("REST request to update Board : {}", id);
        return ResponseEntity.ok(boardService.update(id, dto));
    }

    /**
     * {@code DELETE  /boards/:id} : Soft delete a board.
     *
     * @param id     the ID of the board to delete
     * @param reason optional reason for deletion
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)}
     */
    @Operation(summary = "Delete a board (Soft Delete)", description = "Marks a board as deleted without removing it from the database.")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(
        @PathVariable Long id,
        @RequestParam(required = false) String reason
    ) {
        log.debug("REST request to delete Board : {}", id);
        boardService.delete(id, reason);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /boards/search} : Search boards by keyword.
     *
     * @param keyword keyword to search in title or content
     * @param page    page number
     * @param size    page size
     * @return paginated list of matching boards
     */
    @Operation(summary = "Search boards", description = "Searches boards by keyword within title or content.")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @GetMapping("/search")
    public ResponseEntity<Page<BoardDTO>> searchBoards(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("REST request to search Boards by keyword: {}", keyword);
        return ResponseEntity.ok(boardService.search(keyword, page, size));
    }

    /**
     * {@code GET  /boards/notices} : Get all notice boards.
     *
     * @return list of notice boards
     */
    @Operation(summary = "Get notice boards", description = "Retrieves all boards marked as notice posts.")
    @ApiResponse(responseCode = "200", description = "Notices retrieved")
    @GetMapping("/notices")
    public ResponseEntity<List<BoardDTO>> getNoticeBoards() {
        log.debug("REST request to get notice Boards");
        return ResponseEntity.ok(boardService.findAllNotices());
    }

    /**
     * {@code GET /boards/by-user/:userId} : Get all boards by a user.
     *
     * @param userId the ID of the user
     * @return list of boards created by the specified user
     */
    @Operation(summary = "Get boards by user", description = "Retrieves all boards created by a specific user.")
    @ApiResponse(responseCode = "200", description = "Boards retrieved successfully")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<BoardDTO>> getBoardsByUser(@PathVariable Long userId) {
        log.debug("REST request to get Boards by user : {}", userId);
        return ResponseEntity.ok(boardService.findByUser(userId));
    }
}
