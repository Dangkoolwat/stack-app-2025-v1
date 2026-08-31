# Risk-Based Model Routing & Verification Policy (stack-app-2025-v1)

This document defines the operational standards for evaluating model capability suitability before task execution and performing evidence-based verification after execution in the **stack-app-2025-v1** project (Spring Boot + Vue/Vite).

---

## 1. Three-Tier Risk Classification

Every task must be classified before taking any modifying action.

| Risk Level | Scope & Target Files | Pre-Analysis Requirement | Post-Verification Requirement |
| :--- | :--- | :--- | :--- |
| **High Risk** | · Java backend (Spring Boot, JPA, Liquibase, Security/OAuth2)<br>· Vue frontend state management (Pinia), global router, shared components<br>· Cross-boundary modifications (REST API contracts, DTOs, WebSockets)<br>· Database migrations, Docker/CI-CD configs, build scripts (`pom.xml`, `vite.config.*`)<br>· Complex bugs with uncertain root causes | **Mandatory Higher-Tier Reasoning Analysis** | **Mandatory Higher-Tier Verification** |
| **Medium Risk** | · Single-component Vue template/style adjustments<br>· Isolated service/controller helper methods within single module<br>· Unit test additions without contract changes | Optional Pre-Analysis (Current model analysis permitted) | **Mandatory Higher-Tier Verification** |
| **Low Risk** | · Read-only exploration, explanation, and symbol searches<br>· Obvious typos, comments, documentation fixes<br>· Formatting changes with no runtime/build impact | Assessment report may be omitted | **Higher-tier verification may be omitted** |

### ⚠️ Risk Escalation & Strict Caveats
1. **Cross-Boundary Escalation**:
   - Any modification touching both Spring Boot entities/controllers and Vue frontend API consumers is immediately escalated to **High Risk**.
2. **Shared State & Security Escalation**:
   - Any change to authentication (JWT, OAuth2), database schemas, or global state stores is immediately escalated to **High Risk**.

---

## 2. Pre-Task Model Suitability Gate

- Determine required capability tiers for implementation and verification models upfront.
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

## 3. Fallback & Explicit User Override Policy

1. **Prohibition of Unilateral Execution**:
   - In environments without automated multi-model handoff, agents MUST NOT pretend to switch models and MUST NOT begin high-risk modifications without explicit user instruction.
2. **Override Documentation & Compensatory Verification**:
   - If the user explicitly approves overriding model recommendations, the agent MUST record the override and reason in task logs (`docs/agent-log/` or task progress log).
   - The agent MUST execute rigorous compensatory checks (`./mvnw clean compile`, `npm run build`, unit tests).
   - Tasks lacking higher-tier verification MUST NOT be falsely reported as "Higher-Tier Verified".

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
- Scope Checked: (List of modified files and diff summary)
- Checks Executed: (Maven build, Vitest suite, npm bundle checks)
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
