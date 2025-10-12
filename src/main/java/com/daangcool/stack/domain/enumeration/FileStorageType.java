package com.daangcool.stack.domain.enumeration;

/**
 * 파일 저장소 유형을 정의하는 Enum.
 * application.yml의 application.file.storage-type에 바인딩됩니다.
 */
public enum FileStorageType {
    LOCAL,      // 애플리케이션 실행 경로(user.dir) 기준으로 저장
    SHARE,      // 외부에서 지정된 공유 경로(sharePath)에 저장 (NFS, SMB 마운트 등)
    CLOUD_S3,   // AWS S3와 같은 클라우드 오브젝트 스토리지
    CLOUD_OCI   // Oracle Cloud Infrastructure Object Storage
}
