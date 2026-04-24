# 2026-03-15 Mockito JVM Agent 정식 등록 — JDK 21 동적 로딩 경고 해소

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

Mockito self-attach 경고 해소 — maven-surefire-plugin `-javaagent` 설정 추가

---

## Goal

`./mvnw test` 실행 시 발생하는 JDK 21 동적 에이전트 로딩 경고를 제거한다.

```
Mockito is currently self-attaching to enable the inline-mock-maker.
This will no longer work in future releases of the JDK.
WARNING: A Java agent has been loaded dynamically (byte-buddy-agent-1.17.8.jar)
WARNING: Dynamic loading of agents will be disallowed by default in a future release
```

---

## Context

### 원인 구조

```
JDK 21 이전:  Mockito → ByteBuddy → self-attach() → 허용 (경고 없음)
JDK 21:       Mockito → ByteBuddy → self-attach() → 경고 출력 (동작은 함)
JDK 차기 버전: Mockito → ByteBuddy → self-attach() → 차단 (테스트 실패 예상)
```

Mockito의 `inline-mock-maker`(final class mocking, static mocking 지원)는 내부적으로
ByteBuddy Agent(`byte-buddy-agent`)를 사용합니다. 에이전트를 JVM 시작 후 런타임에
동적으로 첨부(self-attach)하는 방식은 JDK 9+ Attach API 보안 강화로 경고가 발생하며,
JDK 미래 버전에서 기본 차단됩니다.

### 해결 원리

`-javaagent:<jar>` 옵션을 JVM 시작 시 전달하면 에이전트가 사전 등록됩니다.
이 방식에서는 동적 자가 첨부가 필요 없어 경고가 완전히 사라집니다.

### mockito.version 프로퍼티

Spring Boot parent BOM(`spring-boot-starter-parent 4.0.3`)이 `mockito.version`
프로퍼티를 자동으로 관리합니다. pom.xml에 별도로 선언할 필요 없이
`${mockito.version}`으로 참조 가능합니다.

---

## Work Performed

1. 경고 원인 분석: ByteBuddy Agent 동적 self-attach 메커니즘 확인
2. `pom.xml`의 `<build><plugins>` 섹션에 `maven-surefire-plugin` 설정 추가:
   - `<argLine>` 에 `-javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar` 추가
   - `@{argLine}` 접두사 유지 (JaCoCo 등 다른 플러그인 argLine 주입 호환)
   - `-XX:+EnableDynamicAgentLoading` 추가 (과도기 경고 완전 억제)
3. 본 agent-log 작성

---

## Files Modified

- `pom.xml`
  - `<build><plugins>` 에 `maven-surefire-plugin` `<configuration>` 블록 추가 (신규)

---

## 변경 상세

```xml
<!-- 추가된 플러그인 설정 -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>
      @{argLine}
      -javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar
      -XX:+EnableDynamicAgentLoading
    </argLine>
  </configuration>
</plugin>
```

### 각 요소 설명

| 요소 | 역할 |
|------|------|
| `@{argLine}` | JaCoCo 등 다른 Maven 플러그인이 주입하는 JVM 인수 유지 (`${argLine}` 과 다름 — late binding) |
| `-javaagent:<path>` | mockito-core JAR을 JVM 시작 시 정식 에이전트로 등록 |
| `${settings.localRepository}` | Maven 로컬 리포지토리 경로 (`~/.m2/repository`) |
| `${mockito.version}` | Spring Boot parent BOM이 관리하는 Mockito 버전 |
| `-XX:+EnableDynamicAgentLoading` | 과도기 환경에서 동적 로딩 관련 잔여 경고 억제 |

---

## Architecture Impact

테스트 실행 환경(JVM 인수)만 변경됩니다.
프로덕션 코드, 런타임, API 계약, DB 스키마에는 영향 없습니다.

---

## Security Impact

없음. 테스트 전용 JVM 옵션입니다.

---

## Verification

```bash
./mvnw test
```

성공 기준:
- `Mockito is currently self-attaching` 메시지 미출력
- `WARNING: A Java agent has been loaded dynamically` 미출력
- `WARNING: Dynamic loading of agents will be disallowed` 미출력
- 기존 테스트 통과율 유지

---

## Risks

1. mockito-core JAR 미존재: 처음 `./mvnw test` 실행 전 로컬 리포지토리에
   `mockito-core-${mockito.version}.jar`이 없으면 `FileNotFoundException`이 발생합니다.
   `./mvnw dependency:resolve -Dclassifier=` 또는 한 번 `./mvnw test`를 실행하면
   자동 다운로드됩니다.

2. @{argLine} vs ${argLine}: JaCoCo 플러그인 사용 시 반드시 `@{argLine}` (late binding)
   을 사용해야 합니다. `${argLine}` (early binding)을 사용하면 JaCoCo가 주입하는
   `-javaagent:jacocoagent.jar` 인수가 무시되어 커버리지 측정이 실패합니다.

3. 멀티 모듈 프로젝트 확장 시: 하위 모듈에서도 동일한 설정이 필요합니다.
   부모 POM에 설정하면 상속됩니다.

---

## Next Suggested Tasks

없음. 단독으로 완결되는 변경입니다.

---

## Notes for Future Agents

- `${mockito.version}`은 `spring-boot-starter-parent`가 자동으로 제공합니다.
  pom.xml `<properties>`에 별도 선언하지 마세요 (중복 선언 시 BOM 버전이 무시됩니다).
- Mockito 버전이 Spring Boot BOM과 별도로 관리되도록 바꿀 경우,
  `<properties>`에 `<mockito.version>x.y.z</mockito.version>`을 추가하면 됩니다.
- `-XX:+EnableDynamicAgentLoading`은 JDK 21의 과도기 플래그입니다.
  향후 JDK에서 에이전트 선행 등록이 완전히 강제되면 이 플래그는 제거해도 됩니다.
