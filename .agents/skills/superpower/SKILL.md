---
name: superpower
description: >
  Standard execution engine for complex tasks. Forces a structured Brainstorm -> Plan -> Implement workflow.
  Use for Non-trivial or High-Risk tasks to minimize errors and optimize token usage.
---

# Superpower Execution Engine

You have Superpowers. Follow these expert-level workflows to ensure technical integrity and zero-trust implementation.

## 1. Core Workflow: BPI

Follow the `Brainstorm -> Plan -> Implement` sequence for any Non-trivial+ task.

### 🧠 Brainstorming
Before implementation, verify the explicit constraints, edge cases, and existing pattern in the codebase.
Ask only when the answer is still unclear after checking source files and triggered policies.

### 📝 Planning (`writing-plans`)
Create a plan at `docs/superpowers/plans/YYYY-MM-DD-task-name.md`.
- **Files Impacted**: List exact files and line ranges.
- **Step-by-Step**: Use `- [ ]` checkboxes for atomic steps.
- **Verification**: Define success criteria for EACH step (e.g., build output, log check).
- Even a small Non-trivial task needs a short plan; trivial prose-only edits do not.

### 🛠️ Implementation (`executing-plans`)
- Follow the Plan literally.
- Check off boxes (`- [x]`) as you complete tasks.
- If a step fails, STOP. Do not proceed to the next step.

## 2. Systematic Debugging

When a bug occurs, do not guess. Use the following protocol:
1. **Reproduction**: Define the exact steps and environment to trigger the bug.
2. **Hypotheses**: List at least 2 potential root causes.
3. **Verification**: Add logs or tests to confirm/refute hypotheses one by one.
4. **Surgical Fix**: Apply the minimum change needed to fix the root cause.

## 3. TDD (Optional)

Use Red-Green-Refactor when the change is easiest to verify with tests.
- Start from a failing test when a test seam exists.
- Keep the cycle short: fail, pass, then clean up.
- If TDD is not practical, use the strongest available verification instead.

## 4. Subagent Management

For High-Risk tasks, you may delegate to specialized sub-personas:
- **PM**: Requirement and constraint validation.
- **Architect**: Structural and dependency analysis.
- **Reviewer**: Post-implementation verification and protocol check.
- In a single-agent environment, treat these as internal review phases unless real subagents are available.
- Use actual subagents only when they are available and clearly reduce risk or duplication.

## 5. Behavioral Guards

- **Token Economy**: Read only the relevant section of a file or plan. Use `rg` to find the exact target.
- **Zero Trust**: Always verify assumptions by reading the source code.
- **Surgical Precision**: Never rewrite what isn't broken.
- **Priority**: `AGENTS.md` and triggered policy files override this skill when they are more specific.

## 6. Mandatory Check

Before closing a task, confirm:
- Build/Compile: Does the change avoid breaking the build or compile path?
- History: Is `docs/history.md` updated with one concise line?
- Work Log: Is a report saved under `docs/agent-log/YYYY-MM-DD-task-name-model-name/WORK_REPORT.md`?
- Protocol: Were `AGENTS.md` Surgical Edit Rules and triggered policy files followed?
- Applicability: Mark non-applicable items as `N/A` instead of guessing.

---
*Refer to AGENTS.md for the overarching Execution Contract.*
