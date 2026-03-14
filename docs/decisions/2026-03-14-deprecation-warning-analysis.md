# Deprecation Warning 분석 및 수정 방안

**작성일:** 2026-03-14  
**대상 빌드 경고:**
1. `TestUtil.java` — uses or overrides a deprecated API
2. `InlineObject.java` (자동생성) — uses or overrides a deprecated API

---

## 1. TestUtil.java

**파일:** [`src/test/java/com/daangcool/stack/web/rest/TestUtil.java`](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/test/java/com/daangcool/stack/web/rest/TestUtil.java)

### 원인 분석

두 곳에서 deprecated API를 사용하고 있습니다.

---

#### 1-A. `MediaType` Charset 생성자 (L43–47)

```java
// 현재 코드 (deprecated)
public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
    MediaType.APPLICATION_JSON.getType(),
    MediaType.APPLICATION_JSON.getSubtype(),
    StandardCharsets.UTF_8       // ← Charset 파라미터 생성자는 deprecated
);
```

**원인:** `MediaType(String type, String subtype, Charset charset)` 생성자는  
Spring Framework 5.2 이후 deprecated. Spring Boot 4.x (Spring 7.x) 환경에서 경고 발생.

**배경:** HTTP/1.1 JSON 응답에 `charset=UTF-8`을 명시하는 것은 RFC 7159에 의해  
불필요하며, Spring MVC는 기본적으로 UTF-8 인코딩을 사용합니다.

**수정 방안:**

```java
// 수정 코드 — MediaType.APPLICATION_JSON 상수 직접 사용
public static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
```

> **영향 범위:** `APPLICATION_JSON_UTF8`를 참조하는 모든 테스트 파일을 확인하고,  
> 실제로 `application/json;charset=UTF-8` 헤더를 명시적으로 검증하는 테스트가 있다면  
> 해당 검증 로직도 `MediaType.APPLICATION_JSON`으로 변경 필요.

---

#### 1-B. Spring CGLIB `Enhancer` 직접 사용 (L221–236)

```java
// 현재 코드 (deprecated internal API)
import org.springframework.cglib.proxy.Enhancer;      // ← Spring 내부 재패키징 CGLIB
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public static <T> T createUpdateProxyForBean(T update, T original) {
    Enhancer e = new Enhancer();
    e.setSuperclass(original.getClass());
    e.setCallback(new MethodInterceptor() { ... });
    return (T) e.create();
}
```

**원인:** `org.springframework.cglib.*`는 Spring이 내부적으로 재패키징한 CGLIB입니다.  
Spring Framework 6 / Spring Boot 3에서 AOT(Ahead-of-Time) 컴파일 지원을 위해  
CGLIB 프록시 생성 방식이 전면 개편되었으며, `spring.cglib` 패키지는  
**Spring 7(Boot 4) 에서 제거 대상**입니다.

**수정 방안 (두 가지 중 선택):**

**방안 A: Spring `ProxyFactory` 활용 (권장)**

```java
import org.springframework.aop.framework.ProxyFactory;

public static <T> T createUpdateProxyForBean(T update, T original) {
    ProxyFactory factory = new ProxyFactory(original);
    factory.setProxyTargetClass(true);
    factory.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation -> {
        Object val = update.getClass()
            .getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes())
            .invoke(update, invocation.getArguments());
        if (val == null) {
            return invocation.proceed();
        }
        return val;
    });
    @SuppressWarnings("unchecked")
    T proxy = (T) factory.getProxy();
    return proxy;
}
```

**방안 B: 테스트 로직 재설계 (비권장, 간단한 경우)**

`createUpdateProxyForBean()`이 실제로 필요한지 검토. 단순 필드 머지 목적이라면  
프록시 없이 명시적 복사 메서드(빌더, MapStruct 등)로 대체하여 프록시 의존 제거.

---

## 2. InlineObject.java (자동생성 파일)

**파일:** `target/generated-sources/openapi/src/main/java/com/daangcool/stack/service/api/dto/InlineObject.java`  
**생성 도구:** `openapi-generator-maven-plugin` 7.20.0 (generator: `spring`)

### 원인 분석

