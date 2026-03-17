# ADR: Liquibase Production Execution Policy

## Status
Accepted

## Context
In `stack-app-2025-v1`, as of the Spring Boot 4 / Hibernate 7 migration, we have disabled automatic Liquibase migrations in the production environment (`application-prod.yml` set `spring.liquibase.enabled: false`).

The application uses an Oracle Database in production, where schema changes often require DBA review and strict execution windows. Automatic migrations during application startup can lead to:
- Unexpected locks on production tables.
- Application startup delays or failures due to long-running migrations.
- Incompatibility between rolling updates if one node applies a breaking schema change while others are still running.

## Decision
We will not use automatic Liquibase migrations in the production environment. Instead, we follow these rules:

1. **Development/Test Environments**: Automatic migration remains enabled (`spring.liquibase.enabled: true`) to ensure developers always have the latest schema.
2. **Production Environment**: `spring.liquibase.enabled` MUST be `false`.
3. **Migration Workflow**:
   - Schema changes are generated as Liquibase XML changeSets in `src/main/resources/config/liquibase/`.
   - Before production deployment, the DB administrator or the CI/CD pipeline MUST manually execute the migration using the Maven Liquibase plugin or a dedicated script.
   - Example command for manual execution: `./mvnw liquibase:update -Pprod` (with appropriate credentials).

## Consequences
- **Positive**: Safer deployments, no startup-time locks, better integration with enterprise DBA processes.
- **Negative**: Requires an additional manual step during the deployment process, increasing operational burden slightly.
- **Risk**: Forgetting to run the migration before deployment will lead to JPA/Hibernate mapping errors at runtime. This should be mitigated by build/deploy scripts.
