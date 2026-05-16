# [PLAN] 2026-05-16-stack-app-semble-rs-default-rules

## Goal
- Align stack-app-2025-v1 agent docs with the same conditional `semble_rs` defaults used in the Swift projects, but adapted for Spring Boot 3 + Vue 3.
- Keep the existing `Serena`, `rg`, and `code-review-graph` rules, while making `plan` the default when the target is unclear and `digest` the default for noisy logs.

## Files
- `AGENTS.md`
- `docs/workflow/agent-workflow-pipeline.md`
- `docs/standards/semble-operation-guide.md`
- `docs/standards/tooling-policy.md`
- `docs/standards/serena-guide.md`
- `docs/history.md`
- `docs/frontend/agent-log/2026-05-16-semble-rs-default-rules/final-report.md`

## Steps
- [x] Verify `semble_rs` support on a Java source file and a Vue source tree in this repo.
- [x] Update `AGENTS.md` with the new default values and Java/Vue fallback note.
- [x] Update the workflow and standards docs so the same order is repeated in the downstream guides.
- [x] Update history and write a frontend work report for the Vue-facing documentation change.
- [x] Run `git diff --check` and reread the touched policy lines for context hygiene.

## Verification
- `semble_rs deps src/main/java/com/daangcool/stack/web/rest/SettingsResource.java`
- `semble_rs tree --symbols src/main/webapp/app/entities/settings`
- `git diff --check`
