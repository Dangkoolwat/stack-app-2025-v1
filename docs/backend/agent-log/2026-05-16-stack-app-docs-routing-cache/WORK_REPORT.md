# [WORK LOG] 2026-05-16-stack-app-docs-routing-cache

## 1. Task Overview
- **Task Classification**: Non-trivial
- **Goal**: Add a repo-local docs routing cache for stack-app-2025-v1 so agents can reuse the folder layout without rescanning the full tree
- **Status**: Completed

## 2. Analysis & Implementation
- **Files Impacted**:
  - [x] `AGENTS.md`
  - [x] `docs/analysis/2026-05-16-stack-app-docs-routing-cache/report.md`
  - [x] `scripts/sync-docs-routing-cache.sh`
  - [x] `docs/history.md`
  - [x] `docs/backend/agent-log/2026-05-16-stack-app-docs-routing-cache/WORK_REPORT.md`
- **Decisions Made**:
  - Kept `AGENTS.md` and the standards docs as the source of truth.
  - Put the cache in `docs/analysis/` because it is a repo-local summary artifact, not policy.
  - Kept the cache short and generated from a repo-local script with a `--check` mode.
- **Assumptions**:
  - The repo should keep its current `docs/backend/agent-log/`, `docs/frontend/agent-log/`, `docs/analysis/`, and `docs/workflow/` conventions unchanged.

## 3. Verification & Results
- **Evidence-Based Success**: `bash scripts/sync-docs-routing-cache.sh --check` passed after regeneration and `git diff --check` passed.
- **Manual Verification**: Re-read the new cache doc and confirmed it only summarizes the folder map and does not replace the source policy docs.
- **Known Issues/Risks**: If the docs tree changes again, the script must be rerun to refresh the cache; this is intentional and keeps the cache honest.

---
## 📜 History Summary (Copy-paste to docs/history.md)
`2026-05-16 | stack-app-docs-routing-cache | added a docs-analysis cache and repo-local sync script for the docs layout`
