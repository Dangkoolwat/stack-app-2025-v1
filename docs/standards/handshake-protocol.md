# Handshake Protocol

Before any Non-trivial, High-Risk, destructive, or file-modifying action, you MUST perform this handshake to obtain Architect approval.

---

## Step 1: Classification and Acknowledgment

State the following clearly:
- **Task classification:** Trivial / Non-trivial / High-Risk
- **Affected area:** Backend / Frontend / Shared Contract / Config / Docs
- **Triggered Policy Files:** List matched triggers from `AGENTS.md` Section 7.
- **Blast Radius:** Affected files and downstream impact.
- **Protocol Adherence:** `I will follow Surgical Edit Rules and all triggered policy files.`

---

## Step 2: Proposal for Approval

Provide:
- **Implementation Plan:** Step-by-step actions.
- **Refactor Integrity List (MANDATORY for Refactors):** Clearly list:
    - **삭제되는 항목 (Items to be Deleted)**
    - **유지되는 핵심 항목 (Core Items to be Maintained)**
    - *Note: "모든 리팩토링 제안 시, '삭제되는 항목'과 '유지되는 핵심 항목'을 명시적으로 리스트업하여 승인을 받아야 함."*
- **Surgical Diff & Comparison:** A detailed "Before vs. After" comparison of changed logic or documentation.
- **Validation Strategy:** How you will prove success.
    - Backend: `./mvnw verify` (or scoped `./mvnw test`)
    - Frontend: `npm run build` (or scoped `npm run lint`, `npm run test`)

---

## Step 3: Wait for Approval

STOP and wait for explicit approval (e.g., "Go", "Proceed", "Apply").

**A user request is a requirement, not an execution approval.**

---

## Monorepo-Specific Rules

- **Backend-only changes:** Blast radius analysis covers `src/main/java/` and `src/test/java/`.
- **Frontend-only changes:** Blast radius analysis covers `src/main/webapp/`.
- **Shared contract changes (DTO/API/validation):** Blast radius MUST cover both backend and frontend consumers.
- **Config changes (`.env`, `application*.yml`, `vite.config.*`):** Explicitly list all environments affected.
