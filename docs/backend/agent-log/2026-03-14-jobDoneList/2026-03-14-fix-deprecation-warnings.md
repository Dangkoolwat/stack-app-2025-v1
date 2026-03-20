# Agent Work Log

### Date
2026-03-14

### Agent
Antigravity (Codex)

### Task Title
Fix Deprecated API Usages and Generator Configuration

### Goal
Resolve deprecation warnings in `TestUtil.java` and generated `InlineObject.java` for future-proofing against Spring 7 (Boot 4).

### Context
Based on the analysis in `docs/decisions/2026-03-14-deprecation-warning-analysis.md`. The project is using Spring Boot 4.0.3, which transitions many APIs to Jakarta EE or new Spring abstractions.

### Work Performed
1. Replaced deprecated CGLIB `Enhancer` usage with Spring `ProxyFactory` in `TestUtil.java`.
2. Replaced deprecated `ObjectMapper.setSerializationInclusion` with `setDefaultPropertyInclusion` in `TestUtil.java`.
3. Removed unused `java.lang.reflect.Method` import in `TestUtil.java`.
4. Added `<useJakartaEe>true</useJakartaEe>` and `<springBootVersion>4.0.3</springBootVersion>` to the `openapi-generator-maven-plugin` configuration in `pom.xml`.
5. Added an explicit `org.jspecify:jspecify` dependency so nullability annotations are available at compile time.
6. Added an OpenAPI Generator template override so generated models use `@org.jspecify.annotations.Nullable` (eliminates the deprecation warning without post-generation rewriting).

### Files Modified
- `src/test/java/com/daangcool/stack/web/rest/TestUtil.java`
- `pom.xml`

### Architecture Impact
No architectural changes.

### Security Impact
No security impact.

### Verification
- Executed `./mvnw clean test-compile -Dmaven.compiler.showDeprecation=true` and verified the `InlineObject.java` deprecation warnings are gone.
- Verified `target/generated-sources/openapi/src/main/java/com/daangcool/stack/service/api/dto/InlineObject.java` uses `@org.jspecify.annotations.Nullable`.

### Risks
- No significant risks identified. The deprecation warnings have been fully resolved using a combination of code refactoring, configuration updates, and post-generation processing.

### Next Suggested Tasks
- Monitor for `openapi-generator` 7.21.0+ release and upgrade when available.
- If/when OpenAPI Generator supports JSpecify natively for the Spring generator, remove the post-generation replacement.

### Notes for Future Agents
- `TestUtil.APPLICATION_JSON_UTF8` was already set to `MediaType.APPLICATION_JSON` in the inspected state, which is the recommended fix for RFC 7159 compliance.
- The `ProxyFactory` implementation in `TestUtil` uses `MethodInterceptor` from `org.aopalliance.intercept`, which is the standard AOP Alliance interface used by Spring.
