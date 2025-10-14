package com.daangcool.stack.service.board;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.domain.board.Upload;
import com.daangcool.stack.domain.enumeration.FileStorageType;
import com.daangcool.stack.repository.board.UploadRepository;
import com.daangcool.stack.service.storage.StorageService;
import com.daangcool.stack.util.UploadFileUtils;
import com.daangcool.stack.web.exception.UploadNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: UploadService
 * - 외부 의존성(Repository, StorageService 등)은 모두 Mock으로 대체하여 서비스 로직만 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class UploadServiceT {

    @Mock
    private UploadRepository uploadRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private UploadService uploadService;

    private MockMultipartFile multipartFile;
    private Upload upload;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 가짜 MultipartFile을 생성합니다.
        multipartFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // 테스트에 사용할 Upload 엔티티를 생성합니다.
        upload = new Upload();
        upload.setId(1L);
        upload.setFilePath("/uploads/private/test/2025/10/dummy-file.jpg");
        upload.setPublic(false);

        // 캐시 관련 Mock 설정 (NullPointerException 방지)
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    /**
     * 파일 업로드 테스트 (성공 케이스)
     * - StorageService를 통해 파일이 저장되는지 확인합니다.
     * - 파일 메타데이터가 DB에 저장되는지 확인합니다.
     */
    @Test
    void saveUpload_ValidFile_ShouldStoreAndSaveMetadata() {
        // given
        String dummyStoragePath = "/uploads/public/test/2025/10/dummy-file.jpg";
        when(storageService.store(any(MockMultipartFile.class), anyString(), anyBoolean())).thenReturn(dummyStoragePath);
        when(uploadRepository.save(any(Upload.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Upload result = uploadService.saveUpload(multipartFile, "TEST_KEY", true);

        // then
        verify(storageService, times(1)).store(multipartFile, "TEST_KEY", true);
        verify(uploadRepository, times(1)).save(any(Upload.class));
        assertThat(result).isNotNull();
        assertThat(result.getFilePath()).isEqualTo(dummyStoragePath);
        assertThat(result.getSourceFilename()).isEqualTo("test.jpg");
    }

    /**
     * 논리적 삭제(soft delete) 테스트
     * - isDeleted 플래그가 true로 변경되고, DB에 저장되는지 확인합니다.
     */
    @Test
    void softDelete_ValidId_ShouldUpdateFlag() {
        // given
        when(uploadRepository.findById(1L)).thenReturn(Optional.of(upload));

        // when
        uploadService.softDelete(1L, "test reason");

        // then
        verify(uploadRepository, times(1)).save(any(Upload.class));
        assertThat(upload.isDeleted()).isTrue();
        assertThat(upload.getDescription()).isEqualTo("test reason");
    }

    /**
     * 물리적 삭제(hard delete) 테스트
     * - StorageService를 통해 실제 파일이 삭제되는지 확인합니다.
     * - DB에서 메타데이터가 삭제되는지 확인합니다.
     */
    @Test
    void hardDelete_ValidId_ShouldDeleteFileAndMetadata() {
        // given
        when(uploadRepository.findById(1L)).thenReturn(Optional.of(upload));

        // when
        uploadService.hardDelete(1L);

        // then
        verify(storageService, times(1)).delete(upload.getFilePath());
        verify(uploadRepository, times(1)).delete(upload);
    }

    /**
     * 파일 공개/비공개 전환 테스트
     * - 정적(static) 메소드인 UploadFileUtils.moveFileBetweenScopes 호출을 검증합니다.
     * - DB의 isPublic 플래그와 filePath가 갱신되는지 확인합니다.
     */
    @Test
    void changeVisibility_ShouldMoveFileAndUpdateMetadata() {
        // given
        // ApplicationProperties의 Mock 객체가 특정 값을 반환하도록 설정합니다.
        ApplicationProperties.File fileProps = new ApplicationProperties.File();
        fileProps.setStorageType(FileStorageType.LOCAL);
        fileProps.setPublicPath("/uploads/public");
        fileProps.setUploadDir("/uploads"); // 추가: UploadDir 설정
        when(applicationProperties.getFile()).thenReturn(fileProps);

        String newWebPath = "/uploads/public/test/2025/10/dummy-file.jpg";

        // Mockito.mockStatic을 사용하여 정적 메소드를 Mocking합니다.
        try (MockedStatic<UploadFileUtils> mockedUtils = mockStatic(UploadFileUtils.class)) {
            mockedUtils.when(() -> UploadFileUtils.moveFileBetweenScopes(anyString(), anyString(), anyString()))
                .thenReturn(newWebPath);

            when(uploadRepository.findById(1L)).thenReturn(Optional.of(upload));
            when(uploadRepository.save(any(Upload.class))).thenAnswer(i -> i.getArgument(0));

            // 초기 파일 경로를 저장합니다.
            String initialFilePath = upload.getFilePath();

            // when: 비공개 -> 공개로 전환
            Upload result = uploadService.changeVisibility(1L, true);

            // then
            // 1. 정적 메소드가 올바른 인자들로 호출되었는지 검증합니다.
            ArgumentCaptor<String> rootPathCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> currentPathCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> targetPathCaptor = ArgumentCaptor.forClass(String.class);

            mockedUtils.verify(() -> UploadFileUtils.moveFileBetweenScopes(rootPathCaptor.capture(), currentPathCaptor.capture(), targetPathCaptor.capture()));

            assertThat(rootPathCaptor.getValue()).isEqualTo(System.getProperty("user.dir") + fileProps.getUploadDir());
            assertThat(currentPathCaptor.getValue()).isEqualTo(initialFilePath); // 초기 경로와 비교
            assertThat(targetPathCaptor.getValue()).isEqualTo(fileProps.getPublicPath());

            // 2. DB에 저장된 결과가 올바른지 검증합니다.
            verify(uploadRepository, times(1)).save(any(Upload.class));
            assertThat(result.isPublic()).isTrue();
            assertThat(result.getFilePath()).isEqualTo(newWebPath);
        }
    }

     /**
     * 존재하지 않는 파일 삭제 시도 테스트
     * - softDelete, hardDelete 시 UploadNotFoundException이 발생하는지 검증합니다.
     */
    @Test
    void delete_NonExistingId_ShouldThrowException() {
        // given
        when(uploadRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> uploadService.softDelete(99L, "reason"))
            .isInstanceOf(UploadNotFoundException.class);

        assertThatThrownBy(() -> uploadService.hardDelete(99L))
            .isInstanceOf(UploadNotFoundException.class);
    }
}
