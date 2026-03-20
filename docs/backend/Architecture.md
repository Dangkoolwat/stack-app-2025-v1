# Backend Engineering Guideline

## Core Principles
- Maintainability First
- Security by Default
- Clear Layer Separation

---

## Recommended Backend Patterns

### Controller
- Request/Response DTO mapping only
- No business logic

### Service
- Business logic (use-case oriented)
- Transaction boundary

### Repository
- Data access only
- Clear query intent

---

## Maintainability Guidelines

Prefer:
- Explicit queries over magic
- Small services with clear responsibility
- Short transactions

Avoid:
- Deep abstraction layers
- Hidden side effects
- Over-generalization

---

## Security Defaults

- Validate all inputs
- Use standard error format (RFC7807)
- Never log secrets/tokens
- Do NOT cache auth/session data

---

## Change Playbook

### 1. Config / Library Change

Must define:
- Why change is needed
- Scope of impact
- Compatibility (breaking 여부)
- Rollback strategy
- Required tests

Checklist:
- [ ] Dependency tree checked
- [ ] No duplicate libraries (e.g. Jackson)
- [ ] Config consistency 유지
- [ ] Runtime 영향 검증

---

### 2. API Development

Steps:
1. Define contract (DTO)
2. Validate inputs
3. Implement service logic
4. Standardize error handling
5. Document (OpenAPI)

---

### 3. Database Change

- Use Liquibase only
- Never modify schema manually
- Ensure backward compatibility
- Plan rollback

---

## Caching Rules (Redis)

Use ONLY when:
- High read frequency
- Low consistency risk

Rules:
- TTL required
- Cache managed in Service layer
- Avoid auth/session caching

---

## Preferred Defaults

- JSON: Single ObjectMapper
- DB: Oracle + Liquibase
- Cache: Redis centralized
- Error: RFC7807
- Auth: Stateless (JWT)

---

## Self-Check Before Merge

- [ ] Business logic is clear and testable
- [ ] No layer violation
- [ ] Security review done
- [ ] Performance acceptable
- [ ] Rollback possible

---

## Golden Rule

👉 "Write code someone else can safely change in 6 months."