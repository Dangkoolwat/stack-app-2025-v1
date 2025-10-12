package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.util.UploadFileUtils;
import com.daangcool.stack.web.exception.FileUploadException;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * OciFileStorageService
 * -----------------------------------------------------------
 * Oracle Cloud Infrastructure Object Storage 모드에서
 * 파일 업로드/다운로드/삭제를 수행하는 서비스 구현체입니다.
 *
 * 특징:
 * - SDK v3 기준 API (putObjectBody, getObject 등) 사용
 * - @ConditionalOnProperty(application.file.storage-type=CLOUD_OCI)
 * - LOCAL/SHARE와 동일한 StorageService 인터페이스를 구현
 * -----------------------------------------------------------
 */
@Slf4j
@Service
@ConditionalOnProperty(
    name = "application.file.storage-type",
    havingValue = "CLOUD_OCI"
)
public class OciFileStorageService implements StorageService {

    private final ApplicationProperties properties;
    private final ObjectStorageClient client;
    private final String namespace;
    private final String bucketName;

    @Autowired
    public OciFileStorageService(ApplicationProperties properties) {
        this.properties = properties;

        try {
            // 기본 OCI 설정 파일 위치: ~/.oci/config, profile: DEFAULT
            ConfigFileAuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");

            this.client = ObjectStorageClient.builder()
                .region(Region.AP_SEOUL_1) // 필요 시 application.yml에서 주입 가능
                .build(provider);

            // namespace, bucketName은 application.yml에 정의 권장
            this.namespace = System.getProperty("oci.namespace", "your_namespace");
            this.bucketName = System.getProperty("oci.bucket", "your_bucket");

            log.info("[STORAGE-OCI] Initialized OCI Object Storage client (namespace={}, bucket={})",
                namespace, bucketName);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize OCI ObjectStorage client", e);
        }
    }

    // ===============================================================
    // 파일 업로드
    // ===============================================================
    @Override
    public String store(MultipartFile file, String subFolder) {
        try (InputStream inputStream = file.getInputStream()) {

            // 업로드될 Object Storage 경로
            String storagePath = UploadFileUtils.buildWebPath(
                subFolder,
                java.time.LocalDate.now().getYear() + "/" + java.time.LocalDate.now().getMonthValue(),
                file.getOriginalFilename());

            // OCI SDK 3.x 기준: InputStream을 putObjectBody()로 지정
            PutObjectRequest request = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(storagePath.substring(1)) // 경로 맨 앞의 '/' 제거
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .putObjectBody(inputStream)
                .build();

            PutObjectResponse response = client.putObject(request);
            log.info("[STORAGE-OCI] Uploaded file '{}' (ETag={})", storagePath, response.getETag());

            // OCI Object Storage 의 퍼블릭 URL 또는 상대 경로 반환
            return String.format("https://objectstorage.ap-seoul-1.oraclecloud.com/n/%s/b/%s/o/%s",
                namespace, bucketName, storagePath.substring(1));

        } catch (IOException e) {
            throw new FileUploadException("파일 스트림 처리 중 오류", e);
        } catch (Exception e) {
            throw new FileUploadException("OCI Object Storage 업로드 실패", e);
        }
    }

    // ===============================================================
    // 파일 다운로드
    // ===============================================================
    @Override
    public byte[] loadAsResource(String storageFilePath) {
        try {
            // 웹 경로에서 객체 키만 추출
            String objectKey = extractObjectKey(storageFilePath);

            GetObjectRequest request = GetObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectKey)
                .build();

            GetObjectResponse response = client.getObject(request);
            byte[] bytes = response.getInputStream().readAllBytes();
            response.getInputStream().close();

            log.info("[STORAGE-OCI] Loaded object: {}", objectKey);
            return bytes;

        } catch (Exception e) {
            throw new FileUploadException("OCI 파일 다운로드 실패", e);
        }
    }

    // ===============================================================
    // 파일 삭제
    // ===============================================================
    @Override
    public void delete(String storageFilePath) {
        try {
            String objectKey = extractObjectKey(storageFilePath);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectKey)
                .build();

            client.deleteObject(request);
            log.info("[STORAGE-OCI] Deleted object: {}", objectKey);

        } catch (Exception e) {
            log.error("[STORAGE-OCI] 파일 삭제 실패: {}", storageFilePath, e);
        }
    }

    // ===============================================================
    //  보조 메서드
    // ===============================================================

    /**
     * OCI Object Storage URL이나 상대 경로에서 객체 키(ObjectName)를 추출합니다.
     * 예: https://objectstorage.../n/myNs/b/myBucket/o/NOTICE/2025/10/a.png → NOTICE/2025/10/a.png
     */
    private String extractObjectKey(String storageFilePath) {
        if (storageFilePath == null) return "";
        int idx = storageFilePath.indexOf("/o/");
        return (idx > -1)
            ? storageFilePath.substring(idx + 3)
            : storageFilePath.replaceFirst("^/", "");
    }
}

