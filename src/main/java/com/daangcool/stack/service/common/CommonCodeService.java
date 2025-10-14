package com.daangcool.stack.service.common;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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
    // 전체 그룹 리스트 조회 시 캐싱 적용 (키는 사용하지 않음 - 리스트 전체)
    @Cacheable(value = COMMON_GROUP_LIST_CACHE, unless = "#result.isEmpty()")
    public List<CommonCodeGroup> findAllGroups() {
        return groupRepository.findAllByDeletedIsFalseOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    // 단일 그룹 코드 조회 시 캐싱 적용 (groupCode를 키로 사용)
    @Cacheable(value = COMMON_GROUP_CACHE, key = "#groupCode", unless = "#result == null")
    public Optional<CommonCodeGroup> findGroup(String groupCode) {
        return groupRepository.findOneByGroupCodeAndDeletedIsFalse(groupCode);
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
    // 단일 상세 코드 조회 시 캐싱 적용 (ID를 키로 사용)
    @Cacheable(value = COMMON_DETAIL_CACHE, key = "#id", unless = "#result == null")
    public Optional<CommonCodeDetail> findDetail(Long id) {
        return detailRepository.findById(id).filter(detail -> !detail.isDeleted());
    }

    @Transactional(readOnly = true)
    // 그룹별 상세 코드 리스트 조회 시 캐싱 적용 (groupCode를 키로 사용)
    @Cacheable(value = COMMON_DETAIL_LIST_BY_GROUP_CACHE, key = "#groupCode", unless = "#result.isEmpty()")
    public List<CommonCodeDetail> findAllDetailsByGroup(String groupCode) {
        return detailRepository.findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc(groupCode);
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
}
