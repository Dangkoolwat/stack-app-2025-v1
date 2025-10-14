package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.common.util.UploadFileUtils;
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
 * ShareFileStorageService
 * -----------------------------------------------------------
 * 공유 폴더 기반의 파일 저장소 서비스 구현체입니다.
 *
 * - 외부 공유 경로(properties.sharePath)를 기준으로 파일 저장
 * - 공개/비공개 파일 경로 분리 지원
 * -----------------------------------------------------------
 */
@Service
@ConditionalOnProperty(name = "application.file.storage-type", havingValue = "SHARE")
public class ShareFileStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(ShareFileStorageService.class);

    private final ApplicationProperties properties;
    private final String rootLocation;

    @Autowired
    public ShareFileStorageService(ApplicationProperties properties) {
        this.properties = properties;
        this.rootLocation = Paths.get(properties.getFile().getSharePath(), properties.getFile().getUploadDir()).toString();

        try {
            File rootDir = new File(this.rootLocation);
            if (!rootDir.exists()) {
                boolean created = rootDir.mkdirs();
                if (created) {
                    log.info("[STORAGE-SHARE] Created shared upload directory: {}", this.rootLocation);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize shared storage: " + this.rootLocation, e);
        }
    }
    @Override
    public String store(MultipartFile file, String subFolder, boolean isPublic) {
        try {
            String storagePath = UploadFileUtils.fileSave(this.rootLocation, subFolder, file, isPublic);
            String webPath = properties.getFile().getUploadResourceDir() + storagePath;
            log.info("[STORAGE-SHARE] Stored file (public={}): {}", isPublic, webPath);
            return webPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        return store(file, subFolder, true);
    }

    @Override
    public void delete(String storageFilePath) {
        String webPrefix = properties.getFile().getUploadResourceDir();
        boolean deleted = UploadFileUtils.deleteFile(this.rootLocation, storageFilePath, webPrefix);
        if (deleted) {
            log.info("[STORAGE-SHARE] Deleted file: {}", storageFilePath);
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
