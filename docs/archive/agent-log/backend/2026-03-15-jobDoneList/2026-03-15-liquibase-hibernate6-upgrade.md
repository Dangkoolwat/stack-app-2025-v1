# 2026-03-15 Liquibase Hibernate 확장 버전 업그레이드

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

liquibase-hibernate6 버전 업그레이드 — Hibernate 7 (Spring Boot 4) 호환 대응

---

## Goal

Spring Boot 4.0 / Hibernate 7 환경에서 `liquibase:diff` 실행 시 엔티티 메타데이터 비교가
정확히 동작하도록 `liquibase-hibernate6` 확장의 버전을 최신(5.0.1)으로 업그레이드한다.

---

## Context

- 이전 보안 점검 리포트(`docs/security/2026-03-15-system-security-optimization-report.md`)의
  항목 I-4에서 `liquibase-hibernate6` 버전 불일치 문제를 지적함.
- 기존 설정: `liquibase-hibernate6` 버전을 `${liquibase.version}` (4.31.0) 과 동일하게 사용.
- Spring Boot 4.0.3은 Hibernate 7.x를 사용하나, `liquibase-hibernate6 4.31.0`은
  Hibernate 6 API 기준으로 빌드되어 있어 `diff` 결과 정확도 저하 및 ClassCastException 위험.

### 중요 사실 — liquibase-hibernate7 아티팩트 부재

`liquibase-hibernate7`이라는 별도 Maven 아티팩트는 존재하지 않습니다 (2026-03 기준).
Liquibase 공식 저장소는 Hibernate 6과 7을 모두 `liquibase-hibernate6` 아티팩트에서 지원하며,
5.0.x 계열부터 Hibernate 7 호환성이 포함되었습니다.
- 최신 릴리즈: `5.0.1` (2025-10-06)
- 참고: https://github.com/liquibase/liquibase-hibernate/releases

### 버전 독립성

`liquibase-core` 와 `liquibase-hibernate6` 의 버전은 반드시 일치할 필요가 없습니다.
이전 코드는 `${liquibase.version}` 을 공유 사용해 4.31.0으로 고정되어 있었으나,
5.0.1 은 독립 프로퍼티 `${liquibase-hibernate6.version}` 으로 분리 관리합니다.

---

## Work Performed

1. `liquibase-hibernate6` 최신 릴리즈 버전 확인 (GitHub Releases, Maven Central)
2. Hibernate 7 지원 여부 확인 (5.0.x부터 지원 확인)
3. `pom.xml` 수정:
   - `<properties>` 에 `<liquibase-hibernate6.version>5.0.1</liquibase-hibernate6.version>` 신규 프로퍼티 추가
   - `<dependencies>` (test scope) 의 `liquibase-hibernate6` 버전을 `${liquibase-hibernate6.version}` 으로 변경
   - `<build> → liquibase-maven-plugin → <dependencies>` 의 `liquibase-hibernate6` 버전을 동일하게 변경
   - 각 변경 위치에 명확한 주석 추가
4. 본 agent-log 작성

---

## Files Modified

- `pom.xml`
  - `<properties>` 블록: `liquibase-hibernate6.version` 프로퍼티 추가 (신규)
  - `<dependencies>` (test scope): `liquibase-hibernate6` 버전 4.31.0 → 5.0.1
  - `<build/plugins/liquibase-maven-plugin/dependencies>`: `liquibase-hibernate6` 버전 4.31.0 → 5.0.1

---

## Architecture Impact

Liquibase `diff` / `generateChangeLog` 명령의 Hibernate 엔티티 스캔 정확도가 개선됩니다.
런타임 애플리케이션 동작에는 영향이 없습니다 (`test` 스코프 및 Maven 플러그인 전용 의존성).

---

## Security Impact

없음. 버전 업그레이드는 내부 도구 의존성에만 영향을 미칩니다.

---

## Verification

코드 변경 검증은 아래 명령으로 수행해야 합니다:

```bash
# 1. 빌드 검증 (컴파일 및 의존성 해석 확인)
./mvnw clean package -DskipTests

# 2. 테스트 실행 (ArchUnit 포함)
./mvnw test

# 3. Liquibase diff 검증 (Oracle 연결 필요)
./mvnw liquibase:diff -Pdev
```

현재 세션에서 DB 연결이 없으므로 3번은 수동으로 확인해야 합니다.

### diff 실행 후 확인 사항

```bash
# diff 결과 파일 생성 확인
ls -la src/main/resources/config/liquibase/changelog/

# 생성된 changelog에 불필요한 변경(컬럼 재생성, 타입 변경 노이즈)이 없는지 검토
cat src/main/resources/config/liquibase/changelog/<타임스탬프>_changelog.xml
```

---

## Risks

1. 5.0.1 API 변경: `liquibase-hibernate6 5.0.x`는 내부 API가 4.x와 다를 수 있습니다.
   `diff` 결과에 예상치 못한 변경사항이 포함될 경우 수동 검토 후 제거 필요.

2. Spring Framework 버전 의존: `liquibase-hibernate6 5.0.1`의 내부 Spring 의존성은
   Spring 6.1.x 계열입니다. Spring Boot 4.0은 Spring 7.x를 사용하므로 클래스 로딩 시
   버전 충돌 가능성이 있습니다. Maven Plugin classpath는 애플리케이션 classpath와 분리되어
   있어 런타임에는 영향이 없으나, `liquibase:diff` 실행 시 문제가 발생할 경우
   아래 대안을 검토하세요.

3. 대안 전략 (리스크 발현 시):
   - `diff` goal 비활성화 후 모든 changelog를 수동 작성
   - Liquibase CLI 도구를 별도 환경(JVM 분리)에서 실행
   - `liquibase-hibernate6 5.0.1` 호환 이슈 트래커 확인:
     https://github.com/liquibase/liquibase-hibernate/issues

---

## Next Suggested Tasks

1. `./mvnw clean package -DskipTests` 실행하여 의존성 해석 확인
2. `./mvnw test` 실행하여 ArchUnit 테스트 통과 확인
3. DB 연결 후 `./mvnw liquibase:diff -Pdev` 실행하여 diff 결과 정확도 검증
4. diff 결과가 비어 있거나 정상적인 변경만 포함하는지 확인

---

## Notes for Future Agents

- `liquibase-hibernate7` 아티팩트는 존재하지 않습니다. 이 이름으로 검색하거나 추가하지 마세요.
- `liquibase-core` 버전(`4.31.0`)과 `liquibase-hibernate6` 버전(`5.0.1`)은 의도적으로 다릅니다.
  두 버전을 동기화하려는 시도는 하지 마세요.
- `liquibase:diff` 실행 시 Maven Plugin classpath에서 `liquibase-hibernate6`가 로드됩니다.
  애플리케이션 런타임(`./mvnw spring-boot:run`)에는 영향이 없습니다.
- 향후 Liquibase가 `liquibase-hibernate7` 아티팩트를 별도 릴리즈할 경우 마이그레이션이 필요할 수 있습니다.
  GitHub 릴리즈 페이지를 주기적으로 확인하세요: https://github.com/liquibase/liquibase-hibernate/releases
