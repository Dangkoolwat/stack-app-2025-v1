# Validation, Recovery, and Handoff Standards

Defines the detailed procedures for verifying work, handling failures, and performing handoffs.

---

## 1. Evidence-Based Success

Do not claim build, compile, test, or runtime success unless actually verified. Cite specific tool output such as exit code 0 or test pass counts. If tests are missing, do not claim test coverage.

- **Backend:** `./mvnw verify` (full) or `./mvnw test` (unit only). Load `.env` when running Maven tests.
- **Frontend:** `npm run build` (compile check) or `npm run test` (unit tests).
- **Shared contract changes:** Verify both backend and frontend build success.

---

## 2. Failure Recovery & Build Halt

- **Build Failure Halt:** If build or test fails after a change, stop and report the exact error. Do not modify additional files without approval.
- **Recovery Limit:** After any failure, one local recovery attempt is allowed ONLY if:
    - Failure is directly caused by the latest approved change.
    - Fix is within the approved file list and scope.
    - No new policy or protected-area trigger is introduced.
    - Recovery does not expand the task beyond the approved proposal.
- **Failure Boundary:** If the first recovery attempt fails, STOP and report. High-Risk work requires Architect approval before *any* recovery.
- **Atomic Rollback:** If the second attempt fails, execute `git checkout -- <file>` immediately.

---

## 3. Handoff Protocol

For paused or transferred work, provide a compact, state-oriented handoff (~100 tokens):
- Changed files / current state.
- Decisions / rationale (bullets).
- Assumptions / prerequisites.
- Verification status and remaining risks.
- Next steps.

---

## 4. High-Risk & Integrity Guardrails

To prevent model runaways, the following hard guardrails apply universally:

- **[Sequential Thinking / Stop-and-Think]**: Before modifying any code, output a `[Reasoning]` block declaring: *"Why am I changing this line, and how is the existing logic preserved?"*
- **[Compile-Gated Verification]**: Claiming success via text without log proof (Exit code 0) is a critical violation.
- **[Anti-Truncation Rule]**: NEVER rewrite a whole function or class. Only patch exact lines using surgical edits.
- **[No Implicit Deletion]**: NEVER delete, truncate, or simplify existing code (exception handling, safety guards, UI elements) just to "clean it up". Leave unrequested lines exactly as they are.

---

## 5. Error Recovery & Incident Flow (오류 복구 절차)

When an agent realizes it has made a wrong judgment, false positive, or architectural mistake:
1. **Stop & Acknowledge**: Immediately stop the current action and acknowledge the specific error to the user.
2. **Atomic Rollback**: Revert any uncommitted changes to the repository's previous stable state using `git checkout -- <file>`.
3. **Formal Incident Report**: Generate an official Incident Report in the `docs/operations/incident-reports/` directory using the `_template.md` format. Do NOT skip this step if the error involves a Zero-Trust violation, False Positive, or Codebase Pollution.
4. **Re-evaluate**: Restart the analysis from scratch, strictly applying the "Read Before Write" and "Physical Verification" guidelines.
