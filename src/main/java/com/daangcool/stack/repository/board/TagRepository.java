package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Tag} entity.
 *
 *  Hibernate @Filter(softDeleteFilter) 적용으로 Soft Delete 자동 필터링
 *  태그 이름 중복 방지 (unique 제약)
 *  자동완성 / 추천 / 사용 빈도 통계 기능 포함
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    // -----------------------------------------------------
    // 🔹 기본 조회 (softDeleteFilter 적용)
    // -----------------------------------------------------

    /**
     * 전체 태그 목록 (이름 순 정렬)
     * softDeleteFilter가 Soft Delete를 자동 필터링합니다.
     */
    @Override
    @Query("SELECT t FROM Tag t ORDER BY t.name ASC")
    List<Tag> findAll();

    /**
     * 태그 이름으로 단건 조회 (대소문자 구분 없음)
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) = LOWER(:name)")
    Optional<Tag> findByNameIgnoreCase(@Param("name") String name);


    // -----------------------------------------------------
    // 🔹 자동완성 / 추천 기능
    // -----------------------------------------------------

    /**
     * 자동완성 기능용 태그 검색 (Prefix 매칭)
     * 사용 빈도(usageCount) 기준 내림차순 정렬
     */
    @Query("""
        SELECT t FROM Tag t
        WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))
        ORDER BY t.usageCount DESC, t.name ASC
    """)
    List<Tag> findTopByPrefix(@Param("prefix") String prefix, Pageable pageable);


    /**
     * 인기 태그 전체 목록 (사용 빈도 순 페이징)
     * findTop10ByOrderByUsageCountDesc 기능을 대체합니다.
     */
    Page<Tag> findAllByOrderByUsageCountDesc(Pageable pageable);


    // -----------------------------------------------------
    // 🔹 통계 / 카운트 (softDeleteFilter 적용)
    // -----------------------------------------------------

    /**
     * 태그 총 개수 (활성 상태만 카운트)
     */
    @Query("SELECT COUNT(t) FROM Tag t")
    long countActiveTags();

    /**
     * 특정 이름 패턴으로 시작하는 태그 개수 (자동완성용)
     */
    @Query("SELECT COUNT(t) FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    long countTagsByPrefix(@Param("prefix") String prefix);


    // -----------------------------------------------------
    // 🔹 Soft Delete / 상태 변경
    // -----------------------------------------------------

    /**
     * 태그 논리 삭제 (Soft Delete)
     * Auditing 필드(lastModifiedBy, lastModifiedDate)는 자동으로 업데이트됩니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Tag t SET t.deleted = true WHERE t.id = :id")
    int softDelete(@Param("id") Long id);

    /**
     * 사용 빈도 증가 (태그가 게시글에 추가될 때 호출)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :id")
    void increaseUsage(@Param("id") Long id);

    /**
     * 사용 빈도 감소 (게시글에서 태그 제거 시 호출)
     * usageCount가 0 미만이 되는 것을 방지하는 방어 로직 포함.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Tag t SET t.usageCount = CASE WHEN t.usageCount > 0 THEN t.usageCount - 1 ELSE 0 END WHERE t.id = :id")
    void decreaseUsage(@Param("id") Long id);

    /**
     * ID로 태그를 조회합니다. isDeleted 상태와 관계없이 조회합니다.
     * 테스트 또는 관리자 기능에서 논리적으로 삭제된 태그를 조회할 때 사용합니다.
     */
    @Query("SELECT t FROM Tag t WHERE t.id = :id")
    Optional<Tag> findByIdEvenIfDeleted(@Param("id") Long id);
}
