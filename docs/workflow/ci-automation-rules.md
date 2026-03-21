# CI Automation Rules

## Purpose
Define the checks that CI and automation SHOULD perform so that reviewers do not spend time on repetitive validation.

---

## Automation Scope

Items that do not require human judgment SHOULD be automated where practical.

Examples:

- `agent-log` existence checks
- required file existence checks
- OpenAPI presence or update checks
- impact-document checks for configuration or dependency changes
- pull request template completeness checks

---

## Recommended Phase 1 Checks

### Agent-log validation

CI SHOULD check:

- whether non-trivial changes include an `agent-log`
- whether all 6 required files exist

Required files:

- `problem-analysis.md`
- `proposal.md`
- `self-check.md`
- `implementation-plan.md`
- `walkthrough.md`
- `final-report.md`

### OpenAPI validation

CI SHOULD check:

- whether API-related code changes are accompanied by the required OpenAPI update
- whether the resulting specification remains Swagger-compatible

### Config and dependency change validation

CI SHOULD check whether changes to files such as `application.yml`, `.env.sample`, `build.gradle`, `pom.xml`, or `package.json` are accompanied by:

- required impact documentation
- corresponding notes in `final-report.md`

---

## Recommended Phase 2 Checks

### Code comment guidance check

For high-risk areas such as configuration, security, cache, and infrastructure code, CI MAY start with warnings for missing required documentation comments.

### Pull request body check

CI MAY check whether required pull request sections are filled in, such as:

- Problem
- Impact Scope
- Test
- Rollback Plan

---

## Example Failure Conditions

CI MAY fail when:

- required `agent-log` files are missing
- API changes are missing required OpenAPI updates
- configuration changes are missing required impact documentation
- the pull request template is missing critical sections

---

## Gradual Adoption Rule

Do not start by failing every rule immediately.

Recommended order:

1. warnings first
2. fail only on critical omissions
3. expand after the team stabilizes the workflow

---

## Recommended Rollout Order

### Phase 1

- `agent-log` existence check
- required file count check

### Phase 2

- OpenAPI update check
- configuration and dependency impact checks

### Phase 3

- pull request body validation
- warnings for missing comments or missing supporting documentation in high-risk areas

---

## Related Documents

- `docs/standards/environment-variables-guideline.md`
- `docs/workflow/git-workflow.md`
- `docs/workflow/pr-review-checklist.md`
- `docs/workflow/github-actions.md`

---

## Operating Principle

Automation is not a replacement for review. CI catches omissions; reviewers make engineering judgments.
