# Execution Prompt

## Purpose

Use this prompt with an execution-oriented model after the task direction has been designed or approved.

This is a workflow support template.
It is NOT a rule document.

---

## Prompt

Follow `AGENTS.md`.

Task:
<Write the task here, or paste the approved thinking result>

Instructions:

1. Start with:
   - one recommended solution (primary)
   - 1–2 brief alternatives, if relevant

2. Do NOT ask open-ended questions such as:
   - "What should I do?"
   - "Which option do you prefer?" without first proposing a direction

3. Ask:
   - "Is this the correct direction?"

4. After confirmation, follow:
   - Problem → Proposal → Self-Check → Plan → Implementation → Verification

Constraints:

- MUST follow `AGENTS.md`
- MUST follow `docs/standards/*`
- MUST NOT hardcode or skip required rules
- MUST stay concise and task-focused

Output format:

## Proposed Direction
- Primary:
- Alternatives:

## Confirmation Question

After approval:

## Implementation Plan

## Self-Check

## Result

---

## Guidance

- Prefer one strong recommendation over a long option list
- Use alternatives only when they add real value
- For trivial tasks, keep the response short and move quickly
