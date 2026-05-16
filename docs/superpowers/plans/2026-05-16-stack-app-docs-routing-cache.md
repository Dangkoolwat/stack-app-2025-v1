# [PLAN] 2026-05-16-stack-app-docs-routing-cache

## Goal
Add a repo-local docs routing cache for stack-app-2025-v1 so agents can reuse the docs layout without rescanning the full tree.

## Files Impacted
- [ ] `AGENTS.md`
- [ ] `docs/analysis/2026-05-16-stack-app-docs-routing-cache/report.md`
- [ ] `scripts/sync-docs-routing-cache.sh`
- [ ] `docs/history.md`
- [ ] `docs/backend/agent-log/2026-05-16-stack-app-docs-routing-cache/WORK_REPORT.md`

## Steps
- [ ] Confirm the current docs layout and the repo's policy references.
- [ ] Add the cache doc and repo-local sync script with a `--check` path.
- [ ] Update `AGENTS.md`, `docs/history.md`, and the work report with minimal cache-only notes.
- [ ] Verify the generated cache and diff for scope and formatting.

## Verification
- `bash scripts/sync-docs-routing-cache.sh --check`
- `git diff --check`
- Manual re-read of the edited sections for cache-only scope
