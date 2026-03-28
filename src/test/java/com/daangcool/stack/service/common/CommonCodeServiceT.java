package com.daangcool.stack.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daangcool.stack.common.constant.CacheNames;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.DetailDto;
import com.daangcool.stack.service.dto.CommonCodeCacheDto.GroupDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class CommonCodeServiceT {

    @Mock
    private CommonCodeGroupRepository groupRepository;

    @Mock
    private CommonCodeDetailRepository detailRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private CommonCodeService commonCodeService;

    private CommonCodeGroup testGroup;
    private CommonCodeDetail testDetail;

    @BeforeEach
    void setUp() {
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);

        testGroup = new CommonCodeGroup();
        testGroup.setGroupCode("TEST_GROUP");
        testGroup.setGroupName("테스트 그룹");
        testGroup.setDeleted(false);

        testDetail = new CommonCodeDetail();
        testDetail.setId(1L);
        testDetail.setCode("DETAIL");
        testDetail.setName("상세");
        testDetail.setGroup(testGroup);
        testDetail.setDeleted(false);
    }

    @Test
    void findGroup_WhenCacheHit_ShouldReturnCachedDto() {
        GroupDto cached = GroupDto.from(testGroup);
        when(cache.get("TEST_GROUP", GroupDto.class)).thenReturn(cached);

        Optional<GroupDto> found = commonCodeService.findGroup("TEST_GROUP");

        assertThat(found).contains(cached);
        verify(groupRepository, never()).findOneByGroupCodeAndDeletedIsFalse(anyString());
    }

    @Test
    void findGroup_WhenCacheMiss_ShouldLoadFromRepositoryAndPopulateCache() {
        when(cache.get("TEST_GROUP", GroupDto.class)).thenReturn(null);
        when(groupRepository.findOneByGroupCodeAndDeletedIsFalse("TEST_GROUP")).thenReturn(Optional.of(testGroup));

        Optional<GroupDto> found = commonCodeService.findGroup("TEST_GROUP");

        assertThat(found).isPresent();
        assertThat(found.get().groupCode()).isEqualTo("TEST_GROUP");
        verify(cache, times(1)).put("TEST_GROUP", found.get());
    }

    @Test
    void findAllDetailsByGroup_WhenCacheReadFails_ShouldFallbackToRepository() {
        when(cache.get("TEST_GROUP", List.class)).thenThrow(new RuntimeException("cache down"));
        when(detailRepository.findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc("TEST_GROUP")).thenReturn(List.of(testDetail));

        List<DetailDto> details = commonCodeService.findAllDetailsByGroup("TEST_GROUP");

        assertThat(details).hasSize(1);
        assertThat(details.get(0).group()).isNotNull();
        assertThat(details.get(0).group().groupCode()).isEqualTo("TEST_GROUP");
    }

    @Test
    void findAllGroups_WhenCachedAsMaps_ShouldNormalizeToDtos() {
        when(cache.get("all", List.class)).thenReturn(
            List.of(
                Map.of("groupCode", "TEST_GROUP", "groupName", "테스트 그룹", "displayOrder", 1, "deleted", false, "description", "설명")
            )
        );

        List<GroupDto> groups = commonCodeService.findAllGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).groupCode()).isEqualTo("TEST_GROUP");
        verify(groupRepository, never()).findAllByDeletedIsFalseOrderByDisplayOrderAsc();
    }

    @Test
    void findAllDetailsByGroup_WhenCachedAsMaps_ShouldNormalizeToDtos() {
        when(cache.get("TEST_GROUP", List.class)).thenReturn(
            List.of(
                Map.ofEntries(
                    Map.entry("id", 1L),
                    Map.entry("code", "DETAIL"),
                    Map.entry("name", "상세"),
                    Map.entry("sortOrder", 10),
                    Map.entry("deleted", false),
                    Map.entry("group", Map.of("groupCode", "TEST_GROUP")),
                    Map.entry("attribute1", "A1"),
                    Map.entry("attribute2", "A2"),
                    Map.entry("attribute3", "A3"),
                    Map.entry("attribute4", "A4"),
                    Map.entry("attribute5", "A5"),
                    Map.entry("description", "설명")
                )
            )
        );

        List<DetailDto> details = commonCodeService.findAllDetailsByGroup("TEST_GROUP");

        assertThat(details).hasSize(1);
        assertThat(details.get(0).group()).isNotNull();
        assertThat(details.get(0).group().groupCode()).isEqualTo("TEST_GROUP");
        verify(detailRepository, never()).findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc("TEST_GROUP");
    }

    @Test
    void createGroup_WhenExistingDeletedCode_ShouldThrowGroupDeletedException() {
        CommonCodeGroup deletedGroup = new CommonCodeGroup();
        deletedGroup.setGroupCode("DELETED_GROUP");
        deletedGroup.setDeleted(true);

        when(groupRepository.findById("DELETED_GROUP")).thenReturn(Optional.of(deletedGroup));

        CommonCodeGroup newGroup = new CommonCodeGroup();
        newGroup.setGroupCode("DELETED_GROUP");

        assertThatThrownBy(() -> commonCodeService.createGroup(newGroup))
            .isInstanceOf(BadRequestAlertException.class)
            .hasFieldOrPropertyWithValue("errorKey", "groupdeleted");
    }

    @Test
    void createDetail_WhenExistingDeletedCode_ShouldThrowCodeDeletedException() {
        when(groupRepository.findOneByGroupCodeAndDeletedIsFalse("TEST_GROUP")).thenReturn(Optional.of(testGroup));

        CommonCodeDetail deletedDetail = new CommonCodeDetail();
        deletedDetail.setCode("DELETED_CODE");
        deletedDetail.setDeleted(true);

        when(detailRepository.findOneByGroupGroupCodeAndCode("TEST_GROUP", "DELETED_CODE")).thenReturn(Optional.of(deletedDetail));

        CommonCodeDetail newDetail = new CommonCodeDetail();
        newDetail.setCode("DELETED_CODE");
        newDetail.setGroup(testGroup);

        assertThatThrownBy(() -> commonCodeService.createDetail(newDetail))
            .isInstanceOf(BadRequestAlertException.class)
            .hasFieldOrPropertyWithValue("errorKey", "codedeleted");
    }

    @Test
    void softDeleteGroup_ShouldEvictGroupAndListCaches() {
        when(groupRepository.findOneByGroupCodeAndDeletedIsFalse("TEST_GROUP")).thenReturn(Optional.of(testGroup));

        commonCodeService.softDeleteGroup("TEST_GROUP");

        verify(cacheManager, times(1)).getCache(CacheNames.COMMON_GROUPS);
        verify(cacheManager, times(1)).getCache(CacheNames.COMMON_GROUP_LIST);
        verify(cache, times(1)).evictIfPresent(eq("TEST_GROUP"));
        verify(cache, times(1)).clear();
    }
}
