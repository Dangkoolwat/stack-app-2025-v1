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
 * UploadResource
 * ------------------------------------------------------------------
 * 파일 다운로드 / 인라인 미리보기를 제공하는 REST 컨트롤러입니다.
 *
 * H-3 개선: 파일을 전체 byte[]로 heap에 로드하지 않고,
 *           StorageService.loadAsStream()을 통해 StreamingResponseBody로
 *           직접 클라이언트에 파이프합니다. 대용량 파일 OOM 위험을 방지합니다.
 * L-3 개선: User-Agent 분기 제거, RFC 5987 filename*=UTF-8'' 단일 방식 사용.
 * ------------------------------------------------------------------
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
    // 공개 파일 다운로드
    // --------------------------------------------------------

    @Operation(summary = "공개 파일 다운로드")
    @ApiResponse(responseCode = "200", description = "파일 다운로드 성공")
    @ApiResponse(responseCode = "403", description = "Access denied (private file)")
    @ApiResponse(responseCode = "404", description = "File not found")
    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Long id) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
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
                    log.error("[DOWNLOAD] Stream error for path={}", filePath, e);
                    throw e;
                }
            };

            log.info("[DOWNLOAD] Streaming - id={}, filename={}", id, upload.getSourceFilename());
            return ResponseEntity.ok().headers(headers).body(body);

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
    public ResponseEntity<StreamingResponseBody> previewFile(@PathVariable Long id) {
        try {
            Upload upload = uploadService.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Upload ID not found: " + id));

            if (upload.isDeleted()) {
                throw new FileNotFoundException("삭제된 파일입니다: " + id);
            }
            if (!upload.isPublic()) {
                throw new SecurityException("비공개 파일은 인증된 사용자만 접근 가능합니다.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                Optional.ofNullable(upload.getMimeType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ));
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename*=UTF-8''" + encodeFilename(upload.getSourceFilename()));

            final String filePath = upload.getFilePath();
            StreamingResponseBody body = outputStream -> {
                try (InputStream is = storageService.loadAsStream(filePath)) {
                    is.transferTo(outputStream);
                } catch (IOException e) {
                    log.error("[PREVIEW] Stream error for path={}", filePath, e);
                    throw e;
                }
            };

            return ResponseEntity.ok().headers(headers).body(body);

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
