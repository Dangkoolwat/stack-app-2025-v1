---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 문제 분석

## 문제 현상

Apple Silicon 환경에서 통합 테스트 실행 시 Oracle 테스트 이미지가 다음 경고를 출력했다.

- `The architecture 'amd64' for image 'gvenzl/oracle-xe:21-slim-faststart' does not match the Docker server architecture 'arm64'`

이 경고는 x86 이미지가 ARM Docker 환경에서 에뮬레이션으로 실행된다는 의미이며, 실제로 컨테이너 시작 속도 저하와 타임아웃 위험을 유발할 수 있다.

## 원인

현재 테스트 컨테이너 설정이 `gvenzl/oracle-xe:21-slim-faststart`를 사용하고 있었고, 해당 이미지 계열은 ARM 네이티브 실행에 적합하지 않았다.

## 추가 확인 사항

`gvenzl/oracle-free:23-slim-faststart`로 단순 교체만 하면 `OracleContainer` 호환성 검증과 기본 PDB 생성 방식 차이 때문에 바로 실패했다.

따라서 다음 두 보정이 필요했다.

1. `asCompatibleSubstituteFor("gvenzl/oracle-xe")`
2. `withDatabaseName(...)` 제거

