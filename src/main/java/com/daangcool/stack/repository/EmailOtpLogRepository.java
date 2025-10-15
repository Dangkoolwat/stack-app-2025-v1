package com.daangcool.stack.repository;

import com.daangcool.stack.domain.EmailOtpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link EmailOtpLog} entity.
 *
 * <p>OTP 요청 및 검증 로그를 관리합니다.</p>
 *
 * <ul>
 *   <li>이메일별 최근 OTP 요청 내역 조회</li>
 *   <li>상태별 로그 필터링</li>
 *   <li>만료/삭제 관리 (필요 시)</li>
 * </ul>
 *
 * @author Steve
 * @since 2025-10-15
 */
@Repository
public interface EmailOtpLogRepository extends JpaRepository<EmailOtpLog, Long> {

    /**
     * 이메일 기준 최근 로그 목록 조회 (최신순)
     *
     * @param email 이메일 주소
     * @return 최근 로그 목록
     */
    @Query("SELECT e FROM EmailOtpLog e WHERE e.email = :email ORDER BY e.createdDate DESC")
    List<EmailOtpLog> findRecentByEmail(String email);

    /**
     * 이메일 + 상태 기준으로 최근 로그 1건 조회
     *
     * @param email 이메일
     * @param status 상태 코드 (예: SENT, VERIFIED, FAILED)
     * @return 일치하는 최근 로그 (Optional)
     */
    Optional<EmailOtpLog> findFirstByEmailAndStatusOrderByCreatedDateDesc(String email, String status);

    /**
     * 특정 상태 로그 삭제 (예: 오래된 EXPIRED 로그)
     *
     * @param status 상태
     */
    void deleteByStatus(String status);
}
