# 2026-03-17 Spring Boot 4 2차 전체 점검

## 기본 정보

- Date: 2026-03-17
- Agent: Antigravity
- Task Title: Spring Boot 4 최신 가이드 기준 2차 전체 점검 및 리포트 작성
- Goal: 1차 점검 이후 조치 현황을 확인하고, 신규 이슈(성능, 보안, 유지보수, 중복 리소스)를 발견하여 2차 보고서 파일로 저장.

## Context

- 1차 점검 보고서: `docs/agent-log/2026-03-14-sb4-review-report.md`
- 그간 다수의 개선 작업이 완료됨:
  - Redis SSOT 정리 (`2026-03-17-redis-ssot-single-client.md`)
  - OTP 보안 최적화 (`2026-03-17-otp-security-optimization-w7.md`)
  - Redis 모니터링 구현 (`2026-03-17-redis-connection-audit.md`)
  - 파일 업로드 보안 (`2026-03-17-file-upload-security-*`)
  - Rate Limiting Phase3 (`2026-03-16-rate-limiting-phase3-redis.md`)

## Work Performed

1. 1차 보고서의 모든 항목 조치 현황 재검토 (14개 항목 → 12개 완료 확인)
2. 소스코드 전수 검토 대상:
   - `CacheConfiguration.java`, `AsyncConfiguration.java`, `SecurityConfiguration.java`
   - `SecurityJwtConfiguration.java`, `RateLimitingConfiguration.java`
   - `RedisMonitoringConfiguration.java`, `WebConfigurer.java`, `FileResourceConfiguration.java`
   - `ApplicationProperties.java`, `EmailOtpService.java`, `LoggingAspect.java`
   - `UserService.java`, `UploadResource.java`, `DomainUserDetailsService.java`
   - `application.yml`, `application-dev.yml`, `application-prod.yml`
3. 신규 이슈 17개 발견 및 분류 (Critical 2 / High 4 / Medium 6 / Low 5)
4. 보고서 작성 및 저장: `docs/agent-log/2026-03-17-second-review-report.md`

## Files Modified

- `docs/agent-log/2026-03-17-second-review-report.md` [NEW] — 2차 점검 보고서
- `docs/agent-log/2026-03-17-second-review-task-log.md` [NEW] — 본 작업 로그

## Architecture Impact

- 코드 변경 없음. 분석 및 보고서 작성만 수행.

## Security Impact

- 코드 변경 없음. 보고서에서 Critical 2개 보안 이슈 발견:
  - NC-1: JWT Secret 동일 fallback 잔존
  - NC-2: OtpLog self-invocation으로 `REQUIRES_NEW` 무효화

## Verification

- 파일 내용 직접 검토(정적 분석) 수행. 코드 변경 없으므로 빌드 검증 불필요.

## Risks

- NC-2 (self-invocation) 이슈는 현재 코드에서 OTP 감사 로그의 신뢰성에 문제를 일으킬 수 있음.
- NH-1 (Reflection 내부 API)은 Redisson 버전 업그레이드 시 런타임 오류 위험.

## Next Suggested Tasks

1. NC-2 즉시 조치: `OtpLogService` 별도 빈 분리
2. NC-1 조치: JWT prod fallback 제거
3. NH-4 조치: Prometheus 엔드포인트 접근 제한
4. NH-3 조치: OTP 코드 로그 마스킹 적용

## Notes for Future Agents

- `RateLimitingConfiguration`의 Reflection 방식은 Redisson 4.x 기준으로 동작 중. Redisson 업그레이드 전 반드시 NH-1 개선 완료 필요.
- `EmailOtpService.recordLog()`의 self-invocation 문제는 테스트에서 확인이 어렵고, 운영에서 조용히 감사 로그가 누락될 수 있음. 우선순위 높게 처리 권장.
