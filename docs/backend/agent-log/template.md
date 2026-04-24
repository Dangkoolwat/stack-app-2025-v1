# Agent Work Log Template

## Purpose

This template defines the standard format for recording work performed by AI coding agents in this repository.

All non-trivial tasks must create a new log entry in:

docs/agent-log/

The goal is to maintain continuity between multiple agents and preserve context for future work.

For standard tasks, keep the entry short. Use only the fields that matter for later review.

---

## File Naming Convention

Log files should follow this format

YYYY-MM-DD-task-name.md

Example

2026-03-09-auth-refactor.md  
2026-03-10-dashboard-layout.md

---

## Log Entry Structure

### Date

YYYY-MM-DD

---

### Agent

Name or identifier of the AI system or contributor.

Example

Codex  
ChatGPT  
Claude  
Developer Name

---

### Task Title

Short descriptive title of the task.

Example

Dashboard layout refactor

---

### Goal

Describe the objective of the task.

Include

- what problem is being solved
- what outcome is expected

---

### Context

Describe the system state before the change.

Include information such as

- related features
- dependencies
- architectural constraints
- relevant previous agent logs

---

### Work Performed

Describe the actions taken to complete the task.

Example

1 Updated dashboard layout component  
2 Added new sidebar navigation  
3 Refactored feature module imports

---

### Files Modified

List all files that were changed.

Example

src/app/(dashboard)/layout.tsx  
src/components/sidebar/sidebar.tsx  
src/features/dashboard/dashboard-service.ts

---

### Architecture Impact

Describe whether the change affects project architecture.

If none

No architectural changes.

---

### Security Impact

Describe any security implications.

If none

No security impact.

---

### Verification

Explain how the change was verified.

Examples

- `./mvnw test`
- `./mvnw verify`
- `./mvnw clean package`
- Targeted unit/integration tests (JUnit5 / MockMvc)
- Manual API verification (if applicable)

If verification could not be performed, explain why.

---

### Risks

List any remaining risks or uncertainties.

If none identified

No significant risks identified.

---

### Next Suggested Tasks

List logical follow-up tasks.

Example

Implement dashboard analytics widgets  
Add role-based dashboard access

---

### Notes for Future Agents

Provide information that helps future agents continue the work safely.

Include

- assumptions made
- limitations
- important design decisions

---

## Minimal Task Log

Use this compact format when the task is standard and does not need a full narrative.

```md
## Token Check
- Task type:
- Files read:
- Approx. scope:
- Extra context:
- Result:
```

Example:

```md
## Token Check
- Task type: docs
- Files read: AGENTS.md, docs/analysis/2026-04-25-token-usage-analysis/measurement-plan.md
- Approx. scope: 2 files / 220 lines
- Extra context: graphify not used
- Result: Reduced graphify review to migration and cross-module tasks only
```
