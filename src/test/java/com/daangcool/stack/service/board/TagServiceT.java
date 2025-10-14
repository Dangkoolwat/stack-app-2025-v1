package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.service.dto.TagDTO;
import com.daangcool.stack.service.mapper.TagMapper;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: TagService
 * - @ExtendWith(MockitoExtension.class)로 Mockito 프레임워크를 사용합니다.
 * - @Mock으로 Repository, Mapper, CacheManager를 가짜 객체로 만듭니다.
 * - @InjectMocks는 @Mock 객체를 테스트 대상인 TagService에 주입합니다.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceT {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        // 모든 테스트 전에 공통적으로 사용할 객체를 초기화합니다.
        tag = new Tag();
        tag.setId(1L);
        tag.setName("java");

        tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("java");

        // CacheManager의 동작을 모의(mock)합니다.
        // 어떤 캐시 이름을 받더라도 항상 mock cache 객체를 반환하도록 설정합니다.
        lenient().when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    /**
     * 태그 저장 테스트 (성공 케이스)
     * - 새로운 태그를 성공적으로 저장하는지 확인합니다.
     */
    @Test
    void save_NewTag_ShouldSaveAndReturnDTO() {
        // given
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagMapper.toEntity(any(TagDTO.class))).thenReturn(tag);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        when(tagMapper.toDto(any(Tag.class))).thenReturn(tagDTO);

        // when
        TagDTO result = tagService.save(tagDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(tagDTO.getName());
        verify(tagRepository, times(1)).save(any(Tag.class)); // save 메소드가 1번 호출되었는지 검증
        verify(cache, times(3)).clear(); // 캐시가 제거되었는지 검증
    }

    /**
     * 태그 저장 테스트 (실패 케이스 - 중복 이름)
     * - 이미 존재하는 이름으로 태그를 저장하려고 할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void save_ExistingTag_ShouldThrowException() {
        // given
        // 동일한 이름의 태그가 이미 존재한다고 가정합니다.
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(tag));

        // when & then
        // BadRequestAlertException이 발생하는지 검증합니다.
        assertThatThrownBy(() -> tagService.save(tagDTO))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("이미 존재하는 태그입니다");
    }

    /**
     * 모든 태그 조회 테스트
     * - 삭제되지 않은 모든 태그 목록을 DTO로 변환하여 반환하는지 확인합니다.
     */
    @Test
    void findAll_ShouldReturnDtoList() {
        // given
        when(cache.get(anyString(), eq(List.class))).thenReturn(null); // 캐시가 비어있다고 가정
        when(tagRepository.findAll()).thenReturn(List.of(tag));
        when(tagMapper.toDto(any(Tag.class))).thenReturn(tagDTO);

        // when
        List<TagDTO> results = tagService.findAll();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo(tag.getName());
        verify(cache, times(1)).put(anyString(), anyList()); // 결과를 캐시에 저장하는지 검증
    }

    /**
     * 단일 태그 조회 테스트
     * - ID로 태그를 조회하고 DTO로 변환하여 반환하는지 확인합니다.
     */
    @Test
    void findOne_ShouldReturnDto() {
        // given
        when(cache.get(anyLong(), eq(TagDTO.class))).thenReturn(null); // 캐시가 비어있다고 가정
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        when(tagMapper.toDto(any(Tag.class))).thenReturn(tagDTO);

        // when
        Optional<TagDTO> result = tagService.findOne(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(tag.getName());
        verify(cache, times(1)).put(anyLong(), any(TagDTO.class)); // 결과를 캐시에 저장하는지 검증
    }

    /**
     * 태그 삭제 테스트 (Soft Delete)
     * - 태그를 논리적으로 삭제 처리하는지 확인합니다.
     */
    @Test
    void delete_ShouldCallSoftDelete() {
        // given
        when(tagRepository.softDelete(anyLong())).thenReturn(1); // 1개 행이 업데이트되었다고 가정
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));

        // when
        tagService.delete(1L);

        // then
        verify(tagRepository, times(1)).softDelete(1L); // softDelete 메소드가 1번 호출되었는지 검증
        verify(cache, times(3)).clear(); // 관련된 모든 캐시가 제거되었는지 검증
    }

    /**
     * 태그 삭제 테스트 (실패 케이스 - 존재하지 않는 태그)
     * - 존재하지 않는 ID로 삭제를 시도할 때 예외가 발생하는지 확인합니다.
     */
    @Test
    void delete_NonExistingTag_ShouldThrowException() {
        // given
        when(tagRepository.softDelete(anyLong())).thenReturn(0); // 업데이트된 행이 없다고 가정

        // when & then
        assertThatThrownBy(() -> tagService.delete(1L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("삭제할 태그를 찾을 수 없습니다");
    }
}
