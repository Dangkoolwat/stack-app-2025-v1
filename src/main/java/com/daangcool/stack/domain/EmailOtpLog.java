package com.daangcool.stack.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "stack_email_otp_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EmailOtpLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqEmailOtpLogGenerator")
    @SequenceGenerator(name = "seqEmailOtpLogGenerator", sequenceName = "stack_email_otp_log_sequence_generator", allocationSize = 1)

    private Long id;

    /**
     * 이메일 주소
     */
    @Column(name = "email", nullable = false, length = 191)
    private String email;

    /**
     * OTP 코드 (6자리)
     */
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    /**
     * 요청자 IP
     */
    @Column(name = "request_ip", length = 45)
    private String requestIp;

    /**
     * 요청 브라우저 및 플랫폼 정보
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * 클라이언트 기기 유형 (예: mobile, desktop, tablet)
     */
    @Column(name = "device_type", length = 100)
    private String deviceType;

    /**
     * 요청 시각
     */
    @Column(name = "created_date", nullable = false)
    private Instant createdDate = Instant.now();

    /**
     * OTP 요청/검증 결과 상태
     * SENT / VERIFIED / FAILED / EXPIRED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 연결된 사용자 (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
