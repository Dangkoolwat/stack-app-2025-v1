package com.daangcool.stack.service.mapper;

import com.daangcool.stack.domain.board.BoardTag;
import com.daangcool.stack.service.dto.BoardTagDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper for the {@link BoardTag} entity and its DTO {@link BoardTagDTO}.
 *
 * BoardTag는 Board와 Tag 간의 연결 관계만을 표현하므로,
 * 필드 매핑은 ID 및 참조 엔티티의 식별자 수준에서 처리합니다.
 */
@Component
public class BoardTagMapper {

    /**
     * Entity → DTO 변환
     */
    public BoardTagDTO toDto(BoardTag entity) {
        if (entity == null) {
            return null;
        }
        BoardTagDTO dto = new BoardTagDTO();
        dto.setId(entity.getId());
        dto.setDeleted(entity.isDeleted());
        dto.setDescription(entity.getDescription());

        if (entity.getBoard() != null) {
            dto.setBoardId(entity.getBoard().getId());
        }

        if (entity.getTag() != null) {
            dto.setTagId(entity.getTag().getId());
            dto.setTagName(entity.getTag().getName());
        }

        return dto;
    }

    /**
     * DTO → Entity 변환
     * (Board와 Tag는 서비스 계층에서 주입해야 하므로 여기서는 ID만 매핑)
     */
    public BoardTag toEntity(BoardTagDTO dto) {
        if (dto == null) {
            return null;
        }
        BoardTag entity = new BoardTag();
        entity.setId(dto.getId());
        entity.setDeleted(dto.isDeleted());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
