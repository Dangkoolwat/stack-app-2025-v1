package com.daangcool.stack.web.rest;


import com.daangcool.stack.service.dto.OtpLoginResponseDTO;
import com.daangcool.stack.service.otp.EmailOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling email-based OTP authentication requests.
 *
 * <p>
 * This controller provides endpoints for requesting and verifying
 * one-time passwords (OTP) sent to a user's registered email address.
 * </p>
 *
 * <ul>
 *   <li><b>/api/auth/email/request</b> — Request an OTP via email.</li>
 *   <li><b>/api/auth/email/verify</b> — Verify the OTP and issue a JWT token.</li>
 * </ul>
 *
 * <p>
 * All endpoints under <code>/api/auth/email/**</code> are publicly accessible
 * (configured as <code>permitAll()</code> in {@link com.daangcool.stack.config.SecurityConfiguration}).
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * POST /api/auth/email/request?email=user@example.com
 * → 200 OK
 *
 * POST /api/auth/email/verify?email=user@example.com&code=123456
 * → 200 OK (returns JWT token)
 * </pre>
 * </p>
 *
 * @author Steve
 * @since 2025-10-15
 */
@RestController
@RequestMapping("/api/auth/email")
@Tag(name = "Email OTP Authentication", description = "이메일 기반 OTP 인증 및 JWT 발급 API")
public class EmailOtpResource {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpResource.class);

    private final EmailOtpService emailOtpService;
    private final AuthenticateController authenticateController;

    public EmailOtpResource(EmailOtpService emailOtpService, AuthenticateController authenticateController) {
        this.emailOtpService = emailOtpService;
        this.authenticateController = authenticateController;
    }

    /**
     * {@code POST  /api/auth/email/request} :
     * Request a one-time password (OTP) via email.
     *
     * <p>This endpoint triggers generation of a 6-digit OTP and sends it
     * to the user's registered email address. The OTP is valid for 5 minutes
     * (configured in {@link com.daangcool.stack.service.otp.EmailOtpCacheService}).</p>
     *
     * <p>The OTP value itself is never returned in the API response for security reasons.</p>
     *
     * <p><b>Security:</b> This endpoint is open to unauthenticated users (public access).</p>
     *
     * @param email The target email address to which the OTP should be sent.
     * @return {@link ResponseEntity} with status {@code 200 (OK)} if the OTP email was sent successfully.
     *         Returns {@code 400 (Bad Request)} if the email format is invalid.
     *
     * @see com.daangcool.stack.service.otp.EmailOtpService#requestOtp(String)
     * @see com.daangcool.stack.service.MailService#sendEmailOtp(com.daangcool.stack.domain.User, String)
     */
    @Operation(
        summary = "이메일 OTP 요청",
        description = "입력된 이메일 주소로 인증번호를 발송합니다. OTP는 5분간 유효하며, 메일 본문에 안내됩니다."
    )
    @PostMapping("/request")
    public ResponseEntity<Void> requestOtp(
        @RequestParam("email") @Email String email,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        log.debug("[OTP] requestOtp() called for {}", email);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        emailOtpService.requestOtp(email, ip, userAgent);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code POST  /api/auth/email/verify} :
     * Verify the provided OTP code and issue a JWT token if successful.
     *
     * <p>
     * This endpoint validates the OTP previously sent to the user's email.
     * If the OTP is valid and not expired, it generates a JWT token using
     * the same logic as {@link AuthenticateController#createToken(Authentication, boolean)}.
     * </p>
     *
     * <ul>
     *   <li>On success: Returns {@code 200 OK} with JWT in both header and body.</li>
     *   <li>On failure: Returns {@code 401 Unauthorized}.</li>
     * </ul>
     *
     * <p><b>Security:</b> This endpoint is open to unauthenticated users (public access).</p>
     *
     * <p>Example Response:</p>
     * <pre>
     * {
     *   "jwt": "eyJhbGciOiJIUzI1NiJ9...",
     *   "login": "user@example.com",
     *   "email": "user@example.com",
     *   "name": "Steve"
     * }
     * </pre>
     *
     * @param email User's registered email address.
     * @param code  6-digit OTP code received via email.
     * @return {@link ResponseEntity} containing the JWT token and user details if verification succeeds.
     *
     * @see com.daangcool.stack.service.otp.EmailOtpService#verifyOtp(String, String)
     * @see com.daangcool.stack.web.rest.AuthenticateController#createToken(Authentication, boolean)
     */
    @Operation(
        summary = "이메일 OTP 검증 및 JWT 발급",
        description = "입력된 OTP 코드를 검증한 후, 인증 성공 시 JWT 토큰을 발급합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<OtpLoginResponseDTO> verifyOtp(
        @RequestParam("email") @NotBlank @Email String email,
        @RequestParam("code") @NotBlank String code
    ) {
        log.debug("[OTP] verify request for email: {}", email);

        var userOpt = emailOtpService.verifyOtp(email, code);
        if (userOpt.isEmpty()) {
            log.warn("[OTP] 인증 실패 (invalid or expired code): {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userOpt.get();

        var authorities = user.getAuthorities().stream()
            .map(auth -> new SimpleGrantedAuthority(auth.getName()))
            .toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getLogin(),
            null,
            authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = authenticateController.createToken(authentication, false);
        log.info("[OTP] 인증 성공 → JWT 발급 완료: {}", user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);

        var response = new OtpLoginResponseDTO(jwt, user.getLogin(), user.getEmail(), user.getFirstName());
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}

