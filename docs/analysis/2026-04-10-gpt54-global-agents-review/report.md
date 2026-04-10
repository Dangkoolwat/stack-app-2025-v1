---
agent: GPT-5.4
created_at: 2026-04-10 (금)
language: ko
---

# 글로벌 지침과 프로젝트 AGENTS 정합성 리뷰

## 목적

세션 전역 Codex 지침과 현재 프로젝트의 최상위 `AGENTS.md`를 비교해, 실제 작업 중 다른 에이전트가 충돌 또는 모호성으로 오해할 수 있는 지점을 식별한다.

## 검토 범위

- 세션 전역 Codex 지침
- `AGENTS.md`
- `docs/knowledge/2026-03-28-document-authority-boundaries.md`
- `docs/analysis/2026-03-26-gpt54-sb4-jh9-review/testing-rules-ambiguity-review.md`

## 전제

- 저장소 상위 디렉터리에는 별도의 부모 `AGENTS.md`가 보이지 않았다.
- 따라서 여기서 말하는 "글로벌 지침서"는 세션 전역 Codex 지침으로 해석했다.
- 이 문서는 코드 변경 지시가 아니라, 작업 규칙의 충돌 가능성과 인수인계 관점의 모호성을 점검하는 분석 문서다.

## 총평

현재 상태는 치명적인 정면 충돌보다는, 우선순위와 적용 조건이 충분히 명시되지 않아 다른 에이전트가 다르게 해석할 수 있는 지점이 더 크다.

특히 다음 세 가지가 혼동의 핵심이다.

- 언제 즉시 실행하고 언제 확인을 받아야 하는지
- 어떤 문서 규칙이 응답 메시지에도 적용되는지, 아니면 `docs/` 산출물에만 적용되는지
- 프로젝트 규칙이 "이상적인 표준"인지 "즉시 강제되는 실행 규칙"인지

## 판단 요약

### 1. 실질적 충돌: 확인 우선 규칙 vs 즉시 실행 성향

프로젝트 `AGENTS.md`는 작업 시작 시 가설을 제시하고 확인을 받은 뒤 진행하라고 요구한다.

반면 세션 전역 Codex 지침은 가능한 한 합리적인 가정을 하고 바로 실행하라고 강하게 유도한다.

현재 프로젝트 문서에도 예외가 있기는 하다.

- task가 trivial 하거나 explicitly defined 이면 바로 진행 가능

따라서 완전한 모순은 아니지만, 다른 에이전트가 아래처럼 갈릴 수 있다.

- 에이전트 A: "명시된 요청이니 바로 수행"
- 에이전트 B: "프로젝트 규칙상 반드시 확인 필요"

결론적으로 이 지점은 "충돌이라기보다 우선순위 설명 부족"에 가깝다.

권장 해석:

- 프로젝트 로컬 규칙을 우선 적용
- 다만 task가 명시적으로 좁혀져 있고 위험도가 낮으면 확인 없이 진행 가능
- 공용 계약, 보안, 설정, 아키텍처 영향이 있으면 확인을 우선

## 2. 모호성: 문서 포맷 규칙의 적용 범위

프로젝트 `AGENTS.md`는 `docs/` 아래 공유 문서와 agent-log에서 bold 및 emoji 금지를 명시한다.

세션 전역 Codex 지침은 일반 응답에서 Markdown 사용을 허용하고, 필요하면 짧은 헤더나 강조를 사용할 수 있게 한다.

이 둘은 직접 충돌하지는 않는다.

하지만 다른 에이전트가 아래를 혼동할 수 있다.

- 최종 채팅 응답에도 bold 금지가 적용된다고 오해
- 반대로 `docs/analysis` 산출물에도 일반 응답 스타일을 그대로 사용

권장 해석:

- 포맷 금지는 `docs/` 산출물과 `agent-log`에 한정
- 채팅 응답은 세션 전역 응답 규칙을 따르되, 프로젝트 맥락상 과한 장식은 피함

## 3. 모호성: "공유 문서는 영어"와 "분석 문서는 한국어 가능"의 경계

프로젝트 `AGENTS.md`는 `docs/` 아래 공유 문서는 영어를 기본으로 요구한다.

동시에 `docs/analysis/`의 system-wide review 또는 architecture-wide review는 사용자가 명시적으로 한국어를 요청하면 한국어를 허용한다.

이 규칙 자체는 일관적이다.

다만 system-wide review의 범위가 명확히 정의되어 있지 않아, 다음 같은 질문이 생길 수 있다.

- 규칙 충돌 점검도 system-wide review 인가
- 기능 단위 조사 문서도 한국어 허용 대상인가

권장 해석:

- 저장소 전체 정책, 아키텍처, 규칙 충돌, 문서 권한 체계 점검은 system-wide review 로 간주
- 기능 구현 기록이나 일반 운영 문서는 영어 유지

## 4. 잠재 충돌: 글로벌 자율성 vs 프로젝트 Tier/Approval 체계

세션 전역 Codex 지침은 가능한 한 end-to-end 로 해결하라고 유도한다.

프로젝트 `AGENTS.md`는 Tier 3 항목에 대해 Mandatory Peer Review/Approval before Implementation 을 요구한다.

