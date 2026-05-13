# Semble Troubleshooting

이 문서는 Semble 설치와 환경별 수정이 필요한 경우에만 참고하는 운영 노트입니다.

---

## 1. 설치 방법

시스템 파이썬을 오염시키지 않는 `uv`를 사용하는 것을 권장합니다.

```bash
# Semble 및 MCP 지원 패키지 설치
uv tool install "semble[mcp]" --force
```

---

## 2. 인텔 맥 & Python 3.14 호환성 패치

인텔 맥 환경에서 `tree-sitter-language-pack` 라이브러리의 버전 이슈로 `ImportError: cannot import name 'manifest_languages'` 에러가 발생할 수 있습니다. 이 경우 아래와 같이 수동 패치가 필요합니다.

### 패치 대상 파일 경로
`~/.local/share/uv/tools/semble/lib/python3.13/site-packages/semble/chunking/core.py`
*(파이썬 버전 경로는 환경에 따라 다를 수 있음)*

### 수정 내용
`manifest_languages` 임포트를 제거하고, 지원 언어 리스트를 하드코딩된 리스트로 대체합니다.

```python
# 수정 전
from tree_sitter_language_pack import SupportedLanguage, get_parser, manifest_languages
_TREE_SITTER_LANGUAGES: frozenset[str] = frozenset(manifest_languages())

# 수정 후
from tree_sitter_language_pack import SupportedLanguage, get_parser
_TREE_SITTER_LANGUAGES: frozenset[str] = frozenset([
    'bash', 'c', 'cpp', 'csharp', 'css', 'go', 'html', 'java', 'javascript',
    'json', 'kotlin', 'lua', 'markdown', 'objc', 'ocaml', 'perl', 'php',
    'python', 'ruby', 'rust', 'scala', 'swift', 'toml', 'tsx', 'typescript', 'yaml'
])
```

---

## 3. MCP 설정

각 도구의 MCP 설정 파일에서 `uvx` 대신 **패치된 로컬 실행 경로**를 지정해야 합니다.

### Antigravity (`mcp_config.json`)
```json
"semble": {
  "command": "/Users/sanghyoukjin/.local/bin/semble",
  "args": ["."]
}
```

### Codex (`~/.codex/config.toml`)
```toml
[mcp_servers.semble]
command = "/Users/sanghyoukjin/.local/bin/semble"
args = ["."]
```

### OpenCode (`~/.config/opencode/opencode.json`)
```json
"semble": {
  "type": "local",
  "enabled": true,
  "command": ["/Users/sanghyoukjin/.local/bin/semble", "."]
}
```
