---
agent: Antigravity
created_at: 2026-04-22 (수요일)
language: ko
---

# AGENTS.md 업데이트 비교 보고서 (v2)

## 1. 개요
본 보고서는 graphify 도입 및 외부 시니어 아키텍트 가이드라인을 차용하여 업데이트된 AGENTS.md 파일의 변경 사항을 정리합니다. 기존의 Tier 시스템과 프로젝트 맥락을 보존하면서도, 에이전트의 작업 정밀도를 높이는 데 초점을 맞추었습니다.

## 2. 주요 변경 사항 (Before vs After)

| 구분 | 이전 (Before) | 이후 (After) | 비고 |
| :--- | :--- | :--- | :--- |
| 지식 관리 (KI) | docs/knowledge/ 검색 중심 | graphify 기반 아키텍처 분석 연동 | 구조적 영향도 파악 강화 |
| Graphify 통합 | 없음 | Project Knowledge Base 섹션 신설 | GRAPH_REPORT.md 및 MCP 도구 활용 |
| 행동 프로토콜 | 일반적인 Tier 기반 절차 | Behavioral Protocols 추가 | Read-Before-Write, Surgical Precision 명문화 |
| 사이드 이펙트 분석 | 일반적인 체크리스트 | 구체적인 자가 분석 질문(Self-Questions) 추가 | 하위 호환성 및 상태 무결성 검증 강화 |
| 위험 지역 관리 | 없음 | High-Risk Change Zones 정의 | security, config, domain, service 영역 지정 |
| 긴급 대응 절차 | Tier 절차 고수 | Emergency Protocol 예외 조항 신설 | 장애 및 빌드 실패 시 신속한 대응 허용 |

## 3. 세부 변경 내용 및 의도

### 3.1. 시니어 아키텍트 행동 강령 (Behavioral Protocols) 도입
- 내용: Read-Before-Write(쓰기 전 읽기), Surgical Precision(수술적 정밀도) 원칙을 추가했습니다.
- 의도: 불필요한 코드 수정을 최소화하고, 수정 전 호출부와 의존성을 명확히 파악하여 레거시 무결성을 보존하기 위함입니다.

### 3.2. 프로젝트 특화 위험 지역 (High-Risk Zones) 명시
- 내용: 이 프로젝트의 핵심인 security, config, domain, service 패키지를 고위험 지역으로 정의했습니다.
- 의도: 해당 영역 수정 시 에이전트가 더 높은 수준의 주의를 기울이고 전수 테스트를 수행하도록 유도합니다.

### 3.3. 사이드 이펙트 자가 진단 질문 추가
- 내용: 직접 호출자 확인, 하위 호환성 유지 여부, 데이터 무결성 영향 등 4가지 핵심 질문을 추가했습니다.
- 의도: 에이전트가 수동적으로 체크리스트를 채우는 것이 아니라, 구조적인 관점에서 변경의 위험성을 스스로 검토하게 합니다.

### 3.4. Graphify 및 긴급 프로토콜 통합
- 내용: graphify 지식 그래프 활용법과 긴급 상황 발생 시의 행동 요령을 명문화했습니다.

## 4. 기대 효과
- 레거시 코드 보존: 수술적 정밀도 원칙을 통해 기존 로직에 미치는 영향을 최소화합니다.
- 아키텍처 무결성 강화: 지식 그래프와 고위험 지역 정의를 통해 시스템 전체의 안정성을 확보합니다.
- 작업 효율성 및 안전성 양립: 긴급 프로토콜을 통한 신속한 대응과 자가 진단을 통한 안전 장치를 동시에 확보합니다.
