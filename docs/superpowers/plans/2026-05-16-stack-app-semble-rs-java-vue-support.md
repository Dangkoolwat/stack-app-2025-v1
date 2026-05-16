# [PLAN] 2026-05-16-stack-app-semble-rs-java-vue-support

## Goal
- Update stack-app-2025-v1 agent docs so `semble_rs` AST-aware discovery can be used for Java/Vue structure mapping, while `code-review-graph` and Serena remain the validation path.

## Files
- `AGENTS.md`
- `docs/standards/semble-operation-guide.md`
- `docs/standards/semble-troubleshooting.md`
- `docs/history.md`
- `docs/frontend/agent-log/2026-05-16-semble-rs-java-vue-support/final-report.md`

## Steps
- [x] Verify `semble_rs` `tree --symbols`, `deps`, and `impact` on a Java file in this repo.
- [x] Update `semble-operation-guide.md` to describe Java/Vue structure mapping and the `impact` caveat.
- [x] Update `AGENTS.md` so the repo-level search order reflects the same Semble guidance.
- [x] Update troubleshooting and history/report records.
- [x] Run `git diff --check` and confirm the staged scope is limited to the intended docs.

## Verification
- `git diff --check`
- Manual reread of the updated routing lines
