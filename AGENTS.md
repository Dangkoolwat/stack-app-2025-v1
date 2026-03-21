# AGENTS.md

## Language Policy (MANDATORY)

- All shared documentation under `docs/` MUST be written in English.
- All source code comments SHOULD be written in Korean.
- All `agent-log` files MUST be written in Korean.

---

## Documentation Structure Policy (MANDATORY)

All project documentation under `docs/` MUST be organized by **purpose**, not by temporary topic.

### Recommended top-level structure

docs/
  backend/
  frontend/
  standards/
  workflow/
  operations/

---

## Priority Order

1. AGENTS.md
2. docs/standards/
3. docs/workflow/
4. docs/operations/

---

## Core Execution Flow (MANDATORY)

All non-trivial work MUST follow:

1. Problem Analysis
2. Proposal
3. Self-Check Gate
4. Implementation Plan
5. Implementation
6. Verification & Documentation

### Exception (IMPORTANT)

If the task is trivial or explicitly defined,
agents MAY skip Proposal and use a simplified flow:

Problem → Plan → Implementation → Verification

---

## Self-Check Gate (MANDATORY)

- [ ] Architecture compliance
- [ ] No hidden breaking changes
- [ ] Rollback possible
- [ ] Test strategy defined
- [ ] Security impact reviewed
- [ ] Config / dependency impact checked
- [ ] Cache safety checked (if caching involved)
- [ ] OpenAPI impact checked (if API contract changed)

---

## Agent Log File Structure (MANDATORY)

docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/

Required files:

- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

All contents MUST be written in Korean.

---

## Required Cross-Cutting Documents

### Standards
- docs/standards/environment-variables-guideline.md
- docs/standards/configuration-externalization-guideline.md
- docs/standards/java-class-comment-guideline.md
- docs/standards/properties-template.java.md
- docs/standards/configuration-template.java.md
- docs/standards/cache-safety-guideline.md

### Workflow
- docs/workflow/git-workflow.md
- docs/workflow/pr-review-checklist.md
- docs/workflow/ci-automation-rules.md
- docs/workflow/github-actions.md

### Operations
- docs/operations/environment-variables.md

---

## Golden Rule

"Make it correct, safe, and understandable first. Then optimize."
