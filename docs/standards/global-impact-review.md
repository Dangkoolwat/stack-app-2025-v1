# Global Impact Review (MANDATORY)

Required when changing:

- config
- cache
- security
- dependencies
- API contracts
- shared constants, enums, or utility classes
- cross-cutting patterns (annotations, base classes, interfaces)

## Review Process

Must perform:

1. codebase-wide search for all usages of the changed pattern
2. list all affected files in the implementation plan
3. verify zero remaining old-pattern usages after implementation
4. check affected systems for rollback safety
5. verify no performance regression
6. verify no security regression

## Side Effect Analysis Questions

Before proceeding with any Non-trivial change, agents MUST answer these questions:

1. Who are the direct callers of this logic, and do they have specific invariants that must be preserved?
2. Does this change affect backward compatibility with existing data, configurations, or API contracts?
3. If the operation fails halfway, what is the impact on data integrity and how can it be safely rolled back?
4. Are there any shared states, caches, or asynchronous processes that need to be synchronized?

## High-Risk Change Zones (Project Specific)

Modifications in these areas require mandatory impact analysis and exhaustive testing:

- `com.daangcool.stack.security`: Authentication, Authorization, and JWT handling.
- `com.daangcool.stack.config`: Core Spring configurations and externalized property mappings.
- `com.daangcool.stack.domain`: JPA Entities and persistence layer mappings (affecting DB schema).
- `com.daangcool.stack.service`: Core business orchestration and transaction boundaries.
