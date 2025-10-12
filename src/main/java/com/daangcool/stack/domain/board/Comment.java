package com.daangcool.stack.domain.board;

import com.daangcool.stack.domain.AbstractAuditingEntity;
import com.daangcool.stack.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

@Entity
@Table(name = "stack_comment")
@SQLRestriction("is_deleted = 0")
@Getter
@Setter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // Board 엔티티의 캐시 정책과 통일
public class Comment extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "comment_sequence_generator", allocationSize = 1)
    private Long id;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "star_count") // Likes 또는 Rating Count
    private Long star = 0L;

    @Column(name = "reply_count") // 대댓글 수로 이름 변경 (Denormalization)
    private Long replyCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @JsonIgnore
    @Column(name = "comment_description")
    private String description;

    // --- 연관 관계 매핑 (Board 엔티티에서 정의된 내용에 맞춤) ---

    // 1. 댓글이 속한 게시글 (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnoreProperties(value = { "user", "boardType", "comments", "attachments", "boardTags" }, allowSetters = true)
    private Board board;

    // 2. 댓글 작성자 (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    // --- JPA equals/hashCode/toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        return id != null && id.equals(((Comment) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Comment{" +
            "id=" + id +
            ", star=" + star +
            ", replyCount=" + replyCount +
            ", deleted=" + deleted +
            ", content='" + content.substring(0, Math.min(content.length(), 50)) + "...'" +
            "}";
    }
}
