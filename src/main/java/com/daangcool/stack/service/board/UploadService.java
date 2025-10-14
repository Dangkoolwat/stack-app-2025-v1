package com.daangcool.stack.service.board;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.domain.enumeration.FileStorageType;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.common.util.UploadFileUtils;
import com.daangcool.stack.common.exception.FileStorageException;
import com.daangcool.stack.common.exception.UploadNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 파일 업로드, 삭제, 공개/비공개 전환(물리 이동), 메타데이터 관리를 담당하는 서비스.
 *
 * 설계 원칙
 * - 파일 시스템과 DB의 정합성을 유지한다.
 * - 공개 여부(isPublic)에 따라 저장 경로를 분리한다.
 * - 공개/비공개 전환 시 실제 파일을 이동시키고 DB 경로도 함께 갱신한다.
 * - 클라우드 스토리지(CLOUD_*)는 SDK 기반 복사/삭제가 필요하므로 별도 분기한다.
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

    public UploadService(
        UploadRepository uploadRepository,
        StorageService storageService,
        ApplicationProperties fileStorageProperties, CacheManager cacheManager
    ) {
        this.uploadRepository = uploadRepository;
        this.storageService = storageService;
        this.fileStorageProperties = fileStorageProperties;
        this.cacheManager = cacheManager;
    }

    /**
     * 파일 업로드 및 메타데이터 저장.
     * 공개 여부(isPublic)에 따라 /uploads/public 또는 /uploads/private 하위에 저장된다.
     *
     * @param file       업로드 파일
     * @param storageKey 파일 분류용 키 (예: NOTICE, USER_PROFILE)
     * @param isPublic   공개 여부
     * @return 저장된 Upload 엔티티
     */
    public Upload saveUpload(MultipartFile file, String storageKey, boolean isPublic) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일은 비어 있을 수 없습니다.");
        }

        try {
            // 1) 실제 파일 저장 수행. StorageService는 내부적으로 UploadFileUtils.fileSave(...)를 사용하도록 구현되어 있어야 한다.
            String storageFilePath = storageService.store(file, storageKey, isPublic);

            // 2) 메타데이터 생성
            Upload upload = new Upload();
            upload.setStorageKey(storageKey);
            upload.setSourceFilename(file.getOriginalFilename());
            upload.setStorageFilename(FilenameUtils.getName(storageFilePath));
            upload.setFilePath(storageFilePath);
            upload.setFileSize(file.getSize());
            upload.setFileExtension(UploadFileUtils.getExtension(file.getOriginalFilename()));
            upload.setMimeType(file.getContentType());
            upload.setPublic(isPublic);

            // 3) DB 저장
            Upload saved = uploadRepository.save(upload);
            clearUploadCaches(saved); //  캐시 초기화
            log.info("[UPLOAD] uploaded id={}, path={}, public={}", saved.getId(), storageFilePath, isPublic);
            return saved;

        } catch (Exception e) {
            // 파일 시스템 또는 기타 오류는 일괄 래핑하여 상위 계층으로 전파한다.
            throw new FileStorageException("파일 업로드 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 논리 삭제(soft delete). 실제 파일은 유지하고, 조회에서 제외된다.
     *
     * @param id     Upload ID
     * @param reason 삭제 사유(감사 추적 용도)
     */
    public void softDelete(Long id, String reason) {
        uploadRepository.findById(id).ifPresentOrElse(upload -> {
            upload.setDeleted(true);
            upload.setDescription(reason);
            uploadRepository.save(upload);
            clearUploadCaches(upload); //  캐시 무효화
            log.info("[UPLOAD] soft-deleted id={}", id);
        }, () -> {
            throw new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id);
        });
    }

    /**
     * 물리 삭제(hard delete). 파일 시스템과 DB에서 모두 제거한다.
     *
     * @param id Upload ID
     */
    public void hardDelete(Long id) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        try {
            storageService.delete(upload.getFilePath());
            uploadRepository.delete(upload);
            clearUploadCaches(upload); //  캐시 무효화
            log.info("[UPLOAD] hard-deleted id={}, path={}", id, upload.getFilePath());
        } catch (Exception e) {
            throw new FileStorageException("파일 물리 삭제 처리 중 오류가 발생했습니다. id=" + id, e);
        }
    }



    /**
     * 공개/비공개 상태 전환. 실제 파일을 새 경로로 이동시키고 DB 경로 및 isPublic을 갱신한다.
     * 로컬/공유 스토리지에서만 동작하며, 클라우드 스토리지는 별도의 SDK 로직이 필요하다.
     *
     * @param id             Upload ID
     * @param targetIsPublic 변경할 공개 상태
     * @return 갱신된 Upload 엔티티
     */
    public Upload changeVisibility(Long id, boolean targetIsPublic) {
        Upload upload = uploadRepository.findById(id)
            .orElseThrow(() -> new UploadNotFoundException("업로드 메타데이터를 찾을 수 없습니다. id=" + id));

        // 상태 변경이 불필요하면 즉시 반환
        if (upload.isPublic() == targetIsPublic) {
            return upload;
        }

        // 클라우드 스토리지는 SDK 기반 복사/삭제가 필요하다.
        if (isCloudStorage()) {
            // 필요 시 StorageService에 move(복사/삭제) 전용 메서드를 확장해 위임하도록 설계한다.
            throw new UnsupportedOperationException("클라우드 스토리지의 공개 전환은 SDK 기반 복사/삭제 로직이 필요합니다.");
        }

        try {
            // 현재 웹 경로. 예: /uploads/public/NOTICE/2025/10/file.ext
            String currentWebPath = upload.getFilePath();

            // 타겟 기본 디렉토리. 예: /uploads/public 또는 /uploads/private
            String targetBaseDir = targetIsPublic
                ? fileStorageProperties.getFile().getPublicPath()
                : fileStorageProperties.getFile().getPrivatePath();

            // 물리 루트 경로. 예: {user.dir}/uploads 또는 {sharePath}/uploads
            String rootPath = getPhysicalRootLocation();

            // UploadFileUtils에 물리 이동을 위임하고, 이동 후의 새 웹 경로를 반환받는다.
            String newWebPath = UploadFileUtils.moveFileBetweenScopes(rootPath, currentWebPath, targetBaseDir);

            // 만약 moveFileBetweenScopes가 webPrefix를 요구하는 구현이라면 아래와 같이 호출한다.
            // String newWebPath = UploadFileUtils.moveFileBetweenScopes(rootPath, currentWebPath, targetBaseDir, fileStorageProperties.getUploadResourceDir());

            // DB 메타데이터 갱신
            upload.setPublic(targetIsPublic);
            upload.setFilePath(newWebPath);

            Upload saved = uploadRepository.save(upload);
            clearUploadCaches(saved); //  캐시 제거
            log.info("[UPLOAD] visibility changed id={}, public={}, newPath={}", id, targetIsPublic, newWebPath);
            return saved;

        } catch (Exception e) {
            throw new FileStorageException("공개 범위 변경 처리 중 오류가 발생했습니다. id=" + id, e);
        }
    }

    /**
     * 다운로드 횟수 증가. 동시성에 크게 민감하지 않다면 단순 증가로 충분하다.
     * 고도화가 필요하면 쿼리 기반 증가나 Redis 카운터로 오프로딩할 수 있다.
     */
    public void increaseDownloadCount(Long id) {
        uploadRepository.findById(id).ifPresent(upload -> {
            Long current = upload.getDownloadCount() == null ? 0L : upload.getDownloadCount();
            upload.setDownloadCount(current + 1);
            uploadRepository.save(upload);
        });
    }

    /**
     * 업로드 메타데이터 조회. 기본적으로 is_deleted=0 항목을 반환하도록
     * 리포지토리 레벨에서 @SQLRestriction 또는 쿼리 메서드로 제한되어야 한다.
     */
    @Transactional(readOnly = true)
    public Optional<Upload> findById(Long id) {
        Cache cache = cacheManager.getCache(CACHE_UPLOAD_BY_ID);
        if (cache != null) {
            Upload cached = cache.get(id, Upload.class);
            if (cached != null) {
                log.debug("[UPLOAD CACHE] Cache hit for id={}", id);
                return Optional.of(cached);
            }
        }
        Optional<Upload> upload = uploadRepository.findById(id);
        upload.ifPresent(u -> {
            if (cache != null) cache.put(id, u);
        });
        return upload;
    }

    @Transactional(readOnly = true)
    public List<Upload> findAllByBoard(Long boardId) {
        Cache cache = cacheManager.getCache(CACHE_UPLOAD_BY_BOARD);
        if (cache != null) {
            List<Upload> cached = cache.get(boardId, List.class);
            if (cached != null) {
                log.debug("[UPLOAD CACHE] Cache hit for boardId={}", boardId);
                return cached;
            }
        }
        List<Upload> uploads = uploadRepository.findAllByBoard_IdOrderByIdAsc(boardId);
        if (cache != null) cache.put(boardId, uploads);
        return uploads;
    }


    /**
     * 현재 스토리지 타입이 클라우드 계열인지 여부.
     * CLOUD_S3, CLOUD_OCI 등 "CLOUD" 접두 타입들을 클라우드로 분기한다.
     */
    private boolean isCloudStorage() {
        FileStorageType type = fileStorageProperties.getFile().getStorageType();
        return type != null && type.name().startsWith("CLOUD");
    }

    /**
     * 물리 루트 경로 계산.
     * LOCAL: {user.dir}{uploadDir}
     * SHARE: {sharePath}{uploadDir}
     */
    private String getPhysicalRootLocation() {
        if (fileStorageProperties.getFile().getStorageType() == FileStorageType.SHARE) {
            return fileStorageProperties.getFile().getSharePath() + fileStorageProperties.getFile().getUploadDir();
        }
        return System.getProperty("user.dir") + fileStorageProperties.getFile().getUploadDir();
    }

    /**
     * 호환용. 기존 코드가 사용하는 메서드명 유지용 별칭.
     * 내부적으로 changeVisibility(...)로 위임한다.
     */
    @Deprecated
    public Upload moveFileVisibility(Long id, boolean targetIsPublic) {
        return changeVisibility(id, targetIsPublic);
    }




    // ---------------------------------------------------
    // 캐시 클리어 유틸리티
    // ---------------------------------------------------
    private void clearUploadCaches(Upload upload) {
        if (upload == null) return;
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_BY_ID)).evictIfPresent(upload.getId());
            if (upload.getBoard() != null) {
                Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_BY_BOARD)).evictIfPresent(upload.getBoard().getId());
            }
            Objects.requireNonNull(cacheManager.getCache(CACHE_UPLOAD_STATS)).clear();
        } catch (Exception e) {
            log.warn("[UPLOAD CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }

}

