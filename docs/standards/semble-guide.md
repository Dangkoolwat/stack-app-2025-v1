# Semble Setup Guide (Intel Mac & Token Economy)

이 문서는 프로젝트 내 초고속 코드 탐색 도구인 **Semble**의 설치 및 설정 방법을 안내합니다. 특히 인텔 맥(x86_64) 및 최신 파이썬 환경에서의 호환성 문제를 해결하는 패치 방법을 포함하고 있습니다.

---

## 1. 개요 (What is Semble?)
Semble은 로컬 코드베이스에 대한 하이브리드(의미 기반 + 키워드) 검색을 제공하는 도구입니다. AI 에이전트가 넓은 범위를 탐색할 때 발생하는 **토큰 소모를 90% 이상 획기적으로 줄여주는** 필수 도구입니다.

- **search**: 자연어 쿼리로 관련 코드 블록 탐색
- **find-related**: 특정 코드 지점과 유사한 구현 패턴 탐색

---

## 2. 설치 방법 (Installation)

시스템 파이썬을 오염시키지 않는 `uv`를 사용하는 것을 권장합니다.

```bash
# Semble 및 MCP 지원 패키지 설치
uv tool install "semble[mcp]" --force
```

---

## 3. ⚠️ 인텔 맥 & Python 3.14 호환성 패치 (Mandatory Patch)

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

## 4. MCP 설정 (Configuration)

각 도구의 MCP 설정 파일에서 `uvx` 대신 **패치된 로컬 실행 경로**를 지정해야 합니다.

### Antigravity (`mcp_config.json`)
```json
"semble": {
  "command": "/Users/sanghyoukjin/.local/bin/semble"
}
```

### Codex (`~/.codex/config.toml`)
```toml
[mcp_servers.semble]
command = "/Users/sanghyoukjin/.local/bin/semble"
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

## 5. 분석 우선순위 (Analysis Priority)

에이전트가 작업을 시작할 때 다음 순서로 도구를 활용하도록 권장합니다.

1. **Semble**: 개념/자연어 검색으로 후보군 압축 (토큰 절약)
2. **Code Review Graph**: 구조적 의존성 및 파급력 분석
3. **Serena**: 정밀한 심볼 분석 및 코드 수정
4. **Grep/Read**: 보조적인 텍스트 검색
5. **Git**: 변경 이력 및 컨텍스트 추적

---

## 6. 기대 효과
- **토큰 경제**: 대규모 프로젝트 탐색 시 불필요한 파일 읽기 최소화
- **속도**: 인덱싱 기반 검색으로 즉각적인 결과 도출
- **일관성**: `find-related`를 통한 기존 코드 패턴 준수
