package com.daangcool.stack.service.mapper;

import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.service.dto.BoardDTO;
import org.springframework.stereotype.Component;

/**
 * Board <-> BoardDTO 매퍼
 */
@Component
public class BoardMapper {

    public BoardDTO toDto(Board entity) {
        if (entity == null) return null;
        BoardDTO dto = new BoardDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setViewCount(entity.getViewCount());
        dto.setNotice(entity.isNotice());
        dto.setDeleted(entity.isDeleted());
        if (entity.getUser() != null) dto.setUserId(entity.getUser().getId());
        if (entity.getBoardType() != null) dto.setBoardTypeCode(entity.getBoardType().getCode());
        return dto;
    }

    public Board toEntity(BoardDTO dto) {
        if (dto == null) return null;
        Board entity = new Board();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setViewCount(dto.getViewCount() != null ? dto.getViewCount() : 0L);
        entity.setNotice(dto.getNotice() != null && dto.getNotice());
        entity.setDeleted(dto.getDeleted() != null && dto.getDeleted());
        return entity;
    }
}

