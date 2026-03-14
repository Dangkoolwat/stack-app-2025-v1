package com.daangcool.stack.service.storage;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 파일 저장소(로컬, 공유 폴더, 클라우드 등)에 대한 추상화 인터페이스.
 *  주요 목적:
 *  - 저장소 타입에 상관없이 동일한 호출 구조 유지
 *  - DI 기반으로 환경(application.yml)에 따라 구현체 자동 주입
 *  - 파일 저장/삭제/읽기를 통합 관리
 */
public interface StorageService {

    /**
     * 파일을 저장소에 저장하고, DB에 기록할 웹 접근 경로를 반환합니다.
     *
     * @param file       업로드 파일
     * @param subFolder  구분용 서브 폴더 이름 (예: NOTICE, USER_PROFILE 등)
     * @return           DB에 저장할 웹 경로 (예: /uploads/NOTICE/2025/10/file.ext)
     */
    String store(MultipartFile file, String subFolder);

    /**
     * 파일 저장 (공개/비공개 경로 분리 지원)
     */
    default String store(MultipartFile file, String subFolder, boolean isPublic) {
        // 기본 구현: 공개 업로드 경로만 처리하도록 포워딩
        return store(file, subFolder);
    }

    /**
     * 물리적으로 파일을 삭제합니다. (Hard Delete)
     *
     * @param storageFilePath DB에 저장된 파일의 웹 경로
     */
    void delete(String storageFilePath);

    /**
     * 파일을 InputStream으로 열어 스트리밍 전송에 제공합니다.
     * 호출 측에서 반드시 try-with-resources로 스트림을 닫아야 합니다.
     *
     * @param storageFilePath DB에 저장된 파일의 웹 경로
     * @return 파일 InputStream
     * @throws IOException 파일을 읽을 수 없는 경우
     */
    InputStream loadAsStream(String storageFilePath) throws IOException;

    /**
     * 파일을 바이트 배열로 읽어와 다운로드용 스트림에 제공합니다.
     * 대용량 파일에는 {@link #loadAsStream}을 사용하십시오.
     *
     * @param storageFilePath DB에 저장된 파일의 웹 경로
     * @return 파일의 바이트 배열
     */
    default byte[] loadAsResource(String storageFilePath) {
        try (InputStream is = loadAsStream(storageFilePath)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + storageFilePath, e);
        }
    }
}
