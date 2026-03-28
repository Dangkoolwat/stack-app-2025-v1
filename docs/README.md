# Project Documentation Guide

## Purpose

Help humans and agents decide where to start, which document type to follow, and how to resolve conflicts across the `docs/` tree.

---

## Start Here

1. Read `AGENTS.md` first
2. Then follow `docs/standards/`
3. Then follow `docs/workflow/`
4. Use `docs/operations/` for execution HOW TO

---

## Start Rule by Task Type

- New task or unclear task → read `AGENTS.md` first
- Tier 1 (Trivial): single-file, < 10 lines of code → follow Tier 1 flow in `AGENTS.md`
- Tier 2 (Standard): features or bug fixes → follow Tier 2 flow in `AGENTS.md`
- Tier 3 (Critical): security, infra, or breaking changes → follow Tier 3 flow (Peer Review required)
- Implementation rule or engineering pattern → read `docs/standards/`
- Review, PR, CI, merge, or delivery process → read `docs/workflow/`
- Local setup, deployment, runtime operation, or troubleshooting → read `docs/operations/`

If more than one document is relevant, use the documented priority order and do not let `workflow` or `operations` override a stricter `standards` rule.

---

## Key Standards (MANDATORY)

- Formatting: Do NOT use bold (`**`) or emojis in documentation or agent logs.
- Metadata: All agent logs must include `agent`, `created_at`, and `language` in the YAML header.
- Commit Messages: MUST follow [Conventional Commits](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/commit-convention.md) (enforced by `commitlint`).

---

## Documentation Structure

```text
docs/
  backend/
  frontend/
  analysis/
  knowledge/
  standards/
  workflow/
  operations/
  release-notes/
```

---

## Folder Guide

### `standards/`

Mandatory engineering rules and reusable implementation patterns.

### `workflow/`

Collaboration, review, CI, and delivery process.

### `operations/`

Execution guides, runtime HOW TO, and runbooks.

### `knowledge/`

Reusable lessons learned and architectural context.
These documents help explain why a decision was made, but they do not override `AGENTS.md`, `standards/`, `workflow/`, or `operations/`.

### `backend/` and `frontend/`

Domain-specific reference notes and examples.
Unless a document in these folders is explicitly referenced by `AGENTS.md` or a guide document in the priority chain, treat it as supporting context rather than an authoritative rule source.

### `agent-log/`

Task history, implementation trace, and audit evidence.
Agent logs are important for grounding and traceability, but they are not policy documents and must not be used to override current standards.

---

## Rule of Priority

1. `AGENTS.md`
2. `docs/standards/`
3. `docs/workflow/`
4. `docs/operations/`

If another document outside this chain appears to conflict with the priority order, follow the higher-priority source and record the mismatch in the current agent log.

---

## Authority Matrix

| Document location | Primary role | Can define mandatory rules? | Notes |
|---|---|---|---|
| `AGENTS.md` | Repository-wide execution contract | Yes | Highest priority |
| `docs/standards/` | Engineering rules | Yes | Must/Must Not policies |
| `docs/workflow/` | Process rules | Yes | Cannot weaken `standards/` |
| `docs/operations/` | Runbooks and procedures | Yes | Execution HOW TO only |
| `docs/knowledge/` | Shared lessons learned | No | Context and rationale |
| `docs/backend/`, `docs/frontend/` | Domain reference notes | No, unless explicitly promoted | Useful orientation material |
| `docs/*/agent-log/` | Task traceability | No | Historical evidence only |

---

## Rule Interpretation

- MUST = mandatory, no exception unless `AGENTS.md` explicitly allows one
- SHOULD = recommended default, can be overridden with a recorded reason
- MAY = optional

---

## Tip

If unsure, start from `AGENTS.md`, then open the most relevant document in `docs/standards/`.
