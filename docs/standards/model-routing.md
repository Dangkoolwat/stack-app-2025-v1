# Risk-Based Model Routing & Verification Policy (stack-app-2025-v1)

This document defines the operational standards for evaluating model capability suitability before task execution and performing evidence-based verification after execution in the **stack-app-2025-v1** project (Spring Boot + Vue/Vite).

---

## 1. Three-Tier Risk Classification

Every task must be classified before taking any modifying action.

| Risk Level | Scope & Target Files | Pre-Analysis Requirement | Post-Verification Requirement |
| :--- | :--- | :--- | :--- |
| **High Risk** | · Java backend (Spring Boot, JPA, Liquibase, Security/OAuth2)<br>· Vue frontend state management (Pinia), global router, shared components<br>· Cross-boundary modifications (REST API contracts, DTOs, WebSockets)<br>· Database migrations, Docker/CI-CD configs, build scripts (`pom.xml`, `vite.config.*`)<br>· Complex bugs with uncertain root causes | **Mandatory Higher-Tier Reasoning Analysis** | **Mandatory Higher-Tier Verification** |
| **Medium Risk** | · Single-component Vue template/style adjustments<br>· Isolated service/controller helper methods within single module<br>· Unit test additions without contract changes | Optional Pre-Analysis (Current model analysis permitted) | **Mandatory Higher-Tier Verification** |
| **Low Risk** | · Read-only exploration, explanation, and symbol searches<br>· Obvious typos, comments, standard documentation fixes (excluding `AGENTS.md`, `docs/standards/`, `docs/workflow/`, `docs/operations/`, `docs/agent-log/`)<br>· Formatting changes with no runtime/build impact | Assessment report may be omitted | **Higher-tier verification may be omitted** |

### ⚠️ Risk Escalation & Strict Caveats
1. **Cross-Boundary Escalation**:
   - Any modification touching both Spring Boot entities/controllers and Vue frontend API consumers is immediately escalated to **High Risk**.
2. **Shared State & Security Escalation**:
   - Any change to authentication (JWT, OAuth2), database schemas, or global state stores is immediately escalated to **High Risk**.
3. **Operational Contract & Policy Boundary**:
   - Changes to `AGENTS.md`, `docs/standards/`, `docs/workflow/`, and `docs/operations/` are classified as Non-trivial and require Handshake / Two-Step High-Risk Lock. Changes to `docs/agent-log/` require Logging Policy adherence and are at least Non-trivial.

---

## 2. Pre-Task Model Suitability Gate

- Determine required capability tiers for implementation and verification models upfront.
- Capability Tiers are defined by functional reasoning capacity:
  - **High-Reasoning Tier**: Models with advanced hybrid reasoning, extended thinking budgets, deep architectural synthesis, and complex state-tracking capability.
  - **Standard / Cost-Efficient Tier**: Models optimized for local edits, simple transformations, and fast response cycles.
- Minimal read-only inspection is permitted to assess suitability, but NEVER modify files before suitability assessment.
- **High-Risk Lock**: High-risk tasks MUST NOT begin substantive modification without prior analysis from a higher-tier reasoning model.
- If the current model tier is insufficient, report the assessment and pause for user instructions:

```text
[Model Suitability Assessment]
- Task Risk: High / Medium / Low
- Current Model Tier: (Current model or tier)
- Recommended Implementation Model: Cost-Efficient / Standard / High-Reasoning
- Recommended Verification Model: Standard Verification / High Verification
- Reason: (e.g., Cross-boundary Spring Boot/Vue contract & OAuth2 security impact)
- Risk with Current Model: (e.g., Regression risk & compilation breakage)
- Recommended Options:
  1) Switch to higher-tier model for analysis/implementation/verification (Recommended)
  2) Perform read-only exploration / PoC analysis with current model only
  3) Proceed with current model upon explicit user approval (Override)
```

- **PoC Containment**: Findings from Option 2 (read-only PoC) are advisory only and MUST NOT be merged/deployed without approval.

---

## 3. Verification Independence & Fallback Override Policy

### 3.1 Verification Modes & Independence Standards
To prevent self-approval bias and maintain Zero Trust integrity, verification is strictly split into two modes:
1. **Independent Higher-Tier Verification**:
   - Executed in a separate task, subagent, or fresh session isolated from the implementation context (subsequent turns within the same task/session do NOT count as independent sessions).
   - Performed by a model possessing High-Reasoning / verification capability.
   - Grants official `Independent Higher-Tier PASS` status.
2. **Self Compensatory Verification**:
   - Executed within the same implementation session via local tool evidence (compiler, tests, linters, diffs).
   - Grants `Self Compensatory PASS` status.
   - **Critical Rule**: Self Compensatory results, while technically passing, represent local verification under user override and MUST NEVER be falsely claimed as "Independent Higher-Tier PASS" for High-Risk tasks. Self Compensatory PASS does not constitute official High-Risk completion approval without explicit user override.

### 3.2 Single-Session Fallback & Explicit User Override
1. **Prohibition of Unilateral Execution**:
   - In environments without automated multi-model handoff, agents MUST NOT pretend to switch models and MUST NOT begin high-risk modifications without explicit user instruction.
2. **Override Documentation & Compensatory Verification**:
   - If the user explicitly approves overriding model recommendations, the agent MUST record the override and reason in task logs (`docs/agent-log/` or task progress log).
   - The agent MUST execute rigorous compensatory checks (`./mvnw clean compile`, `npm run build`, unit tests).
   - Tasks lacking independent higher-tier verification MUST be explicitly reported as `Self Compensatory`.

---

## 4. Verification Model Scope & Authority

Verification models operate on objective evidence, never on ungrounded assumptions.

- **Prohibited Actions**:
  - Modifying source code, project configs, or docs
  - Git commits, pushes, or branch mutations
  - External write operations
- **Permitted Actions**:
  - Inspecting `git diff` and file changes
  - Reading execution logs and diagnostics
  - Running test and build commands (`./mvnw test`, `npm test`, `npm run build`)
  - Verifying runtime artifacts and browser console logs
- **Temporary Output Isolation**:
  - Any build artifacts generated during verification MUST be contained within designated output directories (`target/`, `dist/`, `scratch/`) without dirtying source control.

---

## 5. Verification Result Format & Feedback Loop

Upon completing verification, report using the standard format:

```text
[Verification Result]
- Status: PASS / FAIL / BLOCKED
- Verifier Mode: Independent Higher-Tier / Self Compensatory
- Implementation Task ID: (Task ID or Session ID)
- Verification Task/Session ID: (Task ID or Session ID)
- Commit or Working-Tree Ref: (git commit SHA or clean/dirty status)
- Scope Checked: (List of modified files and diff summary)
- PASS Evidence Summary:
  · Backend Build: (./mvnw clean compile exit code and result)
  · Frontend Build: (npm run build exit code and result)
  · Test Suite: (./mvnw test / npm test results)
  · Diff/Audit: (CRG detect_changes, Serena diagnostics result)
- Issues Found: (None or detailed defect list)
- Severity: None / Minor / Critical
- Recommended Action: (Declare complete or request fix)
- Re-Verification Required: (Y / N)
```

### 🔄 Re-Verification Loop
- If status is `FAIL` or `BLOCKED`:
  1. **Defect Report**: Verification model reports specific failure logs and root causes.
  2. **Remediation**: Implementation model applies surgical corrections.
  3. **Re-Execution**: Rerun build and test commands.
  4. **Re-Verification**: Verification model inspects new diff and test status until `PASS` is achieved.
- Tasks are NOT complete until final `PASS` status is confirmed.
