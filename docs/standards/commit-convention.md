# Commit Convention Guide

This project follows the Conventional Commits standard (v1.0.0). Automated validation is performed via `commitlint` and `Husky`.

## Message Format

```text
<type>(<optional scope>): <description>

[optional body]

[optional footer(s)]
```

### 1. Type (MANDATORY)

Must be one of the following:

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `build`: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- `ci`: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- `chore`: Other changes that don't modify src or test files

### 2. Scope (OPTIONAL)

Provides additional contextual information.

- Example: `feat(auth): ...`, `fix(api): ...`

### 3. Description (MANDATORY)

A concise summary of the change.

- MUST have a space after the colon.
- MUST NOT start with a hyphen or special character.
- MUST NOT be empty.

---

## Common Mistakes

> [!WARNING]
> These will cause the commit to fail:

- Invalid: `- feat: add login` (Leading hyphen)
- Invalid: `feat:add login` (Missing space after colon)
- Invalid: `feat: add login` (Incorrect format - if hyphen is included accidentally)
- Invalid: `add login` (Missing type)
- Invalid: `feat add login` (Missing colon)

## Examples

- `feat(ui): add new dark mode toggle`
- `fix(bug): resolve crash on startup`
- `docs(standards): update commit convention guide`
