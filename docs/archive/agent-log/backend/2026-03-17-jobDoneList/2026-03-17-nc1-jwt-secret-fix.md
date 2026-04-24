# 2026-03-17 NC-1 JWT Secret Fallback 분리

## 기본 정보

- Date: 2026-03-17
- Agent: Antigravity
- Task Title: NC-1 JWT Secret prod fallback 제거 및 dev/prod 시크릿 완전 분리
- Goal: 2차 점검 보고서 NC-1 항목 즉시 조치 — prod JWT 시크릿이 환경변수 없이도 동작하는 문제 차단.

## Context

- 2차 보고서(`2026-03-17-second-review-report.md`)의 NC-1 항목 대응.
- 기존 `application-dev.yml`, `application-prod.yml` 모두 `${JWT_SECRET:N2U2YTUwODQ2MjI5  }` 동일 fallback 값 사용 (trailing space 포함).
- JWT_SECRET 환경변수 미주입 시 dev 시크릿이 prod에서 그대로 사용되는 보안 결함.

## Work Performed

1. `application-prod.yml` L177 수정:
   - `${JWT_SECRET:N2U2YTUwODQ2MjI5  }` → `${JWT_SECRET}` (fallback 완전 제거)
   - `JWT_SECRET` 미주입 시 Spring Boot가 `IllegalArgumentException`으로 시작을 즉시 거부합니다.
   - 시크릿 생성 방법(`openssl rand -base64 64`) 주석 추가.

2. `application-dev.yml` L155 수정:
   - `${JWT_SECRET:N2U2YTUwODQ2MjI5  }` → `${JWT_SECRET:AAA...AAA}` (dev 전용 fallback)
   - prod와 완전히 다른 값으로 교체하여 혼용 불가.
   - trailing space 제거.

## Files Modified

- `src/main/resources/config/application-prod.yml` — L177: JWT Secret fallback 제거
- `src/main/resources/config/application-dev.yml` — L155: dev 전용 fallback으로 교체

## Architecture Impact

- No architectural changes.

## Security Impact

- High Positive Impact:
  - 운영 배포 시 `JWT_SECRET` 환경변수 미주입이면 앱 시작 자체가 실패 → 취약 상태로 운영되는 시나리오 원천 차단.
  - dev/prod가 동일 시크릿으로 동작했던 위험 제거.
  - dev 시크릿으로 발급된 JWT가 prod에서 검증 통과되는 공격 벡터 삭제.

## Verification

- 코드 변경 정적 검토 완료.
- `./mvnw test` 실행 시 dev 프로파일 fallback이 있어 정상 통과 예상.
- 운영 배포 시 `JWT_SECRET` 환경변수 주입 여부를 CI/CD 파이프라인 사전 검증 게이트에 추가 권장.

## Risks

- 운영 서버에 `JWT_SECRET` 환경변수가 이미 올바르게 설정되어 있다면 영향 없음.
- 만약 환경변수가 없었다면 이번 배포 이후 앱이 시작되지 않음 → 이는 의도된 안전한 실패(fail-safe)

## Next Suggested Tasks

- NC-2 즉시 조치: `EmailOtpService.recordLog()` → `OtpLogService` 빈 분리
- CI/CD Script에 `JWT_SECRET` 환경변수 존재 여부 사전 검증 단계 추가
- `openssl rand -base64 64` 로 강력한 새 시크릿 생성 후 운영 환경변수에 주입

## Notes for Future Agents

- prod `JWT_SECRET`은 최소 512비트(base64 인코딩 시 약 88자) 이상의 랜덤 값 권장.
- JWT 시크릿 교체 시 기존 토큰은 즉시 무효화되므로 사용자 로그아웃 처리 필요.
- dev fallback(모두 'A' 반복) 는 base64로 유효한 값이며 개발 환경 인증 테스트에 사용 가능.
