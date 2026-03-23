package com.daangcool.stack.domain.board;

import com.daangcool.stack.domain.AbstractAuditingEntity;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stack_board")
@FilterDef(name = "softDeleteFilter")
@Filter(name = "softDeleteFilter", condition = "is_deleted = 0")
@Getter
@Setter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Board extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "board_sequence_generator", allocationSize = 1)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /** 공지글 여부 */
    @Column(name = "is_notice", nullable = false)
    private boolean notice = false;

    /** 논리적 삭제 여부 (Soft Delete) */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /** 설명 및 삭제 사유 기재 필드 (Auditing과 함께 삭제 사유로 활용) */
    @Column(name = "board_description")
    private String description;


    // 1. 작성자 (Author) - 필수 관계
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    // 2. 게시판 유형 (Category)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "board_type_code", referencedColumnName = "code", nullable = false)
    @JsonIgnoreProperties(value = { "group" }, allowSetters = true)
    private CommonCodeDetail boardType;

    // 3. 댓글 (Comments)
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "board", "user" }, allowSetters = true)
    private Set<Comment> comments = new HashSet<>();

    // 4. 첨부파일 (Attachments)
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "board" }, allowSetters = true)
    private Set<Upload> attachments = new HashSet<>();

    // 5. 태그 (Tags)
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "board", "tag" })
    private Set<BoardTag> boardTags = new HashSet<>();


    // ------------------------------------------
    // --- Helper methods (연관 관계 편의 메소드) ---
    // ------------------------------------------

    public Board addComment(Comment comment) {
        this.comments.add(comment);
        comment.setBoard(this);
        return this;
    }

    public Board removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setBoard(null);
        return this;
    }

    public Board addAttachment(Upload attachment) {
        this.attachments.add(attachment);
        attachment.setBoard(this);
        return this;
    }

    public Board removeAttachment(Upload attachment) {
        this.attachments.remove(attachment);
        attachment.setBoard(null);
        return this;
    }

    public Board addBoardTag(BoardTag boardTag) {
        this.boardTags.add(boardTag);
        boardTag.setBoard(this);
        return this;
    }

    public Board removeBoardTag(BoardTag boardTag) {
        this.boardTags.remove(boardTag);
        boardTag.setBoard(null);
        return this;
    }

    // ------------------------------------------
    // --- JPA equals/hashCode/toString ---
    // ------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Board)) {
            return false;
        }
        // JPA 영속성을 위해 id가 null이 아닌 경우 id로 비교합니다.
        return id != null && id.equals(((Board) o).id);
    }

    @Override
    public int hashCode() {
        // JPA Entity의 hashCode 구현 권장 패턴
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Board{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", viewCount=" + viewCount +
            ", notice=" + notice +
            ", deleted=" + deleted +
            ", createdBy='" + getCreatedBy() + '\'' +
            ", createdDate=" + getCreatedDate() +
            "}";
    }
}
