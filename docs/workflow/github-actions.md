# GitHub Actions Guide

## Purpose
Describe how GitHub Actions can enforce the workflow and documentation rules defined in `AGENTS.md` and the workflow documents.

---

## Recommended Workflow Files

Store workflow files under:

```text
.github/workflows/agent-guard.yml
.github/workflows/pr-body-check.yml
```

---

## Recommended Responsibilities

### `agent-guard.yml`

This workflow SHOULD check:

- whether non-trivial changes include an `agent-log`
- whether all 6 required `agent-log` files exist
- whether configuration or dependency changes include impact and rollback records
- whether API-related changes require OpenAPI updates or warnings

### `pr-body-check.yml`

This workflow SHOULD check whether required pull request sections are present.

---

## Practical Notes

- These workflows are a first-line guardrail, not a full semantic reviewer
- Teams SHOULD start with warning-heavy behavior and strengthen failures gradually
- Repository-specific paths and naming rules MUST be adjusted to match the actual repository structure

---

## Related Documents

- `docs/workflow/git-workflow.md`
- `docs/workflow/pr-review-checklist.md`
- `docs/workflow/ci-automation-rules.md`
- `.github/pull_request_template.md`
