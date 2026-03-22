package com.daangcool.stack.service.storage;

import com.daangcool.stack.config.ApplicationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class S3FileStorageServiceTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private S3Client s3Client;

    private StorageService storageService;

    private MockMultipartFile multipartFile;

    private MockedStatic<S3Client> s3ClientMockedStatic;
    private MockedStatic<StaticCredentialsProvider> staticCredentialsProviderMockedStatic;
    private MockedStatic<AwsBasicCredentials> awsBasicCredentialsMockedStatic;

    @BeforeEach
    void setUp() {
        // AwsBasicCredentials.create() 호출을 모의합니다.
        awsBasicCredentialsMockedStatic = mockStatic(AwsBasicCredentials.class);
        AwsBasicCredentials mockAwsBasicCredentials = mock(AwsBasicCredentials.class);
        awsBasicCredentialsMockedStatic.when(() -> AwsBasicCredentials.create(anyString(), anyString()))
                                       .thenReturn(mockAwsBasicCredentials);

        // StaticCredentialsProvider.create() 호출을 모의합니다.
        staticCredentialsProviderMockedStatic = mockStatic(StaticCredentialsProvider.class);
        StaticCredentialsProvider mockCredentialsProvider = mock(StaticCredentialsProvider.class);
        staticCredentialsProviderMockedStatic.when(() -> StaticCredentialsProvider.create(any(AwsBasicCredentials.class)))
                                             .thenReturn(mockCredentialsProvider);

        // ApplicationProperties의 Mock 객체가 특정 값을 반환하도록 설정합니다.
        ApplicationProperties.File fileProps = new ApplicationProperties.File();
        fileProps.setSharePath("test-bucket"); // S3에서는 sharePath를 bucket으로 사용합니다.
        when(applicationProperties.getFile()).thenReturn(fileProps);

        // S3Client.builder()가 호출될 때 Mock Builder를 반환하도록 설정합니다.
        S3ClientBuilder s3ClientBuilder = mock(S3ClientBuilder.class);
        s3ClientMockedStatic = mockStatic(S3Client.class);
        s3ClientMockedStatic.when(S3Client::builder).thenReturn(s3ClientBuilder);

        // Builder 체인의 나머지 부분도 Mocking합니다.
        when(s3ClientBuilder.region(any(Region.class))).thenReturn(s3ClientBuilder);
        when(s3ClientBuilder.credentialsProvider(any())).thenReturn(s3ClientBuilder);
        when(s3ClientBuilder.build()).thenReturn(s3Client);

        // 테스트 대상 서비스를 초기화합니다. 생성자 내부에서 S3Client.builder()가 호출됩니다.
        storageService = new S3FileStorageService(applicationProperties);

        // 테스트에 사용할 가짜 파일을 생성합니다。
        multipartFile = new MockMultipartFile("file", "s3-test.jpg", "image/jpeg", "content".getBytes());
    }

    @AfterEach
    void tearDown() {
        // 모든 정적 Mock을 해제합니다.
        s3ClientMockedStatic.close();
        staticCredentialsProviderMockedStatic.close();
        awsBasicCredentialsMockedStatic.close();
    }

    /**
     * S3 파일 업로드 테스트
     */
    @Test
    void store_ShouldCallS3PutObjectWithCorrectParameters() {
        // given
        ArgumentCaptor<PutObjectRequest> putRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // when
        String resultUrl = storageService.store(multipartFile, "S3_TEST");

        // then
        verify(s3Client, times(1)).putObject(putRequestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest capturedRequest = putRequestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.key()).startsWith("S3_TEST/").endsWith(".jpg");
        assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
        assertThat(resultUrl).startsWith("https://test-bucket.s3.amazonaws.com/");
    }

    /**
     * S3 파일 삭제 테스트
     */
    @Test
    void delete_ShouldCallS3DeleteObjectWithCorrectKey() {
        // given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/S3_TEST/2025/10/dummy.jpg";
        ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);

        // when
        storageService.delete(fileUrl);

        // then
        verify(s3Client, times(1)).deleteObject(deleteRequestCaptor.capture());
        DeleteObjectRequest capturedRequest = deleteRequestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.key()).isEqualTo("S3_TEST/2025/10/dummy.jpg");
    }

    /**
     * S3 파일 로드 테스트 (loadAsStream 방식)
     */
    @Test
    void loadAsResource_ShouldCallS3GetObjectAndReturnBytes() throws IOException {
        // given
        String objectKey = "S3_TEST/2025/10/dummy.jpg";
        String fileUrl = String.format("https://test-bucket.s3.amazonaws.com/%s", objectKey);
        byte[] fileContent = "S3 content".getBytes();

        // getObject 호출 시 ResponseInputStream 반환을 모의합니다.
        ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);
        when(mockResponseStream.readAllBytes()).thenReturn(fileContent);
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenReturn(mockResponseStream);

        // when — interface default loadAsResource → loadAsStream 호출
        byte[] result = storageService.loadAsResource(fileUrl);

        // then
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
        assertThat(result).isEqualTo(fileContent);
    }
}
