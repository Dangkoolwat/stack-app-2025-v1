package com.daangcool.stack.service.board;

import com.daangcool.stack.common.constant.CacheNames;

import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.service.softdelete.IncludeDeleted;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.domain.enumeration.FileStorageType;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.common.util.UploadFileUtils;
import com.daangcool.stack.common.exception.FileStorageException;
import com.daangcool.stack.service.dto.UploadDTO;
import com.daangcool.stack.common.exception.InvalidFileException;
import com.daangcool.stack.common.exception.UploadNotFoundException;
import com.daangcool.stack.service.GlobalSettingsService;
import com.daangcool.stack.domain.vo.FileTypePolicy;
import com.daangcool.stack.domain.vo.FileUploadDefaults;
import com.daangcool.stack.service.dto.SettingsDTO;
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
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * 파일 업로드, 삭제, 공개/비공개 전환(물리 이동), 메타데이터 관리를 담당하는 서비스 클래스입니다.
 *
 * 역할:
 * - 파일 시스템과 DB의 정합성 유지
 * - 공개/비공개 전환에 따른 실제 파일 물리적 이동
 * - 보안 검증(Apache Tika MIME 분석) 및 캐시 갱신
 * - 삭제(Soft, Hard, Purge) 처리
 *
 * 에이전트 작업 가이드:
 * - 파일 처리 로직 변경 시 StorageService와 정합성을 맞추세요.
 * - application.file.* 프로퍼티와 긴밀하게 연동됩니다.
 *
 * 주의사항:
 * - 캐시: 업로드 메타데이터 캐싱 로직이 있으므로 업데이트 시 반드시 evict 처리를 유지하세요.
 * - 클라우드 스토리지(CLOUD_*) 연동 시 파일 이동 로직이 각 SDK별로 분기되어야 합니다.
 *
 * 변경 이력:
 * - 2026-03-22: purgeSoftDeleted (소프트 삭제 일괄 물리 삭제) 로직 추가
 */
