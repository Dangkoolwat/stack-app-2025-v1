---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 작업 흐름

## 1. 이미지 전환

`IntegrationTestContainers.java`의 Oracle 이미지를 `gvenzl/oracle-xe:21-slim-faststart`에서 `gvenzl/oracle-free:23-slim-faststart`로 바꿨다.

## 2. 첫 실패 보정

처음에는 `OracleContainer`가 `oracle-free`를 바로 허용하지 않아 호환성 예외가 발생했다.

이를 해결하기 위해 `DockerImageName.parse(...).asCompatibleSubstituteFor("gvenzl/oracle-xe")`를 적용했다.

## 3. 두 번째 실패 보정

이후 Oracle Free가 기본 생성하는 PDB와 `withDatabaseName("FREEPDB1")`가 충돌해 컨테이너가 종료되었다.

그래서 `withDatabaseName(...)`를 제거했다.

## 4. 검증

재검증 결과 대표 관리자 IT 두 개가 모두 통과했고, 로그에서 기존 `amd64 on arm64` 경고는 보이지 않았다.

또한 Oracle Free 컨테이너는 약 12초 정도에 정상 시작되었다.

