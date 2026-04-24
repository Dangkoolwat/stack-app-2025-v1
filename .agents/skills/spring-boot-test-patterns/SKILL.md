---
name: spring-boot-test-patterns
description: Concise Spring Boot 4 testing patterns with JUnit 5, Mockito, and Testcontainers.
---

# Spring Boot Testing Patterns (Slim)

## 1. Test Type Reference
| Type | Annotation | Use Case |
| :--- | :--- | :--- |
| **Unit** | `@ExtendWith(MockitoExtension.class)` | Business logic (Fast) |
| **Repo Slice** | `@DataJpaTest` | Database operations (Slice) |
| **Controller Slice** | `@WebMvcTest` | REST API layer (Slice) |
| **Integration** | `@SpringBootTest` | Full context + Containers |

## 2. Integration Testing with Testcontainers
Use `@ServiceConnection` for automatic property injection (Spring Boot 3.5+).
```java
@SpringBootTest
@Import(TestContainerConfig.class)
class IntegrationTest {
    @Autowired private MockMvc mockMvc;
    // ...
}

@TestConfiguration
public class TestContainerConfig {
    @Bean @ServiceConnection
    public OracleContainer oracleContainer() {
        return new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");
    }
}
```

## 3. Slice Testing Patterns
- **Repository**: Use `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` to use the real DB container.
- **Controller**: Use `@WebMvcTest(MyController.class)` + `@MockBean` for dependencies.

## 4. Best Practices
- ✅ **Favor Slice Tests** (`@DataJpaTest`, `@WebMvcTest`) over full `@SpringBootTest`.
- ✅ **Deterministic Data**: Initialize data in `@BeforeEach`.
- ✅ **Context Caching**: Group tests with identical configurations to reuse Spring context.
- ❌ **Avoid `@DirtiesContext`**: It significantly slows down the test suite.
- ❌ **Avoid Mixing `@MockBean`**: Different mock configurations break context caching.

## 5. Quick Checklist
- [ ] No cross-test data pollution?
- [ ] Mocks used only where necessary?
- [ ] Testcontainers lifecycle managed correctly?
- [ ] Verification proportional to the change?
