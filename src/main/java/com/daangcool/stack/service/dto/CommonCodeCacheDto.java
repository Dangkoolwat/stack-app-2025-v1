package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공통 코드 Redis 2차 캐시 전용 DTO
 * ------------------------------------------------------------------
 * CommonCodeGroup / CommonCodeDetail 은 @Cacheable 로 JPA 엔티티를
 * 직접 캐싱 시 아래 문제가 동일하게 발생합니다.
 *  - CommonCodeGroup.details: @OneToMany LazyLoading → 세션 없이 직렬화 실패
 *  - AbstractAuditingEntity 상속 → Hibernate Proxy 감쌈
 *  - @class 타입 불일치 → 역직렬화 실패
 *
 * 해결 원칙: 단순 타입(String, Long, Integer, boolean)만 담은 record DTO 로 분리.
 * ------------------------------------------------------------------
 */
public class CommonCodeCacheDto {

    /**
     * CommonCodeGroup 캐시 DTO
     * details 컬렉션은 포함하지 않음 (별도 COMMON_DETAIL_LIST_BY_GROUP_CACHE 로 관리)
     */
    public record GroupDto(
        String groupCode,
        String groupName,
        Integer displayOrder,
        boolean deleted,
        String description
    ) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public static GroupDto from(CommonCodeGroup g) {
            return new GroupDto(
                g.getGroupCode(),
                g.getGroupName(),
                g.getDisplayOrder(),
                g.isDeleted(),
                g.getDescription()
            );
        }

        public static List<GroupDto> fromList(List<CommonCodeGroup> list) {
            return list.stream().map(GroupDto::from).collect(Collectors.toList());
        }
    }

    public record GroupRefDto(
        String groupCode
    ) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public static GroupRefDto from(CommonCodeGroup group) {
            if (group == null) {
                return null;
            }
            return new GroupRefDto(group.getGroupCode());
        }
    }

    /**
     * CommonCodeDetail 캐시 DTO
     * group 연관 관계는 식별 정보만 보관 → LazyLoad 원천 차단
     */
    public record DetailDto(
        Long id,
        String code,
        String name,
        Integer sortOrder,
        boolean deleted,
        GroupRefDto group,
        String attribute1,
        String attribute2,
        String attribute3,
        String attribute4,
        String attribute5,
        String description
    ) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public static DetailDto from(CommonCodeDetail d) {
            return new DetailDto(
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getSortOrder(),
                d.isDeleted(),
                GroupRefDto.from(d.getGroup()),
                d.getAttribute1(),
                d.getAttribute2(),
                d.getAttribute3(),
                d.getAttribute4(),
                d.getAttribute5(),
                d.getDescription()
            );
        }

        public static List<DetailDto> fromList(List<CommonCodeDetail> list) {
            return list.stream().map(DetailDto::from).collect(Collectors.toList());
        }
    }
}
