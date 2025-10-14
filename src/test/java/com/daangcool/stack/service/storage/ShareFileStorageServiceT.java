package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.util.UploadFileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: ShareFileStorageService
 * - 정적 유틸리티 클래스인 UploadFileUtils를 Mocking하여 실제 파일 I/O 없이 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class ShareFileStorageServiceT {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.File fileProperties;

    private ShareFileStorageService storageService;

    private MockMultipartFile multipartFile;
    private String expectedRootLocation;

    @BeforeEach
    void setUp() {
        // ApplicationProperties 및 내부 File 클래스의 Mock 객체가 특정 값을 반환하도록 설정합니다.
        when(applicationProperties.getFile()).thenReturn(fileProperties);
        when(fileProperties.getSharePath()).thenReturn("/mnt/share");
        when(fileProperties.getUploadDir()).thenReturn("/uploads");
        when(fileProperties.getUploadResourceDir()).thenReturn("/uploads");

        // 예상되는 루트 경로를 계산합니다.
        expectedRootLocation = Paths.get("/mnt/share", "/uploads").toString();

        // 테스트 대상 서비스를 초기화합니다.
        storageService = new ShareFileStorageService(applicationProperties);

        // 테스트에 사용할 가짜 파일을 생성합니다.
        multipartFile = new MockMultipartFile("file", "share.txt", "text/plain", "content".getBytes());
    }

    /**
     * 파일 저장 테스트
     * - UploadFileUtils.fileSave가 공유 폴더 기반의 올바른 루트 경로와 함께 호출되는지 검증합니다.
     */
    @Test
    void store_ShouldUseSharePathAsRoot() throws IOException {
        String expectedStoragePath = "/public/test/2025/10/dummy.txt";

        try (MockedStatic<UploadFileUtils> mockedUtils = mockStatic(UploadFileUtils.class)) {
            mockedUtils.when(() -> UploadFileUtils.fileSave(anyString(), anyString(), any(), anyBoolean()))
                .thenReturn(expectedStoragePath);

            // when
            String resultPath = storageService.store(multipartFile, "test", true);

            // then
            // 1. 정적 메소드가 공유 폴더 기반의 루트 경로로 호출되었는지 검증합니다.
            mockedUtils.verify(() -> UploadFileUtils.fileSave(eq(expectedRootLocation), eq("test"), eq(multipartFile), eq(true)));

            // 2. 서비스가 반환한 최종 웹 경로가 올바른지 검증합니다.
            assertThat(resultPath).isEqualTo("/uploads" + expectedStoragePath);
        }
    }

    /**
     * 파일 삭제 테스트
     * - UploadFileUtils.deleteFile이 공유 폴더 기반의 올바른 루트 경로와 함께 호출되는지 검증합니다.
     */
    @Test
    void delete_ShouldUseSharePathAsRoot() {
        String storageFilePath = "/uploads/public/test/file.txt";

        try (MockedStatic<UploadFileUtils> mockedUtils = mockStatic(UploadFileUtils.class)) {
            // when
            storageService.delete(storageFilePath);

            // then
            // 정적 메소드가 공유 폴더 기반의 루트 경로로 호출되었는지 검증합니다.
            mockedUtils.verify(() -> UploadFileUtils.deleteFile(eq(expectedRootLocation), eq(storageFilePath), eq("/uploads")));
        }
    }
}
