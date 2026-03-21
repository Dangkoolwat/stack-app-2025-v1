# Environment Variables Operations Guide

## Purpose

Provide a practical HOW TO guide for local environment-variable setup and operational usage.

This document intentionally avoids repository-specific start commands when they are not yet standardized. Use the repository's actual backend and frontend run commands together with this guide.

---

## Local Setup Flow

1. Copy `.env.sample` to `.env`
2. Fill in local-only values in `.env`
3. Keep `.env.sample` free of secrets and committed to the repository
4. Keep `.env` uncommitted
5. Start backend and frontend with the repository's standard local run commands

Example:

```bash
cp .env.sample .env
```

---

## Fallback Rule for Missing Run Commands

If repository-specific run commands are not clearly documented, agents MUST:

1. check `README.md`
2. check package scripts, Gradle tasks, Maven commands, or other repository run definitions
3. avoid inventing undocumented commands
4. ask the user before execution when command ambiguity remains

---

## Operational Rules

### Local development

- `.env` MAY be used for local development
- `.env.sample` SHOULD remain the key reference for required variables
- Missing local values SHOULD be documented clearly for developers

### Production

- `.env` MUST NOT be the production source of truth
- Production values MUST come from OS environment variables or a secret manager
- Sensitive values MUST NOT appear in logs or frontend bundles

### CI/CD

- CI and deployment pipelines SHOULD use managed environment variables or a secret store
- Secrets MUST NOT be committed or copied into workflow files directly

---

## Sync Checklist

- [ ] Every required local key exists in `.env.sample`
- [ ] `.env` remains ignored by Git
- [ ] Newly added variables are documented for local setup
- [ ] Production variable ownership is clear

---

## Troubleshooting

### The app fails because a variable is missing

- compare `.env.sample` and `.env`
- verify the variable name in configuration files
- verify the variable name in the deployment environment

### A value works locally but not in production

- verify production environment variables or secret-manager mappings
- verify naming consistency between runtime and configuration placeholders
- verify that frontend values use `VITE_` only for public values

### A secret was exposed accidentally

- revoke the secret immediately
- rotate and reissue the secret
- update all affected environments
- record the impact and follow-up actions

---

## Related Documents

- `docs/standards/environment-variables-guideline.md`
- `docs/standards/configuration-externalization-guideline.md`
- `docs/workflow/ci-automation-rules.md`
