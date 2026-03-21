# PR Review Checklist

## Purpose
Define a compact review checklist so that reviewers can assess pull requests efficiently without reading every change in depth first.

---

## Review Order

1. Read `final-report.md`
2. Check the changed file list
3. Check whether high-risk areas are involved
4. Inspect detailed code only when needed

---

## Reviewer Quick Check

- [ ] Is this a high-risk change? (Config, Security, Cache, DB, Dependency, OpenAPI)
- [ ] Does it violate architecture?
- [ ] Is rollback possible?

---

## Required Documentation Check

- [ ] `agent-log` exists
- [ ] All 6 required files exist
- [ ] `final-report.md` is clear
- [ ] `self-check.md` exists
- [ ] High-risk changes include impact analysis

---

## Implementation Quality Check

- [ ] The problem is clearly defined
- [ ] The chosen solution is not unnecessarily complex
- [ ] Existing patterns are followed first
- [ ] The change scope is reasonable

---

## Maintainability Check

- [ ] Code responsibilities remain clear
- [ ] Change reasons are recorded in documentation or source code comments where required
- [ ] Future maintainers can understand the change
- [ ] Large refactoring has a clear reason

---

## Security, Config, and Infrastructure Check

- [ ] No sensitive data is logged
- [ ] Authentication and authorization behavior remains correct
- [ ] Cache policy is reviewed
- [ ] Configuration changes include impact analysis
- [ ] Dependency changes include compatibility review

---

## API and Contract Check

- [ ] No API contract change, or OpenAPI is updated
- [ ] Breaking changes are explicitly stated
- [ ] Frontend and backend impact is recorded when relevant

---

## Test Check

- [ ] Test results are recorded
- [ ] Regression-prone areas are reviewed
- [ ] Manual test requirements are explained when automation is not enough

---

## Rejection Conditions

A change MAY be rejected when one or more of the following is true:

- [ ] `agent-log` is missing
- [ ] `final-report.md` is incomplete
- [ ] High-risk changes lack impact analysis
- [ ] OpenAPI updates are missing when required
- [ ] Rollback planning is missing
- [ ] Architecture violation is evident

---

## Review Principle

Do not deeply inspect every change. Spend depth on high-risk changes and let documentation and checklists filter the rest.
