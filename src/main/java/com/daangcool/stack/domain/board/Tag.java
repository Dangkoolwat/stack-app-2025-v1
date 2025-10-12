package com.daangcool.stack.domain.board;

import com.daangcool.stack.domain.AbstractAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 태그 마스터 엔티티 (Tag Master)
 */
@Entity
@Table(name = "stack_tag")
@SQLRestriction("is_deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Tag extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "tag_sequence_generator", allocationSize = 1)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name; // 태그 이름 (고유값)

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L; // 사용 횟수 (Redis 동기화 시 활용)

    /** 논리적 삭제 여부 (Soft Delete) */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // 태그를 참조하는 모든 관계 (BoardTag)
    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "tag", "board" })
    private Set<BoardTag> boardTags = new HashSet<>();


    // --- JPA equals/hashCode/toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        return id != null && id.equals(((Tag) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Tag{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", usageCount=" + usageCount +
            ", deleted=" + deleted +
            "}";
    }
}
