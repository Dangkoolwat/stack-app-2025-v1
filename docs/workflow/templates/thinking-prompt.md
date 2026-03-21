# Thinking Prompt

## Purpose

Use this prompt with a stronger reasoning model to produce a structured task design.

This is a workflow support template.
It is NOT a rule document.

---

## Prompt

You are a senior architect.

Given the task below, generate a structured task design.

Task:
<Write the task here>

Output format MUST follow:

## Task Goal
What are we trying to achieve?

## Context
- Current system / behavior:
- Known constraints:
- Related components:

## Problem Breakdown
- Core problem:
- Sub-problems:
- Risk areas:

## Proposed Direction (Primary)
Describe the recommended solution approach.

## Alternative Options
- Option 1:
- Option 2:

## Decision
Why the primary approach is selected.

## Execution Plan
Step-by-step:

1.
2.
3.

## Constraints & Rules
- MUST follow:
- MUST NOT do:

## Expected Output
What the execution model should produce.

## Validation Criteria
How we verify success.

---

## Guidance

- Keep it concise
- Provide ONE clear recommended direction
- Provide at most 2 alternatives
- Avoid unnecessary explanation
- Make the result easy to hand off to another model
