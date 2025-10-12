package com.daangcool.stack.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Data Transfer Object for the {@link com.daangcool.stack.domain.board.BoardTag} entity.
 *
 * 게시글(Board)과 태그(Tag)의 연결 정보를 전달하기 위한 DTO 클래스입니다.
 * BoardTag는 단순한 연결 엔티티이므로 최소한의 필드만 노출합니다.
 */
@Getter
@Setter
public class BoardTagDTO implements Serializable {

    private Long id;              // 관계 ID
    private Long boardId;         // 게시글 ID
    private Long tagId;           // 태그 ID
    private String tagName;       // 태그 이름 (조회용)
    private boolean deleted;      // 논리 삭제 여부
    private String description;   // 설명 또는 삭제 사유


    @Override
    public String toString() {
        return "BoardTagDTO{" +
            "id=" + id +
            ", boardId=" + boardId +
            ", tagId=" + tagId +
            ", tagName='" + tagName + '\'' +
            ", deleted=" + deleted +
            ", description='" + description + '\'' +
            '}';
    }
}

