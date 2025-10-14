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
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @InjectMocks
    private CommonCodeService commonCodeService;

    private CommonCodeGroup commonCodeGroup;

    @BeforeEach
    void setUp() {
        // 모든 테스트 전에 공통적으로 사용할 테스트 데이터 설정
        commonCodeGroup = new CommonCodeGroup();
        commonCodeGroup.setGroupCode("TEST_GROUP");
        commonCodeGroup.setGroupName("테스트 그룹");
    }

    /**
     * 서비스 테스트: groupCode로 조회
     * - 레포지토리가 특정 groupCode에 대해 CommonCodeGroup 객체를 반환하도록 설정
     * - 서비스의 findGroup 메소드가 예상대로 동작하는지 확인
     */
    @Test
    void whenFindGroup_thenReturnsGroup() {
        // given
        when(commonCodeGroupRepository.findOneByGroupCodeAndDeletedIsFalse(anyString())).thenReturn(Optional.of(commonCodeGroup));

        // when
        Optional<CommonCodeGroup> found = commonCodeService.findGroup("TEST_GROUP");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getGroupCode()).isEqualTo("TEST_GROUP");
    }
}
