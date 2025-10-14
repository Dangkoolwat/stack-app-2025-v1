package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.util.UploadFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * LocalDefaultFileStorageService
 * -----------------------------------------------------------
 * LOCAL 모드 (기본값)를 처리하는 파일 저장소 서비스 구현체입니다.
 *
 * - 실행 경로(user.dir) 기준으로 파일 저장
 * - 공개/비공개 파일을 구분 저장 (/uploads/public, /uploads/private)
 * -----------------------------------------------------------
 */
@Service
@ConditionalOnProperty(name = "application.file.storage-type", havingValue = "LOCAL", matchIfMissing = true)
public class LocalDefaultFileStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalDefaultFileStorageService.class);

    private final ApplicationProperties properties;
    private final String rootLocation;

    @Autowired
    public LocalDefaultFileStorageService(ApplicationProperties properties) {
        this.properties = properties;
        this.rootLocation = Paths.get(System.getProperty("user.dir"), properties.getFile().getUploadDir()).toString();

        try {
            File rootDir = new File(this.rootLocation);
            if (!rootDir.exists()) {
                boolean created = rootDir.mkdirs();
                if (created) {
                    log.info("[STORAGE-LOCAL] Created root upload directory: {}", this.rootLocation);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize local storage: " + this.rootLocation, e);
        }
    }

    /**
     * 파일 저장 (공개/비공개 구분 포함)
     *
     * @param file 업로드 파일
     * @param subFolder 파일 구분용 하위 폴더 (예: NOTICE, PROFILE)
     * @param isPublic 공개 여부
     */
    @Override
    public String store(MultipartFile file, String subFolder, boolean isPublic) {
        try {
            String storagePath = UploadFileUtils.fileSave(this.rootLocation, subFolder, file, isPublic);
            // The UploadFileUtils.fileSave method now returns the full web path including /uploads.
            // So, we should not prepend properties.getFile().getUploadResourceDir() again.
            String webPath = storagePath; // Changed this line
            log.info("[STORAGE-LOCAL] Stored file (public={}): {}", isPublic, webPath);
            return webPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        // 기본값: 공개 파일로 처리 (백워드 호환)
        return store(file, subFolder, true);
    }

    @Override
    public void delete(String storageFilePath) {
        String webPrefix = properties.getFile().getUploadResourceDir();
        boolean deleted = UploadFileUtils.deleteFile(this.rootLocation, storageFilePath, webPrefix);
        if (deleted) {
            log.info("[STORAGE-LOCAL] Deleted file: {}", storageFilePath);
        }
    }

    @Override
    public byte[] loadAsResource(String storageFilePath) {
        try {
            String webPrefix = properties.getFile().getUploadResourceDir();
            String relativePath = storageFilePath.startsWith(webPrefix)
                ? storageFilePath.substring(webPrefix.length())
                : storageFilePath;

            Path filePath = Paths.get(this.rootLocation, relativePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource.getContentAsByteArray();
            } else {
                throw new java.io.FileNotFoundException("Could not read file: " + storageFilePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error resolving file path: " + storageFilePath, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + storageFilePath, e);
        }
    }
}
