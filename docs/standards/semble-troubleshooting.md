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
  "args": []
}
```

### Codex (`~/.codex/config.toml`)
```toml
[mcp_servers.semble]
command = "/Users/sanghyoukjin/.local/bin/semble"
args = []
```

### OpenCode (`~/.config/opencode/opencode.json`)
```json
"semble": {
  "type": "local",
  "enabled": true,
  "command": ["/Users/sanghyoukjin/.local/bin/semble"]
}
```

---

## 4. Heavy Loading & Zombie Processes (대규모 프로젝트 주의사항)

프로젝트 규모가 크거나(예: `.git` 폴더가 수 GB 이상), 대용량 바이너리 파일이 많은 경우 `args: ["."]`를 사용하면 시작 시 전체 인덱싱 및 파일 감시(File Watcher) 오버헤드로 인해 시스템이 느려지거나 "파이썬 좀비 프로세스"가 누적될 수 있습니다.

### 해결 방법
1. **MCP 설정 변경**: `args`를 `[]` (빈 배열)로 설정하여 시작 시 자동 인덱싱을 방지합니다.
2. **좀비 프로세스 정리**: 터미널에서 아래 명령어를 실행하여 기존 프로세스를 강제 종료합니다.
   ```bash
   pkill -f "semble"
   ```
3. **필요 시 호출**: 에이전트가 `search` 도구를 호출할 때만 특정 경로(예: `repo="."`)를 전달하여 필요한 시점에만 인덱싱을 수행하도록 유도합니다.

---

## 5. Java/Vue Discovery Note

- Java/Vue 작업에서는 `tree --symbols`, `deps`, `search --outline`을 먼저 확인할 수 있습니다.
- `impact`는 빠른 reverse-dependency probe로만 사용하고, 결과가 비어 있으면 blast radius가 없다는 뜻으로 해석하지 않습니다.
- 구조가 충분히 좁혀지지 않으면 CRG 또는 Serena로 넘어갑니다.
