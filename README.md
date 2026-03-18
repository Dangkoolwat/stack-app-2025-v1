# 🚀 Stack App 2025 v1
Spring Boot 4.x 기반 REST API 서버 프레임워크  
개인 개발자가 온프레미스(On-Prem) 또는 클라우드(OCI/Azure) 환경에서 빠르게 확장 가능한 백엔드 서비스를 구축하기 위한 Full-Stack Starter Project (Spring Boot 4 + Vue 3)입니다.

현재 Oracle Cloud VM 환경에서 운영 중인 구조를 최신 트렌드에 맞게 개편하고 있으며,  
실제 프로젝트 경험을 기반으로 생산성과 유지보수성을 극대화하도록 설계했습니다.

---

## 📑 목차 (Table of Contents)
- 프로젝트 목적
- 주요 특징
- 기술 스택
- 향후 계획
- 설치 및 실행
- 안내 메시지

---

## 📖 프로젝트 목적 (Project Goals)

### 1) API 일관성
- RFC 7807 기반 공통 에러 응답 구조
- 모든 클라이언트(Web/Flutter/React/Native/macOS)에서 일관성 있는 예외 처리 가능

### 2) 보안 강화
- Spring Security + JWT 기반 인증/인가 적용
- Role 기반 접근 제어, 확장 가능한 사용자 모델

### 3) 성능 최적화
- Hibernate 2nd Cache + Redis 캐시 적용
- 반복 쿼리 감소 및 DB 부하 절감
- 고성능 IoT·Real-time 서비스 환경 대응

### 4) 확장성 고려
- Redis / Kafka / MQTT / Swagger 등 다양한 인프라와 자연스러운 통합
- 모듈 구조 기반으로 기능 확장 및 서비스 추가 용이

### 5) 문서화 자동화
- Swagger(OpenAPI) UI 자동 제공 → API 문서·테스트 통합
- JavaDoc 기반 문서 자동화 구조까지 제공

---

## 🧩 주요 특징 (Key Features)

### ✔ 1. 표준 백엔드 아키텍처 제공
- Controller → Service → Domain → Repository 구조
- 전역 예외 처리, 공통 응답 포맷, Validation, 로깅 기본 내장
- 실서비스 기준의 코드 품질을 템플릿 수준에서 확보

### ✔ 2. 자동 API 문서화
- Swagger(OpenAPI 3) 자동 문서화 제공
- JavaDoc 주석 기반 자동 문서 생성 지원

### ✔ 3. DB 버전 관리 + 고성능 캐시 구조
- Liquibase 기반 DB 스키마 버전관리
- PostgreSQL / MariaDB / MySQL / Oracle DB 호환
- Redis 기반 캐싱·세션·토큰스토리지 확장 가능

### ✔ 4. 클라우드·온프레 모두 즉시 배포 가능
- Oracle Cloud / Azure / On-Prem 환경 모두 프로필 기반 즉시 배포
- Dockerfile + Compose + 배포 스크립트 기본 포함
- HTTPS(SSL-ready) 구성 쉽게 지원

### ✔ 5. 기본 비즈니스 모델 탑재 (Ready-to-Use Modules)
초기 서비스에서 가장 많이 쓰이는 도메인을 기본 내장:

- 회원 관리(User Management)
  - 가입/로그인/권한(Role) 구조
  - JWT 인증/인가 구조 확장 가능
- 게시판 관리(Board/Post)
  - 게시글 CRUD, 카테고리 연동
- 카테고리 관리(Category Management)
  - 계층형 카테고리, 태깅 모델
- 관리자 전용 메트릭 페이지(Admin Metrics)
  - 시스템 상태, DB/Redis 연결, 요청 카운트 등 모니터링

즉, “서비스가 기본적으로 갖춰야 하는 기능”을 반복 구현하지 않아도 된다.

