# Configuration Externalization Guideline

## Purpose
Define the required implementation pattern for externalizing configuration in backend and frontend applications.

This document applies when adding new configuration, removing hardcoded values, handling environment-specific values, or introducing security-sensitive or operationally tunable settings.

---

## Mandatory Rules

For security rules (secrets, `.env` handling, production environment variables), refer to [Environment Variables Guideline](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/environment-variables-guideline.md).

Additional implementation rules:

- MUST externalize deploy-time and environment-specific values.
- MUST use structured configuration binding for grouped backend settings.
- MUST treat frontend environment variables as public values.

---

## What MUST Be Externalized

- Database connection settings
- JWT secrets, API keys, tokens, and credentials
- SMTP credentials
- Redis or cache connection settings
- External service endpoint URLs
- Environment-specific values
- Operationally tunable values such as timeout, pool size, retry count, and TTL

---

## What SHOULD NOT Be Externalized

- Pure algorithm constants
- Stable business rule constants that do not vary by environment
- Static UI copy

---

## Required Backend Pattern

Backend configuration MUST follow this flow:

1. Define the configuration in `application.yml`
2. Use `${ENV_VAR:default}` placeholders
3. Bind grouped values with `@ConfigurationProperties`
4. Register properties through `@EnableConfigurationProperties`
5. Apply validation for required and numeric values

### Example: `application.yml`

```yaml
application:
  feature-name:
    enabled: ${FEATURE_ENABLED:false}
    timeout-seconds: ${FEATURE_TIMEOUT_SECONDS:30}
    max-retries: ${FEATURE_MAX_RETRIES:3}
```

### Example: `@ConfigurationProperties`

```java
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.feature-name")
public class FeatureProperties {

    @NotNull
    private Boolean enabled = false;

    @Min(1)
    @Max(300)
    private Integer timeoutSeconds = 30;

    @Min(0)
    @Max(10)
    private Integer maxRetries = 3;
}
```

### Example: configuration class

```java
@Configuration
@EnableConfigurationProperties(FeatureProperties.class)
public class FeatureConfiguration {

    private final FeatureProperties properties;

    public FeatureConfiguration(FeatureProperties properties) {
        this.properties = properties;
    }
}
```

---

## Required Frontend Pattern

Frontend configuration MUST follow this flow:

1. Define values in `.env` for local development
2. Use the `VITE_` prefix
3. Load values through the Vite environment mechanism
4. Define types in `env.d.ts` when applicable

### Example: `.env`

```bash
VITE_API_TARGET=https://localhost:8443
VITE_FEATURE_ENABLED=true
```

### Example: `vite.config.ts`

```typescript
const env = loadEnv(mode, process.cwd(), 'VITE_');
```

### Example: type definition

```typescript
interface ImportMetaEnv {
  readonly VITE_API_TARGET: string
}
```

---

## Validation Rules

- MUST validate required values
- MUST validate numeric ranges where applicable
- SHOULD avoid unsafe production defaults
- SHOULD group related settings into one properties class instead of scattering `@Value` annotations

---

## Implementation Checklist

### Backend

- [ ] Added configuration to `application.yml`
- [ ] Used environment variable placeholders
- [ ] Bound grouped values with `@ConfigurationProperties`
- [ ] Applied validation
- [ ] Updated `.env.sample`

### Frontend

- [ ] Used `VITE_` prefix
- [ ] Added or updated type definitions when needed
- [ ] Verified that no secret is exposed
- [ ] Updated `.env.sample`

---

## Forbidden Patterns

### Hardcoded secret

```yaml
password: MyPassword123
```

### Hardcoded operational value without externalization

```java
private static final int TIMEOUT_SECONDS = 30;
```

---

## Related Documents

- `docs/standards/environment-variables-guideline.md`
- `docs/standards/templates/properties-template.java.md`
- `docs/standards/templates/configuration-template.java.md`
- `docs/operations/environment-variables.md`
