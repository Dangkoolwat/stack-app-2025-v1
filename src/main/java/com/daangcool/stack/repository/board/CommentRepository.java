package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Comment} entity.
 * Hibernate @Filter(softDeleteFilter) 정책 덕분에 기본 조회에서 삭제된 댓글은 자동 제외됩니다.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    // -----------------------------------------------------
    //  기본 조회 (Query Method 및 softDeleteFilter에 의존)
    // -----------------------------------------------------

    /**
     * 특정 게시글(Board) ID 기준으로 댓글 전체 조회
     * Query Method로 단순화, 작성자(user) 정보 Eager Loading
     */
    @EntityGraph(attributePaths = {"user"})
    List<Comment> findAllByBoard_IdOrderByIdAsc(Long boardId);

    /**
     * 페이징 댓글 조회 (Board 기준)
     * Query Method로 단순화, 작성자(user) 정보 Eager Loading
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByBoard_Id(Long boardId, Pageable pageable);

    /**
     * 단건 상세 조회 (Board/User 포함)
     * JpaRepository의 기본 findById를 오버라이드하여 EntityGraph를 적용합니다.
     */
    @Override
    @EntityGraph(attributePaths = {"board", "user"})
    Optional<Comment> findById(Long id);

    /**
     * 사용자별 작성 댓글 조회
     * Query Method로 단순화, 게시글(board) 정보 Eager Loading
     */
    @EntityGraph(attributePaths = {"board"})
    List<Comment> findAllByUser_IdOrderByIdDesc(Long userId);


    // -----------------------------------------------------
    //  검색 기능
    // -----------------------------------------------------

    /**
     * 댓글 내용 검색 (대소문자 무시)
     * 기본 소프트 삭제 필터가 적용되므로 c.deleted = false 조건은 제거했습니다.
     */
    @EntityGraph(attributePaths = {"user", "board"})
    @Query("""
        SELECT c FROM Comment c
        WHERE c.content LIKE CONCAT('%', :keyword, '%')
        ORDER BY c.id DESC
    """)
    Page<Comment> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    // -----------------------------------------------------

    //  통계 / 카운트 (softDeleteFilter에 의존)
    // -----------------------------------------------------

    /**
     * 게시글(Board)별 댓글 수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

    /**
     * 사용자별 댓글 수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 전체 댓글 수 (Soft Delete 제외)
     */
    @Query("SELECT COUNT(c) FROM Comment c")
    long countActiveComments();

    // -----------------------------------------------------
    //  Soft Delete / 상태 변경
    // -----------------------------------------------------

    /**
     * 댓글 논리 삭제
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.deleted = true, c.description = :reason WHERE c.id = :id")
    int softDelete(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 특정 게시글의 모든 댓글을 논리 삭제합니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.deleted = true, c.description = :reason WHERE c.board.id = :boardId AND c.deleted = false")
    void softDeleteAllByBoardId(@Param("boardId") Long boardId, @Param("reason") String reason);

    /**
     * 대댓글 수(replyCount) 갱신
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + :delta WHERE c.id = :id")
    void updateReplyCount(@Param("id") Long id, @Param("delta") Long delta);

    /**
     * 좋아요(별점) 증가
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.star = c.star + 1 WHERE c.id = :id")
    void increaseStar(@Param("id") Long id);

    /**
     * 삭제 포함 전체 댓글을 조회합니다. (관리자용)
     * {@link com.daangcool.stack.service.softdelete.IncludeDeleted} 스코프에서 호출해야 합니다.
     */
    @Query("SELECT c FROM Comment c ORDER BY c.id DESC")
    List<Comment> findAllWithDeleted();

    /**
     * 고아 댓글 일괄 조회 (삭제 후 24시간 경과 - lastModifiedDate 기준)
     */
    @Query("SELECT c FROM Comment c WHERE c.deleted = true AND c.lastModifiedDate <= :threshold ORDER BY c.id DESC")
    List<Comment> findAllOrphanComments(@Param("threshold") Instant threshold);

    /** 다건 물리 삭제 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}
