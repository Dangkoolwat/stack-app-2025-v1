package com.daangcool.stack.domain.common;

import com.daangcool.stack.domain.AbstractAuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 공통 상세 코드 (Sub Category)
 * 예: NOTICE, FREE (for BOARD_TYPE)
 */
@Entity
@Table(
    name = "stack_common_code_detail",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_code", "code"})
)
@Getter
@Setter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CommonCodeDetail extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",
        sequenceName = "common_detail_sequence_generator",
        allocationSize = 1
    )
    private Long id; // 내부 식별자 ID

    @NotNull
    @Size(max = 50)
    @Column(name = "code", length = 50, nullable = false)
    private String code; // 실제 사용될 상세 코드 (예: 'NOTICE', 'DIGITAL')

    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name; // 상세 코드 명칭 (예: '공지사항', '디지털')

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // 정렬 순서

    /**
     * 논리적 삭제 여부 (Soft Delete)
     * DB 컬럼명: IS_DELETED (Oracle에서는 0:미삭제, 1:삭제)
     */
    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted = false;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    @Size(max = 255)
    @Column(name = "attribute1", length = 255)
    private String attribute1; // 확장 필드 (예: 게시판 아이콘 경로, 색상 코드)

    @Size(max = 255)
    @Column(name = "attribute2", length = 255)
    private String attribute2; // 확장 필드 (예: 게시판 아이콘 경로, 색상 코드)

    @Size(max = 255)
    @Column(name = "attribute3", length = 255)
    private String attribute3; // 확장 필드 (예: 게시판 아이콘 경로, 색상 코드)

    @Size(max = 255)
    @Column(name = "attribute4", length = 255)
    private String attribute4; // 확장 필드 (예: 게시판 아이콘 경로, 색상 코드)

    @Size(max = 255)
    @Column(name = "attribute5", length = 255)
    private String attribute5; // 확장 필드 (예: 게시판 아이콘 경로, 색상 코드)

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    // CommonCodeGroup 과의 연관 관계 - N:1 (필수)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code", nullable = false) // CommonCodeGroup의 groupCode 컬럼과 연결
    private CommonCodeGroup group;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonCodeDetail that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}


