# Spring Boot 4.0 마이그레이션 검토 — 완료 작업 종합 요약

프로젝트: `stack-app-2025-v1`  
기간: 2026-03-14  
작업자: Antigravity (Google DeepMind)

---

## 개요

SB4 마이그레이션 검토 보고서(`2026-03-14-sb4-review-report.md`) 기준으로  
요청된 항목들을 순차적으로 코드 수정 완료한 내역을 기록합니다.

---

## 완료 목록

###  L-3. encodeFilename User-Agent 분기 제거 (RFC 5987 단일화)

상태: 완료  
파일: `UploadResource.java`  
내용:
- MSIE/Trident 분기 로직 제거
- `encodeFilename()`, `setDispositionHeader()` 헬퍼를 RFC 5987 단일 방식으로 단순화
- `HttpServletRequest` 파라미터 및 unused import 제거

---

###  H-3. 파일 다운로드 StreamingResponseBody 전환

상태: 완료  
파일:
- `StorageService.java` — `loadAsStream(String)` 추상 메서드 추가; `loadAsResource()` default 구현으로 위임 (하위 호환 유지)
- `LocalDefaultFileStorageService.java` — `loadAsStream()` 구현 (`UrlResource.getInputStream()`)
- `ShareFileStorageService.java` — 동일
- `S3FileStorageService.java` — `loadAsStream()` 구현 (`S3Client.getObject()` → `ResponseInputStream`)
- `OciFileStorageService.java` — `loadAsStream()` 구현 (`GetObjectResponse.getInputStream()`)
- `UploadResource.java` — 3개 엔드포인트 `ResponseEntity<byte[]>` → `ResponseEntity<StreamingResponseBody>` 전환

효과: 대용량 파일 다운로드 시 JVM 힙 전체 로드 방지 (OOM 위험 제거)

---

###  H-4. JWT URI 쿼리 파라미터 비활성화

상태: 완료  
파일: `SecurityJwtConfiguration.java`  
내용:

```java
// 변경 전
bearerTokenResolver.setAllowUriQueryParameter(true);

// 변경 후
bearerTokenResolver.setAllowUriQueryParameter(false);
```

주의: 기존에 URL에 `?access_token=...` 방식으로 JWT를 전달하는 클라이언트가 있다면  
`Authorization: Bearer` 헤더 방식으로 전환 필요.

---

###  M-2. CSP script-src unsafe-inline 제거

상태: 완료  
파일: `application.yml`  
내용:

```yaml
# 변경 전
script-src 'self' 'unsafe-inline' https://storage.googleapis.com

# 변경 후
script-src 'self' https://storage.googleapis.com
```

참고: `style-src 'unsafe-inline'`은 Vue SFC 런타임 주입 필요로 유지.  
완전 제거 시 프론트엔드에서 nonce 기반 CSP 구성 필요.

---

###  H-1 + M-5. CacheConfiguration 재점검 및 개선

상태: 완료  
파일: `CacheConfiguration.java`  
내용:

| 항목 | 내용 |
|------|------|
| H-1 (이중 생성) | 이전 세션 적용 확인 — `jcacheConfiguration`이 `redissonClient` 빈 재사용 |
| M-5 (TTL 세분화) | `buildTTLConfig()` → `longTtlCacheConfiguration` Bean으로 활성화 (24시간 TTL) |
| 버그 수정 | `Board.boardTags` 중복 캐시 등록 제거 |

TTL 분류:

| TTL | 대상 캐시 |
|-----|-----------|
| 24시간 | Settings, CommonCode(Group/Detail), Tag(ALL/BY_ID/PREFIX) |
| 1시간 (기본) | User, Board, Comment, Upload, BoardTag, Tag(POPULAR), EmailOtpLog |

---

###  테스트 파일 업데이트

상태: 완료  
파일:
- `S3FileStorageServiceT.java` — `getObjectAsBytes` mock → `thenAnswer + ByteArrayInputStream` 방식  
  (H-3 인터페이스 변경에 따른 테스트 정합성 유지)
- `OciFileStorageServiceT.java` — `loadAsResource()` → `loadAsStream()` 기반 테스트로 교체  
  (try-with-resources InputStream 검증)

---

###  Import 정리

상태: 완료  
파일:
- `LocalDefaultFileStorageService.java` — 사용하지 않는 `MalformedURLException` import 제거
- `S3FileStorageServiceT.java` — 사용하지 않는 `GetObjectResponse`, `InputStream` import 정리
- `OciFileStorageServiceT.java` — `InputStream` import 추가 (try-with-resources)

---

## 미완료 / 미착수 항목

| 항목 | 이유 |
|------|------|
| C-1 JWT Secret 환경변수화 | 운영 배포 환경과 연계 필요 — 수동 적용 권장 |
| C-2 DB 자격증명 외부화 | CI/CD 파이프라인 연동 필요 — 수동 적용 권장 |
| C-3 `ddl-auto: none` | 수동 확인 후 적용 권장 (스키마 영향도 검토 필요) |
| H-2 AsyncConfiguration 가상 스레드 | 별도 요청 없어 미착수 |
| M-1 HikariCP 운영 풀 설정 | 운영 DB 성능 요구사항 확인 후 적용 권장 |
| M-3 Prometheus 활성화 | 모니터링 인프라 연동 필요 |
| M-4 EmailValidator 내부 클래스 | 별도 요청 없어 미착수 |
| L-1 로깅 레벨 조정 | 별도 요청 없어 미착수 |
| L-2 allow-bean-definition-overriding | 원인 빈 파악 후 적용 권장 |
| L-4 오류 응답 ProblemDetail 통일 | H-3 수정 시 부분 개선됨, 전체 통일은 미착수 |
| L-5 setPassword() deprecated | Redisson 버전 업그레이드 시 대응 권장 |

---

## Deprecation Warning 분석

별도 분석 문서 작성 완료:  
[`docs/decisions/2026-03-14-deprecation-warning-analysis.md`](../decisions/2026-03-14-deprecation-warning-analysis.md)

| 파일 | deprecated 항목 | 수정 방안 |
|------|-----------------|-----------|
| `TestUtil.java` | `MediaType` Charset 생성자 | `MediaType.APPLICATION_JSON` 상수로 교체 |
| `TestUtil.java` | `spring.cglib.Enhancer` | Spring `ProxyFactory`로 교체 (우선 대응 권장) |
| `InlineObject.java` (자동생성) | `spring.lang.Nullable` | openapi-generator `<useJakartaEe>true</useJakartaEe>` 옵션 추가 |

---

## 검증 권장 명령

```bash
# 컴파일 검증
./mvnw compile

# 전체 테스트
./mvnw test

# 스토리지 서비스 단위 테스트
./mvnw test -Dtest=S3FileStorageServiceT,OciFileStorageServiceT

# Deprecation 상세 출력
./mvnw compile -Xlint:deprecation 2>&1 | grep -i deprecated
```
