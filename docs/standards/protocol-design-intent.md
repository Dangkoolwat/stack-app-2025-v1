# Protocol Design Intent & Agent Capability Modes

Defines the underlying philosophy and operational modes of the Agent Operating Protocol for this JHipster-based Spring Boot 3 + Vue 3 monorepo.

---

## 1. Design Intent

- **Target:** Weak/fast agents, cross-model execution, token economy, strict approval gates, evidence-based reporting.
- **Goals:**
    1. Control over-acting or instruction-weak agents.
    2. Reduce token waste via lazy loading and compressed handoff.
    3. Improve cross-model compatibility for different instruction-following styles.
    4. Preserve Architect authority over final decisions.
    5. Prevent false success reports via evidence-based verification.

---

## 2. Agent Capability Modes

- **Strict Mode:** For distilled/small/flash models. Follow every step literally. No inferred approval. Use `code-review-graph` for all Non-trivial+ work. Run build commands exactly as documented (`./mvnw verify`, `npm run build`).
- **Standard Mode:** For normal coding agents. Follow all protocol rules and mandatory lazy loading.
- **Expert Mode:** For high-capability reasoning models. May recommend improvements but must not bypass safety rules.

---

## 3. Cross-Model Compatibility

Assume agents may skip implicit context or over-compress reasoning. Use explicit `MUST`, `NEVER`, and `STOP`. Include exact task IDs and paths.

Key adaptation for this monorepo:
- Backend changes: reference `pom.xml`, `src/main/java/`, `src/test/java/`
- Frontend changes: reference `package.json`, `src/main/webapp/`
- Shared contract changes: reference both sides explicitly

---

## 4. Token Economy Goal

Token economy means reading the **right** context, not skipping **required** context. Load only triggered policies and directly affected files.

- Backend: Do not read all `*Service.java` files; use `semble_rs` or Serena to locate the exact class.
- Frontend: Do not read all `.vue` files; use `semble_rs` to narrow scope first.
- Shared: Read DTO/API contract files from both sides when modifying shared interfaces.
