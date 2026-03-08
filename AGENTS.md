# AGENTS.md

## Project Overview

This repository is a JHipster-based Spring Boot backend.

Stack includes:

- Spring Boot 3.x
- Java 17+
- Maven
- Spring Data JPA
- Liquibase
- Redis cache
- Spring Security with JWT
- RFC7807 ProblemDetail
- Swagger/OpenAPI
- JUnit5 / MockMvc

Architecture pattern:

Controller -> Service -> Domain -> Repository

---

## Backend Architecture Rules

Controllers must remain thin.

Business logic belongs in the service layer.

Repositories handle persistence only.

Domain models should not contain infrastructure concerns.

Avoid mixing transport, persistence, and business logic.

---

## API Contract Rules

Backend APIs are the source of truth for:

- authentication
- authorization
- domain validation
- error contracts

Preserve API contracts unless explicitly requested.

When modifying API responses:

- document frontend impact
- maintain RFC7807 error format
- verify backward compatibility.

---

## Security Rules

High-risk areas include:

- authentication
- JWT processing
- authorization checks
- user management
- file handling
- database queries
- external integrations

Rules:

- validate input at system boundaries
- never log credentials or tokens
- maintain role-based access checks
- prefer secure defaults.

---

## Persistence Rules

Liquibase changes require careful review.

For schema changes:

- explain migration intent
- check backward compatibility
- consider rollback strategy.

Review entity relationships and fetch strategies before modifying JPA mappings.

Explain cache implications when touching Redis or Hibernate caching.

---

## Backend Engineering Discussion Rules

When proposing backend architecture changes:

Problem  
Explain the limitation in the current structure.

Proposal  
Describe the recommended solution.

Alternatives  
Describe other possible designs.

Trade-offs  
Discuss impacts on:

- transaction boundaries
- domain layering
- API contracts
- persistence model
- caching
- operational complexity

Risks  
Identify migration or compatibility risks.

Decision Needed  
Clearly state when human confirmation is required before applying database, contract, or security-sensitive changes.

---

## Frontend Coordination

Backend changes may affect the frontend.

When modifying:

- authentication flow
- API contracts
- error structures
- authorization rules

explicitly state frontend impact.

Frontend architecture details are defined in `vue3.md`.

---

## Verification

Preferred verification order:

1 unit tests  
2 integration tests  
3 MockMvc API tests  
4 Maven test lifecycle  
5 build verification  

Typical commands:

./mvnw test  
./mvnw verify  
./mvnw clean package

If verification cannot run, explain why.

---

## Completion Checklist

Before finalizing:

- architecture layering preserved
- security impact reviewed
- API contracts validated
- persistence effects checked
- verification executed
