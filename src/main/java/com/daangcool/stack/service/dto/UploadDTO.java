package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.board.Upload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Upload Redis 캐시 전용 DTO
 *
 * 설계 원칙:
 *  - Upload JPA 엔티티(extends AbstractAuditingEntity + @ManyToOne Board)를 직접 캐싱 시
 *    Hibernate Proxy @class 불일치 / LazyLoad 세션 소멸 문제가 동일하게 발생.
 *  - 단순 타입 필드만 포함, board 연관 관계 제외.
 *  - @Data(Lombok) → Jackson 3 직렬화 안전 (getter/setter 기반, 기본 생성자 존재).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String storageKey;
    private String sourceFilename;
    private String storageFilename;
    private String filePath;
    private Long fileSize;
    private String fileExtension;
    private String mimeType;
    private Long downloadCount;
    private boolean isPublic;
    private boolean deleted;

    /** Upload 엔티티 → DTO 변환 (영속성 컨텍스트 안에서 호출) */
    public UploadDTO(Upload upload) {
        this.id             = upload.getId();
        this.storageKey     = upload.getStorageKey();
        this.sourceFilename = upload.getSourceFilename();
        this.storageFilename = upload.getStorageFilename();
        this.filePath       = upload.getFilePath();
        this.fileSize       = upload.getFileSize();
        this.fileExtension  = upload.getFileExtension();
        this.mimeType       = upload.getMimeType();
        this.downloadCount  = upload.getDownloadCount();
        this.isPublic       = upload.isPublic();
        this.deleted        = upload.isDeleted();
    }
}
