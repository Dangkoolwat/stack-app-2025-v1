package com.daangcool.stack.web.rest.common;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.common.CommonCodeDetail;
import com.daangcool.stack.domain.common.CommonCodeGroup;
import com.daangcool.stack.repository.common.CommonCodeDetailRepository;
import com.daangcool.stack.repository.common.CommonCodeGroupRepository;
import com.daangcool.stack.security.AuthoritiesConstants;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: CommonCodeResource (API 계층)
 * - @AutoConfigureMockMvc로 MockMvc를 주입받아 실제 HTTP 요청을 시뮬레이션
 * - @WithMockUser로 ADMIN 권한을 부여하여 인증된 요청을 테스트
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class CommonCodeResourceIT {

    // CommonCodeGroup constants
    private static final String DEFAULT_GROUP_CODE = "STATUS";
    private static final String DEFAULT_GROUP_NAME = "상태 그룹";
    private static final String UPDATED_GROUP_NAME = "상태 그룹 (수정)";

    // CommonCodeDetail constants
    private static final String DEFAULT_DETAIL_CODE = "01";
    private static final String DEFAULT_DETAIL_NAME = "정상";
    private static final String UPDATED_DETAIL_NAME = "사용중";

    @Autowired
    private CommonCodeGroupRepository commonCodeGroupRepository;

    @Autowired
    private CommonCodeDetailRepository commonCodeDetailRepository;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper om;

    private CommonCodeGroup commonCodeGroup;
    private CommonCodeDetail commonCodeDetail;

    @BeforeEach
    public void initTest() {
        commonCodeGroup = new CommonCodeGroup();
        commonCodeGroup.setGroupCode(DEFAULT_GROUP_CODE);
        commonCodeGroup.setGroupName(DEFAULT_GROUP_NAME);

        commonCodeDetail = new CommonCodeDetail();
        commonCodeDetail.setGroup(commonCodeGroup);
        commonCodeDetail.setCode(DEFAULT_DETAIL_CODE);
        commonCodeDetail.setName(DEFAULT_DETAIL_NAME);
    }

    // --- CommonCodeGroup Tests ---

    @Test
    @Transactional
    void createGroup() throws Exception {
        commonCodeGroupRepository.deleteAll();
        restMockMvc.perform(post("/api/common/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commonCodeGroup)))
            .andExpect(status().isCreated());
        assertThat(commonCodeGroupRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void updateGroup() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        commonCodeGroup.setGroupName(UPDATED_GROUP_NAME);
        restMockMvc.perform(put("/api/common/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commonCodeGroup)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groupName").value(UPDATED_GROUP_NAME));
    }

    @Test
    @Transactional
    void getAllGroups() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        restMockMvc.perform(get("/api/common/groups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].groupCode").value(hasItem(DEFAULT_GROUP_CODE)));
    }

    @Test
    @Transactional
    void getGroup() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        restMockMvc.perform(get("/api/common/groups/{groupCode}", DEFAULT_GROUP_CODE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groupCode").value(DEFAULT_GROUP_CODE));
    }

    @Test
    @Transactional
    void softDeleteGroup() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        restMockMvc.perform(delete("/api/common/groups/{groupCode}", DEFAULT_GROUP_CODE))
            .andExpect(status().isNoContent());
        assertThat(commonCodeGroupRepository.findOneByGroupCodeAndDeletedIsFalse(DEFAULT_GROUP_CODE)).isEmpty();
    }

    // --- CommonCodeDetail Tests ---

    @Test
    @Transactional
    void createDetail() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        restMockMvc.perform(post("/api/common/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commonCodeDetail)))
            .andExpect(status().isCreated());
        assertThat(commonCodeDetailRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void updateDetail() throws Exception {
        // given
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        CommonCodeDetail savedDetail = commonCodeDetailRepository.saveAndFlush(commonCodeDetail);
        Long detailId = savedDetail.getId(); // DB에 저장된 후 생성된 ID를 가져옵니다.

        // 엔티티 직렬화 문제를 피하기 위해 요청 본문을 Map으로 직접 구성합니다.
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", detailId); // ID를 명시적으로 넣어줍니다.
        updateRequest.put("code", savedDetail.getCode());
        updateRequest.put("name", UPDATED_DETAIL_NAME);
        updateRequest.put("group", commonCodeGroup);
        updateRequest.put("sortOrder", savedDetail.getSortOrder());

        // when & then
        restMockMvc.perform(put("/api/common/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateRequest))) // Map으로 만든 요청 본문을 전송합니다.
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(UPDATED_DETAIL_NAME));
    }

    @Test
    @Transactional
    void getAllDetailsByGroup() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        commonCodeDetailRepository.saveAndFlush(commonCodeDetail);
        restMockMvc.perform(get("/api/common/details/group/{groupCode}", DEFAULT_GROUP_CODE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_DETAIL_CODE)));
    }

    @Test
    @Transactional
    void getDetail() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        // saveAndFlush가 반환하는, ID가 보장된 인스턴스를 사용합니다.
        CommonCodeDetail savedDetail = commonCodeDetailRepository.saveAndFlush(commonCodeDetail);

        restMockMvc.perform(get("/api/common/details/{id}", savedDetail.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(DEFAULT_DETAIL_CODE));
    }

    @Test
    @Transactional
    void softDeleteDetail() throws Exception {
        commonCodeGroupRepository.saveAndFlush(commonCodeGroup);
        // saveAndFlush가 반환하는, ID가 보장된 인스턴스를 사용합니다.
        CommonCodeDetail savedDetail = commonCodeDetailRepository.saveAndFlush(commonCodeDetail);

        restMockMvc.perform(delete("/api/common/details/{id}", savedDetail.getId()))
            .andExpect(status().isNoContent());

        // 삭제 후, 해당 ID로 조회했을 때 결과가 없는지 확인합니다.
        assertThat(commonCodeDetailRepository.findById(savedDetail.getId()).filter(d -> !d.isDeleted())).isEmpty();
    }
}
