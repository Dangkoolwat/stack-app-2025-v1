# 🚀 Stack App 2025 v1

Spring Boot 3.x 기반 REST API 서버 프로젝트  
다양한 클라이언트(Web, Mobile, Desktop)에서 공통으로 활용 가능한 표준화된 API 백엔드 구축을 목표로 합니다.  
현재 Oracle Cloud VM 환경에서 운영 중인 모듈을 최신 트렌드에 맞게 개선하고 있으며, 전반적인 구성이 Oracle Cloud 환경 최적화에 초점을 맞추고 있습니다.

---

## 📑 목차 (Table of Contents)
- [프로젝트 목적](#-프로젝트-목적)
- [기술 스택](#-기술-스택)
- [향후 계획](#-향후-계획)
- [설치 및 실행](#-설치-및-실행)
- [라이선스](#-라이선스)

---

## 📖 프로젝트 목적
- API 일관성: RFC 7807 기반 에러 응답 → 클라이언트 예외 처리 단일화
- 보안 강화: Spring Security + JWT 인증/인가 적용 → 안전한 API 서비스 제공
- 성능 최적화: Hibernate 2차 캐시 + Redis 연동 → 반복 쿼리 성능 개선
- 확장성 고려: Redis, Kafka, Mqtt, Swagger 등 인프라·툴과의 통합 가능
- 문서화 자동화: Swagger(OpenAPI) UI → API 문서 제공

---

## ⚙️ 기술 스택
- Framework: Spring Boot 3.5.x
- Language: Java 17+
- Build Tool: Maven
- Database: Oracle (개발 시 Docker 사용)
- Persistence: Spring Data JPA (Hibernate)
- Caching: Hibernate 2차 캐시 + Redis (Docker 기반)
- Security: Spring Security, JWT
- Error Handling: RFC 7807 (ProblemDetail)
- API Docs: Swagger UI (springdoc-openapi)
- Testing: JUnit 5, Spring Boot Test, MockMvc
- Deployment Target: Oracle Cloud Infrastructure (OCI) VM
- DevOps: Docker, GitHub
- Messaging Queue: Mqtt Integration

---

## 🛠 향후 계획 (Future Plans)
- 실시간 양방향 통신 (WebSocket)  
  서버와 클라이언트 간 실시간 데이터 교환 및 상호작용 강화

- Event-Driven 아키텍처  
  Loosely Coupled 이벤트 처리 구조 → 서비스 유연성과 확장성 확보

- 외부 시스템 연동  
  Granpada 등 외부 플랫폼과의 안정적인 연동을 통해 기능 및 데이터 교류 확장

- MQTT 기반 메시징 강화  
  IoT 및 경량 메시징 환경에 최적화된 MQTT 프로토콜을 활용하여 다양한 디바이스와 안정적 통신 구현

- 멀티 플랫폼 클라이언트 제공
  - 데스크톱 애플리케이션: Windows / macOS용 클라이언트 개발, 업무 환경에 최적화된 UI 제공
  - 모바일 애플리케이션: iOS(iPhone) 및 Android 클라이언트 제작 → 이동 중에도 서비스 접근성 및 편의성 보장

---

## ⚡ 설치 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/username/stack-app-2025.git
cd stack-app-2025

# 빌드 및 실행
./mvnw clean package
java -jar target/app.jar
