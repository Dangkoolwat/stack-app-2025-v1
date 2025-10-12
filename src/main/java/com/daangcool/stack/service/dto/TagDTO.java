package com.daangcool.stack.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.board.Tag} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    private String name;
    private Long usageCount;
    private boolean deleted;

}
