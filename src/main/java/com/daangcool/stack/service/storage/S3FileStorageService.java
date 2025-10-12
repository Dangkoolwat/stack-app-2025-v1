package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ✅ AWS S3 기반 파일 스토리지 서비스
 *
 * - 파일은 지정된 S3 버킷에 업로드됩니다.
 * - 저장 후 반환 URL은 S3 공개 URL 혹은 프록시 경로입니다.
 */
@Service
@ConditionalOnProperty(name = "application.file.storage-type", havingValue = "CLOUD_S3")
public class S3FileStorageService implements StorageService {

    private final ApplicationProperties properties;
    private final S3Client s3Client;
    private final String bucket;

    @Autowired
    public S3FileStorageService(ApplicationProperties properties) {
        this.properties = properties;

        // 💡 환경 변수 또는 yml에서 설정된 S3 자격 정보 사용
        this.bucket = properties.getFile().getSharePath(); // sharePath → bucket 이름으로 사용

        this.s3Client = S3Client.builder()
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "ap-northeast-2")))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        System.getenv("AWS_ACCESS_KEY_ID"),
                        System.getenv("AWS_SECRET_ACCESS_KEY")
                    )
                )
            )
            .build();
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        try {
            // 날짜 기반 폴더 경로
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String ext = getExtension(file.getOriginalFilename());
            String key = subFolder + "/" + dateFolder + "/" + UUID.randomUUID() + ext;

            // S3 업로드 요청
            PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 💡 URL 인코딩된 접근 경로 반환
            return String.format("https://%s.s3.amazonaws.com/%s",
                bucket,
                URLEncoder.encode(key, StandardCharsets.UTF_8)
            );

        } catch (IOException e) {
            throw new RuntimeException(" S3 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void delete(String storageFilePath) {
        try {
            // 파일 키 추출 (URL에서 버킷명 이후)
            String key = extractKeyFromUrl(storageFilePath);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        } catch (Exception e) {
            throw new RuntimeException(" S3 파일 삭제 실패: " + storageFilePath, e);
        }
    }

    @Override
    public byte[] loadAsResource(String storageFilePath) {
        try {
            String key = extractKeyFromUrl(storageFilePath);
            GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getReq);
            return response.asByteArray();

        } catch (Exception e) {
            throw new RuntimeException(" S3 파일 다운로드 실패: " + storageFilePath, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot > -1) ? filename.substring(dot) : "";
    }

    private String extractKeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx > -1) {
            return url.substring(idx + ".amazonaws.com/".length());
        }
        return url;
    }
}
