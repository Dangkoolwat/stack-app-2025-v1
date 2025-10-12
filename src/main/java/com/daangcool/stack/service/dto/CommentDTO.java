package com.daangcool.stack.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.board.Comment} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {

    private Long id;

    @NotNull
    private String content;
    private Long star;
    private Long replyCount;
    private Long boardId;
    private Long userId;
    private boolean deleted;
}
