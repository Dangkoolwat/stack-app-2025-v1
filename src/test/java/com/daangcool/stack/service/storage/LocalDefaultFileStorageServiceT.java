package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.common.util.UploadFileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: LocalDefaultFileStorageService
 * - 정적 유틸리티 클래스인 UploadFileUtils를 Mocking하여 실제 파일 I/O 없이 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class LocalDefaultFileStorageServiceT {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ApplicationProperties.File fileProperties;

    private LocalDefaultFileStorageService storageService;

    private MockMultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        // ApplicationProperties 및 내부 File 클래스의 Mock 객체가 특정 값을 반환하도록 설정합니다.
        when(applicationProperties.getFile()).thenReturn(fileProperties);
        when(fileProperties.getUploadDir()).thenReturn("/uploads");
        when(fileProperties.getUploadResourceDir()).thenReturn("/uploads");

        // 테스트 대상 서비스를 초기화합니다.
        storageService = new LocalDefaultFileStorageService(applicationProperties);

        // 테스트에 사용할 가짜 파일을 생성합니다.
        multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
    }

    /**
     * 파일 저장 테스트
     * - UploadFileUtils.fileSave가 올바른 인자와 함께 호출되는지 검증합니다.
     * - 반환된 웹 경로가 올바르게 조합되었는지 확인합니다.
     */
    @Test
    void store_ShouldCallFileUtilsAndReturnWebPath() throws IOException {
        String expectedStoragePath = "/public/test/2025/10/dummy.txt";

        // Mockito.mockStatic을 사용하여 정적 메소드를 Mocking합니다.
        try (MockedStatic<UploadFileUtils> mockedUtils = mockStatic(UploadFileUtils.class)) {
            // UploadFileUtils.fileSave가 호출되면, 미리 정의된 경로를 반환하도록 설정합니다.
            mockedUtils.when(() -> UploadFileUtils.fileSave(anyString(), anyString(), any(), anyBoolean()))
                .thenReturn(expectedStoragePath);

            // when
            String resultPath = storageService.store(multipartFile, "test", true);

            // then
            // 1. 정적 메소드가 올바른 인자들로 호출되었는지 검증합니다.
            mockedUtils.verify(() -> UploadFileUtils.fileSave(anyString(), eq("test"), eq(multipartFile), eq(true)));

            // 2. 서비스가 반환한 최종 웹 경로가 올바른지 검증합니다.
            assertThat(resultPath).isEqualTo("/uploads" + expectedStoragePath);
        }
    }

    /**
     * 파일 삭제 테스트
     * - UploadFileUtils.deleteFile이 올바른 인자와 함께 호출되는지 검증합니다.
     */
    @Test
    void delete_ShouldCallFileUtils() {
        String storageFilePath = "/uploads/public/test/file.txt";

        try (MockedStatic<UploadFileUtils> mockedUtils = mockStatic(UploadFileUtils.class)) {
            // when
            storageService.delete(storageFilePath);

            // then
            // 정적 메소드가 올바른 인자들로 호출되었는지 검증합니다.
            // 첫 번째 인자인 rootLocation은 생성자에서 계산되므로 anyString()으로 처리합니다.
            mockedUtils.verify(() -> UploadFileUtils.deleteFile(anyString(), eq(storageFilePath), eq("/uploads")));
        }
    }
}
