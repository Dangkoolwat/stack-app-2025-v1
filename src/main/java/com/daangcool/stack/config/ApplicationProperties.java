package com.daangcool.stack.config;

import com.daangcool.stack.domain.enumeration.FileStorageType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Stack.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */

@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Redis redis = new Redis();

    //Liquibase
    private final Liquibase liquibase = new Liquibase();

    @Setter
    @Getter
    public static class Liquibase {

        private Boolean asyncStart = true;

    }


    //Logging
    private final Logging logging = new Logging();

    @Getter
    @Setter
    public static class Logging {
        private String filePath;
        private String maxFileSize;
        private int maxHistory;
        private String totalSizeCap;

    }

    private final File file = new File();


    @Getter
    @Setter
    public static class File {
        private FileStorageType storageType = FileStorageType.LOCAL;    // 기본값: LOCAL
        private String uploadDir = "/uploads";              // 웹 접근 경로 및 로컬 저장소 접미사 (기본값: /uploads)
        private String sharePath = "/share";                 // 공유 폴더 경로 또는 클라우드 버킷 이름 (기본값: /share)
        private String publicPath = "/uploads/public";       //  공개 리소스 경로 (정적 매핑 대상)
        private String privatePath = "/uploads/private";     //  비공개 리소스 경로 (Controller 접근 대상)
        private String[] allowedMimeTypes = { "image/jpeg", "image/png", "image/gif", "application/pdf" };
        private String[] allowedExtensions = { "jpg", "jpeg", "png", "gif", "pdf" };

        public String getUploadResourceDir() { return uploadDir; }
    }

    /**
     * 애플리케이션 공용 Redis 설정 (SSOT).
     * <p>
     * - server: 단일 서버는 1개 URL, 클러스터는 콤마로 여러 개 URL 지정 가능
     * - cluster: true 인 경우 Redisson 클러스터 모드로 동작
     */
    @Getter
    @Setter
    public static class Redis {
        private String[] server;
        private boolean cluster = false;
    }

    private final AuthCache authCache = new AuthCache();

    /**
     * 인증 2차 캐시 설정
     * Redis 에 UserAuthCacheDto 를 저장하는 TTL 을 외부화합니다.
     * Access Token 유효기간보다 짧게 유지하는 것을 권장합니다 (기본: 5분).
     */
    @Getter
    @Setter
    public static class AuthCache {
        /** 캐시 TTL (분). 기본값 5분. */
        private long ttlMinutes = 5;
    }

    private final RateLimit rateLimit = new RateLimit();

    /**
     * Rate Limiting 설정을 위한 프로퍼티 그룹 (W-1)
     */
    @Getter
    @Setter
    public static class RateLimit {

        private Policy authenticate = new Policy(10, 5);
        private Policy register = new Policy(5, 30);
        private Policy resetPasswordInit = new Policy(3, 15);
        private Policy otpRequest = new Policy(5, 10);
        private Policy otpVerify = new Policy(10, 10);

        /**
         * 엔드포인트별 세부 정책
         */
        @Getter
        @Setter
        public static class Policy {
            private long tokens;
            private long durationMinutes;

            public Policy() {}

            public Policy(long tokens, long durationMinutes) {
                this.tokens = tokens;
                this.durationMinutes = durationMinutes;
            }
        }
    }
}
