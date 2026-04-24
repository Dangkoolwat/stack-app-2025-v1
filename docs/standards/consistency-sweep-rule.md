# Consistency Sweep Rule (MANDATORY)

IF the task involves any of the following:

- adding, renaming, or moving a constant, enum, or shared class
- changing a method signature in a service interface
- modifying cache names, config keys, or annotation values
- renaming a DTO field or changing its type
- changing import paths after package restructuring

## Execution Steps

THEN the agent MUST:

1. Run: `grep -rn "OLD_PATTERN" src/ --include="*.java"` (or equivalent)
2. List every affected file in the implementation plan
3. Apply the change to ALL files, not only the ones in the immediate task scope
4. Run the same grep again to verify zero remaining matches
5. If any old-pattern usages remain, the task is INCOMPLETE

## Known Failure Patterns (DO NOT REPEAT)

- CacheNames constant class was created but only BoardService was updated. Other services continued using string literals, causing NPE.
- ResourceAuthorizationService was introduced but only applied to BoardService. Other services had no authorization checks.
- DTO field was renamed in the service layer but test fixtures still used the old field name, causing compile errors.

## Self-Check Verification

```text
- [ ] grep -rn "OLD_PATTERN" src/ --include="*.java" returns zero results
- [ ] All affected files are listed in implementation-plan.md
- [ ] Verification command and result recorded in self-check.md
```