```java
// 현재 코드 (deprecated)
import org.springframework.lang.Nullable;  // ← Spring 7에서 deprecated

private @Nullable String title;
public @Nullable String getTitle() { ... }
// ... 모든 Optional 필드에 @Nullable 적용
```

**원인:** `org.springframework.lang.Nullable`은 Spring 7.0에서  
Jakarta EE 표준 어노테이션(`jakarta.annotation.Nullable`)으로 이관되었습니다.  
OpenAPI Generator 7.20.0의 `spring` 제너레이터가 아직 `org.springframework.lang.Nullable`을  
사용하는 코드를 생성하여 Spring Boot 4.x(Spring 7)에서 deprecation 경고가 발생합니다.

> **참고:** `InlineObject`는 RFC 7807 ProblemDetail 응답 스키마를 나타내는  
> 자동생성 클래스입니다. `target/` 폴더는 빌드 산출물이므로 직접 수정하면  
> 다음 빌드 시 덮어써집니다.

### 수정 방안

#### 방안 A: OpenAPI Generator 업그레이드 (권장)

`pom.xml`에서 generator 버전을 Spring Boot 4.x 호환 버전으로 업그레이드합니다.

```xml
<!-- pom.xml -->
<properties>
    <!-- 7.20.0 → Spring Boot 4.x 공식 지원 버전으로 업그레이드 -->
    <openapi-generator-maven-plugin.version>7.21.0</openapi-generator-maven-plugin.version>
</properties>
```

> OpenAPI Generator GitHub에서 Spring Boot 4 / Spring 7 지원 여부를 확인하세요:  
> https://github.com/OpenAPITools/openapi-generator/releases

#### 방안 B: `useJakartaEe` 옵션 활성화

`pom.xml`의 openapi-generator 설정에 `useJakartaEe` configOption을 추가합니다.

```xml
<configOptions>
    <delegatePattern>true</delegatePattern>
    <title>stack</title>
    <useSpringBoot3>true</useSpringBoot3>
    <useJakartaEe>true</useJakartaEe>   <!-- 추가: jakarta.annotation.Nullable 사용 -->
</configOptions>
```

`mvn generate-sources` 후 재생성된 `InlineObject.java`를 확인합니다.

#### 방안 C: api.yml에서 InlineObject 스키마 제거 (중장기)

`InlineObject`는 Spring의 `ProblemDetail`과 구조가 동일합니다.  
OpenAPI 스펙(`src/main/resources/swagger/api.yml`)에서 해당 스키마를  
`ProblemDetail`로 직접 참조하거나 제거하면 자동생성 자체를 없앨 수 있습니다.

```yaml
# api.yml (예시)
responses:
  '400':
    description: Bad Request
    content:
      application/problem+json:
        schema:
          $ref: '#/components/schemas/Problem'   # InlineObject 대신 표준 Problem 참조
```

---

## 정리 및 우선순위

| # | 파일 | deprecated 항목 | 심각도 | 권장 수정 |
|---|------|-----------------|--------|-----------|
| 1 | `TestUtil.java` | `MediaType` Charset 생성자 | 🟡 Low | `MediaType.APPLICATION_JSON` 상수로 교체 |
| 2 | `TestUtil.java` | `spring.cglib.Enhancer` | 🟠 High | `ProxyFactory` 또는 테스트 로직 재설계 |
| 3 | `InlineObject.java` | `spring.lang.Nullable` | 🟡 Low | generator `useJakartaEe=true` 옵션 추가 |

> **우선 조치:** 
> - `TestUtil` #2 (spring.cglib)는 Spring Boot 4 정식 릴리즈(혹은 미래 마이너)에서  
>   런타임 오류로 전환될 가능성이 있으므로 조기 대응 권장.
> - `InlineObject` #3은 `useJakartaEe=true` configOption 한 줄 추가로 해결 가능.  
>   옵션 미지원 시 generator 버전 업그레이드.

---

## 검증 방법

```bash
# 수정 후 deprecation 상세 확인
./mvnw compile -Xlint:deprecation 2>&1 | grep -i deprecated

# TestUtil 관련 테스트 실행
./mvnw test -Dtest="*IT" --no-transfer-progress

# OpenAPI 재생성 후 확인
./mvnw generate-sources
grep -r "springframework.lang.Nullable" target/generated-sources/
```
