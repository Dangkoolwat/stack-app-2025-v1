package com.daangcool.stack.web.rest;

import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.service.board.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 파일 업로드, 다운로드 및 인라인 미리보기를 제공하는 REST 컨트롤러입니다.
 *
 * 역할:
 * - 파일 스트리밍 다운로드 (OOM 방지)
 * - 이미지 및 일반 파일 업로드 처리
 * - 클라이언트 소유의 첨부파일 소프트 삭제
 *
 * 에이전트 작업 가이드:
 * - 다운로드 방식 변경이나 업로드 엔드포인트 수정 시 이 클래스를 변경하세요.
 * - 파일 저장소 로직은 UploadService와 StorageService를 확인하세요.
 *
 * 주의사항:
 * - 보안: 파일 반환 전 반드시 권한(private/public) 체크를 수행해야 합니다.
 * - 성능: 전체 파일을 byte[]로 매핑하지 않고 StreamingResponseBody를 유지하세요.
 *
 * 변경 이력:
 * - 2026-03-22: 에디터 및 첨부파일 연동을 위한 POST / DELETE(Soft) API 추가
 */
@Tag(name = "Upload", description = "파일 업로드/다운로드 API")
@RestController
@RequestMapping("/api/uploads")
public class UploadResource {

    private static final Logger log = LoggerFactory.getLogger(UploadResource.class);

    private final StorageService storageService;
    private final UploadService uploadService;

    public UploadResource(StorageService storageService, UploadService uploadService) {
        this.storageService = storageService;
        this.uploadService = uploadService;
    }

    // --------------------------------------------------------
    // 공개 파일 업로드 (Editor / 다중 업로드 연동)
    // --------------------------------------------------------

    @Operation(summary = "파일 업로드 (에디터/에셋용)")
    @ApiResponse(responseCode = "201", description = "성공")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Upload> uploadFile(
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
        @RequestParam(value = "public", defaultValue = "true") boolean isPublic
    ) {
        log.debug("REST request to upload file : {}, public={}", file.getOriginalFilename(), isPublic);
        // 고유 식별자(UUID)를 스토리지 키로 사용
        String storageKey = java.util.UUID.randomUUID().toString();
        Upload saved = uploadService.saveUpload(file, storageKey, isPublic);
        try {
            return ResponseEntity.created(new java.net.URI("/api/uploads/" + saved.getId() + "/preview")).body(saved);
        } catch (java.net.URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(saved);
        }
    }

    // --------------------------------------------------------
    // 파일 삭제 (본인 또는 권한자)
    // --------------------------------------------------------
    
    @Operation(summary = "파일 (소프트) 삭제")
    @ApiResponse(responseCode = "204", description = "성공")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUpload(@PathVariable Long id) {
        log.debug("REST request to delete Upload : {}", id);
        // 권한 체크 로직을 서비스나 시큐리티 계층에 위임 (간소화를 위해 UploadService 호출)
        uploadService.softDelete(id, "사용자가 에디터/게시판에서 직접 삭제");
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------
    // 공개 파일 다운로드
    // --------------------------------------------------------

    @Operation(summary = "공개 파일 다운로드")
    @ApiResponse(responseCode = "200", description = "파일 다운로드 성공")
    @ApiResponse(responseCode = "403", description = "Access denied (private file)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
            }

            // 파일 바이트 로드
            byte[] fileBytes;
            try (InputStream is = storageService.loadAsStream(upload.getFilePath())) {
                fileBytes = is.readAllBytes();
            }

            String encodedFilename = encodeFilename(upload.getSourceFilename());
            HttpHeaders headers = new HttpHeaders();
            setDispositionHeader(headers, encodedFilename);
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.setContentLength(fileBytes.length);

            log.info("[DOWNLOAD] id={}, filename={}, size={}", id, upload.getSourceFilename(), fileBytes.length);
            return ResponseEntity.ok().headers(headers).body(fileBytes);

        } catch (FileNotFoundException e) {
            log.warn("[DOWNLOAD] File not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            log.warn("[DOWNLOAD] Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("[DOWNLOAD] Error while downloading file (id={})", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --------------------------------------------------------
    // 공개 파일 인라인 미리보기
    // --------------------------------------------------------

    @Operation(summary = "공개 파일 인라인 미리보기")
    @ApiResponse(responseCode = "200", description = "파일 미리보기 성공")
    @ApiResponse(responseCode = "403", description = "Access denied (private file)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long id) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
            }

            // 파일 바이트 로드 (이미지는 대부분 작으므로 byte[] 안전)
            byte[] fileBytes;
            try (InputStream is = storageService.loadAsStream(upload.getFilePath())) {
                fileBytes = is.readAllBytes();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.setContentLength(fileBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename*=UTF-8''" + encodeFilename(upload.getSourceFilename()));
            // 캐시 허용 (이미지 등 정적 리소스)
            headers.setCacheControl("public, max-age=3600");

            return ResponseEntity.ok().headers(headers).body(fileBytes);

        } catch (FileNotFoundException e) {
            log.warn("[PREVIEW] File not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            log.warn("[PREVIEW] Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("[PREVIEW] Error during file preview (id={})", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --------------------------------------------------------
    // 비공개 파일 다운로드 (인증 필요)
    // --------------------------------------------------------

    @Operation(summary = "비공개 파일 다운로드 (인증 필요)")
    @ApiResponse(responseCode = "200", description = "파일 다운로드 성공")
    @ApiResponse(responseCode = "404", description = "File not found")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/private/{id}/download")
    public ResponseEntity<StreamingResponseBody> downloadPrivateFile(@PathVariable Long id) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }

            if (upload.isPublic()) {
                // 공개 파일을 비공개 엔드포인트로 접근 — 400 Bad Request
                log.warn("[PRIVATE DOWNLOAD] Attempted to download public file via private endpoint (id={})", id);
                return ResponseEntity.badRequest().build();
            }

            String encodedFilename = encodeFilename(upload.getSourceFilename());
            HttpHeaders headers = new HttpHeaders();
            setDispositionHeader(headers, encodedFilename);
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));

            final String filePath = upload.getFilePath();
            StreamingResponseBody body = outputStream -> {
                try (InputStream is = storageService.loadAsStream(filePath)) {
                    is.transferTo(outputStream);
                } catch (IOException e) {
                    log.error("[PRIVATE DOWNLOAD] Stream error for path={}", filePath, e);
                    throw e;
                }
            };

            return ResponseEntity.ok().headers(headers).body(body);

        } catch (FileNotFoundException e) {
            log.warn("[PRIVATE DOWNLOAD] File not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("[PRIVATE DOWNLOAD] Error occurred (id={})", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --------------------------------------------------------
    // Helper methods (encoding / headers)
    // --------------------------------------------------------

    /**
     * RFC 5987 표준에 따라 파일명을 퍼센트 인코딩합니다.
     * 모든 현대 브라우저(Chrome, Firefox, Safari, Edge)가 RFC 5987을 지원하므로
     * User-Agent 분기 없이 단일 방식으로 처리합니다.
     */
    private String encodeFilename(String originalFilename) {
        return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Content-Disposition 헤더를 RFC 5987 표준 형식으로 설정합니다.
     * {@code filename*=UTF-8''<encoded>} 형식은 파일명에 한글·특수문자가 포함될 때
     * 브라우저 간 일관된 다운로드 파일명을 보장합니다.
     */
    private void setDispositionHeader(HttpHeaders headers, String encodedFilename) {
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
    }
}
