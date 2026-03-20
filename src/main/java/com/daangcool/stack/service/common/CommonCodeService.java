package com.daangcool.stack.service.common;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.service.dto.CommonCodeCacheDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CommonCodeService {

    private final Logger LOG = LoggerFactory.getLogger(CommonCodeService.class);

    private final CommonCodeGroupRepository groupRepository;
    private final CommonCodeDetailRepository detailRepository;
    private final CacheManager cacheManager; // CacheManager 주입

    private static final String ENTITY_GROUP = "commonCodeGroup";
    private static final String ENTITY_DETAIL = "commonCodeDetail";

    private static final String ERR_CODE_EXISTS = "codeexists";
    private static final String ERR_NOT_FOUND = "notfound";

    // --- Custom Cache Names ---
    // Redis Cache Configuration (CacheConfiguration.java)에 이 이름을 추가해야 합니다.
    public static final String COMMON_GROUP_CACHE = "commonGroups";
    public static final String COMMON_GROUP_LIST_CACHE = "commonGroupList";
    public static final String COMMON_DETAIL_CACHE = "commonDetails";
    public static final String COMMON_DETAIL_LIST_BY_GROUP_CACHE = "commonDetailsByGroup";

    public CommonCodeService(
        CommonCodeGroupRepository groupRepository,
        CommonCodeDetailRepository detailRepository,
        CacheManager cacheManager // 생성자를 통해 CacheManager 주입
    ) {
        this.groupRepository = groupRepository;
        this.detailRepository = detailRepository;
        this.cacheManager = cacheManager;
    }

    // --- Cache Clearing Methods (UserService 패턴) ---

    private void clearGroupCaches(String groupCode) {
        // 단일 그룹 조회 캐시 제거
        Objects.requireNonNull(cacheManager.getCache(COMMON_GROUP_CACHE)).evictIfPresent(groupCode);
        // 전체 그룹 리스트 캐시 제거 (새 데이터 생성/수정/삭제 시 리스트 전체 갱신)
        Objects.requireNonNull(cacheManager.getCache(COMMON_GROUP_LIST_CACHE)).clear();
        LOG.debug("Cleared caches for CommonCodeGroup: {}", groupCode);
    }

    private void clearDetailCaches(Long detailId, String groupCode) {
        // 단일 상세 코드 조회 캐시 제거
        Objects.requireNonNull(cacheManager.getCache(COMMON_DETAIL_CACHE)).evictIfPresent(detailId);
        // 특정 그룹의 상세 코드 리스트 캐시 제거 (GroupCode를 키로 사용)
        Objects.requireNonNull(cacheManager.getCache(COMMON_DETAIL_LIST_BY_GROUP_CACHE)).evictIfPresent(groupCode);
        LOG.debug("Cleared caches for CommonCodeDetail ID: {}, Group: {}", detailId, groupCode);
    }

    // --- CommonCodeGroup Operations ---

    public CommonCodeGroup createGroup(CommonCodeGroup group) {
        if (group.getGroupCode() == null || group.getGroupCode().isBlank()) {
            throw new BadRequestAlertException("Group code must be provided", ENTITY_GROUP, "codenull");
        }
        if (groupRepository.existsByGroupCode(group.getGroupCode())) {
            throw new BadRequestAlertException("A CommonCodeGroup with this code already exists", ENTITY_GROUP, ERR_CODE_EXISTS);
        }
        group.setDeleted(false);
        LOG.debug("Creating CommonCodeGroup: {}", group);
        CommonCodeGroup savedGroup = groupRepository.save(group);
        clearGroupCaches(savedGroup.getGroupCode()); // 캐시 제거
        return savedGroup;
    }

    public Optional<CommonCodeGroup> updateGroup(CommonCodeGroup updatedGroup) {
        if (updatedGroup.getGroupCode() == null) {
            throw new BadRequestAlertException("Group code must not be null for update", ENTITY_GROUP, "codenull");
        }

        return groupRepository
            .findOneByGroupCodeAndDeletedIsFalse(updatedGroup.getGroupCode())
            .map(existingGroup -> {
                existingGroup.setGroupName(updatedGroup.getGroupName());
                existingGroup.setDescription(updatedGroup.getDescription());
                existingGroup.setDisplayOrder(updatedGroup.getDisplayOrder());

                LOG.debug("Updating CommonCodeGroup: {}", existingGroup);
                CommonCodeGroup savedGroup = groupRepository.save(existingGroup);
                clearGroupCaches(savedGroup.getGroupCode()); // 캐시 제거
                return savedGroup;
            });
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CommonCodeGroup> findAllGroups() {
        Cache cache = cacheManager.getCache(COMMON_GROUP_LIST_CACHE);
        if (cache != null) {
            // DTO 리스트로 저장 → Hibernate Proxy / LazyLoad 문제 없음
            List<CommonCodeCacheDto.GroupDto> cached =
                (List<CommonCodeCacheDto.GroupDto>) cache.get("all", List.class);
            if (cached != null) {
                LOG.debug("[COMMON CACHE] Hit findAllGroups");
                // DTO → 엔티티 형태로 복원 (호출부 인터페이스 유지)
                return cached.stream().map(this::toGroupEntity).toList();
            }
        }
        List<CommonCodeGroup> groups =
            groupRepository.findAllByDeletedIsFalseOrderByDisplayOrderAsc();
        if (cache != null && !groups.isEmpty()) {
            // 엔티티 대신 DTO 저장 (LazyLoad 세션 안에서 변환)
            cache.put("all", CommonCodeCacheDto.GroupDto.fromList(groups));
        }
        return groups;
    }

    @Transactional(readOnly = true)
    public Optional<CommonCodeGroup> findGroup(String groupCode) {
        Cache cache = cacheManager.getCache(COMMON_GROUP_CACHE);
        if (cache != null) {
            CommonCodeCacheDto.GroupDto cached =
                cache.get(groupCode, CommonCodeCacheDto.GroupDto.class);
            if (cached != null) {
                LOG.debug("[COMMON CACHE] Hit findGroup: {}", groupCode);
                return Optional.of(toGroupEntity(cached));
            }
        }
        Optional<CommonCodeGroup> result =
            groupRepository.findOneByGroupCodeAndDeletedIsFalse(groupCode);
        result.ifPresent(g -> {
            if (cache != null) cache.put(groupCode, CommonCodeCacheDto.GroupDto.from(g));
        });
        return result;
    }

    public void softDeleteGroup(String groupCode) {
        groupRepository
            .findOneByGroupCodeAndDeletedIsFalse(groupCode)
            .ifPresentOrElse(
                group -> {
                    group.setDeleted(true);
                    LOG.debug("Soft deleting CommonCodeGroup: {}", groupCode);
                    clearGroupCaches(groupCode); // 캐시 제거
                },
                () -> {
                    throw new BadRequestAlertException("CommonCodeGroup not found or already deleted", ENTITY_GROUP, ERR_NOT_FOUND);
                }
            );
    }

    // --- CommonCodeDetail Operations ---

    public CommonCodeDetail createDetail(CommonCodeDetail detail) {
        if (detail.getGroup() == null || detail.getGroup().getGroupCode() == null) {
            throw new BadRequestAlertException("Group code is required", ENTITY_DETAIL, "groupcodenull");
        }

        CommonCodeGroup group = groupRepository
            .findOneByGroupCodeAndDeletedIsFalse(detail.getGroup().getGroupCode())
            .orElseThrow(() -> new BadRequestAlertException("CommonCodeGroup not found", ENTITY_GROUP, ERR_NOT_FOUND));

        if (detailRepository.existsByGroupGroupCodeAndCode(group.getGroupCode(), detail.getCode())) {
            throw new BadRequestAlertException("A CommonCodeDetail with this code already exists in the group", ENTITY_DETAIL, ERR_CODE_EXISTS);
        }

        detail.setGroup(group);
        detail.setDeleted(false);
        LOG.debug("Creating CommonCodeDetail: {}", detail);
        CommonCodeDetail savedDetail = detailRepository.save(detail);
        clearDetailCaches(savedDetail.getId(), savedDetail.getGroup().getGroupCode()); // 캐시 제거
        return savedDetail;
    }

    public Optional<CommonCodeDetail> updateDetail(CommonCodeDetail updatedDetail) {
        if (updatedDetail.getId() == null) {
            throw new BadRequestAlertException("Detail ID must not be null for update", ENTITY_DETAIL, "idnull");
        }

        return detailRepository
            .findById(updatedDetail.getId())
            .filter(detail -> !detail.isDeleted())
            .map(existingDetail -> {
                if (!existingDetail.getCode().equals(updatedDetail.getCode()) &&
                    detailRepository.existsByGroupGroupCodeAndCode(existingDetail.getGroup().getGroupCode(), updatedDetail.getCode()))
                {
                    throw new BadRequestAlertException("The new code already exists in the group", ENTITY_DETAIL, ERR_CODE_EXISTS);
                }

                // Update logic
                existingDetail.setCode(updatedDetail.getCode());
                existingDetail.setName(updatedDetail.getName());
                existingDetail.setSortOrder(updatedDetail.getSortOrder());
                existingDetail.setAttribute1(updatedDetail.getAttribute1());
                existingDetail.setAttribute2(updatedDetail.getAttribute2());
                existingDetail.setAttribute3(updatedDetail.getAttribute3());
                existingDetail.setAttribute4(updatedDetail.getAttribute4());
                existingDetail.setAttribute5(updatedDetail.getAttribute5());
                existingDetail.setDescription(updatedDetail.getDescription());

                LOG.debug("Updating CommonCodeDetail: {}", existingDetail);
                CommonCodeDetail savedDetail = detailRepository.save(existingDetail);
                clearDetailCaches(savedDetail.getId(), savedDetail.getGroup().getGroupCode()); // 캐시 제거
                return savedDetail;
            });
    }

    @Transactional(readOnly = true)
    public Optional<CommonCodeDetail> findDetail(Long id) {
        Cache cache = cacheManager.getCache(COMMON_DETAIL_CACHE);
        if (cache != null) {
            CommonCodeCacheDto.DetailDto cached =
                cache.get(id, CommonCodeCacheDto.DetailDto.class);
            if (cached != null) {
                LOG.debug("[COMMON CACHE] Hit findDetail: {}", id);
                return Optional.of(toDetailEntity(cached));
            }
        }
        Optional<CommonCodeDetail> result =
            detailRepository.findById(id).filter(d -> !d.isDeleted());
        result.ifPresent(d -> {
            if (cache != null) cache.put(id, CommonCodeCacheDto.DetailDto.from(d));
        });
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CommonCodeDetail> findAllDetailsByGroup(String groupCode) {
        Cache cache = cacheManager.getCache(COMMON_DETAIL_LIST_BY_GROUP_CACHE);
        if (cache != null) {
            List<CommonCodeCacheDto.DetailDto> cached =
                (List<CommonCodeCacheDto.DetailDto>) cache.get(groupCode, List.class);
            if (cached != null) {
                LOG.debug("[COMMON CACHE] Hit findAllDetailsByGroup: {}", groupCode);
                return cached.stream().map(this::toDetailEntity).toList();
            }
        }
        List<CommonCodeDetail> details =
            detailRepository.findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc(groupCode);
        if (cache != null && !details.isEmpty()) {
            cache.put(groupCode, CommonCodeCacheDto.DetailDto.fromList(details));
        }
        return details;
    }

    public void softDeleteDetail(Long id) {
        detailRepository
            .findById(id)
            .filter(detail -> !detail.isDeleted())
            .ifPresentOrElse(
                detail -> {
                    detail.setDeleted(true);
                    LOG.debug("Soft deleting CommonCodeDetail: {}", id);
                    clearDetailCaches(detail.getId(), detail.getGroup().getGroupCode()); // 캐시 제거
                },
                () -> {
                    throw new BadRequestAlertException("CommonCodeDetail not found or already deleted", ENTITY_DETAIL, ERR_NOT_FOUND);
                }
            );
    }

    // ------------------------------------------------------------------
    // 캐시 DTO → 엔티티 복원 헬퍼 (호출부 인터페이스 유지용)
    // 캐시 히트 시 DB 조회 없이 가볍게 엔티티 형태로 반환합니다.
    // 연관 관계(group.details 등)는 null 로 채워지지만, 조회 전용 용도에서는 충분합니다.
    // 연관 엔티티가 필요한 로직에서는 DB 를 직접 조회하세요.
    // ------------------------------------------------------------------

    private CommonCodeGroup toGroupEntity(CommonCodeCacheDto.GroupDto dto) {
        CommonCodeGroup g = new CommonCodeGroup();
        g.setGroupCode(dto.groupCode());
        g.setGroupName(dto.groupName());
        g.setDisplayOrder(dto.displayOrder());
        g.setDeleted(dto.deleted());
        g.setDescription(dto.description());
        return g;
    }

    private CommonCodeDetail toDetailEntity(CommonCodeCacheDto.DetailDto dto) {
        CommonCodeDetail d = new CommonCodeDetail();
        d.setId(dto.id());
        d.setCode(dto.code());
        d.setName(dto.name());
        d.setSortOrder(dto.sortOrder());
        d.setDeleted(dto.deleted());
        d.setAttribute1(dto.attribute1());
        d.setAttribute2(dto.attribute2());
        d.setAttribute3(dto.attribute3());
        d.setAttribute4(dto.attribute4());
        d.setAttribute5(dto.attribute5());
        d.setDescription(dto.description());
        if (dto.groupCode() != null) {
            CommonCodeGroup g = new CommonCodeGroup();
            g.setGroupCode(dto.groupCode());
            d.setGroup(g);
        }
        return d;
    }
}
