package com.daangcool.stack.service.mapper;

import com.daangcool.stack.domain.board.Board;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.service.dto.BoardDTO;
import com.daangcool.stack.common.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Board <-> BoardDTO 매퍼
 */
@Component
public class BoardMapper {

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    public BoardDTO toDto(Board entity) {
        if (entity == null) return null;
        BoardDTO dto = new BoardDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setViewCount(entity.getViewCount());
        dto.setNotice(entity.isNotice());
        dto.setDeleted(entity.isDeleted());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        if (entity.getUser() != null) dto.setUserId(entity.getUser().getId());
        if (entity.getBoardType() != null) dto.setBoardTypeCode(entity.getBoardType().getCode());

        if (entity.getAttachments() != null) {
            dto.setUploads(entity.getAttachments().stream()
                .filter(u -> !u.isDeleted())
                .map(com.daangcool.stack.service.dto.UploadDTO::new)
                .collect(java.util.stream.Collectors.toList()));
        }

        if (entity.getBoardTags() != null) {
            dto.setTags(entity.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .collect(java.util.stream.Collectors.toList()));
        }

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

        // boardTypeCode를 CommonCodeDetail 엔티티로 변환하여 설정
        if (dto.getBoardTypeCode() != null) {
            // CommonCodeDetailRepository의 올바른 메서드를 사용하여 조회
            CommonCodeDetail boardType = commonCodeDetailRepository
                .findOneByGroupGroupCodeAndCodeAndDeletedIsFalse("BOARD_TYPE", dto.getBoardTypeCode())
                .orElseThrow(() -> new EntityNotFoundException("BoardType을 찾을 수 없습니다. Code=" + dto.getBoardTypeCode()));
            entity.setBoardType(boardType);
        }
        return entity;
    }
}
