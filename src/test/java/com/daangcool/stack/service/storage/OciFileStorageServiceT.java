package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.common.util.UploadFileUtils;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: OciFileStorageService
 * - OCI SDK 관련 클래스의 생성을 가로채고(intercept) Mock 객체로 대체하여 실제 클라우드 API 호출 없이 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class OciFileStorageServiceT {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ObjectStorageClient ociClient;

    private OciFileStorageService storageService;

    private MockMultipartFile multipartFile;

    private MockedStatic<ObjectStorageClient> ociClientMockedStatic;
    private MockedStatic<UploadFileUtils> uploadFileUtilsMockedStatic;
    private MockedConstruction<ConfigFileAuthenticationDetailsProvider> providerConstructionMock;

    @BeforeEach
    void setUp() throws IOException {
        // 시스템 속성 설정 (생성자에서 사용)
        System.setProperty("oci.namespace", "test-namespace");
        System.setProperty("oci.bucket", "test-bucket");

        // ApplicationProperties Mock 설정
        ApplicationProperties.File fileProps = mock(ApplicationProperties.File.class);
        lenient().when(applicationProperties.getFile()).thenReturn(fileProps);
        lenient().when(fileProps.getSharePath()).thenReturn("test-bucket");

        // OCI SDK 클래스 Mocking
        providerConstructionMock = mockConstruction(ConfigFileAuthenticationDetailsProvider.class);

        ObjectStorageClient.Builder ociBuilder = mock(ObjectStorageClient.Builder.class);
        ociClientMockedStatic = mockStatic(ObjectStorageClient.class, Mockito.withSettings().lenient());
        ociClientMockedStatic.when(ObjectStorageClient::builder).thenReturn(ociBuilder);
        when(ociBuilder.region(any(Region.class))).thenReturn(ociBuilder);
        when(ociBuilder.build(any(ConfigFileAuthenticationDetailsProvider.class))).thenReturn(ociClient);

        uploadFileUtilsMockedStatic = mockStatic(UploadFileUtils.class);

        // 테스트 대상 서비스를 초기화합니다.
        storageService = new OciFileStorageService(applicationProperties);

        // 테스트에 사용할 가짜 파일을 생성합니다.
        multipartFile = new MockMultipartFile("file", "oci-test.png", "image/png", "content".getBytes());
    }

    @AfterEach
    void tearDown() {
        // 모든 Mock 객체를 해제합니다.
        providerConstructionMock.close();
        ociClientMockedStatic.close();
        uploadFileUtilsMockedStatic.close();
        System.clearProperty("oci.namespace");
        System.clearProperty("oci.bucket");
    }

    /**
     * OCI 파일 업로드 테스트
     */
    @Test
    void store_ShouldCallOciPutObject() throws IOException {
        // given
        String expectedPath = "/OCI_TEST/2025/10/dummy.png";
        uploadFileUtilsMockedStatic.when(() -> UploadFileUtils.buildWebPath(anyString(), anyString(), anyString()))
            .thenReturn(expectedPath);

        when(ociClient.putObject(any(PutObjectRequest.class))).thenReturn(PutObjectResponse.builder().eTag("test-etag").build());

        ArgumentCaptor<PutObjectRequest> putRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // when
        String resultUrl = storageService.store(multipartFile, "OCI_TEST");

        // then
        verify(ociClient, times(1)).putObject(putRequestCaptor.capture());
        PutObjectRequest capturedRequest = putRequestCaptor.getValue();

        assertThat(capturedRequest.getNamespaceName()).isEqualTo("test-namespace");
        assertThat(capturedRequest.getBucketName()).isEqualTo("test-bucket");
        assertThat(capturedRequest.getObjectName()).isEqualTo(expectedPath.substring(1));
        assertThat(capturedRequest.getContentType()).isEqualTo("image/png");

        assertThat(resultUrl).contains("test-namespace", "test-bucket", expectedPath.substring(1));
    }

    /**
     * OCI 파일 삭제 테스트
     */
    @Test
    void delete_ShouldCallOciDeleteObject() {
        // given
        String objectKey = "OCI_TEST/2025/10/dummy.png";
        String fileUrl = String.format("https://objectstorage.ap-seoul-1.oraclecloud.com/n/test-namespace/b/test-bucket/o/%s", objectKey);
        ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);

        // when
        storageService.delete(fileUrl);

        // then
        verify(ociClient, times(1)).deleteObject(deleteRequestCaptor.capture());
        DeleteObjectRequest capturedRequest = deleteRequestCaptor.getValue();

        assertThat(capturedRequest.getNamespaceName()).isEqualTo("test-namespace");
        assertThat(capturedRequest.getBucketName()).isEqualTo("test-bucket");
        assertThat(capturedRequest.getObjectName()).isEqualTo(objectKey);
    }

    /**
     * OCI 파일 로드 테스트
     */
    @Test
    void loadAsResource_ShouldCallOciGetObject() throws IOException {
        // given
        String objectKey = "OCI_TEST/2025/10/dummy.png";
        String fileUrl = String.format("https://objectstorage.ap-seoul-1.oraclecloud.com/n/test-namespace/b/test-bucket/o/%s", objectKey);
        byte[] fileContent = "OCI content".getBytes();

        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);
        when(getObjectResponse.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        when(ociClient.getObject(any(GetObjectRequest.class))).thenReturn(getObjectResponse);

        ArgumentCaptor<GetObjectRequest> getRequestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);

        // when
        byte[] result = storageService.loadAsResource(fileUrl);

        // then
        verify(ociClient, times(1)).getObject(getRequestCaptor.capture());
        GetObjectRequest capturedRequest = getRequestCaptor.getValue();

        assertThat(capturedRequest.getNamespaceName()).isEqualTo("test-namespace");
        assertThat(capturedRequest.getBucketName()).isEqualTo("test-bucket");
        assertThat(capturedRequest.getObjectName()).isEqualTo(objectKey);
        assertThat(result).isEqualTo(fileContent);
    }
}
