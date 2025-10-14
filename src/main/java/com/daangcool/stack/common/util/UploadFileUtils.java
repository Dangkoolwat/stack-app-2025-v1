package com.daangcool.stack.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 업로드, 삭제, 경로 관리 등의 공통 유틸리티.
 *
 * 주요 기능
 *  - 업로드 시 public/private 분기 저장
 *  - 안전한 파일명 생성 (timestamp + UUID)
 *  - 디렉토리 트래버설 검증
 *  - 물리 파일 삭제 및 폴더 간 이동 지원
 *
 * 주의
 *  - rootPath는 애플리케이션에서 반드시 유효한 절대 경로로 전달되어야 합니다.
 *  - 반환되는 경로는 웹 접근용 경로(예: /uploads/public/...) 형식입니다.
 */
public class UploadFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtils.class);

    // 디렉토리 분할 포맷 (운영체제 독립적으로 처리)
    private static final DateTimeFormatter FOLDER_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy" + File.separator + "MM");

    // 파일명에 사용할 타임스탬프 포맷
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private UploadFileUtils() {
        // 유틸 클래스이므로 인스턴스화 금지
    }

    /**
     * 파일을 저장하고 DB에 기록할 웹 접근 경로를 반환합니다.
     *
     * @param rootPath   애플리케이션 기준 루트 절대 경로 (예: /home/app/uploads)
     * @param storageKey 파일 용도 구분 키 (예: NOTICE, USER_PROFILE)
     * @param file       업로드된 MultipartFile
     * @param isPublic   공개 여부. true이면 /uploads/public, false이면 /uploads/private 하위에 저장
     * @return 웹 접근용 상대 경로 (예: /uploads/public/NOTICE/2025/10/20251009_uuid.ext)
     * @throws IOException 입력 검증 실패 또는 파일 쓰기 실패 시 발생
     */
    public static String fileSave(String rootPath, String storageKey, MultipartFile file, boolean isPublic) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("업로드할 파일이 비어 있습니다.");
        }

        if (!StringUtils.hasText(rootPath)) {
            throw new IllegalArgumentException("rootPath must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IOException("원본 파일명이 비어 있습니다.");
        }

        String cleanFilename = sanitizeFilename(originalFilename);

        // 저장 기본 폴더 결정 (rootPath 바로 하위)
        String scopeDir = isPublic ? "public" : "private";

        // 날짜 기반 서브 폴더 생성 (yyyy/MM)
        String dateFolder = LocalDateTime.now().format(FOLDER_DATE_FORMATTER);

        // 최종 물리 디렉토리: rootPath + scopeDir + storageKey + dateFolder
        Path targetDir = Paths.get(rootPath, scopeDir, storageKey, dateFolder);
        Files.createDirectories(targetDir);

        // 새 파일명 생성: timestamp_uuid.ext
        String ext = FilenameUtils.getExtension(cleanFilename);
        String timeStamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFilename = timeStamp + "_" + uuid + (StringUtils.hasText(ext) ? "." + ext : "");

        Path targetPath = targetDir.resolve(newFilename);

        // 파일 저장
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            logger.error("Failed to save uploaded file to path: {}", targetPath, e);
            throw new IOException("파일 저장에 실패했습니다.", e);
        }

        // 반환: 웹에서 접근 가능한 경로 형식으로 반환
        // 예: /uploads/public/NOTICE/2025/10/20251009_uuid.ext
        return makeWebPath("/uploads", scopeDir, storageKey, dateFolder, newFilename);
    }

    /**
     * 물리 파일 삭제 (로컬/공유 마운트 전용).
     *
     * @param rootLocation    물리적 루트 경로 (예: /home/app/uploads)
     * @param storageFilePath DB에 저장된 웹 경로 (예: /uploads/public/NOTICE/2025/10/...)
     * @param webPrefix       웹 접근 경로 접두사 (예: /uploads)
     * @return 삭제 성공 여부 (파일이 없으면 true)
     */
    public static boolean deleteFile(String rootLocation, String storageFilePath, String webPrefix) {
        if (!StringUtils.hasText(rootLocation) || !StringUtils.hasText(storageFilePath)) {
            logger.warn("Invalid parameters for deleteFile. rootLocation or storageFilePath is empty.");
            return false;
        }

        // storageFilePath에서 webPrefix를 제거하여 rootLocation에 상대적인 경로를 얻는다.
        // 예: storageFilePath = /uploads/public/..., webPrefix = /uploads -> relativePath = /public/...
        String relativePath = storageFilePath;
        if (StringUtils.hasText(webPrefix) && storageFilePath.startsWith(webPrefix)) {
            relativePath = storageFilePath.substring(webPrefix.length());
        }

        // 상대 경로의 '/'를 OS 구분자로 변환하고 Paths.get으로 안전하게 결합
        // rootLocation: /home/app/uploads
        // relativePath: /public/NOTICE/2025/10/file.ext
        // filePath: /home/app/uploads/public/NOTICE/2025/10/file.ext
        String normalizedRelative = relativePath.replace('/', File.separatorChar);
        Path filePath = Paths.get(rootLocation, trimLeadingSlash(normalizedRelative));

        File fileToDelete = filePath.toFile();
        if (!fileToDelete.exists()) {
            logger.warn("File not found for deletion: {}", filePath);
            return true;
        }

        try {
            FileUtils.forceDelete(fileToDelete);
            logger.info("File deleted: {}", filePath);
            cleanEmptyParentDirectories(filePath.getParent(), Paths.get(rootLocation));
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    /**
     * 저장된 파일을 한 범위(public/private)에서 다른 범위로 이동시킵니다.
     * DB의 filePath는 이 메서드 호출 후 반환값으로 교체되어야 합니다.
     *
     * @param rootPath       애플리케이션 루트 절대 경로 (예: /home/app/uploads)
     * @param currentWebPath 현재 DB에 저장된 웹 경로 (예: /uploads/public/...)
     * @param targetBaseDir  이동 대상 baseDir (예: /uploads/private 또는 /uploads/public)
     * @return 이동 후의 웹 경로 (예: /uploads/private/...)
     * @throws IOException 파일 이동 실패 시
     */
    public static String moveFileBetweenScopes(String rootPath, String currentWebPath, String targetBaseDir) throws IOException {
        if (!StringUtils.hasText(rootPath) || !StringUtils.hasText(currentWebPath) || !StringUtils.hasText(targetBaseDir)) {
            throw new IllegalArgumentException("Invalid parameters for moveFileBetweenScopes");
        }

        // currentWebPath에서 웹 접두사 (/uploads)를 제거한 상대 경로 추출
        // 예: /uploads/public/NOTICE/2025/10/file.ext -> public/NOTICE/2025/10/file.ext
        String relativePathWithoutWebPrefix = currentWebPath.substring("/uploads".length());

        // 현재 물리 경로 계산
        // rootPath: /home/app/uploads
        // relativePathWithoutWebPrefix: /public/NOTICE/2025/10/file.ext
        // currentPath: /home/app/uploads/public/NOTICE/2025/10/file.ext
        Path currentPath = Paths.get(rootPath, trimLeadingSlash(relativePathWithoutWebPrefix));

        if (!Files.exists(currentPath)) {
            throw new IOException("Source file does not exist: " + currentPath);
        }

        // 파일명과 storageKey, dateFolder 추출
        // relativePathWithoutWebPrefix: /public/NOTICE/2025/10/file.ext
        String[] parts = relativePathWithoutWebPrefix.split(File.separator);
        if (parts.length < 4) { // parts[0] is empty string if starts with /, parts[1]=public/private, parts[2]=storageKey, parts[3]=yyyy, parts[4]=MM, parts[5]=filename
             parts = relativePathWithoutWebPrefix.substring(1).split(File.separator);
        }

        String currentScopeDir = parts[0]; // public or private
        String storageKey = parts[1];
        String dateFolder = parts[2] + File.separator + parts[3]; // yyyy/MM
        String filename = parts[4];

        // 대상 스코프 디렉토리 (public 또는 private)
        String targetScopeDir = targetBaseDir.contains("private") ? "private" : "public";

        // 대상 물리 디렉토리: rootPath/targetScopeDir/storageKey/dateFolder
        Path targetDir = Paths.get(rootPath, targetScopeDir, storageKey, dateFolder);
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(filename);

        // 이동 수행 (덮어쓰기)
        try {
            Files.move(currentPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to move file from {} to {}", currentPath, targetPath, e);
            throw new IOException("파일 이동에 실패했습니다.", e);
        }

        // 반환 웹 경로
        // 예: /uploads/private/NOTICE/2025/10/file.ext
        String newWebPath = makeWebPath("/uploads", targetScopeDir, storageKey, dateFolder, filename);
        return newWebPath;
    }

    /**
     * 파일명으로부터 확장자를 반환합니다。
     *
     * @param fileName 파일명 또는 경로
     * @return 확장자 (없으면 빈 문자열)
     */
    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    /* ----------------- 보조 메서드 ----------------- */

    private static String makeWebPath(String webPrefix, String scopeDir, String storageKey, String dateFolder, String saveName) {
        String normalizedDateFolder = dateFolder.replace(File.separatorChar, '/');
        return webPrefix + "/" + scopeDir + "/" + storageKey + "/" + normalizedDateFolder + "/" + saveName;
    }

    private static String trimLeadingSlash(String path) {
        if (path == null) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * 기본적인 파일명 정리 및 검증을 수행합니다.
     * 디렉토리 트래버설 문자(.., /, \)를 제거하고, 공백을 트리밍합니다.
     *
     * @param filename 원본 파일명
     * @return 안전한 파일명
     * @throws IOException 유효하지 않은 파일명인 경우
     */
    private static String sanitizeFilename(String filename) throws IOException {
        if (!StringUtils.hasText(filename)) {
            throw new IOException("Invalid file name");
        }
        String cleaned = StringUtils.cleanPath(filename).trim();
        if (cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            throw new IOException("Invalid path sequence in filename: " + filename);
        }
        return cleaned;
    }

    /**
     * 빈 부모 디렉토리를 재귀적으로 정리합니다.
     * 루트 경로(rootLimit)를 만나면 정리 중단합니다.
     */
    private static void cleanEmptyParentDirectories(Path dir, Path rootLimit) {
        try {
            Path current = dir;
            while (current != null && !current.equals(rootLimit) && Files.exists(current) && Files.isDirectory(current)) {
                if (isDirectoryEmpty(current)) {
                    try {
                        Files.delete(current);
                        current = current.getParent();
                    } catch (IOException e) {
                        logger.debug("Unable to delete empty directory: {}", current, e);
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.debug("Exception during cleaning empty parent directories", e);
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
            return !ds.iterator().hasNext();
        }
    }

    /**
     * 외부 서비스에서도 사용할 수 있는 공개 경로 생성 메서드.
     * 파일명 생성은 별도 로직에서 처리되고,
     * 웹 접근 경로를 조합할 때 사용합니다.
     *
     * @param subFolder 파일 구분 폴더 (예: NOTICE, PROFILE)
     * @param dateFolder 연/월 등 날짜 폴더 (예: 2025/10)
     * @param fileName 실제 저장될 파일명
     * @return /uploads/public/NOTICE/2025/10/filename.ext
     */
    public static String buildWebPath(String subFolder, String dateFolder, String fileName) {
        String cleanedDateFolder = dateFolder.replace(File.separatorChar, '/');
        return "/" + subFolder + "/" + cleanedDateFolder + "/" + fileName;
    }
}