### ✔ 6.Vue3 기반의 프런트엔드 템플릿 제공
- TypeScript, Vite, Pinia 등 최신 기술 스택이 기본으로 설정되어 있어 견고한 프런트엔드 기반을 제공
- 백엔드 Spring Boot API와 연동되는 서비스 레이어와 타입 정의가 포함되어 개발 시간을 획기적으로 단축
- JWT, OAuth2 등 Spring Security 설정과 동기화된 로그인, 권한 제어, 세션 관리 기능이 클라이언트 단에 미리 구현
- i18next 라이브러리를 통해 한국어를 포함한 다양한 언어 팩을 손쉽게 관리하고 동적으로 전환할 수 있는 구조를 제공
  사용자 관리, 권한 설정, API 문서(Swagger) 확인, 헬스 체크 등 운영에 필요한 관리자 페이지가 기본 제공
- 관리자 페이지, 대시보드, 웹앱 구조를 빠른 구축 및  커스터마이징 가능
---

## 🔧 기술 스택 (Tech Stack)

### Backend
- Framework: Spring Boot 4.x
- Language: Java 21+  
- Build Tool: Maven  
- Database: Oracle / PostgreSQL / MariaDB / MySQL  
- Database Migration: Liquibase  
- Persistence: Spring Data JPA (Hibernate)  
- Caching: Redis (Redisson) + Hibernate 2nd Cache  
- Security: Spring Security, JWT  
- Error Handling: RFC 7807 (ProblemDetail)  
- API Docs: Swagger UI (springdoc-openapi)  
- Testing: JUnit 5, Spring Boot Test, MockMvc  

### Frontend
- Framework: Vue 3
- Build Tool: Vite
- Language: TypeScript
- State Management: Pinia
- UI Components: Bootstrap-Vue-Next / Bootswatch
- Testing: Vitest

### Infrastructure & DevOps
- Deployment Target: Oracle Cloud Infrastructure (OCI) VM  
- Container: Docker, Docker Compose
- Messaging Queue: MQTT Integration (Eclipse Paho)
- CI/CD: GitHub Actions (ready) / SonarQube

---

## 🛠 향후 계획 (Future Plans)

### 실시간 양방향 통신 (WebSocket)
- 서버 ↔ 클라이언트 실시간 데이터 동기화, 알림·모니터링 기능 강화

### Event-Driven 아키텍처
- Loosely Coupled 구조 → 대규모 서비스 확장성 확보

### 외부 시스템 통합
- Granpada 등 외부 플랫폼과의 안정적 연동

### MQTT 기반 IoT 메시징 강화
- 다양한 디바이스 환경 대응
- 센서 데이터 수집/모니터링 용이

### 멀티 플랫폼 클라이언트 제공 (개별 프로젝트로 진행)
- Windows / macOS 데스크톱 클라이언트
- iOS / Android 모바일 앱

### Spring AI 활용
- RAG(Retrieval-Augmented Generation)를 도입하여 사내 문서 및 DB 데이터를 기반으로 정교한 답변을 제공하는 맞춤형 지식 베이스를 구축
- LLM의 Function Calling 기능을 활용해 사용자의 자연어 요청을 실시간 DB 쿼리나 API 호출로 변환하여 업무 자동화를 실현
- 멀티모달 모델을 연동하여 텍스트뿐만 아니라 이미지 분석, 음성 인식(STT), 요약 등 고도화된 콘텐츠 처리 기능을 제공
- Spring AI의 추상화 레이어를 활용해 OpenAI, Gemini, 로컬 LLM(Ollama) 등 다양한 모델을 유연하게 교체하며 최적의 비용 대비 성능을 확보
- 
---

## ⚡ 설치 및 실행

```bash
# 프로젝트 클론
git clone https://github.com/username/stack-app-2025.git
cd stack-app-2025

# 빌드 및 실행
./mvnw clean package
java -jar target/app.jar
```

---

## 📜 안내 (About)
이 프로젝트는 개발자로서 쌓아온 경험과 생산성을 정리한  
개인 Project이자, Full-Stack Starter Framework입니다.  
온프레미스와 클라우드 어디서나 빠르게 서비스를 구축할 수 있도록 설계되어 있으며,  
앞으로도 지속적으로 개선될 예정입니다.

