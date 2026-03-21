# Environment Variables Guideline

## Purpose

Define mandatory agent actions for adding, changing, validating, and operating environment variables safely.

This document defines action rules. When a trigger condition occurs, agents MUST perform the required steps.

---

## Rule Interpretation

- MUST = mandatory
- SHOULD = recommended default
- MAY = optional

---

## Mandatory Security Rules

- MUST NOT commit `.env` files.
- MUST NOT hardcode secrets in source code or checked-in configuration files.
- MUST NOT expose sensitive values through frontend `VITE_` variables.
- MUST use OS environment variables or a secret manager in production.
- MUST treat all `VITE_` variables as public-by-design values.

---

## Trigger: Adding a New External Integration

Examples include API, SMS, OAuth, payment, email, or other third-party integrations.

Agents MUST:

1. Add local development variables to `.env`
2. Add the same keys to `.env.sample` with placeholder values
3. Define production variables in OS environment variables or a secret manager
4. Remove hardcoded values from code and configuration
5. Reference the variables from `application.yml`, Vite config, or equivalent configuration files
6. Record the change in `agent-log`

---

## Trigger: Adding a New Environment Variable

Agents MUST:

1. Update `.env.sample`
2. Request or document the required `.env` update for local execution
3. Replace direct values in code with environment variable references
4. Check related scripts and runbooks
5. Perform a Global Impact Review when the variable matches any trigger defined below

---

## Trigger: Modifying an Existing Environment Variable

Agents MUST:

1. Analyze the impact scope
2. Verify rollout and rollback feasibility
3. Confirm production environment updates are accounted for
4. Record the reason and impact in `agent-log`

---

## Global Impact Review Trigger

Agents MUST perform Global Impact Review when a variable:

- is used in authentication, authorization, security, token handling, or secret management
- changes runtime behavior such as timeout, retry, pool size, TTL, concurrency, or rate limit
- changes endpoint URL, external integration target, or network routing behavior
- affects cache, database, serialization, or dependency behavior
- changes API-visible behavior between environments

---

## Trigger: Production Deployment

Agents MUST:

- NOT rely on `.env`
- Use OS environment variables or a secret manager
- Verify that no sensitive value is exposed through logs or frontend bundles

---

## Trigger: CI/CD Pipeline Changes

Agents MUST:

- Configure environment variables in the pipeline or deployment environment
- Store secrets in the CI secret store
- Avoid direct `.env` dependence in CI and production paths

---

## Sync Rule

- `.env.sample` is the required key reference for local development
- `.env` is the local runtime value file
- Keys that are required locally SHOULD exist in both files

---

## Validation Rules

CI SHOULD validate at least the following:

- `.env` is not committed
- Secrets are not hardcoded
- `.env.sample` is updated when new variables are introduced

---

## Secret Leak Response

If a secret is exposed, the response MUST include:

1. Immediate revocation
2. Reissue or rotation
3. Replacement in runtime environments
4. Impact review and follow-up tracking

---

## Related Documents

- `docs/standards/configuration-externalization-guideline.md`
- `docs/workflow/ci-automation-rules.md`
- `docs/operations/environment-variables.md`
