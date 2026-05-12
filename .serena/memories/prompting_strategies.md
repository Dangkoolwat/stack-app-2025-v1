# 🧠 Advanced Prompting Strategies

To maximize Serena's capabilities (200% utilization), use the following instruction patterns:

## 🔍 Precision Analysis Request
- **Pattern**: "Don't just read the whole file. Use Serena's `find_symbol` and `find_referencing_symbols` to analyze the implementation of [Function/Class Name] and all its actual call sites, then report the impact radius."
- **Benefit**: Avoids context bloat and ensures 100% accuracy in dependency tracking.

## 🛠 Safe Refactoring Request
- **Pattern**: "Use Serena's `rename_symbol` to change [Old Name] to [New Name]. Ensure type safety and update all references via LSP, not just simple text replacement."
- **Benefit**: Prevents broken references and maintainable refactors across the entire codebase.

## 🧠 Knowledge Update Request (Post-Task)
- **Pattern**: "Record the core logic and design decisions of the [Feature Name] I just implemented using Serena's `write_memory` tool. This will help future agents when they need to modify this code."
- **Benefit**: Builds a persistent, machine-readable knowledge base for long-term project maintenance.