이 부분은 실질적인 긴장 관계가 있다.

특히 세션 전역 지침만 따라 작업하는 에이전트는 아래를 할 위험이 있다.

- config
- dependency
- DB schema
- breaking API

같은 영역을 사용자 재확인 없이 바로 수정

권장 해석:

- 프로젝트 Tier 규칙이 로컬 안전 규칙으로서 우선
- 세션 전역의 자율성은 Tier 1~2 또는 명확히 승인된 범위에서만 적극 적용

## 5. 모호성: "코멘트는 한국어 SHOULD" 와 전역 최소 주석 원칙

프로젝트 `AGENTS.md`는 source code comments SHOULD be written in Korean 이라고 한다.
세션 전역 Codex 지침은 주석을 드물고 꼭 필요한 경우에만 추가하라고 한다.

이 둘은 충돌하지 않는다.

하지만 다른 에이전트는 두 가지로 오해할 수 있다.

- 모든 코드에 한국어 주석을 더 붙여야 한다고 이해
- 주석을 거의 쓰지 않으니 언어 규칙은 무시해도 된다고 이해

권장 해석:

- 주석이 필요한 경우에만 추가
- 추가한다면 한국어 사용

## 6. 모호성: agent-log 의무와 분석 문서 의무의 관계

프로젝트 `AGENTS.md`는 구현 작업에 대해 `docs/{backend|frontend}/agent-log/...` 구조를 강하게 요구한다.
동시에 deep analysis 는 `docs/analysis/YYYY-MM-DD-agentName/` 에 남기라고 요구한다.

이 규칙은 목적에 따라 구분 가능하다.

하지만 다른 에이전트는 아래를 고민할 수 있다.

- 분석 task 에도 backend/frontend agent-log 를 만들어야 하는가
- analysis 와 agent-log 를 둘 다 작성해야 하는가

권장 해석:

- 코드 변경/구현 task 는 agent-log
- 저장소 전반 분석 task 는 analysis
- 둘을 동시에 요구하는 작업이 아니면 중복 생성하지 않음

## 7. 이미 저장소 안에 존재하는 과거 모호성 사례와의 연결

기존 문서도 비슷한 패턴을 이미 지적하고 있다.

- `docs/knowledge/2026-03-28-document-authority-boundaries.md`
  - 우선순위 체계는 맞지만, supporting context 가 더 강한 어조를 가지면 에이전트가 잘못 따라갈 수 있다고 설명한다.
- `docs/analysis/2026-03-26-gpt54-sb4-jh9-review/testing-rules-ambiguity-review.md`
  - 문서가 설명하는 이상적인 표준과 실제 저장소의 과도기 상태가 분리되어 기록되지 않아 오해가 생긴다고 정리한다.

이번 비교에서도 같은 구조가 반복된다.

- 규칙 자체가 틀렸다기보다
- 적용 조건, 예외, 범위가 바로 눈에 띄지 않아
- 다른 에이전트가 "즉시 강제 규칙"과 "이상적 표준"을 혼동하기 쉽다

## 다른 에이전트를 위해 특히 명확히 남겨야 할 핵심 해석

1. 프로젝트 `AGENTS.md`가 세션 전역 Codex 지침보다 우선한다.
2. 다만 프로젝트 `AGENTS.md` 내부에도 trivial 또는 explicitly defined 예외가 있으므로, 모든 task 에서 확인 질문이 필수는 아니다.
3. `docs/` 산출물 규칙과 채팅 응답 규칙은 구분해서 적용해야 한다.
4. Tier 3 성격의 변경은 세션 전역 자율성보다 프로젝트 승인 규칙이 우선한다.
5. deep analysis 는 `docs/analysis/` 산출물로 남기고, 구현 중심 task 와 동일한 agent-log 요구로 자동 확장하지 않는다.

## 권장 보완 문구

다른 에이전트가 덜 헷갈리게 하려면, 프로젝트 `AGENTS.md` 또는 별도 상위 안내에 아래 수준의 문구가 있으면 좋다.

### 제안 1

"Interaction Rule is the default for ambiguous or high-impact tasks. For trivial or explicitly scoped requests, agents may proceed directly without a confirmation turn."

### 제안 2

"Formatting restrictions on bolding and emojis apply to repository documents under `docs/` and `agent-log` outputs, not necessarily to chat responses."

### 제안 3

"For repository-wide reviews, policy audits, and architecture analyses, use `docs/analysis/`. For implementation tasks, use the backend/frontend agent-log flow."

### 제안 4

"Tier 3 approval rules override the default autonomous execution style of the global agent runtime."

## 최종 판단

이번 검토 기준으로는 "즉시 수정이 필요한 치명적 정면 충돌"은 없다.

하지만 다른 에이전트의 실행 방식이 갈릴 수 있는 operational ambiguity 는 분명히 있다.

가장 큰 위험은 다음 두 가지다.

- 확인이 필요한 작업을 즉시 실행해 버리는 것
- 문서 산출물 규칙과 채팅 응답 규칙의 범위를 혼동하는 것

따라서 현 상태는 "충돌 있음"보다는 "우선순위와 적용 범위 설명이 더 선명해져야 함"으로 정리하는 것이 가장 정확하다.
