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

### Priority Order

1. AGENTS.md
2. docs/standards/
3. docs/workflow/
4. docs/operations/

### Authority Clarification

- Files under `docs/backend/` and `docs/frontend/` are reference documents unless a higher-priority guide explicitly promotes one of them as authoritative.
- Files under any `agent-log/` directory are historical task records and MUST NOT be treated as current policy.
- `docs/knowledge/` captures lessons learned and rationale. It is reusable context, but it MUST NOT override the priority order above.
- If two documents conflict, agents MUST follow the higher-priority document and record the mismatch in the current agent log.
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

- Criteria: Single-file changes, < 10 lines of code, no logic changes.
- Flow: Problem → Plan → Implementation → Verification

### Tier 2: Standard

Full process for feature developments and bug fixes.

- Criteria: Most development tasks.
- Flow: Problem Analysis → Proposal → Self-Check → Plan → Implementation → Verification

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
- Synchronization: If the repository workflow provides an `updateGraphify` command, agents SHOULD run it after significant structural changes so the graph artifacts stay aligned with the codebase.
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

All files under `agent-log/` MUST include a metadata header (YAML format) at the top:

```markdown
---
agent: [Agent Model Name]
created_at: YYYY-MM-DD (요일)
language: [en|ko]
---
```

Files:

- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

### Content Guide

problem-analysis.md:

- 문제 현상 / 재현 / 원인 / 영향

proposal.md:

- 최소 2개 방안 / 선택 이유 / 리스크

self-check.md:

- 아키텍처 / 보안 / 영향 / 테스트

implementation-plan.md:

- 단계 / 변경 파일 / 테스트

walkthrough.md:

- 구현 흐름 / 핵심 포인트

final-report.md:

- 수행 에이전트 (Agent Model)
- 요약 / 이유 / 영향 / 결과

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

To ensure high performance and security in the modern Spring Boot 4 environment, all agents MUST follow these patterns:

### Infrastructure

- Use `@IntegrationTest` (composite annotation) for all integration tests.
- Leverage `spring-boot-testcontainers` with `@ServiceConnection` for automatic property injection.
- Do NOT use legacy `spring.factories` or custom `ContextCustomizerFactory` for Testcontainers.

### Authentication

- Stateless JWT environments MUST NOT use `@WithMockUser`.
- Use token-based authentication with `Bearer` header for all protected API tests.
- Generate tokens dynamically using `JwtAuthenticationTestUtils`.

### Data Integrity

- Explicitly handle database cleanup (e.g., `userRepository.deleteAll()`) in `@BeforeEach` or `@AfterEach`.
- Ensure tests are truly isolated to prevent flaky results in shared container environments.

### Performance

- Disable Rate Limiting in test profiles (e.g., `rate-limit.enabled: false`) to prevent intermittent 429 errors.
- Reference [Testing Guideline](docs/operations/testing-guideline.md) for detailed implementation patterns.

---

## Analysis Repository (MANDATORY)

When an agent is asked to perform a deep analysis (not tied to a specific code-changing task):

- Location: `docs/analysis/YYYY-MM-DD-agentName/`
- Metadata: MUST follow the same Metadata header rule as `agent-log` files.
- Goal: To build a persistent knowledge base of system insights and research findings.
- Use `docs/analysis/` for repository-wide reviews, policy audits, architecture studies, and similar non-implementation work.
- Do NOT create a parallel `docs/{backend|frontend}/agent-log/...` set for the same task unless the user also requested implementation work that requires the standard execution log flow.

---

## Global Impact Review

Required when changing:

- config
- cache
- security
- dependencies
- API contracts
- shared constants, enums, or utility classes
- cross-cutting patterns (annotations, base classes, interfaces)

Must perform:

- codebase-wide search for all usages of the changed pattern
- list all affected files in the implementation plan
- verify zero remaining old-pattern usages after implementation
- check affected systems for rollback safety
- verify no performance regression
- verify no security regression

### Side Effect Analysis Questions

Before proceeding with any Non-trivial change, agents MUST answer these questions:

1. Who are the direct callers of this logic, and do they have specific invariants that must be preserved?
2. Does this change affect backward compatibility with existing data, configurations, or API contracts?
3. If the operation fails halfway, what is the impact on data integrity and how can it be safely rolled back?
4. Are there any shared states, caches, or asynchronous processes that need to be synchronized?

### High-Risk Change Zones (Project Specific)

Modifications in these areas require mandatory impact analysis and exhaustive testing:

- `com.daangcool.stack.security`: Authentication, Authorization, and JWT handling.
- `com.daangcool.stack.config`: Core Spring configurations and externalized property mappings.
- `com.daangcool.stack.domain`: JPA Entities and persistence layer mappings (affecting DB schema).
- `com.daangcool.stack.service`: Core business orchestration and transaction boundaries.

---

## Consistency Sweep Rule (MANDATORY)

IF the task involves any of the following:

- adding, renaming, or moving a constant, enum, or shared class
- changing a method signature in a service interface
- modifying cache names, config keys, or annotation values
- renaming a DTO field or changing its type
- changing import paths after package restructuring

THEN the agent MUST:

1. Run: `grep -rn "OLD_PATTERN" src/ --include="*.java"` (or equivalent)
2. List every affected file in the implementation plan
3. Apply the change to ALL files, not only the ones in the immediate task scope
4. Run the same grep again to verify zero remaining matches
5. If any old-pattern usages remain, the task is INCOMPLETE

### Known Failure Patterns (DO NOT REPEAT)

- CacheNames constant class was created but only BoardService was updated. Other services continued using string literals, causing NPE.
- ResourceAuthorizationService was introduced but only applied to BoardService. Other services had no authorization checks.
- DTO field was renamed in the service layer but test fixtures still used the old field name, causing compile errors.

### Self-Check Verification

```text
- [ ] grep -rn "OLD_PATTERN" src/ --include="*.java" returns zero results
- [ ] All affected files are listed in implementation-plan.md
- [ ] Verification command and result recorded in self-check.md
```

---

## Golden Rule

"Make it correct, safe, and understandable first."

- Refer to the Self-Check section for concrete checklists for each pillar.
- Refer to the Consistency Sweep Rule to prevent partial pattern migration.
