package com.daangcool.stack.config;

import com.daangcool.stack.domain.enumeration.FileStorageType;
import tech.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 파일 접근 정책에 따른 정적 리소스 핸들러 등록 설정.
 *
 * - /uploads/public/** : Spring Security 인증 없이 직접 서빙 (permitAll)
 * - /uploads/private/** : 정적 매핑 제외 (Controller를 통한 접근만 허용)
 *
 * 캐시 정책은 JHipster 설정의 http.cache.time-to-live-in-days 값을 따릅니다.
 */
@Configuration
public class FileResourceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FileResourceConfiguration.class);

    private final ApplicationProperties fileStorageProperties;
    private final JHipsterProperties jHipsterProperties;

    public FileResourceConfiguration(ApplicationProperties fileStorageProperties, JHipsterProperties jHipsterProperties) {
        this.fileStorageProperties = fileStorageProperties;
        this.jHipsterProperties = jHipsterProperties;
    }

    /**
     * 공개 업로드 파일 경로를 정적으로 매핑합니다.
     * 비공개 업로드 파일은 정적 매핑하지 않습니다.
     */
    @Bean(name = "fileResourceConfigurer")
    public WebMvcConfigurer fileResourceConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // 1. 물리적 루트 경로 결정 (Local vs Share)
                String physicalRoot;
                if (fileStorageProperties.getFile().getStorageType() == FileStorageType.SHARE) {
                    // SHARE 모드: 외부 마운트 경로 사용 (예: /mnt/nfs/shared_storage)
                    physicalRoot = fileStorageProperties.getFile().getSharePath();
                } else {
                    // LOCAL 모드: 현재 실행 디렉토리 사용
                    physicalRoot = System.getProperty("user.dir");
                }

                // 2. 공개 리소스의 실제 물리적 경로 계산
                String publicPath = fileStorageProperties.getFile().getPublicPath(); // ex) /uploads/public
                String resolvedPath = normalizePath(physicalRoot, fileStorageProperties.getFile().getPublicPath());

                // OS별 경로 보정 및 파일 prefix 지정
                String resourceLocation = "file:" + resolvedPath + File.separator;

                // 3. 리소스 핸들러 등록
                registry.addResourceHandler(publicPath + "/**")   // URL 경로: /uploads/public/**
                    .addResourceLocations(resourceLocation)   // 실제 디렉토리: file:/.../uploads/public/
                    .setCacheControl(getCacheControl())       // 캐시 정책 적용
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());

                // 4. 비공개 폴더는 정적 매핑 제외 (보안 목적)
                log.info(":: Public resource mapping enabled at: {}", resourceLocation);
                log.info(":: Private resource path (not mapped): {}", fileStorageProperties.getFile().getPrivatePath());
            }
        };
    }

    /**
     * JHipster 설정에서 Cache TTL을 읽어 CacheControl을 구성합니다.
     */
    private CacheControl getCacheControl() {
        int ttlDays = jHipsterProperties.getHttp().getCache().getTimeToLiveInDays();
        return CacheControl.maxAge(ttlDays, TimeUnit.DAYS)
            .cachePublic()   // 내부망 배포 시에는 .cachePrivate() 으로 전환 가능
            .mustRevalidate();
    }

    /**
     * 운영체제(OS)에 관계없이 안전하게 경로를 결합하는 유틸리티 메서드입니다.
     *
     * 기능 설명:
     * 1. subPath가 "/uploads/public"처럼 슬래시로 시작하면 선행 슬래시를 제거합니다.
     * 2. 슬래시("/")와 역슬래시("\\")를 운영체제의 구분자(File.separator)로 변환합니다.
     * 3. base와 subPath를 결합할 때 중복된 구분자가 생기지 않도록 조정합니다.
     *
     * 사용 예시:
     *   normalizePath("/Users/project", "/uploads/public")
     *     → "/Users/project/uploads/public"
     *
     *   normalizePath("C:\\project", "uploads\\private")
     *     → "C:\\project\\uploads\\private"
     *
     * @param base    기준 경로 (예: 프로젝트 루트 디렉터리)
     * @param subPath 추가할 하위 경로 (예: uploads/public)
     * @return 결합 및 정규화된 전체 경로 문자열
     */
    private String normalizePath(String base, String subPath) {
        if (subPath == null || subPath.isBlank()) return base;
        String normalized = subPath.replaceFirst("^/+", ""); // 앞의 슬래시 제거
        return base + File.separator + normalized.replace("/", File.separator);
    }
}

