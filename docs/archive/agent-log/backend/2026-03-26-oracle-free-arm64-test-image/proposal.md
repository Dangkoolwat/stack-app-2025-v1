---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 해결 방안 제안

## 선택한 방안

테스트용 Oracle 이미지를 `gvenzl/oracle-free:23-slim-faststart`로 전환하고, Testcontainers가 이를 `oracle-xe` 계열 대체 이미지로 받아들이도록 호환 선언을 추가한다.

그리고 Oracle Free 이미지가 기본으로 PDB를 생성하므로, 중복 생성 충돌을 막기 위해 `withDatabaseName(...)`는 제거한다.

## 선택 이유

- Apple Silicon에서 `amd64 on arm64` 경고를 제거할 수 있다.
- 테스트 시작 속도를 개선할 수 있다.
- 현재 저장소의 개발 환경 문서도 Oracle Free/FREEPDB1 계열을 이미 다루고 있어 방향성이 크게 어긋나지 않는다.

## 대안

기존 `oracle-xe` 이미지를 유지하고 외부 Oracle 서버를 사용하게 하는 방법도 있다.

채택하지 않은 이유

- 이번 요청은 테스트 이미지 자체를 다른 것으로 대체하는 방향에 있었다.
- 외부 Oracle 사용은 별도 연결 정책과 데이터 격리 설계가 필요하다.

