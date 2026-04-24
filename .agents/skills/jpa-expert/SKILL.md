---
name: jpa-expert
description: Concise guide for JPA 3.2+ Entity modeling and domain standards in Spring Boot.
---

# JPA Expert (Slim)

## 1. Core Standards
- **Fetch Strategy**: Mandatory `LAZY` for all relationships.
- **Lombok**: ❌ No `@Data`. ✅ Use `@Getter`, `@Setter`, and `@NoArgsConstructor`.
- **Collections**: Prefer `Set` over `List` for relationships to avoid duplicates and cartesian products.
- **ID**: Use `GenerationType.SEQUENCE` (Optimized for Oracle).

## 2. Mapping Patterns
```java
@Entity
@Getter @Setter
public class Parent {
    @Id @GeneratedValue(strategy = SEQUENCE, generator = "parent_seq")
    private Long id;

    @OneToMany(mappedBy = "parent", cascade = ALL, orphanRemoval = true)
    private Set<Child> children = new HashSet<>();

    public void addChild(Child child) {
        children.add(child);
        child.setParent(this);
    }
}

@Entity
@Getter @Setter
public class Child {
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;
}
```

## 3. Performance Tips
- ✅ **EntityGraph**: Use `@EntityGraph` in Repositories for specific fetch requirements.
- ✅ **Batch Fetching**: Use `@BatchSize(size = 20)` on collections.
- ✅ **DTO Projection**: Use MapStruct to map Entities to DTOs before returning from Service.
- ❌ **Avoid N+1**: Never loop over a collection and trigger lazy loading; use a join query.

## 4. Verification Checklist
- [ ] All `@ManyToOne` / `@OneToMany` are `FetchType.LAZY`?
- [ ] Circular `toString()` issues avoided?
- [ ] Proper cascading and orphan removal applied?
- [ ] Business key used for `equals()` and `hashCode()`?
