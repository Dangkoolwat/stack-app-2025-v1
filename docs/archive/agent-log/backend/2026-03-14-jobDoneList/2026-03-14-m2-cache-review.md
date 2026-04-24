# 2026-03-14 M-2 CSP 개선 및 CacheConfiguration 재점검

## 기본 정보

- Date: 2026-03-14
- Agent: Antigravity (Google DeepMind)
- Task Title: M-2 CSP unsafe-inline 제거 + CacheConfiguration 전면 재점검
- Goal: 보고서 M-2 항목 코드 적용, CacheConfiguration 재점검 및 미적용 개선사항 활성화

## Context

- 이전 작업: L-3, H-3, H-4 적용 완료
- `CacheConfiguration.java`는 H-1(Redisson 이중 생성) 이미 수정됨 (이전 세션)
- `buildTTLConfig()` 메서드는 준비되어 있었으나 실제 Bean에 미사용(M-5)
- `Board.boardTags` 캐시가 L166, L177 두 곳에 중복 등록되는 버그 발견

## 수행 작업

### M-2: CSP script-src unsafe-inline 제거

- 파일: `application.yml` L227
- 변경: `script-src 'self' 'unsafe-inline' https://storage.googleapis.com` → `'unsafe-inline'` 제거
- 유지: `style-src 'unsafe-inline'` — Vue SFC 런타임 주입 필요 (주석으로 위험도 표기)
- 근거: Vite 빌드 결과물은 외부 JS 파일로 번들링되어 인라인 스크립트 불필요

### CacheConfiguration 재점검 결과 및 적용

1. M-5 해결: `buildTTLConfig()` → `longTtlCacheConfiguration()` Bean으로 활성화
   - `RedissonClient`를 인자로 받아 단일 Redisson 연결 재사용 (H-1 유지)
   - TTL: 24시간

2. 캐시 TTL 세분화 적용:

   | TTL | 캐시 그룹 |
   |-----|-----------|
   | 24시간 (longTTL) | Settings, CommonCode(Group/Detail), Tag(BY_ID/ALL/PREFIX) |
   | 1시간 (기본 jhipster) | User, Board, Comment, Upload, BoardTag, EmailOtpLog, Tag(POPULAR) |

3. 버그 수정: `Board.boardTags` 중복 등록 제거
   - 기존: L166(Board 섹션)과 L177(BoardTag 섹션)에 동일한 캐시 키 중복
   - 수정: Board 섹션에만 존재, BoardTag 섹션에서 중복 제거

4. 코드 정리: 주석 및 섹션 구분 개선, `getRedissonConfig()` 위치 재정렬

## 수정된 파일

| 파일 | 변경 내용 |
|------|-----------|
| `application.yml` | script-src에서 `'unsafe-inline'` 제거 |
| `CacheConfiguration.java` | longTtlCacheConfiguration Bean 추가, TTL 세분화, 중복 등록 버그 수정 |

## Architecture Impact

- `JCacheManagerCustomizer` Bean이 이제 2개의 `Configuration` 빈을 주입받음 (기본/장기)
- 새 Bean `longTtlCacheConfiguration` 추가 — 기존 캐시 설정 호환성 영향 없음

## Security Impact

- CSP script-src `'unsafe-inline'` 제거로 인라인 스크립트 기반 XSS 방어 강화

## Verification

```bash
./mvnw compile
./mvnw test
```
- 캐시 변경은 Redis 연결 환경에서만 통합 테스트 가능

## Risks

- `style-src 'unsafe-inline'` 미제거 — Vue SFC 프로젝트에서 제거 시 스타일 깨짐 가능
- `CACHE_TAG_POPULAR` TTL을 기본(1h)으로 분리했으나, 이전에는 24h로 동작하던 경우라면 Redis 서버에서 기존 캐시 수동 flush 필요
- `Board.boardTags` 중복 제거 후 기존에 두 번 clear()가 호출되던 동작이 한 번으로 줄어듦 — 영향 없음

## Next Suggested Tasks

1. H-1 완전 점검: `redissonClient` Bean 단일 생성 확인 (`./mvnw spring-boot:run`으로 Redis 연결 수 모니터링)
2. H-2: `AsyncConfiguration` 가상 스레드 대응
3. C-1/C-2/C-3: Critical 3건 (JWT Secret, DB자격증명, ddl-auto) — 환경변수 적용
