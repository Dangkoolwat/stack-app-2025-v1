# Semble Operation Guide

This guide provides instructions for installing and configuring **Semble** for ultra-efficient code analysis and token optimization, specifically tailored for Intel Mac (x86_64) environments.

---

## 1. Installation

It is recommended to use `uv` to manage tools without polluting the system Python environment.

```bash
# Install Semble with MCP support
uv tool install "semble[mcp]" --force
```

---

## 2. ⚠️ Intel Mac & Python Compatibility Patch (Mandatory)

On Intel Mac environments, you may encounter an `ImportError: cannot import name 'manifest_languages'` due to versioning issues in the `tree-sitter-language-pack` library. A manual patch is required.

### Target File Path
`~/.local/share/uv/tools/semble/lib/python3.13/site-packages/semble/chunking/core.py`
*(Note: Python version path may vary based on your environment)*

### Modification Details
Remove the `manifest_languages` import and replace the dynamic language loading with a hardcoded list of supported languages.

#### Before Patch:
```python
from tree_sitter_language_pack import SupportedLanguage, get_parser, manifest_languages
_TREE_SITTER_LANGUAGES: frozenset[str] = frozenset(manifest_languages())
```

#### After Patch:
```python
from tree_sitter_language_pack import SupportedLanguage, get_parser
_TREE_SITTER_LANGUAGES: frozenset[str] = frozenset([
    'bash', 'c', 'cpp', 'csharp', 'css', 'go', 'html', 'java', 'javascript', 
    'json', 'kotlin', 'lua', 'markdown', 'objc', 'ocaml', 'perl', 'php', 
    'python', 'ruby', 'rust', 'scala', 'swift', 'toml', 'tsx', 'typescript', 'yaml'
])
```

---

## 3. MCP Configuration

Ensure the `mcp_config.json` uses the **patched local binary path** instead of the generic `uvx` command to ensure the patch is applied during execution.

### Antigravity (`mcp_config.json`)
```json
"semble": {
  "command": "/Users/sanghyoukjin/.local/bin/semble"
}
```

---

## 4. Operational Protocol

Agents MUST follow the hierarchical exploration rules defined in `AGENTS.md` (Section 2A). Semble acts as the entry point for all code discovery tasks to maximize token efficiency.

1. **Start with Semble**: Use `mcp_semble_search` for high-level discovery.
2. **Narrow down with Graph**: Use `code-review-graph` to understand the impact.
3. **Pinpoint with Serena**: Use symbol-level tools for precise location.
4. **Final Read**: Only read the actual file content when the exact location is confirmed.
