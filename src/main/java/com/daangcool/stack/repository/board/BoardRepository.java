package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.Board;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Board} entity.
 * Board 엔티티에 적용된 @SQLRestriction("is_deleted = 0") 덕분에
 * 모든 조회(SELECT) 쿼리는 Soft Delete 조건이 자동으로 적용됩니다.
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {

    // -----------------------------------------------------
    //  기본 조회 (Soft Delete 조건은 @SQLRestriction이 자동 추가)
    // -----------------------------------------------------

    /**
     * 게시글 전체 조회 (Soft Delete 자동 적용)
     * @Query는 정렬 순서를 보장하기 위해 사용합니다.
     */
    @Override
    @EntityGraph(attributePaths = {"user", "boardType"})
    @Query("SELECT b FROM Board b ORDER BY b.id DESC")
    List<Board> findAll();

    /**
     * 페이징 조회 (Soft Delete 자동 적용)
     */
    @EntityGraph(attributePaths = {"user", "boardType"})
    @Query("SELECT b FROM Board b ORDER BY b.id DESC")
    Page<Board> findAllActive(Pageable pageable);

    /**
     * 단건 상세 조회
     * (삭제 조건 제거됨: @SQLRestriction이 findById에도 적용됨)
     */
    @EntityGraph(attributePaths = {"user", "boardType"})
    Optional<Board> findById(Long id);

    /**
     * 작성자별 게시글 조회 (삭제 조건 제거됨)
     */
    @EntityGraph(attributePaths = {"boardType"})
    List<Board> findByUser_Id(Long userId);

    /**
     * 게시판 유형별 조회 (삭제 조건 제거됨)
     */
    @EntityGraph(attributePaths = {"user"})
    List<Board> findByBoardType_Code(String boardTypeCode);

    /**
     * 공지글 목록 (is_notice = true)
     */
    @Cacheable("board-notice-cache")
    @EntityGraph(attributePaths = {"user"})
    // @SQLRestriction이 있으므로 b.deleted = false 조건은 제거함.
    @Query("SELECT b FROM Board b WHERE b.notice = true ORDER BY b.id DESC")
    List<Board> findAllNotices();

    // -----------------------------------------------------
    // 🔹 검색 / 통계용 커스텀 쿼리
    // -----------------------------------------------------

    /**
     * 제목 + 내용 검색
     * (삭제 조건은 @SQLRestriction이 자동 추가)
     */
    @EntityGraph(attributePaths = {"user", "boardType"})
    @Query("SELECT b FROM Board b " +
        "WHERE b.title LIKE CONCAT('%', :keyword, '%') " +
        "   OR b.content LIKE CONCAT('%', :keyword, '%') " +
        "ORDER BY b.id DESC")
    Page<Board> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 게시판 총 개수 (Soft Delete 자동 적용)
     * @SQLRestriction가 적용되므로 WHERE b.deleted = false 조건은 제거함.
     */
    @Query("SELECT COUNT(b) FROM Board b")
    long countActiveBoards();

    /**
     * 특정 사용자의 게시글 개수 (Soft Delete 자동 적용)
     * @SQLRestriction가 적용되므로 AND b.deleted = false 조건은 제거함.
     */
    @Query("SELECT COUNT(b) FROM Board b WHERE b.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // -----------------------------------------------------
    // 🔹 Soft Delete 처리 및 업데이트
    // -----------------------------------------------------

    /**
     * 게시글 논리 삭제 (직접 UPDATE 실행)
     * Auditing 필드(lastModifiedBy, lastModifiedDate)는 이 쿼리 실행 후 자동으로 업데이트됩니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Board b SET b.deleted = true, b.description = :reason WHERE b.id = :id")
    int softDelete(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 조회수 증가 (직접 UPDATE 실행)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void increaseViewCount(@Param("id") Long id);
}
