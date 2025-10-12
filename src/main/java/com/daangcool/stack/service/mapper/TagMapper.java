package com.daangcool.stack.service.mapper;


import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.service.dto.TagDTO;
import org.springframework.stereotype.Component;

/**
 * Tag <-> TagDTO 매퍼 클래스
 */
@Component
public class TagMapper {

    public TagDTO toDto(Tag entity) {
        if (entity == null) return null;
        TagDTO dto = new TagDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUsageCount(entity.getUsageCount());
        dto.setDeleted(entity.isDeleted());
        return dto;
    }

    public Tag toEntity(TagDTO dto) {
        if (dto == null) return null;
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setUsageCount(dto.getUsageCount() != null ? dto.getUsageCount() : 0L);
        tag.setDeleted(dto.isDeleted());
        return tag;
    }
}
