---
author: opencode
created_at: 2026-03-27 (Saturday)
language: en
---

# Naming Convention Checker Guide

## 1. Purpose

- Automated validation of naming conventions according to AGENTS.md
- Documentation of preventive measures

## 2. Scope

- Applies to all directory/file creation in:
  - `docs/analysis/`
    | Type | Format | Example
    |--------------|-------------------------|---------
    | Analysis Report | `YYYY-MM-DD-agentname` | `2026-03-26-opencode`
    | Agent Log | `YYYY-MM-DD-taskname` | `2026-03-25-cve-patch`
    | Knowledge Item | `YYYY-MM-DD-topicname` | `2026-03-24-springboot-security`

### 3.2 File Naming

table:
| Type | Format | Example  
 |--------------|--------------------------|------------------------
| Analysis Rpt | `[UpperCamelCase]_REVIEW.md` | `RefactoringReview.md`
| Agent Log | `[StageName].md` | `problem-analysis.md`  
 | Knowledge | `[Topic].md` | `SpringbootSecurity.md`

## 4. Automated Validation Script (Bash)

```bash
#!/bin/bash

echo 'Checking directory naming conventions...'

echo 'This script enforces naming rules defined in AGENTS.md'

check_directory_name() {
  local dir_name="$1"
  if ! echo "$dir_name" | grep -q "opencode"; then
    echo "ERROR: Directory name must include agent name"
    exit 1
  fi
}

echo 'Validation complete'
```

## 5. Git Hook Configuration

### 5.1 pre-commit hook

```bash
#!/bin/bash

git diff --cached --name-only | while read file; do
  if [[ $file =~ ^docs/analysis/ ]]; then
    if ! grep -q "opencode" <<< "$file"; then
      echo "ERROR: Analysis directory name violates AGENTS.md"
      exit 1
    fi
  fi
done
```

## 6. Template Update

### 6.1 analysis_template.md

```markdown
---
agent: opencode
created_at: { { date } }
language: en
---

# {{title}}

[Actual content in Korean]
```

## 7. Implementation Steps

1. Save script: `/scripts/naming_convention_checker.sh`
2. Grant execution: `chmod +x /scripts/naming_convention_checker.sh`
3. Setup Git hook: copy to `.git/hooks/pre-commit`

## 8. Review Checklist

- [ ] agent name in directory name
- [ ] No filename duplication
- [ ] Date format (YYYY-MM-DD)
- [ ] retiring document cleanup
- [ ] rename history tracking

## 9. Agent Log Reference Rule

Commit body must include agent log reference:

```text
feat(server): add user creation API

See docs/backend/agent-log/2026-03-27-user-api-creation
```

## 10. Document Hierarchy Priority

In case of conflict:

1. AGENTS.md (Highest priority)
2. docs/standards/
3. docs/workflow/
4. docs/operations/ (Lowest priority).
