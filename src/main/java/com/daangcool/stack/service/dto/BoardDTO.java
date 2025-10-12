package com.daangcool.stack.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.board.Board} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String title;

    private String content;

    private Long viewCount;

    private Boolean notice;

    private Boolean deleted;

    private Long userId;

    private String boardTypeCode;

}
