---
name: spring-boot-engineer
description: High-level engineering guidelines for Spring Boot 4 applications.
---

# Spring Boot Engineer (Slim)

## 1. Core Workflow
1. **Analyze**: Identify service boundaries, APIs, and data models.
2. **Design**: Plan microservices, security, and persistence.
3. **Implement**: Use layered architecture (Entity -> Repository -> Service -> Controller).
4. **Secure**: Add Spring Security 6 / OAuth2 / JWT.
5. **Test**: Write unit and slice tests; run `./mvnw test`.
6. **Deploy**: Use Actuator for health checks and observability.

## 2. Implementation Standards
- **Constructor Injection**: Always use `private final` fields + `@RequiredArgsConstructor`.
- **Validation**: Apply `@Valid` to `@RequestBody`.
- **Transactions**: Use `@Transactional` for writes, `@Transactional(readOnly = true)` for reads.
- **Exception Handling**: Standardize via `@RestControllerAdvice`.
- **Projections**: Use MapStruct for Entity <-> DTO conversion.

## 3. Engineering Constraints
| MUST DO | MUST NOT DO |
| :--- | :--- |
| ✅ Constructor Injection | ❌ Field Injection (`@Autowired` on field) |
| ✅ Use DTOs | ❌ Expose Entities in API |
| ✅ Externalize Secrets | ❌ Hardcode Secrets/URLs |
| ✅ Paginate Large Lists | ❌ Skip Input Validation |
| ✅ Use @Service/@Repository | ❌ Use Generic @Component for services |

## 4. Verification Checklist
- [ ] No `@Autowired` on private fields?
- [ ] `@Transactional` applied where state changes?
- [ ] Health checks accessible via `/actuator/health`?
- [ ] Secrets managed via `.env` or Vault?
