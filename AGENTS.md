# AGENTS.md

## Language Policy (MANDATORY)

- All shared documentation under `docs/` MUST be written in English.
- Exception: system-wide review or architecture-wide review documents under `docs/analysis/` MAY be written in Korean when the user explicitly requests Korean.
- All source code comments SHOULD be written in Korean.
- All `agent-log` files MUST be written in Korean.

---

## Formatting Policy (MANDATORY)

- All agent logs and shared documentation MUST NOT use bolding (`**`) or emojis.
- Maintain a clean, professional, and plain-text-oriented style.
- This formatting restriction applies to repository documents under `docs/` and `agent-log` outputs.
- Chat or tool-facing responses MAY follow the runtime/client formatting rules unless the user explicitly requests the same plain-text restriction there as well.

---

## Documentation Structure Policy (MANDATORY)

All project documentation under `docs/` MUST be organized by purpose.

### Structure

docs/
  backend/
  frontend/
  analysis/
  knowledge/
  standards/
  workflow/
  operations/
  release-notes/

### Folder Roles

- standards: mandatory engineering rules (MUST / MUST NOT)
- workflow: collaboration, review, CI, delivery rules
- operations: execution HOW TO (run, deploy, troubleshoot)

### Priority Order (Source of Truth)

1. User instructions (Current task)
2. AGENTS.md (Local rules)
3. docs/ (Standards, Workflow, Operations)
4. Code, tests, and configuration

### Authority Clarification

- Files under `docs/backend/` and `docs/frontend/` are reference documents unless a higher-priority guide explicitly promotes one of them as authoritative.
- Files under any `agent-log/` directory are historical task records and MUST NOT be treated as current policy.
- `docs/knowledge/` captures lessons learned and rationale. It is reusable context, but it MUST NOT override the priority order above.
- If two documents conflict, agents MUST follow the higher-priority document and record the mismatch in the current agent log.
- If the conflict significantly affects the task direction, agents MUST ask the user for clarification before proceeding.
- If runtime-level global instructions and this repository `AGENTS.md` differ, agents MUST treat this repository `AGENTS.md` as the local source of truth for work performed in this repository.

---

## Guide Document Ownership Policy (MANDATORY)

All files under `docs/standards/`, `docs/workflow/`, and `docs/operations/` are READ-ONLY for agents.

- Agents MUST NOT directly create, modify, or delete any guide document in these directories.
- If an agent identifies an issue (error, inconsistency, missing content, improvement opportunity) in any guide document:
  1. Document the finding in the agent log (`final-report.md` or dedicated section).
  2. Include: file path, line number(s), issue description, and suggested fix.
  3. Label the finding clearly as "Guide Document Feedback".
- Only the project owner (user) may apply changes to guide documents.
- Exception: The user may explicitly instruct an agent to modify a specific guide document in the current task scope.

---

## Core Execution Flow

All work MUST follow the tiered process system based on complexity:

### Tier 1: Trivial

Direct implementation for minor changes.

- Criteria: Single-file changes, < 30 lines of code, local logic changes only.
- Flow: Problem → Implementation → Verification
- Documentation: Minimal recording. A single `final-report.md` is sufficient, or chat-only record if extremely minor.

### Tier 2: Standard

Full process for feature developments and bug fixes.

- Criteria: Most development tasks.
- Flow: Problem Analysis → Proposal → Self-Check → Plan → Implementation → Verification
- Documentation: **Lightweight Mode** (`task-log.md`) is the default. Use Full Mode (6 files) only if explicitly requested or for high-risk changes.

### Tier 3: Critical

Enhanced review for high-impact changes.

- Criteria: Security, infrastructure (pom.xml, config, etc.), database schema, or breaking API changes.
- Flow: Tier 2 + Mandatory Peer Review/Approval before Implementation.
- This approval rule overrides any default agent tendency toward autonomous end-to-end execution.

### Emergency Protocol

In extreme cases where immediate action is required to restore system functionality, agents MAY bypass the standard proposal phase.

- Applicable Scenarios: Build failure recovery, critical runtime crashes, accidental exposure of sensitive data, or environment-blocking issues.
- Requirements: Modifications MUST be minimal and focused solely on resolving the emergency. A detailed report explaining the cause, fix, and verification results MUST be provided immediately after the intervention.

---

## Behavioral Protocols (Senior Architect Edition)

To ensure system integrity and maintainability, agents MUST adhere to these operational principles:

### 1. Read-Before-Write Protocol

- Action: Execute `grep`, `cat`, or `ls` to fully understand the target block and its caller functions BEFORE writing a single line of code.
- Goal: Maintain context preservation and avoid breaking hidden dependencies.

### 2. Surgical Precision

- Action: Touch only the lines necessary to satisfy the request.
- Constraint: Do not "polish" adjacent code, fix unrelated linter warnings, or change formatting unless explicitly asked.
- Reversion: If unrelated lines were accidentally modified, revert them to their original state immediately.

### 3. No Speculative Abstraction

- Action: Implement the minimum required logic.
- Constraint: Never remove default parameters or change global patterns for a specific local case unless it is part of a deliberate refactoring task.

