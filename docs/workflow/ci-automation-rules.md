# CI Automation Rules

## Purpose

Define the checks that CI and automation SHOULD perform so that reviewers do not spend time on repetitive validation.

---

## Rule Interpretation

- MUST = required for enforced critical checks
- SHOULD = recommended default
- MAY = optional or phased

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

## Enforcement Policy

### CI MUST fail for critical omissions

- missing required `agent-log` files for a non-trivial change
- hardcoded secrets or committed `.env`
- missing required OpenAPI update when an API contract changed
- missing required impact analysis for config, dependency, cache, or security-sensitive changes

### CI SHOULD warn for non-critical omissions

- missing required comments in high-risk classes
- incomplete pull request template sections
- missing supporting documentation that does not block safe review

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

## Gradual Adoption Rule

Do not start by failing every rule immediately.

Recommended order:

1. warnings first
2. fail only on critical omissions
3. expand after the team stabilizes the workflow

---

## Related Documents

- `docs/standards/environment-variables-guideline.md`
- `docs/workflow/git-workflow.md`
- `docs/workflow/pr-review-checklist.md`
- `docs/workflow/github-actions.md`

---

## Operating Principle

Automation is not a replacement for review. CI catches omissions; reviewers make engineering judgments.