@Service
@Transactional
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    // -----------------------------------------------------
    // 캐시 (CacheNames 사용)
    // -----------------------------------------------------

    private final UploadRepository uploadRepository;
    private final StorageService storageService;
    private final ApplicationProperties fileStorageProperties;
    private final GlobalSettingsService globalSettingsService;
    private final CacheManager cacheManager;
    private final com.daangcool.stack.security.ResourceAuthorizationService resourceAuthorizationService;
    private final Tika tika = new Tika();

    public UploadService(
        UploadRepository uploadRepository,
        StorageService storageService,
        ApplicationProperties fileStorageProperties,
        GlobalSettingsService globalSettingsService,
        CacheManager cacheManager,
        com.daangcool.stack.security.ResourceAuthorizationService resourceAuthorizationService
    ) {
        this.uploadRepository = uploadRepository;
        this.storageService = storageService;
        this.fileStorageProperties = fileStorageProperties;
        this.globalSettingsService = globalSettingsService;
        this.cacheManager = cacheManager;
        this.resourceAuthorizationService = resourceAuthorizationService;
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
            // [SEC] 작성자 또는 관리자만 삭제 가능
            resourceAuthorizationService.validateOwnerOrAdminByLogin(upload.getCreatedBy(), "upload", "unauthorized");

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
    @IncludeDeleted
    public void hardDelete(Long id) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        // [SEC] 작성자 또는 관리자만 삭제 가능
        resourceAuthorizationService.validateOwnerOrAdminByLogin(upload.getCreatedBy(), "upload", "unauthorized");

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
     * 소프트 삭제된 파일 목록 조회 (D-19).
     * 
     * 리소스 관리(휴지통) 화면에서 삭제된 파일들을 미리보기 위해 사용합니다.
     * @return 삭제된 파일의 메타데이터 목록 (UploadDTO 리스트)
     */
    @Transactional(readOnly = true)
    @IncludeDeleted
    public List<UploadDTO> purgeSoftDeletedPreview() {
        return uploadRepository.findAllDeletedFiles().stream()
            .map(UploadDTO::new)
            .collect(Collectors.toList());
    }

    /** 
     * 전체 소프트 삭제 파일 일괄 하드 삭제 (가비지 컬렉팅).
     * 
     * 주의: 이 메서드는 DB 레코드를 지울 뿐만 아니라 실제 스토리지(로컬/클라우드)의 파일도 영구 제거합니다.
     * @return 삭제된 파일의 총 개수
     */
    @IncludeDeleted
    public int purgeSoftDeleted() {
        List<Upload> deletedFiles = uploadRepository.findAllDeletedFiles();
        int count = 0;
        for (Upload file : deletedFiles) {
            try {
                storageService.delete(file.getFilePath());
                uploadRepository.delete(file);
                // 캐시는 이미 softDelete 단계에서 삭제되었으므로 추가 처리 불필요
                log.info("[UPLOAD PURGE] hard-deleted orphaned/deleted file id={}", file.getId());
                count++;
            } catch (Exception e) {
                log.error("[UPLOAD PURGE] Error deleting file id={}", file.getId(), e);
            }
        }
        return count;
    }

    /**
     * 고아 파일 메타데이터 목록 조회
     */
    @Transactional(readOnly = true)
    @IncludeDeleted
    public List<UploadDTO> getOrphanUploads(Instant threshold) {
        return uploadRepository.findAllOrphanFiles(threshold).stream()
            .map(UploadDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * 여러 파일 선택 하드 삭제
     */
    @IncludeDeleted
    public int hardDelete(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            Optional<Upload> opt = uploadRepository.findById(id);
            if (opt.isPresent()) {
                Upload file = opt.get();
                try {
                    storageService.delete(file.getFilePath());
                    uploadRepository.delete(file);
                    clearUploadCaches(file);
                    count++;
                } catch (Exception e) {
                    log.error("[UPLOAD DELETE] Error hard-deleting file id={}", id, e);
                }
            }
        }
        return count;
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

        // 1. 글로벌 설정 조회 (캐시 활용)
        SettingsDTO settings = globalSettingsService.getSettings();
        FileUploadDefaults defaults = settings.getFileUploadDefaults();
        List<FileTypePolicy> policies = settings.getFileTypePolicies();

        // 2. 파일 기본 정보 추출
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        long fileSize = file.getSize();
        String detectedMimeType;

        // 3. MIME 타입 감지 (Content-based detection)
        try (InputStream is = file.getInputStream()) {
            detectedMimeType = tika.detect(is);
        } catch (IOException e) {
            throw new InvalidFileException("파일 콘텐츠 분석 중 오류가 발생했습니다.");
        }

        // 4. 정책 매칭 탐색 (확장자 + MIME 타입 동시 일치)
        FileTypePolicy matchedPolicy = policies.stream()
            .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
            .filter(p -> p.getAllowedExtensions().stream().anyMatch(extension::equalsIgnoreCase))
            .filter(p -> p.getAllowedMimeTypes().stream().anyMatch(detectedMimeType::equalsIgnoreCase))
            .findFirst()
            .orElse(null);

        // 5. 정책 기반 검증
        if (matchedPolicy != null) {
            log.debug("[UPLOAD POLICY] Matched policy: {}", matchedPolicy.getKey());
            if (fileSize > matchedPolicy.getMaxFileSizeBytes()) {
                throw new InvalidFileException(String.format("[%s] 타입의 최대 허용 용량(%d MB)을 초과했습니다. (현재: %.2f MB)",
                    matchedPolicy.getLabel(),
                    matchedPolicy.getMaxFileSizeBytes() / (1024 * 1024),
                    (double) fileSize / (1024 * 1024)));
            }
        } else {
            // 매칭되는 개별 정책이 없는 경우
            if (defaults.isBlockUnmatched()) {
                log.warn("[SECURITY] No matching policy found for extension: {} and MIME: {}. Blocked by default.", extension, detectedMimeType);
                throw new InvalidFileException(String.format("허용되지 않는 파일 형식 또는 확장자입니다. (확장자: %s, 형식: %s)", extension, detectedMimeType));
            }

            // 전역 기본값 기반 검증
            if (fileSize > defaults.getDefaultMaxFileSizeBytes()) {
                throw new InvalidFileException(String.format("전역 최대 허용 용량(%d MB)을 초과했습니다. (현재: %.2f MB)",
                    defaults.getDefaultMaxFileSizeBytes() / (1024 * 1024),
                    (double) fileSize / (1024 * 1024)));
            }
        }

        // 6. 브라우저 제공 MIME 타입과 실제 감지된 타입 비교 (Mismatch check - 로깅만 수행)
        String providedMimeType = file.getContentType();
        if (providedMimeType != null && !providedMimeType.equalsIgnoreCase(detectedMimeType)) {
            log.warn("[SECURITY] MIME type mismatch. Provided: {}, Detected: {}", providedMimeType, detectedMimeType);
        }

        return detectedMimeType;
    }

    /** 공개/비공개 상태 전환 */
    public Upload changeVisibility(Long id, boolean targetIsPublic) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        // [SEC] 작성자 또는 관리자만 가시성 변경 가능
        resourceAuthorizationService.validateOwnerOrAdminByLogin(upload.getCreatedBy(), "upload", "unauthorized");

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
        Cache cache = cacheManager.getCache(CacheNames.UPLOAD_BY_ID);
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
        Cache cache = cacheManager.getCache(CacheNames.UPLOAD_BY_BOARD);
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

    /**
     * 비공개 파일 다운로드용 업로드 조회 및 권한 검증.
     */
    @Transactional(readOnly = true)
    public Upload getAuthorizedPrivateUpload(Long id) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        if (upload.isDeleted()) {
            throw new UploadNotFoundException("삭제된 파일입니다. id=" + id);
        }

        if (upload.isPublic()) {
            throw new BadRequestAlertException("공개 파일은 비공개 다운로드 경로로 접근할 수 없습니다.", "upload", "upload.public");
        }

        resourceAuthorizationService.validateOwnerOrAdminByLogin(upload.getCreatedBy(), "upload", "unauthorized");
        return upload;
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
            Objects.requireNonNull(cacheManager.getCache(CacheNames.UPLOAD_BY_ID)).evictIfPresent(upload.getId());
            if (upload.getBoard() != null) {
                Objects.requireNonNull(cacheManager.getCache(CacheNames.UPLOAD_BY_BOARD))
                    .evictIfPresent(upload.getBoard().getId());
            }
            Objects.requireNonNull(cacheManager.getCache(CacheNames.UPLOAD_STATS)).clear();
        } catch (Exception e) {
            log.warn("[UPLOAD CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }
}
