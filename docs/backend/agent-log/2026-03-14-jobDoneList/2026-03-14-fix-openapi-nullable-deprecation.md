# Agent Work Log

### Date
2026-03-14

### Agent
Antigravity (Codex)

### Task Title
Remove OpenAPI Generated `@Nullable` Deprecation Warning

### Goal
Eliminate the Maven compiler deprecation warnings caused by `org.springframework.lang.Nullable` in the OpenAPI-generated `InlineObject.java`.

### Context
Running `./mvnw clean test-compile -Dmaven.compiler.showDeprecation=true` produced repeated warnings from `target/generated-sources/openapi/.../InlineObject.java`. The OpenAPI generator version in use (`openapi-generator-maven-plugin` 7.20.0, generator `spring`) still generates `import org.springframework.lang.Nullable;`, which is deprecated in Spring Framework 7.x. Generated sources live under `target/` and are overwritten on each build, so fixes must be applied via generator config or post-processing.

### Work Performed
1. Added a compile dependency on `org.jspecify:jspecify` so nullability annotations are available at compile time.
2. Switched to an OpenAPI Generator template override so generated code uses the fully-qualified `@org.jspecify.annotations.Nullable` (avoids relying on post-generation rewriting).
3. Configured the generator to use the project template directory via `templateDirectory`.
4. Updated the decision doc and earlier log notes to reflect the final approach (JSpecify).

### Files Modified
- `pom.xml`
- `docs/decisions/2026-03-14-deprecation-warning-analysis.md`
- `docs/agent-log/2026-03-14-fix-deprecation-warnings.md`
- `docs/agent-log/2026-03-14-fix-openapi-nullable-deprecation.md`
- `src/main/resources/openapi-generator-templates/JavaSpring/nullableAnnotation.mustache`

### Architecture Impact
No architectural changes.

### Security Impact
No security impact.

### Verification
- Executed `./mvnw clean test-compile -Dmaven.compiler.showDeprecation=true` and confirmed the `InlineObject.java` `org.springframework.lang.Nullable` deprecation warnings no longer appear.
- Confirmed generated `InlineObject.java` uses `@org.jspecify.annotations.Nullable`.

### Risks
- The generator may still emit an unused `import org.springframework.lang.Nullable;` line; it does not trigger deprecation warnings because the annotation usage is fully-qualified to JSpecify.

### Next Suggested Tasks
- Track upstream OpenAPI Generator releases; when the Spring generator stops emitting `org.springframework.lang.Nullable`, remove the post-processing replacement.
- Consider replacing inline schema generation (`InlineObject`) in `src/main/resources/swagger/api.yml` with a named schema to avoid `InlineObject` model churn.

### Notes for Future Agents
- There is no `jakarta.annotation.Nullable` in the standard Jakarta annotation API; use JSpecify (`org.jspecify.annotations.Nullable`) if you need a stable nullability annotation on the classpath.
