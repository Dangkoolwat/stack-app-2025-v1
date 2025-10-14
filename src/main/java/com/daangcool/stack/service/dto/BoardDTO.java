package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.board.Board;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link Board} entity.
 */
@Getter
@Setter
public class BoardDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String title;

    @NotNull
    private String content;

    private Long userId;

    private Boolean notice = false;

    private Long viewCount = 0L;

    private Instant createdDate;

    private Instant lastModifiedDate;

    private String createdBy;

    private String lastModifiedBy;

    private Boolean deleted = false;

    @NotNull // Board 엔티티의 boardType 필드가 NotNull이므로 DTO에도 추가
    private String boardTypeCode; // 게시판 유형 코드 (예: NOTICE, FREE)

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BoardDTO)) {
            return false;
        }

        return id != null && id.equals(((BoardDTO) o).id);
    }

    @Override
    public int hashCode() {
        // Hash code for DTOs should be based on their business key
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BoardDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", content='" + getContent() + "'" +
            ", userId=" + getUserId() +
            ", notice='" + getNotice() + "'" +
            ", viewCount=" + getViewCount() +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", deleted='" + getDeleted() + "'" +
            ", boardTypeCode='" + getBoardTypeCode() + "'" +
            "}";
    }
}
