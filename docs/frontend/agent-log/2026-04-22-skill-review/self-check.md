---
agent: Gemini 3 Flash
created_at: 2026-04-22 (수요일)
language: ko
---

# Self-Check

## 아키텍처 적합성
- [x] 프로젝트의 핵심 스택(Spring Boot 4, Vue 3)을 지원하는 스킬들이 포함되었는가?
- [x] 시니어 아키텍트 원칙(Legacy-First, 정밀 수정)에 부합하는 분석인가?

## 보안 및 안정성
- [x] 스킬 제거 시 프로젝트 소스 코드에 직접적인 영향이 없는가? (로컬 에이전트 설정일 뿐이므로 안전함)

## 영향도 분석
- [x] 기존 `graphify` 및 `AGENTS.md` 체계와 충돌하지 않는가?
- [x] 한국어 기반 로그 및 주석 정책을 준수하는가?

## 테스트 및 검증
- [x] `package.json` 및 `pom.xml`을 통해 실제 의존성을 전수 조사하였는가?
- [x] 이전 대화 기록을 통해 사용자의 잠재적 요구사항(FFmpeg, Whisper 등)을 확인하였는가?