### 4. Data Safety

- Action: Never overwrite or delete user-generated data without explicit approval.
- Precaution: For high-risk operations, use temporary paths or backups first.

### 5. Proportional Validation

- Action: Keep validation effort proportional to the change.
- Principle: Do not dump long logs; report success/failure and key errors only.

---

## Interaction Rule (MANDATORY)

When receiving a task:

1. Agents MUST start with a proposed solution direction (hypothesis)
2. Agents SHOULD provide:
   - one recommended solution (primary direction)
   - 1–2 brief alternative options (if relevant)
3. Agents MUST NOT ask "what should I do?" or present only open-ended options
4. Agents MUST ask for confirmation after proposing direction:
   - "Is this the correct direction?"
5. Only after confirmation:
   - proceed with detailed plan and implementation

### Exception (Interaction)

- If the task is trivial or explicitly defined, agents MAY proceed directly to implementation
- "Explicitly defined" means the requested outcome and safe scope are already clear enough that an additional confirmation turn would not reduce risk.
- If the task still affects shared contracts, security, infrastructure, persistence, or cross-cutting behavior, agents MUST treat it as approval-sensitive even when the user request is otherwise explicit.

### Response Style (RECOMMENDED)

1. **Core Point First**: Start with the most important information or result.
2. **Conciseness**: Keep responses short and direct. Avoid repeating the entire task history.
3. **Proportional Reporting**: For tiny tasks, keep the report tiny. For complex ones, provide a structured summary.
4. **Actionable Verification**: Report success/failure and the key error only. Do not dump long logs unless requested.

---

## Git Workflow & Commit Policy (MANDATORY)

All agents MUST follow the Conventional Commits standard (v1.0.0).

