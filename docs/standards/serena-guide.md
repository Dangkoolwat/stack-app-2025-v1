# Serena Integration & Operation Guide

This guide defines the standard procedure for introducing **Serena (LSP-based Semantic Agent)** to the project and ensuring AI agents fully understand the code context.

---

## 1. Initial Setup Phase

When activating Serena in a new project environment, provide the following prompt to the agent:

### 📥 Induction Prompt
> "I want to introduce Serena MCP to this project. First, activate the project (`activate_project`), then run the `onboarding` process to store the technical stack and architectural core information in `memories`."

### ✅ Checklist
- [x] Run `activate_project` and verify paths.
- [x] Define project character via `onboarding` tool.
- [x] Verify storage of project knowledge in `memories/`.

---

## 2. Collaboration & Persistence

To share Serena's knowledge with the team, Git management is required.

### 📦 Include in Git
- `.serena/project.yml`: Project-wide configuration.
- `.serena/memories/`: Project knowledge base (Markdown).

### 🚫 Exclude from Git (`.gitignore`)
- `/cache`: Local indexing data.
- `/project.local.yml`: Personal path settings.
- `/logs`: Execution logs.

---

## 3. Advanced Prompting for Performance

Examples of directives that make agents utilize Serena's features to the fullest:

### 🔍 Precision Analysis Request
> "Don't just read the entire file. Use Serena's `find_symbol` and `find_referencing_symbols` to analyze the implementation of [function/class name] and all actual call sites, then report the impact radius."

### 🛠 Safe Refactoring Request
> "Use Serena's `rename_symbol` to change [old name] to [new name]. This must be done via LSP to ensure type safety and update all references, not just a simple text replacement."

### 🧠 Knowledge Update Request (Post-Task)
> "Record the core logic and design decisions of the [feature name] implemented this time using the Serena `write_memory` tool. This will serve as a reference for other agents modifying this code later."

---

## 4. Operating Principles

In conjunction with `AGENTS.md`, the following rules apply:

1. **Precision First**: For all **Non-trivial** tasks (excluding typos or simple text changes), prioritize Serena's symbol analysis.
2. **Memory-Driven**: Always record new architectural decisions or complex business logic modifications in `memories`.
3. **Zero Assumption**: Understand the file structure via Serena's `get_symbols_overview` before reading the code.

---

## 5. Troubleshooting

- **"No active project" error**: Have the agent check registration with `list_repos`, then re-issue `activate_project`.
- **Slow analysis**: Adjust `depth` in `get_symbols_overview` or narrow the scope to specific directories.

---

## 6. Token-Saving Protocol

For efficient context management, use tools in the following priority:

1. **1st Priority: `semble_rs plan`**: Use this when the target is still unclear.
2. **2nd Priority: `rg --files` / `rg`**: Use this when plan output still needs candidate narrowing.
3. **3rd Priority: `search --outline` / `search --compact`**: Use this when the target already looks like a symbol.
4. **4th Priority: `Serena` (LSP Precision)**: Use this for exact references, callers, and symbol edits after the target is known.
5. **5th Priority: `tree --symbols` / `deps`**: Use this for Java/Vue structure mapping when source context is needed.
6. **6th Priority: `semble_rs digest`**: Use this for long or noisy build/test logs.
7. **7th Priority: `code-review-graph` (Structural Analysis)**: Use this when the blast radius is broad or unclear.
8. **8th Priority: `Grep/Read` (Textual Analysis)**: Use this for non-code files and simple text matching after the target is known.
9. **9th Priority: `git` (History Analysis)**: Use this for change history and prior decisions.
