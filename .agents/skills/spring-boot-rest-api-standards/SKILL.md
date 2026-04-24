---
name: spring-boot-rest-api-standards
description: Concise REST API design standards for Spring Boot applications.
---

# REST API Standards (Slim)

## 1. URL & Method Conventions
- **Nouns Only**: Use plural nouns (e.g., `/api/users`). Avoid verbs (e.g., `/getUsers`).
- **Standard Methods**:
  - `GET`: Retrieve (Idempotent)
  - `POST`: Create
  - `PUT`: Full Update (Idempotent)
  - `PATCH`: Partial Update
  - `DELETE`: Remove (Idempotent)
- **Status Codes**: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

## 2. DTO & Validation
- **Separation**: Never expose JPA Entities. Always use DTOs.
- **Immutability**: Prefer Java `record` for DTOs.
- **Validation**: Use Jakarta annotations (`@NotBlank`, `@Email`, `@Size`) and `@Valid` in Controllers.

## 3. Implementation Example
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
```

## 4. Best Practices
- ✅ **Constructor Injection**: Use `@RequiredArgsConstructor` + `private final`.
- ✅ **Global Exception Handling**: Use `@RestControllerAdvice`.
- ✅ **Pagination**: Always paginate large sets using `Pageable`.
- ❌ **No Hardcoded Secrets**: Use environment variables.
- ❌ **No Raw Exceptions**: Return standardized error responses.

## 5. Security & Consistency
- [ ] Are sensitive fields (passwords, etc.) excluded from DTOs?
- [ ] Is input validation applied to all `@RequestBody`?
- [ ] Is error handling consistent across all endpoints?