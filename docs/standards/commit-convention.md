---
author: opencode
created_at: 2026-03-27 (토요일)
language: mixed
---

# Commit Convention Guide

## 1. Commit Types

| Type       | Description                                          | Example                                     |
| ---------- | ---------------------------------------------------- | ------------------------------------------- |
| `feat`     | New feature implementation                           | `feat(server): add user creation API`       |
| `fix`      | Bug fix                                              | `fix(auth): resolve token expiration issue` |
| `docs`     | Documentation update                                 | `docs(readme): update project description`  |
| `refactor` | Code structure improvement (no functionality change) | `refactor(ui): improve form layout`         |
| `perf`     | Performance improvement                              | `perf(db): optimize query index`            |
| `test`     | Test code addition/modification                      | `test(auth): add JWT validation tests`      |
| `build`    | Build process improvement                            | `build(ci): configure GitHub Actions`       |
| `ci`       | CI/CD configuration change                           | `ci(deploy): add staging pipeline`          |
| `chore`    | Non-functional changes                               | `chore: update dependency versions`         |

## 2. Commit Rules

1. Single Responsibility Principle: One commit = one feature
2. Required One-line Description: Avoid uppercase
3. Optional Body Explanation: For complex changes, add after 2 blank lines
4. Additional Information:
   - Include related issue number (e.g., `See #123`)
   - Based on Spring Boot 4.0.4

## 3. Commit Message Examples

### Correct Example

```
feat(server): add JWT refresh token endpoint

- 사용자_refresh token 생성 로직 구현
- Redis에 token 저장
- JWT 보안 설정 업데이트
```

### Incorrect Example

```
fix: update dependencies
chore(update java version)
```

## 4. Commit Message Creation Guide

1. Type: [Type] (case-insensitive)
2. Scope (Optional): `[Type](scope): Description`
   - Scope examples: `feat(server)`, `fix(auth)`
3. Description: Summarize changes concisely

## 5. Commit Command Examples

```bash
# Simple commit
 git commit -m "feat(auth): add password complexity validation"

# Complex changes
 git commit -m "refactor(ui): migrate to new component library\n\n- Replace old UI components with new framework\n- Update related documentation\n- Fix accessibility issues\"\"
```

## 6. Additional Information

- Author Specification: Include name/email
  Example: `By: John Doe <john@example.com>`
- Reference Issue: If related, `See #ISSUE_NUMBER`

## 7. Related Documents

- AGENTS.md
- https://www.conventionalcommits.org/en/v1.0.0/

## 8. Commitlint Rules

All commit messages must validate against `.commitlintrc.json`:

1. Subject Line Rules:
   - Must include type
   - If scope included, type and description must be separated by space
   - Description must not be empty
2. Body Rules:
   - Use imperative mood
   - Line length ≤ 72 characters.

## 9. Agent Log Reference Rule

- Commit body must reference related agent log file:
  Example:

  ```
  feat(server): add user creation API

  See docs/backend/agent-log/2026-03-27-user-api-creation
  ```

## 10. Document Hierarchy Priority

In case of conflict, AGENTS.md takes priority over:

1. AGENTS.md
2. docs/standards/
3. docs/workflow/
4. docs/operations/
