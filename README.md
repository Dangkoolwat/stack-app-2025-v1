# 🚀 Stack App 2025 v1

## Who is this project for?

This project is for individual developers, small teams, and learners who want to start a production-oriented full-stack application without rebuilding the same backend and frontend foundation every time. It provides a practical starter structure based on Spring Boot 4 and Vue 3, including authentication, database migration, API documentation, caching, Docker-based local development, and cloud deployment readiness. The goal is to help developers move faster while keeping the project structure maintainable, testable, and suitable for real-world services.

Spring Boot 4.x 기반 REST API 서버 프레임워크입니다. 개발자가 온프레미스 (On-Prem) 나 클라우드 (OCI/Azure) 환경에서 빠르게 확장 가능한 백엔드 서비스를 구축할 수 있도록 설계한 Full-Stack Starter Project(Spring Boot 4 + Vue 3) 입니다.

현재 Oracle Cloud VM 환경에서 운영 중인 구조를 최신 트렌드에 맞게 개편하고 있으며, 실제 프로젝트 경험을 기반으로 생산성과 유지보수성을 극대화하도록 설계했습니다.

---

## 📑 목차 (Table of Contents)

- 프로젝트 목적
- 주요 특징
- 최근 업데이트 (Migration Highlights)
- 기술 스택
- 향후 계획
- 설치 및 실행
- 안내 메시지

---

## 📖 프로젝트 목적 (Project Goals)

### 1) API 일관성

- RFC 7807 기반 공통 에러 응답 구조를 제공하여 모든 클라이언트 (Web/Flutter/React/Native/macOS) 에서 일관성 있는 예외 처리를 지원합니다.

### 2) 보안 강화

- Spring Security + JWT 기반 인증/인가 적용
- 역할 (Role) 기반 접근 제어 및 확장 가능한 사용자 모델

### 3) 성능 최적화

- Hibernate 2 차 캐시 + Redis 캐시 적용으로 반복 쿼리를 줄이고 DB 부하를 낮춰 고성능 IoT·실시간 서비스 환경에 대응합니다.

### 4) 확장성 고려

- Redis, Kafka, MQTT, Swagger 등 다양한 인프라와 자연스럽게 통합되도록 모듈 구조를 설계하여 기능 확장 및 서비스 추가가 용이합니다.

### 5) 문서화 자동화

- Swagger(OpenAPI) UI 를 자동 제공해 API 문서·테스트를 통합하고, JavaDoc 기반 문서 자동화 구조까지 제공합니다.

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

- Liquibase 기반 DB 스키마 버전 관리
- PostgreSQL / MariaDB / MySQL / Oracle DB 호환
- Redis 기반 캐싱·세션·토큰스토리지 확장 가능

### ✔ 4. 클라우드·온프레 모두 즉시 배포 가능

- Oracle Cloud / Azure / On-Prem 환경 모두 프로필 기반으로 즉시 배포 가능
- Dockerfile + Compose + 배포 스크립트를 기본 포함하여 HTTPS(SSL-ready) 구성을 쉽게 지원

### ✔ 5. 기본 비즈니스 모델 탑재 (Ready-to-Use Modules)

회원 관리 (User Management), 게시판 (Board/Post), 카테고리 (Category) 등 초기 서비스에서 가장 많이 쓰이는 도메인을 기본 내장하여 반복 구현을 줄입니다.

### ✔ 6. Vue 3 기반 프런트엔드 템플릿 제공

TypeScript, Vite, Pinia 등 최신 기술 스택이 기본으로 설정된 Vue 3 템플릿을 제공하며, 백엔드 Spring Boot API 와 연동되는 서비스 레이어 및 타입 정의가 포함되어 개발 시간을 단축합니다. 관리자 페이지, 대시보드, 권한 관리 등이 기본으로 제공되어 빠르게 커스터마이징할 수 있습니다.

### ✔ 7. 체계적인 파일 저장 구조 (Local & Cloud Ready)

로컬 및 클라우드 (OCI, Azure, S3), 공유 폴더 (NFS/SMB) 환경 어디서나 동일한 설정 기반으로 동작하며, 모든 파일은 체계적인 디렉토리 구조로 자동 저장됩니다.

```
uploads/
├── public/                    # 공개 파일 (웹 접근 가능)
│   └── {storageKey}/          # 용도별 분류 (NOTICE, PROFILE, BOARD 등)
│       └── yyyy/MM/           # 연/월 기준 날짜 분할
│           └── {timestamp}_{uuid}.{ext}
└── private/                   # 비공개 파일 (인증 필요)
    └── {storageKey}/
        └── yyyy/MM/
            └── {timestamp}_{uuid}.{ext}
```

- 환경별 저장소: `LOCAL`(실행 경로), `SHARE`(공유 폴더), `CLOUD_S3`, `CLOUD_OCI` 지원
- 자동 디렉토리 생성: 파일 업로드 시 용도 (storageKey) 와 날짜 (yyyy/MM) 기준 폴더 자동 생성
- 파일명 규칙: `yyyyMMddHHmmss_uuid.ext` 형식으로 중복 없는 안전한 파일명 자동 생성
- 공개/비공개 분리: 접근 권한에 따라 물리 경로를 분리하여 보안 강화
- Apache Tika 기반 MIME 타입 검증: 파일 확장자 위조를 방지하기 위해 실제 바이너리 콘텐츠를 분석하여 허용된 타입만 저장
- 스트리밍 전송: 대용량 파일도 `InputStream.transferTo()` 로 OOM 위험 없이 안전 전송

