package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.BoardTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link BoardTag} mapping entity.
 *
 * Board ↔ Tag N:M 관계를 관리하는 중간 테이블
 * Soft Delete 자동 필터링 (Hibernate @Filter)
 * 게시글, 태그 기준 조회 및 삭제 제공
 */
@Repository
public interface BoardTagRepository extends JpaRepository<BoardTag, Long>, JpaSpecificationExecutor<BoardTag> {

    // -----------------------------------------------------
    //  기본 조회 (softDeleteFilter 적용)
    // -----------------------------------------------------

    /**
     * 특정 게시글에 연결된 활성 태그 전체 조회
     */
    @EntityGraph(attributePaths = {"tag"})
    List<BoardTag> findAllByBoard_IdOrderByIdAsc(Long boardId);

    /**
     * 특정 태그가 연결된 활성 게시글 목록 조회
     */
    @EntityGraph(attributePaths = {"board"})
    List<BoardTag> findAllByTag_Id(Long tagId);

    /**
     * 게시글-태그 중복 관계 존재 여부 확인 (활성 관계만 체크)
     */
    boolean existsByBoard_IdAndTag_Id(Long boardId, Long tagId);

    /**
     * 특정 ID를 가진 관계 단건 조회
     */
    Optional<BoardTag> findById(Long id);


    // -----------------------------------------------------
    //  Soft Delete (Auditing 포함)
    // -----------------------------------------------------

    /**
     * 특정 관계 ID를 논리 삭제합니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardTag bt SET bt.deleted = true, bt.description = :reason WHERE bt.id = :id")
    int softDelete(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 게시글 ID로 모든 활성 연결을 일괄 논리 삭제합니다.
     * Soft Delete Auditing: lastModifiedBy/Date 자동 기록 및 reason(description) 기록.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardTag bt SET bt.deleted = true, bt.description = :reason WHERE bt.board.id = :boardId AND bt.deleted = false")
    void softDeleteAllByBoardId(@Param("boardId") Long boardId, @Param("reason") String reason);

    /**
     * 태그 ID로 모든 활성 연결을 일괄 논리 삭제합니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardTag bt SET bt.deleted = true, bt.description = :reason WHERE bt.tag.id = :tagId AND bt.deleted = false")
    void softDeleteAllByTagId(@Param("tagId") Long tagId, @Param("reason") String reason);


    // -----------------------------------------------------
    //  Hard Delete (정책 위반 시에만 사용)
    // -----------------------------------------------------

    /**
     * 게시글-태그 관계를 DB에서 물리적으로 삭제합니다.
     * (Soft Delete 정책을 우회하며, 관리자용 긴급 청소 목적으로만 사용 권장)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM BoardTag bt WHERE bt.board.id = :boardId AND bt.tag.id = :tagId")
    void hardDeleteRelation(@Param("boardId") Long boardId, @Param("tagId") Long tagId);
}