- Format: `<type>(<scope>): <subject>`
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`
- Details: See [Commit Convention Guide](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/commit-convention.md)
- Body: MUST include the Agent Name and a link to the corresponding `agent-log` path.
- Example: `fix(security): patch transitive vulnerabilities in parent POM (see docs/backend/agent-log/2026-03-25-cve-patch/)`

### Commit Execution Rule

- Agents MUST NOT execute `git add`, `git commit`, or `git push` commands automatically after completing implementation.
- The correct flow is:
  1. Complete implementation and verification.
  2. Report completion to the user (via final-report or equivalent).
  3. Wait for the user's explicit instruction to commit.
- Only after the user confirms: the agent may execute the commit using the approved message format.
- Exception: The user may pre-authorize commits for a specific task scope (e.g., "commit when done").

---

## Knowledge Management (KI) (MANDATORY)

### Search First

Before creating a new KI, agents MUST search `docs/knowledge/` for existing items on the same topic. If a relevant KI exists, update it instead of creating a duplicate. When the impact is unclear or the task touches shared or high-centrality areas, agents MAY also consult the supplemental Graphify resources for architectural context.

### Creation Rule

When a task involves complex troubleshooting, non-obvious logic, or critical architectural decisions:

- Action: Agents MUST create a Knowledge Item (KI).
- Location: `docs/knowledge/YYYY-MM-DD-topic-name.md`
- Goal: To prevent recurring issues and shared context across different agents/time.
- Metadata: MUST include the same Metadata header as `agent-log` files.

---

## Agent Skills (RECOMMENDED)

- Use project-local skills under `.agents/skills/` as a primary reference (e.g., `jhipster-vue-standards`, `jpa-expert`).
- If a global skill conflicts with local guidance, local guidance is preferred.
- Consider installing new tools or workflows locally with `npx skills add` when relevant.
- `.agents/skills/karpathy-guidelines/SKILL.md` is recommended for behavioral guidance: surgical changes, simplicity, and explicit assumptions.

---

## Supplemental Architecture Analysis (Graphify)

This project uses `graphify` as a supplemental architecture analysis tool. It helps agents understand dependency relationships and change impact, but it does not replace `AGENTS.md`, `docs/standards/`, `docs/workflow/`, `docs/operations/`, tests, or source code.

### 1. Knowledge Graph Resources

- Analysis Report: docs/graphify/GRAPH_REPORT.md
  - Snapshot report for architectural orientation and impact analysis.
- Graph Data: docs/graphify/graph.json
  - Raw graph snapshot for relationship lookup.
- Visualization: docs/graphify/graph.html
  - Interactive visual map of the system architecture.

### 2. Usage Guidelines

- Context Check: When a task may ripple across modules, shared services, or other high-centrality areas, agents SHOULD review `GRAPH_REPORT.md` before editing.
- Architectural Preservation: When modifying components that appear central in the graph, agents SHOULD treat the graph as an additional impact signal and still follow the Global Impact Review process.
- Update: If there is a significant change in the code structure, maintain the graphs in docs/graphify in the latest state by running the command: graphify update . && rm -rf docs/graphify/* && mv graphify-out/* docs/graphify/ && rm -rf graphify-out
- MCP Tool Integration: Agents MAY use the `graphify` MCP tool when it helps answer relationship or impact questions faster.

---

## Self-Check (MANDATORY)

### Correctness

- [ ] Requirements addressed
- [ ] Unit/Integration tests passing
  - Recommended for unit/non-IT validation: `export $(xargs < .env) && ./mvnw clean test`
  - Recommended for full validation including `*IT`: `export $(xargs < .env) && ./mvnw clean verify`
- [ ] API contracts maintained
- [ ] Architecture compliance
- [ ] Cross-cutting consistency verified (no partial pattern migration remains)

### Safety

- [ ] No hidden breaking changes (Rollback possible)
- [ ] Security impact reviewed (No sensitive data exposure)
- [ ] Config / dependency impact checked
- [ ] Cache safety checked (if used)

### Understandability

- [ ] Code comments in designated language (Korean)
- [ ] Documentation updated in English (`docs/`)
- [ ] Meaningful commit messages (Conventional Commits)
- [ ] Agent logs updated with correct metadata

---

## Agent Log (MANDATORY)

### Location

docs/{backend|frontend}/agent-log/YYYY-MM-DD-task-name/

### File Metadata Requirement

All files under `agent-log/` MUST include a metadata header (YAML format) at the top.

### Logging Modes

1. **Lightweight Mode (Preferred for Tier 1 & 2)**:
   - Use a single `task-log.md` (or `final-report.md` for Tier 1) containing a concise summary of analysis, implementation, and verification.
2. **Full Mode (Mandatory for Tier 3)**:
   - Use the 6-file set below for high-impact or complex changes.

### Full Mode Files

- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

### Archiving Policy

To maintain efficiency and minimize token overhead, old logs should be managed periodically:
- **Frequency**: Every month or when the number of task folders exceeds 20.
- **Action**: Move folders older than 30 days to `docs/archive/agent-log/{backend|frontend}/`.
- **Note**: Ensure critical Knowledge Items (KIs) are extracted before archiving.

### Content Guide (Full Mode)

- problem-analysis.md: 문제 현상 / 재현 / 원인 / 영향
- proposal.md: 최소 2개 방안 / 선택 이유 / 리스크
- self-check.md: 아키텍처 / 보안 / 영향 / 테스트
- implementation-plan.md: 단계 / 변경 파일 / 테스트
- walkthrough.md: 구현 흐름 / 핵심 포인트
- final-report.md: 수행 에이전트 / 요약 / 이유 / 결과

---

## Cross-Cutting Rules

### Configuration & Env

- MUST follow environment-variables-guideline
- MUST NOT hardcode secrets

### Cache

- MUST follow cache-safety-guideline
- Redis application cache MUST use DTO/read-model payloads by default
- Agents MUST NOT introduce new Redis cache entries for JPA entities or Hibernate-managed graphs
- Any new cache MUST define payload, key, TTL, invalidation owner, and fallback path

### Code Comments

- MUST follow java-class-comment-guideline

---

## Required Documents

### Standards

- docs/standards/environment-variables-guideline.md
- docs/standards/configuration-externalization-guideline.md
- docs/standards/java-class-comment-guideline.md
- docs/standards/cache-safety-guideline.md
- docs/standards/commit-convention.md
- docs/standards/naming-convention-checker.md
- docs/standards/jpa-entity-standards.md

### Workflow

- docs/workflow/git-workflow.md
- docs/workflow/pr-review-checklist.md
- docs/workflow/ci-automation-rules.md

### Knowledge

- docs/knowledge/ (Repository for lessons learned and architectural insights)

### Operations

- docs/operations/environment-variables.md
- docs/operations/testing-guideline.md
- docs/operations/cache-operations.md

---

## Spring Boot 4 Testing Standards (MANDATORY)

To ensure high performance and security in the modern Spring Boot 4 environment, all agents MUST follow the established testing patterns.

- Refer to: [Spring Boot 4 Testing Standards](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/spring-boot-4-testing-standards.md)

---

## Analysis Repository (MANDATORY)

When an agent is asked to perform a deep analysis (not tied to a specific code-changing task):

- Location: `docs/analysis/YYYY-MM-DD-agentName/`
- Metadata: MUST follow the same Metadata header rule as `agent-log` files.
- Goal: To build a persistent knowledge base of system insights and research findings.
- Use `docs/analysis/` for repository-wide reviews, policy audits, architecture studies, and similar non-implementation work.
- Do NOT create a parallel `docs/{backend|frontend}/agent-log/...` set for the same task unless the user also requested implementation work that requires the standard execution log flow.

---

## Global Impact Review (MANDATORY)

Required when changing core configurations, security, cache, or shared contracts.

- Refer to: [Global Impact Review](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/global-impact-review.md)

## Consistency Sweep Rule (MANDATORY)

Mandatory check for renaming, moving, or modifying shared types and method signatures.

- Refer to: [Consistency Sweep Rule](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/standards/consistency-sweep-rule.md)

---

## Golden Rule

"Make it correct, safe, and understandable first."

1. **Simplicity Over Complexity**: Prefer the smallest safe change.
2. **Context Over Guesswork**: Read before you write.
3. **Verifiability Over Assumption**: Verify before you claim success.
4. **Communication Over Silence**: State assumptions and ask when ambiguous.
