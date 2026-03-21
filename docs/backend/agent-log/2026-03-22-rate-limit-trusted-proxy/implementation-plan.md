# 구현 계획 (Implementation Plan)

## 1. `application-dev.yml` 수정
- `server.tomcat.remoteip.trusted-proxies` 속성에 개발 로컬용으로 `127.0.0.1, 0:0:0:0:0:0:0:1` 및 사설 IP 대역 `10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16` 콤마 분리 할당 추가.

## 2. `application-prod.yml` 수정
- 운영/상용 환경에서도 LB가 내부망 사설 구조로 동작하므로 `server.tomcat.remoteip.trusted-proxies` 아래 동일한 신뢰 서브넷(Subnet) 구성 세팅 삽입.

## 3. 설정 반영 및 문법 체크
- YAML 들여쓰기 문법 오류가 없는지 `server: tomcat: remoteip:` 계층을 꼼꼼하게 검토.
- `mvnw test` 를 통해 Spring Boot 실행 문맥(Environment Bind)이 깨지지 않았는지 확인하며 성공 유무 확정.
