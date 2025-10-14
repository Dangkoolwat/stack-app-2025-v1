package com.daangcool.stack.repository.board;

import com.daangcool.stack.domain.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardAdminRepository extends JpaRepository<Board, Long> {

    @Query(value = "SELECT * FROM STACK_BOARD", nativeQuery = true)
    List<Board> findAllIncludingDeleted();

    @Query(value = "SELECT * FROM STACK_BOARD WHERE IS_DELETED = 1", nativeQuery = true)
    List<Board> findAllDeleted();

    @Query(value = "SELECT * FROM STACK_BOARD WHERE ID = :id", nativeQuery = true)
    Optional<Board> findByIdIncludingDeleted(@Param("id") Long id);

}
