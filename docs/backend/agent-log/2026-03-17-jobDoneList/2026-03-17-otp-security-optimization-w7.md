# 2026-03-17-otp-security-optimization-w7.md

- Date: 2026-03-17
- Agent: Antigravity
- Task Title: OTP 보안 최적화 및 레거시 제거 (W-7)
- Goal: DB 평문 OTP 저장을 제거하고 Redis 기반 TTL 관리 및 보안 로깅 체계로 통합.

## Context
- 보안 보고서(`2026-03-15-system-security-optimization-report.md`)의 W-7 항목(High Priority) 대응.
- 기존에 혼재되어 있던 DB 기반(`EmailOtpManager`)과 Redis 기반(`EmailOtpService`) 로직을 일원화할 필요가 있었음.

## Work Performed
- **엔티티 수정**: `User.java`에서 `otpCode`, `otpExpireDate` 필드 제거.
- **서비스 통합**:
    - `EmailOtpService`에 `RedissonClient` 및 `EmailOtpLogRepository` 주입.
    - `RLock`을 사용한 동일 이메일 중복 요청 방지(분산 락) 로직 추가.
    - 인증 시도(요청/검증 성공/실패)에 대한 `EmailOtpLog` 기록 로직 통합.
    - 모든 OTP 데이터는 `EmailOtpCacheService`를 통해 Redis TTL(5분)로 관리되도록 일원화.
- **컨트롤러 업데이트**: `EmailOtpResource.java`에서 `HttpServletRequest`를 통해 IP/UA를 추출하여 서비스에 전달하도록 수정.
- **스키마 정리**: Liquibase `20260317100000_drop_user_otp_columns.xml`을 생성하여 DB 컬럼 삭제 예약 및 `master.xml` 등록.
- **레거시 제거**: 사용되지 않는 `EmailOtpManager.java` 분석 완료 (사용자 요청에 따라 물리적 삭제는 보류 가능하나, 코드상에서는 참조 제거됨).

## Files Modified
- `src/main/java/com/daangcool/stack/domain/User.java`
- `src/main/java/com/daangcool/stack/service/otp/EmailOtpService.java`
- `src/main/java/com/daangcool/stack/web/rest/EmailOtpResource.java`
- `src/main/resources/config/liquibase/changelog/20260317100000_drop_user_otp_columns.xml` [NEW]
- `src/main/resources/config/liquibase/master.xml`

## Architecture Impact
- OTP 라이프사이클이 DB에서 Redis로 완전히 이전됨에 따라 DB I/O 감소 및 보안성 향상.
- 중복 요청 방지 및 감사 로그 구성을 통해 인증 보안 강화.

## Security Impact
- **Data Privacy**: DB 탈취 시에도 OTP 정보 유출 원천 차단.
- **Brute Force Protection**: Redis TTL과 실패 카운트(`EmailOtpCacheService`)를 통한 보호 계층 강화.
- **Auditing**: 모든 OTP 활동이 `stack_email_otp_log`에 투명하게 기록됨.

## Verification
- 코드 레벨 정적 분석 및 DTO 정합성 확인.
- `walkthrough.md` 및 `task.md` 업데이트 완료.

## Next Suggested Tasks
- **보안 보고서 잔여 항목 검토**: `W-10` (민감 정보 로깅 여부 전수 조사) 등.
- **프론트엔드 연동 확인**: OTP 필드 제거로직이 프론트엔드 API 호출 결과(DTO)에 영향을 주지 않는지 최종 확인 (현재 DTO는 영향 없음).
