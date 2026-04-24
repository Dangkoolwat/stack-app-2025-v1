# Walkthrough (Korean)

## 1. 구현 흐름
1. 아키텍처 위반 해결: `UserAuthCacheService`를 `security` 패키지로 이동하여 레이어 간 의존성 규칙 준수.
2. 캐시 데이터 호환성: Jackson 3가 Redis에 클래스 정보를 올바르게 저장할 수 있도록 DTO를 `record`에서 `class`로 변경.
3. 인증 로직 안정화: `DomainUserDetailsService`에서 이메일과 로그인을 순차적으로 조회하는 Fallback 전략을 구현하여 식별 오류 방지.
4. 테스트 데이터 보강: 통합 테스트 시 사용자에 권한을 부여하지 않아 발생하던 권한 캐시 오류 해결.

## 2. 핵심 포인트
- `login.matches(Constants.LOGIN_REGEX)`: 정규식 비교 방향 오류 수정.
- `Duration.ofMinutes()`: 최신 API 사용으로 경고 제거.
- `UserAuthCacheDto`: `@Data`, `@NoArgsConstructor` 조합으로 Redis 호환성 극대화.
