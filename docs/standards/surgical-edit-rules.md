# Surgical Edit Rules

Apply these rules to every file modification to ensure system integrity and token economy.

---

## 1. Edit Methods

- **STRICTLY FORBIDDEN:** Do not use `write_to_file` (overwrite mode) to update existing files.
- **MANDATORY:** Use `replace_file_content` or `multi_replace_file_content` for targeted edits.
- **NO REWRITES:** Do not rewrite whole files, functions, or classes unless explicitly approved.
- **Serena Priority:** For code symbol modifications, prefer `replace_symbol_body` or `insert_after_symbol` over raw text replacement.

---

## 2. Integrity Preservation

- Do not delete, truncate, or refactor unrelated code.
- Preserve exception handling, edge-case logic, and existing UI controls.
- **Verify before Edit:** Read current file state and exact target lines before applying any fix.
- **Refactor Preservation Rule:** When refactoring documentation or core logic, you MUST explicitly verify that no established invariants, tools, or safety guards are removed. Deleting established patterns without explicit justification is a **Protocol Violation**.

---

## 3. Technical Constraints

- **Path Safety:** Use only project-relative paths. No hallucinated directories.
- **No Speculative Abstraction:** No new interfaces, helpers, or utility classes unless requested.
- **Language Policy:** Source code comments MUST be written in Korean per the Korean Comment Guideline (`docs/standards/java-class-comment-guideline.md`). For major changes and safety guards, add one short Korean comment explaining *why* it is needed.

---

## 4. Isolated Local Logic Definition

A task is "isolated local logic" only if ALL are true:
- One file only.
- No shared state, Service, or Store dependencies.
- No public API, DTO, or contract impact.
- No downstream caller behavior change.

**Backend:** No changes to `@Service`, `@Repository`, `@Configuration`, or shared DTOs.
**Frontend:** No changes to Pinia stores, composables used by >1 component, or router guards.

---

## 5. Protocol Document (`AGENTS.md`) Modification Rules

Modifying `AGENTS.md` is classified as the **Highest Difficulty and Highest Risk** task.

When instructed to modify `AGENTS.md`, agents MUST follow this Ironclad Protocol:
1. **Mandatory History Audit & Sequential Thinking:** Review `docs/history.md` and past agent logs. Use `[Reasoning]` block before applying changes.
2. **Zero Context Contamination:** Arbitrary modification or "cleaning up" without permission is STRICTLY FORBIDDEN.
3. **Lazy-Loading Architecture:** `AGENTS.md` must remain lightweight. Detailed guides go to `docs/standards/`.
4. **Token-Efficient & Unambiguous:** Terse, concise English. 100% clear and deterministic.
5. **Detailed Accountability Report:** List every Addition, Modification, and Deletion. State: "No existing rules were accidentally omitted."
