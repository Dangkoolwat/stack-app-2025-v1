# JPA Entity & Domain Standards

Document role: mandatory engineering rules for backend domain modeling.

## 1. Core Principles
- **Laziness by Default**: All relationships MUST be `FetchType.LAZY` unless there is a strong reason otherwise.
- **Jakarta Persistence**: Use `jakarta.persistence.*` annotations (JPA 3.2+).
- **Data Safety**: Avoid `@Data` on JPA Entities to prevent circular `toString()`/`hashCode()` issues and performance overhead. Use `@Getter`, `@Setter`, and custom `equals`/`hashCode` based on business keys.

## 2. Relationship Mapping
- **@ManyToOne**: Always define the join column explicitly using `@JoinColumn`.
- **@OneToMany**: Prefer unidirectional `@OneToMany` with `@JoinColumn` or bidirectional mapping if necessary. Always use a `Set` instead of a `List` to prevent cartesian product issues during fetching.
- **@ManyToMany**: Use sparingly. Always define a join table.
- **Orphan Removal**: Use `orphanRemoval = true` only when the child lifecycle is strictly tied to the parent.

## 3. Performance & Optimization
- **N+1 Prevention**: Use `EntityGraph` or `JOIN FETCH` queries for known required relationships.
- **Batch Size**: Use `@BatchSize` or global `default_batch_fetch_size` to optimize collection loading.
- **ID Generation**: Prefer `GenerationType.SEQUENCE` for Oracle/PostgreSQL for better batching support.

## 4. Auditing & Lifecycle
- **Auditing**: Use `AbstractAuditingEntity` or `@EntityListeners(AuditingEntityListener.class)` for `createdBy`, `createdDate`, etc.
- **Validation**: Use Jakarta Bean Validation (`@NotNull`, `@Size`, etc.) on entity fields to mirror database constraints.

## 5. Prohibited Patterns
- ❌ **FetchType.EAGER**: Never use eager fetching as it causes unpredictable N+1 issues.
- ❌ **Bidirectional mapping without helper methods**: Always provide `addXxx` and `removeXxx` methods for bidirectional consistency.
- ❌ **Direct exposure of Entities**: Never return JPA Entities from Controllers; always map to DTOs.
