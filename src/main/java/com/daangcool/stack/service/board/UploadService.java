package com.daangcool.stack.service.board;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.domain.enumeration.FileStorageType;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.common.util.UploadFileUtils;
import com.daangcool.stack.common.exception.FileStorageException;
import com.daangcool.stack.service.dto.UploadDTO;
import com.daangcool.stack.common.exception.InvalidFileException;
import com.daangcool.stack.common.exception.UploadNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 파일 업로드, 삭제, 공개/비공개 전환(물리 이동), 메타데이터 관리를 담당하는 서비스.
 *
 * 설계 원칙:
 *  - 파일 시스템과 DB의 정합성을 유지한다.
 *  - 공개 여부(isPublic)에 따라 저장 경로를 분리한다.
 *  - 공개/비공개 전환 시 실제 파일을 이동시키고 DB 경로도 함께 갱신한다.
 *  - 클라우드 스토리지(CLOUD_*)는 SDK 기반 복사/삭제가 필요하므로 별도 분기한다.
 */
@Service
@Transactional
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    public static final String CACHE_UPLOAD_BY_ID = "UPLOAD_BY_ID";
    public static final String CACHE_UPLOAD_BY_BOARD = "UPLOAD_BY_BOARD";
    public static final String CACHE_UPLOAD_STATS = "UPLOAD_STATS";
    public static final String CACHE_UPLOAD_ALL = "UPLOAD_ALL";

    private final UploadRepository uploadRepository;
    private final StorageService storageService;
    private final ApplicationProperties fileStorageProperties;
    private final CacheManager cacheManager;
    private final Tika tika = new Tika();

    public UploadService(
        UploadRepository uploadRepository,
        StorageService storageService,
        ApplicationProperties fileStorageProperties,
        CacheManager cacheManager
    ) {
        this.uploadRepository = uploadRepository;
        this.storageService = storageService;
        this.fileStorageProperties = fileStorageProperties;
        this.cacheManager = cacheManager;
    }

    /**
     * 파일 업로드 및 메타데이터 저장.
     */
    public Upload saveUpload(MultipartFile file, String storageKey, boolean isPublic) {
        String detectedMimeType = validateFile(file);

        try {
            // 실제 파일 저장
            String storageFilePath = storageService.store(file, storageKey, isPublic);

            // 메타데이터 생성
            Upload upload = new Upload();
            upload.setStorageKey(storageKey);
            upload.setSourceFilename(file.getOriginalFilename());
            upload.setStorageFilename(FilenameUtils.getName(storageFilePath));
            upload.setFilePath(storageFilePath);
            upload.setFileSize(file.getSize());
            upload.setFileExtension(UploadFileUtils.getExtension(file.getOriginalFilename()));
            upload.setMimeType(detectedMimeType); // (C-4) 실제 감지된 MIME 타입 사용
            upload.setPublic(isPublic);

            // DB 저장
            Upload saved = uploadRepository.save(upload);
            clearUploadCaches(saved);
            log.info("[UPLOAD] uploaded id={}, path={}, public={}", saved.getId(), storageFilePath, isPublic);
            return saved;

        } catch (Exception e) {
            throw new FileStorageException("파일 업로드 처리 중 오류가 발생했습니다.", e);
        }
    }

    /** 논리 삭제 (Soft Delete) */
    public void softDelete(Long id, String reason) {
        uploadRepository.findById(id).ifPresentOrElse(upload -> {
            upload.setDeleted(true);
            upload.setDescription(reason);
            uploadRepository.save(upload);
            clearUploadCaches(upload);
            log.info("[UPLOAD] soft-deleted id={}", id);
        }, () -> {
            throw new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id);
        });
    }

    /** 물리 삭제 (Hard Delete) */
    public void hardDelete(Long id) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        try {
            storageService.delete(upload.getFilePath());
            uploadRepository.delete(upload);
            clearUploadCaches(upload);
            log.info("[UPLOAD] hard-deleted id={}, path={}", id, upload.getFilePath());
        } catch (Exception e) {
            throw new FileStorageException("파일 물리 삭제 처리 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    /**
     * 업로드 파일의 보안 및 유효성 검증 (C-4).
     * - 파일 비어있음 확인
     * - 확장자 화이트리스트 검사
     * - MIME 타입 화이트리스트 검사 (Apache Tika 사용)
     * - 확장자와 MIME 타입 일치 여부 확인 (스푸핑 방지)
     *
     * @return 감지된 MIME 타입
     */
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("업로드 파일은 비어 있을 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new InvalidFileException("파일명을 확인할 수 없습니다.");
        }

        // 1. 확장자 검증
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = List.of(fileStorageProperties.getFile().getAllowedExtensions());
        if (!allowedExtensions.contains(extension)) {
            log.warn("[SECURITY] Allowed extensions: {}, requested: {}", allowedExtensions, extension);
            throw new InvalidFileException("허용되지 않는 파일 확장자입니다: " + extension);
        }

        // 2. MIME 타입 검증 (Content-based detection)
        try (InputStream is = file.getInputStream()) {
            String detectedMimeType = tika.detect(is);
            List<String> allowedMimeTypes = List.of(fileStorageProperties.getFile().getAllowedMimeTypes());

            if (!allowedMimeTypes.contains(detectedMimeType)) {
                log.warn("[SECURITY] Allowed MIME types: {}, detected: {}", allowedMimeTypes, detectedMimeType);
                throw new InvalidFileException("허용되지 않는 파일 형식입니다: " + detectedMimeType);
            }

            // 3. 브라우저 제공 MIME 타입과 실제 감지된 타입 비교 (Mismatch check)
            String providedMimeType = file.getContentType();
            if (providedMimeType != null && !providedMimeType.equalsIgnoreCase(detectedMimeType)) {
                log.warn("[SECURITY] MIME type mismatch. Provided: {}, Detected: {}", providedMimeType, detectedMimeType);
            }

            return detectedMimeType;

        } catch (IOException e) {
            throw new InvalidFileException("파일 콘텐츠 분석 중 오류가 발생했습니다.");
        }
    }

    /** 공개/비공개 상태 전환 */
    public Upload changeVisibility(Long id, boolean targetIsPublic) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        if (upload.isPublic() == targetIsPublic) {
            return upload;
        }

        if (isCloudStorage()) {
            throw new UnsupportedOperationException("클라우드 스토리지의 공개 전환은 SDK 기반 복사/삭제 로직이 필요합니다.");
        }

        try {
            String currentWebPath = upload.getFilePath();
            String targetBaseDir = targetIsPublic
                ? fileStorageProperties.getFile().getPublicPath()
                : fileStorageProperties.getFile().getPrivatePath();

            String rootPath = getPhysicalRootLocation();
            String newWebPath = UploadFileUtils.moveFileBetweenScopes(rootPath, currentWebPath, targetBaseDir);

            upload.setPublic(targetIsPublic);
            upload.setFilePath(newWebPath);

            Upload saved = uploadRepository.save(upload);
            clearUploadCaches(saved);
            log.info("[UPLOAD] visibility changed id={}, public={}, newPath={}", id, targetIsPublic, newWebPath);
            return saved;

        } catch (Exception e) {
            throw new FileStorageException("공개 범위 변경 처리 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    /** 다운로드 횟수 증가 */
    public void increaseDownloadCount(Long id) {
        uploadRepository.findById(id).ifPresent(upload -> {
            long current = Optional.ofNullable(upload.getDownloadCount()).orElse(0L);
            upload.setDownloadCount(current + 1);
            uploadRepository.save(upload);
        });
    }

    /**
     * 단건 조회 (캐시 포함)
     * Redis 에는 UploadDTO(단순 타입) 저장 → Hibernate Proxy @class 문제 원천 차단.
     * 캐시 히트 시 DTO → Upload 경량 복원 객체 반환 (DB 재조회 없음).
     * 캐시 미스 시 DB 조회 후 DTO 저장.
     */
    @Transactional(readOnly = true)
    public Optional<Upload> findById(Long id) {
        Cache cache = cacheManager.getCache(CACHE_UPLOAD_BY_ID);
        if (cache != null) {
            UploadDTO cached = cache.get(id, UploadDTO.class);
            if (cached != null) {
                log.debug("[UPLOAD CACHE] Cache hit for id={}", id);
                return Optional.of(toUploadEntity(cached));
            }
        }
        Optional<Upload> upload = uploadRepository.findById(id);
        upload.ifPresent(u -> {
            if (cache != null) cache.put(id, new UploadDTO(u));
        });
        return upload;
    }

    /**
     * 게시글별 업로드 목록 (캐시 포함)
     * Redis 에는 List<UploadDTO> 저장 → LazyLoad / Proxy 문제 없음.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Upload> findAllByBoard(Long boardId) {
        Cache cache = cacheManager.getCache(CACHE_UPLOAD_BY_BOARD);
        if (cache != null) {
            List<UploadDTO> cached = (List<UploadDTO>) cache.get(boardId, List.class);
            if (cached != null) {
                log.debug("[UPLOAD CACHE] Cache hit for boardId={}", boardId);
                return cached.stream().map(this::toUploadEntity).toList();
            }
        }
        List<Upload> uploads = uploadRepository.findAllByBoard_IdOrderByIdAsc(boardId);
        if (cache != null && !uploads.isEmpty()) {
            cache.put(boardId, uploads.stream().map(UploadDTO::new).toList());
        }
        return uploads;
    }

    /** 스토리지 타입이 클라우드 계열인지 여부 */
    private boolean isCloudStorage() {
        FileStorageType type = fileStorageProperties.getFile().getStorageType();
        return type != null && type.name().startsWith("CLOUD");
    }

    /** 물리 루트 경로 계산 */
    private String getPhysicalRootLocation() {
        if (fileStorageProperties.getFile().getStorageType() == FileStorageType.SHARE) {
            return fileStorageProperties.getFile().getSharePath() + fileStorageProperties.getFile().getUploadDir();
        }
        return System.getProperty("user.dir") + fileStorageProperties.getFile().getUploadDir();
    }

    /** 호환용 별칭 */
    @Deprecated
    public Upload moveFileVisibility(Long id, boolean targetIsPublic) {
        return changeVisibility(id, targetIsPublic);
    }

    // ---------------------------------------------------
    // 캐시 DTO → 엔티티 복원 헬퍼 (호출부 인터페이스 유지용)
    // board 연관 관계는 null — 파일 메타데이터 조회 전용으로 충분합니다.
    // board 정보가 필요한 로직에서는 DB 를 직접 조회하세요.
    // ---------------------------------------------------
    private Upload toUploadEntity(UploadDTO dto) {
        Upload u = new Upload();
        u.setId(dto.getId());
        u.setStorageKey(dto.getStorageKey());
        u.setSourceFilename(dto.getSourceFilename());
        u.setStorageFilename(dto.getStorageFilename());
        u.setFilePath(dto.getFilePath());
        u.setFileSize(dto.getFileSize());
        u.setFileExtension(dto.getFileExtension());
        u.setMimeType(dto.getMimeType());
        u.setDownloadCount(dto.getDownloadCount());
        u.setPublic(dto.isPublic());
        u.setDeleted(dto.isDeleted());
        return u;
    }

    // ---------------------------------------------------
    // 캐시 클리어 유틸리티
    // ---------------------------------------------------
    private void clearUploadCaches(Upload upload) {
        if (upload == null) return;
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_BY_ID)).evictIfPresent(upload.getId());
            if (upload.getBoard() != null) {
                Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_BY_BOARD))
                    .evictIfPresent(upload.getBoard().getId());
            }
            Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_STATS)).clear();
        } catch (Exception e) {
            log.warn("[UPLOAD CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }
}
