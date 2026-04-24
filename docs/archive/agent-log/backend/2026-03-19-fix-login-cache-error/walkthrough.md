# Fix Login Cache Error — Walkthrough

## 문제
`admin@localhost` 로그인 시 Redis 캐시 관련 오류 — 두 가지 계층의 문제가 복합적으로 발생.

## 근본 원인

### 1. Instant 직렬화 실패
Redisson `JsonJacksonCodec`의 기본 `ObjectMapper`에 `JavaTimeModule`이 없어, Hibernate L2 캐시의 `StandardCacheEntryImpl` 내 `java.time.Instant` 직렬화 실패.

### 2. @JsonIgnore로 인한 비밀번호 캐시 누락 (핵심)
`User.password`에 `@JsonIgnore` 어노테이션 → Spring `@Cacheable` → Redisson → Jackson 직렬화 시 비밀번호 제외 → 캐시 히트 시 `null` 반환 → `BadCredentialsException`.

## 수정 내용

### CacheConfiguration.java
- `ObjectMapper.findAndRegisterModules()`: JavaTimeModule 등록
- Custom `AnnotationIntrospector`: `@JsonIgnore` 비활성화 → 캐시에 모든 필드 포함

### DomainUserDetailsService.java
- 디버그 코드 제거, 원본 복원

## 검증
- 컴파일 성공
- Redis FLUSHALL 후 로그인 성공 확인
- 영구 수정 적용 완료