### ✔ 8. 다층 보안 아키텍처 (Multi-Layer Security)

- Rate Limiting: 토큰 버킷 알고리즘 기반 IP/엔드포인트별 요청 제한 (로그인, 회원가입, OTP 등)
- Trusted Proxy 지원: L4/L7 프록시 환경에서 `X-Forwarded-For` 헤더 기반 클라이언트 IP 식별
- CSP (Content Security Policy): XSS 공격 방지를 위한 nonce 기반 스크립트 실행 제한
- JWT 보안: Bearer Token 기반 상태 비저장 (STATELESS) 인증, 512-bit Base64 시크릿 지원
- 파일 업로드 보안: MIME 타입 화이트리스트, 확장자 제한, Apache Tika 기반 콘텐츠 검증

---

### ✔ 9. 동적 파일 업로드 정책 & 템플릿 관리
- 실시간 정책 변경: 서버 재시작 없이 관리자 UI에서 파일 확장자, MIME 타입, 최대 용량 제한을 실시간으로 변경하고 즉시 적용 가능합니다.
- 추천 템플릿 시스템: MS 오피스, 한글(HWP, HWPX), PDF, 이미지, 비디오, 압축 파일 등 자주 사용되는 10여 종의 정책을 템플릿으로 제공하여 원클릭으로 추가할 수 있습니다.
- MIME 타입 입력 가이드: 일반 사용자가 알기 어려운 MIME 타입 및 확장자 조합(예: Adobe Photoshop, 한컴오피스 등)에 대한 가이드를 UI 내에 내장하여 편의성을 극대화했습니다.
- 표준값 복원 기능: 언제든지 시스템 표준 초기 템플릿 목록으로 복원할 수 있는 기능을 제공하여 관리 실수를 방지합니다.

---

## ✨ 2026 년 3 월 업데이트 (Migration Highlights)

2026 년 3 월에 진행된 주요 시스템 고도화 및 보안 강화 사항입니다.

- Jackson 3 통합: JSON 직렬화 엔진을 Jackson 3 로 통일하여 Spring Boot 4 환경 최적화
- 동적 파일 업로드 정책 및 템플릿 시스템: 관리자 UI에서 실시간으로 업로드 허용 범위 제어 (PSD, HWP 등 특수 타입 대응 가이드 및 원클릭 복원 제공)
- Apache Tika 기반 MIME 검증: 실제 바이너리 콘텐츠 분석으로 파일 확장자 위조 방지 (보안 강화)
- 다층 보안 체계: Rate Limiting, CSP Nonce, Trusted Proxy 지원
- HikariCP 동적 풀: CPU 코어 기반 자동 계산으로 환경별 최적 DB 연결 풀 유지
- 가상 스레드 활성화: Java 21 가상 스레드로 동시성 처리 성능 향상
- 글로벌 Settings JSON 통합: 단일 JSON 필드로 스키마 변경 없이 유연한 설정 확장

---

## 🔧 기술 스택 (Tech Stack)

### Backend

- Framework: Spring Boot 4.x (가상 스레드, JSpecify 지원)
- Language: Java 21+
- Build Tool: Maven
- Database: Oracle / PostgreSQL / MariaDB / MySQL
- Database Migration: Liquibase
- Persistence: Spring Data JPA (Hibernate 7.x)
- Caching: Redis (Redisson) + Hibernate 2nd Cache
- JSON Engine: Jackson 3 (Unification)
- Security: Spring Security, JWT, Rate Limiting
- Error Handling: RFC 7807 (ProblemDetail)
- Cloud Storage: AWS S3, Oracle Cloud Infrastructure (OCI)
- File Analysis: Apache Tika

### Frontend

- Framework: Vue 3
- Build Tool: Vite
- Language: TypeScript
- State Management: Pinia
- UI Components: Bootstrap-Vue-Next / Bootswatch
- Testing: Vitest

---

## ⚡ 설치 및 실행 (Installation & Execution)

### 1) 백엔드 실행 (Backend)
로컬 환경에서 다음 커맨드로 서버를 실행합니다.
```bash
./springboot
```

### 2) 프런트엔드 실행 (Frontend)
클라이언트 개발 서버를 실행합니다.
```bash
npm start
```

### 3) 접속 정보
- 사용자/관리자 페이지: [https://localhost:9000](https://localhost:9000)
- API 문서 (Swagger): [https://localhost:8443/swagger-ui/index.html](https://localhost:8443/swagger-ui/index.html)

---

### 4) 개발 환경에서 데이터 초기화

```bash
./mvnw liquibase:dropAll liquibase:update -Pdev
```
---

## 📜 안내 (About)
이 프로젝트는 Spring Boot 4와 Vue 3 기반의 Full-Stack Starter Framework입니다.

개인 개발자와 소규모 팀이 반복적인 초기 설정을 줄이고, 인증, 데이터베이스 마이그레이션, API 문서화, 캐시, Docker 기반 개발 환경, 클라우드 배포 준비까지 한 번에 시작할 수 있도록 구성했습니다.

온프레미스와 클라우드 환경 모두를 고려해 설계했으며, 실제 서비스 개발에 가까운 구조를 유지하면서도 학습과 확장이 쉬운 오픈소스 템플릿으로 발전시키는 것을 목표로 합니다.

앞으로 문서, 예제, 배포 가이드, 테스트, AI 에이전트 기반 유지관리 워크플로를 지속적으로 개선해 나갈 예정입니다.
