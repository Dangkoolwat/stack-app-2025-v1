# 📏 Conventions & Architecture

## 🏛️ General Architecture
- **Monorepo**: Backend and Frontend coexist.
- **JHipster Patterns**: Follows standard JHipster service-layer architecture.
- **DTOs**: Strict separation between Domain Entities and API DTOs.
- **LSP Integration**: Serena uses `jdtls` (Java) and `volar`/`typescript-language-server` (Vue/TS).

## 🔙 Backend (Java)
- **Package Structure**: `com.daangcool.stack`
  - `.domain`: Entities
  - `.repository`: Data access
  - `.service`: Business logic
  - `.web.rest`: Controllers
- **Naming**: PascalCase for classes, camelCase for methods/variables.
- **Validation**: JSR-303 (Bean Validation) on DTOs and Entities.

## 🔜 Frontend (Vue)
- **Standard**: Follow `docs/standards/jhipster-vue-standards.md`.
- **Components**: Functional components in `src/main/webapp/app/shared/`.
- **Views**: Page components in `src/main/webapp/app/entities/` or `src/main/webapp/app/core/`.
- **Naming**: PascalCase for `.vue` files, camelCase for variables.

## 🛡️ Security
- **Auth**: JWT based. Tokens stored in LocalStorage or Cookies.
- **Permissions**: Authority-based access control (`ROLE_ADMIN`, `ROLE_USER`).

## 🤖 AI Operating Principles (Serena)
1. **Precision First**: Always perform symbolic analysis (`find_symbol`, `find_referencing_symbols`) before any non-trivial edits.
2. **Memory-Driven**: Record all architectural decisions and complex logic updates via `write_memory`.
3. **Zero Assumption**: Use `get_symbols_overview` to understand file structures before reading code.
4. **Surgical Edits**: Prefer `replace_symbol_body` or `insert_after_symbol` over full file overwrites.
