package com.daangcool.stack.service.board;

import com.daangcool.stack.domain.board.Tag;
import com.daangcool.stack.repository.board.TagRepository;
import com.daangcool.stack.service.dto.TagDTO;
import com.daangcool.stack.service.mapper.TagMapper;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService {

    private final Logger log = LoggerFactory.getLogger(TagService.class);
    private static final String ENTITY_NAME = "tag";

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final CacheManager cacheManager;

    // 전역 캐시 상수 선언 (모두 대문자 + _)
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
    //  캐시 유틸리티
    // ------------------------------------------------------
    private void clearTagCaches(Tag tag) {
        try {
            Objects.requireNonNull(cacheManager.getCache(CACHE_TAG_BY_ID)).evictIfPresent(tag.getId());
            Objects.requireNonNull(cacheManager.getCache(CACHE_TAG_ALL)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_TAG_PREFIX)).clear();
            Objects.requireNonNull(cacheManager.getCache(CACHE_TAG_POPULAR)).clear();
        } catch (Exception e) {
            log.warn("[TAG CACHE] 캐시 제거 중 오류: {}", e.getMessage());
        }
    }

    public void clearAllTagCaches() {
        List<String> caches = List.of(CACHE_TAG_BY_ID, CACHE_TAG_ALL, CACHE_TAG_PREFIX, CACHE_TAG_POPULAR);
        caches.forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                log.debug("Clearing cache: {}", name);
                cache.clear();
            }
        });
    }

    // ------------------------------------------------------
    //  태그 저장
    // ------------------------------------------------------
    public TagDTO save(TagDTO tagDTO) {
        log.debug("Request to save Tag : {}", tagDTO);

        String name = tagDTO.getName();
        if (name == null || name.isBlank()) {
            throw new BadRequestAlertException("태그 이름은 비어 있을 수 없습니다.", ENTITY_NAME, "tag.emptyName");
        }

        Optional<Tag> existingTag = tagRepository.findByNameIgnoreCase(name);
        if (existingTag.isPresent() && !existingTag.get().isDeleted()) {
            throw new BadRequestAlertException(
                URI.create("/problem/tag/duplicate"),
                "이미 존재하는 태그입니다: " + name,
                ENTITY_NAME,
                "tag.exists"
            );
        }

        Tag tag = tagMapper.toEntity(tagDTO);
        tag = tagRepository.save(tag);

        clearTagCaches(tag);

        Cache cache = cacheManager.getCache(CACHE_TAG_BY_ID);
        if (cache != null) cache.put(tag.getId(), tagMapper.toDto(tag));

        return tagMapper.toDto(tag);
    }

    // ------------------------------------------------------
    //  전체 태그 목록 (캐시 사용)
    // ------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TagDTO> findAll() {
        Cache cache = cacheManager.getCache(CACHE_TAG_ALL);
        if (cache != null) {
            List<TagDTO> cached = cache.get("all", List.class);
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

        if (cache != null) cache.put("all", tags);
        return tags;
    }

    // ------------------------------------------------------
    //  단건 조회 (캐시 사용)
    // ------------------------------------------------------
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

        Optional<TagDTO> tagOpt = tagRepository.findById(id)
            .filter(tag -> !tag.isDeleted())
            .map(tagMapper::toDto);

        tagOpt.ifPresent(dto -> {
            if (cache != null) cache.put(id, dto);
        });

        return tagOpt;
    }

    // ------------------------------------------------------
    //  Soft Delete
    // ------------------------------------------------------
    public void delete(Long id) {
        log.debug("Request to soft delete Tag : {}", id);
        int updated = tagRepository.softDelete(id);
        if (updated == 0) throw new EntityNotFoundException("삭제할 태그를 찾을 수 없습니다. ID=" + id);
        tagRepository.findById(id).ifPresent(this::clearTagCaches);
    }

    // ------------------------------------------------------
    //  복구
    // ------------------------------------------------------
    public void undelete(Long id) {
        log.debug("Request to undelete Tag : {}", id);
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다. ID=" + id));
        tag.setDeleted(false);
        tagRepository.save(tag);
        clearTagCaches(tag);
    }

    // ------------------------------------------------------
    //  Prefix 검색 (캐시 사용)
    // ------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TagDTO> searchByPrefix(String prefix, int limit) {
        Cache cache = cacheManager.getCache(CACHE_TAG_PREFIX);
        if (cache != null) {
            List<TagDTO> cached = cache.get(prefix.toLowerCase(), List.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for prefix={}", prefix);
                return cached;
            }
        }

        List<TagDTO> tags = tagRepository.findTopByPrefix(prefix, org.springframework.data.domain.PageRequest.of(0, limit))
            .stream()
            .map(tagMapper::toDto)
            .collect(Collectors.toList());

        if (cache != null) cache.put(prefix.toLowerCase(), tags);
        return tags;
    }

    // ------------------------------------------------------
    //  인기 태그 (캐시 사용)
    // ------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TagDTO> findPopularTags(int limit) {
        Cache cache = cacheManager.getCache(CACHE_TAG_POPULAR);
        if (cache != null) {
            List<TagDTO> cached = cache.get("popular", List.class);
            if (cached != null) {
                log.debug("[TAG CACHE] Hit for popular tags");
                return cached;
            }
        }

        List<TagDTO> tags = tagRepository.findAllByOrderByUsageCountDesc(org.springframework.data.domain.PageRequest.of(0, limit))
            .map(tagMapper::toDto)
            .toList();

        if (cache != null) cache.put("popular", tags);
        return tags;
    }
}
