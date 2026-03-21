package com.daangcool.stack.config;

import com.daangcool.stack.domain.enumeration.FileStorageType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties specific to Stack App.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 *
 * 🤖 에이전트 가이드:
 * - Redis 설정: application.redis.*
 * - 파일 업로드: application.file.*
 * - 인증 캐시: application.auth-cache.*
 * - Rate Limit: application.rate-limit.*
 * - 보안 경로: application.security.*
 * - 캐시 TTL: application.cache.*
 */
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    /** 데이터베이스 설정 (application.database.*) */
    private final Database database = new Database();

    /** Redis 설정 (application.redis.*) */
    private final Redis redis = new Redis();

    /** 파일 업로드 및 저장소 설정 (application.file.*) */
    private final File file = new File();

    /** 인증 관련 캐시 설정 (application.auth-cache.*) */
    private final AuthCache authCache = new AuthCache();

    /** 요청 횟수 제한 설정 (application.rate-limit.*) */
    private final RateLimit rateLimit = new RateLimit();

    /** 보안 및 공개 경로 설정 (application.security.*) */
    private final Security security = new Security();

    /** 캐시 TTL 설정 (application.cache.*) */
    private final Cache cache = new Cache();

    /** Liquibase 비동기 실행 설정 (application.liquibase.*) */
    private final Liquibase liquibase = new Liquibase();

    /** 로깅 및 Logstash 설정 (application.logging.*) */
    private final Logging logging = new Logging();

    /** Redis 연결 설정 클래스 */
    @Getter
    @Setter
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password;
    }

    /** 파일 업로드 및 저장소 정책 클래스 */
    @Getter
    @Setter
    public static class File {
        private String uploadDir = "/uploads";          // 서버 로컬 저장 경로
        private String uploadResourceDir = "/uploads";   // 웹 접근용 리소스 경로 접두사
        private String publicPath = "/public";           // 공개 파일 경로
        private String privatePath = "/private";         // 비공개 파일 경로
        private FileStorageType storageType = FileStorageType.LOCAL; // 저장소 타입 (LOCAL, S3, SHARE)
        private String sharePath;                        // 공유 폴더 경로 (SHARE 타입인 경우)
        private String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"}; // 허용 확장자
        private String[] allowedMimeTypes = {"image/jpeg", "image/png", "image/gif"}; // 허용 MIME 타입
    }

    /** 인증 캐시 만료 설정 클래스 */
    @Getter
    @Setter
    public static class AuthCache {
        private long ttlMinutes = 60; // 인증 토큰/데이터 캐시 유지 시간 (분)
    }

    /** Rate Limit (요청 제한) 정책 클래스 */
    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = true; // 활성화 여부

        // 엔드포인트별 독립 정책 설정
        private final Policy authenticate = new Policy(10, 5);      // 로그인 시도 (10회/5분)
        private final Policy register = new Policy(5, 60);          // 회원가입 (5회/60분)
        private final Policy resetPasswordInit = new Policy(5, 60); // 비번 초기화 (5회/60분)
        private final Policy otpRequest = new Policy(5, 10);        // OTP 발송 (5회/10분)
        private final Policy otpVerify = new Policy(10, 10);        // OTP 검증 (10회/10분)
        
        private Map<String, Policy> policies = new HashMap<>(); // 기타 커스텀 정책

        @Getter
        @Setter
        public static class Policy {
            private long tokens;            // 허용 토큰 수 (요청 횟수)
            private long durationMinutes;   // 제한 주기 (분)

            public Policy() {
                // Spring Boot 바인딩을 위한 기본 생성자
                this.tokens = 10;
                this.durationMinutes = 1;
            }

            public Policy(long tokens, long durationMinutes) {
                this.tokens = tokens;
                this.durationMinutes = durationMinutes;
            }
        }
    }

    /** 보안 관련 설정 클래스 (공개 경로 등) */
    @Getter
    @Setter
    public static class Security {
        private final PublicPaths publicPaths = new PublicPaths();

        /** 인증 없이 접근 가능한 공개 경로 정의 */
        @Getter
        @Setter
        public static class PublicPaths {
            private String[] staticResources = {"/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"};
            private String[] i18n = {"/i18n/**"};
            private String[] swagger = {"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
            private String[] websocket = {"/websocket/**"};
            private String[] management = {"/management/**"};
        }
    }

    /** 서비스별 캐시 TTL 설정 클래스 */
    @Getter
    @Setter
    public static class Cache {
        private final Ttl ttl = new Ttl();

        @Getter
        @Setter
        public static class Ttl {
            private int defaultSeconds = 3600; // 기본 캐시 만료 시간 (1시간)
            private int longSeconds = 86400;   // 장기 캐시 만료 시간 (1일)
            private int authSeconds = 300;     // 인증 캐시 만료 시간 (5분)
        }
    }

    /** Liquibase 실행 정책 클래스 */
    @Getter
    @Setter
    public static class Liquibase {
        private boolean async = true;      // 비동기 실행 여부
        private boolean asyncStart = true; // 시작 시 비동기 체크 여부

        public boolean getAsyncStart() {
            return asyncStart;
        }
    }

    /** 로깅 설정 클래스 (JSON, Logstash 등) */
    @Getter
    @Setter
    public static class Logging {
        private boolean useJsonFormat = false; // JSON 형식 로그 사용 여부
        private final Logstash logstash = new Logstash();

        @Getter
        @Setter
        public static class Logstash {
            private boolean enabled = false;   // Logstash 전송 활성화
            private String host = "localhost"; // Logstash 호스트
            private int port = 5000;           // Logstash 포트
            private int queueSize = 512;       // 로그 전송 큐 크기
        }
    }

    @Getter
    @Setter
    public static class Database {
        /** 최대 풀 사이즈. 0이면 자동 계산 */
        private int maxPoolSize = 0;
        /** 최소 idle 연결 수. 0 미만이면 maxPoolSize와 동일하게 맞춤 (고정 풀) */
        private int minimumIdle = 2;
        /** 커넥션 타임아웃(ms) */
        private long connectionTimeout = 30000L;
        /** idle 타임아웃(ms) */
        private long idleTimeout = 600000L;
        /** max lifetime(ms) */
        private long maxLifetime = 1800000L;
        /** keepalive time(ms) */
        private long keepaliveTime = 120000L;
    }
}

