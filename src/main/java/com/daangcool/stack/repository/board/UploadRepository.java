package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.Upload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Spring Data JPA repository for the {@link Upload} entity (범용 파일 메타데이터).
 * @SQLRestriction("is_deleted = 0") 정책 덕분에 모든 조회에서 삭제된 파일은 자동 제외됩니다.
 */
@Repository
public interface UploadRepository extends JpaRepository<Upload, Long>, JpaSpecificationExecutor<Upload> {

    // -----------------------------------------------------
    // 🔹 기본 조회 (Soft Delete 자동 적용)
    // -----------------------------------------------------

    /**
     * 게시글(Board) ID 기준으로 첨부파일 전체 조회
     */
    @EntityGraph(attributePaths = {"board"})
    List<Upload> findAllByBoard_IdOrderByIdAsc(Long boardId);

    /**
     * 단건 파일 상세 조회 (Board 정보 Eager Loading)
     */
    @Override
    @EntityGraph(attributePaths = {"board"})
    Optional<Upload> findById(Long id);

    /**
     * 파일 원본명(sourceFilename)으로 조회
     */
    Optional<Upload> findBySourceFilename(String sourceFilename);

    /**
     * 게시글 ID 기준 페이징 조회
     */
    @EntityGraph(attributePaths = {"board"})
    Page<Upload> findAllByBoard_Id(Long boardId, Pageable pageable);


    // -----------------------------------------------------
    // 🔹 검색 기능 (파일명, 확장자)
    // -----------------------------------------------------

    /**
     * 파일명 또는 확장자 검색 (대소문자 무시)
     * @SQLRestriction이 적용되므로 삭제 조건은 필요 없습니다.
     */
    @EntityGraph(attributePaths = {"board"})
    @Query("""
        SELECT u FROM Upload u
        WHERE LOWER(u.sourceFilename) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.fileExtension) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY u.id DESC
    """)
    Page<Upload> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    // -----------------------------------------------------
    // 🔹 Soft Delete / 상태 변경
    // -----------------------------------------------------

    /**
     * 파일 논리 삭제 (직접 UPDATE 실행)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Upload u SET u.deleted = true, u.description = :reason WHERE u.id = :id")
    int softDelete(@Param("id") Long id, @Param("reason") String reason);

    /**
     * 다운로드 횟수 증가 (직접 UPDATE 실행)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Upload u SET u.downloadCount = u.downloadCount + 1 WHERE u.id = :id")
    void increaseDownloadCount(@Param("id") Long id);


    // -----------------------------------------------------
    // 🔹 통계 / 파일 관리 (@SQLRestriction에 의존)
    // -----------------------------------------------------

    /**
     * 게시글별 첨부파일 개수 (활성 상태만 카운트)
     */
    @Query("SELECT COUNT(u) FROM Upload u WHERE u.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

    /**
     * 전체 첨부파일 수 (활성 상태만 카운트)
     */
    @Query("SELECT COUNT(u) FROM Upload u")
    long countActiveFiles();

    /**
     * 확장자별 파일 개수 통계
     * GROUP BY 쿼리에도 @SQLRestriction이 적용되어 삭제된 파일은 제외됩니다.
     */
    @Query("""
        SELECT u.fileExtension, COUNT(u)
        FROM Upload u
        GROUP BY u.fileExtension
        ORDER BY COUNT(u) DESC
    """)
    List<Object[]> countFilesByExtension();
}
