package com.daangcool.stack.web.rest;

import com.daangcool.stack.service.dto.TagDTO;
import com.daangcool.stack.service.board.TagService;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.daangcool.stack.domain.board.Tag}.
 *
 * <p>
 * Public API for:
 * - Tag creation (for editor use)
 * - Tag listing
 * - Prefix-based search (autocomplete)
 * </p>
 */
@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag Resource", description = "Public Tag Management API")
public class TagResource {

    private static final Logger log = LoggerFactory.getLogger(TagResource.class);
    private static final String ENTITY_NAME = "tag";

    private final TagService tagService;

    public TagResource(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * {@code POST  /tags} : Create a new tag.
     *
     * @param tagDTO the tag to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new tag
     * @throws BadRequestAlertException if the tag name is blank or duplicated
     */
    @Operation(summary = "Create a new tag", description = "Creates a new tag if it does not already exist.")
    @ApiResponse(responseCode = "201", description = "Tag created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or duplicate tag name")
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) throws URISyntaxException {
        log.debug("REST request to save Tag : {}", tagDTO);
        TagDTO result = tagService.save(tagDTO);
        return ResponseEntity
            .created(new URI("/api/tags/" + result.getId()))
            .body(result);
    }

    /**
     * {@code GET  /tags} : Get all tags.
     *
     * @return list of all active tags
     */
    @Operation(summary = "Get all tags", description = "Retrieves all active (non-deleted) tags.")
    @ApiResponse(responseCode = "200", description = "List of tags retrieved successfully")
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        log.debug("REST request to get all Tags");
        List<TagDTO> tags = tagService.findAll();
        return ResponseEntity.ok(tags);
    }

    /**
     * {@code GET  /tags/:id} : Get a single tag by ID.
     *
     * @param id the id of the tag
     * @return the tagDTO
     */
    @Operation(summary = "Get a tag by ID", description = "Fetches tag details by ID.")
    @ApiResponse(responseCode = "200", description = "Tag retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTag(@PathVariable Long id) {
        log.debug("REST request to get Tag : {}", id);
        Optional<TagDTO> tag = tagService.findOne(id);
        return tag.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@code GET  /tags/search} : Search tags by prefix.
     *
     * @param prefix search prefix (keyword)
     * @param limit  max results to return
     * @return list of matching tags
     */
    @Operation(summary = "Search tags by prefix", description = "Finds tags starting with the given prefix (autocomplete).")
    @ApiResponse(responseCode = "200", description = "Tags found successfully")
    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> searchByPrefix(
        @RequestParam String prefix,
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("REST request to search Tags by prefix: {}", prefix);
        List<TagDTO> results = tagService.searchByPrefix(prefix, limit);
        return ResponseEntity.ok(results);
    }
}
