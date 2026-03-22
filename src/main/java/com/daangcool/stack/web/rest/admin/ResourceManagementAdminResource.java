package com.daangcool.stack.web.rest.admin;

import com.daangcool.stack.service.board.BoardService;
import com.daangcool.stack.service.board.UploadService;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.service.dto.UploadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리소스 관리 (휴지통 및 하드 삭제) 전용 관리자 API.
 * 
 * 역할:
 * - 소프트 삭제된(is_deleted = true) 게시글 및 파일 목록을 통합 조회합니다.
 * - 선택된 항목들을 데이터베이스와 물리 저장소에서 영구 삭제(Hard Delete)합니다.
 * - 관리자(ROLE_ADMIN) 권한이 필수입니다.
 */
@RestController
@RequestMapping("/api/admin/resource-management")
@Tag(name = "00. 리소스 관리 (관리자)", description = "삭제된 리소스(게시글, 파일 등)의 복구 불가능한 영구 삭제 및 관리 API")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ResourceManagementAdminResource {

    private final Logger log = LoggerFactory.getLogger(ResourceManagementAdminResource.class);

    private final BoardService boardService;
    private final UploadService uploadService;

    public ResourceManagementAdminResource(BoardService boardService, UploadService uploadService) {
        this.boardService = boardService;
        this.uploadService = uploadService;
    }

    /**
     * 삭제된 모든 게시글 조회.
     * @return 삭제 상태인 게시글 목록 (BoardDTO 리스트)
     */
    @GetMapping("/boards")
    @Operation(summary = "삭제된 게시물 목록 조회", description = "소프트 삭제된 모든 게시물 정보를 리스트 형식으로 반환합니다.")
    public ResponseEntity<List<BoardDTO>> getAllDeletedBoards() {
        log.debug("REST request to get all deleted Boards");
        return ResponseEntity.ok(boardService.findAllDeleted());
    }

    /**
     * 삭제된 모든 파일(Upload) 메타데이터 조회.
     * @return 삭제 상태인 파일 목록 (UploadDTO 리스트)
     */
    @GetMapping("/uploads")
    @Operation(summary = "삭제된 파일 목록 조회", description = "소프트 삭제된 모든 파일(첨부파일, 이미지 등) 정보를 리스트 형식으로 반환합니다.")
    public ResponseEntity<List<UploadDTO>> getAllDeletedUploads() {
        log.debug("REST request to get all deleted Uploads");
        return ResponseEntity.ok(uploadService.purgeSoftDeletedPreview());
    }

    /**
     * 게시글 일괄 영구 삭제 (Hard Delete).
     * @param ids 삭제할 게시글 ID 목록
     * @return 삭제 완료 응답 (204 No Content)
     */
    @PostMapping("/boards/hard-delete")
    @Operation(summary = "게시물 일괄 영구 삭제", description = "선택된 게시물들을 데이터베이스에서 완전히 제거합니다. (복구 불가능)")
    public ResponseEntity<Void> hardDeleteBoards(@RequestBody List<Long> ids) {
        log.debug("REST request to hard delete Boards : {}", ids);
        for (Long id : ids) {
            boardService.hardDelete(id);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 파일 일괄 영구 삭제 (Hard Delete).
     * @param ids 삭제할 파일 ID 목록
     * @return 삭제 완료 응답 (204 No Content)
     */
    @PostMapping("/uploads/hard-delete")
    @Operation(summary = "파일 일괄 영구 삭제", description = "선택된 파일들을 DB와 물리 저장소(Storage)에서 완전히 삭제합니다. (복구 불가능)")
    public ResponseEntity<Void> hardDeleteUploads(@RequestBody List<Long> ids) {
        log.debug("REST request to hard delete Uploads : {}", ids);
        for (Long id : ids) {
            uploadService.hardDelete(id);
        }
        return ResponseEntity.noContent().build();
    }
}
