package com.daangcool.stack.service.board;

import com.daangcool.stack.common.exception.BadRequestAlertException;
import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.service.dto.TagDTO;
import com.daangcool.stack.service.mapper.TagMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TagService
 * -----------------------------------------------------------
 * 게시판 태그(Tag) 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 *
 * 주요 기능:
 *  - 태그 등록, 수정, 삭제(Soft Delete), 복구
 *  - 전체 / 인기 / 접두사 검색
 *  - 캐시 관리 (byId, all, prefix, popular)
 * -----------------------------------------------------------
 */
@Service
@Transactional
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);
    private static final String ENTITY_NAME = "tag";

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final CacheManager cacheManager;

    // ------------------------------------------------------
    // 캐시 이름 상수 (CacheConfiguration의 이름과 일치해야 함)
    // ------------------------------------------------------
    public static final String CACHE_TAG_BY_ID = "TAG_BY_ID";
    public static final String CACHE_TAG_ALL = "TAG_ALL";
    public static final String CACHE_TAG_PREFIX = "TAG_PREFIX";
    public static final String CACHE_TAG_POPULAR = "TAG_POPULAR";

    public TagService(TagRepository tagRepository, TagMapper tagMapper, CacheManager cacheManager) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.cacheManager = cacheManager;
    }

    // ------------------------------------------------------
    /** 캐시 초기화 유틸리티 */
    private void clearTagCaches(Tag tag) {
        try {
            Optional.ofNullable(cacheManager.getCache(CACHE_TAG_BY_ID))
                .ifPresent(c -> c.evictIfPresent(tag.getId()));
            Arrays.asList(CACHE_TAG_ALL, CACHE_TAG_PREFIX, CACHE_TAG_POPULAR)
                .forEach(name -> Optional.ofNullable(cacheManager.getCache(name))
                    .ifPresent(Cache::clear));
            log.debug("[TAG CACHE] Cleared caches for tag id={}", tag.getId());
        } catch (Exception e) {
            log.warn("[TAG CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }

    /** 모든 태그 관련 캐시 전체 초기화 */
    public void clearAllTagCaches() {
        List.of(CACHE_TAG_BY_ID, CACHE_TAG_ALL, CACHE_TAG_PREFIX, CACHE_TAG_POPULAR)
            .forEach(name -> Optional.ofNullable(cacheManager.getCache(name))
                .ifPresent(cache -> {
                    log.debug("[TAG CACHE] Clearing cache: {}", name);
                    cache.clear();
                }));
    }

    // ------------------------------------------------------
    /** 태그 저장 */
    public TagDTO save(TagDTO tagDTO) {
        log.debug("Request to save Tag : {}", tagDTO);

        String name = tagDTO.getName();
        if (name == null || name.isBlank())
            throw new BadRequestAlertException("태그 이름은 비어 있을 수 없습니다.", ENTITY_NAME, "tag.emptyName");

        Optional<Tag> existing = tagRepository.findByNameIgnoreCase(name);
        if (existing.isPresent() && !existing.get().isDeleted()) {
            throw new BadRequestAlertException(
                URI.create("/problem/tag/duplicate"),
                "이미 존재하는 태그입니다: " + name,
                ENTITY_NAME,
                "tag.exists"
            );
        }

        Tag tag = tagMapper.toEntity(tagDTO);
        tag = tagRepository.save(tag);
        final Tag savedTag = tag; // effectively final

        clearTagCaches(savedTag);

        Optional.ofNullable(cacheManager.getCache(CACHE_TAG_BY_ID))
            .ifPresent(cache -> cache.put(savedTag.getId(), tagMapper.toDto(savedTag)));

        return tagMapper.toDto(savedTag);
    }

    // ------------------------------------------------------
    /** 전체 태그 목록 (캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TagDTO> findAll() {
        Cache cache = cacheManager.getCache(CACHE_TAG_ALL);
        if (cache != null) {
            List<TagDTO> cached = (List<TagDTO>) cache.get("all", List.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for all tags");
                return cached;
            }
        }

        List<TagDTO> tags = tagRepository.findAll().stream()
            .filter(tag -> !tag.isDeleted())
            .sorted(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER))
            .map(tagMapper::toDto)
            .collect(Collectors.toList());

        if (cache != null && !tags.isEmpty()) cache.put("all", tags);
        return tags;
    }

    // ------------------------------------------------------
    /** 단건 조회 (캐시 활용) */
    @Transactional(readOnly = true)
    public Optional<TagDTO> findOne(Long id) {
        Cache cache = cacheManager.getCache(CACHE_TAG_BY_ID);
        if (cache != null) {
            TagDTO cached = cache.get(id, TagDTO.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for id={}", id);
                return Optional.of(cached);
            }
        }

        Optional<TagDTO> dtoOpt = tagRepository.findById(id)
            .filter(tag -> !tag.isDeleted())
            .map(tagMapper::toDto);

        dtoOpt.ifPresent(dto ->
            Optional.ofNullable(cacheManager.getCache(CACHE_TAG_BY_ID))
                .ifPresent(c -> c.put(id, dto))
        );

        return dtoOpt;
    }

    // ------------------------------------------------------
    /** 태그 Soft Delete */
    public void delete(Long id) {
        log.debug("Request to soft delete Tag : {}", id);
        int updated = tagRepository.softDelete(id);
        if (updated == 0)
            throw new EntityNotFoundException("삭제할 태그를 찾을 수 없습니다. ID=" + id);

        tagRepository.findById(id).ifPresent(this::clearTagCaches);
    }

    // ------------------------------------------------------
    /** 태그 복구 */
    public void undelete(Long id) {
        log.debug("Request to undelete Tag : {}", id);
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다. ID=" + id));
        tag.setDeleted(false);
        Tag savedTag = tagRepository.save(tag);
        clearTagCaches(savedTag);
    }

    // ------------------------------------------------------
    /** Prefix 검색 (캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TagDTO> searchByPrefix(String prefix, int limit) {
        Cache cache = cacheManager.getCache(CACHE_TAG_PREFIX);
        String key = prefix.toLowerCase();
        if (cache != null) {
            List<TagDTO> cached = (List<TagDTO>) cache.get(key, List.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for prefix={}", prefix);
                return cached;
            }
        }

        List<TagDTO> tags = tagRepository.findTopByPrefix(prefix, PageRequest.of(0, limit))
            .stream()
            .map(tagMapper::toDto)
            .collect(Collectors.toList());

        if (cache != null && !tags.isEmpty()) cache.put(key, tags);
        return tags;
    }

    // ------------------------------------------------------
    /** 인기 태그 (캐시 활용) */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TagDTO> findPopularTags(int limit) {
        Cache cache = cacheManager.getCache(CACHE_TAG_POPULAR);
        if (cache != null) {
            List<TagDTO> cached = (List<TagDTO>) cache.get("popular", List.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for popular tags");
                return cached;
            }
        }

        List<TagDTO> tags = tagRepository.findAllByOrderByUsageCountDesc(PageRequest.of(0, limit))
            .map(tagMapper::toDto)
            .toList();

        if (cache != null && !tags.isEmpty()) cache.put("popular", tags);
        return tags;
    }
}
