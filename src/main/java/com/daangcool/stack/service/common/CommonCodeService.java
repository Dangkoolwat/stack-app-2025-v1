package com.daangcool.stack.service.common;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.service.dto.CommonCodeCacheDto;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.DetailDto;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.GroupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.daangcool.stack.common.constant.CacheNames.*;

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
        evictIfPresent(COMMON_GROUPS, groupCode);
        clear(COMMON_GROUP_LIST);
        LOG.debug("Cleared caches for CommonCodeGroup: {}", groupCode);
    }

    private void clearDetailCaches(Long detailId, String groupCode) {
        evictIfPresent(COMMON_DETAILS, detailId);
        evictIfPresent(COMMON_DETAILS_BY_GROUP, groupCode);
        LOG.debug("Cleared caches for CommonCodeDetail ID: {}, Group: {}", detailId, groupCode);
    }

    // --- CommonCodeGroup Operations ---

    public CommonCodeGroup createGroup(CommonCodeGroup group) {
        // 그룹 코드 중복 체크 (삭제된 항목 포함)
        groupRepository.findById(group.getGroupCode()).ifPresent(existing -> {
            if (existing.isDeleted()) {
                throw new BadRequestAlertException("This group code was previously used and is deleted. It cannot be reused.", ENTITY_GROUP, "groupdeleted");
            } else {
                throw new BadRequestAlertException("A CommonCodeGroup with this code already exists", ENTITY_GROUP, "groupexists");
            }
        });

        group.setDeleted(false);
        LOG.debug("Creating CommonCodeGroup: {}", group);
        CommonCodeGroup saved = groupRepository.save(group);
        clearGroupCaches(saved.getGroupCode());
        return saved;
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
    public List<GroupDto> findAllGroups() {
        Cache cache = cacheManager.getCache(COMMON_GROUP_LIST);
        try {
            if (cache != null) {
                List<GroupDto> cached = (List<GroupDto>) cache.get("all", List.class);
                if (cached != null) {
                    LOG.debug("[COMMON CACHE] Hit findAllGroups");
                    return cached;
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to read group list cache: {}", e.getMessage());
        }
        List<CommonCodeGroup> groups =
            groupRepository.findAllByDeletedIsFalseOrderByDisplayOrderAsc();
        List<GroupDto> groupDtos = CommonCodeCacheDto.GroupDto.fromList(groups);
        if (cache != null && !groupDtos.isEmpty()) {
            try {
                cache.put("all", groupDtos);
            } catch (RuntimeException e) {
                LOG.warn("[COMMON CACHE] Failed to store group list cache: {}", e.getMessage());
            }
        }
        return groupDtos;
    }

    @Transactional(readOnly = true)
    public Optional<GroupDto> findGroup(String groupCode) {
        Cache cache = cacheManager.getCache(COMMON_GROUPS);
        try {
            if (cache != null) {
                GroupDto cached = cache.get(groupCode, GroupDto.class);
                if (cached != null) {
                    LOG.debug("[COMMON CACHE] Hit findGroup: {}", groupCode);
                    return Optional.of(cached);
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to read group cache ({}): {}", groupCode, e.getMessage());
        }
        Optional<GroupDto> result = groupRepository.findOneByGroupCodeAndDeletedIsFalse(groupCode)
            .map(CommonCodeCacheDto.GroupDto::from);
        result.ifPresent(g -> {
            if (cache != null) {
                try {
                    cache.put(groupCode, g);
                } catch (RuntimeException e) {
                    LOG.warn("[COMMON CACHE] Failed to store group cache ({}): {}", groupCode, e.getMessage());
                }
            }
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

        // 상세 코드 중복 체크 (삭제된 항목 포함)
        detailRepository.findOneByGroupGroupCodeAndCode(group.getGroupCode(), detail.getCode())
            .ifPresent(existing -> {
                if (existing.isDeleted()) {
                    throw new BadRequestAlertException("This code was previously used and is deleted. It cannot be reused.", ENTITY_DETAIL, "codedeleted");
                } else {
                    throw new BadRequestAlertException("A CommonCodeDetail with this code already exists in the group", ENTITY_DETAIL, ERR_CODE_EXISTS);
                }
            });

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
                // 상용 코드(Code) 변경 시 중복 체크
                if (!existingDetail.getCode().equals(updatedDetail.getCode())) {
                    detailRepository.findOneByGroupGroupCodeAndCode(existingDetail.getGroup().getGroupCode(), updatedDetail.getCode())
                        .ifPresent(other -> {
                            if (other.isDeleted()) {
                                throw new BadRequestAlertException("The new code was previously used and is deleted. It cannot be reused.", ENTITY_DETAIL, "codedeleted");
                            } else {
                                throw new BadRequestAlertException("The new code already exists in the group", ENTITY_DETAIL, ERR_CODE_EXISTS);
                            }
                        });
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
    public Optional<DetailDto> findDetail(Long id) {
        Cache cache = cacheManager.getCache(COMMON_DETAILS);
        try {
            if (cache != null) {
                DetailDto cached = cache.get(id, DetailDto.class);
                if (cached != null) {
                    LOG.debug("[COMMON CACHE] Hit findDetail: {}", id);
                    return Optional.of(cached);
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to read detail cache ({}): {}", id, e.getMessage());
        }
        Optional<DetailDto> result =
            detailRepository.findById(id).filter(d -> !d.isDeleted()).map(CommonCodeCacheDto.DetailDto::from);
        result.ifPresent(d -> {
            if (cache != null) {
                try {
                    cache.put(id, d);
                } catch (RuntimeException e) {
                    LOG.warn("[COMMON CACHE] Failed to store detail cache ({}): {}", id, e.getMessage());
                }
            }
        });
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DetailDto> findAllDetailsByGroup(String groupCode) {
        Cache cache = cacheManager.getCache(COMMON_DETAILS_BY_GROUP);
        try {
            if (cache != null) {
                List<DetailDto> cached = (List<DetailDto>) cache.get(groupCode, List.class);
                if (cached != null) {
                    LOG.debug("[COMMON CACHE] Hit findAllDetailsByGroup: {}", groupCode);
                    return cached;
                }
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to read detail list cache ({}): {}", groupCode, e.getMessage());
        }
        List<CommonCodeDetail> details =
            detailRepository.findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc(groupCode);
        List<DetailDto> detailDtos = CommonCodeCacheDto.DetailDto.fromList(details);
        if (cache != null && !detailDtos.isEmpty()) {
            try {
                cache.put(groupCode, detailDtos);
            } catch (RuntimeException e) {
                LOG.warn("[COMMON CACHE] Failed to store detail list cache ({}): {}", groupCode, e.getMessage());
            }
        }
        return detailDtos;
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

    private void evictIfPresent(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evictIfPresent(key);
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to evict cache {}: {}", cacheName, e.getMessage());
        }
    }

    private void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (RuntimeException e) {
            LOG.warn("[COMMON CACHE] Failed to clear cache {}: {}", cacheName, e.getMessage());
        }
    }
}
