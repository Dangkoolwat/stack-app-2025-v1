package com.daangcool.stack.domain.board;

import com.daangcool.stack.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Cache;
import java.io.Serializable;

/**
 * 게시글-태그 연결 엔티티 (Junction Table)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "stack_board_tag",
    uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "tag_id"})
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Filter(name = "softDeleteFilter", condition = "is_deleted = 0") // 조회 시 논리적으로 삭제된 관계 자동 제외
public class BoardTag extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "board_tag_sequence_generator", allocationSize = 1)
    private Long id;

    /** 논리적 삭제 여부 (Soft Delete) */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // Board (N:1) - 필수
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnoreProperties(value = { "boardTags" })
    private Board board;

    // Tag (N:1) - 필수
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "tag_id", nullable = false)
    @JsonIgnoreProperties(value = { "boardTags" })
    private Tag tag;

    @Column(name = "description") // 명시적으로 컬럼명 지정
    private String description;

    // --- JPA equals/hashCode/toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardTag)) return false;
        return id != null && id.equals(((BoardTag) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BoardTag{" +
            "id=" + id +
            ", boardId=" + (board != null ? board.getId() : "null") +
            ", tagName=" + (tag != null ? tag.getName() : "null") +
            ", deleted=" + deleted +
            "}";
    }
}
