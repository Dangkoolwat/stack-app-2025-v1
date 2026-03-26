package com.daangcool.stack.service.common;

import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 서비스 단위 테스트: CommonCodeService
 * - @ExtendWith(MockitoExtension.class)로 Mockito 프레임워크를 사용
 * - @Mock으로 레포지토리를 가짜 객체로 만들어 서비스 로직만 순수하게 테스트
 * - @InjectMocks는 @Mock으로 생성된 가짜 객체를 테스트 대상 서비스에 주입
 */
@ExtendWith(MockitoExtension.class)
class CommonCodeServiceT {

    @Mock
    private CommonCodeGroupRepository groupRepository;
    @Mock
    private com.daangcool.stack.repository.common.CommonCodeDetailRepository detailRepository;
    @Mock
    private org.springframework.cache.CacheManager cacheManager;
    @Mock
    private org.springframework.cache.Cache cache;

    @InjectMocks
    private CommonCodeService commonCodeService;

    private CommonCodeGroup testGroup;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(cacheManager.getCache(org.mockito.ArgumentMatchers.anyString())).thenReturn(cache);

        testGroup = new CommonCodeGroup();
        testGroup.setGroupCode("TEST_GROUP");
        testGroup.setGroupName("테스트 그룹");
        testGroup.setDeleted(false);
    }

    @Test
    void whenFindGroup_thenReturnsGroup() {
        when(groupRepository.findOneByGroupCodeAndDeletedIsFalse("TEST_GROUP")).thenReturn(Optional.of(testGroup));

        Optional<CommonCodeGroup> found = commonCodeService.findGroup("TEST_GROUP");

        assertThat(found).isPresent();
        assertThat(found.get().getGroupCode()).isEqualTo("TEST_GROUP");
    }

    @Test
    void whenCreateGroupWithExistingDeletedCode_thenThrowsGroupDeletedException() {
        CommonCodeGroup deletedGroup = new CommonCodeGroup();
        deletedGroup.setGroupCode("DELETED_GROUP");
        deletedGroup.setDeleted(true);

        when(groupRepository.findById("DELETED_GROUP")).thenReturn(Optional.of(deletedGroup));

        CommonCodeGroup newGroup = new CommonCodeGroup();
        newGroup.setGroupCode("DELETED_GROUP");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> commonCodeService.createGroup(newGroup))
            .isInstanceOf(com.daangcool.stack.common.exception.BadRequestAlertException.class)
            .hasFieldOrPropertyWithValue("errorKey", "groupdeleted");
    }

    @Test
    void whenCreateDetailWithExistingDeletedCode_thenThrowsCodeDeletedException() {
        when(groupRepository.findOneByGroupCodeAndDeletedIsFalse("TEST_GROUP")).thenReturn(Optional.of(testGroup));

        com.daangcool.stack.domain.common.CommonCodeDetail deletedDetail = new com.daangcool.stack.domain.common.CommonCodeDetail();
        deletedDetail.setCode("DELETED_CODE");
        deletedDetail.setDeleted(true);

        when(detailRepository.findOneByGroupGroupCodeAndCode("TEST_GROUP", "DELETED_CODE"))
            .thenReturn(Optional.of(deletedDetail));

        com.daangcool.stack.domain.common.CommonCodeDetail newDetail = new com.daangcool.stack.domain.common.CommonCodeDetail();
        newDetail.setCode("DELETED_CODE");
        newDetail.setGroup(testGroup);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> commonCodeService.createDetail(newDetail))
            .isInstanceOf(com.daangcool.stack.common.exception.BadRequestAlertException.class)
            .hasFieldOrPropertyWithValue("errorKey", "codedeleted");
    }
}
