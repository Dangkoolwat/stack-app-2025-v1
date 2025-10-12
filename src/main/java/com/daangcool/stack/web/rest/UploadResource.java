package com.daangcool.stack.web.rest;

import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.service.board.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * REST controller for managing file uploads and downloads.
 *
 * <p>
 * User-facing API for:
 * - Downloading public or private files
 * - Inline preview of media (images, PDF, etc.)
 * </p>
 */
@RestController
@RequestMapping("/api/uploads")
@Tag(name = "Upload Resource", description = "Public File Access and Download API")
public class UploadResource {

    private static final Logger log = LoggerFactory.getLogger(UploadResource.class);

    private final UploadService uploadService;
    private final StorageService storageService;

    public UploadResource(UploadService uploadService, StorageService storageService) {
        this.uploadService = uploadService;
        this.storageService = storageService;
    }

    /**
     * {@code GET  /uploads/:id/download} : Download a public file.
     *
     * @param id the file ID
     * @param request the browser request for content disposition detection
     * @return {@link ResponseEntity} with file stream
     */
    @Operation(summary = "Download a public file", description = "Downloads a file by ID if it is publicly accessible.")
    @ApiResponse(responseCode = "200", description = "File downloaded successfully")
    @ApiResponse(responseCode = "403", description = "Access denied (private file)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
            }

            byte[] data = storageService.loadAsResource(upload.getFilePath());
            if (data == null || data.length == 0) {
                throw new FileNotFoundException("파일이 존재하지 않거나 손상되었습니다: " + upload.getFilePath());
            }

            String encodedFilename = encodeFilename(upload.getSourceFilename(), request);

            HttpHeaders headers = new HttpHeaders();
            setDispositionHeader(headers, request, encodedFilename);
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.setContentLength(data.length);

            log.info("[DOWNLOAD] Success - id={}, filename={}", id, upload.getSourceFilename());
            return ResponseEntity.ok().headers(headers).body(data);

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

    /**
     * {@code GET  /uploads/:id/preview} : Preview an image or PDF inline.
     *
     * @param id file ID
     * @param request request info
     * @return inline viewable file (e.g., image/pdf)
     */
    @Operation(summary = "Preview file inline", description = "Returns an image or document for browser inline preview.")
    @ApiResponse(responseCode = "200", description = "Preview rendered successfully")
    @ApiResponse(responseCode = "403", description = "Access denied (private file)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long id, HttpServletRequest request) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
            }

            byte[] data = storageService.loadAsResource(upload.getFilePath());
            if (data == null || data.length == 0) {
                throw new FileNotFoundException("파일이 존재하지 않거나 손상되었습니다: " + upload.getFilePath());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                encodeFilename(upload.getSourceFilename(), request) + "\"");

            return ResponseEntity.ok().headers(headers).body(data);

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

    /**
     * {@code GET  /uploads/private/:id/download} : Download a private file (authentication required).
     *
     * @param id file ID
     * @param request browser info
     * @return file stream
     */
    @Operation(summary = "Download private file", description = "Downloads a private file. Authentication required.")
    @ApiResponse(responseCode = "200", description = "Private file downloaded successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "File not found")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/private/{id}/download")
    public ResponseEntity<byte[]> downloadPrivateFile(@PathVariable Long id, HttpServletRequest request) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }

            if (upload.isPublic()) {
                return ResponseEntity.badRequest()
                    .body("이 파일은 공개 파일입니다. /api/uploads/{id}/download 경로를 사용하십시오."
                        .getBytes(StandardCharsets.UTF_8));
            }

            byte[] data = storageService.loadAsResource(upload.getFilePath());
            if (data == null || data.length == 0) {
                throw new FileNotFoundException("파일이 존재하지 않거나 손상되었습니다: " + upload.getFilePath());
            }

            String encodedFilename = encodeFilename(upload.getSourceFilename(), request);
            HttpHeaders headers = new HttpHeaders();
            setDispositionHeader(headers, request, encodedFilename);
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.setContentLength(data.length);

            return ResponseEntity.ok().headers(headers).body(data);

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

    private String encodeFilename(String originalFilename, HttpServletRequest request) {
        String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("").toLowerCase();
        if (ua.contains("trident") || ua.contains("msie")) {
            return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", " ");
        } else if (ua.contains("edge") || ua.contains("edg")) {
            return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
        } else if (ua.contains("chrome")) {
            return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return new String(originalFilename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        } else if (ua.contains("firefox")) {
            return "UTF-8''" + URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
        }
        return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void setDispositionHeader(HttpHeaders headers, HttpServletRequest request, String encodedFilename) {
        String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("").toLowerCase();
        if (ua.contains("firefox") || ua.contains("trident") || ua.contains("msie")) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"");
        } else {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
        }
    }
}
