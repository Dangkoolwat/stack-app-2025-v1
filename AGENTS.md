# AGENTS.md (2026-03-21)

## 1. Language Policy (MANDATORY)

- All guideline documents MUST be written in English
- All code comments MUST be written in Korean
- All agent-log files MUST be written in Korean

---

## 2. Core Principles

### 2.1 Configuration Externalization (MANDATORY)

NEVER hardcode configuration.

MUST:
- Use `.env` for local development
- Use OS environment variables in production
- Reflect all variables in `.env.sample`
- Bind configuration via application.yml or Vite config

---

### 2.2 Security Principles

MUST NOT:
- Commit secrets
- Expose sensitive values in frontend (VITE_)

MUST:
- Use JWT_SECRET >= 64 chars
- Use external secrets only

---

## 3. Backend Standard Pattern

### REQUIRED FLOW

1. Define config in application.yml
2. Use ${ENV_VAR:default}
3. Bind with @ConfigurationProperties
4. Inject via @Configuration

Example:

application.yml
application:
  feature:
    timeout: ${FEATURE_TIMEOUT:30}

---

## 4. Frontend Standard Pattern

### REQUIRED FLOW

1. Use .env
2. Prefix with VITE_
3. Load via loadEnv
4. Define types in env.d.ts

---

## 5. Agent Execution Rules

WHEN adding new configuration:

MUST:
1. Add to .env
2. Add to .env.sample
3. Reflect in application.yml or vite.config.ts
4. Remove hardcoded values
5. Document in agent-log (Korean)

---

## 6. Code Comment Template (MANDATORY)

/**
 * Description
 *
 * 🤖 Agent Guide:
 * - Purpose
 * - Usage
 *
 * ⚠️ Warning:
 * - Important notes
 *
 * Change History:
 * - YYYY-MM-DD: [Task] Description
 */

---

## 7. agent-log Rule

ALL logs MUST be written in Korean.

Format:

- 작업 내용
- 변경 이유
- 영향 범위
- 추가 작업 필요 여부

---

## 8. CI / Automation Rules

CI MUST validate:

- .env is not committed
- .env.sample exists and updated
- No hardcoded secrets
- Configuration follows externalization pattern
- Comments follow template

---

## 9. Forbidden Patterns

STRICTLY FORBIDDEN:

- Hardcoded secrets
- Hardcoded URLs
- Hardcoded ports
- Mixing environment logic in code
- Using VITE_ for sensitive data

---

## 10. Folder Structure Rule

Docs must be placed:

docs/
  process/
    environment-variables.md
    configuration-externalization.md

  backend/
  frontend/

DO NOT place guideline files at root.

---

## 11. Agent Workflow

1. Read AGENTS.md
2. Follow process docs
3. Apply patterns
4. Write Korean comment
5. Write Korean logs
6. Pass CI validation

---

## END
