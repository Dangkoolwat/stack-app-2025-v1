package com.daangcool.stack.domain.board;


import com.daangcool.stack.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;

import java.io.Serializable;

/**
 * 범용 업로드 파일 엔티티 (Attachment 역할을 수행)
 * - AbstractAuditingEntity를 상속하여 생성/수정/삭제자 추적 가능
 */
@Entity
@Table(name = "stack_upload_file")
@Filter(name = "softDeleteFilter", condition = "is_deleted = 0") // 조회 시 논리적으로 삭제된 파일 자동 제외
@Getter
@Setter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Upload extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "upload_sequence_generator", allocationSize = 1)
    private Long id;

    // 💡 NEW FIELD: 파일의 용도(모듈)를 구분하는 키 (예: BOARD_NOTICE, USER_PROFILE)
    @NotNull
    @Size(max = 50)
    @Column(name = "storage_key", length = 50, nullable = false)
    private String storageKey;

    /**
     * 파일의 공개 여부
     * true  → /uploads/public/ 경로에 저장
     * false → /uploads/private/ 경로에 저장 (Controller를 통해서만 접근 가능)
     */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @NotNull
    @Column(name = "source_filename", nullable = false)
    private String sourceFilename;

    @NotNull
    @Column(name = "storage_filename", nullable = false) // 서버/스토리지 저장 파일명
    private String storageFilename;

    @NotNull
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // String -> Long으로 타입 변경 (바이트 단위)

    @Size(max = 50)
    @Column(name = "file_extension", length = 50)
    private String fileExtension;

    @Column(name = "mime_type") // contents_type -> mime_type으로 표준 용어 변경
    private String mimeType;

    @Column(name = "download_count") // cnt -> download_count로 목적 명확화
    private Long downloadCount = 0L;

    @Column(name = "is_deleted", nullable = false) // is_Deleted -> is_deleted로 정규화
    private boolean deleted = false;

    @JsonIgnore
    @Column(name = "upload_description")
    private String description;

    // --- 연관 관계 추가 (게시판 첨부파일 역할) ---

    // 이 파일이 첨부된 게시글 (선택적 관계 - 공용 파일 테이블이므로 nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @JsonIgnoreProperties(value = { "comments", "attachments", "boardTags" })
    private Board board;


    // --- JPA equals/hashCode/toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Upload)) return false;
        // JPA 영속성 관리를 위해 id를 기준으로 비교
        return id != null && id.equals(((Upload) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Upload{" +
            "id=" + id +
            ", sourceFilename='" + sourceFilename + '\'' +
            ", storageFilename='" + storageFilename + '\'' +
            ", fileSize=" + fileSize +
            ", mimeType='" + mimeType + '\'' +
            ", downloadCount=" + downloadCount +
            ", deleted=" + deleted +
            '}';
    }
}
