package com.daangcool.stack.service.mapper;

import com.daangcool.stack.domain.board.Comment;
import com.daangcool.stack.service.dto.CommentDTO;
import org.springframework.stereotype.Component;

/**
 * Comment <-> CommentDTO 매퍼
 */
@Component
public class CommentMapper {

    public CommentDTO toDto(Comment entity) {
        if (entity == null) return null;
        CommentDTO dto = new CommentDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setStar(entity.getStar());
        dto.setReplyCount(entity.getReplyCount());
        dto.setDeleted(entity.isDeleted());
        if (entity.getBoard() != null) dto.setBoardId(entity.getBoard().getId());
        if (entity.getUser() != null) dto.setUserId(entity.getUser().getId());
        return dto;
    }

    public Comment toEntity(CommentDTO dto) {
        if (dto == null) return null;
        Comment entity = new Comment();
        entity.setId(dto.getId());
        entity.setContent(dto.getContent());
        entity.setStar(dto.getStar());
        entity.setReplyCount(dto.getReplyCount());
        entity.setDeleted(dto.isDeleted());
        return entity;
    }
}
