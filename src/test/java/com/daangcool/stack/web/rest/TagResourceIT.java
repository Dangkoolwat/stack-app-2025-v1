package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.service.dto.TagDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Import added
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트: TagResource (API 계층)
 * - @AutoConfigureMockMvc로 MockMvc를 주입받아 실제 HTTP 요청을 시뮬레이션합니다.
 * - 공개 API이므로 별도의 인증 설정 없이 테스트를 진행합니다.
 */
@AutoConfigureMockMvc
@IntegrationTest
class TagResourceIT {

    private static final String DEFAULT_TAG_NAME = "java";
    private static final String ANOTHER_TAG_NAME = "spring";

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper om;

    private Tag tag;

    @BeforeEach
    public void initTest() {
        // 테스트 실행 전, 데이터베이스를 초기화합니다.
        tagRepository.deleteAll();

        // 테스트에 사용할 엔티티 객체를 생성합니다.
        tag = new Tag();
        tag.setName(DEFAULT_TAG_NAME);
    }

    /**
     * 태그 생성 API 테스트 (POST /api/tags)
     * - 새로운 태그를 성공적으로 생성하는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser // Added @WithMockUser
    void createTag() throws Exception {
        // given
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName(DEFAULT_TAG_NAME);

        // when & then
        restMockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(tagDTO)))
            .andExpect(status().isCreated());

        // 데이터베이스에 태그가 1개 저장되었는지 확인합니다.
        assertThat(tagRepository.count()).isEqualTo(1);
        Tag savedTag = tagRepository.findAll().get(0);
        assertThat(savedTag.getName()).isEqualTo(DEFAULT_TAG_NAME);
    }

    /**
     * 모든 태그 조회 API 테스트 (GET /api/tags)
     * - 저장된 모든 태그 목록을 정상적으로 반환하는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser // Added @WithMockUser
    void getAllTags() throws Exception {
        // given
        tagRepository.saveAndFlush(tag);

        // when & then
        restMockMvc.perform(get("/api/tags"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_TAG_NAME)));
    }

    /**
     * 단일 태그 조회 API 테스트 (GET /api/tags/{id})
     * - ID로 특정 태그를 정확하게 조회하는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser // Added @WithMockUser
    void getTag() throws Exception {
        // given
        tagRepository.saveAndFlush(tag);

        // when & then
        restMockMvc.perform(get("/api/tags/{id}", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tag.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_TAG_NAME));
    }

    /**
     * 접두어 검색 API 테스트 (GET /api/tags/search)
     * - 'ja'라는 접두어로 검색했을 때 'java' 태그가 포함되는지 확인합니다.
     */
    @Test
    @Transactional
    @WithMockUser // Added @WithMockUser
    void searchByPrefix() throws Exception {
        // given
        tagRepository.saveAndFlush(tag);
        Tag anotherTag = new Tag();
        anotherTag.setName(ANOTHER_TAG_NAME);
        tagRepository.saveAndFlush(anotherTag);

        // when & then
        restMockMvc.perform(get("/api/tags/search?prefix=ja"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[0].name", is("java")));
    }
}
